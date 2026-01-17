package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.diplomacy.VillageRelation;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 外交地图屏幕 - 显示村庄之间的关系
 *
 * 功能：
 * - 显示已发现村庄的位置（简化地图视图）
 * - 显示村庄之间的关系线（盟友/敌对）
 * - 点击村庄查看详细外交信息
 * - 显示文化间的默认关系
 *
 * @author Based on diplomacy system requirements
 * @version 1.0.0
 */
public class DiplomacyMapScreen extends Screen {

    // ================ 常量 ================

    private static final int GUI_WIDTH = 320;
    private static final int GUI_HEIGHT = 240;

    private static final int MAP_PADDING = 10;
    private static final int VILLAGE_ICON_SIZE = 12;

    // ================ 颜色 ================

    private static final int COLOR_BACKGROUND = 0xE0102030;
    private static final int COLOR_PANEL = 0xC0203040;
    private static final int COLOR_BORDER = 0xFF406080;
    private static final int COLOR_MAP_BG = 0xFF1a2a1a;
    private static final int COLOR_GRID = 0x40406040;

    // 关系颜色
    private static final int COLOR_ALLIED = 0xFF00FF00;
    private static final int COLOR_FRIENDLY = 0xFF80FF80;
    private static final int COLOR_NEUTRAL = 0xFFFFFFFF;
    private static final int COLOR_TENSE = 0xFFFFFF00;
    private static final int COLOR_HOSTILE = 0xFFFF8000;
    private static final int COLOR_WAR = 0xFFFF0000;

    // 文化颜色
    private static final Map<Culture, Integer> CULTURE_COLORS = new EnumMap<>(Culture.class);
    static {
        CULTURE_COLORS.put(Culture.NORMAN, 0xFF4080FF);     // 蓝色
        CULTURE_COLORS.put(Culture.JAPANESE, 0xFFFF4040);   // 红色
        CULTURE_COLORS.put(Culture.INDIAN, 0xFFFF8000);     // 橙色
        CULTURE_COLORS.put(Culture.MAYAN, 0xFF40FF40);      // 绿色
        CULTURE_COLORS.put(Culture.BYZANTINE, 0xFFFF00FF);  // 紫色
        CULTURE_COLORS.put(Culture.INUIT, 0xFF80FFFF);      // 青色
        CULTURE_COLORS.put(Culture.SELJUK, 0xFFFFFF40);     // 黄色
    }

    // ================ 状态 ================

    @Nullable
    private VillageNode selectedVillage = null;
    private boolean showRelationLines = true;
    private int mapOffsetX = 0;
    private int mapOffsetY = 0;
    private float mapZoom = 1.0f;

    // ================ 数据 ================

    private final List<VillageNode> villages = new ArrayList<>();
    private final List<RelationLine> relationLines = new ArrayList<>();

    // ================ 内部类 ================

    /**
     * 村庄节点
     */
    private static class VillageNode {
        UUID villageId;
        String name;
        Culture culture;
        BlockPos worldPos;
        int mapX, mapY; // 屏幕坐标

        VillageNode(UUID id, String name, Culture culture, BlockPos pos) {
            this.villageId = id;
            this.name = name;
            this.culture = culture;
            this.worldPos = pos;
        }
    }

    /**
     * 关系连线
     */
    private static class RelationLine {
        VillageNode villageA;
        VillageNode villageB;
        VillageRelation relation;

        RelationLine(VillageNode a, VillageNode b, VillageRelation rel) {
            this.villageA = a;
            this.villageB = b;
            this.relation = rel;
        }

        int getColor() {
            return switch (relation) {
                case ALLIED -> COLOR_ALLIED;
                case FRIENDLY -> COLOR_FRIENDLY;
                case NEUTRAL -> COLOR_NEUTRAL;
                case UNFRIENDLY -> COLOR_TENSE;
                case HOSTILE -> COLOR_HOSTILE;
                case WAR -> COLOR_WAR;
            };
        }
    }

