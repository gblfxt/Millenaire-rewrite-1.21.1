package com.jasoncian.millenaire_rewrite.blockentity;

import com.jasoncian.millenaire_rewrite.core.ModBlockEntities;
import com.jasoncian.millenaire_rewrite.menu.ImportTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 导入桌方块实体 - 基于OldSource TileEntityImportTable实现
 *
 * 管理建筑模板的导入/导出配置。
 * 存储建筑标识、变体、升级级别、尺寸和方向等数据。
 *
 * 功能特性：
 * - 建筑模板标识和变体管理
 * - 尺寸和方向配置
 * - 导出选项（雪层、模拟方块等）
 * - 父建筑引用（用于层级建筑）
 *
 * @author Based on OldSource TileEntityImportTable
 * @version 1.0.0
 */
public class ImportTableBlockEntity extends BlockEntity implements MenuProvider {

    // ================ 建筑标识 ================

    /** 建筑模板键（唯一标识符） */
    private String buildingKey = "";

    /** 建筑变体索引（A=0, B=1, C=2...） */
    private int variation = 0;

    /** 升级级别 */
    private int upgradeLevel = 0;

    // ================ 尺寸配置 ================

    /** 建筑长度（X轴） */
    private int length = 10;

    /** 建筑宽度（Z轴） */
    private int width = 10;

    /** 建筑起始Y偏移 */
    private int startingLevel = 0;

    /** 建筑方向（0=北, 1=西, 2=南, 3=东） */
    private int orientation = 0;

    // ================ 导出选项 ================

    /** 导出时包含雪层 */
    private boolean exportSnow = false;

    /** 导入时包含模拟方块（动物生成点等） */
    private boolean importMockBlocks = true;

    /** 自动转换为保留地面 */
    private boolean autoconvertToPreserveGround = true;

    /** 导出普通箱子 */
    private boolean exportRegularChests = false;

    // ================ 父建筑引用 ================

    /** 父导入桌位置（用于层级建筑） */
    @Nullable
    private BlockPos parentTablePos;

