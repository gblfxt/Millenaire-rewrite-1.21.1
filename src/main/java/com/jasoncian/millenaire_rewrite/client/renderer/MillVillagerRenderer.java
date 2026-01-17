package com.jasoncian.millenaire_rewrite.client.renderer;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.client.model.MillVillagerModel;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import com.jasoncian.millenaire_rewrite.client.renderer.layer.VillagerClothesLayer;

/**
 * Millenaire村民渲染器
 *
 * 特性：
 * - 基于文化选择纹理
 * - 基于性别选择纹理
 * - 基于职业选择纹理
 * - 儿童缩放支持
 * - 护甲层渲染
 *
 * @author Based on OldSource RenderMillVillager
 * @version 1.0.0
 */
public class MillVillagerRenderer extends HumanoidMobRenderer<MillVillager, MillVillagerModel> {

    // ================ 纹理路径 ================

    private static final String TEXTURE_BASE = "textures/entity/villager/";

    /** 默认纹理（诺曼男性农民） */
    private static final ResourceLocation DEFAULT_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(MillenaireRewrite.MOD_ID,
            TEXTURE_BASE + "norman/male_farmer.png");

    // ================ 构造函数 ================

    public MillVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new MillVillagerModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);

        // 添加护甲层
        this.addLayer(new HumanoidArmorLayer<>(this,
            new MillVillagerModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
            new MillVillagerModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
            context.getModelManager()));

        // 添加手持物品渲染层
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));

        // 添加衣服渲染层（2层）
        this.addLayer(new VillagerClothesLayer(this, 0));
        this.addLayer(new VillagerClothesLayer(this, 1));
    }

    // ================ 纹理获取 ================

    @Override
    public ResourceLocation getTextureLocation(MillVillager entity) {
        // 构建纹理路径：culture/gender_profession.png
        Culture culture = entity.getCulture();
        String gender = entity.isMale() ? "male" : "female";
        String profession = entity.getProfession().getId();

        String texturePath = TEXTURE_BASE + culture.getId() + "/" + gender + "_" + profession + ".png";
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
            MillenaireRewrite.MOD_ID, texturePath);

        // TODO: 检查纹理是否存在，否则使用默认纹理
        // 目前返回默认纹理直到纹理文件创建
        return DEFAULT_TEXTURE;
    }

    // ================ 缩放处理 ================

    @Override
    protected void scale(MillVillager entity, PoseStack poseStack, float partialTickTime) {
        float scale = entity.getVillagerScale();

        // 儿童缩放（0.5倍）
        if (entity.isChild()) {
            scale *= 0.5f;
            // 儿童的头部相对更大
            poseStack.scale(scale, scale, scale);
        } else {
            // 成人正常缩放
            poseStack.scale(scale, scale, scale);
        }
    }

    // ================ 渲染 ================

    @Override
    public void render(MillVillager entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        // TODO: 渲染职业特定物品（如农民的锄头）
        // TODO: 渲染名字标签（如果在范围内）
    }

    @Override
    protected boolean shouldShowName(MillVillager entity) {
        // 只有当玩家靠近时显示名字
        return super.shouldShowName(entity);
    }
}
