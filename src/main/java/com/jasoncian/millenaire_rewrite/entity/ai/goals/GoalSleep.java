package com.jasoncian.millenaire_rewrite.entity.ai.goals;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalInformation;
import com.jasoncian.millenaire_rewrite.entity.ai.MillGoal;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 睡眠目标 - 村民在夜间寻找床铺睡觉
 *
 * 高优先级夜间活动
 * 村民会寻找最近的床铺并睡觉直到天亮
 *
 * @author Based on OldSource GoalSleep
 * @version 1.0.0
 */
public class GoalSleep extends MillGoal {

    /** 搜索床的范围 */
    private int searchRadius = 20;

    /** 是否正在睡觉 */
    private boolean isSleeping = false;

    public GoalSleep() {
        super("sleep");
        this.leisure = false;
        this.tags.add("generic");
        this.tags.add("rest");
        this.canBeDoneInDayTime = false;
        this.canBeDoneAtNight = true;
        this.minimumHour = 13000; // 下午7点左右
        this.maximumHour = 23500; // 接近午夜
        this.range = 2.0;
    }

    @Override
    public int priority(MillVillager villager) {
        // 夜间高优先级
        long time = villager.level().getDayTime() % 24000L;
        if (time > 13000 && time < 23500) {
            return 800; // 高优先级
        }
        return 0; // 白天不睡觉
    }

    @Nullable
    @Override
    public GoalInformation getDestination(MillVillager villager) {
        // 首先检查是否有家
        BlockPos housePos = villager.getHousePos();
        if (housePos != null) {
            // 在家附近搜索床
            BlockPos bedPos = findBedNear(villager, housePos, 10);
            if (bedPos != null) {
                return GoalInformation.ofDestination(bedPos);
            }
        }

        // 在附近搜索任意床
        BlockPos bedPos = findBedNear(villager, villager.blockPosition(), searchRadius);
        if (bedPos != null) {
            return GoalInformation.ofDestination(bedPos);
        }

        return null;
    }

    /**
     * 在指定位置附近搜索床
     */
    @Nullable
    private BlockPos findBedNear(MillVillager villager, BlockPos center, int radius) {
        BlockPos closestBed = null;
        double closestDistance = Double.MAX_VALUE;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = villager.level().getBlockState(checkPos);

                    if (state.getBlock() instanceof BedBlock) {
                        // 检查床是否被占用
                        if (!state.getValue(BedBlock.OCCUPIED)) {
                            double dist = villager.distanceToSqr(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                            if (dist < closestDistance) {
                                closestDistance = dist;
                                closestBed = checkPos;
                            }
                        }
                    }
                }
            }
        }

        return closestBed;
    }

    @Override
    public void onAccept(MillVillager villager) {
        isSleeping = false;
    }

    @Override
    public boolean performAction(MillVillager villager) {
        if (!isSleeping) {
            // 开始睡觉
            isSleeping = true;
            villager.setAnimationState(VillagerAnimationState.SLEEPING);
        }

        // 检查是否应该醒来（天亮了）
        long time = villager.level().getDayTime() % 24000L;
        if (time > 0 && time < 12000) {
            // 天亮了，醒来
            return true;
        }

        // 继续睡觉
        return false;
    }

    @Override
    public void onComplete(MillVillager villager) {
        isSleeping = false;
        villager.setAnimationState(VillagerAnimationState.IDLE);
    }

    @Override
    public void onInterrupt(MillVillager villager) {
        isSleeping = false;
        villager.setAnimationState(VillagerAnimationState.IDLE);
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        // 如果天亮了，睡眠不再有效
        long time = villager.level().getDayTime() % 24000L;
        return time > 12500 || time < 100;
    }

    @Override
    protected boolean isPossibleSpecific(MillVillager villager) {
        // 只在夜间可执行
        return !villager.level().isDay();
    }

    /**
     * 设置床搜索范围
     */
    public GoalSleep withSearchRadius(int radius) {
        this.searchRadius = radius;
        return this;
    }
}
