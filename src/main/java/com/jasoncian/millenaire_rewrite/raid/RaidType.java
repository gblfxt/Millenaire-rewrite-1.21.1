package com.jasoncian.millenaire_rewrite.raid;

import net.minecraft.world.entity.EntityType;

import java.util.List;

/**
 * 袭击类型枚举 - 定义不同类型的村庄袭击
 *
 * 每种袭击类型有不同的：
 * - 触发条件
 * - 敌人组成
 * - 难度和奖励
 *
 * @author Based on OldSource raids
 * @version 1.0.0
 */
public enum RaidType {

    // ================ 怪物袭击 ================

    /**
     * 僵尸围城 - 夜间僵尸群攻
     */
    ZOMBIE_SIEGE(
        "zombie_siege",
        "Zombie Siege",
        "A horde of undead approaches!",
        3,      // 波数
        100,    // 基础难度
        50,     // 声望奖励
        true,   // 夜间限定
        false   // 非村庄间冲突
    ),

    /**
     * 骷髅袭击 - 远程骷髅攻击
     */
    SKELETON_ATTACK(
        "skeleton_attack",
        "Skeleton Attack",
        "Archers emerge from the darkness!",
        2,
        80,
        40,
        true,
        false
    ),

    /**
     * 蜘蛛入侵 - 蜘蛛群落
     */
    SPIDER_INVASION(
        "spider_invasion",
        "Spider Invasion",
        "Spiders crawl from the shadows!",
        2,
        60,
        30,
        true,
        false
    ),

    /**
     * 掠夺者袭击 - 掠夺者团伙
     */
    PILLAGER_RAID(
        "pillager_raid",
        "Pillager Raid",
        "Pillagers approach the village!",
        4,
        150,
        80,
        false,
        false
    ),

    // ================ 特殊怪物袭击 ================

    /**
     * 女巫来袭 - 带有女巫的混合袭击
     */
    WITCH_ATTACK(
        "witch_attack",
        "Witch Attack",
        "Dark magic fills the air!",
        2,
        120,
        60,
        true,
        false
    ),

    /**
     * 爬行者威胁 - 爆炸性袭击
     */
    CREEPER_THREAT(
        "creeper_threat",
        "Creeper Threat",
        "Explosive creatures approach!",
        2,
        100,
        45,
        true,
        false
    ),

    // ================ 村庄冲突 ================

    /**
     * 敌对村庄袭击 - 来自敌对村庄的攻击
     */
    VILLAGE_RAID(
        "village_raid",
        "Village Raid",
        "Warriors from a rival village attack!",
        3,
        200,
        100,
        false,
        true
    ),

    /**
     * 边境冲突 - 小规模村庄间摩擦
     */
    BORDER_SKIRMISH(
        "border_skirmish",
        "Border Skirmish",
        "Border tensions escalate!",
        1,
        50,
        25,
        false,
        true
    ),

    // ================ 稀有事件 ================

    /**
     * 强盗团 - 大规模盗贼袭击
     */
    BANDIT_GANG(
        "bandit_gang",
        "Bandit Gang",
        "Bandits target the village!",
        3,
        180,
        90,
        false,
        false
    ),

    /**
     * 末日灾难 - 极难的末影生物袭击
     */
    ENDER_CALAMITY(
        "ender_calamity",
        "Ender Calamity",
        "Reality tears as Ender creatures emerge!",
        2,
        300,
        150,
        true,
        false
    );

    // ================ 属性 ================

    private final String id;
    private final String displayName;
    private final String announcement;
    private final int waveCount;
    private final int baseDifficulty;
    private final int reputationReward;
    private final boolean nightOnly;
    private final boolean villageConflict;

    RaidType(String id, String displayName, String announcement,
             int waveCount, int baseDifficulty, int reputationReward,
             boolean nightOnly, boolean villageConflict) {
        this.id = id;
        this.displayName = displayName;
        this.announcement = announcement;
        this.waveCount = waveCount;
        this.baseDifficulty = baseDifficulty;
        this.reputationReward = reputationReward;
        this.nightOnly = nightOnly;
        this.villageConflict = villageConflict;
    }

    // ================ Getters ================

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public int getWaveCount() {
        return waveCount;
    }

    public int getBaseDifficulty() {
        return baseDifficulty;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public boolean isNightOnly() {
        return nightOnly;
    }

    public boolean isVillageConflict() {
        return villageConflict;
    }

    // ================ 实用方法 ================

    /**
     * 获取此袭击类型的难度描述
     */
    public String getDifficultyDescription() {
        if (baseDifficulty >= 250) return "Extreme";
        if (baseDifficulty >= 150) return "Hard";
        if (baseDifficulty >= 100) return "Medium";
        if (baseDifficulty >= 50) return "Easy";
        return "Trivial";
    }

    /**
     * 获取主要敌人类型
     */
    public EntityType<?> getPrimaryEntityType() {
        return switch (this) {
            case ZOMBIE_SIEGE -> EntityType.ZOMBIE;
            case SKELETON_ATTACK -> EntityType.SKELETON;
            case SPIDER_INVASION -> EntityType.SPIDER;
            case PILLAGER_RAID -> EntityType.PILLAGER;
            case WITCH_ATTACK -> EntityType.WITCH;
            case CREEPER_THREAT -> EntityType.CREEPER;
            case ENDER_CALAMITY -> EntityType.ENDERMAN;
            case VILLAGE_RAID, BORDER_SKIRMISH, BANDIT_GANG -> EntityType.VINDICATOR;
        };
    }

    /**
     * 根据ID获取袭击类型
     */
    public static RaidType fromId(String id) {
        for (RaidType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return ZOMBIE_SIEGE; // 默认
    }

    /**
     * 获取所有怪物袭击类型
     */
    public static List<RaidType> getMonsterRaids() {
        return List.of(ZOMBIE_SIEGE, SKELETON_ATTACK, SPIDER_INVASION,
            PILLAGER_RAID, WITCH_ATTACK, CREEPER_THREAT, ENDER_CALAMITY, BANDIT_GANG);
    }

    /**
     * 获取所有村庄冲突类型
     */
    public static List<RaidType> getVillageConflicts() {
        return List.of(VILLAGE_RAID, BORDER_SKIRMISH);
    }

    /**
     * 根据难度范围获取随机袭击类型
     */
    public static RaidType getRandomForDifficulty(int minDifficulty, int maxDifficulty, boolean allowNightOnly) {
        List<RaidType> suitable = new java.util.ArrayList<>();
        for (RaidType type : getMonsterRaids()) {
            if (type.baseDifficulty >= minDifficulty &&
                type.baseDifficulty <= maxDifficulty &&
                (allowNightOnly || !type.nightOnly)) {
                suitable.add(type);
            }
        }
        if (suitable.isEmpty()) {
            return ZOMBIE_SIEGE;
        }
        return suitable.get(new java.util.Random().nextInt(suitable.size()));
    }
}
