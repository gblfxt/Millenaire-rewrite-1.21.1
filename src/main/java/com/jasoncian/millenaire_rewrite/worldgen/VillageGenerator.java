package com.jasoncian.millenaire_rewrite.worldgen;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 村庄生成器 - 在世界中生成Millenaire村庄
 *
 * 生成策略：
 * - 基于区块坐标的伪随机判定
 * - 根据生物群系选择文化
 * - 确保村庄间距
 * - 地形适应性检查
 *
 * @author Based on OldSource village generation
 * @version 1.0.0
 */
public class VillageGenerator {

    // ================ 配置常量 ================

    /** 村庄生成基础概率（每区块组） */
    private static final double BASE_SPAWN_CHANCE = 0.02; // 2%

    /** 区块组大小（用于分布控制） */
    private static final int CHUNK_GRID_SIZE = 32;

    /** 村庄最小间距（区块） */
    private static final int MIN_VILLAGE_DISTANCE_CHUNKS = 20;

    /** 村庄最小间距（方块） */
    private static final int MIN_VILLAGE_DISTANCE_BLOCKS = MIN_VILLAGE_DISTANCE_CHUNKS * 16;

    /** 生成检查的种子偏移 */
    private static final long SEED_OFFSET = 0x4D494C4C454E41L; // "MILLENA" in hex

    /** 地形检查范围 */
    private static final int TERRAIN_CHECK_RADIUS = 16;

    /** 最大高度差（用于地形平坦度检查） */
    private static final int MAX_HEIGHT_VARIANCE = 8;

    // ================ 缓存 ================

    /** 已检查的区块组（防止重复生成） */
    private static final Set<Long> checkedGridCells = Collections.synchronizedSet(new HashSet<>());

    // ================ 主生成方法 ================

    /**
     * 尝试在指定区块生成村庄
     * 由世界生成事件调用
     *
     * @param level 服务端世界
     * @param chunkPos 区块位置
     * @return 生成的村庄，如果未生成则返回null
     */
    @Nullable
    public static Village tryGenerateVillage(ServerLevel level, ChunkPos chunkPos) {
        // 计算区块组坐标
        int gridX = Math.floorDiv(chunkPos.x, CHUNK_GRID_SIZE);
        int gridZ = Math.floorDiv(chunkPos.z, CHUNK_GRID_SIZE);
        long gridKey = packGridKey(gridX, gridZ);

        // 检查是否已处理此区块组
        if (checkedGridCells.contains(gridKey)) {
            return null;
        }

        // 计算是否应该在此区块组生成村庄
        long worldSeed = level.getSeed();
        RandomSource random = RandomSource.create(worldSeed ^ SEED_OFFSET ^ gridKey);

        if (random.nextDouble() > BASE_SPAWN_CHANCE) {
            checkedGridCells.add(gridKey);
            return null;
        }

        // 在区块组内选择具体位置
        int offsetX = random.nextInt(CHUNK_GRID_SIZE);
        int offsetZ = random.nextInt(CHUNK_GRID_SIZE);
        int targetChunkX = gridX * CHUNK_GRID_SIZE + offsetX;
        int targetChunkZ = gridZ * CHUNK_GRID_SIZE + offsetZ;

        // 如果当前区块不是目标区块，暂不生成
        if (chunkPos.x != targetChunkX || chunkPos.z != targetChunkZ) {
            return null;
        }

        checkedGridCells.add(gridKey);

        // 执行实际生成
        return generateVillageAt(level, new ChunkPos(targetChunkX, targetChunkZ), random);
    }

    /**
     * 在指定区块生成村庄
     */
    @Nullable
    private static Village generateVillageAt(ServerLevel level, ChunkPos chunkPos, RandomSource random) {
        // 获取区块中心位置
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        // 获取地面高度
        int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE, centerX, centerZ);
        BlockPos centerPos = new BlockPos(centerX, groundY, centerZ);

        // 检查与现有村庄的距离
        VillageManager villageManager = VillageManager.get(level);
        if (isTooCloseToExistingVillage(villageManager, centerPos)) {
            MillenaireRewrite.LOGGER.debug("Village spawn cancelled at {} - too close to existing village", centerPos);
            return null;
        }

        // 检查地形适宜性
        if (!isTerrainSuitable(level, centerPos)) {
            MillenaireRewrite.LOGGER.debug("Village spawn cancelled at {} - unsuitable terrain", centerPos);
            return null;
        }

        // 获取生物群系并选择文化
        Holder<Biome> biome = level.getBiome(centerPos);
        Culture culture = BiomeCultureMapping.getCultureForBiome(biome, random.fork().nextLong() > 0 ?
            new Random(random.nextLong()) : new Random());

        if (culture == null) {
            MillenaireRewrite.LOGGER.debug("Village spawn cancelled at {} - no suitable culture for biome", centerPos);
            return null;
        }

        // 创建村庄
        Village village = new Village(centerPos, culture);
        village.setDimension(level.dimension().location().toString());

        // 注册到村庄管理器
        villageManager.registerVillage(village);

        // 生成初始建筑（市政厅标记）
        placeVillageMarker(level, centerPos, culture);

        MillenaireRewrite.LOGGER.info("Generated {} village '{}' at {}",
            culture.getDisplayName(), village.getName(), centerPos);

