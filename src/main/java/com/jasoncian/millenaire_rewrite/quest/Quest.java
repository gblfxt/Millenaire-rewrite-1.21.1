package com.jasoncian.millenaire_rewrite.quest;

import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 任务类 - 表示一个具体的任务实例
 *
 * 包含：
 * - 任务元数据（类型、标题、描述）
 * - 任务目标列表
 * - 奖励定义
 * - 进度追踪
 *
 * @author Based on OldSource Quest
 * @version 1.0.0
 */
public class Quest {

    // ================ 基础属性 ================

    /** 任务唯一ID */
    private final UUID questId;

    /** 任务类型 */
    private final QuestType type;

    /** 任务标题 */
    private String title;

    /** 任务描述 */
    private String description;

    /** 来源村庄ID */
    private UUID villageId;

    /** 来源文化 */
    private Culture culture;

    /** 任务发布者（村民ID） */
    @Nullable
    private Long questGiverId;

    // ================ 目标 ================

    /** 任务目标列表 */
    private final List<QuestObjective> objectives = new ArrayList<>();

    // ================ 奖励 ================

    /** 物品奖励 */
    private final List<ItemStack> itemRewards = new ArrayList<>();

    /** 第纳尔奖励 */
    private int denierReward = 0;

    /** 声望奖励 */
    private int reputationReward = 0;

    // ================ 状态 ================

    /** 任务状态 */
    private QuestStatus status = QuestStatus.AVAILABLE;

    /** 接受时间 */
    private long acceptedTime = 0;

    /** 完成时间 */
    private long completedTime = 0;

    /** 时间限制（tick，0表示无限制） */
    private long timeLimit = 0;

    /** 最低声望要求 */
    private int minimumReputation = 0;

    // ================ 目标位置 ================

    /** 任务目标位置（可选） */
    @Nullable
    private BlockPos targetPos;

    /** 目标村庄ID（用于交付任务） */
    @Nullable
    private UUID targetVillageId;

    // ================ 构造函数 ================

    public Quest(QuestType type) {
        this.questId = UUID.randomUUID();
        this.type = type;
        this.title = type.getDisplayName();
        this.description = "";
        this.reputationReward = type.getBaseReputationReward();
    }

    public Quest(UUID questId, QuestType type) {
        this.questId = questId;
        this.type = type;
        this.title = type.getDisplayName();
        this.description = "";
        this.reputationReward = type.getBaseReputationReward();
    }

    // ================ 目标管理 ================

    /**
     * 添加目标
     */
    public void addObjective(QuestObjective objective) {
        objectives.add(objective);
    }

    /**
     * 获取所有目标
     */
    public List<QuestObjective> getObjectives() {
        return Collections.unmodifiableList(objectives);
    }

    /**
     * 检查所有目标是否完成
     */
    public boolean areAllObjectivesComplete() {
        for (QuestObjective objective : objectives) {
            if (!objective.isComplete()) {
                return false;
            }
        }
        return !objectives.isEmpty();
    }

    /**
     * 获取完成进度（0.0 - 1.0）
     */
    public double getProgress() {
        if (objectives.isEmpty()) return 0;

        double total = 0;
        for (QuestObjective objective : objectives) {
            total += objective.getProgress();
        }
        return total / objectives.size();
    }

    /**
     * 更新物品收集进度
     */
    public void updateItemProgress(Item item, int count) {
        for (QuestObjective objective : objectives) {
            if (objective.getType() == QuestObjective.ObjectiveType.COLLECT_ITEM) {
                if (objective.getTargetItem() == item) {
                    objective.addProgress(count);
                }
            }
        }
        checkCompletion();
    }

    /**
     * 更新击杀进度
     */
    public void updateKillProgress(String entityType, int count) {
        for (QuestObjective objective : objectives) {
            if (objective.getType() == QuestObjective.ObjectiveType.KILL_ENTITY) {
                if (entityType.equals(objective.getTargetEntityType())) {
                    objective.addProgress(count);
                }
            }
        }
        checkCompletion();
    }

