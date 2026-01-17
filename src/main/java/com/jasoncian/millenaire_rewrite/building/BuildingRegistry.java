package com.jasoncian.millenaire_rewrite.building;

import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 建筑注册表 - 管理所有文化的建筑蓝图
 *
 * @author Based on OldSource BuildingRegistry
 * @version 1.0.0
 */
public class BuildingRegistry {

    // ================ 单例 ================

    private static final BuildingRegistry INSTANCE = new BuildingRegistry();

    public static BuildingRegistry getInstance() {
        return INSTANCE;
    }

    // ================ 数据 ================

    /** 所有蓝图（按键索引） */
    private final Map<String, BuildingBlueprint> blueprints = new HashMap<>();

    /** 按文化索引的蓝图 */
    private final Map<Culture, List<BuildingBlueprint>> blueprintsByCulture = new EnumMap<>(Culture.class);

    /** 按类型索引的蓝图 */
    private final Map<BuildingType, List<BuildingBlueprint>> blueprintsByType = new EnumMap<>(BuildingType.class);

    // ================ 构造函数 ================

    private BuildingRegistry() {
        // 初始化索引
        for (Culture culture : Culture.values()) {
            blueprintsByCulture.put(culture, new ArrayList<>());
        }
        for (BuildingType type : BuildingType.values()) {
            blueprintsByType.put(type, new ArrayList<>());
        }

        // 注册默认建筑
        registerDefaultBuildings();
    }

    // ================ 注册 ================

    /**
     * 注册蓝图
     */
    public void register(BuildingBlueprint blueprint) {
        blueprints.put(blueprint.getKey(), blueprint);
        blueprintsByCulture.get(blueprint.getCulture()).add(blueprint);
        blueprintsByType.get(blueprint.getType()).add(blueprint);
    }

    /**
     * 注册默认建筑
     */
    private void registerDefaultBuildings() {
        // 诺曼建筑
        registerNormanBuildings();

        // 日本建筑
        registerJapaneseBuildings();

        // 印度建筑
        registerIndianBuildings();

        // 玛雅建筑
        registerMayanBuildings();

        // 拜占庭建筑
        registerByzantineBuildings();
    }

    // ================ 诺曼建筑 ================

