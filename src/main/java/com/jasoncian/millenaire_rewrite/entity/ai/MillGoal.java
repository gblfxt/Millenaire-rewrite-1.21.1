package com.jasoncian.millenaire_rewrite.entity.ai;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Millenaire目标基类 - 所有村民AI目标的抽象基类
 *
 * 目标系统特点：
 * - 优先级驱动的目标选择
 * - 状态机管理（旅行中/执行中/完成）
 * - 时间窗口限制
 * - 资源阈值限制
 * - 可中断性控制
 *
 * @author Based on OldSource Goal
 * @version 1.0.0
 */
public abstract class MillGoal {

    // ================ 目标注册表 ================

    /** 所有已注册的目标 */
    private static final Map<String, MillGoal> GOALS = new HashMap<>();

    // ================ 基础属性 ================

    /** 目标唯一标识符 */
    protected final String key;

    /** 目标标签（用于分类） */
    protected final List<String> tags = new ArrayList<>();

    /** 是否为休闲目标（可被非休闲目标打断） */
    protected boolean leisure = false;

    /** 是否在执行时加速移动 */
    protected boolean sprint = false;

    /** 到达目的地的判定距离 */
    protected double range = 2.0;

    /** 卡住后的等待tick数 */
    protected int stuckDelay = 100;

    // ================ 时间限制 ================

    /** 最早开始时间（0-24000的Minecraft时间） */
    protected int minimumHour = 0;

    /** 最晚结束时间（0-24000的Minecraft时间） */
    protected int maximumHour = 24000;

    /** 是否可在白天执行 */
    protected boolean canBeDoneInDayTime = true;

    /** 是否可在夜间执行 */
    protected boolean canBeDoneAtNight = true;

    // ================ 并发限制 ================

    /** 同一建筑内最大同时执行数 */
    protected int maxSimultaneousInBuilding = -1; // -1 = 无限制

    /** 村庄内最大同时执行数 */
    protected int maxSimultaneousTotal = -1;

    // ================ 构造函数 ================

    protected MillGoal(String key) {
        this.key = key;
    }

    // ================ 核心抽象方法 ================

    /**
     * 计算目标优先级
     * 值越高，被选中的概率越大
     *
     * @param villager 执行目标的村民
     * @return 优先级值（通常0-1000）
     */
    public abstract int priority(MillVillager villager);

    /**
     * 获取目标的目的地信息
     * 返回null表示目标无法执行
     *
     * @param villager 执行目标的村民
     * @return 目的地信息，或null
     */
    @Nullable
    public abstract GoalInformation getDestination(MillVillager villager);

    /**
     * 在目的地执行动作
     * 每tick调用直到返回true（完成）
     *
     * @param villager 执行目标的村民
     * @return true表示目标完成，false表示继续执行
     */
    public abstract boolean performAction(MillVillager villager);

    // ================ 可选覆盖方法 ================

    /**
     * 目标被接受时调用
     */
    public void onAccept(MillVillager villager) {
        // 默认无操作
    }

    /**
     * 目标完成时调用
     */
    public void onComplete(MillVillager villager) {
        // 默认无操作
    }

    /**
     * 目标失败时调用
     */
    public void onFail(MillVillager villager) {
        // 默认无操作
    }

    /**
     * 目标被中断时调用
     */
    public void onInterrupt(MillVillager villager) {
        // 默认无操作
    }

    /**
     * 检查目标是否仍然有效
     */
    public boolean isStillValid(MillVillager villager) {
        return true;
    }

    /**
     * 卡住时的恢复动作
     */
    public void stuckAction(MillVillager villager) {
        // 默认无操作
    }

    /**
     * 检查目标特定的可执行条件
     */
    protected boolean isPossibleSpecific(MillVillager villager) {
        return true;
    }

    // ================ 持有物品 ================

    /**
     * 旅途中主手持有的物品
     */
    @Nullable
    public ItemStack[] getHeldItemsTraveling(MillVillager villager) {
        return null;
    }

    /**
     * 目的地主手持有的物品
     */
    @Nullable
    public ItemStack[] getHeldItemsDestination(MillVillager villager) {
        return null;
    }

    /**
     * 旅途中副手持有的物品
     */
    @Nullable
    public ItemStack[] getHeldItemsOffHandTraveling(MillVillager villager) {
        return null;
    }

    /**
     * 目的地副手持有的物品
     */
    @Nullable
    public ItemStack[] getHeldItemsOffHandDestination(MillVillager villager) {
        return null;
    }

    // ================ 条件检查 ================

    /**
     * 检查目标是否可以执行
     * 综合检查时间、资源、特定条件等
     */
    public boolean isPossible(MillVillager villager) {
        // 检查时间限制
        if (!checkTimeRestrictions(villager)) {
            return false;
        }

        // 检查昼夜限制
        if (!checkDayNightRestrictions(villager)) {
            return false;
        }

        // 检查特定条件
        if (!isPossibleSpecific(villager)) {
            return false;
        }

        // 检查是否能获取目的地
        GoalInformation dest = getDestination(villager);
        return dest != null && dest.hasDestination();
    }

    /**
     * 检查时间窗口限制
     */
    protected boolean checkTimeRestrictions(MillVillager villager) {
        long worldTime = villager.level().getDayTime() % 24000L;

        if (minimumHour > 0 && worldTime < minimumHour) {
            return false;
        }
        if (maximumHour < 24000 && worldTime > maximumHour) {
            return false;
        }
        return true;
    }

    /**
     * 检查昼夜限制
     */
    protected boolean checkDayNightRestrictions(MillVillager villager) {
        boolean isDaytime = villager.level().isDay();

        if (isDaytime && !canBeDoneInDayTime) {
            return false;
        }
        if (!isDaytime && !canBeDoneAtNight) {
            return false;
        }
        return true;
    }

    // ================ Getters ================

    public String getKey() {
        return key;
    }

    public List<String> getTags() {
        return Collections.unmodifiableList(tags);
    }

    public boolean isLeisure() {
        return leisure;
    }

    public boolean isSprint() {
        return sprint;
    }

    public double getRange() {
        return range;
    }

    public int getStuckDelay() {
        return stuckDelay;
    }

    // ================ 注册方法 ================

    /**
     * 注册目标到全局注册表
     */
    public static void registerGoal(MillGoal goal) {
        GOALS.put(goal.getKey(), goal);
    }

    /**
     * 获取已注册的目标
     */
    @Nullable
    public static MillGoal getGoal(String key) {
        return GOALS.get(key);
    }

    /**
     * 获取所有已注册的目标
     */
    public static Collection<MillGoal> getAllGoals() {
        return Collections.unmodifiableCollection(GOALS.values());
    }

    /**
     * 根据标签获取目标
     */
    public static List<MillGoal> getGoalsByTag(String tag) {
        List<MillGoal> result = new ArrayList<>();
        for (MillGoal goal : GOALS.values()) {
            if (goal.tags.contains(tag)) {
                result.add(goal);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "MillGoal{" + key + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MillGoal millGoal)) return false;
        return Objects.equals(key, millGoal.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
