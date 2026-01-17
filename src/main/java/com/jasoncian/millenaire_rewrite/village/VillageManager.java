package com.jasoncian.millenaire_rewrite.village;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 村庄管理器 - 管理世界中所有Millenaire村庄
 *
 * 基于OldSource MillWorldData.java
 * 职责：
 * - 注册和追踪所有村庄
 * - 保存和加载村庄数据
 * - 提供村庄查询方法
 * - 每tick更新所有村庄
 *
 * @author Based on OldSource MillWorldData
 * @version 1.0.0
 */
public class VillageManager extends SavedData {

    private static final String DATA_NAME = "millenaire_villages";

    // ================ 村庄存储 ================

    /** 所有村庄（UUID -> Village） */
    private final ConcurrentHashMap<UUID, Village> villages = new ConcurrentHashMap<>();

    /** 按位置索引的村庄（市政厅位置 -> Village） */
    private final ConcurrentHashMap<BlockPos, Village> villagesByPos = new ConcurrentHashMap<>();

    /** 所属Level */
    @Nullable
    private ServerLevel level;

    // ================ 构造函数 ================

    public VillageManager() {
        // 默认构造函数
    }

    /**
     * 从NBT加载的工厂方法
     */
    public static VillageManager load(CompoundTag tag, HolderLookup.Provider registries) {
        VillageManager manager = new VillageManager();
        manager.loadFromTag(tag);
        return manager;
    }

    // ================ 获取实例 ================

    /**
     * 获取指定Level的VillageManager
     */
    public static VillageManager get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        VillageManager manager = storage.computeIfAbsent(
            new SavedData.Factory<>(VillageManager::new, VillageManager::load),
            DATA_NAME
        );
        manager.level = level;
        return manager;
    }

    // ================ 村庄管理 ================

    /**
     * 创建新村庄
     */
    public Village createVillage(BlockPos townHallPos, Culture culture) {
        // 检查位置是否已有村庄
        if (getVillageAt(townHallPos) != null) {
            MillenaireRewrite.LOGGER.warn("Attempted to create village at existing location: {}", townHallPos);
            return null;
        }

        // 检查是否与其他村庄太近
        Village nearby = getNearestVillage(townHallPos);
        if (nearby != null && nearby.getTownHallPos().distSqr(townHallPos) < 100 * 100) {
            MillenaireRewrite.LOGGER.warn("Attempted to create village too close to existing village at: {}",
                nearby.getTownHallPos());
            return null;
        }

        Village village = new Village(townHallPos, culture);
        if (level != null) {
            village.setDimension(level.dimension().location().toString());
        }

        registerVillage(village);

        MillenaireRewrite.LOGGER.info("Created new village '{}' at {}", village.getName(), townHallPos);

        return village;
    }

    /**
     * 注册村庄
     */
    public void registerVillage(Village village) {
        villages.put(village.getVillageId(), village);
        villagesByPos.put(village.getTownHallPos(), village);
        setDirty();
    }

    /**
     * 移除村庄
     */
    public void removeVillage(UUID villageId) {
        Village village = villages.remove(villageId);
        if (village != null) {
            villagesByPos.remove(village.getTownHallPos());
            setDirty();

            MillenaireRewrite.LOGGER.info("Removed village '{}' ({})", village.getName(), villageId);
        }
    }

    /**
     * 移除村庄
     */
    public void removeVillage(Village village) {
        removeVillage(village.getVillageId());
    }

    // ================ 村庄查询 ================

    /**
     * 通过UUID获取村庄
     */
    @Nullable
    public Village getVillage(UUID villageId) {
        return villages.get(villageId);
    }

    /**
     * 通过市政厅位置获取村庄
     */
    @Nullable
    public Village getVillageAt(BlockPos pos) {
        return villagesByPos.get(pos);
    }

    /**
     * 获取包含指定位置的村庄
     */
    @Nullable
    public Village getVillageContaining(BlockPos pos) {
        for (Village village : villages.values()) {
            if (village.isInVillageArea(pos)) {
                return village;
            }
        }
        return null;
    }

    /**
     * 获取最近的村庄
     */
    @Nullable
    public Village getNearestVillage(BlockPos pos) {
        Village nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Village village : villages.values()) {
            double dist = village.getTownHallPos().distSqr(pos);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = village;
            }
        }

        return nearest;
    }

    /**
     * 获取指定范围内的村庄
     */
    public List<Village> getVillagesInRange(BlockPos center, double radius) {
        List<Village> result = new ArrayList<>();
        double radiusSq = radius * radius;

        for (Village village : villages.values()) {
            if (village.getTownHallPos().distSqr(center) <= radiusSq) {
                result.add(village);
            }
        }

        return result;
    }

    /**
     * 获取指定文化的村庄
     */
    public List<Village> getVillagesByCulture(Culture culture) {
        List<Village> result = new ArrayList<>();

        for (Village village : villages.values()) {
            if (village.getCulture() == culture) {
                result.add(village);
            }
        }

        return result;
    }

    /**
     * 获取所有村庄
     */
    public Collection<Village> getAllVillages() {
        return Collections.unmodifiableCollection(villages.values());
    }

    /**
     * 获取村庄数量
     */
    public int getVillageCount() {
        return villages.size();
    }

    // ================ Tick更新 ================

    /**
     * 每tick更新所有村庄
     * 应该在ServerLevel tick时调用
     */
    public void tick() {
        if (level == null) return;

        for (Village village : villages.values()) {
            village.tick(level);

            // 如果村庄数据被修改，标记需要保存
            if (village.isDirty()) {
                village.clearDirty();
                setDirty();
            }
        }
    }

    // ================ NBT序列化 ================

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag villagesTag = new ListTag();

        for (Village village : villages.values()) {
            villagesTag.add(village.save());
        }

        tag.put("Villages", villagesTag);
        tag.putInt("Version", 1);

        return tag;
    }

    private void loadFromTag(CompoundTag tag) {
        villages.clear();
        villagesByPos.clear();

        ListTag villagesTag = tag.getList("Villages", 10);
        for (int i = 0; i < villagesTag.size(); i++) {
            CompoundTag villageTag = villagesTag.getCompound(i);

            UUID id = villageTag.getUUID("VillageId");
            Village village = new Village(id);
            village.load(villageTag);

            villages.put(id, village);
            villagesByPos.put(village.getTownHallPos(), village);
        }

        MillenaireRewrite.LOGGER.info("Loaded {} villages from saved data", villages.size());
    }

    // ================ 调试方法 ================

    /**
     * 获取调试信息
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Millenaire Villages ===\n");
        sb.append("Total villages: ").append(villages.size()).append("\n");

        for (Village village : villages.values()) {
            sb.append("\n").append(village.toString());
        }

        return sb.toString();
    }
}
