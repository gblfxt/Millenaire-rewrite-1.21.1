package com.jasoncian.millenaire_rewrite.quest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * 任务目标 - 表示单个任务目标
 *
 * 支持多种目标类型：
 * - 收集物品
 * - 击杀生物
 * - 到达地点
 * - 与村民交谈
 * - 交付物品
 *
 * @author Based on OldSource quest objectives
 * @version 1.0.0
 */
public class QuestObjective {

    // ================ 目标类型枚举 ================

    public enum ObjectiveType {
        /** 收集指定物品 */
        COLLECT_ITEM("collect_item", "Collect"),

        /** 交付物品给NPC */
        DELIVER_ITEM("deliver_item", "Deliver"),

        /** 击杀指定生物 */
        KILL_ENTITY("kill_entity", "Kill"),

        /** 到达指定地点 */
        REACH_LOCATION("reach_location", "Reach"),

        /** 与指定村民交谈 */
        TALK_TO_VILLAGER("talk_to_villager", "Talk to"),

        /** 保护目标（一定时间内不被摧毁） */
        PROTECT("protect", "Protect"),

        /** 护送村民到目的地 */
        ESCORT("escort", "Escort"),

        /** 建造/提供建筑材料 */
        BUILD("build", "Build"),

        /** 自定义目标（脚本扩展用） */
        CUSTOM("custom", "Complete");

        private final String id;
        private final String displayPrefix;

        ObjectiveType(String id, String displayPrefix) {
            this.id = id;
            this.displayPrefix = displayPrefix;
        }

        public String getId() {
            return id;
        }

        public String getDisplayPrefix() {
            return displayPrefix;
        }

        public static ObjectiveType fromId(String id) {
            for (ObjectiveType type : values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            return CUSTOM;
        }
    }

    // ================ 数据 ================

    /** 目标类型 */
    private final ObjectiveType type;

    /** 目标描述 */
    private String description;

    /** 当前进度 */
    private int currentProgress;

    /** 目标数量 */
    private int targetAmount;

    /** 是否完成 */
    private boolean complete;

    // ================ 目标特定数据 ================

    /** 目标物品（收集/交付） */
    @Nullable
    private Item targetItem;

    /** 目标生物类型（击杀） */
    @Nullable
    private String targetEntityType;

    /** 目标位置（到达/护送目的地） */
    @Nullable
    private BlockPos targetLocation;

    /** 目标村民ID（交谈/护送） */
    @Nullable
    private Long targetVillagerId;

    /** 目标建筑类型（建造） */
    @Nullable
    private String targetBuildingType;

    // ================ 构造函数 ================

    public QuestObjective(ObjectiveType type) {
        this.type = type;
        this.description = "";
        this.currentProgress = 0;
        this.targetAmount = 1;
        this.complete = false;
    }

    // ================ 工厂方法 ================

    /**
     * 创建收集物品目标
     */
    public static QuestObjective collectItem(Item item, int amount) {
        QuestObjective objective = new QuestObjective(ObjectiveType.COLLECT_ITEM);
        objective.targetItem = item;
        objective.targetAmount = amount;
        objective.description = "Collect " + amount + "x " + getItemName(item);
        return objective;
    }

    /**
     * 创建交付物品目标
     */
    public static QuestObjective deliverItem(Item item, int amount, Long villagerId) {
        QuestObjective objective = new QuestObjective(ObjectiveType.DELIVER_ITEM);
        objective.targetItem = item;
        objective.targetAmount = amount;
        objective.targetVillagerId = villagerId;
        objective.description = "Deliver " + amount + "x " + getItemName(item);
        return objective;
    }

    /**
     * 创建击杀生物目标
     */
    public static QuestObjective killEntity(String entityType, int amount) {
        QuestObjective objective = new QuestObjective(ObjectiveType.KILL_ENTITY);
        objective.targetEntityType = entityType;
        objective.targetAmount = amount;
        objective.description = "Kill " + amount + " " + formatEntityName(entityType);
        return objective;
    }

    /**
     * 创建到达地点目标
     */
    public static QuestObjective reachLocation(BlockPos location, String locationName) {
        QuestObjective objective = new QuestObjective(ObjectiveType.REACH_LOCATION);
        objective.targetLocation = location;
        objective.targetAmount = 1;
        objective.description = "Reach " + locationName;
        return objective;
    }

    /**
     * 创建交谈目标
     */
    public static QuestObjective talkToVillager(Long villagerId, String villagerName) {
        QuestObjective objective = new QuestObjective(ObjectiveType.TALK_TO_VILLAGER);
        objective.targetVillagerId = villagerId;
        objective.targetAmount = 1;
        objective.description = "Talk to " + villagerName;
        return objective;
    }

    /**
     * 创建护送目标
     */
    public static QuestObjective escort(Long villagerId, BlockPos destination, String villagerName) {
        QuestObjective objective = new QuestObjective(ObjectiveType.ESCORT);
        objective.targetVillagerId = villagerId;
        objective.targetLocation = destination;
        objective.targetAmount = 1;
        objective.description = "Escort " + villagerName + " to destination";
        return objective;
    }

