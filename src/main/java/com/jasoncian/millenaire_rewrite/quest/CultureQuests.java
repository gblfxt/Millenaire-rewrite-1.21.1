package com.jasoncian.millenaire_rewrite.quest;

import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 文化任务定义 - 为每种文化生成特色任务
 *
 * 每种文化有独特的：
 * - 收集任务物品
 * - 建造材料需求
 * - 特殊任务类型
 *
 * @author Based on OldSource culture quests
 * @version 1.0.0
 */
public class CultureQuests {

    private static final Random random = new Random();

    // ================ 物品定义 ================

    /** 诺曼文化物品 */
    private static final Item[] NORMAN_GATHER_ITEMS = {
        Items.WHEAT, Items.OAK_LOG, Items.COBBLESTONE, Items.IRON_INGOT,
        Items.APPLE, Items.BREAD, Items.LEATHER, Items.WHITE_WOOL
    };

    private static final Item[] NORMAN_BUILD_ITEMS = {
        Items.OAK_PLANKS, Items.COBBLESTONE, Items.STONE_BRICKS, Items.OAK_LOG
    };

    /** 日本文化物品 */
    private static final Item[] JAPANESE_GATHER_ITEMS = {
        Items.BAMBOO, Items.CHERRY_LEAVES, Items.PAPER, Items.CHERRY_LOG,
        Items.SALMON, Items.COD, Items.SUGAR_CANE, Items.INK_SAC
    };

    private static final Item[] JAPANESE_BUILD_ITEMS = {
        Items.BAMBOO_PLANKS, Items.CHERRY_PLANKS, Items.PAPER, Items.BAMBOO_BLOCK
    };

    /** 印度文化物品 */
    private static final Item[] INDIAN_GATHER_ITEMS = {
        Items.SUGAR_CANE, Items.COCOA_BEANS, Items.GOLD_INGOT, Items.LAPIS_LAZULI,
        Items.BLUE_DYE, Items.RED_DYE, Items.YELLOW_DYE, Items.EMERALD
    };

    private static final Item[] INDIAN_BUILD_ITEMS = {
        Items.SANDSTONE, Items.RED_SANDSTONE, Items.TERRACOTTA, Items.GOLD_BLOCK
    };

    /** 玛雅文化物品 */
    private static final Item[] MAYAN_GATHER_ITEMS = {
        Items.JUNGLE_LOG, Items.COCOA_BEANS, Items.GOLD_INGOT, Items.FEATHER,
        Items.BONE, Items.OBSIDIAN, Items.MELON_SLICE, Items.PUMPKIN
    };

    private static final Item[] MAYAN_BUILD_ITEMS = {
        Items.MOSSY_COBBLESTONE, Items.JUNGLE_PLANKS, Items.CHISELED_STONE_BRICKS, Items.OBSIDIAN
    };

    /** 拜占庭文化物品 */
    private static final Item[] BYZANTINE_GATHER_ITEMS = {
        Items.GOLD_INGOT, Items.EMERALD, Items.PURPLE_DYE, Items.AMETHYST_SHARD,
        Items.COPPER_INGOT, Items.IRON_INGOT, Items.REDSTONE, Items.QUARTZ
    };

    private static final Item[] BYZANTINE_BUILD_ITEMS = {
        Items.POLISHED_DEEPSLATE, Items.GOLD_BLOCK, Items.QUARTZ_BLOCK, Items.COPPER_BLOCK
    };

    /** 因纽特文化物品 */
    private static final Item[] INUIT_GATHER_ITEMS = {
        Items.COD, Items.SALMON, Items.SNOW_BLOCK, Items.ICE,
        Items.LEATHER, Items.BONE, Items.BLAZE_POWDER, Items.COAL
    };

    private static final Item[] INUIT_BUILD_ITEMS = {
        Items.PACKED_ICE, Items.SNOW_BLOCK, Items.SPRUCE_PLANKS, Items.BONE_BLOCK
    };

