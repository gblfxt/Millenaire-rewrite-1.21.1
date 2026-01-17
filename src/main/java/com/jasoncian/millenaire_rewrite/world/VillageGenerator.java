package com.jasoncian.millenaire_rewrite.world;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.building.BuildingBlueprint;
import com.jasoncian.millenaire_rewrite.building.BuildingRegistry;
import com.jasoncian.millenaire_rewrite.building.BuildingType;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.village.BuildingLocation;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 村庄生成器 - 在世界中生成Millenaire村庄
 *
 * 功能：
 * - 在世界生成时自动放置村庄
 * - 根据生物群系选择适当文化
 * - 保持村庄间最小距离
 * - 地形分析和验证
 *
 * @author Based on OldSource WorldGenVillage
 * @version 1.0.0
 */
public class VillageGenerator {

    // ================ 单例 ================

    private static VillageGenerator instance;

    public static VillageGenerator getInstance() {
        if (instance == null) {
            instance = new VillageGenerator();
        }
        return instance;
    }

    // ================ 配置常量 ================

    /** 村庄间最小距离（方块） */
    private static final int MIN_VILLAGE_DISTANCE = 500;

    /** 村庄生成几率（每个有效区块） */
    private static final double VILLAGE_SPAWN_CHANCE = 0.002;

    /** 区块检查间隔（减少检查频率） */
    private static final int CHUNK_CHECK_INTERVAL = 4;

    /** 村庄半径 */
    private static final int VILLAGE_RADIUS = 50;

    /** 最小可用面积百分比 */
    private static final double MIN_USABLE_AREA_PERCENT = 0.7;

    /** 出生点保护半径 */
    private static final int SPAWN_PROTECTION_RADIUS = 200;

    // ================ 状态 ================

    /** 已检查的区块坐标（避免重复检查） */
    private final Set<Long> checkedChunks = Collections.synchronizedSet(new HashSet<>());

    /** 生成尝试缓存 */
    private final Map<Long, Long> generationAttempts = Collections.synchronizedMap(new HashMap<>());

    // ================ 构造函数 ================

    private VillageGenerator() {
        // 私有构造函数
    }

    // ================ 公共方法 ================

    /**
     * 尝试在区块加载时生成村庄
     *
     * @param level 服务器世界
     * @param chunk 加载的区块
     */
    public void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        // 仅在主世界生成
        if (!isOverworld(level)) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();

        // 检查间隔
        if ((chunkPos.x % CHUNK_CHECK_INTERVAL) != 0 || (chunkPos.z % CHUNK_CHECK_INTERVAL) != 0) {
            return;
        }

        // 检查是否已经检查过
        long chunkKey = ChunkPos.asLong(chunkPos.x, chunkPos.z);
        if (checkedChunks.contains(chunkKey)) {
            return;
        }
        checkedChunks.add(chunkKey);

        // 随机检查是否应该尝试生成
        RandomSource random = level.random;
        if (random.nextDouble() > VILLAGE_SPAWN_CHANCE) {
            return;
        }

        // 获取区块中心位置
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        BlockPos centerPos = new BlockPos(centerX, 64, centerZ);

