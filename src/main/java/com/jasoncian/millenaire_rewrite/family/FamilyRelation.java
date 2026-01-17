package com.jasoncian.millenaire_rewrite.family;

/**
 * 家庭关系枚举 - 定义村民之间的亲属关系
 *
 * @author Based on OldSource family relations
 * @version 1.0.0
 */
public enum FamilyRelation {

    // ================ 直系亲属 ================

    /** 配偶 */
    SPOUSE("spouse", "Spouse", true, 0),

    /** 父亲 */
    FATHER("father", "Father", false, 1),

    /** 母亲 */
    MOTHER("mother", "Mother", false, 1),

    /** 儿子 */
    SON("son", "Son", false, -1),

    /** 女儿 */
    DAUGHTER("daughter", "Daughter", false, -1),

    // ================ 兄弟姐妹 ================

    /** 兄弟 */
    BROTHER("brother", "Brother", false, 0),

    /** 姐妹 */
    SISTER("sister", "Sister", false, 0),

    // ================ 祖辈 ================

    /** 祖父 */
    GRANDFATHER("grandfather", "Grandfather", false, 2),

    /** 祖母 */
    GRANDMOTHER("grandmother", "Grandmother", false, 2),

    // ================ 孙辈 ================

    /** 孙子 */
    GRANDSON("grandson", "Grandson", false, -2),

    /** 孙女 */
    GRANDDAUGHTER("granddaughter", "Granddaughter", false, -2),

    // ================ 叔伯姑姨 ================

    /** 叔叔/伯伯 */
    UNCLE("uncle", "Uncle", false, 1),

    /** 阿姨/姑姑 */
    AUNT("aunt", "Aunt", false, 1),

    // ================ 侄子侄女 ================

    /** 侄子/外甥 */
    NEPHEW("nephew", "Nephew", false, -1),

    /** 侄女/外甥女 */
    NIECE("niece", "Niece", false, -1),

    // ================ 堂/表亲 ================

    /** 堂/表兄弟姐妹 */
    COUSIN("cousin", "Cousin", false, 0),

    // ================ 姻亲 ================

    /** 岳父/公公 */
    FATHER_IN_LAW("father_in_law", "Father-in-law", false, 1),

    /** 岳母/婆婆 */
    MOTHER_IN_LAW("mother_in_law", "Mother-in-law", false, 1),

    /** 女婿 */
    SON_IN_LAW("son_in_law", "Son-in-law", false, -1),

    /** 儿媳 */
    DAUGHTER_IN_LAW("daughter_in_law", "Daughter-in-law", false, -1),

    /** 大伯/小叔/姐夫/妹夫 */
    BROTHER_IN_LAW("brother_in_law", "Brother-in-law", false, 0),

    /** 大姑/小姑/嫂子/弟媳 */
    SISTER_IN_LAW("sister_in_law", "Sister-in-law", false, 0),

    // ================ 特殊关系 ================

    /** 未婚夫/未婚妻 */
    FIANCE("fiance", "Fiancé(e)", true, 0),

    /** 无关系 */
    NONE("none", "Unrelated", false, 0);

    // ================ 属性 ================

    private final String id;
    private final String displayName;
    private final boolean romantic;
    private final int generationDiff; // 代差（正数为长辈，负数为晚辈）

    FamilyRelation(String id, String displayName, boolean romantic, int generationDiff) {
        this.id = id;
        this.displayName = displayName;
        this.romantic = romantic;
        this.generationDiff = generationDiff;
    }

    // ================ Getters ================

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 是否为浪漫关系
     */
    public boolean isRomantic() {
        return romantic;
    }

    /**
     * 获取代差
     */
    public int getGenerationDiff() {
        return generationDiff;
    }

    /**
     * 是否为直系亲属
     */
    public boolean isDirectFamily() {
        return this == FATHER || this == MOTHER || this == SON || this == DAUGHTER ||
               this == GRANDFATHER || this == GRANDMOTHER || this == GRANDSON || this == GRANDDAUGHTER;
    }

    /**
     * 是否为兄弟姐妹
     */
    public boolean isSibling() {
        return this == BROTHER || this == SISTER;
    }

    /**
     * 是否为姻亲
     */
    public boolean isInLaw() {
        return this == FATHER_IN_LAW || this == MOTHER_IN_LAW ||
               this == SON_IN_LAW || this == DAUGHTER_IN_LAW ||
               this == BROTHER_IN_LAW || this == SISTER_IN_LAW;
    }

    /**
     * 是否为父母
     */
    public boolean isParent() {
        return this == FATHER || this == MOTHER;
    }

    /**
     * 是否为子女
     */
    public boolean isChild() {
        return this == SON || this == DAUGHTER;
    }

    /**
     * 是否禁止结婚（近亲）
     */
    public boolean prohibitsMarriage() {
        // 禁止与直系亲属、兄弟姐妹、叔伯姑姨结婚
        return isDirectFamily() || isSibling() ||
               this == UNCLE || this == AUNT ||
               this == NEPHEW || this == NIECE;
    }

    /**
     * 获取反向关系（从对方角度）
     */
    public FamilyRelation getReverse(boolean isMale) {
        return switch (this) {
            case SPOUSE -> SPOUSE;
            case FIANCE -> FIANCE;
            case FATHER, MOTHER -> isMale ? SON : DAUGHTER;
            case SON, DAUGHTER -> isMale ? FATHER : MOTHER;
            case BROTHER, SISTER -> isMale ? BROTHER : SISTER;
            case GRANDFATHER, GRANDMOTHER -> isMale ? GRANDSON : GRANDDAUGHTER;
            case GRANDSON, GRANDDAUGHTER -> isMale ? GRANDFATHER : GRANDMOTHER;
            case UNCLE, AUNT -> isMale ? NEPHEW : NIECE;
            case NEPHEW, NIECE -> isMale ? UNCLE : AUNT;
            case COUSIN -> COUSIN;
            case FATHER_IN_LAW, MOTHER_IN_LAW -> isMale ? SON_IN_LAW : DAUGHTER_IN_LAW;
            case SON_IN_LAW, DAUGHTER_IN_LAW -> isMale ? FATHER_IN_LAW : MOTHER_IN_LAW;
            case BROTHER_IN_LAW, SISTER_IN_LAW -> isMale ? BROTHER_IN_LAW : SISTER_IN_LAW;
            case NONE -> NONE;
        };
    }

    /**
     * 根据ID获取关系
     */
    public static FamilyRelation fromId(String id) {
        for (FamilyRelation relation : values()) {
            if (relation.id.equals(id)) {
                return relation;
            }
        }
        return NONE;
    }

    /**
     * 获取父母关系
     */
    public static FamilyRelation getParentRelation(boolean isMale) {
        return isMale ? FATHER : MOTHER;
    }

    /**
     * 获取子女关系
     */
    public static FamilyRelation getChildRelation(boolean isMale) {
        return isMale ? SON : DAUGHTER;
    }

    /**
     * 获取兄弟姐妹关系
     */
    public static FamilyRelation getSiblingRelation(boolean isMale) {
        return isMale ? BROTHER : SISTER;
    }
}
