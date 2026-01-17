package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.menu.VillagerInteractionMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 村民交互界面 - 显示村民信息和交互选项
 *
 * 功能：
 * - 显示村民名字、文化、职业
 * - 显示与玩家的关系状态
 * - 提供交易、雇佣等按钮
 *
 * @author Based on OldSource VillagerInteractionScreen
 * @version 1.0.0
 */
public class VillagerInteractionScreen extends AbstractContainerScreen<VillagerInteractionMenu> {

    // ================ 资源位置 ================

    private static final ResourceLocation BACKGROUND_TEXTURE =
        ResourceLocation.fromNamespaceAndPath(MillenaireRewrite.MOD_ID,
            "textures/gui/villager_interaction.png");

    // ================ 按钮 ================

    private Button tradeButton;
    private Button hireButton;
    private Button closeButton;

    // ================ 构造函数 ================

    public VillagerInteractionScreen(VillagerInteractionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 180;
    }

    // ================ 初始化 ================

    @Override
    protected void init() {
        super.init();

        // 计算GUI位置
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 交易按钮
        this.tradeButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.trade"),
            button -> onTradePressed()
        ).bounds(x + 20, y + 100, 70, 20).build();

        // 雇佣按钮
        this.hireButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.hire"),
            button -> onHirePressed()
        ).bounds(x + 110, y + 100, 70, 20).build();

        // 关闭按钮
        this.closeButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.close"),
            button -> onClose()
        ).bounds(x + 65, y + 130, 70, 20).build();

        // 添加按钮到屏幕
        this.addRenderableWidget(tradeButton);
        this.addRenderableWidget(hireButton);
        this.addRenderableWidget(closeButton);

        // 根据村民状态启用/禁用按钮
        updateButtonStates();
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        if (tradeButton != null) {
            tradeButton.active = menu.canTrade();
        }
        if (hireButton != null) {
            hireButton.active = menu.canHire();
        }
    }

    // ================ 渲染 ================

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 渲染背景（使用简单的灰色背景直到纹理创建）
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 绘制半透明背景
        guiGraphics.fill(x, y, x + imageWidth, y + imageHeight, 0xC0101010);

        // 绘制边框
        guiGraphics.fill(x, y, x + imageWidth, y + 1, 0xFF808080);
        guiGraphics.fill(x, y + imageHeight - 1, x + imageWidth, y + imageHeight, 0xFF808080);
        guiGraphics.fill(x, y, x + 1, y + imageHeight, 0xFF808080);
        guiGraphics.fill(x + imageWidth - 1, y, x + imageWidth, y + imageHeight, 0xFF808080);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 渲染背景暗化
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 渲染村民信息
        renderVillagerInfo(guiGraphics);

        // 渲染鼠标悬停提示
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    /**
     * 渲染村民信息
     */
    private void renderVillagerInfo(GuiGraphics guiGraphics) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        MillVillager villager = menu.getVillager();
        if (villager == null) return;

        // 标题：村民名字
        String name = villager.getFullName();
        guiGraphics.drawCenteredString(this.font, name, x + imageWidth / 2, y + 10, 0xFFFFFF);

        // 文化
        String cultureLabel = Component.translatable("gui.millenaire_rewrite.culture").getString();
        String culture = villager.getCulture().getDisplayName();
        guiGraphics.drawString(this.font, cultureLabel + ": " + culture, x + 15, y + 35, 0xAAAAAA);

        // 职业
        String professionLabel = Component.translatable("gui.millenaire_rewrite.profession").getString();
        String profession = villager.getProfession().getDisplayName();
        guiGraphics.drawString(this.font, professionLabel + ": " + profession, x + 15, y + 50, 0xAAAAAA);

        // 性别
        String genderLabel = Component.translatable("gui.millenaire_rewrite.gender").getString();
        String gender = villager.isMale() ?
            Component.translatable("gui.millenaire_rewrite.male").getString() :
            Component.translatable("gui.millenaire_rewrite.female").getString();
        guiGraphics.drawString(this.font, genderLabel + ": " + gender, x + 15, y + 65, 0xAAAAAA);

        // 状态
        String statusLabel = Component.translatable("gui.millenaire_rewrite.status").getString();
        String status;
        if (villager.isHired()) {
            status = Component.translatable("gui.millenaire_rewrite.hired").getString();
        } else if (villager.isChild()) {
            status = Component.translatable("gui.millenaire_rewrite.child").getString();
        } else {
            status = Component.translatable("gui.millenaire_rewrite.normal").getString();
        }
        guiGraphics.drawString(this.font, statusLabel + ": " + status, x + 15, y + 80, 0xAAAAAA);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 不渲染默认标签（我们自定义渲染）
    }

    // ================ 按钮回调 ================

    private void onTradePressed() {
        // TODO: 打开交易界面
        MillenaireRewrite.LOGGER.info("Trade button pressed for villager: {}", menu.getVillagerName());
    }

    private void onHirePressed() {
        // TODO: 实现雇佣逻辑
        MillenaireRewrite.LOGGER.info("Hire button pressed for villager: {}", menu.getVillagerName());
    }
}
