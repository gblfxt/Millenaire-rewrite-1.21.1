package com.jasoncian.millenaire_rewrite.building;

/**
 * 建筑类型枚举 - 定义建筑的功能分类
 *
 * @author Based on OldSource BuildingTypes
 * @version 1.0.0
 */
public enum BuildingType {

    // ================ 核心建筑 ================

    /** 市政厅 - 村庄核心 */
    TOWN_HALL("town_hall", "Town Hall", true, 0),

    /** 住宅 - 村民居住 */
    HOUSE("house", "House", true, 10),

    // ================ 生产建筑 ================

    /** 农场 - 农业生产 */
    FARM("farm", "Farm", true, 20),

    /** 伐木场 - 木材生产 */
    LUMBERYARD("lumberyard", "Lumberyard", true, 20),

    /** 矿场 - 矿石生产 */
    MINE("mine", "Mine", true, 20),

    /** 铁匠铺 - 工具武器 */
    FORGE("forge", "Forge", true, 30),

    /** 面包房 - 食物生产 */
    BAKERY("bakery", "Bakery", true, 30),

    /** 磨坊 - 粮食加工 */
    MILL("mill", "Mill", true, 25),

    // ================ 商业建筑 ================

    /** 市场 - 交易场所 */
    MARKET("market", "Market", true, 40),

    /** 仓库 - 资源储存 */
    WAREHOUSE("warehouse", "Warehouse", true, 35),

    /** 酒馆 - 休息娱乐 */
    TAVERN("tavern", "Tavern", true, 45),

    // ================ 宗教建筑 ================

    /** 教堂/神庙 - 宗教场所 */
    TEMPLE("temple", "Temple", true, 50),

    /** 墓地 - 安葬场所 */
    GRAVEYARD("graveyard", "Graveyard", false, 60),

    // ================ 军事建筑 ================

    /** 兵营 - 士兵训练 */
    BARRACKS("barracks", "Barracks", true, 55),

    /** 瞭望塔 - 防御设施 */
    WATCHTOWER("watchtower", "Watchtower", false, 45),

    /** 城墙 - 防御设施 */
    WALL("wall", "Wall", false, 70),

    /** 城门 - 出入口 */
    GATE("gate", "Gate", false, 70),

    // ================ 特殊建筑 ================

    /** 井 - 水源 */
    WELL("well", "Well", false, 15),

    /** 装饰 - 美化建筑 */
    DECORATION("decoration", "Decoration", false, 80),

    /** 道路 - 连接建筑 */
    ROAD("road", "Road", false, 75),

    /** 自定义 - 玩家自定义 */
    CUSTOM("custom", "Custom Building", true, 100);

    // ================ 属性 ================

    private final String id;
    private final String displayName;
    private final boolean canHouseVillagers;
    private final int defaultPriority;

    BuildingType(String id, String displayName, boolean canHouseVillagers, int defaultPriority) {
        this.id = id;
        this.displayName = displayName;
        this.canHouseVillagers = canHouseVillagers;
        this.defaultPriority = defaultPriority;
    }

    // ================ Getters ================

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 此类型建筑是否可以容纳村民居住
     */
    public boolean canHouseVillagers() {
        return canHouseVillagers;
    }

    /**
     * 默认建造优先级（越低越优先）
     */
    public int getDefaultPriority() {
        return defaultPriority;
    }

    /**
     * 是否是生产类建筑
     */
    public boolean isProduction() {
        return this == FARM || this == LUMBERYARD || this == MINE ||
               this == FORGE || this == BAKERY || this == MILL;
    }

    /**
     * 是否是军事类建筑
     */
    public boolean isMilitary() {
        return this == BARRACKS || this == WATCHTOWER || this == WALL || this == GATE;
    }

    /**
     * 是否是基础设施
     */
    public boolean isInfrastructure() {
        return this == WELL || this == ROAD || this == DECORATION;
    }

    /**
     * 从ID获取建筑类型
     */
    public static BuildingType fromId(String id) {
        for (BuildingType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return CUSTOM;
    }
}
