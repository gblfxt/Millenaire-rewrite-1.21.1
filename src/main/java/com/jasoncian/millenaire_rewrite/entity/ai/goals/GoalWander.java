package com.jasoncian.millenaire_rewrite.entity.ai.goals;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalInformation;
import com.jasoncian.millenaire_rewrite.entity.ai.MillGoal;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 漫游目标 - 随机在附近移动
 *
 * 低优先级休闲活动，让村民看起来更有生气
 *
 * @author Based on OldSource GoalWander
 * @version 1.0.0
 */
public class GoalWander extends MillGoal {

    /** 漫游范围（方块） */
    private int wanderRadius = 10;

    /** 漫游时长（ticks） */
    private int wanderDuration = 200;

    private int wanderTicks = 0;

    public GoalWander() {
        super("wander");
        this.leisure = true;
        this.tags.add("generic");
        this.tags.add("movement");
        this.range = 1.5;
    }

    @Override
    public int priority(MillVillager villager) {
        // 非常低的优先级
        return 5;
    }

    @Nullable
    @Override
    public GoalInformation getDestination(MillVillager villager) {
        // 在附近随机选择一个目的地
        BlockPos currentPos = villager.blockPosition();

        for (int attempts = 0; attempts < 10; attempts++) {
            int dx = villager.getRandom().nextInt(wanderRadius * 2 + 1) - wanderRadius;
            int dz = villager.getRandom().nextInt(wanderRadius * 2 + 1) - wanderRadius;

            BlockPos target = currentPos.offset(dx, 0, dz);

            // 调整Y坐标到地面
            target = findGroundLevel(villager, target);

            if (target != null && isValidDestination(villager, target)) {
                return GoalInformation.ofDestination(target);
            }
        }

        // 找不到有效目的地
        return null;
    }

    /**
     * 找到目标位置的地面高度
     */
    @Nullable
    private BlockPos findGroundLevel(MillVillager villager, BlockPos pos) {
        // 向下搜索实心方块
        for (int y = 5; y >= -5; y--) {
            BlockPos checkPos = pos.offset(0, y, 0);
            if (villager.level().getBlockState(checkPos).isSolid() &&
                villager.level().getBlockState(checkPos.above()).isAir()) {
                return checkPos.above();
            }
        }
        return null;
    }

    /**
     * 检查目的地是否有效
     */
    private boolean isValidDestination(MillVillager villager, BlockPos pos) {
        // 检查是否可以站立
        return villager.level().getBlockState(pos).isAir() &&
               villager.level().getBlockState(pos.above()).isAir();
    }

    @Override
    public void onAccept(MillVillager villager) {
        wanderTicks = 0;
        villager.setAnimationState(VillagerAnimationState.WALKING);
    }

    @Override
    public boolean performAction(MillVillager villager) {
        wanderTicks++;

        // 到达目的地后短暂停留然后完成
        villager.setAnimationState(VillagerAnimationState.IDLE);
        return wanderTicks >= 40; // 停留2秒
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        return wanderTicks < wanderDuration;
    }

    /**
     * 设置漫游范围
     */
    public GoalWander withRadius(int radius) {
        this.wanderRadius = radius;
        return this;
    }

    /**
     * 设置漫游时长
     */
    public GoalWander withDuration(int ticks) {
        this.wanderDuration = ticks;
        return this;
    }
}
