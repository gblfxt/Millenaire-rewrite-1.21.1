package com.jasoncian.millenaire_rewrite.diplomacy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 村庄外交数据 - 存储两个村庄之间的关系数据
 *
 * 包含：
 * - 关系点数和等级
 * - 关系历史记录
 * - 特殊状态（贸易协定、战争声明等）
 *
 * @author Based on OldSource diplomacy
 * @version 1.0.0
 */
public class VillageDiplomacy {

    // ================ 数据 ================

    /** 村庄A的UUID */
    private final UUID villageA;

    /** 村庄B的UUID */
    private final UUID villageB;

    /** 关系点数 */
    private int relationPoints;

    /** 当前关系等级 */
    private VillageRelation currentRelation;

    /** 是否有贸易协定 */
    private boolean hasTradeAgreement;

    /** 贸易协定到期时间（游戏tick） */
    private long tradeAgreementExpiry;

    /** 是否有互不侵犯条约 */
    private boolean hasNonAggressionPact;

    /** 互不侵犯条约到期时间 */
    private long nonAggressionPactExpiry;

    /** 战争开始时间（0表示无战争） */
    private long warStartTime;

    /** 关系变化历史 */
    private final List<RelationChange> history = new ArrayList<>();

    /** 最大历史记录数 */
    private static final int MAX_HISTORY = 20;

    // ================ 构造函数 ================

    public VillageDiplomacy(UUID villageA, UUID villageB) {
        // 确保UUID顺序一致（用于查找）
        if (villageA.compareTo(villageB) > 0) {
            this.villageA = villageB;
            this.villageB = villageA;
        } else {
            this.villageA = villageA;
            this.villageB = villageB;
        }

        this.relationPoints = 0;
        this.currentRelation = VillageRelation.NEUTRAL;
        this.hasTradeAgreement = false;
        this.hasNonAggressionPact = false;
        this.warStartTime = 0;
    }

    // ================ 关系管理 ================

    /**
     * 修改关系点数
     *
     * @param amount 变化量（正数增加，负数减少）
     * @param reason 原因
     * @param timestamp 时间戳
     * @return 关系变化结果
     */
    public RelationChangeResult modifyRelation(int amount, String reason, long timestamp) {
        VillageRelation oldRelation = currentRelation;
        int oldPoints = relationPoints;

        // 修改点数
        relationPoints = clampPoints(relationPoints + amount);

        // 更新关系等级
        currentRelation = VillageRelation.fromPoints(relationPoints);

        // 记录历史
        addHistory(new RelationChange(amount, reason, timestamp, oldRelation, currentRelation));

        return new RelationChangeResult(oldRelation, currentRelation, oldPoints, relationPoints);
    }

    /**
     * 设置关系点数
     */
    public void setRelationPoints(int points) {
        this.relationPoints = clampPoints(points);
        this.currentRelation = VillageRelation.fromPoints(this.relationPoints);
    }

    /**
     * 限制点数范围
     */
    private int clampPoints(int points) {
        return Math.max(-1000, Math.min(1000, points));
    }

    // ================ 贸易协定 ================

    /**
     * 签订贸易协定
     *
     * @param duration 持续时间（tick）
     * @param currentTime 当前时间
     */
    public void signTradeAgreement(long duration, long currentTime) {
        this.hasTradeAgreement = true;
        this.tradeAgreementExpiry = currentTime + duration;

        // 贸易协定增加关系
        modifyRelation(50, "Signed trade agreement", currentTime);
    }

    /**
     * 检查贸易协定是否有效
     */
    public boolean isTradeAgreementActive(long currentTime) {
        if (!hasTradeAgreement) return false;
        if (currentTime > tradeAgreementExpiry) {
            hasTradeAgreement = false;
            return false;
        }
        return true;
    }

    /**
     * 终止贸易协定
     */
    public void terminateTradeAgreement(long currentTime) {
        if (hasTradeAgreement) {
            hasTradeAgreement = false;
            modifyRelation(-30, "Trade agreement terminated", currentTime);
        }
    }

    // ================ 互不侵犯条约 ================

    /**
     * 签订互不侵犯条约
     */
    public void signNonAggressionPact(long duration, long currentTime) {
        this.hasNonAggressionPact = true;
        this.nonAggressionPactExpiry = currentTime + duration;

        // 结束战争状态
        if (warStartTime > 0) {
            endWar(currentTime);
        }

        modifyRelation(100, "Signed non-aggression pact", currentTime);
    }

    /**
     * 检查互不侵犯条约是否有效
     */
    public boolean isNonAggressionPactActive(long currentTime) {
        if (!hasNonAggressionPact) return false;
        if (currentTime > nonAggressionPactExpiry) {
            hasNonAggressionPact = false;
            return false;
        }
        return true;
    }

    // ================ 战争 ================

    /**
     * 宣战
     */
    public void declareWar(long currentTime) {
        if (warStartTime > 0) return; // 已经处于战争状态

        this.warStartTime = currentTime;
        this.hasTradeAgreement = false;
        this.hasNonAggressionPact = false;

        // 大幅降低关系
        modifyRelation(-300, "War declared", currentTime);
    }

    /**
     * 结束战争
     */
    public void endWar(long currentTime) {
        if (warStartTime == 0) return;

        this.warStartTime = 0;

        // 战争结束，关系略微恢复
        modifyRelation(50, "War ended", currentTime);
    }

    /**
     * 检查是否处于战争状态
     */
    public boolean isAtWar() {
        return warStartTime > 0;
    }

