package com.jasoncian.millenaire_rewrite.client.gui;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.menu.TownHallMenu;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 市政厅屏幕 - 村庄管理GUI
 *
 * 显示：
 * - 村庄名称和文化
 * - 人口统计
 * - 建筑数量
 * - 资源/金币
 * - 村民列表
 *
 * @author Based on OldSource TownHall GUI
 * @version 1.0.0
 */
public class TownHallScreen extends AbstractContainerScreen<TownHallMenu> {

    // ================ 资源 ================

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(
        MillenaireRewrite.MOD_ID, "textures/gui/town_hall.png");

    // ================ 尺寸 ================

    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 200;

    // ================ 组件 ================

    private Button spawnButton;
    private Button refreshButton;

    // ================ 滚动状态 ================

    private int villagerScrollOffset = 0;
    private static final int VILLAGERS_PER_PAGE = 6;

    // ================ 构造函数 ================

    public TownHallScreen(TownHallMenu menu, Inventory playerInventory, Component title) {
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

        // 生成村民按钮
        spawnButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.spawn_villager"),
            button -> {
                menu.requestSpawnVillager();
            })
            .bounds(x + 10, y + imageHeight - 30, 80, 20)
            .build();
        addRenderableWidget(spawnButton);

        // 刷新按钮
        refreshButton = Button.builder(
            Component.translatable("gui.millenaire_rewrite.refresh"),
            button -> {
                menu.refresh();
            })
            .bounds(x + 100, y + imageHeight - 30, 60, 20)
            .build();
        addRenderableWidget(refreshButton);
    }

    // ================ 渲染 ================

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 绘制背景（如果没有自定义纹理，绘制简单背景）
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

        // 绘制村庄信息
        renderVillageInfo(graphics, x, y);

        // 绘制村民列表
        renderVillagerList(graphics, x, y);

        renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * 渲染村庄信息
     */
    private void renderVillageInfo(GuiGraphics graphics, int x, int y) {
        int textX = x + 10;
        int textY = y + 10;

        // 村庄名称（标题）
        String villageName = menu.getVillageName();
        graphics.drawString(font, "§6§l" + villageName, textX, textY, 0xFFFFFF, false);
        textY += 15;

        // 文化
        String culture = getCultureDisplayName(menu.getCultureId());
        graphics.drawString(font, "§7Culture: §f" + culture, textX, textY, 0xFFFFFF, false);
        textY += 12;

        // 分隔线
        graphics.fill(x + 8, textY, x + imageWidth - 8, textY + 1, 0xFF404040);
        textY += 5;

        // 人口
        int pop = menu.getPopulation();
        int popCap = menu.getPopulationCap();
        String popColor = pop >= popCap ? "§c" : (pop >= popCap * 0.8 ? "§e" : "§a");
        graphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.population",
                popColor + pop + "§f", popCap),
            textX, textY, 0xFFFFFF, false);
        textY += 12;

        // 建筑
        graphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.buildings", menu.getBuildingCount()),
            textX, textY, 0xFFFFFF, false);
        textY += 12;

        // 金币
        long deniers = menu.getDeniers();
        graphics.drawString(font,
            Component.translatable("gui.millenaire_rewrite.deniers", "§e" + deniers + "§f"),
            textX, textY, 0xFFFFFF, false);
    }

    /**
     * 渲染村民列表
     */
    private void renderVillagerList(GuiGraphics graphics, int x, int y) {
        int listX = x + 10;
        int listY = y + 85;
        int listWidth = imageWidth - 20;
        int listHeight = 80;

        // 列表标题
        graphics.drawString(font, "§l" +
            Component.translatable("gui.millenaire_rewrite.villagers").getString(),
            listX, listY - 12, 0xFFFFFF, false);

        // 列表背景
        graphics.fill(listX, listY, listX + listWidth, listY + listHeight, 0xFF404040);
        graphics.fill(listX + 1, listY + 1, listX + listWidth - 1, listY + listHeight - 1, 0xFF202020);

        // 获取村民列表
        List<VillagerInfo> villagers = getVillagerList();

        if (villagers.isEmpty()) {
            graphics.drawString(font, "§7No villagers",
                listX + 5, listY + 5, 0xFFFFFF, false);
            return;
        }

        // 渲染村民条目
        int entryY = listY + 3;
        int maxIndex = Math.min(villagerScrollOffset + VILLAGERS_PER_PAGE, villagers.size());

        for (int i = villagerScrollOffset; i < maxIndex; i++) {
            VillagerInfo info = villagers.get(i);
            renderVillagerEntry(graphics, listX + 3, entryY, listWidth - 6, info);
            entryY += 13;
        }

        // 滚动指示器
        if (villagers.size() > VILLAGERS_PER_PAGE) {
            int scrollbarHeight = listHeight - 4;
            int thumbHeight = Math.max(10, scrollbarHeight * VILLAGERS_PER_PAGE / villagers.size());
            int thumbY = listY + 2 + (scrollbarHeight - thumbHeight) * villagerScrollOffset /
                (villagers.size() - VILLAGERS_PER_PAGE);

            // 滚动条轨道
            graphics.fill(listX + listWidth - 8, listY + 2,
                listX + listWidth - 4, listY + listHeight - 2, 0xFF303030);
            // 滚动条滑块
            graphics.fill(listX + listWidth - 7, thumbY,
                listX + listWidth - 5, thumbY + thumbHeight, 0xFF808080);
        }
    }

    /**
     * 渲染单个村民条目
     */
    private void renderVillagerEntry(GuiGraphics graphics, int x, int y, int width, VillagerInfo info) {
        // 背景
        graphics.fill(x, y, x + width, y + 12, 0xFF303030);

        // 名字
        graphics.drawString(font, info.name, x + 2, y + 2, 0xFFFFFF, false);

        // 职业
        String profColor = info.isCombat ? "§c" : "§a";
        graphics.drawString(font, profColor + info.profession,
            x + width - font.width(info.profession) - 2, y + 2, 0xFFFFFF, false);
    }

    /**
     * 获取村民列表
     */
    private List<VillagerInfo> getVillagerList() {
        List<VillagerInfo> list = new ArrayList<>();

        Village village = menu.getVillage();
        if (village != null) {
            for (MillVillager villager : village.getActiveVillagers()) {
                VillagerInfo info = new VillagerInfo();
                info.name = villager.getFullName();
                info.profession = villager.getProfession().getDisplayName();
                info.isCombat = villager.getProfession().isCombatProfession();
                list.add(info);
            }
        }

        return list;
    }

    /**
     * 获取文化显示名称
     */
    private String getCultureDisplayName(String cultureId) {
        return switch (cultureId.toLowerCase()) {
            case "norman" -> "Norman";
            case "japanese" -> "Japanese";
            case "indian" -> "Indian";
            case "mayan" -> "Mayan";
            case "byzantine" -> "Byzantine";
            case "inuit" -> "Inuit";
            case "seljuk" -> "Seljuk";
            default -> cultureId;
        };
    }

    // ================ 输入处理 ================

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        List<VillagerInfo> villagers = getVillagerList();
        int maxScroll = Math.max(0, villagers.size() - VILLAGERS_PER_PAGE);

        if (scrollY > 0) {
            villagerScrollOffset = Math.max(0, villagerScrollOffset - 1);
        } else if (scrollY < 0) {
            villagerScrollOffset = Math.min(maxScroll, villagerScrollOffset + 1);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 不绘制默认标签
    }

    // ================ 内部类 ================

    private static class VillagerInfo {
        String name;
        String profession;
        boolean isCombat;
    }
}
