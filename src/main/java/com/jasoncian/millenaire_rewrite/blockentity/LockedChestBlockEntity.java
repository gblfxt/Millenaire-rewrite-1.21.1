package com.jasoncian.millenaire_rewrite.blockentity;

import com.jasoncian.millenaire_rewrite.core.ModBlockEntities;
import com.jasoncian.millenaire_rewrite.menu.LockedChestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 锁定箱子方块实体 - 基于OldSource TileEntityLockedChest实现
 *
 * 管理锁定箱子的存储、权限和动画。
 * 与村庄建筑系统集成，支持基于建筑所有权的访问控制。
 *
 * 功能特性：
 * - 27槽位存储
 * - 盖子开合动画
 * - 村庄权限系统（待完整实现）
 * - 双箱子支持（待完整实现）
 * - 声音效果
 *
 * @author Based on OldSource TileEntityLockedChest
 * @version 1.0.0
 */
public class LockedChestBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int SLOT_COUNT = 27;

    /** 物品存储 */
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    /** 自定义名称 */
    private Component customName;

    /** 建筑位置（用于权限检查） */
    @Nullable
    private BlockPos buildingPos;

    /** 是否锁定（临时实现，后续与村庄系统集成） */
    private boolean locked = false;

    /** 盖子角度（0.0-1.0） */
    public float lidAngle;
    public float prevLidAngle;

    /** 正在使用的玩家数量 */
    private int openCount;

    /** 动画tick计数器 */
    private int ticksSinceSync;

    public LockedChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOCKED_CHEST.get(), pos, state);
    }

    /**
     * 客户端tick - 处理盖子动画
     */
    public static void clientTick(Level level, BlockPos pos, BlockState state, LockedChestBlockEntity blockEntity) {
        blockEntity.prevLidAngle = blockEntity.lidAngle;

        if (blockEntity.openCount > 0 && blockEntity.lidAngle == 0.0F) {
            // 播放打开声音
            blockEntity.playSound(SoundEvents.CHEST_OPEN);
        }

        if (blockEntity.openCount == 0 && blockEntity.lidAngle > 0.0F || blockEntity.openCount > 0 && blockEntity.lidAngle < 1.0F) {
            float prevAngle = blockEntity.lidAngle;

            if (blockEntity.openCount > 0) {
                blockEntity.lidAngle += 0.1F;
            } else {
                blockEntity.lidAngle -= 0.1F;
            }

            blockEntity.lidAngle = Math.max(0.0F, Math.min(1.0F, blockEntity.lidAngle));

            if (blockEntity.lidAngle < 0.5F && prevAngle >= 0.5F) {
                // 播放关闭声音
                blockEntity.playSound(SoundEvents.CHEST_CLOSE);
            }
        }
    }

    private void playSound(SoundEvent sound) {
        if (level != null) {
            double x = worldPosition.getX() + 0.5;
            double y = worldPosition.getY() + 0.5;
            double z = worldPosition.getZ() + 0.5;
            level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    /**
     * 检查箱子是否对玩家锁定
     *
     * @param player 玩家
     * @return 是否锁定
     */
    public boolean isLockedFor(Player player) {
        // TODO: 与村庄建筑系统集成
        // 当前简单实现：检查locked标志
        // 完整实现应该检查：
        // 1. buildingPos是否存在
        // 2. 对应建筑是否存在
        // 3. building.lockedForPlayer(player)
        return locked && !player.isCreative();
    }

    /**
     * 设置锁定状态
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        setChanged();
    }

    /**
     * 设置建筑位置
     */
    public void setBuildingPos(@Nullable BlockPos pos) {
        this.buildingPos = pos;
        setChanged();
    }

    /**
     * 获取建筑位置
     */
    @Nullable
    public BlockPos getBuildingPos() {
        return buildingPos;
    }

    /**
     * 更新相邻箱子连接
     * TODO: 实现双箱子逻辑
     */
    public void updateNeighbors() {
        // 待实现：检查相邻方块是否为LockedChest并建立连接
    }

    /**
     * 设置自定义名称
     */
    public void setCustomName(Component name) {
        this.customName = name;
    }

    // ================ 玩家打开/关闭追踪 ================

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            openCount++;
            updateOpenCount();
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            openCount--;
            updateOpenCount();
        }
    }

    private void updateOpenCount() {
        if (level != null) {
            level.blockEntityChanged(worldPosition);
        }
    }

    // ================ 保存/加载 ================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);

        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName, registries));
        }

        if (buildingPos != null) {
            tag.putInt("BuildingX", buildingPos.getX());
            tag.putInt("BuildingY", buildingPos.getY());
            tag.putInt("BuildingZ", buildingPos.getZ());
        }

        tag.putBoolean("Locked", locked);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);

        if (tag.contains("CustomName", 8)) {
            customName = Component.Serializer.fromJson(tag.getString("CustomName"), registries);
        }

        if (tag.contains("BuildingX")) {
            buildingPos = new BlockPos(
                tag.getInt("BuildingX"),
                tag.getInt("BuildingY"),
                tag.getInt("BuildingZ")
            );
        }

        locked = tag.getBoolean("Locked");
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

    // ================ MenuProvider实现 ================

    @Override
    public Component getDisplayName() {
        return customName != null ? customName :
            Component.translatable("container.millenaire_rewrite.locked_chest");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new LockedChestMenu(containerId, playerInventory, this, isLockedFor(player));
    }
}