    /**
     * 获取战争持续时间（tick）
     */
    public long getWarDuration(long currentTime) {
        if (warStartTime == 0) return 0;
        return currentTime - warStartTime;
    }

    // ================ 同盟 ================

    /**
     * 建立同盟
     */
    public void formAlliance(long currentTime) {
        // 需要足够友好才能建立同盟
        if (relationPoints < VillageRelation.FRIENDLY.getMinPoints()) {
            return;
        }

        modifyRelation(300, "Alliance formed", currentTime);
    }

    /**
     * 解除同盟
     */
    public void breakAlliance(long currentTime) {
        if (currentRelation != VillageRelation.ALLIED) return;

        modifyRelation(-200, "Alliance broken", currentTime);
    }

    // ================ 历史记录 ================

    /**
     * 添加历史记录
     */
    private void addHistory(RelationChange change) {
        history.add(0, change);
        while (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
    }

    /**
     * 获取历史记录
     */
    public List<RelationChange> getHistory() {
        return Collections.unmodifiableList(history);
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("VillageA", villageA);
        tag.putUUID("VillageB", villageB);
        tag.putInt("RelationPoints", relationPoints);
        tag.putString("CurrentRelation", currentRelation.getId());

        tag.putBoolean("HasTradeAgreement", hasTradeAgreement);
        tag.putLong("TradeAgreementExpiry", tradeAgreementExpiry);
        tag.putBoolean("HasNonAggressionPact", hasNonAggressionPact);
        tag.putLong("NonAggressionPactExpiry", nonAggressionPactExpiry);
        tag.putLong("WarStartTime", warStartTime);

        // 保存历史
        ListTag historyList = new ListTag();
        for (RelationChange change : history) {
            historyList.add(change.save());
        }
        tag.put("History", historyList);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static VillageDiplomacy fromNbt(CompoundTag tag) {
        UUID villageA = tag.getUUID("VillageA");
        UUID villageB = tag.getUUID("VillageB");

        VillageDiplomacy diplomacy = new VillageDiplomacy(villageA, villageB);
        diplomacy.relationPoints = tag.getInt("RelationPoints");
        diplomacy.currentRelation = VillageRelation.fromId(tag.getString("CurrentRelation"));

        diplomacy.hasTradeAgreement = tag.getBoolean("HasTradeAgreement");
        diplomacy.tradeAgreementExpiry = tag.getLong("TradeAgreementExpiry");
        diplomacy.hasNonAggressionPact = tag.getBoolean("HasNonAggressionPact");
        diplomacy.nonAggressionPactExpiry = tag.getLong("NonAggressionPactExpiry");
        diplomacy.warStartTime = tag.getLong("WarStartTime");

        // 加载历史
        ListTag historyList = tag.getList("History", 10);
        for (int i = 0; i < historyList.size(); i++) {
            diplomacy.history.add(RelationChange.fromNbt(historyList.getCompound(i)));
        }

        return diplomacy;
    }

    // ================ Getters ================

    public UUID getVillageA() {
        return villageA;
    }

    public UUID getVillageB() {
        return villageB;
    }

    /**
     * 获取另一个村庄的UUID
     */
    public UUID getOtherVillage(UUID village) {
        if (village.equals(villageA)) return villageB;
        if (village.equals(villageB)) return villageA;
        return null;
    }

    /**
     * 检查此外交关系是否涉及指定村庄
     */
    public boolean involves(UUID villageId) {
        return villageA.equals(villageId) || villageB.equals(villageId);
    }

    public int getRelationPoints() {
        return relationPoints;
    }

    public VillageRelation getCurrentRelation() {
        return currentRelation;
    }

    public boolean hasTradeAgreement() {
        return hasTradeAgreement;
    }

    public boolean hasNonAggressionPact() {
        return hasNonAggressionPact;
    }

    public long getWarStartTime() {
        return warStartTime;
    }

    // ================ 内部记录类 ================

    /**
     * 关系变化记录
     */
    public record RelationChange(
        int amount,
        String reason,
        long timestamp,
        VillageRelation oldRelation,
        VillageRelation newRelation
    ) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Amount", amount);
            tag.putString("Reason", reason);
            tag.putLong("Timestamp", timestamp);
            tag.putString("OldRelation", oldRelation.getId());
            tag.putString("NewRelation", newRelation.getId());
            return tag;
        }

        public static RelationChange fromNbt(CompoundTag tag) {
            return new RelationChange(
                tag.getInt("Amount"),
                tag.getString("Reason"),
                tag.getLong("Timestamp"),
                VillageRelation.fromId(tag.getString("OldRelation")),
                VillageRelation.fromId(tag.getString("NewRelation"))
            );
        }

        public boolean relationChanged() {
            return oldRelation != newRelation;
        }
    }

    /**
     * 关系变化结果
     */
    public record RelationChangeResult(
        VillageRelation oldRelation,
        VillageRelation newRelation,
        int oldPoints,
        int newPoints
    ) {
        public boolean relationChanged() {
            return oldRelation != newRelation;
        }

        public boolean improved() {
            return newRelation.ordinal() < oldRelation.ordinal();
        }

        public boolean worsened() {
            return newRelation.ordinal() > oldRelation.ordinal();
        }

        public int pointsChanged() {
            return newPoints - oldPoints;
        }
    }

    @Override
    public String toString() {
        return "VillageDiplomacy{" +
            "villageA=" + villageA +
            ", villageB=" + villageB +
            ", relation=" + currentRelation +
            ", points=" + relationPoints +
            (isAtWar() ? ", AT WAR" : "") +
            '}';
    }
}
