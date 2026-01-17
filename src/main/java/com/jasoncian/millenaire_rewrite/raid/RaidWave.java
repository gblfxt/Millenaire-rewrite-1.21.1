package com.jasoncian.millenaire_rewrite.raid;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;

/**
 * 袭击波次 - 定义和管理单个攻击波次
 *
 * 包含：
 * - 敌人类型和数量
 * - 生成位置
 * - 存活敌人跟踪
 *
 * @author Based on OldSource raid waves
 * @version 1.0.0
 */
public class RaidWave {

    // ================ 波次状态枚举 ================

    public enum WaveStatus {
        /** 等待开始 */
        PENDING,
        /** 正在生成敌人 */
        SPAWNING,
        /** 战斗进行中 */
        ACTIVE,
        /** 波次完成 */
        COMPLETED,
        /** 波次失败（敌人逃跑或其他原因） */
        FAILED
    }

    // ================ 数据 ================

    /** 波次编号（从1开始） */
    private final int waveNumber;

    /** 袭击类型 */
    private final RaidType raidType;

    /** 敌人定义（实体类型 -> 数量） */
    private final Map<EntityType<?>, Integer> enemyDefinitions = new LinkedHashMap<>();

    /** 已生成的敌人UUID列表 */
    private final Set<UUID> spawnedEnemies = new HashSet<>();

    /** 存活敌人UUID列表 */
    private final Set<UUID> aliveEnemies = new HashSet<>();

    /** 波次状态 */
    private WaveStatus status = WaveStatus.PENDING;

    /** 波次开始时间 */
    private long startTime = 0;

    /** 生成位置 */
    private BlockPos spawnCenter;

    /** 难度修正 */
    private float difficultyMultiplier = 1.0f;

    // ================ 构造函数 ================

    public RaidWave(int waveNumber, RaidType raidType) {
        this.waveNumber = waveNumber;
        this.raidType = raidType;

        // 根据袭击类型和波次生成敌人定义
        generateEnemyDefinitions();
    }

    // ================ 敌人定义生成 ================

    /**
     * 根据袭击类型生成敌人配置
     */
    private void generateEnemyDefinitions() {
        int baseCount = 3 + waveNumber * 2; // 基础数量随波次增加

        switch (raidType) {
            case ZOMBIE_SIEGE -> {
                enemyDefinitions.put(EntityType.ZOMBIE, baseCount);
                if (waveNumber >= 2) {
                    enemyDefinitions.put(EntityType.HUSK, baseCount / 2);
                }
                if (waveNumber >= 3) {
                    enemyDefinitions.put(EntityType.ZOMBIE_VILLAGER, 2);
                }
            }
            case SKELETON_ATTACK -> {
                enemyDefinitions.put(EntityType.SKELETON, baseCount);
                if (waveNumber >= 2) {
                    enemyDefinitions.put(EntityType.STRAY, baseCount / 2);
                }
            }
            case SPIDER_INVASION -> {
                enemyDefinitions.put(EntityType.SPIDER, baseCount);
                if (waveNumber >= 2) {
                    enemyDefinitions.put(EntityType.CAVE_SPIDER, baseCount / 2);
                }
            }
            case PILLAGER_RAID -> {
                enemyDefinitions.put(EntityType.PILLAGER, baseCount);
                if (waveNumber >= 2) {
                    enemyDefinitions.put(EntityType.VINDICATOR, baseCount / 2);
                }
                if (waveNumber >= 3) {
                    enemyDefinitions.put(EntityType.EVOKER, 1);
                }
                if (waveNumber >= 4) {
                    enemyDefinitions.put(EntityType.RAVAGER, 1);
                }
            }
            case WITCH_ATTACK -> {
                enemyDefinitions.put(EntityType.WITCH, Math.max(2, baseCount / 2));
                enemyDefinitions.put(EntityType.ZOMBIE, baseCount / 2);
            }
            case CREEPER_THREAT -> {
                enemyDefinitions.put(EntityType.CREEPER, baseCount);
            }
            case VILLAGE_RAID, BANDIT_GANG -> {
                enemyDefinitions.put(EntityType.VINDICATOR, baseCount);
                if (waveNumber >= 2) {
                    enemyDefinitions.put(EntityType.PILLAGER, baseCount / 2);
                }
                if (waveNumber >= 3) {
                    enemyDefinitions.put(EntityType.EVOKER, 1);
                }
            }
            case BORDER_SKIRMISH -> {
                enemyDefinitions.put(EntityType.VINDICATOR, Math.max(2, baseCount / 2));
            }
            case ENDER_CALAMITY -> {
                enemyDefinitions.put(EntityType.ENDERMAN, baseCount);
                if (waveNumber >= 2) {
                    enemyDefinitions.put(EntityType.ENDERMITE, baseCount);
                }
            }
        }
    }

