package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端村庄缓存 - 存储从服务端同步的村庄数据
 *
 * 用于客户端GUI显示：
 * - 村庄信息
 * - 村民数据
 * - 任务进度
 * - 经济数据
 * - 声望
 *
 * @author Based on client-side caching patterns
 * @version 1.0.0
 */
@OnlyIn(Dist.CLIENT)
public class ClientVillageCache {

    // ================ 缓存数据 ================

    /** 村庄数据缓存 */
    private static final Map<UUID, CachedVillage> villageCache = new ConcurrentHashMap<>();

    /** 村民数据缓存 */
    private static final Map<UUID, CachedVillager> villagerCache = new ConcurrentHashMap<>();

    /** 任务数据缓存 */
    private static final Map<String, CachedQuest> questCache = new ConcurrentHashMap<>();

    /** 声望数据缓存 */
    private static final Map<UUID, CachedReputation> reputationCache = new ConcurrentHashMap<>();

    /** 经济数据缓存 */
    private static final Map<UUID, CachedEconomy> economyCache = new ConcurrentHashMap<>();

    // ================ 数据类 ================

    /**
     * 缓存的村庄数据
     */
    public static class CachedVillage {
        public final UUID id;
        public final String name;
        public final BlockPos position;
        public final String dimension;
        public final String cultureId;
        public int villagerCount;
        public int buildingCount;
        public long deniers;
        public int level;
        public boolean isActive;
        public long lastUpdate;

        public CachedVillage(VillageSyncPacket packet) {
            this.id = packet.villageId();
            this.name = packet.name();
            this.position = packet.townHallPos();
            this.dimension = packet.dimension();
            this.cultureId = packet.cultureId();
            this.villagerCount = packet.villagerCount();
            this.buildingCount = packet.buildingCount();
            this.deniers = packet.deniers();
            this.level = packet.level();
            this.isActive = packet.isActive();
            this.lastUpdate = System.currentTimeMillis();
        }

