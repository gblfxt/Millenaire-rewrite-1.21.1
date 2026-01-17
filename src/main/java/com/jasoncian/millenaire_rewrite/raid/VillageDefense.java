package com.jasoncian.millenaire_rewrite.raid;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 村庄防御系统 - 管理村庄的防御机制
 *
 * 功能：
 * - 警戒状态管理
 * - 守卫分配
 * - 防御优先级
 * - 村民避难
 *
 * @author Based on OldSource defense system
 * @version 1.0.0
 */
public class VillageDefense {

    // ================ 警戒状态枚举 ================

    public enum AlertLevel {
        /** 和平 - 无威胁 */
        PEACEFUL(0, "Peaceful", 0),
        /** 警觉 - 发现可疑目标 */
        ALERT(1, "Alert", 50),
        /** 警告 - 威胁接近 */
        WARNING(2, "Warning", 100),
        /** 危险 - 正在受到攻击 */
        DANGER(3, "Danger", 150),
        /** 紧急 - 袭击进行中 */
        EMERGENCY(4, "Emergency", 200);

        private final int level;
        private final String displayName;
        private final int detectionRange;

        AlertLevel(int level, String displayName, int detectionRange) {
            this.level = level;
            this.displayName = displayName;
            this.detectionRange = detectionRange;
        }

        public int getLevel() {
            return level;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getDetectionRange() {
            return detectionRange;
        }
    }

    // ================ 常量 ================

    /** 威胁检测间隔（tick） */
    private static final int DETECTION_INTERVAL = 20;

    /** 威胁消失后降级延迟（tick） */
    private static final int ALERT_DECAY_DELAY = 200;

    /** 默认巡逻范围 */
    private static final int DEFAULT_PATROL_RANGE = 32;

    // ================ 数据 ================

    /** 所属村庄 */
    private final Village village;

    /** 当前警戒等级 */
    private AlertLevel alertLevel = AlertLevel.PEACEFUL;

    /** 警戒等级变化时间 */
    private long alertChangeTime = 0;

    /** 守卫村民列表 */
    private final Set<Long> guards = new HashSet<>();

    /** 当前检测到的威胁 */
    private final Set<UUID> detectedThreats = new HashSet<>();

    /** 巡逻点列表 */
    private final List<BlockPos> patrolPoints = new ArrayList<>();

    /** 当前巡逻点索引（每个守卫） */
    private final Map<Long, Integer> guardPatrolIndex = new HashMap<>();

    /** 避难点 */
    private BlockPos shelterPoint;

    /** 集结点 */
    private BlockPos rallyPoint;

    // ================ 构造函数 ================

    public VillageDefense(Village village) {
        this.village = village;
        initializeDefensePoints();
    }

    /**
     * 初始化防御点
     */
    private void initializeDefensePoints() {
        BlockPos center = village.getCenterPos();
        if (center == null) return;

        // 设置默认避难点（村庄中心附近的建筑内）
        this.shelterPoint = center;

        // 设置默认集结点（村庄中心）
        this.rallyPoint = center;

        // 生成默认巡逻点（村庄周围的四个方向）
        int range = DEFAULT_PATROL_RANGE;
        patrolPoints.add(center.offset(range, 0, 0));
        patrolPoints.add(center.offset(0, 0, range));
        patrolPoints.add(center.offset(-range, 0, 0));
        patrolPoints.add(center.offset(0, 0, -range));
    }

    // ================ 威胁检测 ================

    /**
     * 每tick更新
     */
    public void tick(ServerLevel level) {
        long currentTime = level.getGameTime();

        // 定期检测威胁
        if (currentTime % DETECTION_INTERVAL == 0) {
            detectThreats(level);
            updateAlertLevel(level, currentTime);
        }

        // 根据警戒等级执行行动
        if (alertLevel.getLevel() >= AlertLevel.WARNING.getLevel()) {
            assignDefenders(level);
        }
    }

    /**
     * 检测威胁
     */
    private void detectThreats(ServerLevel level) {
        detectedThreats.clear();

        BlockPos center = village.getCenterPos();
        if (center == null) return;

        // 检测范围
        int range = Math.max(DEFAULT_PATROL_RANGE, alertLevel.getDetectionRange());
        AABB searchBox = new AABB(
            center.getX() - range, center.getY() - 20, center.getZ() - range,
            center.getX() + range, center.getY() + 20, center.getZ() + range
        );

        // 查找敌对生物
        List<Mob> hostileMobs = level.getEntitiesOfClass(Mob.class, searchBox, this::isHostile);

        for (Mob mob : hostileMobs) {
            detectedThreats.add(mob.getUUID());
        }
    }

    /**
     * 判断生物是否敌对
     */
    private boolean isHostile(Mob mob) {
        // 检查是否为敌对生物类型
        return mob.getType().getCategory() == net.minecraft.world.entity.MobCategory.MONSTER;
    }

