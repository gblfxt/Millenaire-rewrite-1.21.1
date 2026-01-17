package com.jasoncian.millenaire_rewrite.raid;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.diplomacy.DiplomacyManager;
import com.jasoncian.millenaire_rewrite.diplomacy.VillageRelation;
import com.jasoncian.millenaire_rewrite.reputation.ReputationManager;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 袭击管理器 - 管理所有活跃袭击
 *
 * 功能：
 * - 存储和加载袭击数据
 * - 管理袭击生命周期
 * - 触发随机袭击
 * - 发放袭击奖励
 *
 * @author Based on OldSource raid management
 * @version 1.0.0
 */
public class RaidManager extends SavedData {

    // ================ 常量 ================

    private static final String DATA_NAME = MillenaireRewrite.MOD_ID + "_raids";

    /** 随机袭击检查间隔（tick） */
    private static final int RAID_CHECK_INTERVAL = 24000; // 1游戏天

    /** 每个村庄最小袭击间隔（tick） */
    private static final int MIN_RAID_INTERVAL = 72000; // 1小时

    /** 基础袭击概率（每次检查） */
    private static final float BASE_RAID_CHANCE = 0.15f;

    // ================ 数据 ================

    /** 活跃袭击（袭击ID -> 袭击） */
    private final Map<UUID, Raid> activeRaids = new HashMap<>();

    /** 已完成袭击历史（村庄ID -> 最后袭击时间） */
    private final Map<UUID, Long> lastRaidTime = new HashMap<>();

    /** 上次袭击检查时间 */
    private long lastCheckTime = 0;

    /** 随机数生成器 */
    private final Random random = new Random();

    // ================ 构造函数 ================

    public RaidManager() {
        // 默认构造
    }

    // ================ SavedData实现 ================

    /**
     * 从NBT加载
     */
    public static RaidManager load(CompoundTag tag, HolderLookup.Provider registries) {
        RaidManager manager = new RaidManager();

        // 加载活跃袭击
        ListTag raidList = tag.getList("ActiveRaids", 10);
        for (int i = 0; i < raidList.size(); i++) {
            Raid raid = Raid.fromNbt(raidList.getCompound(i));
            manager.activeRaids.put(raid.getRaidId(), raid);
        }

        // 加载袭击历史
        ListTag historyList = tag.getList("RaidHistory", 10);
        for (int i = 0; i < historyList.size(); i++) {
            CompoundTag historyTag = historyList.getCompound(i);
            UUID villageId = historyTag.getUUID("VillageId");
            long time = historyTag.getLong("Time");
            manager.lastRaidTime.put(villageId, time);
        }

        manager.lastCheckTime = tag.getLong("LastCheckTime");

        MillenaireRewrite.LOGGER.info("Loaded raid data: {} active raids", manager.activeRaids.size());

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        // 保存活跃袭击
        ListTag raidList = new ListTag();
        for (Raid raid : activeRaids.values()) {
            raidList.add(raid.save());
        }
        tag.put("ActiveRaids", raidList);

        // 保存袭击历史
        ListTag historyList = new ListTag();
        for (Map.Entry<UUID, Long> entry : lastRaidTime.entrySet()) {
            CompoundTag historyTag = new CompoundTag();
            historyTag.putUUID("VillageId", entry.getKey());
            historyTag.putLong("Time", entry.getValue());
            historyList.add(historyTag);
        }
        tag.put("RaidHistory", historyList);

        tag.putLong("LastCheckTime", lastCheckTime);

        return tag;
    }

    // ================ 静态访问 ================

