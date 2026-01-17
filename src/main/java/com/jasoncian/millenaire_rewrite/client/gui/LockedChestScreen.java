package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.menu.LockedChestMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 锁定箱子GUI屏幕 - 基于OldSource GuiLockedChest实现
 *
 * 显示锁定箱子的内容。当箱子被锁定时，
 * 禁用除关闭外的所有交互。
 *
 * @author Based on OldSource GuiLockedChest
 * @version 1.0.0
 */
public class LockedChestScreen extends AbstractContainerScreen<LockedChestMenu> {

    /** 使用原版箱子纹理 */
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace(
        "textures/gui/container/generic_54.png");

    public LockedChestScreen(LockedChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 114 + menu.getRows() * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 绘制背景（使用3行箱子纹理）
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, menu.getRows() * 18 + 17);
        guiGraphics.blit(TEXTURE, x, y + menu.getRows() * 18 + 17, 0, 126, imageWidth, 96);

        // 如果锁定，绘制锁定覆盖层
        if (menu.isLocked()) {
            // 半透明红色覆盖
            guiGraphics.fill(x + 7, y + 17, x + 169, y + menu.getRows() * 18 + 17, 0x80FF0000);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // 如果锁定，显示锁定提示
        if (menu.isLocked()) {
            Component lockedText = Component.translatable("container.millenaire_rewrite.locked_chest.locked_hint");
            int textWidth = font.width(lockedText);
            int textX = (width - textWidth) / 2;
            int textY = (height - imageHeight) / 2 + menu.getRows() * 9 + 17;
            guiGraphics.drawString(font, lockedText, textX, textY, 0xFFFFFF, true);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 锁定时阻止点击箱子槽位
        if (menu.isLocked()) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            // 检查是否点击在箱子区域
            if (mouseX >= x + 7 && mouseX < x + 169 &&
                mouseY >= y + 17 && mouseY < y + menu.getRows() * 18 + 17) {
                return false;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 锁定时只允许ESC关闭
        if (menu.isLocked()) {
            if (keyCode == 256) { // ESC
                this.onClose();
                return true;
            }
            // 允许物品栏快捷键
            if (this.minecraft != null && this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
                this.onClose();
                return true;
            }
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