        public void update(VillageSyncPacket packet) {
            this.villagerCount = packet.villagerCount();
            this.buildingCount = packet.buildingCount();
            this.deniers = packet.deniers();
            this.level = packet.level();
            this.isActive = packet.isActive();
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    /**
     * 缓存的村民数据
     */
    public static class CachedVillager {
        public final UUID uuid;
        public final int entityId;
        public String name;
        public String profession;
        public int gender;
        public UUID villageId;
        public String villageName;
        public int health;
        public int maxHealth;
        public boolean isWorking;
        public String currentTask;
        public long lastUpdate;

        public CachedVillager(VillagerSyncPacket packet) {
            this.uuid = packet.villagerUuid();
            this.entityId = packet.entityId();
            update(packet);
        }

        public void update(VillagerSyncPacket packet) {
            this.name = packet.name();
            this.profession = packet.profession();
            this.gender = packet.gender();
            this.villageId = packet.villageId();
            this.villageName = packet.villageName();
            this.health = packet.health();
            this.maxHealth = packet.maxHealth();
            this.isWorking = packet.isWorking();
            this.currentTask = packet.currentTask();
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    /**
     * 缓存的任务数据
     */
    public static class CachedQuest {
        public final String questId;
        public String title;
        public String description;
        public String status;
        public UUID villageId;
        public String villageName;
        public int denierReward;
        public int reputationReward;
        public List<CachedObjective> objectives;
        public long lastUpdate;

        public CachedQuest(QuestSyncPacket packet) {
            this.questId = packet.questId();
            update(packet);
        }

        public void update(QuestSyncPacket packet) {
            this.title = packet.title();
            this.description = packet.description();
            this.status = packet.status();
            this.villageId = packet.villageId();
            this.villageName = packet.villageName();
            this.denierReward = packet.denierReward();
            this.reputationReward = packet.reputationReward();
            this.objectives = new ArrayList<>();
            for (QuestSyncPacket.ObjectiveData obj : packet.objectives()) {
                this.objectives.add(new CachedObjective(obj));
            }
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    /**
     * 缓存的目标数据
     */
    public static class CachedObjective {
        public final String description;
        public final int currentProgress;
        public final int targetAmount;
        public final boolean isComplete;

        public CachedObjective(QuestSyncPacket.ObjectiveData data) {
            this.description = data.description();
            this.currentProgress = data.currentProgress();
            this.targetAmount = data.targetAmount();
            this.isComplete = data.isComplete();
        }
    }

    /**
     * 缓存的声望数据
     */
    public static class CachedReputation {
        public final UUID villageId;
        public String villageName;
        public String cultureName;
        public int points;
        public String level;
        public long lastUpdate;

        public CachedReputation(ReputationSyncPacket.VillageReputation rep) {
            this.villageId = rep.villageId();
            this.villageName = rep.villageName();
            this.cultureName = rep.cultureName();
            this.points = rep.reputationPoints();
            this.level = rep.reputationLevel();
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    /**
     * 缓存的经济数据
     */
    public static class CachedEconomy {
        public final UUID villageId;
        public long deniers;
        public List<EconomySyncPacket.ResourceEntry> resources;
        public List<EconomySyncPacket.TradeEntry> trades;
        public long lastUpdate;

        public CachedEconomy(EconomySyncPacket packet) {
            this.villageId = packet.villageId();
            update(packet);
        }

        public void update(EconomySyncPacket packet) {
            this.deniers = packet.deniers();
            this.resources = new ArrayList<>(packet.resources());
            this.trades = new ArrayList<>(packet.availableTrades());
            this.lastUpdate = System.currentTimeMillis();
        }
    }

    // ================ 更新方法 ================

    public static void updateVillage(VillageSyncPacket packet) {
        villageCache.compute(packet.villageId(), (id, existing) -> {
            if (existing == null) {
                return new CachedVillage(packet);
            } else {
                existing.update(packet);
                return existing;
            }
        });
    }

    public static void updateVillager(VillagerSyncPacket packet) {
        villagerCache.compute(packet.villagerUuid(), (id, existing) -> {
            if (existing == null) {
                return new CachedVillager(packet);
            } else {
                existing.update(packet);
                return existing;
            }
        });
    }

    public static void updateQuest(QuestSyncPacket packet) {
        questCache.compute(packet.questId(), (id, existing) -> {
            if (existing == null) {
                return new CachedQuest(packet);
            } else {
                existing.update(packet);
                return existing;
            }
        });
    }

    public static void updateReputation(ReputationSyncPacket packet) {
        for (ReputationSyncPacket.VillageReputation rep : packet.reputations()) {
            reputationCache.put(rep.villageId(), new CachedReputation(rep));
        }
    }

    public static void updateEconomy(EconomySyncPacket packet) {
        economyCache.compute(packet.villageId(), (id, existing) -> {
            if (existing == null) {
                return new CachedEconomy(packet);
            } else {
                existing.update(packet);
                return existing;
            }
        });
    }

    public static void updateVillageList(VillageListSyncPacket packet) {
        for (VillageListSyncPacket.VillageEntry entry : packet.villages()) {
            if (!villageCache.containsKey(entry.villageId())) {
                // 创建简化的缓存条目
                CachedVillage cached = new CachedVillage(new VillageSyncPacket(
                    entry.villageId(),
                    entry.name(),
                    entry.position(),
                    "minecraft:overworld",
                    entry.cultureId(),
                    entry.villagerCount(),
                    0,
                    0,
                    entry.level(),
                    true
                ));
                villageCache.put(entry.villageId(), cached);
            }
        }
    }

    // ================ 查询方法 ================

    public static CachedVillage getVillage(UUID id) {
        return villageCache.get(id);
    }

    public static Collection<CachedVillage> getAllVillages() {
        return Collections.unmodifiableCollection(villageCache.values());
    }

    public static CachedVillager getVillager(UUID id) {
        return villagerCache.get(id);
    }

    public static Collection<CachedVillager> getVillagersInVillage(UUID villageId) {
        List<CachedVillager> result = new ArrayList<>();
        for (CachedVillager v : villagerCache.values()) {
            if (villageId.equals(v.villageId)) {
                result.add(v);
            }
        }
        return result;
    }

    public static CachedQuest getQuest(String questId) {
        return questCache.get(questId);
    }

    public static Collection<CachedQuest> getAllQuests() {
        return Collections.unmodifiableCollection(questCache.values());
    }

    public static List<CachedQuest> getActiveQuests() {
        List<CachedQuest> result = new ArrayList<>();
        for (CachedQuest q : questCache.values()) {
            if ("ACTIVE".equals(q.status)) {
                result.add(q);
            }
        }
        return result;
    }

    public static CachedReputation getReputation(UUID villageId) {
        return reputationCache.get(villageId);
    }

    public static Collection<CachedReputation> getAllReputations() {
        return Collections.unmodifiableCollection(reputationCache.values());
    }

    public static CachedEconomy getEconomy(UUID villageId) {
        return economyCache.get(villageId);
    }

    // ================ 清理方法 ================

    public static void clear() {
        villageCache.clear();
        villagerCache.clear();
        questCache.clear();
        reputationCache.clear();
        economyCache.clear();
        MillenaireRewrite.LOGGER.debug("Client village cache cleared");
    }

    public static void clearOldData(long maxAgeMs) {
        long now = System.currentTimeMillis();

        villageCache.entrySet().removeIf(e -> now - e.getValue().lastUpdate > maxAgeMs);
        villagerCache.entrySet().removeIf(e -> now - e.getValue().lastUpdate > maxAgeMs);
        questCache.entrySet().removeIf(e -> now - e.getValue().lastUpdate > maxAgeMs);
        economyCache.entrySet().removeIf(e -> now - e.getValue().lastUpdate > maxAgeMs);
    }
}
