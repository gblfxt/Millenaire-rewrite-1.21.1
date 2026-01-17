package com.jasoncian.millenaire_rewrite.blocks.system;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingBlock;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingSlabBlock;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingStairsBlock;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingWallBlock;
import com.jasoncian.millenaire_rewrite.blocks.decorative.PathBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 建筑方块系统注册器 - 统一管理所有建筑方块的注册
 *
 * 负责根据配置自动生成和注册所有建筑方块变体，
 * 包括普通方块、楼梯、台阶、墙等形式。
 *
 * 功能特性：
 * - 自动化方块族系生成
 * - 统一的注册管理
 * - 类型安全的方块访问
 * - 灵活的配置系统
 * - 支持文化特色定制
 *
 * @author JasonCian
 * @version 1.0.0
 */
public class BuildingBlockRegistry {
    
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(BuiltInRegistries.BLOCK, MillenaireRewrite.MOD_ID);
    
    // 存储所有注册的方块，按文化和材料分类
    private static final Map<String, DeferredHolder<Block, Block>> REGISTERED_BLOCKS = new HashMap<>();

    // 路径材料集合（用于判断是否使用PathBlock）
    private static final Set<BasicBuildingMaterial> PATH_MATERIALS = new HashSet<>(
        Arrays.asList(BasicBuildingMaterial.getPathMaterials())
    );
    
    // ================ 注册管理方法 ================
    
    /**
     * 注册完整的方块族系
     * 包括：方块、楼梯、台阶、墙
     * 路径方块只注册基础方块（无变体）
     *
     * @param material 基础建筑材料
     * @param culture 文化系列
     * @return 注册的方块映射表
     */
    public static Map<BlockVariantType, DeferredHolder<Block, Block>> registerBlockFamily(
            BasicBuildingMaterial material, CulturalBlockFamily culture) {

        Map<BlockVariantType, DeferredHolder<Block, Block>> family = new HashMap<>();

        // 路径方块特殊处理 - 只注册基础方块
        if (isPathMaterial(material)) {
            DeferredHolder<Block, Block> pathBlock = registerPathBlock(material, culture);
            family.put(BlockVariantType.BLOCK, pathBlock);
            return family;
        }

        // 注册基础方块
        DeferredHolder<Block, Block> baseBlock = registerBlock(material, culture, BlockVariantType.BLOCK);
        family.put(BlockVariantType.BLOCK, baseBlock);

        // 注册楼梯方块
        DeferredHolder<Block, Block> stairsBlock = registerStairs(material, culture, baseBlock);
        family.put(BlockVariantType.STAIRS, stairsBlock);

        // 注册台阶方块
        DeferredHolder<Block, Block> slabBlock = registerSlab(material, culture);
        family.put(BlockVariantType.SLAB, slabBlock);

        // 注册墙方块
        DeferredHolder<Block, Block> wallBlock = registerWall(material, culture);
        family.put(BlockVariantType.WALL, wallBlock);

        return family;
    }

    /**
     * 检查材料是否为路径材料
     *
     * @param material 基础建筑材料
     * @return 是否为路径材料
     */
    public static boolean isPathMaterial(BasicBuildingMaterial material) {
        return PATH_MATERIALS.contains(material);
    }

    /**
     * 注册路径方块
     *
     * @param material 基础建筑材料
     * @param culture 文化系列
     * @return 注册的路径方块
     */
    public static DeferredHolder<Block, Block> registerPathBlock(
            BasicBuildingMaterial material, CulturalBlockFamily culture) {

        String registryName = material.generateBlockRegistryName(culture, BlockVariantType.BLOCK);

        DeferredHolder<Block, Block> block = BLOCKS.register(registryName,
            () -> new PathBlock(material, culture));

        REGISTERED_BLOCKS.put(registryName, block);
        return block;
    }
    
