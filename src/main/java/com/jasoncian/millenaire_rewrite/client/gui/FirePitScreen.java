package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.menu.FirePitMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 火坑GUI屏幕 - 基于OldSource GuiFirePit实现
 *
 * 显示火坑的烹饪进度、燃烧状态和物品槽位。
 *
 * 视觉元素：
 * - 3个烹饪进度箭头（对应3个输入槽位）
 * - 1个燃烧指示器（显示燃料剩余）
 *
 * @author Based on OldSource GuiFirePit
 * @version 1.0.0
 */
public class FirePitScreen extends AbstractContainerScreen<FirePitMenu> {

    /** GUI纹理路径 */
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        MillenaireRewrite.MOD_ID, "textures/gui/firepit.png");

    /** GUI尺寸 */
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 175;

    /** 箭头渲染数据 [x, y, width, height, textureX, textureY] */
    private static final int[][] ARROW_DATA = {
        {76, 8, 23, 8, 176, 0},      // 箭头0：右侧，较窄
        {63, 28, 37, 16, 176, 8},    // 箭头1：中间，较宽
        {76, 48, 23, 8, 176, 24}     // 箭头2：右侧，较窄
    };

    /** 火焰指示器位置 */
    private static final int BURN_X = 80;
    private static final int BURN_Y = 56;
    private static final int BURN_WIDTH = 14;
    private static final int BURN_HEIGHT = 13;
    private static final int BURN_TEXTURE_X = 176;
    private static final int BURN_TEXTURE_Y = 32;

    public FirePitScreen(FirePitMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 绘制背景
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 绘制燃烧指示器
        if (menu.isLit()) {
            float burnProgress = menu.getBurnProgressScaled();
            int burnHeight = (int) (BURN_HEIGHT * burnProgress);
            guiGraphics.blit(TEXTURE,
                x + BURN_X, y + BURN_Y + BURN_HEIGHT - burnHeight,
                BURN_TEXTURE_X, BURN_TEXTURE_Y + BURN_HEIGHT - burnHeight,
                BURN_WIDTH, burnHeight);
        }

        // 绘制烹饪进度箭头
        for (int i = 0; i < 3; i++) {
            float progress = menu.getCookingProgressScaled(i);
            if (progress > 0) {
                int[] data = ARROW_DATA[i];
                int progressWidth = (int) (data[2] * progress);
                guiGraphics.blit(TEXTURE,
                    x + data[0], y + data[1],
                    data[4], data[5],
                    progressWidth, data[3]);
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
