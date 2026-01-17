package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.reputation.ReputationLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 声望屏幕 - 显示玩家与各村庄的声望关系
 *
 * 功能：
 * - 显示所有已发现村庄的声望
 * - 显示声望等级和进度条
 * - 显示各等级的解锁内容
 * - 按文化分类显示
 *
 * @author Based on reputation system requirements
 * @version 1.0.0
 */
public class ReputationScreen extends Screen {

    // ================ 常量 ================

    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 220;

    private static final int ENTRY_HEIGHT = 32;
    private static final int MAX_VISIBLE_ENTRIES = 5;

    // ================ 颜色 ================

    private static final int COLOR_BACKGROUND = 0xE0102030;
    private static final int COLOR_PANEL = 0xC0203040;
    private static final int COLOR_BORDER = 0xFF406080;
    private static final int COLOR_ENTRY_BG = 0x80304050;
    private static final int COLOR_ENTRY_HOVER = 0x80406080;
    private static final int COLOR_PROGRESS_BG = 0xFF203040;

    // ================ 状态 ================

    private int scrollOffset = 0;
    @Nullable
    private VillageRepEntry selectedEntry = null;

    // ================ 缓存数据 ================

    private final List<VillageRepEntry> villageReputations = new ArrayList<>();

    // ================ 组件 ================

    private Button sortByNameButton;
    private Button sortByRepButton;
    private Button sortByCultureButton;

    // ================ 内部类 ================

    /**
     * 村庄声望条目
     */
    private static class VillageRepEntry {
        UUID villageId;
        String villageName;
        Culture culture;
        int reputation;
        ReputationLevel level;

        VillageRepEntry(UUID villageId, String villageName, Culture culture, int reputation) {
            this.villageId = villageId;
            this.villageName = villageName;
            this.culture = culture;
            this.reputation = reputation;
            this.level = ReputationLevel.fromPoints(reputation);
        }

        /**
         * 获取声望等级颜色
         */
        int getLevelColor() {
            return level.getColor() | 0xFF000000; // Add alpha
        }

        /**
         * 获取到下一级所需声望
         */
        int getProgressToNextLevel() {
            ReputationLevel next = level.getNextLevel();
            if (next == level) return 100; // 已满级

            int currentMin = level.getMinPoints();
            int nextMin = next.getMinPoints();
            int range = nextMin - currentMin;

            if (range <= 0) return 100;
            return Math.min(100, Math.max(0, (reputation - currentMin) * 100 / range));
        }
    }

    // ================ 构造函数 ================

    public ReputationScreen() {
        super(Component.translatable("gui.millenaire_rewrite.reputation"));
    }

    // ================ 初始化 ================

    @Override
    protected void init() {
        super.init();

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;

        // 排序按钮
        int buttonWidth = 70;
        int buttonY = y + GUI_HEIGHT - 28;

        sortByNameButton = Button.builder(
            Component.literal("By Name"),
            btn -> sortByName()
        ).bounds(x + 10, buttonY, buttonWidth, 18).build();

        sortByRepButton = Button.builder(
            Component.literal("By Rep"),
            btn -> sortByReputation()
        ).bounds(x + 85, buttonY, buttonWidth, 18).build();

        sortByCultureButton = Button.builder(
            Component.literal("By Culture"),
            btn -> sortByCulture()
        ).bounds(x + 160, buttonY, buttonWidth, 18).build();

        addRenderableWidget(sortByNameButton);
        addRenderableWidget(sortByRepButton);
        addRenderableWidget(sortByCultureButton);

        // 加载数据
        refreshReputationData();
    }

    /**
     * 刷新声望数据
     */
    private void refreshReputationData() {
        villageReputations.clear();

        // TODO: 通过网络包从服务端获取实际数据
        // 这里添加示例数据用于UI测试
        // 实际实现需要从ReputationManager同步数据

        // 示例数据 - 实际运行时会被服务端数据替换
        /*
        villageReputations.add(new VillageRepEntry(
            UUID.randomUUID(), "Normandy Village", Culture.NORMAN, 500));
        villageReputations.add(new VillageRepEntry(
            UUID.randomUUID(), "Kyoto Settlement", Culture.JAPANESE, 200));
        villageReputations.add(new VillageRepEntry(
            UUID.randomUUID(), "Delhi Outpost", Culture.INDIAN, -100));
        */
    }

    // ================ 排序 ================

    private void sortByName() {
        villageReputations.sort((a, b) -> a.villageName.compareToIgnoreCase(b.villageName));
        scrollOffset = 0;
    }

    private void sortByReputation() {
        villageReputations.sort((a, b) -> Integer.compare(b.reputation, a.reputation));
        scrollOffset = 0;
    }

    private void sortByCulture() {
        villageReputations.sort((a, b) -> {
            int cmp = a.culture.getId().compareTo(b.culture.getId());
            if (cmp == 0) {
                return a.villageName.compareToIgnoreCase(b.villageName);
            }
            return cmp;
        });
        scrollOffset = 0;
    }

    // ================ 渲染 ================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;