    /**
     * 更新位置到达进度
     */
    public void updateLocationProgress(BlockPos pos) {
        for (QuestObjective objective : objectives) {
            if (objective.getType() == QuestObjective.ObjectiveType.REACH_LOCATION) {
                BlockPos target = objective.getTargetLocation();
                if (target != null && pos.closerThan(target, 10)) {
                    objective.setComplete(true);
                }
            }
        }
        checkCompletion();
    }

    /**
     * 检查并更新完成状态
     */
    private void checkCompletion() {
        if (status == QuestStatus.ACTIVE && areAllObjectivesComplete()) {
            status = QuestStatus.READY_TO_TURN_IN;
        }
    }

    // ================ 奖励管理 ================

    /**
     * 添加物品奖励
     */
    public void addItemReward(ItemStack stack) {
        itemRewards.add(stack.copy());
    }

    /**
     * 添加物品奖励
     */
    public void addItemReward(Item item, int count) {
        itemRewards.add(new ItemStack(item, count));
    }

    /**
     * 获取物品奖励
     */
    public List<ItemStack> getItemRewards() {
        return Collections.unmodifiableList(itemRewards);
    }

    // ================ 状态管理 ================

    /**
     * 接受任务
     */
    public boolean accept(long currentTime) {
        if (status != QuestStatus.AVAILABLE) {
            return false;
        }
        status = QuestStatus.ACTIVE;
        acceptedTime = currentTime;
        return true;
    }

    /**
     * 完成任务
     */
    public boolean complete(long currentTime) {
        if (status != QuestStatus.READY_TO_TURN_IN && status != QuestStatus.ACTIVE) {
            return false;
        }
        if (!areAllObjectivesComplete()) {
            return false;
        }
        status = QuestStatus.COMPLETED;
        completedTime = currentTime;
        return true;
    }

    /**
     * 放弃任务
     */
    public void abandon() {
        if (status == QuestStatus.ACTIVE || status == QuestStatus.READY_TO_TURN_IN) {
            status = QuestStatus.ABANDONED;
        }
    }

    /**
     * 任务失败
     */
    public void fail() {
        if (status == QuestStatus.ACTIVE) {
            status = QuestStatus.FAILED;
        }
    }

    /**
     * 检查是否超时
     */
    public boolean isExpired(long currentTime) {
        if (timeLimit <= 0 || acceptedTime <= 0) {
            return false;
        }
        return (currentTime - acceptedTime) > timeLimit;
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putUUID("QuestId", questId);
        tag.putString("Type", type.getId());
        tag.putString("Title", title);
        tag.putString("Description", description);
        tag.putString("Status", status.name());

        if (villageId != null) {
            tag.putUUID("VillageId", villageId);
        }
        if (culture != null) {
            tag.putString("Culture", culture.getId());
        }
        if (questGiverId != null) {
            tag.putLong("QuestGiverId", questGiverId);
        }

        // 保存目标
        ListTag objectiveList = new ListTag();
        for (QuestObjective objective : objectives) {
            objectiveList.add(objective.save());
        }
        tag.put("Objectives", objectiveList);

        // 保存奖励
        ListTag rewardList = new ListTag();
        for (ItemStack reward : itemRewards) {
            CompoundTag rewardTag = new CompoundTag();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(reward.getItem());
            rewardTag.putString("ItemId", itemId.toString());
            rewardTag.putInt("Count", reward.getCount());
            rewardList.add(rewardTag);
        }
        tag.put("ItemRewards", rewardList);

        tag.putInt("DenierReward", denierReward);
        tag.putInt("ReputationReward", reputationReward);
        tag.putLong("AcceptedTime", acceptedTime);
        tag.putLong("CompletedTime", completedTime);
        tag.putLong("TimeLimit", timeLimit);
        tag.putInt("MinimumReputation", minimumReputation);

        if (targetPos != null) {
            tag.putInt("TargetX", targetPos.getX());
            tag.putInt("TargetY", targetPos.getY());
            tag.putInt("TargetZ", targetPos.getZ());
        }
        if (targetVillageId != null) {
            tag.putUUID("TargetVillageId", targetVillageId);
        }

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static Quest fromNbt(CompoundTag tag) {
        UUID questId = tag.getUUID("QuestId");
        QuestType type = QuestType.fromId(tag.getString("Type"));
        Quest quest = new Quest(questId, type);

        quest.title = tag.getString("Title");
        quest.description = tag.getString("Description");
        quest.status = QuestStatus.valueOf(tag.getString("Status"));

        if (tag.contains("VillageId")) {
            quest.villageId = tag.getUUID("VillageId");
        }
        if (tag.contains("Culture")) {
            quest.culture = Culture.fromId(tag.getString("Culture"));
        }
        if (tag.contains("QuestGiverId")) {
            quest.questGiverId = tag.getLong("QuestGiverId");
        }

        // 加载目标
        ListTag objectiveList = tag.getList("Objectives", 10);
        for (int i = 0; i < objectiveList.size(); i++) {
            quest.objectives.add(QuestObjective.fromNbt(objectiveList.getCompound(i)));
        }

        // 加载奖励
        ListTag rewardList = tag.getList("ItemRewards", 10);
        for (int i = 0; i < rewardList.size(); i++) {
            CompoundTag rewardTag = rewardList.getCompound(i);
            String itemIdStr = rewardTag.getString("ItemId");
            int count = rewardTag.getInt("Count");
            ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);
            if (itemId != null) {
                Item item = BuiltInRegistries.ITEM.get(itemId);
                if (item != Items.AIR) {
                    quest.itemRewards.add(new ItemStack(item, count));
                }
            }
        }

        quest.denierReward = tag.getInt("DenierReward");
        quest.reputationReward = tag.getInt("ReputationReward");
        quest.acceptedTime = tag.getLong("AcceptedTime");
        quest.completedTime = tag.getLong("CompletedTime");
        quest.timeLimit = tag.getLong("TimeLimit");
        quest.minimumReputation = tag.getInt("MinimumReputation");

        if (tag.contains("TargetX")) {
            quest.targetPos = new BlockPos(
                tag.getInt("TargetX"),
                tag.getInt("TargetY"),
                tag.getInt("TargetZ")
            );
        }
        if (tag.contains("TargetVillageId")) {
            quest.targetVillageId = tag.getUUID("TargetVillageId");
        }

        return quest;
    }