    // ================ 构造函数 ================

    public DiplomacyMapScreen() {
        super(Component.translatable("gui.millenaire_rewrite.diplomacy_map"));
    }

    // ================ 初始化 ================

    @Override
    protected void init() {
        super.init();

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;

        // 切换关系线显示
        Button toggleLinesBtn = Button.builder(
            Component.literal("Toggle Lines"),
            btn -> {
                showRelationLines = !showRelationLines;
            }
        ).bounds(x + 10, y + GUI_HEIGHT - 25, 80, 18).build();
        addRenderableWidget(toggleLinesBtn);

        // 重置视图
        Button resetViewBtn = Button.builder(
            Component.literal("Reset View"),
            btn -> {
                mapOffsetX = 0;
                mapOffsetY = 0;
                mapZoom = 1.0f;
            }
        ).bounds(x + 95, y + GUI_HEIGHT - 25, 70, 18).build();
        addRenderableWidget(resetViewBtn);

        // 关闭按钮
        Button closeBtn = Button.builder(
            Component.literal("Close"),
            btn -> onClose()
        ).bounds(x + GUI_WIDTH - 60, y + GUI_HEIGHT - 25, 50, 18).build();
        addRenderableWidget(closeBtn);

        // 加载数据
        refreshDiplomacyData();
        calculateMapPositions();
    }

    /**
     * 刷新外交数据
     */
    private void refreshDiplomacyData() {
        villages.clear();
        relationLines.clear();

        // TODO: 通过网络包从服务端获取实际数据
        // 这里可以添加示例数据用于UI测试
    }

    /**
     * 计算地图上的村庄位置
     */
    private void calculateMapPositions() {
        if (villages.isEmpty()) return;

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;
        int mapX = x + MAP_PADDING;
        int mapY = y + 25;
        int mapWidth = GUI_WIDTH - MAP_PADDING * 2;
        int mapHeight = GUI_HEIGHT - 60;

        // 找到世界坐标边界
        int minWorldX = Integer.MAX_VALUE, maxWorldX = Integer.MIN_VALUE;
        int minWorldZ = Integer.MAX_VALUE, maxWorldZ = Integer.MIN_VALUE;

        for (VillageNode node : villages) {
            minWorldX = Math.min(minWorldX, node.worldPos.getX());
            maxWorldX = Math.max(maxWorldX, node.worldPos.getX());
            minWorldZ = Math.min(minWorldZ, node.worldPos.getZ());
            maxWorldZ = Math.max(maxWorldZ, node.worldPos.getZ());
        }

        // 添加边距
        int rangeX = Math.max(100, maxWorldX - minWorldX + 200);
        int rangeZ = Math.max(100, maxWorldZ - minWorldZ + 200);

        // 转换为屏幕坐标
        for (VillageNode node : villages) {
            float normX = (float)(node.worldPos.getX() - minWorldX + 100) / rangeX;
            float normZ = (float)(node.worldPos.getZ() - minWorldZ + 100) / rangeZ;

            node.mapX = mapX + (int)(normX * mapWidth * mapZoom) + mapOffsetX;
            node.mapY = mapY + (int)(normZ * mapHeight * mapZoom) + mapOffsetY;
        }
    }

