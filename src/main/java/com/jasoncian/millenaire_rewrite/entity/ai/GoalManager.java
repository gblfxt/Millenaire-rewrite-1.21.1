package com.jasoncian.millenaire_rewrite.entity.ai;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 目标管理器 - 管理村民的AI目标选择和执行
 *
 * 职责：
 * - 选择下一个最优目标
 * - 跟踪当前目标状态
 * - 处理目标转换
 * - 管理路径和卡住检测
 *
 * @author Based on OldSource MillVillager goal logic
 * @version 1.0.0
 */
public class GoalManager {

    // ================ 所属村民 ================

    private final MillVillager villager;

    // ================ 当前目标状态 ================

    /** 当前目标 */
    @Nullable
    private MillGoal currentGoal;

    /** 当前目标状态 */
    private GoalState state = GoalState.INACTIVE;

    /** 当前目标信息 */
    @Nullable
    private GoalInformation goalInfo;

    /** 目标开始时间 */
    private long goalStartTick = 0;

    /** 卡住计时器 */
    private int stuckTicks = 0;

    /** 上一次位置（用于卡住检测） */
    @Nullable
    private BlockPos lastPosition;

    // ================ 可用目标列表 ================

    /** 村民可用的目标列表 */
    private final List<MillGoal> availableGoals = new ArrayList<>();

    // ================ 构造函数 ================

    public GoalManager(MillVillager villager) {
        this.villager = villager;
    }

    // ================ 目标管理 ================

    /**
     * 添加可用目标
     */
    public void addGoal(MillGoal goal) {
        if (!availableGoals.contains(goal)) {
            availableGoals.add(goal);
        }
    }

    /**
     * 移除可用目标
     */
    public void removeGoal(MillGoal goal) {
        availableGoals.remove(goal);
    }

    /**
     * 清空可用目标
     */
    public void clearGoals() {
        availableGoals.clear();
    }

    /**
     * 设置可用目标列表
     */
    public void setGoals(List<MillGoal> goals) {
        availableGoals.clear();
        availableGoals.addAll(goals);
    }

    // ================ 目标选择 ================

    /**
     * 选择下一个目标
     * 基于优先级和可执行性选择最优目标
     */
    public void selectNextGoal() {
        // 清除当前目标
        if (currentGoal != null) {
            currentGoal.onInterrupt(villager);
        }

        currentGoal = null;
        goalInfo = null;
        state = GoalState.INACTIVE;

        // 分离休闲和非休闲目标
        List<MillGoal> leisureGoals = new ArrayList<>();
        List<MillGoal> nonLeisureGoals = new ArrayList<>();

        for (MillGoal goal : availableGoals) {
            if (goal.isPossible(villager)) {
                if (goal.isLeisure()) {
                    leisureGoals.add(goal);
                } else {
                    nonLeisureGoals.add(goal);
                }
            }
        }

        // 优先选择非休闲目标
        MillGoal selectedGoal = selectHighestPriority(nonLeisureGoals);

        // 如果没有非休闲目标，选择休闲目标
        if (selectedGoal == null) {
            selectedGoal = selectHighestPriority(leisureGoals);
        }

        // 接受选中的目标
        if (selectedGoal != null) {
            acceptGoal(selectedGoal);
        }
    }

    /**
     * 从列表中选择最高优先级的目标
     */
    @Nullable
    private MillGoal selectHighestPriority(List<MillGoal> goals) {
        if (goals.isEmpty()) {
            return null;
        }

        MillGoal best = null;
        int bestPriority = Integer.MIN_VALUE;

        for (MillGoal goal : goals) {
            int priority = goal.priority(villager);
            if (priority > bestPriority) {
                bestPriority = priority;
                best = goal;
            }
        }

        return best;
    }

    /**
     * 接受目标并开始执行
     */
    private void acceptGoal(MillGoal goal) {
        currentGoal = goal;
        goalInfo = goal.getDestination(villager);
        state = GoalState.TRAVELING;
        goalStartTick = villager.level().getGameTime();
        stuckTicks = 0;
        lastPosition = null;

        // 调用目标的接受回调
        goal.onAccept(villager);

        // 更新村民的目标键
        villager.setCurrentGoalKey(goal.getKey());

        // 如果需要加速
        if (goal.isSprint()) {
            // TODO: 应用速度加成
        }

        // 重置持有物品
        villager.resetHeldItems();

        MillenaireRewrite.LOGGER.debug("Villager {} accepted goal: {}",
            villager.getFullName(), goal.getKey());
    }

    // ================ 目标执行 ================

