package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.menu.ImportTableMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 导入桌GUI屏幕 - 基于OldSource GuiImportTable实现
 *
 * 显示建筑模板配置界面。
 * 允许用户配置导入/导出选项和建筑参数。
 *
 * @author Based on OldSource GuiImportTable
 * @version 1.0.0
 */
public class ImportTableScreen extends AbstractContainerScreen<ImportTableMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
        MillenaireRewrite.MOD_ID, "textures/gui/import_table.png");

    public ImportTableScreen(ImportTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 添加基本按钮（简化版本）
        // TODO: 完整实现所有按钮功能

        // 导入按钮
        addRenderableWidget(Button.builder(
            Component.translatable("gui.millenaire_rewrite.import_table.import"),
            button -> {
                // TODO: 发送导入请求到服务端
            })
            .bounds(x + 10, y + 140, 85, 20)
            .build());

        // 导出按钮
        addRenderableWidget(Button.builder(
            Component.translatable("gui.millenaire_rewrite.import_table.export"),
            button -> {
                // TODO: 发送导出请求到服务端
            })
            .bounds(x + 105, y + 140, 85, 20)
            .build());

        // 设置按钮
        addRenderableWidget(Button.builder(
            Component.translatable("gui.millenaire_rewrite.import_table.settings"),
            button -> {
                // TODO: 打开设置界面
            })
            .bounds(x + 10, y + 165, 85, 12)
            .build());

        // 新建区域按钮
        addRenderableWidget(Button.builder(
            Component.translatable("gui.millenaire_rewrite.import_table.new_area"),
            button -> {
                // TODO: 创建新建筑区域
            })
            .bounds(x + 105, y + 165, 85, 12)
            .build());
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 绘制简单背景（如果纹理不存在则使用纯色）
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        guiGraphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF8B8B8B);
        guiGraphics.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xFFC6C6C6);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 绘制标题
        guiGraphics.drawCenteredString(font, title, width / 2, y + 10, 0x404040);

        // 绘制建筑信息
        int infoY = y + 30;
        int lineHeight = 12;

        guiGraphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.import_table.dimensions",
                menu.getLength(), menu.getWidth()),
            x + 10, infoY, 0x404040, false);

        guiGraphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.import_table.variation",
                menu.getVariationLetter()),
            x + 10, infoY + lineHeight, 0x404040, false);

        guiGraphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.import_table.level",
                menu.getUpgradeLevel()),
            x + 10, infoY + lineHeight * 2, 0x404040, false);

        guiGraphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.import_table.orientation",
                menu.getOrientationName()),
            x + 10, infoY + lineHeight * 3, 0x404040, false);

        guiGraphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.import_table.starting_level",
                menu.getStartingLevel()),
            x + 10, infoY + lineHeight * 4, 0x404040, false);

        // 绘制导出选项状态
        int optionsY = infoY + lineHeight * 6;
        guiGraphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.import_table.options"),
            x + 10, optionsY, 0x404040, false);

        String snowStatus = menu.isExportSnow() ? "§a✓" : "§c✗";
        guiGraphics.drawString(font,
            Component.literal(snowStatus + " ").append(
                Component.translatable("gui.millenaire_rewrite.import_table.export_snow")),
            x + 10, optionsY + lineHeight, 0x606060, false);

        String mockStatus = menu.isImportMockBlocks() ? "§a✓" : "§c✗";
        guiGraphics.drawString(font,
            Component.literal(mockStatus + " ").append(
                Component.translatable("gui.millenaire_rewrite.import_table.mock_blocks")),
            x + 10, optionsY + lineHeight * 2, 0x606060, false);

        String preserveStatus = menu.isAutoconvertToPreserveGround() ? "§a✓" : "§c✗";
        guiGraphics.drawString(font,
            Component.literal(preserveStatus + " ").append(
                Component.translatable("gui.millenaire_rewrite.import_table.preserve_ground")),
            x + 10, optionsY + lineHeight * 3, 0x606060, false);

        // 绘制工具提示
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 标签在render方法中绘制
    }
}
