package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.quest.Quest;
import com.jasoncian.millenaire_rewrite.quest.QuestManager;
import com.jasoncian.millenaire_rewrite.quest.QuestObjective;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 任务日志屏幕 - 显示玩家的活跃任务和完成的任务
 *
 * 功能：
 * - 显示活跃任务列表
 * - 显示任务详情和进度
 * - 显示可用任务
 * - 显示已完成任务历史
 *
 * @author Based on quest system requirements
 * @version 1.0.0
 */
public class QuestLogScreen extends Screen {

    // ================ 常量 ================

    private static final int GUI_WIDTH = 280;
    private static final int GUI_HEIGHT = 220;

    private static final int TAB_HEIGHT = 20;
    private static final int LIST_ENTRY_HEIGHT = 24;
    private static final int MAX_VISIBLE_QUESTS = 6;

    // ================ 颜色 ================

    private static final int COLOR_BACKGROUND = 0xE0102030;
    private static final int COLOR_PANEL = 0xC0203040;
    private static final int COLOR_BORDER = 0xFF406080;
    private static final int COLOR_TAB_ACTIVE = 0xFF508090;
    private static final int COLOR_TAB_INACTIVE = 0xFF304050;
    private static final int COLOR_ENTRY_BG = 0x80304050;
    private static final int COLOR_ENTRY_SELECTED = 0x80508090;
    private static final int COLOR_PROGRESS_BG = 0xFF203040;
    private static final int COLOR_PROGRESS_FILL = 0xFF40A060;

    // ================ 状态 ================

    private Tab currentTab = Tab.ACTIVE;
    private int scrollOffset = 0;
    @Nullable
    private Quest selectedQuest = null;

    // ================ 缓存数据 ================

    private List<Quest> activeQuests = new ArrayList<>();
    private List<Quest> availableQuests = new ArrayList<>();
    private List<Quest> completedQuests = new ArrayList<>();

    // ================ 组件 ================

    private Button tabActiveButton;
    private Button tabAvailableButton;
    private Button tabCompletedButton;
    private Button acceptButton;
    private Button abandonButton;

    // ================ 枚举 ================

    private enum Tab {
        ACTIVE("Active"),
        AVAILABLE("Available"),
        COMPLETED("Completed");

        private final String displayName;

        Tab(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ================ 构造函数 ================

    public QuestLogScreen() {
        super(Component.translatable("gui.millenaire_rewrite.quest_log"));
    }

    // ================ 初始化 ================

    @Override
    protected void init() {
        super.init();

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;

        // 标签按钮
        int tabWidth = (GUI_WIDTH - 20) / 3;

        tabActiveButton = Button.builder(
            Component.literal(Tab.ACTIVE.getDisplayName()),
            btn -> switchTab(Tab.ACTIVE)
        ).bounds(x + 10, y + 5, tabWidth, TAB_HEIGHT).build();

        tabAvailableButton = Button.builder(
            Component.literal(Tab.AVAILABLE.getDisplayName()),
            btn -> switchTab(Tab.AVAILABLE)
        ).bounds(x + 10 + tabWidth, y + 5, tabWidth, TAB_HEIGHT).build();

        tabCompletedButton = Button.builder(
            Component.literal(Tab.COMPLETED.getDisplayName()),
            btn -> switchTab(Tab.COMPLETED)
        ).bounds(x + 10 + tabWidth * 2, y + 5, tabWidth, TAB_HEIGHT).build();

        addRenderableWidget(tabActiveButton);
        addRenderableWidget(tabAvailableButton);
        addRenderableWidget(tabCompletedButton);

        // 操作按钮
        acceptButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.accept_quest"),
            btn -> acceptSelectedQuest()
        ).bounds(x + 10, y + GUI_HEIGHT - 30, 80, 20).build();

        abandonButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.abandon_quest"),
            btn -> abandonSelectedQuest()
        ).bounds(x + 100, y + GUI_HEIGHT - 30, 80, 20).build();

        addRenderableWidget(acceptButton);
        addRenderableWidget(abandonButton);

        // 刷新数据
        refreshQuestData();
        updateButtonStates();
    }

    /**
     * 切换标签页
     */
    private void switchTab(Tab tab) {
        this.currentTab = tab;
        this.scrollOffset = 0;
        this.selectedQuest = null;
        updateButtonStates();
    }

    /**
     * 刷新任务数据
     */
    private void refreshQuestData() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        UUID playerId = mc.player.getUUID();

        // 注意：这在客户端可能无法直接访问ServerLevel
        // 实际实现需要通过网络包从服务端获取数据
        // 这里提供模拟数据用于UI测试
        activeQuests.clear();
        availableQuests.clear();
        completedQuests.clear();