    /**
     * 注册基础方块
     *
     * @param material 基础建筑材料
     * @param culture 文化系列
     * @param variant 方块变体
     * @return 注册的方块
     */
    public static DeferredHolder<Block, Block> registerBlock(
            BasicBuildingMaterial material, CulturalBlockFamily culture, BlockVariantType variant) {
        
        String registryName = material.generateBlockRegistryName(culture, variant);
        
        DeferredHolder<Block, Block> block = BLOCKS.register(registryName, 
            () -> new BaseBuildingBlock(material, culture, variant));
        
        REGISTERED_BLOCKS.put(registryName, block);
        return block;
    }
    
    /**
     * 注册楼梯方块
     *
     * @param material 基础建筑材料
     * @param culture 文化系列
     * @param baseBlock 基础方块引用
     * @return 注册的楼梯方块
     */
    public static DeferredHolder<Block, Block> registerStairs(
            BasicBuildingMaterial material, CulturalBlockFamily culture, DeferredHolder<Block, Block> baseBlock) {
        
        String registryName = material.generateBlockRegistryName(culture, BlockVariantType.STAIRS);
        
        DeferredHolder<Block, Block> stairs = BLOCKS.register(registryName,
            () -> new BaseBuildingStairsBlock(
                () -> baseBlock.get().defaultBlockState(),
                material, 
                culture
            ));
        
        REGISTERED_BLOCKS.put(registryName, stairs);
        return stairs;
    }
    
    /**
     * 注册台阶方块
     *
     * @param material 基础建筑材料
     * @param culture 文化系列
     * @return 注册的台阶方块
     */
    public static DeferredHolder<Block, Block> registerSlab(
            BasicBuildingMaterial material, CulturalBlockFamily culture) {
        
        String registryName = material.generateBlockRegistryName(culture, BlockVariantType.SLAB);
        
        DeferredHolder<Block, Block> slab = BLOCKS.register(registryName,
            () -> new BaseBuildingSlabBlock(material, culture));
        
        REGISTERED_BLOCKS.put(registryName, slab);
        return slab;
    }
    
    /**
     * 注册墙方块
     *
     * @param material 基础建筑材料
     * @param culture 文化系列
     * @return 注册的墙方块
     */
    public static DeferredHolder<Block, Block> registerWall(
            BasicBuildingMaterial material, CulturalBlockFamily culture) {
        
        String registryName = material.generateBlockRegistryName(culture, BlockVariantType.WALL);
        
        DeferredHolder<Block, Block> wall = BLOCKS.register(registryName,
            () -> new BaseBuildingWallBlock(material, culture));
        
        REGISTERED_BLOCKS.put(registryName, wall);
        return wall;
    }
    
    // ================ 批量注册方法 ================
    
    /**
     * 批量注册所有基础建筑材料的方块族系
     *
     * @param culture 文化系列
     */
    public static void registerAllMaterialsForCulture(CulturalBlockFamily culture) {
        // 注册所有基础建筑材料
        for (BasicBuildingMaterial material : BasicBuildingMaterial.values()) {
            registerBlockFamily(material, culture);
        }
    }
    
    /**
     * 注册所有文化的所有材料方块
     */
    public static void registerAllBlocks() {
        // 只为基础文化注册所有材料
        registerAllMaterialsForCulture(CulturalBlockFamily.BASIC);
        
        // 为其他文化注册选定的材料
        registerCulturalSpecificBlocks();
    }
    