    /**
     * 获取维度的袭击管理器
     */
    public static RaidManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(RaidManager::new, RaidManager::load),
            DATA_NAME
        );
    }

    // ================ 袭击管理 ================

    /**
     * 开始一个新袭击
     */
    public Raid startRaid(ServerLevel level, Village village, RaidType raidType) {
        // 检查是否已有活跃袭击
        if (hasActiveRaid(village.getVillageId())) {
            return null;
        }

        // 创建袭击
        Raid raid = new Raid(village.getVillageId(), raidType);

        // 根据村庄大小调整难度
        float difficulty = calculateDifficulty(village);
        raid.setDifficulty(difficulty);

        // 开始袭击
        raid.start(level, village);

        // 记录
        activeRaids.put(raid.getRaidId(), raid);
        lastRaidTime.put(village.getVillageId(), level.getGameTime());
        setDirty();

        return raid;
    }

    /**
     * 开始村庄间冲突袭击
     */
    public Raid startVillageRaid(ServerLevel level, Village attacker, Village defender) {
        if (hasActiveRaid(defender.getVillageId())) {
            return null;
        }

        Raid raid = new Raid(defender.getVillageId(), RaidType.VILLAGE_RAID);
        raid.setAttackerVillageId(attacker.getVillageId());

        // 根据两个村庄的实力计算难度
        float difficulty = calculateVillageConflictDifficulty(attacker, defender);
        raid.setDifficulty(difficulty);

        raid.start(level, defender);

        activeRaids.put(raid.getRaidId(), raid);
        lastRaidTime.put(defender.getVillageId(), level.getGameTime());
        setDirty();

        MillenaireRewrite.LOGGER.info("Village raid started: {} attacks {}",
            attacker.getName(), defender.getName());

        return raid;
    }

    /**
     * 计算袭击难度
     */
    private float calculateDifficulty(Village village) {
        // 基础难度
        float difficulty = 1.0f;

        // 根据村庄大小增加难度
        int buildingCount = village.getBuildingCount();
        difficulty += buildingCount * 0.05f;

        // 根据村庄等级增加难度
        // difficulty += village.getLevel() * 0.1f;

        return Math.min(3.0f, difficulty); // 最大3倍难度
    }

    /**
     * 计算村庄冲突难度
     */
    private float calculateVillageConflictDifficulty(Village attacker, Village defender) {
        // 攻击方村庄越大，袭击越强
        float attackerStrength = 1.0f + attacker.getBuildingCount() * 0.1f;

        // 防御方村庄越大，相对难度降低
        float defenderStrength = 1.0f + defender.getBuildingCount() * 0.05f;

        return Math.max(0.5f, Math.min(3.0f, attackerStrength / defenderStrength));
    }

    /**
     * 检查村庄是否有活跃袭击
     */
    public boolean hasActiveRaid(UUID villageId) {
        return activeRaids.values().stream()
            .anyMatch(raid -> raid.getTargetVillageId().equals(villageId) && raid.isActive());
    }

    /**
     * 获取村庄的活跃袭击
     */
    @Nullable
    public Raid getActiveRaid(UUID villageId) {
        return activeRaids.values().stream()
            .filter(raid -> raid.getTargetVillageId().equals(villageId) && raid.isActive())
            .findFirst()
            .orElse(null);
    }

    /**
     * 停止指定袭击
     */
    public void stopRaid(ServerLevel level, UUID raidId) {
        Raid raid = activeRaids.get(raidId);
        if (raid != null) {
            raid.stop(level);
            setDirty();
        }
    }

    /**
     * 获取所有活跃袭击
     */
    public Collection<Raid> getActiveRaids() {
        return activeRaids.values().stream()
            .filter(Raid::isActive)
            .toList();
    }

    // ================ Tick更新 ================

    /**
     * 每tick更新
     */
    public void tick(ServerLevel level) {
        long currentTime = level.getGameTime();

        // 更新所有活跃袭击
        List<Raid> finishedRaids = new ArrayList<>();
        for (Raid raid : activeRaids.values()) {
            if (raid.isActive()) {
                raid.tick(level);
            }

            if (raid.isFinished()) {
                finishedRaids.add(raid);
            }
        }

        // 处理完成的袭击
        for (Raid raid : finishedRaids) {
            onRaidFinished(level, raid);
        }

        // 定期检查是否触发随机袭击
        if (currentTime - lastCheckTime >= RAID_CHECK_INTERVAL) {
            lastCheckTime = currentTime;
            checkRandomRaids(level);
        }

        if (!finishedRaids.isEmpty()) {
            setDirty();
        }
    }

    /**
     * 检查随机袭击
     */
    private void checkRandomRaids(ServerLevel level) {
        VillageManager villageManager = VillageManager.get(level);
        long currentTime = level.getGameTime();

        boolean isNight = level.isNight();

        for (Village village : villageManager.getAllVillages()) {
            UUID villageId = village.getVillageId();

            // 检查冷却
            Long lastTime = lastRaidTime.get(villageId);
            if (lastTime != null && currentTime - lastTime < MIN_RAID_INTERVAL) {
                continue;
            }

            // 检查是否已有袭击
            if (hasActiveRaid(villageId)) {
                continue;
            }

            // 计算袭击概率
            float chance = calculateRaidChance(village);
            if (random.nextFloat() < chance) {
                // 选择袭击类型
                RaidType raidType = selectRaidType(village, isNight);
                if (raidType != null) {
                    startRaid(level, village, raidType);
                }
            }
        }

        // 检查村庄间战争冲突
        checkVillageConflicts(level);
    }

    /**
     * 计算袭击概率
     */
    private float calculateRaidChance(Village village) {
        float chance = BASE_RAID_CHANCE;

        // 村庄越大越容易被袭击
        chance += village.getBuildingCount() * 0.01f;

        return Math.min(0.5f, chance); // 最大50%
    }

    /**
     * 选择袭击类型
     */
    @Nullable
    private RaidType selectRaidType(Village village, boolean isNight) {
        List<RaidType> candidates = new ArrayList<>();

        for (RaidType type : RaidType.getMonsterRaids()) {
            if (type.isNightOnly() && !isNight) {
                continue;
            }

            // 根据村庄等级过滤太难的袭击
            int villageDifficultyCap = 50 + village.getBuildingCount() * 20;
            if (type.getBaseDifficulty() <= villageDifficultyCap) {
                candidates.add(type);
            }
        }

        if (candidates.isEmpty()) {
            return RaidType.ZOMBIE_SIEGE; // 默认
        }

        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * 检查村庄间冲突
     */
    private void checkVillageConflicts(ServerLevel level) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        VillageManager villageManager = VillageManager.get(level);

        List<Village> villages = new ArrayList<>(villageManager.getAllVillages());

        for (int i = 0; i < villages.size(); i++) {
            Village villageA = villages.get(i);

            for (int j = i + 1; j < villages.size(); j++) {
                Village villageB = villages.get(j);

                // 检查是否处于战争状态
                if (diplomacyManager.areAtWar(villageA.getVillageId(), villageB.getVillageId())) {
                    // 有一定概率发起袭击
                    if (random.nextFloat() < 0.1f) {
                        // 随机选择攻击方
                        Village attacker = random.nextBoolean() ? villageA : villageB;
                        Village defender = attacker == villageA ? villageB : villageA;

                        if (!hasActiveRaid(defender.getVillageId())) {
                            startVillageRaid(level, attacker, defender);
                        }
                    }
                }
            }
        }
    }

    // ================ 袭击完成处理 ================

    /**
     * 袭击完成处理
     */
    private void onRaidFinished(ServerLevel level, Raid raid) {
        VillageManager villageManager = VillageManager.get(level);
        ReputationManager reputationManager = ReputationManager.get(level);
        Village village = villageManager.getVillage(raid.getTargetVillageId());

        if (village == null) {
            activeRaids.remove(raid.getRaidId());
            return;
        }

        if (raid.getStatus() == Raid.RaidStatus.VICTORY) {
            // 胜利奖励
            int reputationReward = raid.calculateReputationReward();

            for (UUID defenderId : raid.getDefenders()) {
                int playerReward = raid.calculatePlayerReward(defenderId);

                ServerPlayer player = level.getServer().getPlayerList().getPlayer(defenderId);
                if (player != null) {
                    // 给予声望奖励
                    reputationManager.onDefendVillage(player, village, raid.getPlayerKills(defenderId));
                }
            }

            MillenaireRewrite.LOGGER.info("Raid victory! Rewards distributed to {} defenders",
                raid.getDefenders().size());

        } else if (raid.getStatus() == Raid.RaidStatus.DEFEAT) {
            // 失败惩罚
            // 村庄可能损失资源或建筑
            onRaidDefeat(level, raid, village);
        }

        // 如果是村庄冲突，更新外交关系
        if (raid.getAttackerVillageId() != null) {
            DiplomacyManager diplomacyManager = DiplomacyManager.get(level);

            if (raid.getStatus() == Raid.RaidStatus.VICTORY) {
                // 防御成功，攻击方关系略微恢复
                diplomacyManager.modifyRelation(
                    raid.getAttackerVillageId(),
                    raid.getTargetVillageId(),
                    10,
                    "Failed raid",
                    level.getGameTime()
                );
            } else if (raid.getStatus() == Raid.RaidStatus.DEFEAT) {
                // 攻击成功
                diplomacyManager.onRaid(raid.getAttackerVillageId(), raid.getTargetVillageId(), level.getGameTime());
            }
        }

        // 清理完成的袭击
        activeRaids.remove(raid.getRaidId());
    }

    /**
     * 处理袭击失败后果
     */
    private void onRaidDefeat(ServerLevel level, Raid raid, Village village) {
        // TODO: 实现村庄损失逻辑
        // - 损失第纳尔
        // - 可能损失建筑
        // - 村民可能死亡

        int denierLoss = 100 * (int) raid.getDifficulty();
        village.addDeniers(-denierLoss);

        MillenaireRewrite.LOGGER.info("Village {} lost {} deniers from raid defeat",
            village.getName(), denierLoss);
    }

    // ================ 统计 ================

    /**
     * 获取村庄的袭击统计
     */
    public int getRaidCount(UUID villageId) {
        // 简化：只统计活跃袭击数
        return (int) activeRaids.values().stream()
            .filter(r -> r.getTargetVillageId().equals(villageId))
            .count();
    }
}
