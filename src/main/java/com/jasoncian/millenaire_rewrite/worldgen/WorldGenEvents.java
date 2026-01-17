package com.jasoncian.millenaire_rewrite.worldgen;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 世界生成事件处理器 - 监听区块生成并触发村庄生成
 *
 * 功能：
 * - 监听区块加载事件
 * - 触发村庄生成检查
 * - 处理世界切换时的缓存清理
 *
 * @author Based on NeoForge event patterns
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID)
public class WorldGenEvents {

    // ================ 延迟生成队列 ================

    /** 待处理的区块位置队列 */
    private static final Queue<PendingChunk> pendingChunks = new ConcurrentLinkedQueue<>();

    /** 每tick处理的最大区块数 */
    private static final int MAX_CHUNKS_PER_TICK = 5;

    /** 待处理区块记录 */
    private record PendingChunk(ServerLevel level, ChunkPos pos) {}

    // ================ 事件处理 ================

    /**
     * 区块加载事件处理
     * 当新区块被加载时检查是否应该生成村庄
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelAccessor levelAccessor = event.getLevel();

        // 只在服务端处理
        if (levelAccessor.isClientSide()) {
            return;
        }

        if (!(levelAccessor instanceof ServerLevel serverLevel)) {
            return;
        }

        // 只在主世界生成村庄（可配置）
        if (!isValidDimension(serverLevel)) {
            return;
        }

        ChunkAccess chunk = event.getChunk();
        ChunkPos chunkPos = chunk.getPos();

        // 添加到延迟处理队列（避免在区块加载时直接修改世界）
        pendingChunks.offer(new PendingChunk(serverLevel, chunkPos));
    }

    /**
     * 服务端tick事件处理
     * 处理延迟的村庄生成
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        int processed = 0;

        while (!pendingChunks.isEmpty() && processed < MAX_CHUNKS_PER_TICK) {
            PendingChunk pending = pendingChunks.poll();
            if (pending != null && pending.level() != null) {
                try {
                    Village village = VillageGenerator.tryGenerateVillage(pending.level(), pending.pos());
                    if (village != null) {
                        MillenaireRewrite.LOGGER.debug("Village generation triggered at chunk {}",
                            pending.pos());
                    }
                } catch (Exception e) {
                    MillenaireRewrite.LOGGER.error("Error generating village at chunk {}: {}",
                        pending.pos(), e.getMessage());
                }
            }
            processed++;
        }
    }

    /**
     * 世界卸载事件处理
     * 清理缓存
     */
    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event) {
        LevelAccessor level = event.getLevel();

        if (level instanceof ServerLevel) {
            // 清除该世界相关的待处理区块
            pendingChunks.removeIf(pending -> pending.level() == level);

            // 清除生成器缓存
            VillageGenerator.clearCache();

            MillenaireRewrite.LOGGER.debug("Cleared village generation cache for unloaded world");
        }
    }

    // ================ 辅助方法 ================

    /**
     * 检查维度是否允许村庄生成
     */
    private static boolean isValidDimension(ServerLevel level) {
        String dimension = level.dimension().location().toString();

        // 默认只在主世界和自定义维度生成
        // 禁止在下界和末地生成
        return !dimension.equals("minecraft:the_nether") &&
               !dimension.equals("minecraft:the_end");
    }

    /**
     * 获取待处理区块数量（用于调试）
     */
    public static int getPendingChunkCount() {
        return pendingChunks.size();
    }

    /**
     * 清除所有待处理区块（用于测试）
     */
    public static void clearPendingChunks() {
        pendingChunks.clear();
    }
}
