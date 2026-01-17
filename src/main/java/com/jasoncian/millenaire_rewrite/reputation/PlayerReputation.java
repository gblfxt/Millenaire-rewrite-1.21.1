package com.jasoncian.millenaire_rewrite.reputation;

import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.*;

/**
 * 玩家声望数据 - 存储单个玩家与所有村庄和文化的声望关系
 *
 * 包含：
 * - 与每个村庄的声望点数
 * - 与每个文化的整体声望
 * - 声望变化历史
 *
 * @author Based on OldSource player reputation
 * @version 1.0.0
 */
public class PlayerReputation {

    // ================ 数据 ================

    /** 玩家UUID */
    private final UUID playerId;

    /** 玩家名称（用于显示） */
    private String playerName;

    /** 与各村庄的声望点数（村庄ID -> 点数） */
    private final Map<UUID, Integer> villageReputation = new HashMap<>();

    /** 与各文化的整体声望（文化 -> 点数） */
    private final Map<Culture, Integer> cultureReputation = new EnumMap<>(Culture.class);

    /** 声望变化记录（最近的变化） */
    private final List<ReputationChange> recentChanges = new ArrayList<>();

    /** 最大记录的变化数量 */
    private static final int MAX_RECENT_CHANGES = 50;

    // ================ 构造函数 ================

    public PlayerReputation(UUID playerId) {
        this.playerId = playerId;
        this.playerName = "";

        // 初始化所有文化声望为0
        for (Culture culture : Culture.values()) {
            cultureReputation.put(culture, 0);
        }
    }

    public PlayerReputation(UUID playerId, String playerName) {
        this(playerId);
        this.playerName = playerName;
    }

    // ================ 村庄声望 ================

    /**
     * 获取与特定村庄的声望点数
     */
    public int getVillageReputation(UUID villageId) {
        return villageReputation.getOrDefault(villageId, 0);
    }

    /**
     * 获取与特定村庄的声望等级
     */
    public ReputationLevel getVillageReputationLevel(UUID villageId) {
        return ReputationLevel.fromPoints(getVillageReputation(villageId));
    }

    /**
     * 设置与特定村庄的声望点数
     */
    public void setVillageReputation(UUID villageId, int points) {
        villageReputation.put(villageId, clampPoints(points));
    }

    /**
     * 增加与特定村庄的声望
     */
    public ReputationChangeResult addVillageReputation(UUID villageId, int amount, String reason) {
        int oldPoints = getVillageReputation(villageId);
        ReputationLevel oldLevel = ReputationLevel.fromPoints(oldPoints);

        int newPoints = clampPoints(oldPoints + amount);
        villageReputation.put(villageId, newPoints);

        ReputationLevel newLevel = ReputationLevel.fromPoints(newPoints);

        // 记录变化
        ReputationChange change = new ReputationChange(
            villageId, null, amount, reason, System.currentTimeMillis()
        );
        addRecentChange(change);

        return new ReputationChangeResult(oldLevel, newLevel, oldPoints, newPoints);
    }

    // ================ 文化声望 ================

    /**
     * 获取与特定文化的声望点数
     */
    public int getCultureReputation(Culture culture) {
        return cultureReputation.getOrDefault(culture, 0);
    }

    /**
     * 获取与特定文化的声望等级
     */
    public ReputationLevel getCultureReputationLevel(Culture culture) {
        return ReputationLevel.fromPoints(getCultureReputation(culture));
    }

    /**
     * 设置与特定文化的声望点数
     */
    public void setCultureReputation(Culture culture, int points) {
        cultureReputation.put(culture, clampPoints(points));
    }

    /**
     * 增加与特定文化的声望
     */
    public ReputationChangeResult addCultureReputation(Culture culture, int amount, String reason) {
        int oldPoints = getCultureReputation(culture);
        ReputationLevel oldLevel = ReputationLevel.fromPoints(oldPoints);

        int newPoints = clampPoints(oldPoints + amount);
        cultureReputation.put(culture, newPoints);

        ReputationLevel newLevel = ReputationLevel.fromPoints(newPoints);

        // 记录变化
        ReputationChange change = new ReputationChange(
            null, culture, amount, reason, System.currentTimeMillis()
        );
        addRecentChange(change);

        return new ReputationChangeResult(oldLevel, newLevel, oldPoints, newPoints);
    }

    // ================ 综合声望 ================

    /**
     * 增加与村庄及其文化的声望（常用操作）
     * 文化声望获得村庄声望的一半
     */
    public void addReputationWithVillage(UUID villageId, Culture culture, int amount, String reason) {
        addVillageReputation(villageId, amount, reason);
        addCultureReputation(culture, amount / 2, reason);
    }

