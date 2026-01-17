package com.jasoncian.millenaire_rewrite.menu;

import com.jasoncian.millenaire_rewrite.blockentity.ImportTableBlockEntity;
import com.jasoncian.millenaire_rewrite.core.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

/**
 * 导入桌菜单 - 基于OldSource实现
 *
 * 管理导入桌的GUI数据同步。
 * 这是一个纯数据菜单，没有物品槽位。
 *
 * @author Based on OldSource
 * @version 1.0.0
 */
public class ImportTableMenu extends AbstractContainerMenu {

    private final ImportTableBlockEntity blockEntity;
    private final ContainerData data;

    /**
     * 客户端构造函数
     */
    public ImportTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, null, new SimpleContainerData(10));
    }

    /**
     * 服务端构造函数
     */
    public ImportTableMenu(int containerId, Inventory playerInventory,
                           ImportTableBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.IMPORT_TABLE.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        // 没有槽位，不需要快速移动
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.getLevel() != null &&
            player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5
            ) <= 64.0;
    }

    // ================ 数据访问 ================

    public int getVariation() {
        return data.get(0);
    }

    public int getUpgradeLevel() {
        return data.get(1);
    }

    public int getLength() {
        return data.get(2);
    }

    public int getWidth() {
        return data.get(3);
    }

    public int getStartingLevel() {
        return data.get(4);
    }

    public int getOrientation() {
        return data.get(5);
    }

    public boolean isExportSnow() {
        return data.get(6) != 0;
    }

    public boolean isImportMockBlocks() {
        return data.get(7) != 0;
    }

    public boolean isAutoconvertToPreserveGround() {
        return data.get(8) != 0;
    }

    public boolean isExportRegularChests() {
        return data.get(9) != 0;
    }

    public String getOrientationName() {
        return switch (getOrientation()) {
            case 0 -> "North";
            case 1 -> "West";
            case 2 -> "South";
            case 3 -> "East";
            default -> "Unknown";
        };
    }

    public char getVariationLetter() {
        return (char) ('A' + getVariation());
    }

    public ImportTableBlockEntity getBlockEntity() {
        return blockEntity;
    }
}
