package com.jasoncian.millenaire_rewrite.village;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.core.ModEntities;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 村民生成器 - 管理村庄的村民生成和分配
 *
 * 职责：
 * - 根据村庄需求生成新村民
 * - 分配村民到建筑（家、工作场所）
 * - 管理村民职业分配
 * - 控制村庄人口上限
 *
 * @author Based on OldSource villager spawning logic
 * @version 1.0.0
 */
public class VillagerSpawner {

    // ================ 配置常量 ================

    /** 每个建筑的最大村民数 */
    private static final int MAX_VILLAGERS_PER_BUILDING = 4;

    /** 基础村庄人口上限 */
    private static final int BASE_POPULATION_CAP = 6;

    /** 每个额外建筑增加的人口上限 */
    private static final int POPULATION_PER_BUILDING = 2;

    /** 生成冷却时间（ticks） */
    private static final int SPAWN_COOLDOWN = 6000; // 5分钟

    /** 最小生成间隔（ticks） */
    private static final int MIN_SPAWN_INTERVAL = 1200; // 1分钟

    // ================ 状态 ================

    /** 所属村庄 */
    private final Village village;

    /** 上次生成时间 */
    private long lastSpawnTime = 0;

    /** 生成队列（等待生成的职业） */
    private final Queue<VillagerProfession> spawnQueue = new LinkedList<>();

    // ================ 构造函数 ================

    public VillagerSpawner(Village village) {
        this.village = village;
    }

    // ================ 主要方法 ================

    /**
     * 每tick更新生成逻辑
     * 由Village.tick()调用
     */
    public void tick(ServerLevel level) {
        long currentTime = level.getGameTime();

        // 检查冷却
        if (currentTime - lastSpawnTime < MIN_SPAWN_INTERVAL) {
            return;
        }

        // 检查是否需要更多村民
        if (shouldSpawnVillager()) {
            // 确定要生成的职业
            VillagerProfession profession = determineNeededProfession();

            if (profession != null) {
                // 找到生成位置
                BlockPos spawnPos = findSpawnPosition(level);

                if (spawnPos != null) {
                    spawnVillager(level, spawnPos, profession);
                    lastSpawnTime = currentTime;
                }
            }
        }
    }

    /**
     * 检查是否应该生成新村民
     */
    private boolean shouldSpawnVillager() {
        int currentPop = village.getActiveVillagerCount();
        int maxPop = calculatePopulationCap();

        return currentPop < maxPop;
    }

    /**
     * 计算村庄人口上限
     */
    public int calculatePopulationCap() {
        int buildingCount = village.getBuildingCount();
        return BASE_POPULATION_CAP + (buildingCount * POPULATION_PER_BUILDING);
    }

    /**
     * 确定需要生成的职业
     */
    @Nullable
    private VillagerProfession determineNeededProfession() {
        // 统计当前各职业人数
        Map<VillagerProfession, Integer> professionCounts = new HashMap<>();
        for (VillagerProfession prof : VillagerProfession.values()) {
            professionCounts.put(prof, 0);
        }

        for (MillVillager villager : village.getActiveVillagers()) {
            VillagerProfession prof = villager.getProfession();
            professionCounts.merge(prof, 1, Integer::sum);
        }

        // 根据文化确定职业优先级
        Culture culture = village.getCulture();
        List<VillagerProfession> priorityList = getProfessionPriority(culture);

        // 找到最需要的职业
        for (VillagerProfession prof : priorityList) {
            int current = professionCounts.getOrDefault(prof, 0);
            int desired = getDesiredCount(prof, village.getBuildingCount());

            if (current < desired) {
                return prof;
            }
        }

        // 如果所有职业都满足需求，随机选择一个通用职业
        return VillagerProfession.FARMER;
    }

    /**
     * 获取文化的职业优先级列表
     */
    private List<VillagerProfession> getProfessionPriority(Culture culture) {
        List<VillagerProfession> priority = new ArrayList<>();

        // 所有文化都需要基础职业
        priority.add(VillagerProfession.FARMER);
        priority.add(VillagerProfession.LUMBERJACK);
        priority.add(VillagerProfession.MINER);

        // 根据文化添加特色职业
        switch (culture) {
            case NORMAN -> {
                priority.add(VillagerProfession.BLACKSMITH);
                priority.add(VillagerProfession.SOLDIER);
                priority.add(VillagerProfession.BAKER);
            }
            case JAPANESE -> {
                priority.add(VillagerProfession.SILK_FARMER);
                priority.add(VillagerProfession.MERCHANT);
                priority.add(VillagerProfession.SOLDIER);
            }
            case INDIAN -> {
                priority.add(VillagerProfession.MERCHANT);
                priority.add(VillagerProfession.FARMER);
                priority.add(VillagerProfession.PRIEST);
            }
            case MAYAN -> {
                priority.add(VillagerProfession.FARMER);
                priority.add(VillagerProfession.PRIEST);
                priority.add(VillagerProfession.SOLDIER);
            }
            case BYZANTINE -> {
                priority.add(VillagerProfession.MERCHANT);
                priority.add(VillagerProfession.BLACKSMITH);
                priority.add(VillagerProfession.SOLDIER);
            }
            default -> {
                priority.add(VillagerProfession.BLACKSMITH);
                priority.add(VillagerProfession.MERCHANT);
            }
        }

        return priority;
    }

    /**
     * 获取职业的期望数量
     */
    private int getDesiredCount(VillagerProfession profession, int buildingCount) {
        // 基础职业需要更多人
        return switch (profession) {
            case FARMER -> 2 + buildingCount / 3;
            case LUMBERJACK, MINER -> 1 + buildingCount / 4;
            case BLACKSMITH, MERCHANT -> 1 + buildingCount / 5;
            case SOLDIER -> buildingCount / 4;
            default -> 1;
        };
    }