    // ================ 容器数据同步 ================

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> variation;
                case 1 -> upgradeLevel;
                case 2 -> length;
                case 3 -> width;
                case 4 -> startingLevel;
                case 5 -> orientation;
                case 6 -> exportSnow ? 1 : 0;
                case 7 -> importMockBlocks ? 1 : 0;
                case 8 -> autoconvertToPreserveGround ? 1 : 0;
                case 9 -> exportRegularChests ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> variation = value;
                case 1 -> upgradeLevel = value;
                case 2 -> length = value;
                case 3 -> width = value;
                case 4 -> startingLevel = value;
                case 5 -> orientation = value;
                case 6 -> exportSnow = value != 0;
                case 7 -> importMockBlocks = value != 0;
                case 8 -> autoconvertToPreserveGround = value != 0;
                case 9 -> exportRegularChests = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public ImportTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.IMPORT_TABLE.get(), pos, state);
    }

    // ================ Getters/Setters ================

    public String getBuildingKey() {
        return buildingKey;
    }

    public void setBuildingKey(String buildingKey) {
        this.buildingKey = buildingKey;
        setChanged();
    }

    public int getVariation() {
        return variation;
    }

    public void setVariation(int variation) {
        this.variation = variation;
        setChanged();
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = upgradeLevel;
        setChanged();
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
        setChanged();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
        setChanged();
    }

    public int getStartingLevel() {
        return startingLevel;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
        setChanged();
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation % 4;
        setChanged();
    }

    public boolean isExportSnow() {
        return exportSnow;
    }

    public void setExportSnow(boolean exportSnow) {
        this.exportSnow = exportSnow;
        setChanged();
    }

    public boolean isImportMockBlocks() {
        return importMockBlocks;
    }

    public void setImportMockBlocks(boolean importMockBlocks) {
        this.importMockBlocks = importMockBlocks;
        setChanged();
    }

    public boolean isAutoconvertToPreserveGround() {
        return autoconvertToPreserveGround;
    }

    public void setAutoconvertToPreserveGround(boolean autoconvertToPreserveGround) {
        this.autoconvertToPreserveGround = autoconvertToPreserveGround;
        setChanged();
    }

    public boolean isExportRegularChests() {
        return exportRegularChests;
    }

    public void setExportRegularChests(boolean exportRegularChests) {
        this.exportRegularChests = exportRegularChests;
        setChanged();
    }

    @Nullable
    public BlockPos getParentTablePos() {
        return parentTablePos;
    }

    public void setParentTablePos(@Nullable BlockPos parentTablePos) {
        this.parentTablePos = parentTablePos;
        setChanged();
    }

    public ContainerData getDataAccess() {
        return dataAccess;
    }

    /**
     * 获取方向名称
     */
    public String getOrientationName() {
        return switch (orientation) {
            case 0 -> "North";
            case 1 -> "West";
            case 2 -> "South";
            case 3 -> "East";
            default -> "Unknown";
        };
    }

    /**
     * 获取变体字母
     */
    public char getVariationLetter() {
        return (char) ('A' + variation);
    }

    // ================ 建筑操作方法 ================

    /**
     * 导入建筑模板
     * TODO: 实现与BuildingPlan系统的集成
     */
    public boolean importBuilding(Player player) {
        if (buildingKey.isEmpty()) {
            player.displayClientMessage(
                Component.translatable("message.millenaire_rewrite.import_table.no_plan"),
                true);
            return false;
        }
        // TODO: 实际导入逻辑
        player.displayClientMessage(
            Component.translatable("message.millenaire_rewrite.import_table.import_not_implemented"),
            true);
        return false;
    }

    /**
     * 导出建筑模板
     * TODO: 实现与BuildingPlan系统的集成
     */
    public boolean exportBuilding(Player player) {
        if (length <= 0 || width <= 0) {
            player.displayClientMessage(
                Component.translatable("message.millenaire_rewrite.import_table.invalid_dimensions"),
                true);
            return false;
        }
        // TODO: 实际导出逻辑
        player.displayClientMessage(
            Component.translatable("message.millenaire_rewrite.import_table.export_not_implemented"),
            true);
        return false;
    }

    /**
     * 创建新建筑区域（用羊毛标记）
     * TODO: 实现
     */
    public void createNewBuildingArea() {
        // TODO: 用羊毛标记建筑边界
    }

    // ================ 保存/加载 ================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putString("BuildingKey", buildingKey);
        tag.putInt("Variation", variation);
        tag.putInt("UpgradeLevel", upgradeLevel);
        tag.putInt("Length", length);
        tag.putInt("Width", width);
        tag.putInt("StartingLevel", startingLevel);
        tag.putInt("Orientation", orientation);

        tag.putBoolean("ExportSnow", exportSnow);
        tag.putBoolean("ImportMockBlocks", importMockBlocks);
        tag.putBoolean("AutoconvertToPreserveGround", autoconvertToPreserveGround);
        tag.putBoolean("ExportRegularChests", exportRegularChests);

        if (parentTablePos != null) {
            tag.putInt("ParentX", parentTablePos.getX());
            tag.putInt("ParentY", parentTablePos.getY());
            tag.putInt("ParentZ", parentTablePos.getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        buildingKey = tag.getString("BuildingKey");
        variation = tag.getInt("Variation");
        upgradeLevel = tag.getInt("UpgradeLevel");
        length = tag.getInt("Length");
        width = tag.getInt("Width");
        startingLevel = tag.getInt("StartingLevel");
        orientation = tag.getInt("Orientation");

        exportSnow = tag.getBoolean("ExportSnow");
        importMockBlocks = tag.getBoolean("ImportMockBlocks");
        autoconvertToPreserveGround = tag.getBoolean("AutoconvertToPreserveGround");
        exportRegularChests = tag.getBoolean("ExportRegularChests");

        if (tag.contains("ParentX")) {
            parentTablePos = new BlockPos(
                tag.getInt("ParentX"),
                tag.getInt("ParentY"),
                tag.getInt("ParentZ")
            );
        } else {
            parentTablePos = null;
        }
    }

    // ================ MenuProvider实现 ================

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.millenaire_rewrite.import_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ImportTableMenu(containerId, playerInventory, this, dataAccess);
    }
}