    /**
     * 注册文化特色方块
     */
    private static void registerCulturalSpecificBlocks() {
        // 诺曼文化：石材和木材系列 + 装饰材料
        registerSelectedMaterials(CulturalBlockFamily.NORMAN,
            BasicBuildingMaterial.getStoneMaterials());
        registerSelectedMaterials(CulturalBlockFamily.NORMAN,
            BasicBuildingMaterial.getStoneDecorativeMaterials());
        registerSelectedMaterials(CulturalBlockFamily.NORMAN,
            BasicBuildingMaterial.getWoodDecorativeMaterials());
        registerBlockFamily(BasicBuildingMaterial.TIMBER_FRAME, CulturalBlockFamily.NORMAN);
        // 诺曼路径
        registerBlockFamily(BasicBuildingMaterial.STONE_PATH, CulturalBlockFamily.NORMAN);
        registerBlockFamily(BasicBuildingMaterial.GRAVEL_PATH, CulturalBlockFamily.NORMAN);

        // 拜占庭文化：拜占庭瓦片 + 马赛克 + 金装饰 (OldSource BlockDecorativeStone)
        registerSelectedMaterials(CulturalBlockFamily.BYZANTINE,
            BasicBuildingMaterial.getByzantineMaterials());
        // 浅蓝砖 - 拜占庭风格
        registerSelectedMaterials(CulturalBlockFamily.BYZANTINE,
            BasicBuildingMaterial.getLightBlueBrickMaterials());
        // 拜占庭路径
        registerBlockFamily(BasicBuildingMaterial.STONE_PATH, CulturalBlockFamily.BYZANTINE);

        // 印度文化：特色瓦片 + 装饰材料
        registerBlockFamily(BasicBuildingMaterial.RED_TILES, CulturalBlockFamily.INDIAN);
        registerBlockFamily(BasicBuildingMaterial.OCHRE_TILES, CulturalBlockFamily.INDIAN);
        registerSelectedMaterials(CulturalBlockFamily.INDIAN,
            BasicBuildingMaterial.getEarthDecorativeMaterials());
        // 印度路径
        registerBlockFamily(BasicBuildingMaterial.SANDSTONE_PATH, CulturalBlockFamily.INDIAN);

        // 日本文化：特色材料 + 木质装饰
        registerBlockFamily(BasicBuildingMaterial.TIMBER_FRAME, CulturalBlockFamily.JAPANESE);
        registerSelectedMaterials(CulturalBlockFamily.JAPANESE,
            BasicBuildingMaterial.getWoodDecorativeMaterials());
        // 日本路径
        registerBlockFamily(BasicBuildingMaterial.WOODEN_PATH, CulturalBlockFamily.JAPANESE);
        registerBlockFamily(BasicBuildingMaterial.STONE_PATH, CulturalBlockFamily.JAPANESE);

        // 玛雅文化：石材系列 + 特殊装饰 (OldSource BlockDecorativeStone MAYANGOLDBLOCK)
        registerSelectedMaterials(CulturalBlockFamily.MAYAN,
            BasicBuildingMaterial.getStoneMaterials());
        registerSelectedMaterials(CulturalBlockFamily.MAYAN,
            BasicBuildingMaterial.getMayanMaterials());
        // 玛雅路径
        registerBlockFamily(BasicBuildingMaterial.STONE_PATH, CulturalBlockFamily.MAYAN);

        // 因纽特文化：冰雪材料
        registerBlockFamily(BasicBuildingMaterial.ICE_BRICKS, CulturalBlockFamily.INUIT);
        registerBlockFamily(BasicBuildingMaterial.SNOW_BRICKS, CulturalBlockFamily.INUIT);
    }
    
    /**
     * 为指定文化注册选定材料的方块族系
     *
     * @param culture 文化系列
     * @param materials 材料数组
     */
    private static void registerSelectedMaterials(CulturalBlockFamily culture, BasicBuildingMaterial[] materials) {
        for (BasicBuildingMaterial material : materials) {
            registerBlockFamily(material, culture);
        }
    }
    
    // ================ 访问器方法 ================
    
    /**
     * 获取注册的方块
     *
     * @param registryName 注册名
     * @return 注册的方块，如果不存在则返回null
     */
    public static DeferredHolder<Block, Block> getBlock(String registryName) {
        return REGISTERED_BLOCKS.get(registryName);
    }
    
    /**
     * 获取注册的方块
     *
     * @param material 基础建筑材料
     * @param culture 文化系列
     * @param variant 方块变体
     * @return 注册的方块，如果不存在则返回null
     */
    public static DeferredHolder<Block, Block> getBlock(
            BasicBuildingMaterial material, CulturalBlockFamily culture, BlockVariantType variant) {
        
        String registryName = material.generateBlockRegistryName(culture, variant);
        return getBlock(registryName);
    }
    
    /**
     * 获取所有注册的方块
     *
     * @return 所有注册方块的映射表
     */
    public static Map<String, DeferredHolder<Block, Block>> getAllBlocks() {
        return new HashMap<>(REGISTERED_BLOCKS);
    }
}
