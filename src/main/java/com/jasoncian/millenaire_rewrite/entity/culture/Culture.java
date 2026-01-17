package com.jasoncian.millenaire_rewrite.entity.culture;

import java.util.HashMap;
import java.util.Map;

/**
 * 文化枚举 - 定义Millenaire支持的所有文化
 *
 * 每个文化有独特的：
 * - 建筑风格
 * - 村民类型和职业
 * - 物品和食物
 * - 名字生成规则
 *
 * @author Based on OldSource Culture system
 * @version 1.0.0
 */
public enum Culture {

    NORMAN("norman", "Norman", "诺曼"),
    BYZANTINE("byzantine", "Byzantine", "拜占庭"),
    INDIAN("indian", "Indian", "印度"),
    JAPANESE("japanese", "Japanese", "日本"),
    MAYAN("mayan", "Mayan", "玛雅"),
    INUIT("inuit", "Inuit", "因纽特"),
    SELJUK("seljuk", "Seljuk", "塞尔柱");

    private static final Map<String, Culture> BY_ID = new HashMap<>();

    static {
        for (Culture culture : values()) {
            BY_ID.put(culture.id, culture);
        }
    }

    private final String id;
    private final String displayName;
    private final String chineseName;

    Culture(String id, String displayName, String chineseName) {
        this.id = id;
        this.displayName = displayName;
        this.chineseName = chineseName;
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
     * 从ID获取文化
     */
    public static Culture fromId(String id) {
        Culture culture = BY_ID.get(id.toLowerCase());
        return culture != null ? culture : NORMAN;
    }

    /**
     * 检查ID是否有效
     */
    public static boolean isValidId(String id) {
        return BY_ID.containsKey(id.toLowerCase());
    }
}
