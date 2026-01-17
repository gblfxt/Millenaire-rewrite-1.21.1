package com.jasoncian.millenaire_rewrite.blockentity;

import com.jasoncian.millenaire_rewrite.blocks.functional.FirePitBlock;
import com.jasoncian.millenaire_rewrite.core.ModBlockEntities;
import com.jasoncian.millenaire_rewrite.menu.FirePitMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 火坑方块实体 - 基于OldSource TileEntityFirePit实现
 *
 * 管理火坑的烹饪逻辑、库存和燃烧状态。
 * 支持同时烹饪3个物品，使用标准熔炉配方。
 *
 * 槽位分配：
 * - 0-2: 输入槽位（待烹饪物品）
 * - 3: 燃料槽位
 * - 4-6: 输出槽位（烹饪完成物品）
 *
 * @author Based on OldSource TileEntityFirePit
 * @version 1.0.0
 */
public class FirePitBlockEntity extends BlockEntity implements MenuProvider, WorldlyContainer {

    /** 槽位常量 */
    public static final int INPUT_SLOT_1 = 0;
    public static final int INPUT_SLOT_2 = 1;
    public static final int INPUT_SLOT_3 = 2;
    public static final int FUEL_SLOT = 3;
    public static final int OUTPUT_SLOT_1 = 4;
    public static final int OUTPUT_SLOT_2 = 5;
    public static final int OUTPUT_SLOT_3 = 6;
    public static final int SLOT_COUNT = 7;

    /** 烹饪时间常量 */
    public static final int COOK_TIME = 200; // 10秒

    /** 物品存储 */
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    /** 烹饪进度（每个输入槽位独立） */
    private int[] cookingProgress = new int[3];

    /** 燃烧时间 */
    private int burnTime = 0;

    /** 当前燃料的总燃烧时间 */
    private int burnTimeTotal = 0;

