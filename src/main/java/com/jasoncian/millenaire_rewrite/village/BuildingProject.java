package com.jasoncian.millenaire_rewrite.village;

import com.jasoncian.millenaire_rewrite.item.InvItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 建造项目 - 表示一个待建造或升级的建筑
 *
 * 基于OldSource BuildingProject.java
 * 用于：
 * - 追踪建造进度
 * - 管理所需资源
 * - 控制建造优先级
 *
 * @author Based on OldSource BuildingProject
 * @version 1.0.0
 */
public class BuildingProject {

    // ================ 项目类型 ================

    /**
     * 项目等级/类型
     * 决定建造优先级
     */
    public enum ProjectTier {
        CENTRE(0, "中心建筑"),      // 市政厅等核心建筑
        START(1, "起始建筑"),       // 初始基础建筑
        PLAYER(2, "玩家指定"),      // 玩家请求的建筑
        CORE(3, "核心设施"),        // 必要的功能建筑
        SECONDARY(4, "次要设施"),   // 辅助建筑
        EXTRA(5, "额外建筑"),       // 可选装饰建筑
        CUSTOM(6, "自定义建筑"),    // 玩家自定义建筑
        WALL(7, "防御设施");        // 城墙等防御建筑

        private final int priority;
        private final String displayName;

        ProjectTier(int priority, String displayName) {
            this.priority = priority;
            this.displayName = displayName;
        }