        // 绘制主背景
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COLOR_BACKGROUND);
        drawBorder(graphics, x, y, GUI_WIDTH, GUI_HEIGHT, COLOR_BORDER);

        // 绘制标题
        graphics.drawCenteredString(font, title, x + GUI_WIDTH / 2, y + 8, 0xFFFFFF);

        // 绘制列表区域
        int listY = y + 25;
        int listHeight = GUI_HEIGHT - 65;
        graphics.fill(x + 5, listY, x + GUI_WIDTH - 5, listY + listHeight, COLOR_PANEL);
        drawBorder(graphics, x + 5, listY, GUI_WIDTH - 10, listHeight, COLOR_BORDER);

        // 绘制声望列表
        renderReputationList(graphics, x + 8, listY + 3, GUI_WIDTH - 16, listHeight - 6, mouseX, mouseY);

        // 渲染图例
        renderLegend(graphics, x + GUI_WIDTH - 100, y + GUI_HEIGHT - 45);

        // 渲染组件
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * 绘制边框
     */
    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    /**
     * 渲染声望列表
     */
    private void renderReputationList(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (villageReputations.isEmpty()) {
            graphics.drawCenteredString(font, "§7No villages discovered yet",
                x + width / 2, y + height / 2 - 4, 0xFFFFFF);
            graphics.drawCenteredString(font, "§7Explore to find Millenaire villages!",
                x + width / 2, y + height / 2 + 8, 0xFFFFFF);
            return;
        }

        int visibleCount = Math.min(MAX_VISIBLE_ENTRIES, villageReputations.size() - scrollOffset);
        for (int i = 0; i < visibleCount; i++) {
            int index = scrollOffset + i;
            if (index >= villageReputations.size()) break;

            VillageRepEntry entry = villageReputations.get(index);
            int entryY = y + i * ENTRY_HEIGHT;
            boolean isSelected = entry == selectedEntry;
            boolean isHovered = mouseX >= x && mouseX < x + width - 10 &&
                               mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT - 2;

            renderReputationEntry(graphics, x, entryY, width - 10, entry, isSelected, isHovered);
        }

        // 滚动条
        if (villageReputations.size() > MAX_VISIBLE_ENTRIES) {
            int scrollbarX = x + width - 6;
            int scrollbarHeight = height - 4;
            int thumbHeight = Math.max(15, scrollbarHeight * MAX_VISIBLE_ENTRIES / villageReputations.size());
            int maxScroll = villageReputations.size() - MAX_VISIBLE_ENTRIES;
            int thumbY = y + 2 + (scrollbarHeight - thumbHeight) * scrollOffset / maxScroll;

            graphics.fill(scrollbarX, y + 2, scrollbarX + 4, y + scrollbarHeight + 2, COLOR_PROGRESS_BG);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, COLOR_BORDER);
        }
    }

    /**
     * 渲染单个声望条目
     */
    private void renderReputationEntry(GuiGraphics graphics, int x, int y, int width,
                                       VillageRepEntry entry, boolean selected, boolean hovered) {
        int bgColor = selected ? 0x80508090 : (hovered ? COLOR_ENTRY_HOVER : COLOR_ENTRY_BG);
        graphics.fill(x, y, x + width, y + ENTRY_HEIGHT - 2, bgColor);

        // 村庄名称
        graphics.drawString(font, "§f" + entry.villageName, x + 3, y + 2, 0xFFFFFF, false);

        // 文化标签
        String cultureTag = "§7[" + entry.culture.getDisplayName() + "]";
        int tagWidth = font.width(cultureTag);
        graphics.drawString(font, cultureTag, x + width - tagWidth - 3, y + 2, 0xFFFFFF, false);

        // 声望等级
        String levelStr = entry.level.getDisplayName();
        int levelColor = entry.getLevelColor();
        graphics.drawString(font, levelStr, x + 3, y + 12, levelColor, false);

        // 声望数值
        String repStr = "(" + entry.reputation + ")";
        graphics.drawString(font, "§7" + repStr, x + 5 + font.width(levelStr), y + 12, 0xFFFFFF, false);

        // 进度条
        int barX = x + 3;
        int barY = y + 23;
        int barWidth = width - 6;
        int barHeight = 5;

        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, COLOR_PROGRESS_BG);

        int progress = entry.getProgressToNextLevel();
        int fillWidth = barWidth * progress / 100;
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + barHeight, levelColor);
        }
    }

    /**
     * 渲染图例
     */
    private void renderLegend(GuiGraphics graphics, int x, int y) {
        graphics.drawString(font, "§7Legend:", x, y, 0xFFFFFF, false);
        y += 10;

        // 简化图例，只显示关键等级
        graphics.drawString(font, "§a■ §7Friendly+", x, y, 0xFFFFFF, false);
        graphics.drawString(font, "§f■ §7Neutral", x + 50, y, 0xFFFFFF, false);
    }

    // ================ 输入处理 ================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (width - GUI_WIDTH) / 2;
            int y = (height - GUI_HEIGHT) / 2;
            int listY = y + 25 + 3;
            int listX = x + 8;
            int listWidth = GUI_WIDTH - 16 - 10;

            // 检查是否点击了条目
            for (int i = 0; i < Math.min(MAX_VISIBLE_ENTRIES, villageReputations.size() - scrollOffset); i++) {
                int entryY = listY + i * ENTRY_HEIGHT;
                if (mouseX >= listX && mouseX < listX + listWidth &&
                    mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT - 2) {
                    int index = scrollOffset + i;
                    if (index < villageReputations.size()) {
                        selectedEntry = villageReputations.get(index);
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, villageReputations.size() - MAX_VISIBLE_ENTRIES);

        if (scrollY > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (scrollY < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