    // ================ Getters/Setters ================

    public UUID getQuestId() {
        return questId;
    }

    public QuestType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getVillageId() {
        return villageId;
    }

    public void setVillageId(UUID villageId) {
        this.villageId = villageId;
    }

    public Culture getCulture() {
        return culture;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    @Nullable
    public Long getQuestGiverId() {
        return questGiverId;
    }

    public void setQuestGiverId(@Nullable Long questGiverId) {
        this.questGiverId = questGiverId;
    }

    public int getDenierReward() {
        return denierReward;
    }

    public void setDenierReward(int denierReward) {
        this.denierReward = denierReward;
    }

    public int getReputationReward() {
        return reputationReward;
    }

    public void setReputationReward(int reputationReward) {
        this.reputationReward = reputationReward;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public long getAcceptedTime() {
        return acceptedTime;
    }

    public long getCompletedTime() {
        return completedTime;
    }

    public long getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(long timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getMinimumReputation() {
        return minimumReputation;
    }

    public void setMinimumReputation(int minimumReputation) {
        this.minimumReputation = minimumReputation;
    }

    @Nullable
    public BlockPos getTargetPos() {
        return targetPos;
    }

    public void setTargetPos(@Nullable BlockPos targetPos) {
        this.targetPos = targetPos;
    }

    @Nullable
    public UUID getTargetVillageId() {
        return targetVillageId;
    }

    public void setTargetVillageId(@Nullable UUID targetVillageId) {
        this.targetVillageId = targetVillageId;
    }

    // ================ 任务状态枚举 ================

    public enum QuestStatus {
        /** 可用 - 等待接受 */
        AVAILABLE,
        /** 活跃 - 正在进行 */
        ACTIVE,
        /** 可交付 - 目标完成，等待交付 */
        READY_TO_TURN_IN,
        /** 已完成 */
        COMPLETED,
        /** 已放弃 */
        ABANDONED,
        /** 失败 */
        FAILED
    }

    @Override
    public String toString() {
        return "Quest{" +
            "title='" + title + '\'' +
            ", type=" + type +
            ", status=" + status +
            ", progress=" + String.format("%.0f%%", getProgress() * 100) +
            '}';
    }
}
