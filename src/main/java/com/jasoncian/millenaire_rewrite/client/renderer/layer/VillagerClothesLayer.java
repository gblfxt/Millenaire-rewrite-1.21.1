package com.jasoncian.millenaire_rewrite.client.renderer.layer;

import com.jasoncian.millenaire_rewrite.client.model.MillVillagerModel;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 村民衣服渲染层 - 在基础皮肤上渲染衣服纹理
 *
 * 特性：
 * - 支持多层衣服（layer 0和layer 1）
 * - 轻微的垂直偏移防止z-fighting
 * - 基于文化和职业选择纹理
 *
 * @author Based on OldSource LayerVillagerClothes
 * @version 1.0.0
 */
public class VillagerClothesLayer extends RenderLayer<MillVillager, MillVillagerModel> {

    /** 衣服层索引（0或1） */
    private final int layerIndex;

    /** 垂直偏移量，用于防止z-fighting */
    private final float verticalOffset;

    public VillagerClothesLayer(RenderLayerParent<MillVillager, MillVillagerModel> renderer, int layerIndex) {
        super(renderer);
        this.layerIndex = layerIndex;
        // 每层略微向外偏移
        this.verticalOffset = 0.001f * (layerIndex + 1);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       MillVillager villager, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 获取该层的衣服纹理
        ResourceLocation clothTexture = villager.getClothTexture(layerIndex);

        if (clothTexture == null) {
            return; // 没有衣服纹理，跳过渲染
        }

        // 应用轻微偏移防止z-fighting
        poseStack.pushPose();
        poseStack.scale(1.0f + verticalOffset, 1.0f + verticalOffset, 1.0f + verticalOffset);

        // 获取模型并设置姿态
        MillVillagerModel model = this.getParentModel();

        // 渲染衣服纹理
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(clothTexture));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
    }
}
