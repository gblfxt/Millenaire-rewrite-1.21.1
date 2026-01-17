package com.jasoncian.millenaire_rewrite.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 村庄放置计算器 - 分析地形以确定合适的村庄位置
 *
 * 分析：
 * - 地形平坦度
 * - 水体覆盖
 * - 可建造面积
 * - 高度变化
 *
 * @author Based on OldSource VillageMapInfo
 * @version 1.0.0
 */
public class VillagePlacementCalculator {

    // ================ 配置 ================

    /** 最大允许高度差 */
    private static final int MAX_HEIGHT_VARIATION = 10;

    /** 最小可建造百分比 */
    private static final double MIN_BUILDABLE_PERCENT = 0.7;

    /** 最大水体百分比 */
    private static final double MAX_WATER_PERCENT = 0.2;

    /** 采样间隔（方块） */
    private static final int SAMPLE_INTERVAL = 4;

    // ================ 数据 ================

    private final ServerLevel level;

    // ================ 构造函数 ================

    public VillagePlacementCalculator(ServerLevel level) {
        this.level = level;
    }

    // ================ 公共方法 ================

    /**
     * 检查位置是否适合放置村庄
     *
     * @param center 中心位置
     * @param radius 村庄半径
     * @return true如果位置有效
     */
    public boolean isValidPlacement(BlockPos center, int radius) {
        // 分析地形
        TerrainAnalysis analysis = analyzeTerrainArea(center, radius);

        // 检查可建造面积
        if (analysis.buildablePercent < MIN_BUILDABLE_PERCENT) {
            return false;
        }

        // 检查水体覆盖
        if (analysis.waterPercent > MAX_WATER_PERCENT) {
            return false;
        }

        // 检查高度变化
        if (analysis.heightVariation > MAX_HEIGHT_VARIATION) {
            return false;
        }

        return true;
    }

    /**
     * 获取地形分析详情
     */
    public TerrainAnalysis analyzeTerrainArea(BlockPos center, int radius) {
        int totalSamples = 0;
        int buildableSamples = 0;
        int waterSamples = 0;
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;

        // 采样区域
        for (int x = -radius; x <= radius; x += SAMPLE_INTERVAL) {
            for (int z = -radius; z <= radius; z += SAMPLE_INTERVAL) {
                BlockPos samplePos = center.offset(x, 0, z);

                // 获取地表高度
                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, samplePos.getX(), samplePos.getZ());
                BlockPos surfacePos = new BlockPos(samplePos.getX(), surfaceY - 1, samplePos.getZ());
                BlockState surfaceState = level.getBlockState(surfacePos);

                totalSamples++;

                // 记录高度
                if (surfaceY < minHeight) minHeight = surfaceY;
                if (surfaceY > maxHeight) maxHeight = surfaceY;

                // 检查是否可建造
                if (isBuildable(surfaceState)) {
                    buildableSamples++;
                }

                // 检查是否是水
                if (isWater(surfaceState)) {
                    waterSamples++;
                }
            }
        }

        TerrainAnalysis analysis = new TerrainAnalysis();
        analysis.totalSamples = totalSamples;
        analysis.buildablePercent = totalSamples > 0 ? (double) buildableSamples / totalSamples : 0;
        analysis.waterPercent = totalSamples > 0 ? (double) waterSamples / totalSamples : 0;
        analysis.heightVariation = maxHeight - minHeight;
        analysis.minHeight = minHeight;
        analysis.maxHeight = maxHeight;
        analysis.averageHeight = (minHeight + maxHeight) / 2;

        return analysis;
    }

    /**
     * 找到区域内最平坦的位置
     *
     * @param searchCenter 搜索中心
     * @param searchRadius 搜索半径
     * @param villageRadius 村庄半径
     * @return 最佳位置，如果找不到返回null
     */
    public BlockPos findBestPlacement(BlockPos searchCenter, int searchRadius, int villageRadius) {
        BlockPos bestPos = null;
        double bestScore = 0;

        // 在搜索区域内采样
        for (int x = -searchRadius; x <= searchRadius; x += 16) {
            for (int z = -searchRadius; z <= searchRadius; z += 16) {
                BlockPos candidate = searchCenter.offset(x, 0, z);
                TerrainAnalysis analysis = analyzeTerrainArea(candidate, villageRadius);

                // 计算评分
                double score = calculatePlacementScore(analysis);

                if (score > bestScore && isValidPlacement(candidate, villageRadius)) {
                    bestScore = score;
                    bestPos = candidate;
                }
            }
        }

        return bestPos;
    }

    // ================ 私有方法 ================

    /**
     * 检查方块是否可建造
     */
    private boolean isBuildable(BlockState state) {
        // 不可建造的方块类型
        if (state.isAir()) return false;
        if (state.is(Blocks.WATER)) return false;
        if (state.is(Blocks.LAVA)) return false;
        if (state.is(Blocks.BEDROCK)) return false;
        if (state.is(BlockTags.ICE)) return false;

        // 固体方块可以建造
        return state.isSolid();
    }

    /**
     * 检查是否是水方块
     */
    private boolean isWater(BlockState state) {
        return state.is(Blocks.WATER) ||
               state.is(Blocks.SEAGRASS) ||
               state.is(Blocks.TALL_SEAGRASS) ||
               state.is(Blocks.KELP) ||
               state.is(Blocks.KELP_PLANT);
    }

    /**
     * 计算放置评分
     */
    private double calculatePlacementScore(TerrainAnalysis analysis) {
        double score = 0;

        // 可建造面积加分（0-40分）
        score += analysis.buildablePercent * 40;

        // 高度变化越小越好（0-30分）
        double heightScore = Math.max(0, 30 - analysis.heightVariation * 3);
        score += heightScore;

        // 水体越少越好（0-20分）
        score += (1 - analysis.waterPercent) * 20;

        // 高度适中加分（0-10分）
        // 理想高度在60-80之间
        int idealMin = 60, idealMax = 80;
        if (analysis.averageHeight >= idealMin && analysis.averageHeight <= idealMax) {
            score += 10;
        } else {
            int heightDiff = Math.min(
                Math.abs(analysis.averageHeight - idealMin),
                Math.abs(analysis.averageHeight - idealMax)
            );
            score += Math.max(0, 10 - heightDiff * 0.5);
        }

        return score;
    }

    // ================ 内部类 ================

    /**
     * 地形分析结果
     */
    public static class TerrainAnalysis {
        public int totalSamples;
        public double buildablePercent;
        public double waterPercent;
        public int heightVariation;
        public int minHeight;
        public int maxHeight;
        public int averageHeight;

        @Override
        public String toString() {
            return String.format(
                "TerrainAnalysis{buildable=%.1f%%, water=%.1f%%, heightVar=%d, avgHeight=%d}",
                buildablePercent * 100, waterPercent * 100, heightVariation, averageHeight
            );
        }
    }
}
