package com.jasoncian.millenaire_rewrite.datagen;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingBlock;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingSlabBlock;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingStairsBlock;
import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingWallBlock;
import com.jasoncian.millenaire_rewrite.blocks.decorative.PathBlock;
import com.jasoncian.millenaire_rewrite.blocks.system.BuildingBlockRegistry;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 方块状态数据生成器
 * 
 * 负责生成所有方块的blockstates json文件
 * 这比手动创建文件更高效且不容易出错
 * 
 * ⚠️  注意：装饰方块的数据生成已弃用，需要为新的统一方块系统添加支持
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MillenaireRewrite.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        MillenaireRewrite.LOGGER.info("Generating block states and models...");

        // 自动为BuildingBlockRegistry中的所有方块生成模型和状态
        int blockCount = 0;
        for (var entry : BuildingBlockRegistry.getAllBlocks().entrySet()) {
            String registryName = entry.getKey();
            DeferredHolder<Block, Block> holder = entry.getValue();
            Block block = holder.get();

            try {
                if (block instanceof PathBlock) {
                    // 路径方块使用特殊模型（低于一格高度）
                    generatePathBlockModel(registryName, block);
                } else if (block instanceof BaseBuildingStairsBlock) {
                    // 楼梯方块
                    generateStairsBlockModel(registryName, (StairBlock) block);
                } else if (block instanceof BaseBuildingSlabBlock) {
                    // 台阶方块
                    generateSlabBlockModel(registryName, (SlabBlock) block);
                } else if (block instanceof BaseBuildingWallBlock) {
                    // 墙方块
                    generateWallBlockModel(registryName, (WallBlock) block);
                } else if (block instanceof BaseBuildingBlock) {
                    // 基础方块
                    generateSimpleBlockModel(registryName, block);
                }
                blockCount++;
            } catch (Exception e) {
                MillenaireRewrite.LOGGER.warn("Failed to generate model for block: {}", registryName, e);
            }
        }

        MillenaireRewrite.LOGGER.info("Generated block states for {} blocks", blockCount);
    }

    /**
     * 为简单的立方体方块生成模型
     */
    private void generateSimpleBlockModel(String registryName, Block block) {
        ResourceLocation texture = modLoc("block/" + registryName);
        simpleBlockWithItem(block, cubeAll(block));
    }

    /**
     * 为路径方块生成模型（15/16高度）
     */
    private void generatePathBlockModel(String registryName, Block block) {
        ResourceLocation texture = modLoc("block/" + registryName);
        // 路径方块使用特殊模型，高度为15像素而不是16像素
        ModelFile model = models().withExistingParent(registryName, mcLoc("block/dirt_path"))
            .texture("top", texture)
            .texture("side", texture);
        simpleBlockWithItem(block, model);
    }

    /**
     * 为楼梯方块生成模型
     */
    private void generateStairsBlockModel(String registryName, StairBlock block) {
        // 从楼梯名称推导基础方块纹理名称
        String baseTextureName = registryName.replace("_stairs", "_block");
        ResourceLocation texture = modLoc("block/" + baseTextureName);
        stairsBlock(block, texture);
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + registryName)));
    }

    /**
     * 为台阶方块生成模型
     */
    private void generateSlabBlockModel(String registryName, SlabBlock block) {
        // 从台阶名称推导基础方块纹理名称
        String baseTextureName = registryName.replace("_slab", "_block");
        ResourceLocation texture = modLoc("block/" + baseTextureName);
        ResourceLocation fullBlockModel = modLoc("block/" + baseTextureName);
        slabBlock(block, fullBlockModel, texture, texture, texture);
        simpleBlockItem(block, models().getExistingFile(modLoc("block/" + registryName)));
    }

    /**
     * 为墙方块生成模型
     */
    private void generateWallBlockModel(String registryName, WallBlock block) {
        // 从墙名称推导基础方块纹理名称
        String baseTextureName = registryName.replace("_wall", "_block");
        ResourceLocation texture = modLoc("block/" + baseTextureName);
        wallBlock(block, texture);
        // 墙的物品模型使用inventory模型
        itemModels().wallInventory(registryName, texture);
    }

    /**
     * 生成石材装饰方块的状态和模型 - 已弃用
     * 
     * @deprecated 装饰方块已迁移至统一方块系统，需要为新系统编写对应的数据生成逻辑
     */
    /*
    private void decorativeStoneBlock() {
        VariantBlockStateBuilder builder = getVariantBuilder(ModBlocks.DECORATIVE_STONE.get());

        for (StoneDecorativeVariant variant : StoneDecorativeVariant.values()) {
            String modelName = "decorative_stone_" + variant.getSerializedName();
            String texturePath = "block/" + variant.getSerializedName();

            ModelFile model = models().cubeAll(modelName, modLoc(texturePath));

            builder.partialState()
                    .with(DecorativeStoneBlock.VARIANT, variant)
                    .setModels(ConfiguredModel.builder()
                            .modelFile(model)
                            .build());
        }
    }
    */

    /**
     * 生成木材装饰方块的状态和模型 - 已弃用
     * 
     * @deprecated 装饰方块已迁移至统一方块系统，需要为新系统编写对应的数据生成逻辑
     */
    /*
    private void decorativeWoodBlock() {
        VariantBlockStateBuilder builder = getVariantBuilder(ModBlocks.DECORATIVE_WOOD.get());

        for (WoodDecorativeVariant variant : WoodDecorativeVariant.values()) {
            String modelName = "decorative_wood_" + variant.getSerializedName();
            String texturePath = "block/" + variant.getSerializedName();

            ModelFile model = models().cubeAll(modelName, modLoc(texturePath));

            builder.partialState()
                    .with(DecorativeWoodBlock.VARIANT, variant)
                    .setModels(ConfiguredModel.builder()
                            .modelFile(model)
                            .build());
        }
    }
    */

    /**
     * 生成土质装饰方块的状态和模型 - 已弃用
     * 
     * @deprecated 装饰方块已迁移至统一方块系统，需要为新系统编写对应的数据生成逻辑
     */
    /*
    private void decorativeEarthBlock() {
        VariantBlockStateBuilder builder = getVariantBuilder(ModBlocks.DECORATIVE_EARTH.get());

        for (EarthDecorativeVariant variant : EarthDecorativeVariant.values()) {
            String modelName = "decorative_earth_" + variant.getSerializedName();
            String texturePath = "block/" + variant.getSerializedName();

            ModelFile model = models().cubeAll(modelName, modLoc(texturePath));

            builder.partialState()
                    .with(DecorativeEarthBlock.VARIANT, variant)
                    .setModels(ConfiguredModel.builder()
                            .modelFile(model)
                            .build());
        }
    }
    */

    /**
     * 为简单的立方体方块生成状态和模型
     * 这个方法会：
     * 1. 生成blockstates文件
     * 2. 生成方块模型
     * 3. 生成物品模型（引用方块模型）
     */
    
    // TODO: 为新的统一方块系统添加自动化数据生成支持
}
