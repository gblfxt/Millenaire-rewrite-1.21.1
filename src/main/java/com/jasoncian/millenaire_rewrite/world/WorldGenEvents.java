package com.jasoncian.millenaire_rewrite.world;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/**
 * 世界生成事件处理 - 处理区块加载和世界卸载事件
 *
 * 功能：
 * - 在区块加载时触发村庄生成检查
 * - 在世界卸载时清理缓存
 *
 * @author Based on OldSource world gen hooks
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID)
public class WorldGenEvents {

    /**
     * 区块加载事件处理
     *
     * 当新区块被加载时，检查是否应该在该区块附近生成村庄
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        // 仅在服务器端处理
        if (event.getLevel().isClientSide()) {
            return;
        }

        // 确保是ServerLevel
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        // 确保区块是LevelChunk（不是空区块）
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        // 仅处理新生成的区块（避免重复处理已存在的区块）
        // 注意：这是一个简化的检查，实际上可能需要更复杂的逻辑
        if (!chunk.isOldNoiseGeneration()) {
            // 调用村庄生成器
            try {
                VillageGenerator.getInstance().onChunkLoad(level, chunk);
            } catch (Exception e) {
                MillenaireRewrite.LOGGER.debug("Error during village generation check: {}", e.getMessage());
            }
        }
    }

    /**
     * 世界卸载事件处理
     *
     * 清理与该世界相关的缓存
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        // 仅在服务器端处理
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getLevel() instanceof ServerLevel level) {
            String dimension = level.dimension().location().toString();
            VillageGenerator.getInstance().clearCacheForDimension(dimension);
            MillenaireRewrite.LOGGER.debug("Cleared village generation cache for dimension: {}", dimension);
        }
    }
}
