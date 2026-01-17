package com.jasoncian.millenaire_rewrite.quest;

/**
 * 任务类型枚举 - 定义不同类型的任务
 *
 * @author Based on OldSource quest types
 * @version 1.0.0
 */
public enum QuestType {

    // ================ 收集任务 ================

    /** 收集物品 - 收集指定数量的物品 */
    GATHER("gather", "Gather Items", 5, false),

    /** 采集资源 - 采集特定资源 */
    HARVEST("harvest", "Harvest Resources", 5, false),

    /** 狩猎 - 击杀特定生物 */
    HUNT("hunt", "Hunt Creatures", 10, false),

    // ================ 交付任务 ================

    /** 交付物品 - 将物品送到指定地点/人 */
    DELIVER("deliver", "Deliver Items", 8, false),

    /** 贸易运输 - 在村庄间运送货物 */
    TRADE_ROUTE("trade_route", "Trade Route", 15, false),

    // ================ 建造任务 ================

    /** 帮助建造 - 提供建造材料 */
    CONSTRUCTION("construction", "Construction Help", 12, false),

    /** 修复建筑 - 修复损坏的建筑 */
    REPAIR("repair", "Repair Building", 10, false),

    // ================ 探索任务 ================

    /** 探索 - 发现新地点 */
    EXPLORE("explore", "Explore Area", 8, false),

    /** 侦察 - 调查特定区域 */
    SCOUT("scout", "Scout Location", 10, false),

    // ================ 社交任务 ================

    /** 外交 - 改善与其他村庄的关系 */
    DIPLOMACY("diplomacy", "Diplomatic Mission", 20, false),

    /** 信使 - 传递消息 */
    MESSENGER("messenger", "Deliver Message", 5, false),

    // ================ 战斗任务 ================

    /** 保护 - 保护村庄免受攻击 */
    DEFEND("defend", "Defend Village", 25, true),

    /** 清剿 - 清除附近威胁 */
    CLEAR("clear", "Clear Threats", 20, true),

    /** 护送 - 护送村民到目的地 */
    ESCORT("escort", "Escort Villager", 15, true),

    // ================ 特殊任务 ================

    /** 主线任务 - 推进村庄发展 */
    STORY("story", "Village Story", 30, false),

    /** 节日任务 - 特殊活动期间 */
    FESTIVAL("festival", "Festival Task", 10, false),

    /** 紧急任务 - 需要立即完成 */
    URGENT("urgent", "Urgent Task", 20, true);

    // ================ 属性 ================

    private final String id;
    private final String displayName;
    private final int baseReputationReward;
    private final boolean combat;

    QuestType(String id, String displayName, int baseReputationReward, boolean combat) {
        this.id = id;
        this.displayName = displayName;
        this.baseReputationReward = baseReputationReward;
        this.combat = combat;
    }

    // ================ Getters ================

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取基础声望奖励
     */
    public int getBaseReputationReward() {
        return baseReputationReward;
    }

    /**
     * 是否是战斗类任务
     */
    public boolean isCombat() {
        return combat;
    }

    /**
     * 是否是收集类任务
     */
    public boolean isGathering() {
        return this == GATHER || this == HARVEST || this == HUNT;
    }

    /**
     * 是否是交付类任务
     */
    public boolean isDelivery() {
        return this == DELIVER || this == TRADE_ROUTE || this == MESSENGER;
    }

    /**
     * 是否是建造类任务
     */
    public boolean isConstruction() {
        return this == CONSTRUCTION || this == REPAIR;
    }

    /**
     * 是否是探索类任务
     */
    public boolean isExploration() {
        return this == EXPLORE || this == SCOUT;
    }

    /**
     * 获取任务难度描述
     */
    public String getDifficultyDescription() {
        if (baseReputationReward >= 25) return "Hard";
        if (baseReputationReward >= 15) return "Medium";
        if (baseReputationReward >= 10) return "Easy";
        return "Simple";
    }

    /**
     * 从ID获取任务类型
     */
    public static QuestType fromId(String id) {
        for (QuestType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return GATHER; // 默认
    }
}
