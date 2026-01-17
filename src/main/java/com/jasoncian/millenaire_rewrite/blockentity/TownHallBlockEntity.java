package com.jasoncian.millenaire_rewrite.blockentity;

import com.jasoncian.millenaire_rewrite.core.ModBlockEntities;
import com.jasoncian.millenaire_rewrite.menu.TownHallMenu;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 市政厅方块实体 - 管理村庄数据链接和tick更新
 *
 * @author Based on OldSource TownHall
 * @version 1.0.0
 */
public class TownHallBlockEntity extends BlockEntity implements MenuProvider {

    /** 关联的村庄ID */
    @Nullable
    private UUID villageId;

    /** 缓存的村庄引用 */
    @Nullable
    private Village cachedVillage;

    /** tick计数器 */
    private int tickCount = 0;

    public TownHallBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TOWN_HALL.get(), pos, state);
    }

    // ================ Tick更新 ================

    /**
     * 服务端tick
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  TownHallBlockEntity blockEntity) {
        blockEntity.tickCount++;

        // 每20tick（1秒）更新一次村庄
        if (blockEntity.tickCount % 20 == 0) {
            blockEntity.updateVillage();
        }
    }

    /**
     * 更新村庄
     */
    private void updateVillage() {
        if (level == null || level.isClientSide()) return;

        Village village = getVillage();
        if (village != null && level instanceof ServerLevel serverLevel) {
            // 触发村庄tick（VillageManager也会调用，这里是额外的同步点）
            // 可以在这里处理特定于市政厅的逻辑

            // 例如：检查村庄边界、生成村民等
        }
    }

    // ================ 村庄访问 ================

    /**
     * 获取关联的村庄
     */
    @Nullable
    public Village getVillage() {
        if (villageId == null) {
            return null;
        }

        // 使用缓存
        if (cachedVillage != null && cachedVillage.getVillageId().equals(villageId)) {
            return cachedVillage;
        }

        // 从VillageManager获取
        if (level instanceof ServerLevel serverLevel) {
            VillageManager manager = VillageManager.get(serverLevel);
            cachedVillage = manager.getVillage(villageId);
            return cachedVillage;
        }

        return null;
    }

    /**
     * 设置村庄ID
     */
    public void setVillageId(@Nullable UUID villageId) {
        this.villageId = villageId;
        this.cachedVillage = null; // 清除缓存
        setChanged();
    }

    /**
     * 获取村庄ID
     */
    @Nullable
    public UUID getVillageId() {
        return villageId;
    }

    // ================ MenuProvider实现 ================

    @Override
    public Component getDisplayName() {
        Village village = getVillage();
        if (village != null) {
            return Component.literal(village.getName());
        }
        return Component.translatable("block.millenaire_rewrite.town_hall");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new TownHallMenu(containerId, playerInventory, this);
    }

    // ================ NBT序列化 ================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (villageId != null) {
            tag.putUUID("VillageId", villageId);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("VillageId")) {
            villageId = tag.getUUID("VillageId");
        } else {
            villageId = null;
        }
        cachedVillage = null; // 清除缓存，下次访问时重新获取
    }
}
