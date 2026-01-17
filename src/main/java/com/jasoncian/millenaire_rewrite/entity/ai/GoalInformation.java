package com.jasoncian.millenaire_rewrite.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * 目标信息类 - 包含目标的目的地和上下文信息
 *
 * @author Based on OldSource GoalInformation
 * @version 1.0.0
 */
public class GoalInformation {

    /** 目的地坐标 */
    @Nullable
    private final BlockPos destination;

    /** 关联的建筑位置（可选） */
    @Nullable
    private final BlockPos buildingPos;

    /** 目标实体（可选） */
    @Nullable
    private final Entity targetEntity;

    /** 附加数据（目标特定） */
    @Nullable
    private Object extraData;

    // ================ 构造函数 ================

    private GoalInformation(@Nullable BlockPos destination, @Nullable BlockPos buildingPos,
                           @Nullable Entity targetEntity) {
        this.destination = destination;
        this.buildingPos = buildingPos;
        this.targetEntity = targetEntity;
    }

    // ================ 工厂方法 ================

    /**
     * 创建只有目的地的目标信息
     */
    public static GoalInformation ofDestination(BlockPos destination) {
        return new GoalInformation(destination, null, null);
    }

    /**
     * 创建带建筑上下文的目标信息
     */
    public static GoalInformation ofBuilding(BlockPos destination, BlockPos buildingPos) {
        return new GoalInformation(destination, buildingPos, null);
    }

    /**
     * 创建针对实体的目标信息
     */
    public static GoalInformation ofEntity(Entity entity) {
        return new GoalInformation(entity.blockPosition(), null, entity);
    }

    /**
     * 创建完整的目标信息
     */
    public static GoalInformation of(BlockPos destination, @Nullable BlockPos buildingPos,
                                     @Nullable Entity targetEntity) {
        return new GoalInformation(destination, buildingPos, targetEntity);
    }

    /**
     * 创建空的目标信息（无目的地）
     */
    public static GoalInformation empty() {
        return new GoalInformation(null, null, null);
    }

    // ================ Getters ================

    @Nullable
    public BlockPos getDestination() {
        return destination;
    }

    @Nullable
    public BlockPos getBuildingPos() {
        return buildingPos;
    }

    @Nullable
    public Entity getTargetEntity() {
        return targetEntity;
    }

    @Nullable
    public Object getExtraData() {
        return extraData;
    }

    public GoalInformation withExtraData(Object data) {
        this.extraData = data;
        return this;
    }

    /**
     * 检查是否有有效目的地
     */
    public boolean hasDestination() {
        return destination != null;
    }

    /**
     * 检查是否有目标实体
     */
    public boolean hasTargetEntity() {
        return targetEntity != null && targetEntity.isAlive();
    }

    /**
     * 获取有效的目的地（优先实体位置）
     */
    @Nullable
    public BlockPos getEffectiveDestination() {
        if (targetEntity != null && targetEntity.isAlive()) {
            return targetEntity.blockPosition();
        }
        return destination;
    }
}