        // 尝试生成村庄
        tryGenerateVillage(level, centerPos, random);
    }

    /**
     * 尝试在指定位置生成村庄
     *
     * @param level 服务器世界
     * @param targetPos 目标位置
     * @param random 随机源
     * @return 生成的村庄，如果失败返回null
     */
    @Nullable
    public Village tryGenerateVillage(ServerLevel level, BlockPos targetPos, RandomSource random) {
        // 检查出生点保护
        if (isWithinSpawnProtection(level, targetPos)) {
            return null;
        }

        // 检查与其他村庄的距离
        VillageManager manager = VillageManager.get(level);
        if (manager == null) {
            return null;
        }

        if (isTooCloseToExistingVillage(manager, targetPos)) {
            return null;
        }

        // 选择文化
        Culture culture = CultureSelector.selectCultureForBiome(level, targetPos);
        if (culture == null) {
            return null;
        }

        // 验证地形
        VillagePlacementCalculator calculator = new VillagePlacementCalculator(level);
        if (!calculator.isValidPlacement(targetPos, VILLAGE_RADIUS)) {
            return null;
        }

        // 找到合适的地面位置
        BlockPos groundPos = findGroundPosition(level, targetPos);
        if (groundPos == null) {
            return null;
        }

        // 生成村庄
        return generateVillage(level, groundPos, culture, random);
    }

    /**
     * 强制在指定位置生成村庄（用于命令或玩家放置）
     */
    @Nullable
    public Village forceGenerateVillage(ServerLevel level, BlockPos pos, Culture culture) {
        // 找到地面位置
        BlockPos groundPos = findGroundPosition(level, pos);
        if (groundPos == null) {
            groundPos = pos;
        }

        return generateVillage(level, groundPos, culture, level.random);
    }

    // ================ 私有方法 ================

    /**
     * 检查是否是主世界
     */
    private boolean isOverworld(ServerLevel level) {
        return level.dimension() == ServerLevel.OVERWORLD;
    }

    /**
     * 检查是否在出生点保护范围内
     */
    private boolean isWithinSpawnProtection(ServerLevel level, BlockPos pos) {
        BlockPos spawn = level.getSharedSpawnPos();
        double distance = Math.sqrt(pos.distSqr(spawn));
        return distance < SPAWN_PROTECTION_RADIUS;
    }

    /**
     * 检查是否距离现有村庄太近
     */
    private boolean isTooCloseToExistingVillage(VillageManager manager, BlockPos pos) {
        for (Village village : manager.getAllVillages()) {
            BlockPos villagePos = village.getTownHallPos();
            double distance = Math.sqrt(pos.distSqr(villagePos));
            if (distance < MIN_VILLAGE_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    /**
     * 找到地面位置
     */
    @Nullable
    private BlockPos findGroundPosition(ServerLevel level, BlockPos pos) {
        int maxY = level.getMaxBuildHeight() - 1;
        int minY = level.getMinBuildHeight();

        // 从高处向下找地面
        for (int y = Math.min(maxY, 256); y > minY; y--) {
            BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());

            if (isSolidGround(state) && !above.isSolid()) {
                return check.above();
            }
        }

        return null;
    }

    /**
     * 检查是否是有效的地面方块
     */
    private boolean isSolidGround(BlockState state) {
        // 检查常见的地面方块
        return state.isSolid() &&
               !state.is(Blocks.WATER) &&
               !state.is(Blocks.LAVA) &&
               !state.is(Blocks.BEDROCK);
    }

    /**
     * 生成村庄
     */
    @Nullable
    private Village generateVillage(ServerLevel level, BlockPos pos, Culture culture, RandomSource random) {
        try {
            // 创建村庄
            Village village = new Village(pos, culture);
            village.setDimension(level.dimension().location().toString());

            // 获取村庄管理器
            VillageManager manager = VillageManager.get(level);
            if (manager == null) {
                return null;
            }

            // 注册村庄
            manager.registerVillage(village);

            // 获取市政厅蓝图
            String townHallKey = culture.getId().toLowerCase() + "_town_hall";
            BuildingBlueprint townHallBlueprint = BuildingRegistry.getInstance().getBlueprint(townHallKey);

            if (townHallBlueprint != null) {
                // 创建市政厅建筑位置
                BuildingLocation townHallLocation = new BuildingLocation(pos, townHallKey);
                townHallLocation.setDisplayName(townHallBlueprint.getDisplayName());
                townHallLocation.setLevel(1);
                townHallLocation.setTownHall(true);
                townHallLocation.setDimensions(
                    townHallBlueprint.getLength(),
                    townHallBlueprint.getWidth(),
                    townHallBlueprint.getHeight()
                );

                // 添加功能位置
                for (BlockPos spot : townHallBlueprint.getSleepingSpots()) {
                    townHallLocation.addSleepingPosition(pos.offset(spot));
                }
                for (BlockPos spot : townHallBlueprint.getWorkSpots()) {
                    townHallLocation.addCraftingPosition(pos.offset(spot));
                }
                for (BlockPos spot : townHallBlueprint.getStorageSpots()) {
                    townHallLocation.addChestPosition(pos.offset(spot));
                }
                if (townHallBlueprint.getEntranceSpot() != null) {
                    townHallLocation.setEntrancePos(pos.offset(townHallBlueprint.getEntranceSpot()));
                }

                // 添加到村庄
                village.addBuilding(townHallLocation);

                // 放置市政厅方块结构
                placeBuilding(level, pos, townHallBlueprint);
            }

            // 生成初始村民
            spawnInitialVillagers(level, village, random);

            MillenaireRewrite.LOGGER.info("Generated {} village '{}' at {}",
                culture.getDisplayName(), village.getName(), pos);

            return village;

        } catch (Exception e) {
            MillenaireRewrite.LOGGER.error("Failed to generate village at {}: {}", pos, e.getMessage());
            return null;
        }
    }

    /**
     * 放置建筑方块
     */
    private void placeBuilding(ServerLevel level, BlockPos origin, BuildingBlueprint blueprint) {
        Map<BlockPos, BlockState> layout = blueprint.getBlockLayout();

        for (Map.Entry<BlockPos, BlockState> entry : layout.entrySet()) {
            BlockPos relativePos = entry.getKey();
            BlockState state = entry.getValue();

            if (state != null && !state.isAir()) {
                BlockPos worldPos = origin.offset(relativePos);
                level.setBlock(worldPos, state, 3);
            }
        }
    }

    /**
     * 生成初始村民
     */
    private void spawnInitialVillagers(ServerLevel level, Village village, RandomSource random) {
        // 由VillagerSpawner处理，这里只是触发
        village.getVillagerSpawner().setInitialSpawnPending(true);
    }

    /**
     * 清理检查缓存（当维度卸载时调用）
     */
    public void clearCache() {
        checkedChunks.clear();
        generationAttempts.clear();
    }

    /**
     * 清理特定维度的缓存
     */
    public void clearCacheForDimension(String dimension) {
        // 当前实现中不区分维度，直接清理
        // 可以在未来改进为按维度存储
    }
}
