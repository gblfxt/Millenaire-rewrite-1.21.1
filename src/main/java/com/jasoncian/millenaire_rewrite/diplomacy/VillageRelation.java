package com.jasoncian.millenaire_rewrite.diplomacy;

/**
 * 村庄关系枚举 - 定义两个村庄之间可能的关系状态
 *
 * 关系等级从最友好到最敌对：
 * ALLIED > FRIENDLY > NEUTRAL > UNFRIENDLY > HOSTILE > WAR
 *
 * @author Based on OldSource diplomacy
 * @version 1.0.0
 */
public enum VillageRelation {

    // ================ 关系等级 ================

    /**
     * 同盟 - 最高级别的友好关系
     * - 共享资源和防御
     * - 村民可以自由迁移
     * - 联合对抗敌人
     */
    ALLIED("allied", "Allied", 500, 0.8f, true, true),

    /**
     * 友好 - 良好的贸易关系
     * - 贸易价格优惠
     * - 可能提供援助
     * - 不会主动攻击
     */
    FRIENDLY("friendly", "Friendly", 200, 0.9f, true, false),

    /**
     * 中立 - 默认关系
     * - 正常贸易
     * - 互不干涉
     */
    NEUTRAL("neutral", "Neutral", 0, 1.0f, true, false),

    /**
     * 不友好 - 紧张关系
     * - 贸易价格上涨
     * - 可能拒绝交易
     * - 边境摩擦
     */
    UNFRIENDLY("unfriendly", "Unfriendly", -100, 1.2f, false, false),

    /**
     * 敌对 - 公开的敌意
     * - 拒绝所有贸易
     * - 可能发生小规模冲突
     * - 村民之间敌视
     */
    HOSTILE("hostile", "Hostile", -300, 1.5f, false, false),

    /**
     * 战争 - 公开战争状态
     * - 主动攻击对方村民
     * - 袭击对方村庄
     * - 完全敌对
     */
    WAR("war", "At War", -500, 2.0f, false, false);

    // ================ 属性 ================

    private final String id;
    private final String displayName;
    private final int minPoints;
    private final float tradeModifier;
    private final boolean allowsTrade;
    private final boolean sharesDefense;

    VillageRelation(String id, String displayName, int minPoints,
                    float tradeModifier, boolean allowsTrade, boolean sharesDefense) {
        this.id = id;
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.tradeModifier = tradeModifier;
        this.allowsTrade = allowsTrade;
        this.sharesDefense = sharesDefense;
    }

    // ================ Getters ================

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 获取此关系等级所需的最低点数
     */
    public int getMinPoints() {
        return minPoints;
    }

    /**
     * 获取贸易价格修正系数
     * < 1.0 表示折扣，> 1.0 表示加价
     */
    public float getTradeModifier() {
        return tradeModifier;
    }

    /**
     * 是否允许贸易
     */
    public boolean allowsTrade() {
        return allowsTrade;
    }

    /**
     * 是否共享防御（同盟间）
     */
    public boolean sharesDefense() {
        return sharesDefense;
    }

    /**
     * 是否为敌对关系
     */
    public boolean isHostile() {
        return this == HOSTILE || this == WAR;
    }

    /**
     * 是否为友好关系
     */
    public boolean isFriendly() {
        return this == ALLIED || this == FRIENDLY;
    }

    /**
     * 是否为战争状态
     */
    public boolean isAtWar() {
        return this == WAR;
    }

    // ================ 静态方法 ================

    /**
     * 根据关系点数获取关系等级
     */
    public static VillageRelation fromPoints(int points) {
        if (points >= ALLIED.minPoints) return ALLIED;
        if (points >= FRIENDLY.minPoints) return FRIENDLY;
        if (points >= NEUTRAL.minPoints) return NEUTRAL;
        if (points >= UNFRIENDLY.minPoints) return UNFRIENDLY;
        if (points >= HOSTILE.minPoints) return HOSTILE;
        return WAR;
    }

    /**
     * 根据ID获取关系等级
     */
    public static VillageRelation fromId(String id) {
        for (VillageRelation relation : values()) {
            if (relation.id.equals(id)) {
                return relation;
            }
        }
        return NEUTRAL;
    }

    /**
     * 获取下一个更好的关系等级
     */
    public VillageRelation getBetter() {
        return switch (this) {
            case WAR -> HOSTILE;
            case HOSTILE -> UNFRIENDLY;
            case UNFRIENDLY -> NEUTRAL;
            case NEUTRAL -> FRIENDLY;
            case FRIENDLY, ALLIED -> ALLIED;
        };
    }

    /**
     * 获取下一个更差的关系等级
     */
    public VillageRelation getWorse() {
        return switch (this) {
            case ALLIED -> FRIENDLY;
            case FRIENDLY -> NEUTRAL;
            case NEUTRAL -> UNFRIENDLY;
            case UNFRIENDLY -> HOSTILE;
            case HOSTILE, WAR -> WAR;
        };
    }

    /**
     * 获取从当前关系到目标点数所需的点数变化
     */
    public int getPointsToReach(VillageRelation target) {
        return target.minPoints - this.minPoints;
    }

    /**
     * 获取关系颜色代码（用于UI显示）
     */
    public int getColor() {
        return switch (this) {
            case ALLIED -> 0x00FF00;    // 绿色
            case FRIENDLY -> 0x90EE90;  // 浅绿
            case NEUTRAL -> 0xFFFFFF;   // 白色
            case UNFRIENDLY -> 0xFFFF00; // 黄色
            case HOSTILE -> 0xFFA500;   // 橙色
            case WAR -> 0xFF0000;       // 红色
        };
    }

    /**
     * 获取关系描述文本
     */
    public String getDescription() {
        return switch (this) {
            case ALLIED -> "Strong allies sharing resources and defense";
            case FRIENDLY -> "Good relations with favorable trade";
            case NEUTRAL -> "Neither friend nor foe";
            case UNFRIENDLY -> "Tensions exist between villages";
            case HOSTILE -> "Open hostility, no trade allowed";
            case WAR -> "Active warfare between villages";
        };
    }
}
