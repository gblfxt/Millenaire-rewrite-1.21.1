package com.jasoncian.millenaire_rewrite.menu;

import com.jasoncian.millenaire_rewrite.blockentity.TownHallBlockEntity;
import com.jasoncian.millenaire_rewrite.core.ModMenuTypes;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 市政厅菜单 - 村庄管理界面的容器
 *
 * 提供：
 * - 村庄基本信息
 * - 村民列表访问
 * - 资源管理
 * - 建造项目管理
 *
 * @author Based on OldSource TownHall GUI
 * @version 1.0.0
 */
public class TownHallMenu extends AbstractContainerMenu {

    // ================ 数据 ================

    /** 市政厅方块实体 */
    @Nullable
    private final TownHallBlockEntity blockEntity;

    /** 市政厅位置 */
    private final BlockPos pos;

    /** 访问器 */
    private final ContainerLevelAccess access;

    /** 关联的Level */
    private final Level level;

    // ================ 客户端同步数据 ================

    /** 村庄名称 */
    private String villageName = "";

    /** 村庄文化ID */
    private String cultureId = "";

    /** 当前人口 */
    private int population = 0;

    /** 人口上限 */
    private int populationCap = 0;

    /** 建筑数量 */
    private int buildingCount = 0;

    /** 金币数量 */
    private long deniers = 0;

    // ================ 构造函数 ================

    /**
     * 服务端构造函数
     */
    public TownHallMenu(int containerId, Inventory playerInventory, TownHallBlockEntity blockEntity) {
        super(ModMenuTypes.TOWN_HALL.get(), containerId);
        this.blockEntity = blockEntity;
        this.pos = blockEntity.getBlockPos();
        this.level = playerInventory.player.level();
        this.access = ContainerLevelAccess.create(level, pos);

        // 初始化数据
        syncFromVillage();
    }

    /**
     * 客户端构造函数（从网络数据）
     */
    public TownHallMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.TOWN_HALL.get(), containerId);
        this.pos = buf.readBlockPos();
        this.level = playerInventory.player.level();
        this.access = ContainerLevelAccess.create(level, pos);

        // 从网络读取数据
        this.villageName = buf.readUtf();
        this.cultureId = buf.readUtf();
        this.population = buf.readInt();
        this.populationCap = buf.readInt();
        this.buildingCount = buf.readInt();
        this.deniers = buf.readLong();

        // 尝试获取BlockEntity
        BlockEntity be = level.getBlockEntity(pos);
        this.blockEntity = be instanceof TownHallBlockEntity ? (TownHallBlockEntity) be : null;
    }

    // ================ 数据同步 ================

    /**
     * 从村庄同步数据
     */
    private void syncFromVillage() {
        if (blockEntity == null) return;

        Village village = blockEntity.getVillage();
        if (village != null) {
            this.villageName = village.getName();
            this.cultureId = village.getCulture().getId();
            this.population = village.getActiveVillagerCount();
            this.populationCap = village.getVillagerSpawner().getPopulationCap();
            this.buildingCount = village.getBuildingCount();
            this.deniers = village.getDeniers();
        }
    }

    /**
     * 写入网络数据（服务端调用）
     */
    public static void writeToBuffer(FriendlyByteBuf buf, TownHallBlockEntity blockEntity) {
        buf.writeBlockPos(blockEntity.getBlockPos());

        Village village = blockEntity.getVillage();
        if (village != null) {
            buf.writeUtf(village.getName());
            buf.writeUtf(village.getCulture().getId());
            buf.writeInt(village.getActiveVillagerCount());
            buf.writeInt(village.getVillagerSpawner().getPopulationCap());
            buf.writeInt(village.getBuildingCount());
            buf.writeLong(village.getDeniers());
        } else {
            buf.writeUtf("Unknown Village");
            buf.writeUtf("norman");
            buf.writeInt(0);
            buf.writeInt(0);
            buf.writeInt(0);
            buf.writeLong(0);
        }
    }

    // ================ 菜单方法 ================

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // 没有物品槽位，直接返回空
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity != null ?
            blockEntity.getBlockState().getBlock() : null);
    }

    // ================ Getters ================

    public BlockPos getPos() {
        return pos;
    }

    @Nullable
    public TownHallBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Nullable
    public Village getVillage() {
        if (blockEntity != null) {
            return blockEntity.getVillage();
        }
        return null;
    }

    public String getVillageName() {
        return villageName;
    }

    public String getCultureId() {
        return cultureId;
    }

    public int getPopulation() {
        return population;
    }

    public int getPopulationCap() {
        return populationCap;
    }

    public int getBuildingCount() {
        return buildingCount;
    }

    public long getDeniers() {
        return deniers;
    }

    // ================ 操作方法 ================

    /**
     * 请求生成村民（由GUI按钮调用）
     */
    public void requestSpawnVillager() {
        if (level instanceof ServerLevel serverLevel && blockEntity != null) {
            Village village = blockEntity.getVillage();
            if (village != null) {
                village.getVillagerSpawner().forceSpawn(serverLevel, null);
                syncFromVillage();
            }
        }
    }

    /**
     * 刷新数据
     */
    public void refresh() {
        syncFromVillage();
    }
}
