package com.jasoncian.millenaire_rewrite.entity.ai.goals;

import com.jasoncian.millenaire_rewrite.building.BuildingBlueprint;
import com.jasoncian.millenaire_rewrite.building.BuildingConstructionManager;
import com.jasoncian.millenaire_rewrite.building.BuildingRegistry;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalInformation;
import com.jasoncian.millenaire_rewrite.entity.ai.MillGoal;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import com.jasoncian.millenaire_rewrite.village.BuildingProject;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 建造目标 - 控制建筑工村民的建造行为
 *
 * 行为：
 * 1. 检查是否有活跃建造项目
 * 2. 前往建造地点
 * 3. 放置方块
 * 4. 完成建造
 *
 * @author Based on OldSource building AI
 * @version 1.0.0
 */
public class GoalBuild extends MillGoal {

    // ================ 状态 ================

    /** 目标建造位置 */
    @Nullable
    private BlockPos targetPos;

    /** 当前项目 */
    @Nullable
    private BuildingProject currentProject;

    /** 当前蓝图 */
    @Nullable
    private BuildingBlueprint currentBlueprint;

    /** 当前建造索引 */
    private int buildIndex = 0;

    /** 建造顺序 */
    private List<BlockPos> buildOrder;

    /** 工作计时器 */
    private int workTimer = 0;

    /** 每次放置方块间隔 */
    private static final int BLOCK_PLACE_INTERVAL = 10;

    // ================ 构造函数 ================

    public GoalBuild() {
        super("build");
        this.leisure = false;
        this.tags.add("work");
        this.tags.add("build");
        this.tags.add("profession");
        this.range = 4.0;
        this.canBeDoneInDayTime = true;
        this.canBeDoneAtNight = false;
        this.minimumHour = 0;
        this.maximumHour = 12500;
    }

    // ================ 核心实现 ================

    @Override
    public int priority(MillVillager villager) {
        // 检查是否是建筑相关职业
        if (!isBuildingProfession(villager)) {
            return 0;
        }

        // 检查是否有村庄
        Village village = villager.getHomeVillage();
        if (village == null) {
            return 0;
        }

        // 检查是否有建造项目
        BuildingConstructionManager manager = village.getConstructionManager();
        if (manager == null) {
            return 0;
        }

        BuildingProject project = manager.getActiveProject();
        if (project == null || project.isCompleted() || project.isCancelled()) {
            return 0;
        }

        // 白天工作高优先级
        if (villager.level().isDay()) {
            return 600; // 建造优先级高于一般工作
        }

        return 0;
    }

    /**
     * 检查是否是建筑相关职业
     */
    private boolean isBuildingProfession(MillVillager villager) {
        return switch (villager.getProfession()) {
            case FARMER, LUMBERJACK, MINER, BLACKSMITH, MERCHANT -> true;
            default -> false;
        };
    }

    @Nullable
    @Override
    public GoalInformation getDestination(MillVillager villager) {
        Village village = villager.getHomeVillage();
        if (village == null) return null;

        BuildingConstructionManager manager = village.getConstructionManager();
        if (manager == null) return null;

        currentProject = manager.getActiveProject();
        if (currentProject == null || currentProject.isCompleted() || currentProject.isCancelled()) {
            return null;
        }

        // 获取蓝图
        currentBlueprint = BuildingRegistry.getInstance().getBlueprint(currentProject.getPlanKey());
        if (currentBlueprint == null) {
            return null;
        }

        // 设置目标位置
        targetPos = currentProject.getTargetPos();
        return GoalInformation.ofDestination(targetPos);
    }

    @Override
    public void onAccept(MillVillager villager) {
        if (currentBlueprint != null) {
            buildOrder = currentBlueprint.getBuildOrder();
            buildIndex = 0;
            workTimer = 0;
        }
        villager.setAnimationState(VillagerAnimationState.WALKING);
    }

    @Override
    public boolean performAction(MillVillager villager) {
        if (currentProject == null || targetPos == null || currentBlueprint == null) {
            return true;
        }

        // 检查项目是否已完成或取消
        if (currentProject.isCompleted() || currentProject.isCancelled()) {
            return true;
        }

        // 设置建造动画
        villager.setAnimationState(VillagerAnimationState.BUILDING);

        // 在范围内，执行建造
        workTimer++;

        if (workTimer >= BLOCK_PLACE_INTERVAL) {
            workTimer = 0;
            boolean done = placeNextBlock(villager);
            if (done) {
                return true;
            }
        }

        return false;
    }

    /**
     * 放置下一个方块
     * @return true if building is complete
     */
    private boolean placeNextBlock(MillVillager villager) {
        if (buildOrder == null || buildIndex >= buildOrder.size()) {
            return true;
        }

        if (!(villager.level() instanceof ServerLevel level)) {
            return true;
        }

        // 找到下一个需要放置的方块
        while (buildIndex < buildOrder.size()) {
            BlockPos relativePos = buildOrder.get(buildIndex);
            BlockState targetState = currentBlueprint.getBlockLayout().get(relativePos);

            if (targetState != null && !targetState.isAir()) {
                BlockPos worldPos = targetPos.offset(relativePos);
                BlockState currentState = level.getBlockState(worldPos);

                // 检查是否需要放置
                if (currentState.isAir() || currentState.canBeReplaced()) {
                    // 放置方块
                    level.setBlock(worldPos, targetState, 3);

                    // 播放动画/效果
                    villager.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

                    buildIndex++;

                    // 更新项目进度
                    int progress = (buildIndex * 100) / buildOrder.size();
                    while (currentProject.getProgress() < progress) {
                        currentProject.addProgress(1);
                    }

                    return false;
                }
            }

            buildIndex++;
        }

        // 建造完成
        if (buildIndex >= buildOrder.size()) {
            currentProject.complete();
            return true;
        }

        return false;
    }

    @Override
    public void onComplete(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        cleanup();
    }

    @Override
    public void onInterrupt(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        cleanup();
    }

    private void cleanup() {
        targetPos = null;
        currentProject = null;
        currentBlueprint = null;
        buildIndex = 0;
        workTimer = 0;
        buildOrder = null;
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        // 项目完成或取消时停止
        if (currentProject == null || currentProject.isCompleted() || currentProject.isCancelled()) {
            return false;
        }

        // 夜晚停止
        if (!villager.level().isDay()) {
            return false;
        }

        return true;
    }

    @Override
    protected boolean isPossibleSpecific(MillVillager villager) {
        if (!villager.level().isDay()) {
            return false;
        }

        if (!isBuildingProfession(villager)) {
            return false;
        }

        Village village = villager.getHomeVillage();
        if (village == null) {
            return false;
        }

        BuildingConstructionManager manager = village.getConstructionManager();
        if (manager == null) {
            return false;
        }

        BuildingProject project = manager.getActiveProject();
        return project != null && !project.isCompleted() && !project.isCancelled();
    }

    @Nullable
    @Override
    public ItemStack[] getHeldItemsDestination(MillVillager villager) {
        // 持有斧头和镐子
        return new ItemStack[]{ new ItemStack(Items.IRON_AXE), new ItemStack(Items.IRON_PICKAXE) };
    }
}
