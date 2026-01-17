package com.jasoncian.millenaire_rewrite.raid;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 袭击实例 - 表示对村庄的一次完整袭击事件
 *
 * 包含：
 * - 多个波次
 * - 袭击状态跟踪
 * - 参与者管理
 * - 奖励计算
 *
 * @author Based on OldSource raids
 * @version 1.0.0
 */
public class Raid {

    // ================ 袭击状态枚举 ================

    public enum RaidStatus {
        /** 等待开始 */
        PENDING,
        /** 进行中 */
        ONGOING,
        /** 波次间歇 */
        BETWEEN_WAVES,
        /** 胜利 - 防御成功 */
        VICTORY,
        /** 失败 - 村庄被攻陷 */
        DEFEAT,
        /** 停止 - 被管理员取消 */
        STOPPED
    }

    // ================ 常量 ================

    /** 波次间隔时间（tick） */
    private static final int WAVE_INTERVAL = 600; // 30秒

    /** 袭击最长持续时间（tick） */
    private static final int MAX_RAID_DURATION = 48000; // 40分钟

    /** 胜利后庆祝时间（tick） */
    private static final int VICTORY_CELEBRATION = 200; // 10秒

    // ================ 数据 ================

    /** 袭击唯一ID */
    private final UUID raidId;

    /** 目标村庄ID */
    private final UUID targetVillageId;

    /** 袭击类型 */
    private final RaidType raidType;

    /** 攻击方村庄ID（村庄间冲突时） */
    @Nullable
    private UUID attackerVillageId;

    /** 所有波次 */
    private final List<RaidWave> waves = new ArrayList<>();

    /** 当前波次索引 */
    private int currentWaveIndex = 0;

    /** 袭击状态 */
    private RaidStatus status = RaidStatus.PENDING;

    /** 袭击开始时间 */
    private long startTime;

    /** 当前波次开始时间 */
    private long waveStartTime;

    /** 下一波次开始时间 */
    private long nextWaveTime;

    /** 袭击难度（1.0为基准） */
    private float difficulty = 1.0f;

    /** 参与防御的玩家 */
    private final Set<UUID> defenders = new HashSet<>();

    /** 击杀数统计（玩家UUID -> 击杀数） */
    private final Map<UUID, Integer> killCounts = new HashMap<>();

    /** 总击杀数 */
    private int totalKills = 0;

    /** 村庄中心位置 */
    private BlockPos villageCenter;

    // ================ 构造函数 ================

    public Raid(UUID targetVillageId, RaidType raidType) {
        this.raidId = UUID.randomUUID();
        this.targetVillageId = targetVillageId;
        this.raidType = raidType;

        // 创建波次
        for (int i = 1; i <= raidType.getWaveCount(); i++) {
            RaidWave wave = new RaidWave(i, raidType);
            wave.setDifficultyMultiplier(difficulty);
            waves.add(wave);
        }
    }

    public Raid(UUID raidId, UUID targetVillageId, RaidType raidType) {
        this.raidId = raidId;
        this.targetVillageId = targetVillageId;
        this.raidType = raidType;
    }

    // ================ 袭击控制 ================

    /**
     * 开始袭击
     */
    public void start(ServerLevel level, Village village) {
        this.status = RaidStatus.ONGOING;
        this.startTime = level.getGameTime();
        this.villageCenter = village.getCenterPos();

        // 通知附近玩家
        announceRaid(level);

        // 开始第一波
        startNextWave(level);

        MillenaireRewrite.LOGGER.info("Raid {} started on village {}", raidId, village.getName());
    }

    /**
     * 开始下一波
     */
    private void startNextWave(ServerLevel level) {
        if (currentWaveIndex >= waves.size()) {
            onVictory(level);
            return;
        }

        RaidWave wave = waves.get(currentWaveIndex);
        wave.start(level, villageCenter, level.getGameTime());
        waveStartTime = level.getGameTime();

        // 通知玩家波次开始
        announceWave(level, wave);

        MillenaireRewrite.LOGGER.info("Raid {}: Wave {} started", raidId, wave.getWaveNumber());
    }