    /** 塞尔柱文化物品 */
    private static final Item[] SELJUK_GATHER_ITEMS = {
        Items.SAND, Items.SANDSTONE, Items.CACTUS, Items.DEAD_BUSH,
        Items.GOLD_INGOT, Items.IRON_INGOT, Items.LEATHER, Items.STRING
    };

    private static final Item[] SELJUK_BUILD_ITEMS = {
        Items.SANDSTONE, Items.CUT_SANDSTONE, Items.TERRACOTTA, Items.BLUE_TERRACOTTA
    };

    // ================ 生物定义 ================

    private static final String[] COMMON_HOSTILE_MOBS = {
        "minecraft:zombie", "minecraft:skeleton", "minecraft:spider", "minecraft:creeper"
    };

    private static final String[] NETHER_MOBS = {
        "minecraft:blaze", "minecraft:wither_skeleton", "minecraft:piglin"
    };

    private static final String[] WATER_MOBS = {
        "minecraft:drowned", "minecraft:guardian"
    };

    // ================ 任务生成 ================

    /**
     * 为村庄生成随机任务
     */
    @Nullable
    public static Quest generateRandomQuest(Culture culture, Village village) {
        // 根据文化选择任务类型权重
        QuestType type = selectQuestType(culture);

        return switch (type) {
            case GATHER -> generateGatherQuest(culture, village);
            case HARVEST -> generateHarvestQuest(culture, village);
            case HUNT -> generateHuntQuest(culture, village);
            case DELIVER -> generateDeliverQuest(culture, village);
            case CONSTRUCTION -> generateConstructionQuest(culture, village);
            case EXPLORE -> generateExploreQuest(culture, village);
            case DEFEND -> generateDefendQuest(culture, village);
            case CLEAR -> generateClearQuest(culture, village);
            default -> generateGatherQuest(culture, village); // 默认收集任务
        };
    }

    /**
     * 选择任务类型（带文化权重）
     */
    private static QuestType selectQuestType(Culture culture) {
        // 基础权重
        Map<QuestType, Integer> weights = new HashMap<>();
        weights.put(QuestType.GATHER, 30);
        weights.put(QuestType.HARVEST, 20);
        weights.put(QuestType.HUNT, 15);
        weights.put(QuestType.DELIVER, 10);
        weights.put(QuestType.CONSTRUCTION, 15);
        weights.put(QuestType.EXPLORE, 5);
        weights.put(QuestType.DEFEND, 3);
        weights.put(QuestType.CLEAR, 2);

        // 文化调整
        switch (culture) {
            case NORMAN -> {
                weights.put(QuestType.CONSTRUCTION, 25);
                weights.put(QuestType.DEFEND, 8);
            }
            case JAPANESE -> {
                weights.put(QuestType.HARVEST, 30);
                weights.put(QuestType.EXPLORE, 10);
            }
            case INDIAN -> {
                weights.put(QuestType.DELIVER, 20);
                weights.put(QuestType.GATHER, 35);
            }
            case MAYAN -> {
                weights.put(QuestType.HUNT, 25);
                weights.put(QuestType.EXPLORE, 15);
            }
            case BYZANTINE -> {
                weights.put(QuestType.DELIVER, 25);
                weights.put(QuestType.CONSTRUCTION, 20);
            }
            case INUIT -> {
                weights.put(QuestType.HUNT, 30);
                weights.put(QuestType.GATHER, 25);
            }
            case SELJUK -> {
                weights.put(QuestType.DELIVER, 25);
                weights.put(QuestType.CLEAR, 10);
            }
        }

        // 加权随机选择
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();
        int roll = random.nextInt(totalWeight);

        int cumulative = 0;
        for (var entry : weights.entrySet()) {
            cumulative += entry.getValue();
            if (roll < cumulative) {
                return entry.getKey();
            }
        }

        return QuestType.GATHER;
    }

    // ================ 具体任务生成 ================

