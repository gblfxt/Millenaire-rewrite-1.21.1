package com.jasoncian.millenaire_rewrite.entity.ai.goals;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalInformation;
import com.jasoncian.millenaire_rewrite.entity.ai.MillGoal;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import org.jetbrains.annotations.Nullable;

/**
 * 空闲目标 - 村民默认的空闲行为
 *
 * 当没有其他更高优先级的目标时执行
 * 村民会站在原地或轻微移动
 *
 * @author Based on OldSource GoalIdle
 * @version 1.0.0
 */
public class GoalIdle extends MillGoal {

    /** 空闲持续时间（ticks） */
    private int idleDuration = 100;

    /** 当前空闲计时 */
    private int idleTicks = 0;

    public GoalIdle() {
        super("idle");
        this.leisure = true;
        this.tags.add("generic");
        this.tags.add("idle");
        this.range = 0.5;
    }

    @Override
    public int priority(MillVillager villager) {
        // 最低优先级
        return 1;
    }

    @Nullable
    @Override
    public GoalInformation getDestination(MillVillager villager) {
        // 停留在当前位置
        return GoalInformation.ofDestination(villager.blockPosition());
    }

    @Override
    public void onAccept(MillVillager villager) {
        idleTicks = 0;
        villager.setAnimationState(VillagerAnimationState.IDLE);
    }

    @Override
    public boolean performAction(MillVillager villager) {
        idleTicks++;

        // 随机看向周围
        if (villager.getRandom().nextInt(40) == 0) {
            villager.setYRot(villager.getYRot() + (villager.getRandom().nextFloat() - 0.5f) * 30);
        }

        // 空闲一段时间后完成，允许选择其他目标
        return idleTicks >= idleDuration;
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        return true;
    }

    /**
     * 设置空闲持续时间
     */
    public GoalIdle withDuration(int ticks) {
        this.idleDuration = ticks;
        return this;
    }
}