    /** 用于GUI同步的容器数据 */
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookingProgress[0];
                case 1 -> cookingProgress[1];
                case 2 -> cookingProgress[2];
                case 3 -> burnTime;
                case 4 -> burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookingProgress[0] = value;
                case 1 -> cookingProgress[1] = value;
                case 2 -> cookingProgress[2] = value;
                case 3 -> burnTime = value;
                case 4 -> burnTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 5;
        }
    };

    public FirePitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIRE_PIT.get(), pos, state);
    }

    /**
     * 服务端tick处理
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, FirePitBlockEntity blockEntity) {
        boolean wasLit = blockEntity.isLit();
        boolean changed = false;

        // 处理燃烧
        if (blockEntity.isLit()) {
            blockEntity.burnTime--;
        }

        // 检查是否有物品需要烹饪
        boolean hasCookableItem = false;
        for (int i = 0; i < 3; i++) {
            if (blockEntity.canCookSlot(level, i)) {
                hasCookableItem = true;
                break;
            }
        }

        // 如果需要燃烧且有可烹饪物品，消耗燃料
        if (!blockEntity.isLit() && hasCookableItem) {
            ItemStack fuel = blockEntity.items.get(FUEL_SLOT);
            if (!fuel.isEmpty()) {
                int fuelTime = blockEntity.getBurnDuration(fuel);
                if (fuelTime > 0) {
                    blockEntity.burnTime = fuelTime;
                    blockEntity.burnTimeTotal = fuelTime;
                    fuel.shrink(1);

                    // 处理桶等容器物品
                    if (fuel.isEmpty()) {
                        Item remainingItem = fuel.getItem().getCraftingRemainingItem();
                        blockEntity.items.set(FUEL_SLOT, remainingItem != null ?
                            new ItemStack(remainingItem) : ItemStack.EMPTY);
                    }
                    changed = true;
                }
            }
        }

        // 处理烹饪进度
        if (blockEntity.isLit()) {
            for (int i = 0; i < 3; i++) {
                if (blockEntity.canCookSlot(level, i)) {
                    blockEntity.cookingProgress[i]++;
                    if (blockEntity.cookingProgress[i] >= COOK_TIME) {
                        blockEntity.cookItem(level, i);
                        blockEntity.cookingProgress[i] = 0;
                        changed = true;
                    }
                } else {
                    // 如果无法继续烹饪，重置进度
                    if (blockEntity.cookingProgress[i] > 0) {
                        blockEntity.cookingProgress[i] = 0;
                        changed = true;
                    }
                }
            }
        } else {
            // 不燃烧时，烹饪进度逐渐降低
            for (int i = 0; i < 3; i++) {
                if (blockEntity.cookingProgress[i] > 0) {
                    blockEntity.cookingProgress[i] = Math.max(0, blockEntity.cookingProgress[i] - 2);
                    changed = true;
                }
            }
        }

        // 更新方块状态
        if (wasLit != blockEntity.isLit()) {
            FirePitBlock.setLit(level, pos, state, blockEntity.isLit());
            changed = true;
        }

        if (changed) {
            blockEntity.setChanged();
        }
    }

    /**
     * 检查是否正在燃烧
     */
    public boolean isLit() {
        return burnTime > 0;
    }

    /**
     * 检查指定槽位是否可以烹饪
     */
    private boolean canCookSlot(Level level, int inputIndex) {
        ItemStack input = items.get(inputIndex);
        if (input.isEmpty()) {
            return false;
        }

        // 获取烹饪结果
        Optional<ItemStack> result = getCookingResult(level, input);
        if (result.isEmpty()) {
            return false;
        }

        ItemStack resultStack = result.get();
        int outputSlot = OUTPUT_SLOT_1 + inputIndex;
        ItemStack currentOutput = items.get(outputSlot);

        if (currentOutput.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItem(currentOutput, resultStack)) {
            return false;
        }
        return currentOutput.getCount() + resultStack.getCount() <= currentOutput.getMaxStackSize();
    }

    /**
     * 烹饪指定槽位的物品
     */
    private void cookItem(Level level, int inputIndex) {
        ItemStack input = items.get(inputIndex);
        if (input.isEmpty()) {
            return;
        }

        Optional<ItemStack> result = getCookingResult(level, input);
        if (result.isEmpty()) {
            return;
        }

        ItemStack resultStack = result.get().copy();
        int outputSlot = OUTPUT_SLOT_1 + inputIndex;
        ItemStack currentOutput = items.get(outputSlot);

        if (currentOutput.isEmpty()) {
            items.set(outputSlot, resultStack);
        } else if (ItemStack.isSameItem(currentOutput, resultStack)) {
            currentOutput.grow(resultStack.getCount());
        }

        input.shrink(1);
    }

    /**
     * 获取烹饪结果
     */
    private Optional<ItemStack> getCookingResult(Level level, ItemStack input) {
        if (level == null) {
            return Optional.empty();
        }
        RecipeManager recipeManager = level.getRecipeManager();
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<RecipeHolder<SmeltingRecipe>> recipe = recipeManager.getRecipeFor(
            RecipeType.SMELTING, recipeInput, level);

        return recipe.map(holder -> holder.value().assemble(recipeInput, level.registryAccess()));
    }

    /**
     * 获取燃料燃烧时间
     */
    private int getBurnDuration(ItemStack fuel) {
        if (fuel.isEmpty()) {
            return 0;
        }
        // 使用NeoForge 1.21.1 API获取燃烧时间
        return fuel.getBurnTime(RecipeType.SMELTING);
    }

    /**
     * 检查物品是否可以在火坑中烹饪
     * 只允许食物或烹饪后变成食物的物品
     */
    public boolean isFirePitBurnable(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // 如果是食物，直接允许
        if (stack.getFoodProperties(null) != null) {
            return true;
        }

        // 检查烹饪结果是否是食物
        Optional<ItemStack> result = getCookingResult(level, stack);
        if (result.isPresent()) {
            ItemStack resultStack = result.get();
            return resultStack.getFoodProperties(null) != null;
        }

        return false;
    }

    // ================ 保存/加载 ================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putIntArray("CookingProgress", cookingProgress);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        if (tag.contains("CookingProgress")) {
            int[] progress = tag.getIntArray("CookingProgress");
            System.arraycopy(progress, 0, cookingProgress, 0, Math.min(progress.length, 3));
        }
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
    }

    // ================ Container实现 ================

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this &&
            player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    // ================ WorldlyContainer实现（自动化支持） ================

    private static final int[] SLOTS_FOR_UP = {INPUT_SLOT_1, INPUT_SLOT_2, INPUT_SLOT_3};
    private static final int[] SLOTS_FOR_DOWN = {OUTPUT_SLOT_1, OUTPUT_SLOT_2, OUTPUT_SLOT_3};
    private static final int[] SLOTS_FOR_SIDES = {FUEL_SLOT};

    @Override
    public int[] getSlotsForFace(net.minecraft.core.Direction side) {
        if (side == net.minecraft.core.Direction.UP) {
            return SLOTS_FOR_UP;
        } else if (side == net.minecraft.core.Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        return SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable net.minecraft.core.Direction direction) {
        if (slot >= INPUT_SLOT_1 && slot <= INPUT_SLOT_3) {
            return isFirePitBurnable(level, stack);
        } else if (slot == FUEL_SLOT) {
            return getBurnDuration(stack) > 0;
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, net.minecraft.core.Direction direction) {
        // 只能从输出槽位取出物品
        return slot >= OUTPUT_SLOT_1 && slot <= OUTPUT_SLOT_3;
    }

    // ================ MenuProvider实现 ================

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.millenaire_rewrite.fire_pit");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new FirePitMenu(containerId, playerInventory, this, dataAccess);
    }
}
