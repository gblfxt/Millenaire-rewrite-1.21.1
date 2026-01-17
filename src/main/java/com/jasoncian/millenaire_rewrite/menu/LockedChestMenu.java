package com.jasoncian.millenaire_rewrite.menu;

import com.jasoncian.millenaire_rewrite.blockentity.LockedChestBlockEntity;
import com.jasoncian.millenaire_rewrite.core.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 锁定箱子菜单 - 基于OldSource ContainerLockedChest实现
 *
 * 管理锁定箱子的槽位布局和物品转移逻辑。
 * 支持权限控制，锁定时阻止物品取出。
 *
 * @author Based on OldSource ContainerLockedChest
 * @version 1.0.0
 */
public class LockedChestMenu extends AbstractContainerMenu {

    private final Container container;
    private final boolean locked;
    private final int rows;

    // 槽位范围
    private static final int CHEST_SLOTS = 27;

    /**
     * 客户端构造函数
     */
    public LockedChestMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(CHEST_SLOTS), false);
    }

    /**
     * 服务端构造函数
     */
    public LockedChestMenu(int containerId, Inventory playerInventory, Container container, boolean locked) {
        super(ModMenuTypes.LOCKED_CHEST.get(), containerId);
        checkContainerSize(container, CHEST_SLOTS);
        this.container = container;
        this.locked = locked;
        this.rows = 3;

        container.startOpen(playerInventory.player);

        // 添加箱子槽位
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIndex = col + row * 9;
                if (locked) {
                    addSlot(new LockedSlot(container, slotIndex, 8 + col * 18, 18 + row * 18));
                } else {
                    addSlot(new Slot(container, slotIndex, 8 + col * 18, 18 + row * 18));
                }
            }
        }

        // 添加玩家物品栏
        int playerInvY = 18 + rows * 18 + 13;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        // 添加玩家快捷栏
        int hotbarY = playerInvY + 58;
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 从箱子移动到玩家物品栏
            if (slotIndex < CHEST_SLOTS) {
                // 如果箱子被锁定，阻止取出
                if (locked) {
                    return ItemStack.EMPTY;
                }
                if (!this.moveItemStackTo(slotStack, CHEST_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            // 从玩家物品栏移动到箱子
            else {
                if (!this.moveItemStackTo(slotStack, 0, CHEST_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    /**
     * 检查箱子是否锁定
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * 获取行数
     */
    public int getRows() {
        return rows;
    }

    // ================ 自定义槽位类 ================

    /**
     * 锁定槽位 - 阻止取出物品
     */
    private static class LockedSlot extends Slot {
        public LockedSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            // 锁定时不允许取出
            return false;
        }
    }
}