    /**
     * 生成收集任务
     */
    private static Quest generateGatherQuest(Culture culture, Village village) {
        Item[] items = getGatherItems(culture);
        Item targetItem = items[random.nextInt(items.length)];
        int amount = 16 + random.nextInt(48); // 16-64

        Quest quest = new Quest(QuestType.GATHER);
        quest.setTitle("Gather " + formatItemName(targetItem));
        quest.setDescription("The village needs " + amount + " " + formatItemName(targetItem) +
            " for daily needs. Please gather and bring them back.");

        quest.addObjective(QuestObjective.collectItem(targetItem, amount));

        // 奖励
        quest.setDenierReward(amount * 2);
        quest.setReputationReward(5 + amount / 16);

        // 物品奖励（随机）
        if (random.nextFloat() < 0.3f) {
            quest.addItemReward(Items.EMERALD, 1 + random.nextInt(3));
        }

        return quest;
    }

    /**
     * 生成采集任务
     */
    private static Quest generateHarvestQuest(Culture culture, Village village) {
        // 农作物采集
        Item[] crops = { Items.WHEAT, Items.CARROT, Items.POTATO, Items.BEETROOT };
        Item targetCrop = crops[random.nextInt(crops.length)];
        int amount = 32 + random.nextInt(64);

        Quest quest = new Quest(QuestType.HARVEST);
        quest.setTitle("Harvest " + formatItemName(targetCrop));
        quest.setDescription("Our farms need more " + formatItemName(targetCrop) +
            ". Please harvest " + amount + " for the village stores.");

        quest.addObjective(QuestObjective.collectItem(targetCrop, amount));

        quest.setDenierReward(amount);
        quest.setReputationReward(5);

        return quest;
    }

    /**
     * 生成狩猎任务
     */
    private static Quest generateHuntQuest(Culture culture, Village village) {
        String entityType = COMMON_HOSTILE_MOBS[random.nextInt(COMMON_HOSTILE_MOBS.length)];
        int amount = 5 + random.nextInt(15); // 5-20

        Quest quest = new Quest(QuestType.HUNT);
        quest.setTitle("Hunt " + formatEntityName(entityType));
        quest.setDescription("Dangerous creatures threaten our village. Please eliminate " +
            amount + " " + formatEntityName(entityType) + " to protect us.");

        quest.addObjective(QuestObjective.killEntity(entityType, amount));

        quest.setDenierReward(amount * 10);
        quest.setReputationReward(10 + amount / 2);

        // 战斗奖励
        if (random.nextFloat() < 0.4f) {
            quest.addItemReward(Items.IRON_SWORD, 1);
        }

        return quest;
    }

    /**
     * 生成交付任务
     */
    private static Quest generateDeliverQuest(Culture culture, Village village) {
        Item[] items = getGatherItems(culture);
        Item targetItem = items[random.nextInt(items.length)];
        int amount = 8 + random.nextInt(24);

        Quest quest = new Quest(QuestType.DELIVER);
        quest.setTitle("Deliver " + formatItemName(targetItem));
        quest.setDescription("Please deliver " + amount + " " + formatItemName(targetItem) +
            " to a neighboring village as part of our trade agreement.");

        quest.addObjective(QuestObjective.deliverItem(targetItem, amount, null));

        quest.setDenierReward(amount * 3);
        quest.setReputationReward(8);

        // 设置时间限制（2游戏天）
        quest.setTimeLimit(48000);

        return quest;
    }

    /**
     * 生成建造任务
     */
    private static Quest generateConstructionQuest(Culture culture, Village village) {
        Item[] materials = getBuildItems(culture);
        Item targetMaterial = materials[random.nextInt(materials.length)];
        int amount = 32 + random.nextInt(96); // 32-128

        Quest quest = new Quest(QuestType.CONSTRUCTION);
        quest.setTitle("Construction Materials");
        quest.setDescription("We are building a new structure and need " + amount +
            " " + formatItemName(targetMaterial) + ". Your help would be appreciated.");

        quest.addObjective(QuestObjective.build("building", targetMaterial, amount));

        quest.setDenierReward(amount * 2);
        quest.setReputationReward(12);

        // 建筑奖励
        if (random.nextFloat() < 0.2f) {
            quest.addItemReward(Items.DIAMOND, 1);
        }

        return quest;
    }