    /**
     * 每tick更新目标
     */
    public void tick() {
        if (currentGoal == null || state == GoalState.INACTIVE) {
            // 没有目标，尝试选择新目标
            selectNextGoal();
            return;
        }

        // 检查目标是否仍然有效
        if (!currentGoal.isStillValid(villager)) {
            completeGoal(GoalState.FAILED);
            return;
        }

        // 检查是否应该被更高优先级目标打断
        if (currentGoal.isLeisure() && shouldInterrupt()) {
            selectNextGoal();
            return;
        }

        // 根据状态执行
        switch (state) {
            case TRAVELING -> tickTraveling();
            case PERFORMING -> tickPerforming();
            default -> {}
        }

        // 更新持有物品
        updateHeldItems();
    }

    /**
     * 旅行状态tick
     */
    private void tickTraveling() {
        if (goalInfo == null || !goalInfo.hasDestination()) {
            completeGoal(GoalState.FAILED);
            return;
        }

        BlockPos dest = goalInfo.getEffectiveDestination();
        if (dest == null) {
            completeGoal(GoalState.FAILED);
            return;
        }

        double distance = villager.distanceToSqr(dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5);
        double range = currentGoal.getRange();

        // 检查是否到达目的地
        if (distance <= range * range) {
            state = GoalState.PERFORMING;
            stuckTicks = 0;
            return;
        }

        // 检查是否卡住
        checkStuck();

        // 移动到目的地由MillVillager的导航系统处理
    }

    /**
     * 执行状态tick
     */
    private void tickPerforming() {
        if (currentGoal == null) {
            completeGoal(GoalState.FAILED);
            return;
        }

        // 执行目标动作
        boolean completed = currentGoal.performAction(villager);

        if (completed) {
            completeGoal(GoalState.COMPLETED);
        }
    }

    /**
     * 检查是否卡住
     */
    private void checkStuck() {
        BlockPos currentPos = villager.blockPosition();

        if (lastPosition != null && lastPosition.equals(currentPos)) {
            stuckTicks++;

            if (stuckTicks >= currentGoal.getStuckDelay()) {
                // 触发卡住动作
                currentGoal.stuckAction(villager);
                stuckTicks = 0;
            }
        } else {
            stuckTicks = 0;
        }

        lastPosition = currentPos;
    }

    /**
     * 检查是否应该被打断
     */
    private boolean shouldInterrupt() {
        // 检查是否有更高优先级的非休闲目标可用
        for (MillGoal goal : availableGoals) {
            if (!goal.isLeisure() && goal.isPossible(villager)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 完成目标
     */
    private void completeGoal(GoalState finalState) {
        if (currentGoal != null) {
            switch (finalState) {
                case COMPLETED -> currentGoal.onComplete(villager);
                case FAILED -> currentGoal.onFail(villager);
                case INTERRUPTED -> currentGoal.onInterrupt(villager);
                default -> {}
            }

            MillenaireRewrite.LOGGER.debug("Villager {} completed goal {} with state: {}",
                villager.getFullName(), currentGoal.getKey(), finalState);
        }

        state = finalState;
        currentGoal = null;
        goalInfo = null;
        villager.setCurrentGoalKey("");

        // 立即尝试选择新目标
        if (finalState != GoalState.INTERRUPTED) {
            selectNextGoal();
        }
    }

    /**
     * 更新持有物品
     */
    private void updateHeldItems() {
        if (currentGoal == null) {
            return;
        }

        ItemStack[] mainHand;
        ItemStack[] offHand;

        if (state == GoalState.TRAVELING) {
            mainHand = currentGoal.getHeldItemsTraveling(villager);
            offHand = currentGoal.getHeldItemsOffHandTraveling(villager);
        } else {
            mainHand = currentGoal.getHeldItemsDestination(villager);
            offHand = currentGoal.getHeldItemsOffHandDestination(villager);
        }

        villager.updateHeldItems(mainHand, offHand);
    }

    // ================ Getters ================

    @Nullable
    public MillGoal getCurrentGoal() {
        return currentGoal;
    }

    public GoalState getState() {
        return state;
    }

    @Nullable
    public GoalInformation getGoalInfo() {
        return goalInfo;
    }

    @Nullable
    public BlockPos getDestination() {
        return goalInfo != null ? goalInfo.getEffectiveDestination() : null;
    }

    public long getGoalDuration() {
        return villager.level().getGameTime() - goalStartTick;
    }

    public List<MillGoal> getAvailableGoals() {
        return new ArrayList<>(availableGoals);
    }

    public boolean hasGoal() {
        return currentGoal != null && state.isActive();
    }
}