        // TODO: 通过网络包从服务端获取实际任务数据
        // 目前使用空列表，实际数据需要同步
    }

    /**
     * 更新按钮状态
     */
    private void updateButtonStates() {
        // 接受按钮 - 仅在Available标签且有选中任务时启用
        acceptButton.active = currentTab == Tab.AVAILABLE && selectedQuest != null;
        acceptButton.visible = currentTab == Tab.AVAILABLE;

        // 放弃按钮 - 仅在Active标签且有选中任务时启用
        abandonButton.active = currentTab == Tab.ACTIVE && selectedQuest != null;
        abandonButton.visible = currentTab == Tab.ACTIVE;
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
        graphics.drawCenteredString(font, title, x + GUI_WIDTH / 2, y - 12, 0xFFFFFF);

        // 绘制任务列表区域
        int listY = y + TAB_HEIGHT + 10;
        int listHeight = 100;
        graphics.fill(x + 5, listY, x + GUI_WIDTH - 5, listY + listHeight, COLOR_PANEL);
        drawBorder(graphics, x + 5, listY, GUI_WIDTH - 10, listHeight, COLOR_BORDER);

        // 绘制任务列表
        renderQuestList(graphics, x + 8, listY + 3, GUI_WIDTH - 16, listHeight - 6, mouseX, mouseY);

        // 绘制任务详情区域
        int detailY = listY + listHeight + 5;
        int detailHeight = GUI_HEIGHT - TAB_HEIGHT - listHeight - 50;
        graphics.fill(x + 5, detailY, x + GUI_WIDTH - 5, detailY + detailHeight, COLOR_PANEL);
        drawBorder(graphics, x + 5, detailY, GUI_WIDTH - 10, detailHeight, COLOR_BORDER);

        // 绘制任务详情
        renderQuestDetails(graphics, x + 10, detailY + 5, GUI_WIDTH - 20, detailHeight - 10);

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
     * 渲染任务列表
     */
    private void renderQuestList(GuiGraphics graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        List<Quest> quests = getCurrentQuestList();

        if (quests.isEmpty()) {
            String emptyText = switch (currentTab) {
                case ACTIVE -> "No active quests";
                case AVAILABLE -> "No available quests";
                case COMPLETED -> "No completed quests";
            };
            graphics.drawCenteredString(font, emptyText, x + width / 2, y + height / 2 - 4, 0x808080);
            return;
        }

        int visibleCount = Math.min(MAX_VISIBLE_QUESTS, quests.size() - scrollOffset);
        for (int i = 0; i < visibleCount; i++) {
            int index = scrollOffset + i;
            if (index >= quests.size()) break;

            Quest quest = quests.get(index);
            int entryY = y + i * (LIST_ENTRY_HEIGHT - 4);
            boolean isSelected = quest == selectedQuest;
            boolean isHovered = mouseX >= x && mouseX < x + width &&
                               mouseY >= entryY && mouseY < entryY + LIST_ENTRY_HEIGHT - 6;

            renderQuestEntry(graphics, x, entryY, width, quest, isSelected, isHovered);
        }

        // 滚动条
        if (quests.size() > MAX_VISIBLE_QUESTS) {
            int scrollbarX = x + width - 6;
            int scrollbarHeight = height - 4;
            int thumbHeight = Math.max(10, scrollbarHeight * MAX_VISIBLE_QUESTS / quests.size());
            int maxScroll = quests.size() - MAX_VISIBLE_QUESTS;
            int thumbY = y + 2 + (scrollbarHeight - thumbHeight) * scrollOffset / maxScroll;

            graphics.fill(scrollbarX, y + 2, scrollbarX + 4, y + scrollbarHeight + 2, COLOR_PROGRESS_BG);
            graphics.fill(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight, COLOR_BORDER);
        }
    }

    /**
     * 渲染单个任务条目
     */
    private void renderQuestEntry(GuiGraphics graphics, int x, int y, int width, Quest quest, boolean selected, boolean hovered) {
        int bgColor = selected ? COLOR_ENTRY_SELECTED : (hovered ? 0x60406080 : COLOR_ENTRY_BG);
        graphics.fill(x, y, x + width - 8, y + LIST_ENTRY_HEIGHT - 6, bgColor);

        // 任务名称
        String name = quest.getTitle();
        if (name.length() > 30) {
            name = name.substring(0, 28) + "...";
        }
        graphics.drawString(font, name, x + 3, y + 2, 0xFFFFFF, false);

        // 进度条（仅活跃任务）
        if (currentTab == Tab.ACTIVE) {
            float progress = (float) quest.getProgress();
            int barWidth = width - 16;
            int barY = y + 12;

            graphics.fill(x + 3, barY, x + 3 + barWidth, barY + 4, COLOR_PROGRESS_BG);
            int fillWidth = (int) (barWidth * progress);
            if (fillWidth > 0) {
                graphics.fill(x + 3, barY, x + 3 + fillWidth, barY + 4, COLOR_PROGRESS_FILL);
            }
        }

        // 任务类型图标/文字
        String typeStr = "§7[" + quest.getType().getDisplayName() + "]";
        int typeWidth = font.width(typeStr);
        graphics.drawString(font, typeStr, x + width - 12 - typeWidth, y + 2, 0xFFFFFF, false);
    }

    /**
     * 渲染任务详情
     */
    private void renderQuestDetails(GuiGraphics graphics, int x, int y, int width, int height) {
        if (selectedQuest == null) {
            graphics.drawCenteredString(font, "Select a quest to view details",
                x + width / 2, y + height / 2 - 4, 0x808080);
            return;
        }

        // 任务标题
        graphics.drawString(font, "§e§l" + selectedQuest.getTitle(), x, y, 0xFFFFFF, false);
        y += 12;

        // 任务描述
        String desc = selectedQuest.getDescription();
        if (desc.length() > 50) {
            // 简单换行处理
            List<String> lines = wrapText(desc, width);
            for (String line : lines) {
                graphics.drawString(font, "§7" + line, x, y, 0xFFFFFF, false);
                y += 10;
                if (y > height - 30) break;
            }
        } else {
            graphics.drawString(font, "§7" + desc, x, y, 0xFFFFFF, false);
            y += 12;
        }

        // 目标列表
        y += 5;
        graphics.drawString(font, "§fObjectives:", x, y, 0xFFFFFF, false);
        y += 10;

        for (QuestObjective objective : selectedQuest.getObjectives()) {
            String status = objective.isComplete() ? "§a✓" : "§c○";
            String objText = status + " §f" + objective.getDescription();
            if (objective.getTargetAmount() > 1) {
                objText += " §7(" + objective.getCurrentProgress() + "/" + objective.getTargetAmount() + ")";
            }
            graphics.drawString(font, objText, x + 5, y, 0xFFFFFF, false);
            y += 10;
        }

        // 奖励
        y += 5;
        graphics.drawString(font, "§6Rewards:", x, y, 0xFFFFFF, false);
        y += 10;

        int deniers = selectedQuest.getDenierReward();
        if (deniers > 0) {
            graphics.drawString(font, "  §e" + deniers + " Deniers", x, y, 0xFFFFFF, false);
            y += 10;
        }

        int rep = selectedQuest.getReputationReward();
        if (rep != 0) {
            String repColor = rep > 0 ? "§a+" : "§c";
            graphics.drawString(font, "  " + repColor + rep + " Reputation", x, y, 0xFFFFFF, false);
        }
    }

    /**
     * 简单文本换行
     */
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (font.width(line + " " + word) > maxWidth) {
                if (line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
            }
            if (line.length() > 0) line.append(" ");
            line.append(word);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }

        return lines;
    }

    /**
     * 获取当前标签的任务列表
     */
    private List<Quest> getCurrentQuestList() {
        return switch (currentTab) {
            case ACTIVE -> activeQuests;
            case AVAILABLE -> availableQuests;
            case COMPLETED -> completedQuests;
        };
    }

    // ================ 输入处理 ================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (width - GUI_WIDTH) / 2;
            int y = (height - GUI_HEIGHT) / 2;
            int listY = y + TAB_HEIGHT + 10;
            int listX = x + 8;
            int listWidth = GUI_WIDTH - 16;

            // 检查是否点击了任务列表
            List<Quest> quests = getCurrentQuestList();
            for (int i = 0; i < Math.min(MAX_VISIBLE_QUESTS, quests.size() - scrollOffset); i++) {
                int entryY = listY + 3 + i * (LIST_ENTRY_HEIGHT - 4);
                if (mouseX >= listX && mouseX < listX + listWidth - 8 &&
                    mouseY >= entryY && mouseY < entryY + LIST_ENTRY_HEIGHT - 6) {
                    int index = scrollOffset + i;
                    if (index < quests.size()) {
                        selectedQuest = quests.get(index);
                        updateButtonStates();
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<Quest> quests = getCurrentQuestList();
        int maxScroll = Math.max(0, quests.size() - MAX_VISIBLE_QUESTS);

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

    // ================ 按钮操作 ================

    private void acceptSelectedQuest() {
        if (selectedQuest != null) {
            // TODO: 发送网络包接受任务
            MillenaireRewrite.LOGGER.info("Accept quest: {}", selectedQuest.getTitle());
        }
    }

    private void abandonSelectedQuest() {
        if (selectedQuest != null) {
            // TODO: 发送网络包放弃任务
            MillenaireRewrite.LOGGER.info("Abandon quest: {}", selectedQuest.getTitle());
        }
    }
}