        public int getPriority() {
            return priority;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ================ 基础属性 ================

    /** 项目唯一ID */
    private final long projectId;

    /** 建筑模板键 */
    private String planKey;

    /** 建筑显示名称 */
    private String displayName;

    /** 目标位置 */
    private BlockPos targetPos;

    /** 建筑朝向 */
    private int orientation = 0;

    /** 项目等级 */
    private ProjectTier tier = ProjectTier.SECONDARY;

    /** 是否是升级项目（vs新建） */
    private boolean isUpgrade = false;

    /** 目标等级（升级时使用） */
    private int targetLevel = 1;

    // ================ 资源需求 ================

    /** 所需资源 */
    private final Map<InvItem, Integer> requiredResources = new HashMap<>();

    /** 已收集的资源 */
    private final Map<InvItem, Integer> collectedResources = new HashMap<>();

    // ================ 进度 ================

    /** 建造进度（0-100） */
    private int progress = 0;

    /** 是否已开始建造 */
    private boolean started = false;

    /** 是否已完成 */
    private boolean completed = false;

    /** 是否已取消 */
    private boolean cancelled = false;

    /** 创建时间 */
    private long createdTime;

    /** 开始建造时间 */
    private long startedTime = 0;

    /** 完成时间 */
    private long completedTime = 0;

    // ================ 构造函数 ================

    public BuildingProject(String planKey, BlockPos targetPos) {
        this.projectId = System.currentTimeMillis() ^ (long)(Math.random() * Long.MAX_VALUE);
        this.planKey = planKey;
        this.displayName = planKey;
        this.targetPos = targetPos;
        this.createdTime = System.currentTimeMillis();
    }

    /**
     * 从NBT恢复的构造函数
     */
    private BuildingProject(long id) {
        this.projectId = id;
    }

    // ================ 资源管理 ================

    /**
     * 添加所需资源
     */
    public void addRequiredResource(InvItem item, int count) {
        requiredResources.merge(item, count, Integer::sum);
    }

    /**
     * 添加所需资源（Item版本）
     */
    public void addRequiredResource(Item item, int count) {
        addRequiredResource(InvItem.create(item), count);
    }

    /**
     * 贡献资源到项目
     * @return 实际接受的数量
     */
    public int contributeResource(InvItem item, int count) {
        int required = requiredResources.getOrDefault(item, 0);
        int collected = collectedResources.getOrDefault(item, 0);
        int needed = required - collected;

        if (needed <= 0) return 0;

        int accepted = Math.min(count, needed);
        collectedResources.merge(item, accepted, Integer::sum);

        return accepted;
    }

    /**
     * 检查是否有足够资源开始建造
     */
    public boolean hasEnoughResources() {
        for (Map.Entry<InvItem, Integer> entry : requiredResources.entrySet()) {
            int required = entry.getValue();
            int collected = collectedResources.getOrDefault(entry.getKey(), 0);
            if (collected < required) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取资源收集进度（0-100）
     */
    public int getResourceProgress() {
        if (requiredResources.isEmpty()) return 100;

        int totalRequired = 0;
        int totalCollected = 0;

        for (Map.Entry<InvItem, Integer> entry : requiredResources.entrySet()) {
            totalRequired += entry.getValue();
            totalCollected += Math.min(
                collectedResources.getOrDefault(entry.getKey(), 0),
                entry.getValue());
        }

        if (totalRequired == 0) return 100;
        return (totalCollected * 100) / totalRequired;
    }

    /**
     * 获取仍需的资源
     */
    public Map<InvItem, Integer> getMissingResources() {
        Map<InvItem, Integer> missing = new HashMap<>();

        for (Map.Entry<InvItem, Integer> entry : requiredResources.entrySet()) {
            int required = entry.getValue();
            int collected = collectedResources.getOrDefault(entry.getKey(), 0);
            int needed = required - collected;
            if (needed > 0) {
                missing.put(entry.getKey(), needed);
            }
        }

        return missing;
    }

    // ================ 建造进度 ================

    /**
     * 开始建造
     */
    public void start() {
        if (!started && hasEnoughResources()) {
            started = true;
            startedTime = System.currentTimeMillis();
        }
    }

    /**
     * 增加建造进度
     */
    public void addProgress(int amount) {
        if (!started || completed || cancelled) return;

        progress = Math.min(100, progress + amount);
        if (progress >= 100) {
            complete();
        }
    }

    /**
     * 完成建造
     */
    public void complete() {
        if (!completed && !cancelled) {
            completed = true;
            progress = 100;
            completedTime = System.currentTimeMillis();
        }
    }

    /**
     * 取消项目
     */
    public void cancel() {
        if (!completed) {
            cancelled = true;
        }
    }

    // ================ 优先级比较 ================

    /**
     * 比较项目优先级
     * @return 负数表示this优先级更高
     */
    public int comparePriority(BuildingProject other) {
        // 首先按等级
        int tierCompare = this.tier.getPriority() - other.tier.getPriority();
        if (tierCompare != 0) return tierCompare;

        // 然后按创建时间（先创建的优先）
        return Long.compare(this.createdTime, other.createdTime);
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // 基础属性
        tag.putLong("ProjectId", projectId);
        tag.putString("PlanKey", planKey);
        tag.putString("DisplayName", displayName);
        tag.putInt("TargetX", targetPos.getX());
        tag.putInt("TargetY", targetPos.getY());
        tag.putInt("TargetZ", targetPos.getZ());
        tag.putInt("Orientation", orientation);
        tag.putString("Tier", tier.name());
        tag.putBoolean("IsUpgrade", isUpgrade);
        tag.putInt("TargetLevel", targetLevel);

        // 进度
        tag.putInt("Progress", progress);
        tag.putBoolean("Started", started);
        tag.putBoolean("Completed", completed);
        tag.putBoolean("Cancelled", cancelled);
        tag.putLong("CreatedTime", createdTime);
        tag.putLong("StartedTime", startedTime);
        tag.putLong("CompletedTime", completedTime);

        // 所需资源
        ListTag requiredTag = new ListTag();
        for (Map.Entry<InvItem, Integer> entry : requiredResources.entrySet()) {
            CompoundTag resTag = new CompoundTag();
            resTag.putString("Item", entry.getKey().getRegistryName().toString());
            resTag.putInt("Meta", entry.getKey().getMeta());
            resTag.putInt("Count", entry.getValue());
            requiredTag.add(resTag);
        }
        tag.put("RequiredResources", requiredTag);

        // 已收集资源
        ListTag collectedTag = new ListTag();
        for (Map.Entry<InvItem, Integer> entry : collectedResources.entrySet()) {
            CompoundTag resTag = new CompoundTag();
            resTag.putString("Item", entry.getKey().getRegistryName().toString());
            resTag.putInt("Meta", entry.getKey().getMeta());
            resTag.putInt("Count", entry.getValue());
            collectedTag.add(resTag);
        }
        tag.put("CollectedResources", collectedTag);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static BuildingProject load(CompoundTag tag) {
        BuildingProject project = new BuildingProject(tag.getLong("ProjectId"));

        // 基础属性
        project.planKey = tag.getString("PlanKey");
        project.displayName = tag.getString("DisplayName");
        project.targetPos = new BlockPos(
            tag.getInt("TargetX"),
            tag.getInt("TargetY"),
            tag.getInt("TargetZ"));
        project.orientation = tag.getInt("Orientation");
        project.tier = ProjectTier.valueOf(tag.getString("Tier"));
        project.isUpgrade = tag.getBoolean("IsUpgrade");
        project.targetLevel = tag.getInt("TargetLevel");

        // 进度
        project.progress = tag.getInt("Progress");
        project.started = tag.getBoolean("Started");
        project.completed = tag.getBoolean("Completed");
        project.cancelled = tag.getBoolean("Cancelled");
        project.createdTime = tag.getLong("CreatedTime");
        project.startedTime = tag.getLong("StartedTime");
        project.completedTime = tag.getLong("CompletedTime");

        // 加载资源
        loadResources(tag.getList("RequiredResources", 10), project.requiredResources);
        loadResources(tag.getList("CollectedResources", 10), project.collectedResources);

        return project;
    }

    private static void loadResources(ListTag tag, Map<InvItem, Integer> target) {
        target.clear();
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag resTag = tag.getCompound(i);
            String itemId = resTag.getString("Item");
            int meta = resTag.getInt("Meta");
            int count = resTag.getInt("Count");

            net.minecraft.resources.ResourceLocation loc =
                net.minecraft.resources.ResourceLocation.tryParse(itemId);
            if (loc != null) {
                Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(loc);
                if (item != null && item != Items.AIR) {
                    InvItem invItem = InvItem.create(item, meta);
                    if (invItem != null) {
                        target.put(invItem, count);
                    }
                }
            }
        }
    }

    // ================ Getters/Setters ================

    public long getProjectId() {
        return projectId;
    }

    public String getPlanKey() {
        return planKey;
    }

    public void setPlanKey(String planKey) {
        this.planKey = planKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public void setTargetPos(BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public ProjectTier getTier() {
        return tier;
    }

    public void setTier(ProjectTier tier) {
        this.tier = tier;
    }

    public boolean isUpgrade() {
        return isUpgrade;
    }

    public void setUpgrade(boolean upgrade) {
        isUpgrade = upgrade;
    }

    public int getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(int targetLevel) {
        this.targetLevel = targetLevel;
    }

    public Map<InvItem, Integer> getRequiredResources() {
        return new HashMap<>(requiredResources);
    }

    public Map<InvItem, Integer> getCollectedResources() {
        return new HashMap<>(collectedResources);
    }

    public int getProgress() {
        return progress;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isActive() {
        return started && !completed && !cancelled;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public long getStartedTime() {
        return startedTime;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    @Override
    public String toString() {
        return "BuildingProject{" +
            "planKey='" + planKey + '\'' +
            ", pos=" + targetPos +
            ", tier=" + tier +
            ", progress=" + progress + "%" +
            ", started=" + started +
            ", completed=" + completed +
            '}';
    }
}
