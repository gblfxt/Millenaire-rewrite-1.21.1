package com.jasoncian.millenaire_rewrite.client.model;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Millenaire村民模型 - 基于人形模型
 *
 * 支持：
 * - 标准人形骨骼结构
 * - 儿童缩放
 * - 职业特定动画
 * - 动画状态系统
 *
 * @author Based on OldSource RenderMillVillager
 * @version 1.0.0
 */
public class MillVillagerModel extends HumanoidModel<MillVillager> {

    public MillVillagerModel(ModelPart root) {
        super(root);
    }

    /**
     * 创建模型层定义
     * 使用标准人形模型布局
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshDefinition.getRoot();

        // 使用标准人形模型部件
        // 可以在此处添加额外的装饰部件

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(MillVillager entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // 调用父类基础动画
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 根据动画状态应用额外动画
        VillagerAnimationState state = entity.getAnimationState();
        int animTicks = entity.getAnimationTicks();

        switch (state) {
            case WORKING, FARMING -> applyFarmingAnimation(animTicks);
            case MINING, CHOPPING -> applyMiningAnimation(animTicks);
            case ATTACKING -> applyAttackAnimation(animTicks);
            case DEFENDING -> applyDefendAnimation();
            case EATING -> applyEatingAnimation(animTicks);
            case SOCIALIZING -> applySocializingAnimation(animTicks);
            case SLEEPING -> applySleepingAnimation();
            case HURT -> applyHurtAnimation(animTicks);
            default -> {
                // IDLE, WALKING, RUNNING 使用基础动画
            }
        }

        // 儿童特殊处理
        if (entity.isChild()) {
            // 儿童头部更大（由渲染器处理缩放）
            this.head.y += 4.0F;
        }
    }

    /**
     * 耕作/工作动画 - 弯腰和手臂摆动
     */
    private void applyFarmingAnimation(int ticks) {
        float progress = ticks * 0.15f;

        // 轻微弯腰
        this.body.xRot = 0.3f;

        // 手臂上下摆动
        this.rightArm.xRot = -1.0f + Mth.sin(progress) * 0.5f;
        this.leftArm.xRot = -0.8f + Mth.cos(progress) * 0.3f;
    }

    /**
     * 采矿/砍伐动画 - 挥动动作
     */
    private void applyMiningAnimation(int ticks) {
        float progress = ticks * 0.2f;

        // 挥动主手臂
        this.rightArm.xRot = -2.0f + Mth.abs(Mth.sin(progress)) * 1.5f;
        this.rightArm.zRot = -0.3f;

        // 轻微弯腰
        this.body.xRot = 0.2f + Mth.abs(Mth.sin(progress)) * 0.1f;
    }

    /**
     * 攻击动画 - 挥舞武器
     */
    private void applyAttackAnimation(int ticks) {
        float progress = ticks * 0.25f;

        // 战斗姿态
        this.rightArm.xRot = -1.5f + Mth.sin(progress) * 0.8f;
        this.rightArm.yRot = -0.3f;

        // 左臂防御姿态
        this.leftArm.xRot = -0.8f;
        this.leftArm.yRot = 0.5f;
    }

    /**
     * 防御动画 - 举盾姿态
     */
    private void applyDefendAnimation() {
        // 举起左臂（假设盾在左手）
        this.leftArm.xRot = -1.2f;
        this.leftArm.yRot = 0.5f;

        // 右手准备攻击
        this.rightArm.xRot = -0.5f;
    }

    /**
     * 进食动画 - 手举到嘴边
     */
    private void applyEatingAnimation(int ticks) {
        float progress = ticks * 0.1f;

        // 右手举到嘴边
        this.rightArm.xRot = -1.8f;
        this.rightArm.yRot = 0.3f + Mth.sin(progress) * 0.1f;

        // 头部轻微下看
        this.head.xRot = 0.2f;
    }

    /**
     * 社交动画 - 点头和手势
     */
    private void applySocializingAnimation(int ticks) {
        float progress = ticks * 0.1f;

        // 点头
        this.head.xRot = Mth.sin(progress) * 0.15f;

        // 偶尔抬手
        if ((ticks % 60) < 20) {
            this.rightArm.xRot = -0.5f + Mth.sin(progress * 2) * 0.3f;
            this.rightArm.zRot = -0.3f;
        }
    }

    /**
     * 睡眠动画 - 躺平
     */
    private void applySleepingAnimation() {
        // 整体旋转使身体平躺（实际渲染由渲染器处理）
        // 这里只做手臂和腿的调整
        this.rightArm.xRot = 0.0f;
        this.leftArm.xRot = 0.0f;
        this.rightArm.zRot = 0.1f;
        this.leftArm.zRot = -0.1f;

        this.rightLeg.xRot = 0.0f;
        this.leftLeg.xRot = 0.0f;
    }

    /**
     * 受伤动画 - 后仰
     */
    private void applyHurtAnimation(int ticks) {
        // 身体后仰
        this.body.xRot = -0.3f;

        // 手臂张开
        this.rightArm.zRot = -0.5f;
        this.leftArm.zRot = 0.5f;
    }
}