    /**
     * 更新警戒等级
     */
    private void updateAlertLevel(ServerLevel level, long currentTime) {
        AlertLevel previousLevel = alertLevel;

        // 检查是否有活跃袭击
        RaidManager raidManager = RaidManager.get(level);
        if (raidManager.hasActiveRaid(village.getVillageId())) {
            alertLevel = AlertLevel.EMERGENCY;
        }
        // 根据威胁数量调整等级
        else if (detectedThreats.size() >= 10) {
            alertLevel = AlertLevel.DANGER;
        } else if (detectedThreats.size() >= 5) {
            alertLevel = AlertLevel.WARNING;
        } else if (detectedThreats.size() >= 1) {
            alertLevel = AlertLevel.ALERT;
        } else {
            // 无威胁，逐渐降级
            if (currentTime - alertChangeTime > ALERT_DECAY_DELAY) {
                if (alertLevel.getLevel() > AlertLevel.PEACEFUL.getLevel()) {
                    alertLevel = AlertLevel.values()[alertLevel.ordinal() - 1];
                }
            }
        }

        // 记录等级变化
        if (previousLevel != alertLevel) {
            alertChangeTime = currentTime;
            onAlertLevelChanged(previousLevel, alertLevel);
        }
    }

    /**
     * 警戒等级变化处理
     */
    private void onAlertLevelChanged(AlertLevel previous, AlertLevel current) {
        MillenaireRewrite.LOGGER.debug("Village {} alert level changed: {} -> {}",
            village.getName(), previous.getDisplayName(), current.getDisplayName());

        if (current.getLevel() >= AlertLevel.DANGER.getLevel()) {
            // 触发紧急响应
            triggerEmergencyResponse();
        } else if (current == AlertLevel.PEACEFUL && previous.getLevel() >= AlertLevel.WARNING.getLevel()) {
            // 解除警报
            endEmergencyResponse();
        }
    }

    // ================ 守卫管理 ================

    /**
     * 注册守卫
     */
    public void registerGuard(MillVillager villager) {
        guards.add(villager.getVillagerId());
        guardPatrolIndex.put(villager.getVillagerId(), 0);
    }

    /**
     * 取消注册守卫
     */
    public void unregisterGuard(MillVillager villager) {
        guards.remove(villager.getVillagerId());
        guardPatrolIndex.remove(villager.getVillagerId());
    }

    /**
     * 检查是否为守卫
     */
    public boolean isGuard(MillVillager villager) {
        return guards.contains(villager.getVillagerId());
    }

    /**
     * 获取守卫数量
     */
    public int getGuardCount() {
        return guards.size();
    }

    /**
     * 分配防御任务
     */
    private void assignDefenders(ServerLevel level) {
        // 在紧急情况下，所有守卫移动到集结点或威胁位置
        if (alertLevel == AlertLevel.EMERGENCY) {
            // 守卫应该攻击最近的威胁
            // 这在村民AI中处理
        }
    }

    /**
     * 获取守卫的下一个巡逻点
     */
    public BlockPos getNextPatrolPoint(MillVillager guard) {
        if (patrolPoints.isEmpty()) {
            return village.getCenterPos();
        }

        int currentIndex = guardPatrolIndex.getOrDefault(guard.getVillagerId(), 0);
        BlockPos point = patrolPoints.get(currentIndex);

        // 更新到下一个点
        int nextIndex = (currentIndex + 1) % patrolPoints.size();
        guardPatrolIndex.put(guard.getVillagerId(), nextIndex);

        return point;
    }

    // ================ 紧急响应 ================

    /**
     * 触发紧急响应
     */
    private void triggerEmergencyResponse() {
        MillenaireRewrite.LOGGER.info("Village {} triggered emergency response!", village.getName());

        // 非战斗村民应该躲避
        // 守卫应该集结
        // 这些行为在村民AI中实现
    }

    /**
     * 结束紧急响应
     */
    private void endEmergencyResponse() {
        MillenaireRewrite.LOGGER.info("Village {} emergency response ended", village.getName());
    }

    /**
     * 触发袭击警报
     */
    public void triggerRaidAlert(Raid raid) {
        alertLevel = AlertLevel.EMERGENCY;
        alertChangeTime = 0; // 防止自动降级

        MillenaireRewrite.LOGGER.info("Raid alert triggered for village {}", village.getName());
    }

    /**
     * 清除袭击警报
     */
    public void clearRaidAlert() {
        alertChangeTime = village.getCenterPos() != null ? 0 : System.currentTimeMillis();
        // 将在下次检测时自动降级
    }

    // ================ 威胁目标 ================

    /**
     * 获取最近的威胁
     */
    @Nullable
    public LivingEntity getNearestThreat(ServerLevel level, BlockPos from) {
        LivingEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (UUID threatId : detectedThreats) {
            Entity entity = level.getEntity(threatId);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                double dist = entity.blockPosition().distSqr(from);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = living;
                }
            }
        }

