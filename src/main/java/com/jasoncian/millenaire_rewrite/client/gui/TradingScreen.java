package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.menu.TradingMenu;
import com.jasoncian.millenaire_rewrite.trade.TradeOffer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * 交易屏幕 - 显示村民交易界面
 *
 * 显示：
 * - 村民信息
 * - 可用交易列表
 * - 玩家货币
 * - 交易按钮
 *
 * @author Based on OldSource Trading GUI
 * @version 1.0.0
 */
public class TradingScreen extends AbstractContainerScreen<TradingMenu> {

    // ================ 资源 ================

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
        MillenaireRewrite.MOD_ID, "textures/gui/trading.png");

    // ================ 尺寸 ================

    private static final int GUI_WIDTH = 276;
    private static final int GUI_HEIGHT = 200;

    // ================ 组件 ================

    private Button buyButton;

    // ================ 滚动状态 ================

    private int scrollOffset = 0;
    private static final int OFFERS_PER_PAGE = 7;

    // ================ 选中状态 ================

    private int selectedIndex = -1;

    // ================ 构造函数 ================

    public TradingScreen(TradingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    // ================ 初始化 ================

    @Override
    protected void init() {
        super.init();

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 购买按钮
        buyButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.buy"),
            button -> {
                if (selectedIndex >= 0) {
                    menu.requestTrade(selectedIndex);
                }
            })
            .bounds(x + imageWidth - 70, y + imageHeight - 30, 60, 20)
            .build();
        buyButton.active = false;
        addRenderableWidget(buyButton);
    }

    // ================ 渲染 ================

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 绘制背景
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFFC6C6C6);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFF8B8B8B);
        graphics.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xFFC6C6C6);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 渲染村民信息
        renderVillagerInfo(graphics, x, y);

        // 渲染交易列表
        renderTradeList(graphics, x, y, mouseX, mouseY);

        // 渲染玩家货币
        renderPlayerMoney(graphics, x, y);

        // 渲染选中交易详情
        if (selectedIndex >= 0) {
            renderSelectedOffer(graphics, x, y);
        }

        renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * 渲染村民信息
     */
    private void renderVillagerInfo(GuiGraphics graphics, int x, int y) {
        int textX = x + 10;
        int textY = y + 10;

        // 村民名称
        String name = menu.getVillagerName();
        graphics.drawString(font, Component.literal("§6§l" + name), textX, textY, 0xFFFFFF, false);
        textY += 12;

        // 文化和职业
        String culture = menu.getVillagerCulture();
        String profession = menu.getVillagerProfession();
        graphics.drawString(font, Component.literal("§7" + culture + " " + profession),
            textX, textY, 0xFFFFFF, false);
    }

    /**
     * 渲染交易列表
     */
    private void renderTradeList(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        int listX = x + 10;
        int listY = y + 40;
        int listWidth = 150;
        int listHeight = 120;

        // 列表标题
        graphics.drawString(font, Component.translatable("gui.millenaire_rewrite.trades"),
            listX, listY - 12, 0x404040, false);

        // 列表背景
        graphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF404040);
        graphics.fill(listX + 1, listY + 1, listX + listWidth - 1, listY + listHeight - 1, 0xFF202020);

        int offerCount = menu.getOfferCount();
        if (offerCount == 0) {
            graphics.drawString(font, Component.literal("§7No trades available"),
                listX + 5, listY + 5, 0xFFFFFF, false);
            return;
        }

        // 渲染交易条目
        int entryY = listY + 2;
        int maxIndex = Math.min(scrollOffset + OFFERS_PER_PAGE, offerCount);

        for (int i = scrollOffset; i < maxIndex; i++) {
            TradeOffer offer = menu.getOffer(i);
            if (offer != null) {
                renderTradeEntry(graphics, listX + 2, entryY, listWidth - 4, i, offer, mouseX, mouseY);
                entryY += 17;
            }
        }

        // 滚动条
        if (offerCount > OFFERS_PER_PAGE) {
            int scrollbarHeight = listHeight - 4;
            int thumbHeight = Math.max(10, scrollbarHeight * OFFERS_PER_PAGE / offerCount);
            int thumbY = listY + 2 + (scrollbarHeight - thumbHeight) * scrollOffset /
                (offerCount - OFFERS_PER_PAGE);

            graphics.fill(listX + listWidth - 8, listY + 2,
                listX + listWidth - 4, listY + listHeight - 2, 0xFF303030);
            graphics.fill(listX + listWidth - 7, thumbY,
                listX + listWidth - 5, thumbY + thumbHeight, 0xFF808080);
        }
    }

    /**
     * 渲染单个交易条目
     */
    private void renderTradeEntry(GuiGraphics graphics, int x, int y, int width, int index,
                                   TradeOffer offer, int mouseX, int mouseY) {
        boolean isSelected = index == selectedIndex;
        boolean isHovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + 16;
        boolean canAfford = menu.getPlayerMoney() >= offer.getPriceInDeniers();
        boolean available = offer.canUse();

        // 背景
        int bgColor;
        if (isSelected) {
            bgColor = 0xFF4080FF;
        } else if (isHovered) {
            bgColor = 0xFF505050;
        } else {
            bgColor = 0xFF303030;
        }
        graphics.fill(x, y, x + width, y + 16, bgColor);

        // 物品图标
        ItemStack item = offer.getSellingItem();
        graphics.renderItem(item, x + 2, y);

        // 物品名称
        String itemName = item.getHoverName().getString();
        if (itemName.length() > 12) {
            itemName = itemName.substring(0, 10) + "..";
        }
        String textColor = available && canAfford ? "§f" : "§8";
        graphics.drawString(font, textColor + itemName, x + 22, y + 1, 0xFFFFFF, false);

        // 价格
        String priceText = offer.getPriceDisplay();
        String priceColor = canAfford ? "§e" : "§c";
        graphics.drawString(font, priceColor + priceText, x + 22, y + 9, 0xFFFFFF, false);

        // 不可用标记
        if (!available) {
            graphics.drawString(font, "§c[X]", x + width - 20, y + 4, 0xFFFFFF, false);
        }
    }

    /**
     * 渲染玩家货币
     */
    private void renderPlayerMoney(GuiGraphics graphics, int x, int y) {
        int moneyX = x + 170;
        int moneyY = y + 10;

        graphics.drawString(font, Component.translatable("gui.millenaire_rewrite.your_money"),
            moneyX, moneyY, 0x404040, false);

        int money = menu.getPlayerMoney();
        String moneyText = menu.formatMoney(money);
        graphics.drawString(font, Component.literal("§e" + moneyText),
            moneyX, moneyY + 12, 0xFFFFFF, false);
    }

    /**
     * 渲染选中交易详情
     */
    private void renderSelectedOffer(GuiGraphics graphics, int x, int y) {
        TradeOffer offer = menu.getOffer(selectedIndex);
        if (offer == null) return;

        int detailX = x + 170;
        int detailY = y + 50;

        // 标题
        graphics.drawString(font, Component.translatable("gui.millenaire_rewrite.selected_trade"),
            detailX, detailY, 0x404040, false);
        detailY += 15;

        // 物品信息
        ItemStack item = offer.getSellingItem();
        graphics.renderItem(item, detailX, detailY);
        graphics.drawString(font, item.getHoverName(), detailX + 20, detailY + 4, 0xFFFFFF, false);
        detailY += 20;

        // 数量
        graphics.drawString(font, Component.literal("§7Qty: §f" + item.getCount()),
            detailX, detailY, 0xFFFFFF, false);
        detailY += 12;

        // 价格
        graphics.drawString(font, Component.literal("§7Price: §e" + offer.getPriceDisplay()),
            detailX, detailY, 0xFFFFFF, false);
        detailY += 12;

        // 剩余次数
        int remaining = offer.getRemainingUses();
        String usesText = remaining < 0 ? "Unlimited" : String.valueOf(remaining);
        graphics.drawString(font, Component.literal("§7Stock: §f" + usesText),
            detailX, detailY, 0xFFFFFF, false);

        // 更新购买按钮状态
        boolean canBuy = offer.canUse() && menu.getPlayerMoney() >= offer.getPriceInDeniers();
        buyButton.active = canBuy;
    }

    // ================ 输入处理 ================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // 检查点击了哪个交易
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            int listX = x + 10;
            int listY = y + 40;
            int listWidth = 150;

            if (mouseX >= listX && mouseX < listX + listWidth - 10) {
                int entryY = listY + 2;
                int offerCount = menu.getOfferCount();
                int maxIndex = Math.min(scrollOffset + OFFERS_PER_PAGE, offerCount);

                for (int i = scrollOffset; i < maxIndex; i++) {
                    if (mouseY >= entryY && mouseY < entryY + 16) {
                        selectedIndex = i;
                        menu.selectOffer(i);
                        return true;
                    }
                    entryY += 17;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int offerCount = menu.getOfferCount();
        int maxScroll = Math.max(0, offerCount - OFFERS_PER_PAGE);

        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 不绘制默认标签
    }
}