    private void registerNormanBuildings() {
        // 诺曼市政厅
        BuildingBlueprint normanTownHall = new BuildingBlueprint("norman_town_hall");
        normanTownHall.setDisplayName("Norman Town Hall");
        normanTownHall.setCulture(Culture.NORMAN);
        normanTownHall.setType(BuildingType.TOWN_HALL);
        normanTownHall.setDimensions(9, 9, 6);
        normanTownHall.setBuildPriority(0);
        normanTownHall.setVillagerCapacity(4);
        normanTownHall.generateSimpleHouse(9, 9, 6, Blocks.COBBLESTONE, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS);
        normanTownHall.addResource(Items.COBBLESTONE, 64);
        normanTownHall.addResource(Items.OAK_PLANKS, 48);
        normanTownHall.addResource(Items.OAK_LOG, 16);
        register(normanTownHall);

        // 诺曼农舍
        BuildingBlueprint normanFarmhouse = new BuildingBlueprint("norman_farmhouse");
        normanFarmhouse.setDisplayName("Norman Farmhouse");
        normanFarmhouse.setCulture(Culture.NORMAN);
        normanFarmhouse.setType(BuildingType.HOUSE);
        normanFarmhouse.setDimensions(6, 5, 4);
        normanFarmhouse.setBuildPriority(10);
        normanFarmhouse.setVillagerCapacity(2);
        normanFarmhouse.setRequiredProfession(VillagerProfession.FARMER);
        normanFarmhouse.generateSimpleHouse(6, 5, 4, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS);
        normanFarmhouse.addResource(Items.OAK_PLANKS, 32);
        normanFarmhouse.addResource(Items.OAK_LOG, 8);
        register(normanFarmhouse);

        // 诺曼铁匠铺
        BuildingBlueprint normanForge = new BuildingBlueprint("norman_forge");
        normanForge.setDisplayName("Norman Forge");
        normanForge.setCulture(Culture.NORMAN);
        normanForge.setType(BuildingType.FORGE);
        normanForge.setDimensions(7, 6, 5);
        normanForge.setBuildPriority(30);
        normanForge.setVillagerCapacity(2);
        normanForge.setRequiredProfession(VillagerProfession.BLACKSMITH);
        normanForge.generateSimpleHouse(7, 6, 5, Blocks.COBBLESTONE, Blocks.STONE, Blocks.COBBLESTONE);
        normanForge.addWorkSpot(3, 1, 3);
        normanForge.addResource(Items.COBBLESTONE, 48);
        normanForge.addResource(Items.IRON_INGOT, 8);
        register(normanForge);

        // 诺曼兵营
        BuildingBlueprint normanBarracks = new BuildingBlueprint("norman_barracks");
        normanBarracks.setDisplayName("Norman Barracks");
        normanBarracks.setCulture(Culture.NORMAN);
        normanBarracks.setType(BuildingType.BARRACKS);
        normanBarracks.setDimensions(10, 8, 5);
        normanBarracks.setBuildPriority(55);
        normanBarracks.setMinVillageLevel(2);
        normanBarracks.setVillagerCapacity(4);
        normanBarracks.setRequiredProfession(VillagerProfession.SOLDIER);
        normanBarracks.generateSimpleHouse(10, 8, 5, Blocks.COBBLESTONE, Blocks.STONE, Blocks.OAK_PLANKS);
        normanBarracks.addResource(Items.COBBLESTONE, 72);
        normanBarracks.addResource(Items.OAK_PLANKS, 24);
        normanBarracks.addResource(Items.IRON_INGOT, 4);
        register(normanBarracks);

        // 诺曼教堂
        BuildingBlueprint normanChurch = new BuildingBlueprint("norman_church");
        normanChurch.setDisplayName("Norman Church");
        normanChurch.setCulture(Culture.NORMAN);
        normanChurch.setType(BuildingType.TEMPLE);
        normanChurch.setDimensions(8, 12, 8);
        normanChurch.setBuildPriority(50);
        normanChurch.setMinVillageLevel(2);
        normanChurch.setVillagerCapacity(1);
        normanChurch.setRequiredProfession(VillagerProfession.PRIEST);
        normanChurch.generateSimpleHouse(8, 12, 8, Blocks.STONE_BRICKS, Blocks.STONE, Blocks.STONE_BRICKS);
        normanChurch.addResource(Items.STONE_BRICKS, 96);
        normanChurch.addResource(Items.GLASS, 16);
        register(normanChurch);
    }

    // ================ 日本建筑 ================

