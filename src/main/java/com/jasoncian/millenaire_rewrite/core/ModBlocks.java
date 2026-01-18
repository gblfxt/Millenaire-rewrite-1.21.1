package com.jasoncian.millenaire_rewrite.core;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.blocks.agriculture.FruitLeavesBlock;
import com.jasoncian.millenaire_rewrite.blocks.agriculture.GrapeVineBlock;
import com.jasoncian.millenaire_rewrite.blocks.agriculture.MillCropBlock;
import com.jasoncian.millenaire_rewrite.blocks.agriculture.SilkWormBlock;
import com.jasoncian.millenaire_rewrite.blocks.functional.FirePitBlock;
import com.jasoncian.millenaire_rewrite.blocks.functional.ImportTableBlock;
import com.jasoncian.millenaire_rewrite.blocks.functional.LockedChestBlock;
import com.jasoncian.millenaire_rewrite.blocks.functional.TownHallBlock;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.blocks.system.BuildingBlockRegistry;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * 方块注册器 - 管理所有Millenaire mod方块的注册
 *
 * 负责注册所有模组方块，按功能分类组织
 * 采用新的统一建筑方块系统，大幅简化注册流程
 *
 * 功能特性：
 * - 核心功能方块（村庄石等）
 * - 统一建筑方块系统（自动生成所有变体）
 * - 装饰性方块系统
 * - 文化特色建筑方块
 * - 功能性建筑方块
 *
 * 系统优势：
 * - 自动化：一次注册生成完整方块族系（方块+楼梯+台阶+墙）
 * - 统一性：所有建筑方块使用相同的属性系统
 * - 扩展性：易于添加新材料和文化
 * - 高效性：减少重复代码，提高开发效率
 *
 * @author JasonCian
 * @version 2.0.0-stable
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK,
            MillenaireRewrite.MOD_ID);

    // ================ 核心功能方块 ================

    /** 火坑方块 - 多槽位烹饪 */
    public static final DeferredHolder<Block, FirePitBlock> FIRE_PIT =
        BLOCKS.register("fire_pit", () -> new FirePitBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(0.2F)
                .sound(SoundType.WOOD)
                .noOcclusion()
        ));

    /** 锁定箱子方块 - 村庄存储 */
    public static final DeferredHolder<Block, LockedChestBlock> LOCKED_CHEST =
        BLOCKS.register("locked_chest", () -> new LockedChestBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion()
        ));

    /** 导入桌方块 - 建筑模板导入导出 */
    public static final DeferredHolder<Block, ImportTableBlock> IMPORT_TABLE =
        BLOCKS.register("import_table", () -> new ImportTableBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.0F)
                .sound(SoundType.WOOD)
        ));

    // ================ 村庄核心方块 ================

    /** 诺曼市政厅 - 诺曼文化村庄中心 */
    public static final DeferredHolder<Block, TownHallBlock> TOWN_HALL_NORMAN =
        BLOCKS.register("town_hall_norman", () -> new TownHallBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops(),
            Culture.NORMAN
        ));

    /** 日本市政厅 */
    public static final DeferredHolder<Block, TownHallBlock> TOWN_HALL_JAPANESE =
        BLOCKS.register("town_hall_japanese", () -> new TownHallBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(3.0F, 6.0F)
                .sound(SoundType.WOOD)
                .requiresCorrectToolForDrops(),
            Culture.JAPANESE
        ));

    /** 印度市政厅 */
    public static final DeferredHolder<Block, TownHallBlock> TOWN_HALL_INDIAN =
        BLOCKS.register("town_hall_indian", () -> new TownHallBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_ORANGE)
                .strength(3.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops(),
            Culture.INDIAN
        ));

    /** 玛雅市政厅 */
    public static final DeferredHolder<Block, TownHallBlock> TOWN_HALL_MAYAN =
        BLOCKS.register("town_hall_mayan", () -> new TownHallBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops(),
            Culture.MAYAN
        ));

    /** 拜占庭市政厅 */
    public static final DeferredHolder<Block, TownHallBlock> TOWN_HALL_BYZANTINE =
        BLOCKS.register("town_hall_byzantine", () -> new TownHallBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.GOLD)
                .strength(3.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops(),
            Culture.BYZANTINE
        ));

    // ================ 农业方块 ================

    /** 稻米作物 - 需要灌溉 */
    public static final DeferredHolder<Block, MillCropBlock> CROP_RICE =
        BLOCKS.register("crop_rice", () -> new MillCropBlock(
            cropProperties(),
            () -> ModItems.RICE.get(),
            true, false));

    /** 姜黄作物 - 印度文化 */
    public static final DeferredHolder<Block, MillCropBlock> CROP_TURMERIC =
        BLOCKS.register("crop_turmeric", () -> new MillCropBlock(
            cropProperties(),
            () -> ModItems.TURMERIC.get(),
            false, false));

    /** 玉米作物 - 玛雅文化，慢速生长 */
    public static final DeferredHolder<Block, MillCropBlock> CROP_MAIZE =
        BLOCKS.register("crop_maize", () -> new MillCropBlock(
            cropProperties(),
            () -> ModItems.MAIZE.get(),
            false, true));

    /** 棉花作物 - 塞尔柱文化，需要灌溉 */
    public static final DeferredHolder<Block, MillCropBlock> CROP_COTTON =
        BLOCKS.register("crop_cotton", () -> new MillCropBlock(
            cropProperties(),
            () -> ModItems.COTTON.get(),
            true, false));

    /** 葡萄藤 - 拜占庭文化，双层高度 */
    public static final DeferredHolder<Block, GrapeVineBlock> CROP_VINE =
        BLOCKS.register("crop_vine", () -> new GrapeVineBlock(
            cropProperties(),
            () -> ModItems.GRAPES.get(),
            () -> ModItems.GRAPES.get()));

    /** 蚕室方块 - 日本文化，丝绸生产 */
    public static final DeferredHolder<Block, SilkWormBlock> SILKWORM =
        BLOCKS.register("silkworm", () -> new SilkWormBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 5.0F)
                .sound(SoundType.WOOD)
                .noOcclusion(),
            () -> ModItems.SILK.get()));

    /** 苹果树叶 - 诺曼文化 */
    public static final DeferredHolder<Block, FruitLeavesBlock> LEAVES_APPLETREE =
        BLOCKS.register("leaves_appletree", () -> new FruitLeavesBlock(
            fruitLeavesProperties(),
            () -> ModItems.CIDER_APPLE.get(),
            () -> net.minecraft.world.item.Items.OAK_SAPLING));

    /** 橄榄树叶 - 拜占庭文化 */
    public static final DeferredHolder<Block, FruitLeavesBlock> LEAVES_OLIVETREE =
        BLOCKS.register("leaves_olivetree", () -> new FruitLeavesBlock(
            fruitLeavesProperties(),
            () -> ModItems.OLIVES.get(),
            () -> net.minecraft.world.item.Items.OAK_SAPLING));

    /** 开心果树叶 - 塞尔柱文化 */
    public static final DeferredHolder<Block, FruitLeavesBlock> LEAVES_PISTACHIO =
        BLOCKS.register("leaves_pistachio", () -> new FruitLeavesBlock(
            fruitLeavesProperties(),
            () -> ModItems.PISTACHIOS.get(),
            () -> net.minecraft.world.item.Items.OAK_SAPLING));

    /** 樱桃树叶 - 日本文化 */
    public static final DeferredHolder<Block, FruitLeavesBlock> CHERRY_LEAVES =
        BLOCKS.register("cherry_leaves", () -> new FruitLeavesBlock(
            fruitLeavesProperties(),
            () -> ModItems.CHERRIES.get(),
            () -> net.minecraft.world.item.Items.CHERRY_SAPLING));

    /** 樱花树叶 - 日本文化 */
    public static final DeferredHolder<Block, FruitLeavesBlock> SAKURA_LEAVES =
        BLOCKS.register("sakura_leaves", () -> new FruitLeavesBlock(
            fruitLeavesProperties(),
            () -> ModItems.CHERRY_BLOSSOM.get(),
            () -> net.minecraft.world.item.Items.CHERRY_SAPLING));

    // ================ Building Material Blocks ================

    // Indian Building Blocks
    /** Mud Brick Block - Indian building material */
    public static final DeferredHolder<Block, Block> MUD_BRICK_BLOCK =
        BLOCKS.register("mud_brick_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_BROWN)
                .strength(1.5F)
                .sound(SoundType.MUD_BRICKS)
                .requiresCorrectToolForDrops()
        ));

    /** Cooked Brick Block - Fired Indian building material */
    public static final DeferredHolder<Block, Block> COOKED_BRICK_BLOCK =
        BLOCKS.register("cooked_brick_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_RED)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        ));

    // Japanese Building Blocks
    /** Thatch Block - Japanese roofing */
    public static final DeferredHolder<Block, Block> THATCH_BLOCK =
        BLOCKS.register("thatch_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .strength(0.5F)
                .sound(SoundType.GRASS)
                .ignitedByLava()
        ));

    /** Paper Wall Block - Japanese shoji screen */
    public static final DeferredHolder<Block, Block> PAPER_WALL_BLOCK =
        BLOCKS.register("paper_wall_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(0.3F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .ignitedByLava()
        ));

    // Norman Building Blocks
    /** Timber Frame Block - Norman half-timbered construction */
    public static final DeferredHolder<Block, Block> TIMBER_FRAME_BLOCK =
        BLOCKS.register("timber_frame_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava()
        ));

    /** Wattle and Daub Block - Norman wall material */
    public static final DeferredHolder<Block, Block> WATTLE_DAUB_BLOCK =
        BLOCKS.register("wattle_daub_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(1.0F)
                .sound(SoundType.GRAVEL)
        ));

    /** Plaster Block - Finished wall material */
    public static final DeferredHolder<Block, Block> PLASTER_BLOCK =
        BLOCKS.register("plaster_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(1.0F)
                .sound(SoundType.CALCITE)
        ));

    // Byzantine Building Blocks
    /** Byzantine Tile Block - Decorative Byzantine tile */
    public static final DeferredHolder<Block, Block> BYZANTINE_TILE_BLOCK =
        BLOCKS.register("byzantine_tile_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_ORANGE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        ));

    /** Marble Block - Byzantine marble */
    public static final DeferredHolder<Block, Block> MARBLE_BLOCK =
        BLOCKS.register("marble_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.QUARTZ)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        ));

    // Mayan Building Blocks
    /** Limestone Block - Mayan building stone */
    public static final DeferredHolder<Block, Block> LIMESTONE_BLOCK =
        BLOCKS.register("limestone_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        ));

    // Seljuk Building Blocks
    /** Glazed Tile Block - Decorative Seljuk tile */
    public static final DeferredHolder<Block, Block> GLAZED_TILE_BLOCK =
        BLOCKS.register("glazed_tile_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLUE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        ));

    /** Carved Stone Block - Ornamental Seljuk stonework */
    public static final DeferredHolder<Block, Block> CARVED_STONE_BLOCK =
        BLOCKS.register("carved_stone_block", () -> new Block(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        ));

    // ================ Quest Related Blocks ================

    /** Galianite Ore - Rare ore found deep underground, used in Creation Quest */
    public static final DeferredHolder<Block, Block> GALIANITE_ORE =
        BLOCKS.register("galianite_ore", () -> new net.minecraft.world.level.block.DropExperienceBlock(
            net.minecraft.util.valueproviders.UniformInt.of(3, 7),
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F, 3.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 3) // Slight glow
        ));

    /** Deepslate Galianite Ore - Deepslate variant found in deeper layers */
    public static final DeferredHolder<Block, Block> DEEPSLATE_GALIANITE_ORE =
        BLOCKS.register("deepslate_galianite_ore", () -> new net.minecraft.world.level.block.DropExperienceBlock(
            net.minecraft.util.valueproviders.UniformInt.of(3, 7),
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.DEEPSLATE)
                .strength(4.5F, 3.0F)
                .sound(SoundType.DEEPSLATE)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> 3) // Slight glow
        ));

    // ================ 方块属性辅助方法 ================

    /**
     * 作物方块属性
     */
    private static BlockBehaviour.Properties cropProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .noCollission()
            .randomTicks()
            .instabreak()
            .sound(SoundType.CROP)
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }

    /**
     * 水果树叶属性
     */
    private static BlockBehaviour.Properties fruitLeavesProperties() {
        return BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.2F)
            .randomTicks()
            .sound(SoundType.GRASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> type == net.minecraft.world.entity.EntityType.OCELOT ||
                type == net.minecraft.world.entity.EntityType.PARROT)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false)
            .ignitedByLava()
            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            .isRedstoneConductor((state, level, pos) -> false);
    }

    // ================ 统一建筑方块系统（已整合装饰方块） ================

    /*
     * 注意：所有基础建筑方块和装饰方块现在通过BuildingBlockRegistry自动注册
     * 
     * 这个新系统将自动为每种材料生成完整的方块族系：
     * - 基础方块 (例如: stone)
     * - 楼梯方块 (例如: stone_stairs)
     * - 台阶方块 (例如: stone_slab)
     * - 墙方块 (例如: stone_wall)
     * 
     * 支持的材料包括：
     * 
     * 【基础建筑材料】
     * - 石质系列：stone, cobblestone, stone_bricks等
     * - 砂岩系列：sandstone, red_sandstone等
     * - 泥砖系列：mud_bricks, dried_bricks等
     * - 瓦片系列：byzantine_tiles, gray_tiles等
     * - 木质系列：timber_frame, thatch
     * - 特殊系列：ice_bricks, snow_bricks
     * 
     * 【装饰材料（已整合）】
     * - 石质装饰：gold_ornament（金装饰）, galianite_block（Galianite方块）, cooked_bricks（烧制砖）
     * - 木质装饰：simple_timber_frame（简朴木框架）, cross_timber_frame（十字木框架）
     * - 土质装饰：earth_wall（土墙）, dried_bricks（风干砖）
     * 
     * 文化特色分配：
     * - BASIC: 所有基础建筑材料
     * - NORMAN: 石材系列 + 石质装饰 + 木质装饰
     * - BYZANTINE: 拜占庭瓦片 + 金装饰
     * - INDIAN: 红瓦片 + 赭石瓦片 + 土质装饰
     * - JAPANESE: 木框架 + 木质装饰
     * - MAYAN: 石材系列 + Galianite方块
     * - INUIT: 冰砖 + 雪砖
     * 
     * 访问方式：
     * BuildingBlockRegistry.getBlock(material, culture, variant)
     * 
     * 迁移说明：
     * - 原DecorativeStoneBlock的三个变体已拆分为独立材料
     * - 原DecorativeWoodBlock的变体已整合为独立木质材料
     * - 原DecorativeEarthBlock的变体已整合为独立土质材料
     * - 所有装饰方块现在支持完整的变体系统（楼梯、台阶、墙）
     */

    // ================ Legacy装饰方块（已废弃，保留用于参考） ================

    /*
     * 以下装饰方块已迁移到统一建筑方块系统，不再独立注册
     * 
     * DecorativeStoneBlock -> 拆分为：
     * - GOLD_ORNAMENT (金装饰)
     * - COOKED_BRICKS (烧制砖)
     * - GALIANITE_BLOCK (Galianite方块)
     * 
     * DecorativeWoodBlock -> 拆分为：
     * - SIMPLE_TIMBER_FRAME (简朴木框架)
     * - CROSS_TIMBER_FRAME (十字木框架)
     * - THATCH (茅草，已存在)
     * 
     * DecorativeEarthBlock -> 拆分为：
     * - EARTH_WALL (土墙)
     * - DRIED_BRICKS (风干砖)
     * - MUD_BRICKS (泥砖，已存在)
     */

    // ================ 初始化方法 ================

    /**
     * 初始化统一建筑方块系统
     * 
     * 必须在注册阶段早期调用，用于设置所有建筑方块的自动注册
     */
    public static void initializeBuildingBlocks() {
        // 注册所有建筑方块族系
        BuildingBlockRegistry.registerAllBlocks();
    }

    /**
     * 注册所有方块到模组事件总线
     * 
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        // 首先初始化建筑方块系统
        initializeBuildingBlocks();

        // 注册传统方块
        BLOCKS.register(eventBus);

        // 注册建筑方块系统
        BuildingBlockRegistry.BLOCKS.register(eventBus);
    }

    // ================ 已完成任务 ================

    // ✅ COMPLETED: 装饰方块系统已成功迁移到统一系统
    // ✅ COMPLETED: Village Stone已迁移到新系统
    // ✅ COMPLETED: 数据生成器已更新以支持新系统

    // ================ 待完成任务 ================

    // TODO: 添加路径系统整合到统一方块系统
    // TODO: 添加功能性方块（Mill Chest, Mill Sign等）
    // TODO: 添加特殊功能方块（农作物、特殊装饰等）
    // TODO: 完善方块物品注册系统以支持新的统一方块系统
    // TODO: 更新数据生成器以支持新的方块注册模式
}