    // ================ 渲染 ================

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);

        int x = (width - GUI_WIDTH) / 2;
        int y = (height - GUI_HEIGHT) / 2;

        // 主背景
        graphics.fill(x, y, x + GUI_WIDTH, y + GUI_HEIGHT, COLOR_BACKGROUND);
        drawBorder(graphics, x, y, GUI_WIDTH, GUI_HEIGHT, COLOR_BORDER);

        // 标题
        graphics.drawCenteredString(font, title, x + GUI_WIDTH / 2, y + 8, 0xFFFFFF);

        // 地图区域
        int mapX = x + MAP_PADDING;
        int mapY = y + 25;
        int mapWidth = GUI_WIDTH - MAP_PADDING * 2;
        int mapHeight = GUI_HEIGHT - 60;

        graphics.fill(mapX, mapY, mapX + mapWidth, mapY + mapHeight, COLOR_MAP_BG);
        drawBorder(graphics, mapX, mapY, mapWidth, mapHeight, COLOR_BORDER);

        // 绘制网格
        renderMapGrid(graphics, mapX, mapY, mapWidth, mapHeight);

        // 绘制关系线
        if (showRelationLines) {
            renderRelationLines(graphics, mapX, mapY, mapWidth, mapHeight);
        }

        // 绘制村庄节点
        renderVillageNodes(graphics, mapX, mapY, mapWidth, mapHeight, mouseX, mouseY);

        // 绘制图例
        renderLegend(graphics, x + 10, y + 25);

        // 绘制选中村庄信息
        if (selectedVillage != null) {
            renderSelectedInfo(graphics, x + GUI_WIDTH - 110, y + 25);
        }

        // 如果没有村庄，显示提示
        if (villages.isEmpty()) {
            graphics.drawCenteredString(font, "§7No villages discovered",
                x + GUI_WIDTH / 2, y + GUI_HEIGHT / 2 - 10, 0xFFFFFF);
            graphics.drawCenteredString(font, "§7Explore to find villages!",
                x + GUI_WIDTH / 2, y + GUI_HEIGHT / 2 + 5, 0xFFFFFF);
        }

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
     * 绘制地图网格
     */
    private void renderMapGrid(GuiGraphics graphics, int mapX, int mapY, int mapWidth, int mapHeight) {
        int gridSpacing = 20;

        // 垂直线
        for (int gx = gridSpacing; gx < mapWidth; gx += gridSpacing) {
            graphics.fill(mapX + gx, mapY, mapX + gx + 1, mapY + mapHeight, COLOR_GRID);
        }

        // 水平线
        for (int gy = gridSpacing; gy < mapHeight; gy += gridSpacing) {
            graphics.fill(mapX, mapY + gy, mapX + mapWidth, mapY + gy + 1, COLOR_GRID);
        }
    }

    /**
     * 绘制关系连线
     */
    private void renderRelationLines(GuiGraphics graphics, int mapX, int mapY, int mapWidth, int mapHeight) {
        for (RelationLine line : relationLines) {
            // 只绘制非中立关系
            if (line.relation == VillageRelation.NEUTRAL) continue;

            int x1 = line.villageA.mapX + VILLAGE_ICON_SIZE / 2;
            int y1 = line.villageA.mapY + VILLAGE_ICON_SIZE / 2;
            int x2 = line.villageB.mapX + VILLAGE_ICON_SIZE / 2;
            int y2 = line.villageB.mapY + VILLAGE_ICON_SIZE / 2;

            // 简单的线段绘制（粗线）
            drawLine(graphics, x1, y1, x2, y2, line.getColor());
        }
    }

    /**
     * 简单的线段绘制
     */
    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        // 使用Bresenham算法的简化版本
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            graphics.fill(x1 - 1, y1 - 1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    /**
     * 绘制村庄节点
     */
    private void renderVillageNodes(GuiGraphics graphics, int mapX, int mapY, int mapWidth, int mapHeight, int mouseX, int mouseY) {
        for (VillageNode node : villages) {
            // 检查是否在地图范围内
            if (node.mapX < mapX || node.mapX > mapX + mapWidth - VILLAGE_ICON_SIZE ||
                node.mapY < mapY || node.mapY > mapY + mapHeight - VILLAGE_ICON_SIZE) {
                continue;
            }

            int color = CULTURE_COLORS.getOrDefault(node.culture, 0xFFFFFFFF);
            boolean isHovered = mouseX >= node.mapX && mouseX < node.mapX + VILLAGE_ICON_SIZE &&
                               mouseY >= node.mapY && mouseY < node.mapY + VILLAGE_ICON_SIZE;
            boolean isSelected = node == selectedVillage;

            // 绘制节点
            if (isSelected) {
                // 选中的高亮边框
                graphics.fill(node.mapX - 2, node.mapY - 2,
                    node.mapX + VILLAGE_ICON_SIZE + 2, node.mapY + VILLAGE_ICON_SIZE + 2, 0xFFFFFFFF);
            } else if (isHovered) {
                // 悬停的边框
                graphics.fill(node.mapX - 1, node.mapY - 1,
                    node.mapX + VILLAGE_ICON_SIZE + 1, node.mapY + VILLAGE_ICON_SIZE + 1, 0x80FFFFFF);
            }

            // 村庄图标（简单的方块）
            graphics.fill(node.mapX, node.mapY,
                node.mapX + VILLAGE_ICON_SIZE, node.mapY + VILLAGE_ICON_SIZE, color);

            // 悬停时显示名称
            if (isHovered) {
                graphics.drawString(font, node.name, node.mapX + VILLAGE_ICON_SIZE + 3, node.mapY, 0xFFFFFF, true);
            }
        }
    }

    /**
     * 绘制图例
     */
    private void renderLegend(GuiGraphics graphics, int x, int y) {
        graphics.drawString(font, "§7Relations:", x, y, 0xFFFFFF, false);
        y += 10;

        graphics.fill(x, y, x + 8, y + 8, COLOR_ALLIED);
        graphics.drawString(font, "§7Allied", x + 10, y, 0xFFFFFF, false);
        y += 10;

        graphics.fill(x, y, x + 8, y + 8, COLOR_HOSTILE);
        graphics.drawString(font, "§7Hostile", x + 10, y, 0xFFFFFF, false);
        y += 10;

        graphics.fill(x, y, x + 8, y + 8, COLOR_WAR);
        graphics.drawString(font, "§7War", x + 10, y, 0xFFFFFF, false);
    }

    /**
     * 绘制选中村庄信息
     */
    private void renderSelectedInfo(GuiGraphics graphics, int x, int y) {
        if (selectedVillage == null) return;

        graphics.fill(x - 5, y - 5, x + 105, y + 60, COLOR_PANEL);
        drawBorder(graphics, x - 5, y - 5, 110, 65, COLOR_BORDER);

        graphics.drawString(font, "§e" + selectedVillage.name, x, y, 0xFFFFFF, false);
        y += 12;

        int cultureColor = CULTURE_COLORS.getOrDefault(selectedVillage.culture, 0xFFFFFFFF);
        graphics.drawString(font, "§7Culture:", x, y, 0xFFFFFF, false);
        y += 10;
        graphics.fill(x, y, x + 8, y + 8, cultureColor);
        graphics.drawString(font, selectedVillage.culture.getDisplayName(), x + 10, y, 0xFFFFFF, false);
        y += 12;

        graphics.drawString(font, "§7Pos: " + selectedVillage.worldPos.getX() + ", " + selectedVillage.worldPos.getZ(), x, y, 0xFFFFFF, false);
    }

    // ================ 输入处理 ================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // 检查是否点击了村庄节点
            for (VillageNode node : villages) {
                if (mouseX >= node.mapX && mouseX < node.mapX + VILLAGE_ICON_SIZE &&
                    mouseY >= node.mapY && mouseY < node.mapY + VILLAGE_ICON_SIZE) {
                    selectedVillage = node;
                    return true;
                }
            }
            // 点击空白处取消选择
            selectedVillage = null;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            // 拖拽移动地图
            mapOffsetX += (int) dragX;
            mapOffsetY += (int) dragY;
            calculateMapPositions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // 缩放地图
        if (scrollY > 0) {
            mapZoom = Math.min(3.0f, mapZoom * 1.1f);
        } else if (scrollY < 0) {
            mapZoom = Math.max(0.5f, mapZoom / 1.1f);
        }
        calculateMapPositions();
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