    private void registerJapaneseBuildings() {
        // 日本市政厅（城）
        BuildingBlueprint japaneseTownHall = new BuildingBlueprint("japanese_town_hall");
        japaneseTownHall.setDisplayName("Japanese Castle");
        japaneseTownHall.setCulture(Culture.JAPANESE);
        japaneseTownHall.setType(BuildingType.TOWN_HALL);
        japaneseTownHall.setDimensions(11, 11, 8);
        japaneseTownHall.setBuildPriority(0);
        japaneseTownHall.setVillagerCapacity(4);
        japaneseTownHall.generateSimpleHouse(11, 11, 8, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS);
        japaneseTownHall.addResource(Items.DARK_OAK_PLANKS, 80);
        japaneseTownHall.addResource(Items.DARK_OAK_LOG, 24);
        japaneseTownHall.addResource(Items.STONE, 32);
        register(japaneseTownHall);

        // 日本农家
        BuildingBlueprint japaneseFarmhouse = new BuildingBlueprint("japanese_farmhouse");
        japaneseFarmhouse.setDisplayName("Japanese Farmhouse");
        japaneseFarmhouse.setCulture(Culture.JAPANESE);
        japaneseFarmhouse.setType(BuildingType.HOUSE);
        japaneseFarmhouse.setDimensions(6, 6, 4);
        japaneseFarmhouse.setBuildPriority(10);
        japaneseFarmhouse.setVillagerCapacity(2);
        japaneseFarmhouse.generateSimpleHouse(6, 6, 4, Blocks.DARK_OAK_PLANKS, Blocks.BAMBOO_MOSAIC, Blocks.DARK_OAK_PLANKS);
        japaneseFarmhouse.addResource(Items.DARK_OAK_PLANKS, 28);
        japaneseFarmhouse.addResource(Items.BAMBOO, 16);
        register(japaneseFarmhouse);

        // 道场
        BuildingBlueprint dojo = new BuildingBlueprint("japanese_dojo");
        dojo.setDisplayName("Dojo");
        dojo.setCulture(Culture.JAPANESE);
        dojo.setType(BuildingType.BARRACKS);
        dojo.setDimensions(8, 8, 5);
        dojo.setBuildPriority(55);
        dojo.setMinVillageLevel(2);
        dojo.setVillagerCapacity(3);
        dojo.setRequiredProfession(VillagerProfession.SAMURAI);
        dojo.generateSimpleHouse(8, 8, 5, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_PLANKS);
        dojo.addResource(Items.DARK_OAK_PLANKS, 48);
        dojo.addResource(Items.BAMBOO, 24);
        register(dojo);

        // 神社
        BuildingBlueprint shrine = new BuildingBlueprint("japanese_shrine");
        shrine.setDisplayName("Shinto Shrine");
        shrine.setCulture(Culture.JAPANESE);
        shrine.setType(BuildingType.TEMPLE);
        shrine.setDimensions(7, 10, 6);
        shrine.setBuildPriority(50);
        shrine.setMinVillageLevel(2);
        shrine.setVillagerCapacity(1);
        shrine.setRequiredProfession(VillagerProfession.MONK);
        shrine.generateSimpleHouse(7, 10, 6, Blocks.SPRUCE_PLANKS, Blocks.STONE, Blocks.SPRUCE_PLANKS);
        shrine.addResource(Items.SPRUCE_PLANKS, 56);
        shrine.addResource(Items.STONE, 24);
        register(shrine);
    }

    // ================ 印度建筑 ================

    private void registerIndianBuildings() {
        // 印度市政厅（宫殿）
        BuildingBlueprint indianTownHall = new BuildingBlueprint("indian_town_hall");
        indianTownHall.setDisplayName("Indian Palace");
        indianTownHall.setCulture(Culture.INDIAN);
        indianTownHall.setType(BuildingType.TOWN_HALL);
        indianTownHall.setDimensions(10, 10, 7);
        indianTownHall.setBuildPriority(0);
        indianTownHall.setVillagerCapacity(4);
        indianTownHall.generateSimpleHouse(10, 10, 7, Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE);
        indianTownHall.addResource(Items.SANDSTONE, 72);
        indianTownHall.addResource(Items.TERRACOTTA, 24);
        register(indianTownHall);

        // 印度民居
        BuildingBlueprint indianHouse = new BuildingBlueprint("indian_house");
        indianHouse.setDisplayName("Indian House");
        indianHouse.setCulture(Culture.INDIAN);
        indianHouse.setType(BuildingType.HOUSE);
        indianHouse.setDimensions(5, 5, 4);
        indianHouse.setBuildPriority(10);
        indianHouse.setVillagerCapacity(2);
        indianHouse.generateSimpleHouse(5, 5, 4, Blocks.TERRACOTTA, Blocks.TERRACOTTA, Blocks.TERRACOTTA);
        indianHouse.addResource(Items.TERRACOTTA, 36);
        register(indianHouse);

        // 制砖坊
        BuildingBlueprint brickMaker = new BuildingBlueprint("indian_brick_maker");
        brickMaker.setDisplayName("Brick Workshop");
        brickMaker.setCulture(Culture.INDIAN);
        brickMaker.setType(BuildingType.FORGE);
        brickMaker.setDimensions(6, 6, 4);
        brickMaker.setBuildPriority(30);
        brickMaker.setVillagerCapacity(2);
        brickMaker.setRequiredProfession(VillagerProfession.BRICK_MAKER);
        brickMaker.generateSimpleHouse(6, 6, 4, Blocks.BRICKS, Blocks.BRICKS, Blocks.BRICKS);
        brickMaker.addResource(Items.BRICK, 48);
        brickMaker.addResource(Items.CLAY_BALL, 16);
        register(brickMaker);

        // 神庙
        BuildingBlueprint hinduTemple = new BuildingBlueprint("indian_temple");
        hinduTemple.setDisplayName("Hindu Temple");
        hinduTemple.setCulture(Culture.INDIAN);
        hinduTemple.setType(BuildingType.TEMPLE);
        hinduTemple.setDimensions(9, 9, 10);
        hinduTemple.setBuildPriority(50);
        hinduTemple.setMinVillageLevel(2);
        hinduTemple.setVillagerCapacity(1);
        hinduTemple.setRequiredProfession(VillagerProfession.SADHU);
        hinduTemple.generateSimpleHouse(9, 9, 10, Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.SANDSTONE);
        hinduTemple.addResource(Items.SANDSTONE, 96);
        hinduTemple.addResource(Items.GOLD_INGOT, 4);
        register(hinduTemple);
    }

