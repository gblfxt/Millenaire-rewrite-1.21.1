package com.jasoncian.millenaire_rewrite.datagen;

import com.jasoncian.millenaire_rewrite.blocks.base.BaseBuildingSlabBlock;
import com.jasoncian.millenaire_rewrite.blocks.decorative.PathBlock;
import com.jasoncian.millenaire_rewrite.blocks.system.BuildingBlockRegistry;
import com.jasoncian.millenaire_rewrite.core.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

/**
 * 方块战利品表数据生成器
 *
 * 负责生成方块被破坏时掉落的物品
 * Village Stone根据legacy逻辑不应该掉落任何物品
 *
 * ⚠️  注意：装饰方块战利品表已弃用，需要为新的统一方块系统添加支持
 */
public class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Village Stone - 不掉落任何物品（参考legacy的quantityDropped返回0）
        // 通过不添加战利品表来实现无掉落

        // ================ BuildingBlockRegistry 自动战利品表生成 ================
        // 自动为所有建筑方块生成战利品表
        for (var entry : BuildingBlockRegistry.getAllBlocks().entrySet()) {
            Block block = entry.getValue().get();

            if (block instanceof SlabBlock slabBlock) {
                // 台阶方块使用特殊掉落逻辑（放置两个时掉落1个）
                this.add(slabBlock, createSlabItemTable(slabBlock));
            } else {
                // 其他方块掉落自身
                this.dropSelf(block);
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // 合并 ModBlocks 和 BuildingBlockRegistry 中的所有方块
        java.util.List<Block> allBlocks = new java.util.ArrayList<>();

        // 添加 ModBlocks 中的方块
        ModBlocks.BLOCKS.getEntries().stream()
            .map(holder -> (Block) holder.get())
            .forEach(allBlocks::add);

        // 添加 BuildingBlockRegistry 中的方块
        BuildingBlockRegistry.getAllBlocks().values().stream()
            .map(holder -> (Block) holder.get())
            .forEach(allBlocks::add);

        return allBlocks;
    }
}