    /**
     * 获取有效声望（村庄声望 + 文化声望加成）
     */
    public int getEffectiveReputation(UUID villageId, Culture culture) {
        int villageRep = getVillageReputation(villageId);
        int cultureRep = getCultureReputation(culture);

        // 文化声望提供25%加成
        return villageRep + (cultureRep / 4);
    }

    /**
     * 获取有效声望等级
     */
    public ReputationLevel getEffectiveReputationLevel(UUID villageId, Culture culture) {
        return ReputationLevel.fromPoints(getEffectiveReputation(villageId, culture));
    }

    // ================ 变化记录 ================

    /**
     * 添加声望变化记录
     */
    private void addRecentChange(ReputationChange change) {
        recentChanges.add(0, change);
        while (recentChanges.size() > MAX_RECENT_CHANGES) {
            recentChanges.remove(recentChanges.size() - 1);
        }
    }

    /**
     * 获取最近的声望变化
     */
    public List<ReputationChange> getRecentChanges() {
        return Collections.unmodifiableList(recentChanges);
    }

    /**
     * 获取特定村庄的最近声望变化
     */
    public List<ReputationChange> getRecentChangesForVillage(UUID villageId) {
        return recentChanges.stream()
            .filter(c -> villageId.equals(c.villageId()))
            .toList();
    }

    // ================ 工具方法 ================

    /**
     * 限制声望点数范围
     */
    private int clampPoints(int points) {
        return Math.max(-2000, Math.min(10000, points));
    }

    /**
     * 获取总村庄数量
     */
    public int getKnownVillageCount() {
        return villageReputation.size();
    }

    /**
     * 获取所有已知村庄的声望
     */
    public Map<UUID, Integer> getAllVillageReputations() {
        return Collections.unmodifiableMap(villageReputation);
    }

    /**
     * 获取所有文化声望
     */
    public Map<Culture, Integer> getAllCultureReputations() {
        return Collections.unmodifiableMap(cultureReputation);
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("PlayerId", playerId);
        tag.putString("PlayerName", playerName);

        // 保存村庄声望
        ListTag villageList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : villageReputation.entrySet()) {
            CompoundTag villageTag = new CompoundTag();
            villageTag.putUUID("VillageId", entry.getKey());
            villageTag.putInt("Points", entry.getValue());
            villageList.add(villageTag);
        }
        tag.put("VillageReputations", villageList);

        // 保存文化声望
        CompoundTag cultureTag = new CompoundTag();
        for (Map.Entry<Culture, Integer> entry : cultureReputation.entrySet()) {
            cultureTag.putInt(entry.getKey().getId(), entry.getValue());
        }
        tag.put("CultureReputations", cultureTag);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public void load(CompoundTag tag) {
        if (tag.contains("PlayerName")) {
            playerName = tag.getString("PlayerName");
        }

        // 加载村庄声望
        villageReputation.clear();
        ListTag villageList = tag.getList("VillageReputations", 10);
        for (int i = 0; i < villageList.size(); i++) {
            CompoundTag villageTag = villageList.getCompound(i);
            UUID villageId = villageTag.getUUID("VillageId");
            int points = villageTag.getInt("Points");
            villageReputation.put(villageId, points);
        }

        // 加载文化声望
        CompoundTag cultureTag = tag.getCompound("CultureReputations");
        for (Culture culture : Culture.values()) {
            if (cultureTag.contains(culture.getId())) {
                cultureReputation.put(culture, cultureTag.getInt(culture.getId()));
            }
        }
    }

    /**
     * 从NBT加载并创建
     */
    public static PlayerReputation fromNbt(CompoundTag tag) {
        UUID playerId = tag.getUUID("PlayerId");
        PlayerReputation reputation = new PlayerReputation(playerId);
        reputation.load(tag);
        return reputation;
    }

    // ================ Getters ================

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    // ================ 内部记录类 ================

    /**
     * 声望变化记录
     */
    public record ReputationChange(
        UUID villageId,
        Culture culture,
        int amount,
        String reason,
        long timestamp
    ) {
        public boolean isVillageChange() {
            return villageId != null;
        }

        public boolean isCultureChange() {
            return culture != null;
        }
    }

    /**
     * 声望变化结果
     */
    public record ReputationChangeResult(
        ReputationLevel oldLevel,
        ReputationLevel newLevel,
        int oldPoints,
        int newPoints
    ) {
        public boolean levelChanged() {
            return oldLevel != newLevel;
        }

        public boolean levelIncreased() {
            return newLevel.ordinal() > oldLevel.ordinal();
        }

        public boolean levelDecreased() {
            return newLevel.ordinal() < oldLevel.ordinal();
        }

        public int pointsChanged() {
            return newPoints - oldPoints;
        }
    }
}
