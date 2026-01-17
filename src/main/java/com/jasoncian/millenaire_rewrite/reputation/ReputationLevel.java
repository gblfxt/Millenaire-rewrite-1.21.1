package com.jasoncian.millenaire_rewrite.reputation;

/**
 * 声望等级枚举 - 定义玩家与村庄/文化的关系等级
 *
 * 声望影响：
 * - 交易价格折扣/加价
 * - 可用任务
 * - 村民态度
 * - 特殊物品解锁
 *
 * @author Based on OldSource reputation system
 * @version 1.0.0
 */
public enum ReputationLevel {

    // ================ 负面等级 ================

    /** 仇敌 - 村民会攻击玩家 */
    ENEMY("enemy", -1000, -500, -0.50, false, true),

    /** 敌对 - 村民拒绝交易 */
    HOSTILE("hostile", -500, -200, -0.30, false, false),

    /** 不受欢迎 - 价格大幅提高 */
    UNWELCOME("unwelcome", -200, -50, -0.15, false, false),

    // ================ 中立等级 ================

    /** 陌生人 - 默认状态 */
    STRANGER("stranger", -50, 50, 0.0, true, false),

    /** 熟人 - 略有好感 */
    ACQUAINTANCE("acquaintance", 50, 200, 0.05, true, false),

    // ================ 正面等级 ================

    /** 朋友 - 价格优惠 */
    FRIEND("friend", 200, 500, 0.10, true, false),

    /** 盟友 - 更多任务和优惠 */
    ALLY("ally", 500, 1000, 0.15, true, false),

    /** 英雄 - 最高荣誉 */
    HERO("hero", 1000, 2000, 0.25, true, false),

    /** 传奇 - 文化领袖级别 */
    LEGEND("legend", 2000, Integer.MAX_VALUE, 0.35, true, false);

    // ================ 属性 ================

    private final String id;
    private final int minPoints;
    private final int maxPoints;
    private final double priceModifier;
    private final boolean canTrade;
    private final boolean isHostile;

    ReputationLevel(String id, int minPoints, int maxPoints, double priceModifier,
                    boolean canTrade, boolean isHostile) {
        this.id = id;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.priceModifier = priceModifier;
        this.canTrade = canTrade;
        this.isHostile = isHostile;
    }

    // ================ Getters ================

    public String getId() {
        return id;
    }

    /**
     * 获取此等级的最低声望点数
     */
    public int getMinPoints() {
        return minPoints;
    }

    /**
     * 获取此等级的最高声望点数
     */
    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * 获取价格修正
     * 正数表示折扣，负数表示加价
     */
    public double getPriceModifier() {
        return priceModifier;
    }

    /**
     * 是否可以与村民交易
     */
    public boolean canTrade() {
        return canTrade;
    }

    /**
     * 村民是否会攻击玩家
     */
    public boolean isHostile() {
        return isHostile;
    }

    // ================ 工具方法 ================

    /**
     * 根据声望点数获取对应等级
     */
    public static ReputationLevel fromPoints(int points) {
        for (ReputationLevel level : values()) {
            if (points >= level.minPoints && points < level.maxPoints) {
                return level;
            }
        }
        // 极端情况处理
        if (points < ENEMY.minPoints) {
            return ENEMY;
        }
        return LEGEND;
    }

    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        return switch (this) {
            case ENEMY -> "Enemy";
            case HOSTILE -> "Hostile";
            case UNWELCOME -> "Unwelcome";
            case STRANGER -> "Stranger";
            case ACQUAINTANCE -> "Acquaintance";
            case FRIEND -> "Friend";
            case ALLY -> "Ally";
            case HERO -> "Hero";
            case LEGEND -> "Legend";
        };
    }

    /**
     * 获取显示颜色代码（用于GUI）
     */
    public int getColor() {
        return switch (this) {
            case ENEMY -> 0xFF0000;      // 红色
            case HOSTILE -> 0xFF4444;    // 浅红
            case UNWELCOME -> 0xFF8800;  // 橙色
            case STRANGER -> 0xAAAAAA;   // 灰色
            case ACQUAINTANCE -> 0xFFFF00; // 黄色
            case FRIEND -> 0x00FF00;     // 绿色
            case ALLY -> 0x00FFFF;       // 青色
            case HERO -> 0x00AAFF;       // 蓝色
            case LEGEND -> 0xAA00FF;     // 紫色
        };
    }

    /**
     * 获取下一个等级（用于升级提示）
     */
    public ReputationLevel getNextLevel() {
        int ordinal = this.ordinal();
        if (ordinal < values().length - 1) {
            return values()[ordinal + 1];
        }
        return this;
    }

    /**
     * 获取上一个等级（用于降级提示）
     */
    public ReputationLevel getPreviousLevel() {
        int ordinal = this.ordinal();
        if (ordinal > 0) {
            return values()[ordinal - 1];
        }
        return this;
    }

    /**
     * 计算到下一等级所需的点数
     */
    public int getPointsToNextLevel(int currentPoints) {
        ReputationLevel next = getNextLevel();
        if (next == this) {
            return 0; // 已经是最高等级
        }
        return next.minPoints - currentPoints;
    }

    /**
     * 检查是否是正面声望
     */
    public boolean isPositive() {
        return this.ordinal() >= ACQUAINTANCE.ordinal();
    }

    /**
     * 检查是否是负面声望
     */
    public boolean isNegative() {
        return this.ordinal() <= UNWELCOME.ordinal();
    }

    /**
     * 从ID获取等级
     */
    public static ReputationLevel fromId(String id) {
        for (ReputationLevel level : values()) {
            if (level.id.equals(id)) {
                return level;
            }
        }
        return STRANGER;
    }
}
