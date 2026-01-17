package com.jasoncian.millenaire_rewrite.entity.villager;

import java.util.HashMap;
import java.util.Map;

/**
 * 村民职业枚举 - 定义所有可用的村民职业
 *
 * 职业决定：
 * - 村民的工作目标
 * - 可用的工具和装备
 * - 在村庄中的角色
 *
 * @author Based on OldSource VillagerType
 * @version 1.0.0
 */
public enum VillagerProfession {

    // ================ 通用职业 ================
    FARMER("farmer", "Farmer", "农民", true),
    MINER("miner", "Miner", "矿工", true),
    LUMBERJACK("lumberjack", "Lumberjack", "伐木工", true),
    GUARD("guard", "Guard", "守卫", true),
    MERCHANT("merchant", "Merchant", "商人", true),
    WIFE("wife", "Wife", "妻子", false),
    CHILD("child", "Child", "孩子", true),

    // ================ 诺曼特有 ================
    KNIGHT("knight", "Knight", "骑士", true),
    PRIEST("priest", "Priest", "牧师", true),
    BLACKSMITH("blacksmith", "Blacksmith", "铁匠", true),

    // ================ 拜占庭特有 ================
    SILK_FARMER("silk_farmer", "Silk Farmer", "蚕农", true),
    ORTHODOX_PRIEST("orthodox_priest", "Orthodox Priest", "东正教牧师", true),

    // ================ 日本特有 ================
    SAMURAI("samurai", "Samurai", "武士", true),
    MONK("monk", "Monk", "僧侣", true),

    // ================ 玛雅特有 ================
    HUNTER("hunter", "Hunter", "猎人", true),
    SHAMAN("shaman", "Shaman", "萨满", true),

    // ================ 印度特有 ================
    BRICK_MAKER("brick_maker", "Brick Maker", "制砖工", true),
    SADHU("sadhu", "Sadhu", "苦行僧", true),

    // ================ 因纽特特有 ================
    FISHER("fisher", "Fisher", "渔夫", true),

    // ================ 塞尔柱特有 ================
    SHEPHERD("shepherd", "Shepherd", "牧羊人", true),
    IMAM("imam", "Imam", "伊玛目", true),

    // ================ 特殊职业 ================
    CHIEF("chief", "Chief", "首领", true),
    VISITOR("visitor", "Visitor", "访客", true),
    FOREIGN_MERCHANT("foreign_merchant", "Foreign Merchant", "外国商人", true),
    RAIDER("raider", "Raider", "劫掠者", true);

    private static final Map<String, VillagerProfession> BY_ID = new HashMap<>();

    static {
        for (VillagerProfession profession : values()) {
            BY_ID.put(profession.id, profession);
        }
    }

    private final String id;
    private final String displayName;
    private final String chineseName;
    private final boolean canBeMale;

    VillagerProfession(String id, String displayName, String chineseName, boolean canBeMale) {
        this.id = id;
        this.displayName = displayName;
        this.chineseName = chineseName;
        this.canBeMale = canBeMale;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getChineseName() {
        return chineseName;
    }

    /**
     * 检查此职业是否可以是男性
     * 某些职业（如妻子）只能是女性
     */
    public boolean canBeMale() {
        return canBeMale;
    }

    /**
     * 检查是否为战斗职业
     */
    public boolean isCombatProfession() {
        return this == GUARD || this == KNIGHT || this == SAMURAI ||
               this == HUNTER || this == RAIDER;
    }

    /**
     * 检查是否为领导职业
     */
    public boolean isLeaderProfession() {
        return this == CHIEF || this == PRIEST || this == ORTHODOX_PRIEST ||
               this == MONK || this == SHAMAN || this == SADHU || this == IMAM;
    }

    /**
     * 检查是否为商业职业
     */
    public boolean isMerchantProfession() {
        return this == MERCHANT || this == FOREIGN_MERCHANT;
    }

    /**
     * 从ID获取职业
     */
    public static VillagerProfession fromId(String id) {
        VillagerProfession profession = BY_ID.get(id.toLowerCase());
        return profession != null ? profession : FARMER;
    }

    /**
     * 检查ID是否有效
     */
    public static boolean isValidId(String id) {
        return BY_ID.containsKey(id.toLowerCase());
    }
}