        return village;
    }

    // ================ 检查方法 ================

    /**
     * 检查是否距离现有村庄太近
     */
    private static boolean isTooCloseToExistingVillage(VillageManager manager, BlockPos pos) {
        for (Village existing : manager.getAllVillages()) {
            BlockPos existingPos = existing.getTownHallPos();
            if (existingPos != null) {
                double distance = Math.sqrt(existingPos.distSqr(pos));
                if (distance < MIN_VILLAGE_DISTANCE_BLOCKS) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查地形是否适合建造村庄
     */
    private static boolean isTerrainSuitable(ServerLevel level, BlockPos center) {
        int centerY = center.getY();
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        // 检查周围地形高度变化
        for (int dx = -TERRAIN_CHECK_RADIUS; dx <= TERRAIN_CHECK_RADIUS; dx += 4) {
            for (int dz = -TERRAIN_CHECK_RADIUS; dz <= TERRAIN_CHECK_RADIUS; dz += 4) {
                int checkX = center.getX() + dx;
                int checkZ = center.getZ() + dz;
                int height = level.getHeight(Heightmap.Types.WORLD_SURFACE, checkX, checkZ);

                minY = Math.min(minY, height);
                maxY = Math.max(maxY, height);
            }
        }

        // 检查高度变化是否在可接受范围内
        if (maxY - minY > MAX_HEIGHT_VARIANCE) {
            return false;
        }

        // 检查地面方块类型
        BlockState groundBlock = level.getBlockState(center.below());
        if (!isValidGroundBlock(groundBlock)) {
            return false;
        }

        // 检查是否在水中
        BlockState surfaceBlock = level.getBlockState(center);
        if (surfaceBlock.is(Blocks.WATER) || surfaceBlock.is(Blocks.LAVA)) {
            return false;
        }

        return true;
    }

    /**
     * 检查方块是否是有效的地面
     */
    private static boolean isValidGroundBlock(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) ||
               state.is(Blocks.DIRT) ||
               state.is(Blocks.PODZOL) ||
               state.is(Blocks.SAND) ||
               state.is(Blocks.RED_SAND) ||
               state.is(Blocks.GRAVEL) ||
               state.is(Blocks.STONE) ||
               state.is(Blocks.SNOW_BLOCK) ||
               state.is(Blocks.TERRACOTTA) ||
               state.is(Blocks.COARSE_DIRT) ||
               state.is(Blocks.ROOTED_DIRT) ||
               state.is(Blocks.MUD) ||
               state.is(Blocks.MYCELIUM);
    }

    // ================ 村庄标记放置 ================

    /**
     * 放置村庄标记（临时，直到建筑系统完善）
     */
    private static void placeVillageMarker(ServerLevel level, BlockPos pos, Culture culture) {
        // 放置一个标记方块表示村庄中心
        // 实际实现中这里会生成市政厅建筑

        // 使用文化对应的方块作为标记
        BlockState markerBlock = getMarkerBlockForCulture(culture);

        // 创建简单的3x3平台
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos markerPos = pos.offset(dx, 0, dz);
                level.setBlock(markerPos, markerBlock, 3);
            }
        }

        // 放置中心柱
        for (int dy = 1; dy <= 3; dy++) {
            level.setBlock(pos.above(dy), getCulturePillarBlock(culture), 3);
        }

        // 放置火把
        level.setBlock(pos.above(4), Blocks.TORCH.defaultBlockState(), 3);
    }

    /**
     * 获取文化对应的标记方块
     */
    private static BlockState getMarkerBlockForCulture(Culture culture) {
        return switch (culture) {
            case NORMAN -> Blocks.COBBLESTONE.defaultBlockState();
            case JAPANESE -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
            case INDIAN -> Blocks.SANDSTONE.defaultBlockState();
            case MAYAN -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case BYZANTINE -> Blocks.STONE_BRICKS.defaultBlockState();
            case INUIT -> Blocks.PACKED_ICE.defaultBlockState();
            case SELJUK -> Blocks.RED_SANDSTONE.defaultBlockState();
        };
    }

    /**
     * 获取文化对应的柱子方块
     */
    private static BlockState getCulturePillarBlock(Culture culture) {
        return switch (culture) {
            case NORMAN -> Blocks.OAK_FENCE.defaultBlockState();
            case JAPANESE -> Blocks.DARK_OAK_FENCE.defaultBlockState();
            case INDIAN -> Blocks.BIRCH_FENCE.defaultBlockState();
            case MAYAN -> Blocks.JUNGLE_FENCE.defaultBlockState();
            case BYZANTINE -> Blocks.STONE_BRICK_WALL.defaultBlockState();
            case INUIT -> Blocks.SPRUCE_FENCE.defaultBlockState();
            case SELJUK -> Blocks.ACACIA_FENCE.defaultBlockState();
        };
    }

    // ================ 工具方法 ================

    /**
     * 打包区块组坐标为long key
     */
    private static long packGridKey(int gridX, int gridZ) {
        return ((long) gridX << 32) | (gridZ & 0xFFFFFFFFL);
    }

    /**
     * 清除缓存（用于世界切换时）
     */
    public static void clearCache() {
        checkedGridCells.clear();
    }

    /**
     * 强制在指定位置生成村庄（用于命令/测试）
     */
    @Nullable
    public static Village forceGenerateVillage(ServerLevel level, BlockPos pos, @Nullable Culture culture) {
        if (culture == null) {
            Holder<Biome> biome = level.getBiome(pos);
            culture = BiomeCultureMapping.getCultureForBiome(biome, new Random());
            if (culture == null) {
                culture = Culture.NORMAN; // 默认
            }
        }

        // 调整到地面高度
        int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        BlockPos groundPos = new BlockPos(pos.getX(), groundY, pos.getZ());

        VillageManager villageManager = VillageManager.get(level);

        // 创建村庄
        Village village = new Village(groundPos, culture);
        village.setDimension(level.dimension().location().toString());
        villageManager.registerVillage(village);

        // 放置标记
        placeVillageMarker(level, groundPos, culture);

        MillenaireRewrite.LOGGER.info("Force generated {} village '{}' at {}",
            culture.getDisplayName(), village.getName(), groundPos);

        return village;
    }
}