    // ================ 波次执行 ================

    /**
     * 开始波次
     */
    public void start(ServerLevel level, BlockPos villageCenter, long currentTime) {
        this.status = WaveStatus.SPAWNING;
        this.startTime = currentTime;
        this.spawnCenter = findSpawnLocation(level, villageCenter);

        // 生成所有敌人
        spawnEnemies(level);

        this.status = WaveStatus.ACTIVE;
    }

    /**
     * 找到合适的生成位置
     */
    private BlockPos findSpawnLocation(ServerLevel level, BlockPos villageCenter) {
        Random random = new Random();

        // 在村庄边缘生成（30-50格距离）
        int distance = 30 + random.nextInt(20);
        double angle = random.nextDouble() * Math.PI * 2;

        int x = villageCenter.getX() + (int) (Math.cos(angle) * distance);
        int z = villageCenter.getZ() + (int) (Math.sin(angle) * distance);
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        return new BlockPos(x, y, z);
    }

    /**
     * 生成敌人
     */
    private void spawnEnemies(ServerLevel level) {
        Random random = new Random();

        for (Map.Entry<EntityType<?>, Integer> entry : enemyDefinitions.entrySet()) {
            EntityType<?> entityType = entry.getKey();
            int count = (int) (entry.getValue() * difficultyMultiplier);

            for (int i = 0; i < count; i++) {
                // 随机偏移生成位置
                int offsetX = random.nextInt(10) - 5;
                int offsetZ = random.nextInt(10) - 5;
                BlockPos spawnPos = spawnCenter.offset(offsetX, 0, offsetZ);

                // 调整Y坐标
                int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    spawnPos.getX(), spawnPos.getZ());
                spawnPos = new BlockPos(spawnPos.getX(), y, spawnPos.getZ());

                // 创建实体
                Entity entity = entityType.create(level);
                if (entity != null) {
                    entity.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

                    // 应用难度增强
                    if (entity instanceof Mob mob) {
                        applyDifficultyBonus(mob);
                        mob.setPersistenceRequired();
                    }

                    level.addFreshEntity(entity);
                    spawnedEnemies.add(entity.getUUID());
                    aliveEnemies.add(entity.getUUID());
                }
            }
        }
    }

    /**
     * 应用难度加成
     */
    private void applyDifficultyBonus(Mob mob) {
        // 根据波次增加生命值
        double healthBonus = 1.0 + (waveNumber - 1) * 0.2;
        var healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            double newHealth = healthAttr.getBaseValue() * healthBonus * difficultyMultiplier;
            healthAttr.setBaseValue(newHealth);
            mob.setHealth((float) newHealth);
        }

        // 根据波次增加攻击力
        double damageBonus = 1.0 + (waveNumber - 1) * 0.1;
        var damageAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(damageAttr.getBaseValue() * damageBonus);
        }
    }

    // ================ 波次更新 ================

    /**
     * 更新波次状态
     */
    public void tick(ServerLevel level) {
        if (status != WaveStatus.ACTIVE) {
            return;
        }

        // 检查存活敌人
        aliveEnemies.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });

        // 检查是否完成
        if (aliveEnemies.isEmpty()) {
            status = WaveStatus.COMPLETED;
        }
    }

    /**
     * 检查波次是否完成
     */
    public boolean isComplete() {
        return status == WaveStatus.COMPLETED;
    }

    /**
     * 检查波次是否激活
     */
    public boolean isActive() {
        return status == WaveStatus.ACTIVE;
    }

    /**
     * 获取存活敌人数量
     */
    public int getAliveEnemyCount() {
        return aliveEnemies.size();
    }

    /**
     * 获取总敌人数量
     */
    public int getTotalEnemyCount() {
        return spawnedEnemies.size();
    }

    /**
     * 获取进度（0.0 - 1.0）
     */
    public double getProgress() {
        if (spawnedEnemies.isEmpty()) return 0;
        int killed = spawnedEnemies.size() - aliveEnemies.size();
        return (double) killed / spawnedEnemies.size();
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putInt("WaveNumber", waveNumber);
        tag.putString("RaidType", raidType.getId());
        tag.putString("Status", status.name());
        tag.putLong("StartTime", startTime);
        tag.putFloat("DifficultyMultiplier", difficultyMultiplier);

        if (spawnCenter != null) {
            tag.putInt("SpawnX", spawnCenter.getX());
            tag.putInt("SpawnY", spawnCenter.getY());
            tag.putInt("SpawnZ", spawnCenter.getZ());
        }

        // 保存敌人UUID
        ListTag aliveList = new ListTag();
        for (UUID uuid : aliveEnemies) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("UUID", uuid);
            aliveList.add(uuidTag);
        }
        tag.put("AliveEnemies", aliveList);

        ListTag spawnedList = new ListTag();
        for (UUID uuid : spawnedEnemies) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("UUID", uuid);
            spawnedList.add(uuidTag);
        }
        tag.put("SpawnedEnemies", spawnedList);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static RaidWave fromNbt(CompoundTag tag) {
        int waveNumber = tag.getInt("WaveNumber");
        RaidType raidType = RaidType.fromId(tag.getString("RaidType"));

        RaidWave wave = new RaidWave(waveNumber, raidType);
        wave.status = WaveStatus.valueOf(tag.getString("Status"));
        wave.startTime = tag.getLong("StartTime");
        wave.difficultyMultiplier = tag.getFloat("DifficultyMultiplier");

        if (tag.contains("SpawnX")) {
            wave.spawnCenter = new BlockPos(
                tag.getInt("SpawnX"),
                tag.getInt("SpawnY"),
                tag.getInt("SpawnZ")
            );
        }

        ListTag aliveList = tag.getList("AliveEnemies", 10);
        for (int i = 0; i < aliveList.size(); i++) {
            wave.aliveEnemies.add(aliveList.getCompound(i).getUUID("UUID"));
        }

        ListTag spawnedList = tag.getList("SpawnedEnemies", 10);
        for (int i = 0; i < spawnedList.size(); i++) {
            wave.spawnedEnemies.add(spawnedList.getCompound(i).getUUID("UUID"));
        }

        return wave;
    }

    // ================ Getters/Setters ================

    public int getWaveNumber() {
        return waveNumber;
    }

    public RaidType getRaidType() {
        return raidType;
    }

    public WaveStatus getStatus() {
        return status;
    }

    public long getStartTime() {
        return startTime;
    }

    public BlockPos getSpawnCenter() {
        return spawnCenter;
    }

    public float getDifficultyMultiplier() {
        return difficultyMultiplier;
    }

    public void setDifficultyMultiplier(float multiplier) {
        this.difficultyMultiplier = multiplier;
    }

    public Set<UUID> getAliveEnemies() {
        return Collections.unmodifiableSet(aliveEnemies);
    }

    @Override
    public String toString() {
        return "RaidWave{" +
            "wave=" + waveNumber +
            ", type=" + raidType +
            ", status=" + status +
            ", alive=" + aliveEnemies.size() + "/" + spawnedEnemies.size() +
            '}';
    }
}