    /**
     * 生成探索任务
     */
    private static Quest generateExploreQuest(Culture culture, Village village) {
        Quest quest = new Quest(QuestType.EXPLORE);
        quest.setTitle("Explore the Wilderness");
        quest.setDescription("Scout the surrounding area and report back. " +
            "Travel far from the village to discover new lands.");

        // 目标位置（村庄中心偏移500-1000格）
        int offsetX = (random.nextBoolean() ? 1 : -1) * (500 + random.nextInt(500));
        int offsetZ = (random.nextBoolean() ? 1 : -1) * (500 + random.nextInt(500));

        if (village.getCenterPos() != null) {
            var targetPos = village.getCenterPos().offset(offsetX, 0, offsetZ);
            quest.setTargetPos(targetPos);
            quest.addObjective(QuestObjective.reachLocation(targetPos, "Exploration Point"));
        }

        quest.setDenierReward(100);
        quest.setReputationReward(8);

        // 探索奖励
        quest.addItemReward(Items.MAP, 1);

        return quest;
    }

    /**
     * 生成防御任务
     */
    private static Quest generateDefendQuest(Culture culture, Village village) {
        Quest quest = new Quest(QuestType.DEFEND);
        quest.setTitle("Defend the Village");
        quest.setDescription("Our scouts have spotted hostile creatures approaching! " +
            "Help defend the village from the incoming threat.");

        // 需要击杀多种敌人
        quest.addObjective(QuestObjective.killEntity("minecraft:zombie", 5));
        quest.addObjective(QuestObjective.killEntity("minecraft:skeleton", 5));

        quest.setDenierReward(200);
        quest.setReputationReward(25);
        quest.setMinimumReputation(50); // 需要一定声望才能接

        // 战斗奖励
        quest.addItemReward(Items.IRON_CHESTPLATE, 1);

        return quest;
    }

    /**
     * 生成清剿任务
     */
    private static Quest generateClearQuest(Culture culture, Village village) {
        String entityType = COMMON_HOSTILE_MOBS[random.nextInt(COMMON_HOSTILE_MOBS.length)];
        int amount = 10 + random.nextInt(20);

        Quest quest = new Quest(QuestType.CLEAR);
        quest.setTitle("Clear the Threat");
        quest.setDescription("A dangerous nest of " + formatEntityName(entityType) +
            " has been discovered nearby. Clear them out to protect our village.");

        quest.addObjective(QuestObjective.killEntity(entityType, amount));

        quest.setDenierReward(amount * 15);
        quest.setReputationReward(20);
        quest.setMinimumReputation(25);

        return quest;
    }

    // ================ 工具方法 ================

    /**
     * 获取文化的收集物品
     */
    private static Item[] getGatherItems(Culture culture) {
        return switch (culture) {
            case NORMAN -> NORMAN_GATHER_ITEMS;
            case JAPANESE -> JAPANESE_GATHER_ITEMS;
            case INDIAN -> INDIAN_GATHER_ITEMS;
            case MAYAN -> MAYAN_GATHER_ITEMS;
            case BYZANTINE -> BYZANTINE_GATHER_ITEMS;
            case INUIT -> INUIT_GATHER_ITEMS;
            case SELJUK -> SELJUK_GATHER_ITEMS;
        };
    }

    /**
     * 获取文化的建造物品
     */
    private static Item[] getBuildItems(Culture culture) {
        return switch (culture) {
            case NORMAN -> NORMAN_BUILD_ITEMS;
            case JAPANESE -> JAPANESE_BUILD_ITEMS;
            case INDIAN -> INDIAN_BUILD_ITEMS;
            case MAYAN -> MAYAN_BUILD_ITEMS;
            case BYZANTINE -> BYZANTINE_BUILD_ITEMS;
            case INUIT -> INUIT_BUILD_ITEMS;
            case SELJUK -> SELJUK_BUILD_ITEMS;
        };
    }

    /**
     * 格式化物品名称
     */
    private static String formatItemName(Item item) {
        String name = item.toString();
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        return name.replace("_", " ");
    }

    /**
     * 格式化生物名称
     */
    private static String formatEntityName(String entityType) {
        String name = entityType;
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        name = name.replace("_", " ");
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name + "s";
    }
}