    /**
     * 找到生成位置
     */
    @Nullable
    private BlockPos findSpawnPosition(ServerLevel level) {
        BlockPos townHall = village.getTownHallPos();

        // 优先在市政厅附近生成
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = level.random.nextInt(11) - 5;
            int dz = level.random.nextInt(11) - 5;
            BlockPos checkPos = townHall.offset(dx, 0, dz);

            // 找到地面
            BlockPos groundPos = findGroundLevel(level, checkPos);
            if (groundPos != null && isValidSpawnPos(level, groundPos)) {
                return groundPos;
            }
        }

        // 备选：在任意建筑附近生成
        for (BuildingLocation building : village.getAllBuildings()) {
            BlockPos buildingPos = building.getPos();
            for (int attempt = 0; attempt < 5; attempt++) {
                int dx = level.random.nextInt(7) - 3;
                int dz = level.random.nextInt(7) - 3;
                BlockPos checkPos = buildingPos.offset(dx, 0, dz);

                BlockPos groundPos = findGroundLevel(level, checkPos);
                if (groundPos != null && isValidSpawnPos(level, groundPos)) {
                    return groundPos;
                }
            }
        }

        return null;
    }

    /**
     * 找到地面高度
     */
    @Nullable
    private BlockPos findGroundLevel(ServerLevel level, BlockPos pos) {
        for (int y = 5; y >= -5; y--) {
            BlockPos checkPos = pos.offset(0, y, 0);
            if (level.getBlockState(checkPos).isSolid() &&
                level.getBlockState(checkPos.above()).isAir() &&
                level.getBlockState(checkPos.above(2)).isAir()) {
                return checkPos.above();
            }
        }
        return null;
    }

    /**
     * 检查是否是有效的生成位置
     */
    private boolean isValidSpawnPos(ServerLevel level, BlockPos pos) {
        // 检查是否在村庄范围内
        if (!village.isInVillageArea(pos)) {
            return false;
        }

        // 检查是否有足够空间
        return level.getBlockState(pos).isAir() &&
               level.getBlockState(pos.above()).isAir();
    }

    // ================ 生成村民 ================

    /**
     * 生成村民
     */
    @Nullable
    public MillVillager spawnVillager(ServerLevel level, BlockPos pos, VillagerProfession profession) {
        MillVillager villager = ModEntities.MILL_VILLAGER.get().create(level);

        if (villager == null) {
            return null;
        }

        // 设置位置
        villager.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
            level.random.nextFloat() * 360F, 0F);

        // 设置属性
        villager.setCulture(village.getCulture());
        villager.setProfession(profession);
        villager.setGender(level.random.nextBoolean() ? MillVillager.GENDER_MALE : MillVillager.GENDER_FEMALE);
        villager.setTownHallPos(village.getTownHallPos());

        // 分配家
        assignHome(villager);

        // 生成名字（在finalizeSpawn中处理）
        villager.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
            MobSpawnType.MOB_SUMMONED, null);

        // 添加到世界
        level.addFreshEntity(villager);

        // 注册到村庄
        village.registerVillager(villager);

        MillenaireRewrite.LOGGER.info("Spawned villager {} ({}) at {} for village {}",
            villager.getFullName(), profession.getId(), pos, village.getName());

        return villager;
    }

    /**
     * 为村民分配家
     */
    public void assignHome(MillVillager villager) {
        // 找到有空位的建筑
        for (BuildingLocation building : village.getAllBuildings()) {
            if (!building.isTownHall() && building.isBuilt()) {
                // 检查建筑是否有空间
                int residentsCount = countResidents(building);
                if (residentsCount < MAX_VILLAGERS_PER_BUILDING) {
                    villager.setHousePos(building.getPos());
                    MillenaireRewrite.LOGGER.debug("Assigned villager {} to building at {}",
                        villager.getFullName(), building.getPos());
                    return;
                }
            }
        }

        // 如果没有空闲建筑，分配到市政厅
        villager.setHousePos(village.getTownHallPos());
    }

    /**
     * 统计建筑的居民数量
     */
    private int countResidents(BuildingLocation building) {
        int count = 0;
        for (MillVillager villager : village.getActiveVillagers()) {
            BlockPos house = villager.getHousePos();
            if (house != null && house.equals(building.getPos())) {
                count++;
            }
        }
        return count;
    }

    // ================ 强制生成 ================

    /**
     * 强制生成指定职业的村民（用于测试/命令）
     */
    @Nullable
    public MillVillager forceSpawn(ServerLevel level, VillagerProfession profession) {
        BlockPos spawnPos = findSpawnPosition(level);
        if (spawnPos == null) {
            spawnPos = village.getTownHallPos().above();
        }
        return spawnVillager(level, spawnPos, profession);
    }

    /**
     * 强制生成指定数量的村民
     */
    public List<MillVillager> forceSpawnMultiple(ServerLevel level, int count) {
        List<MillVillager> spawned = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            VillagerProfession profession = determineNeededProfession();
            if (profession == null) {
                profession = VillagerProfession.FARMER;
            }

            MillVillager villager = forceSpawn(level, profession);
            if (villager != null) {
                spawned.add(villager);
            }
        }

        return spawned;
    }

    // ================ 统计信息 ================

    /**
     * 获取当前人口
     */
    public int getCurrentPopulation() {
        return village.getActiveVillagerCount();
    }

    /**
     * 获取人口上限
     */
    public int getPopulationCap() {
        return calculatePopulationCap();
    }

    /**
     * 检查是否可以生成更多村民
     */
    public boolean canSpawnMore() {
        return getCurrentPopulation() < getPopulationCap();
    }
}