    /**
     * 创建建造目标
     */
    public static QuestObjective build(String buildingType, Item material, int amount) {
        QuestObjective objective = new QuestObjective(ObjectiveType.BUILD);
        objective.targetBuildingType = buildingType;
        objective.targetItem = material;
        objective.targetAmount = amount;
        objective.description = "Provide " + amount + "x " + getItemName(material) + " for " + buildingType;
        return objective;
    }

    // ================ 进度管理 ================

    /**
     * 添加进度
     */
    public void addProgress(int amount) {
        if (complete) return;

        currentProgress = Math.min(currentProgress + amount, targetAmount);
        if (currentProgress >= targetAmount) {
            complete = true;
        }
    }

    /**
     * 设置进度
     */
    public void setProgress(int progress) {
        this.currentProgress = Math.min(progress, targetAmount);
        if (currentProgress >= targetAmount) {
            complete = true;
        }
    }

    /**
     * 获取进度百分比（0.0 - 1.0）
     */
    public double getProgress() {
        if (targetAmount <= 0) return complete ? 1.0 : 0.0;
        return (double) currentProgress / targetAmount;
    }

    /**
     * 获取进度显示字符串
     */
    public String getProgressString() {
        if (type == ObjectiveType.REACH_LOCATION ||
            type == ObjectiveType.TALK_TO_VILLAGER) {
            return complete ? "Complete" : "Incomplete";
        }
        return currentProgress + "/" + targetAmount;
    }

    /**
     * 设置完成状态
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
        if (complete) {
            this.currentProgress = targetAmount;
        }
    }

    /**
     * 检查是否完成
     */
    public boolean isComplete() {
        return complete;
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString("Type", type.getId());
        tag.putString("Description", description);
        tag.putInt("CurrentProgress", currentProgress);
        tag.putInt("TargetAmount", targetAmount);
        tag.putBoolean("Complete", complete);

        if (targetItem != null) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(targetItem);
            tag.putString("TargetItem", itemId.toString());
        }
        if (targetEntityType != null) {
            tag.putString("TargetEntityType", targetEntityType);
        }
        if (targetLocation != null) {
            tag.putInt("TargetX", targetLocation.getX());
            tag.putInt("TargetY", targetLocation.getY());
            tag.putInt("TargetZ", targetLocation.getZ());
        }
        if (targetVillagerId != null) {
            tag.putLong("TargetVillagerId", targetVillagerId);
        }
        if (targetBuildingType != null) {
            tag.putString("TargetBuildingType", targetBuildingType);
        }

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static QuestObjective fromNbt(CompoundTag tag) {
        ObjectiveType type = ObjectiveType.fromId(tag.getString("Type"));
        QuestObjective objective = new QuestObjective(type);

        objective.description = tag.getString("Description");
        objective.currentProgress = tag.getInt("CurrentProgress");
        objective.targetAmount = tag.getInt("TargetAmount");
        objective.complete = tag.getBoolean("Complete");

        if (tag.contains("TargetItem")) {
            ResourceLocation itemId = ResourceLocation.tryParse(tag.getString("TargetItem"));
            if (itemId != null) {
                objective.targetItem = BuiltInRegistries.ITEM.get(itemId);
                if (objective.targetItem == Items.AIR) {
                    objective.targetItem = null;
                }
            }
        }
        if (tag.contains("TargetEntityType")) {
            objective.targetEntityType = tag.getString("TargetEntityType");
        }
        if (tag.contains("TargetX")) {
            objective.targetLocation = new BlockPos(
                tag.getInt("TargetX"),
                tag.getInt("TargetY"),
                tag.getInt("TargetZ")
            );
        }
        if (tag.contains("TargetVillagerId")) {
            objective.targetVillagerId = tag.getLong("TargetVillagerId");
        }
        if (tag.contains("TargetBuildingType")) {
            objective.targetBuildingType = tag.getString("TargetBuildingType");
        }

        return objective;
    }

    // ================ 工具方法 ================

    /**
     * 获取物品显示名称
     */
    private static String getItemName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).getPath().replace("_", " ");
    }

    /**
     * 格式化生物类型名称
     */
    private static String formatEntityName(String entityType) {
        // minecraft:zombie -> Zombies
        String name = entityType;
        if (name.contains(":")) {
            name = name.substring(name.indexOf(":") + 1);
        }
        name = name.replace("_", " ");
        // Capitalize first letter
        if (!name.isEmpty()) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name + "s"; // Pluralize
    }

    // ================ Getters ================

    public ObjectiveType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(int targetAmount) {
        this.targetAmount = targetAmount;
    }

    @Nullable
    public Item getTargetItem() {
        return targetItem;
    }

    @Nullable
    public String getTargetEntityType() {
        return targetEntityType;
    }

    @Nullable
    public BlockPos getTargetLocation() {
        return targetLocation;
    }

    @Nullable
    public Long getTargetVillagerId() {
        return targetVillagerId;
    }

    @Nullable
    public String getTargetBuildingType() {
        return targetBuildingType;
    }

    @Override
    public String toString() {
        return "QuestObjective{" +
            "type=" + type +
            ", description='" + description + '\'' +
            ", progress=" + getProgressString() +
            ", complete=" + complete +
            '}';
    }
}