    /**
     * 通知袭击开始
     */
    private void announceRaid(ServerLevel level) {
        Component message = Component.literal("[Millenaire] " + raidType.getAnnouncement());

        // 通知村庄附近的玩家
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(villageCenter, 100)) {
                player.sendSystemMessage(message);
                defenders.add(player.getUUID());
            }
        }
    }

    /**
     * 通知波次开始
     */
    private void announceWave(ServerLevel level, RaidWave wave) {
        Component message = Component.literal(
            String.format("[Millenaire] Wave %d/%d - %d enemies approaching!",
                wave.getWaveNumber(), waves.size(), wave.getTotalEnemyCount())
        );

        for (UUID defenderId : defenders) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(defenderId);
            if (player != null) {
                player.sendSystemMessage(message);
            }
        }
    }

    // ================ 袭击更新 ================

    /**
     * 每tick更新
     */
    public void tick(ServerLevel level) {
        if (status != RaidStatus.ONGOING && status != RaidStatus.BETWEEN_WAVES) {
            return;
        }

        long currentTime = level.getGameTime();

        // 检查超时
        if (currentTime - startTime > MAX_RAID_DURATION) {
            onTimeout(level);
            return;
        }

        // 更新参与玩家
        updateDefenders(level);

        if (status == RaidStatus.ONGOING) {
            // 更新当前波次
            RaidWave currentWave = getCurrentWave();
            if (currentWave != null) {
                currentWave.tick(level);

                if (currentWave.isComplete()) {
                    onWaveComplete(level, currentWave);
                }
            }
        } else if (status == RaidStatus.BETWEEN_WAVES) {
            // 检查是否开始下一波
            if (currentTime >= nextWaveTime) {
                status = RaidStatus.ONGOING;
                startNextWave(level);
            }
        }
    }

    /**
     * 更新防御者列表
     */
    private void updateDefenders(ServerLevel level) {
        // 添加新进入范围的玩家
        for (ServerPlayer player : level.players()) {
            if (player.blockPosition().closerThan(villageCenter, 100)) {
                defenders.add(player.getUUID());
            }
        }
    }

    /**
     * 波次完成处理
     */
    private void onWaveComplete(ServerLevel level, RaidWave wave) {
        MillenaireRewrite.LOGGER.info("Raid {}: Wave {} completed", raidId, wave.getWaveNumber());

        currentWaveIndex++;

        if (currentWaveIndex >= waves.size()) {
            onVictory(level);
        } else {
            // 进入波次间歇
            status = RaidStatus.BETWEEN_WAVES;
            nextWaveTime = level.getGameTime() + WAVE_INTERVAL;

            // 通知玩家
            Component message = Component.literal(
                String.format("[Millenaire] Wave complete! Next wave in %d seconds...", WAVE_INTERVAL / 20)
            );
            notifyDefenders(level, message);
        }
    }

    /**
     * 袭击胜利
     */
    private void onVictory(ServerLevel level) {
        status = RaidStatus.VICTORY;

        Component message = Component.literal("[Millenaire] Victory! The village is saved!");
        notifyDefenders(level, message);

        MillenaireRewrite.LOGGER.info("Raid {} on village {} ended in VICTORY", raidId, targetVillageId);
    }

    /**
     * 袭击失败
     */
    public void onDefeat(ServerLevel level) {
        status = RaidStatus.DEFEAT;

        Component message = Component.literal("[Millenaire] Defeat! The village has fallen...");
        notifyDefenders(level, message);

        MillenaireRewrite.LOGGER.info("Raid {} on village {} ended in DEFEAT", raidId, targetVillageId);
    }

    /**
     * 袭击超时
     */
    private void onTimeout(ServerLevel level) {
        // 超时视为双方平局，袭击停止
        status = RaidStatus.STOPPED;

        Component message = Component.literal("[Millenaire] The raiders have retreated...");
        notifyDefenders(level, message);

        // 清理剩余敌人
        cleanupEnemies(level);

        MillenaireRewrite.LOGGER.info("Raid {} timed out", raidId);
    }

    /**
     * 强制停止袭击
     */
    public void stop(ServerLevel level) {
        status = RaidStatus.STOPPED;
        cleanupEnemies(level);

        MillenaireRewrite.LOGGER.info("Raid {} was stopped", raidId);
    }

    /**
     * 清理所有袭击敌人
     */
    private void cleanupEnemies(ServerLevel level) {
        for (RaidWave wave : waves) {
            for (UUID enemyId : wave.getAliveEnemies()) {
                var entity = level.getEntity(enemyId);
                if (entity != null) {
                    entity.discard();
                }
            }
        }
    }

    /**
     * 通知所有防御者
     */
    private void notifyDefenders(ServerLevel level, Component message) {
        for (UUID defenderId : defenders) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(defenderId);
            if (player != null) {
                player.sendSystemMessage(message);
            }
        }
    }

    // ================ 击杀追踪 ================

    /**
     * 记录击杀
     */
    public void recordKill(UUID playerId) {
        killCounts.merge(playerId, 1, Integer::sum);
        totalKills++;
    }

    /**
     * 获取玩家击杀数
     */
    public int getPlayerKills(UUID playerId) {
        return killCounts.getOrDefault(playerId, 0);
    }

    // ================ 奖励计算 ================

    /**
     * 计算声望奖励
     */
    public int calculateReputationReward() {
        if (status != RaidStatus.VICTORY) {
            return 0;
        }

        int baseReward = raidType.getReputationReward();
        float waveBonus = 1.0f + (waves.size() - 1) * 0.1f;
        float difficultyBonus = difficulty;

        return (int) (baseReward * waveBonus * difficultyBonus);
    }

    /**
     * 计算玩家个人奖励
     */
    public int calculatePlayerReward(UUID playerId) {
        if (status != RaidStatus.VICTORY) {
            return 0;
        }

        int kills = getPlayerKills(playerId);
        int baseReward = calculateReputationReward();

        // 根据击杀贡献分配额外奖励
        if (totalKills > 0) {
            float contribution = (float) kills / totalKills;
            return (int) (baseReward * (0.5f + contribution * 0.5f));
        }

        return baseReward / 2;
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("RaidId", raidId);
        tag.putUUID("TargetVillageId", targetVillageId);
        tag.putString("RaidType", raidType.getId());
        tag.putString("Status", status.name());

        if (attackerVillageId != null) {
            tag.putUUID("AttackerVillageId", attackerVillageId);
        }

        tag.putInt("CurrentWaveIndex", currentWaveIndex);
        tag.putLong("StartTime", startTime);
        tag.putLong("WaveStartTime", waveStartTime);
        tag.putLong("NextWaveTime", nextWaveTime);
        tag.putFloat("Difficulty", difficulty);
        tag.putInt("TotalKills", totalKills);

        if (villageCenter != null) {
            tag.putInt("CenterX", villageCenter.getX());
            tag.putInt("CenterY", villageCenter.getY());
            tag.putInt("CenterZ", villageCenter.getZ());
        }

        // 保存波次
        ListTag waveList = new ListTag();
        for (RaidWave wave : waves) {
            waveList.add(wave.save());
        }
        tag.put("Waves", waveList);

        // 保存防御者
        ListTag defenderList = new ListTag();
        for (UUID defenderId : defenders) {
            CompoundTag defenderTag = new CompoundTag();
            defenderTag.putUUID("UUID", defenderId);
            defenderList.add(defenderTag);
        }
        tag.put("Defenders", defenderList);

        // 保存击杀统计
        ListTag killList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : killCounts.entrySet()) {
            CompoundTag killTag = new CompoundTag();
            killTag.putUUID("Player", entry.getKey());
            killTag.putInt("Kills", entry.getValue());
            killList.add(killTag);
        }
        tag.put("KillCounts", killList);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static Raid fromNbt(CompoundTag tag) {
        UUID raidId = tag.getUUID("RaidId");
        UUID targetVillageId = tag.getUUID("TargetVillageId");
        RaidType raidType = RaidType.fromId(tag.getString("RaidType"));

        Raid raid = new Raid(raidId, targetVillageId, raidType);
        raid.status = RaidStatus.valueOf(tag.getString("Status"));

        if (tag.contains("AttackerVillageId")) {
            raid.attackerVillageId = tag.getUUID("AttackerVillageId");
        }

        raid.currentWaveIndex = tag.getInt("CurrentWaveIndex");
        raid.startTime = tag.getLong("StartTime");
        raid.waveStartTime = tag.getLong("WaveStartTime");
        raid.nextWaveTime = tag.getLong("NextWaveTime");
        raid.difficulty = tag.getFloat("Difficulty");
        raid.totalKills = tag.getInt("TotalKills");

        if (tag.contains("CenterX")) {
            raid.villageCenter = new BlockPos(
                tag.getInt("CenterX"),
                tag.getInt("CenterY"),
                tag.getInt("CenterZ")
            );
        }

        // 加载波次
        ListTag waveList = tag.getList("Waves", 10);
        for (int i = 0; i < waveList.size(); i++) {
            raid.waves.add(RaidWave.fromNbt(waveList.getCompound(i)));
        }

        // 加载防御者
        ListTag defenderList = tag.getList("Defenders", 10);
        for (int i = 0; i < defenderList.size(); i++) {
            raid.defenders.add(defenderList.getCompound(i).getUUID("UUID"));
        }

        // 加载击杀统计
        ListTag killList = tag.getList("KillCounts", 10);
        for (int i = 0; i < killList.size(); i++) {
            CompoundTag killTag = killList.getCompound(i);
            raid.killCounts.put(killTag.getUUID("Player"), killTag.getInt("Kills"));
        }

        return raid;
    }

    // ================ Getters/Setters ================

    public UUID getRaidId() {
        return raidId;
    }

    public UUID getTargetVillageId() {
        return targetVillageId;
    }

    public RaidType getRaidType() {
        return raidType;
    }

    @Nullable
    public UUID getAttackerVillageId() {
        return attackerVillageId;
    }

    public void setAttackerVillageId(UUID attackerVillageId) {
        this.attackerVillageId = attackerVillageId;
    }

    public RaidStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return status == RaidStatus.ONGOING || status == RaidStatus.BETWEEN_WAVES;
    }

    public boolean isFinished() {
        return status == RaidStatus.VICTORY || status == RaidStatus.DEFEAT || status == RaidStatus.STOPPED;
    }

    @Nullable
    public RaidWave getCurrentWave() {
        if (currentWaveIndex < waves.size()) {
            return waves.get(currentWaveIndex);
        }
        return null;
    }

    public List<RaidWave> getWaves() {
        return Collections.unmodifiableList(waves);
    }

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
        for (RaidWave wave : waves) {
            wave.setDifficultyMultiplier(difficulty);
        }
    }

    public Set<UUID> getDefenders() {
        return Collections.unmodifiableSet(defenders);
    }

    public int getTotalKills() {
        return totalKills;
    }

    public BlockPos getVillageCenter() {
        return villageCenter;
    }

    /**
     * 获取袭击进度（0.0 - 1.0）
     */
    public double getOverallProgress() {
        if (waves.isEmpty()) return 0;

        double completedWaves = currentWaveIndex;
        RaidWave currentWave = getCurrentWave();
        if (currentWave != null) {
            completedWaves += currentWave.getProgress();
        }

        return completedWaves / waves.size();
    }

    @Override
    public String toString() {
        return "Raid{" +
            "id=" + raidId +
            ", type=" + raidType +
            ", status=" + status +
            ", wave=" + (currentWaveIndex + 1) + "/" + waves.size() +
            ", progress=" + String.format("%.0f%%", getOverallProgress() * 100) +
            '}';
    }
}