    // ================ 玛雅建筑 ================

    private void registerMayanBuildings() {
        // 玛雅金字塔（市政厅）
        BuildingBlueprint mayanPyramid = new BuildingBlueprint("mayan_town_hall");
        mayanPyramid.setDisplayName("Mayan Pyramid");
        mayanPyramid.setCulture(Culture.MAYAN);
        mayanPyramid.setType(BuildingType.TOWN_HALL);
        mayanPyramid.setDimensions(13, 13, 10);
        mayanPyramid.setBuildPriority(0);
        mayanPyramid.setVillagerCapacity(4);
        mayanPyramid.generateSimpleHouse(13, 13, 10, Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
        mayanPyramid.addResource(Items.COBBLESTONE, 128);
        mayanPyramid.addResource(Items.MOSSY_COBBLESTONE, 48);
        register(mayanPyramid);

        // 玛雅小屋
        BuildingBlueprint mayanHut = new BuildingBlueprint("mayan_hut");
        mayanHut.setDisplayName("Mayan Hut");
        mayanHut.setCulture(Culture.MAYAN);
        mayanHut.setType(BuildingType.HOUSE);
        mayanHut.setDimensions(5, 5, 4);
        mayanHut.setBuildPriority(10);
        mayanHut.setVillagerCapacity(2);
        mayanHut.generateSimpleHouse(5, 5, 4, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_PLANKS);
        mayanHut.addResource(Items.JUNGLE_PLANKS, 28);
        mayanHut.addResource(Items.JUNGLE_LOG, 8);
        register(mayanHut);

        // 祭坛
        BuildingBlueprint altar = new BuildingBlueprint("mayan_altar");
        altar.setDisplayName("Sacrificial Altar");
        altar.setCulture(Culture.MAYAN);
        altar.setType(BuildingType.TEMPLE);
        altar.setDimensions(7, 7, 5);
        altar.setBuildPriority(50);
        altar.setMinVillageLevel(2);
        altar.setVillagerCapacity(1);
        altar.setRequiredProfession(VillagerProfession.SHAMAN);
        altar.generateSimpleHouse(7, 7, 5, Blocks.MOSSY_COBBLESTONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE);
        altar.addResource(Items.COBBLESTONE, 48);
        altar.addResource(Items.OBSIDIAN, 4);
        register(altar);
    }

    // ================ 拜占庭建筑 ================

    private void registerByzantineBuildings() {
        // 拜占庭总督府
        BuildingBlueprint byzantineTownHall = new BuildingBlueprint("byzantine_town_hall");
        byzantineTownHall.setDisplayName("Governor's Palace");
        byzantineTownHall.setCulture(Culture.BYZANTINE);
        byzantineTownHall.setType(BuildingType.TOWN_HALL);
        byzantineTownHall.setDimensions(11, 11, 7);
        byzantineTownHall.setBuildPriority(0);
        byzantineTownHall.setVillagerCapacity(4);
        byzantineTownHall.generateSimpleHouse(11, 11, 7, Blocks.STONE_BRICKS, Blocks.POLISHED_GRANITE, Blocks.STONE_BRICKS);
        byzantineTownHall.addResource(Items.STONE_BRICKS, 96);
        byzantineTownHall.addResource(Items.POLISHED_GRANITE, 32);
        register(byzantineTownHall);

        // 拜占庭民居
        BuildingBlueprint byzantineHouse = new BuildingBlueprint("byzantine_house");
        byzantineHouse.setDisplayName("Byzantine House");
        byzantineHouse.setCulture(Culture.BYZANTINE);
        byzantineHouse.setType(BuildingType.HOUSE);
        byzantineHouse.setDimensions(6, 6, 5);
        byzantineHouse.setBuildPriority(10);
        byzantineHouse.setVillagerCapacity(2);
        byzantineHouse.generateSimpleHouse(6, 6, 5, Blocks.WHITE_TERRACOTTA, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA);
        byzantineHouse.addResource(Items.WHITE_TERRACOTTA, 36);
        byzantineHouse.addResource(Items.TERRACOTTA, 12);
        register(byzantineHouse);

        // 东正教教堂
        BuildingBlueprint orthodoxChurch = new BuildingBlueprint("byzantine_church");
        orthodoxChurch.setDisplayName("Orthodox Church");
        orthodoxChurch.setCulture(Culture.BYZANTINE);
        orthodoxChurch.setType(BuildingType.TEMPLE);
        orthodoxChurch.setDimensions(10, 14, 12);
        orthodoxChurch.setBuildPriority(50);
        orthodoxChurch.setMinVillageLevel(2);
        orthodoxChurch.setVillagerCapacity(1);
        orthodoxChurch.setRequiredProfession(VillagerProfession.ORTHODOX_PRIEST);
        orthodoxChurch.generateSimpleHouse(10, 14, 12, Blocks.STONE_BRICKS, Blocks.POLISHED_GRANITE, Blocks.STONE_BRICKS);
        orthodoxChurch.addResource(Items.STONE_BRICKS, 128);
        orthodoxChurch.addResource(Items.GOLD_BLOCK, 2);
        orthodoxChurch.addResource(Items.LAPIS_LAZULI, 16);
        register(orthodoxChurch);

        // 蚕房
        BuildingBlueprint silkFarm = new BuildingBlueprint("byzantine_silk_farm");
        silkFarm.setDisplayName("Silk Farm");
        silkFarm.setCulture(Culture.BYZANTINE);
        silkFarm.setType(BuildingType.FARM);
        silkFarm.setDimensions(8, 6, 4);
        silkFarm.setBuildPriority(25);
        silkFarm.setVillagerCapacity(2);
        silkFarm.setRequiredProfession(VillagerProfession.SILK_FARMER);
        silkFarm.generateSimpleHouse(8, 6, 4, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS, Blocks.OAK_PLANKS);
        silkFarm.addResource(Items.OAK_PLANKS, 36);
        silkFarm.addResource(Items.OAK_LEAVES, 24);
        register(silkFarm);
    }

    // ================ 查询 ================

    /**
     * 获取蓝图
     */
    @Nullable
    public BuildingBlueprint getBlueprint(String key) {
        return blueprints.get(key);
    }

    /**
     * 获取文化的所有蓝图
     */
    public List<BuildingBlueprint> getBlueprintsForCulture(Culture culture) {
        return Collections.unmodifiableList(blueprintsByCulture.getOrDefault(culture, Collections.emptyList()));
    }

    /**
     * 获取指定类型的所有蓝图
     */
    public List<BuildingBlueprint> getBlueprintsOfType(BuildingType type) {
        return Collections.unmodifiableList(blueprintsByType.getOrDefault(type, Collections.emptyList()));
    }

    /**
     * 获取文化的可建造蓝图（按优先级排序）
     */
    public List<BuildingBlueprint> getAvailableBlueprints(Culture culture, int villageLevel) {
        List<BuildingBlueprint> available = new ArrayList<>();

        for (BuildingBlueprint blueprint : blueprintsByCulture.get(culture)) {
            if (blueprint.getMinVillageLevel() <= villageLevel) {
                available.add(blueprint);
            }
        }

        available.sort(Comparator.comparingInt(BuildingBlueprint::getBuildPriority));
        return available;
    }

    /**
     * 获取所有蓝图键
     */
    public Set<String> getAllBlueprintKeys() {
        return Collections.unmodifiableSet(blueprints.keySet());
    }

    /**
     * 检查蓝图是否存在
     */
    public boolean hasBlueprint(String key) {
        return blueprints.containsKey(key);
    }
}