        return nearest;
    }

    /**
     * 获取所有威胁
     */
    public List<LivingEntity> getAllThreats(ServerLevel level) {
        List<LivingEntity> threats = new ArrayList<>();
        for (UUID threatId : detectedThreats) {
            Entity entity = level.getEntity(threatId);
            if (entity instanceof LivingEntity living && living.isAlive()) {
                threats.add(living);
            }
        }
        return threats;
    }

    /**
     * 获取威胁数量
     */
    public int getThreatCount() {
        return detectedThreats.size();
    }

    // ================ 防御点管理 ================

    /**
     * 添加巡逻点
     */
    public void addPatrolPoint(BlockPos pos) {
        patrolPoints.add(pos);
    }

    /**
     * 移除巡逻点
     */
    public void removePatrolPoint(BlockPos pos) {
        patrolPoints.remove(pos);
    }

    /**
     * 设置集结点
     */
    public void setRallyPoint(BlockPos pos) {
        this.rallyPoint = pos;
    }

    /**
     * 设置避难点
     */
    public void setShelterPoint(BlockPos pos) {
        this.shelterPoint = pos;
    }

    // ================ 防御评估 ================

    /**
     * 计算防御力量
     */
    public int calculateDefenseStrength() {
        int strength = 0;

        // 守卫贡献
        strength += guards.size() * 10;

        // 村庄建筑贡献
        strength += village.getBuildingCount() * 2;

        // 警戒等级贡献
        strength += alertLevel.getLevel() * 5;

        return strength;
    }

    /**
     * 评估是否能抵御袭击
     */
    public boolean canDefendAgainst(RaidType raidType) {
        int defenseStrength = calculateDefenseStrength();
        return defenseStrength >= raidType.getBaseDifficulty() * 0.5;
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("AlertLevel", alertLevel.name());
        tag.putLong("AlertChangeTime", alertChangeTime);

        // 保存守卫
        ListTag guardList = new ListTag();
        for (Long guardId : guards) {
            CompoundTag guardTag = new CompoundTag();
            guardTag.putLong("Id", guardId);
            guardList.add(guardTag);
        }
        tag.put("Guards", guardList);

        // 保存巡逻点
        ListTag patrolList = new ListTag();
        for (BlockPos pos : patrolPoints) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", pos.getX());
            posTag.putInt("Y", pos.getY());
            posTag.putInt("Z", pos.getZ());
            patrolList.add(posTag);
        }
        tag.put("PatrolPoints", patrolList);

        if (shelterPoint != null) {
            tag.putInt("ShelterX", shelterPoint.getX());
            tag.putInt("ShelterY", shelterPoint.getY());
            tag.putInt("ShelterZ", shelterPoint.getZ());
        }

        if (rallyPoint != null) {
            tag.putInt("RallyX", rallyPoint.getX());
            tag.putInt("RallyY", rallyPoint.getY());
            tag.putInt("RallyZ", rallyPoint.getZ());
        }

        return tag;
    }

    /**
     * 从NBT加载
     */
    public void load(CompoundTag tag) {
        alertLevel = AlertLevel.valueOf(tag.getString("AlertLevel"));
        alertChangeTime = tag.getLong("AlertChangeTime");

        // 加载守卫
        guards.clear();
        ListTag guardList = tag.getList("Guards", 10);
        for (int i = 0; i < guardList.size(); i++) {
            guards.add(guardList.getCompound(i).getLong("Id"));
        }

        // 加载巡逻点
        patrolPoints.clear();
        ListTag patrolList = tag.getList("PatrolPoints", 10);
        for (int i = 0; i < patrolList.size(); i++) {
            CompoundTag posTag = patrolList.getCompound(i);
            patrolPoints.add(new BlockPos(
                posTag.getInt("X"),
                posTag.getInt("Y"),
                posTag.getInt("Z")
            ));
        }

        if (tag.contains("ShelterX")) {
            shelterPoint = new BlockPos(
                tag.getInt("ShelterX"),
                tag.getInt("ShelterY"),
                tag.getInt("ShelterZ")
            );
        }

        if (tag.contains("RallyX")) {
            rallyPoint = new BlockPos(
                tag.getInt("RallyX"),
                tag.getInt("RallyY"),
                tag.getInt("RallyZ")
            );
        }
    }

    // ================ Getters ================

    public Village getVillage() {
        return village;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public BlockPos getShelterPoint() {
        return shelterPoint;
    }

    public BlockPos getRallyPoint() {
        return rallyPoint;
    }

    public List<BlockPos> getPatrolPoints() {
        return Collections.unmodifiableList(patrolPoints);
    }

    public Set<UUID> getDetectedThreats() {
        return Collections.unmodifiableSet(detectedThreats);
    }

    @Override
    public String toString() {
        return "VillageDefense{" +
            "village=" + village.getName() +
            ", alertLevel=" + alertLevel +
            ", guards=" + guards.size() +
            ", threats=" + detectedThreats.size() +
            '}';
    }
}
