package com.jasoncian.millenaire_rewrite.menu;

import com.jasoncian.millenaire_rewrite.blockentity.FirePitBlockEntity;
import com.jasoncian.millenaire_rewrite.core.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * 火坑菜单 - 基于OldSource ContainerFirePit实现
 *
 * 管理火坑的槽位布局和物品转移逻辑。
 *
 * 槽位布局（基于OldSource）：
 * - 输入槽位：(56,8), (44,28), (56,48)
 * - 燃料槽位：(80,70)
 * - 输出槽位：(104,8), (116,28), (104,48)
 *
 * @author Based on OldSource ContainerFirePit
 * @version 1.0.0
 */
public class FirePitMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;
    private final Level level;

    // 槽位索引
    private static final int INPUT_START = 0;
    private static final int INPUT_END = 3;
    private static final int FUEL_SLOT = 3;
    private static final int OUTPUT_START = 4;
    private static final int OUTPUT_END = 7;

    // 玩家物品栏槽位范围
    private static final int PLAYER_INV_START = 7;
    private static final int PLAYER_INV_END = 34;
    private static final int PLAYER_HOTBAR_START = 34;
    private static final int PLAYER_HOTBAR_END = 43;

    /**
     * 客户端构造函数
     */
    public FirePitMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(7), new SimpleContainerData(5));
    }

    /**
     * 服务端构造函数
     */
    public FirePitMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.FIRE_PIT.get(), containerId);
        checkContainerSize(container, 7);
        checkContainerDataCount(data, 5);
        this.container = container;
        this.data = data;
        this.level = playerInventory.player.level();

        // 添加输入槽位
        addSlot(new FirePitInputSlot(container, 0, 56, 8));
        addSlot(new FirePitInputSlot(container, 1, 44, 28));
        addSlot(new FirePitInputSlot(container, 2, 56, 48));

        // 添加燃料槽位
        addSlot(new FirePitFuelSlot(container, 3, 80, 70));

        // 添加输出槽位
        addSlot(new FirePitOutputSlot(container, 4, 104, 8));
        addSlot(new FirePitOutputSlot(container, 5, 116, 28));
        addSlot(new FirePitOutputSlot(container, 6, 104, 48));

        // 添加玩家物品栏
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 93 + row * 18));
            }
        }

        // 添加玩家快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 151));
        }

        // 添加数据追踪
        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            // 从输出槽位移动
            if (slotIndex >= OUTPUT_START && slotIndex < OUTPUT_END) {
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, itemstack);
            }
            // 从玩家物品栏移动
            else if (slotIndex >= PLAYER_INV_START) {
                // 尝试放入输入槽位（如果是可烹饪物品）
                if (isFirePitBurnable(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, INPUT_START, INPUT_END, false)) {
                        // 尝试放入燃料槽位
                        if (isFuel(slotStack)) {
                            if (!this.moveItemStackTo(slotStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // 尝试放入燃料槽位
                else if (isFuel(slotStack)) {
                    if (!this.moveItemStackTo(slotStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // 在物品栏和快捷栏之间移动
                else if (slotIndex < PLAYER_HOTBAR_START) {
                    if (!this.moveItemStackTo(slotStack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
            // 从输入或燃料槽位移动
            else {
                if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_HOTBAR_END, false)) {
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

    /**
     * 检查物品是否可以在火坑中烹饪
     */
    private boolean isFirePitBurnable(ItemStack stack) {
        if (container instanceof FirePitBlockEntity firePit) {
            return firePit.isFirePitBurnable(level, stack);
        }
        // 回退检查：是否是食物
        return stack.getFoodProperties(null) != null;
    }

    /**
     * 检查物品是否是燃料
     */
    private boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0;
    }

    // ================ 数据访问 ================

    public int getCookingProgress(int slot) {
        return data.get(slot);
    }

    public int getBurnTime() {
        return data.get(3);
    }

    public int getBurnTimeTotal() {
        return data.get(4);
    }

    public boolean isLit() {
        return getBurnTime() > 0;
    }

    /**
     * 获取烹饪进度（0-1）
     */
    public float getCookingProgressScaled(int slot) {
        int progress = getCookingProgress(slot);
        return progress > 0 ? (float) progress / FirePitBlockEntity.COOK_TIME : 0;
    }

    /**
     * 获取燃烧进度（0-1）
     */
    public float getBurnProgressScaled() {
        int burnTimeTotal = getBurnTimeTotal();
        if (burnTimeTotal == 0) {
            return 0;
        }
        return (float) getBurnTime() / burnTimeTotal;
    }

    // ================ 自定义槽位类 ================

    /**
     * 火坑输入槽位 - 只接受可烹饪物品
     */
    private class FirePitInputSlot extends Slot {
        public FirePitInputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isFirePitBurnable(stack);
        }
    }

    /**
     * 火坑燃料槽位 - 只接受燃料
     */
    private class FirePitFuelSlot extends Slot {
        public FirePitFuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isFuel(stack);
        }
    }

    /**
     * 火坑输出槽位 - 只能取出物品
     */
    private static class FirePitOutputSlot extends Slot {
        public FirePitOutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
