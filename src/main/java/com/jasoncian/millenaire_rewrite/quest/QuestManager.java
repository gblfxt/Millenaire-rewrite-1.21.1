package com.jasoncian.millenaire_rewrite.quest;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.reputation.ReputationManager;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务管理器 - 管理所有玩家的任务
 *
 * 功能：
 * - 存储和加载任务数据
 * - 跟踪活跃任务进度
 * - 处理任务完成和奖励
 * - 生成村庄任务
 *
 * @author Based on OldSource quest management
 * @version 1.0.0
 */
public class QuestManager extends SavedData {

    // ================ 常量 ================

    private static final String DATA_NAME = MillenaireRewrite.MOD_ID + "_quests";

    /** 每个玩家最大活跃任务数 */
    public static final int MAX_ACTIVE_QUESTS = 5;

    /** 每个村庄最大可用任务数 */
    public static final int MAX_AVAILABLE_QUESTS_PER_VILLAGE = 3;

    /** 任务刷新间隔（tick，约1小时） */
    public static final int QUEST_REFRESH_INTERVAL = 72000;

    // ================ 数据 ================

    /** 玩家活跃任务（玩家UUID -> 任务列表） */
    private final Map<UUID, List<Quest>> playerActiveQuests = new HashMap<>();

    /** 玩家已完成任务（玩家UUID -> 任务ID集合） */
    private final Map<UUID, Set<UUID>> playerCompletedQuests = new HashMap<>();

    /** 村庄可用任务（村庄UUID -> 任务列表） */
    private final Map<UUID, List<Quest>> villageAvailableQuests = new HashMap<>();

    /** 上次刷新时间 */
    private long lastRefreshTime = 0;

    // ================ 构造函数 ================

    public QuestManager() {
        // 默认构造
    }

    // ================ SavedData实现 ================

    /**
     * 从NBT加载
     */
    public static QuestManager load(CompoundTag tag, HolderLookup.Provider registries) {
        QuestManager manager = new QuestManager();

        // 加载玩家活跃任务
        ListTag playerQuestsList = tag.getList("PlayerActiveQuests", 10);
        for (int i = 0; i < playerQuestsList.size(); i++) {
            CompoundTag playerTag = playerQuestsList.getCompound(i);
            UUID playerId = playerTag.getUUID("PlayerId");
            List<Quest> quests = new ArrayList<>();

            ListTag questList = playerTag.getList("Quests", 10);
            for (int j = 0; j < questList.size(); j++) {
                quests.add(Quest.fromNbt(questList.getCompound(j)));
            }

            manager.playerActiveQuests.put(playerId, quests);
        }

        // 加载玩家已完成任务
        ListTag completedList = tag.getList("PlayerCompletedQuests", 10);
        for (int i = 0; i < completedList.size(); i++) {
            CompoundTag playerTag = completedList.getCompound(i);
            UUID playerId = playerTag.getUUID("PlayerId");
            Set<UUID> completed = new HashSet<>();

            ListTag questIds = playerTag.getList("QuestIds", 10);
            for (int j = 0; j < questIds.size(); j++) {
                completed.add(questIds.getCompound(j).getUUID("Id"));
            }

            manager.playerCompletedQuests.put(playerId, completed);
        }

        // 加载村庄可用任务
        ListTag villageQuestsList = tag.getList("VillageAvailableQuests", 10);
        for (int i = 0; i < villageQuestsList.size(); i++) {
            CompoundTag villageTag = villageQuestsList.getCompound(i);
            UUID villageId = villageTag.getUUID("VillageId");
            List<Quest> quests = new ArrayList<>();

            ListTag questList = villageTag.getList("Quests", 10);
            for (int j = 0; j < questList.size(); j++) {
                quests.add(Quest.fromNbt(questList.getCompound(j)));
            }

            manager.villageAvailableQuests.put(villageId, quests);
        }

        manager.lastRefreshTime = tag.getLong("LastRefreshTime");

        MillenaireRewrite.LOGGER.info("Loaded quest data for {} players, {} villages",
            manager.playerActiveQuests.size(), manager.villageAvailableQuests.size());

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        // 保存玩家活跃任务
        ListTag playerQuestsList = new ListTag();
        for (Map.Entry<UUID, List<Quest>> entry : playerActiveQuests.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("PlayerId", entry.getKey());

            ListTag questList = new ListTag();
            for (Quest quest : entry.getValue()) {
                questList.add(quest.save());
            }
            playerTag.put("Quests", questList);

            playerQuestsList.add(playerTag);
        }
        tag.put("PlayerActiveQuests", playerQuestsList);

        // 保存玩家已完成任务
        ListTag completedList = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : playerCompletedQuests.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("PlayerId", entry.getKey());

            ListTag questIds = new ListTag();
            for (UUID questId : entry.getValue()) {
                CompoundTag idTag = new CompoundTag();
                idTag.putUUID("Id", questId);
                questIds.add(idTag);
            }
            playerTag.put("QuestIds", questIds);

            completedList.add(playerTag);
        }
        tag.put("PlayerCompletedQuests", completedList);

        // 保存村庄可用任务
        ListTag villageQuestsList = new ListTag();
        for (Map.Entry<UUID, List<Quest>> entry : villageAvailableQuests.entrySet()) {
            CompoundTag villageTag = new CompoundTag();
            villageTag.putUUID("VillageId", entry.getKey());

            ListTag questList = new ListTag();
            for (Quest quest : entry.getValue()) {
                questList.add(quest.save());
            }
            villageTag.put("Quests", questList);

            villageQuestsList.add(villageTag);
        }
        tag.put("VillageAvailableQuests", villageQuestsList);

        tag.putLong("LastRefreshTime", lastRefreshTime);

        return tag;
    }

    // ================ 静态访问 ================

    /**
     * 获取维度的任务管理器
     */
    public static QuestManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(QuestManager::new, QuestManager::load),
            DATA_NAME
        );
    }

    // ================ 任务获取 ================

    /**
     * 获取玩家的活跃任务
     */
    public List<Quest> getActiveQuests(Player player) {
        return playerActiveQuests.getOrDefault(player.getUUID(), Collections.emptyList());
    }

    /**
     * 获取玩家在特定村庄的活跃任务
     */
    public List<Quest> getActiveQuestsForVillage(Player player, UUID villageId) {
        return getActiveQuests(player).stream()
            .filter(q -> villageId.equals(q.getVillageId()))
            .collect(Collectors.toList());
    }

    /**
     * 获取村庄的可用任务
     */
    public List<Quest> getAvailableQuests(UUID villageId) {
        return villageAvailableQuests.getOrDefault(villageId, Collections.emptyList());
    }

    /**
     * 获取玩家可以接受的村庄任务
     */
    public List<Quest> getAvailableQuestsForPlayer(Player player, Village village) {
        if (!(player.level() instanceof ServerLevel level)) {
            return Collections.emptyList();
        }

        ReputationManager repManager = ReputationManager.get(level);
        int playerRep = repManager.getReputationPoints(player, village);

        return getAvailableQuests(village.getVillageId()).stream()
            .filter(q -> q.getStatus() == Quest.QuestStatus.AVAILABLE)
            .filter(q -> playerRep >= q.getMinimumReputation())
            .collect(Collectors.toList());
    }

    /**
     * 检查玩家是否已完成某任务
     */
    public boolean hasCompletedQuest(Player player, UUID questId) {
        Set<UUID> completed = playerCompletedQuests.get(player.getUUID());
        return completed != null && completed.contains(questId);
    }

    // ================ 任务管理 ================

    /**
     * 玩家接受任务
     */
    public boolean acceptQuest(Player player, Quest quest) {
        UUID playerId = player.getUUID();
        List<Quest> activeQuests = playerActiveQuests.computeIfAbsent(playerId, k -> new ArrayList<>());

        // 检查任务数量限制
        if (activeQuests.size() >= MAX_ACTIVE_QUESTS) {
            return false;
        }

        // 检查任务是否可接受
        if (quest.getStatus() != Quest.QuestStatus.AVAILABLE) {
            return false;
        }

        // 接受任务
        long currentTime = player.level().getGameTime();
        if (quest.accept(currentTime)) {
            activeQuests.add(quest);

            // 从村庄可用任务中移除
            UUID villageId = quest.getVillageId();
            if (villageId != null) {
                List<Quest> villageQuests = villageAvailableQuests.get(villageId);
                if (villageQuests != null) {
                    villageQuests.remove(quest);
                }
            }

            setDirty();
            MillenaireRewrite.LOGGER.info("Player {} accepted quest: {}",
                player.getName().getString(), quest.getTitle());
            return true;
        }

        return false;
    }

    /**
     * 玩家放弃任务
     */
    public void abandonQuest(Player player, Quest quest) {
        UUID playerId = player.getUUID();
        List<Quest> activeQuests = playerActiveQuests.get(playerId);

        if (activeQuests != null && activeQuests.contains(quest)) {
            quest.abandon();
            activeQuests.remove(quest);
            setDirty();

            MillenaireRewrite.LOGGER.info("Player {} abandoned quest: {}",
                player.getName().getString(), quest.getTitle());
        }
    }

    /**
     * 完成任务并给予奖励
     */
    public boolean completeQuest(ServerPlayer player, Quest quest) {
        if (!quest.areAllObjectivesComplete()) {
            return false;
        }

        long currentTime = player.level().getGameTime();
        if (!quest.complete(currentTime)) {
            return false;
        }

        // 移除活跃任务
        UUID playerId = player.getUUID();
        List<Quest> activeQuests = playerActiveQuests.get(playerId);
        if (activeQuests != null) {
            activeQuests.remove(quest);
        }

        // 添加到已完成列表
        playerCompletedQuests.computeIfAbsent(playerId, k -> new HashSet<>())
            .add(quest.getQuestId());

        // 给予奖励
        grantRewards(player, quest);

        setDirty();

        MillenaireRewrite.LOGGER.info("Player {} completed quest: {}",
            player.getName().getString(), quest.getTitle());

        return true;
    }

    /**
     * 给予任务奖励
     */
    private void grantRewards(ServerPlayer player, Quest quest) {
        // 物品奖励
        for (var reward : quest.getItemRewards()) {
            if (!player.getInventory().add(reward.copy())) {
                player.drop(reward.copy(), false);
            }
        }

        // 第纳尔奖励
        if (quest.getDenierReward() > 0) {
            // TODO: 给予第纳尔物品
            // 临时：直接添加到村庄（玩家可以去取）
        }

        // 声望奖励
        if (quest.getReputationReward() > 0 && quest.getVillageId() != null) {
            ServerLevel level = player.serverLevel();
            ReputationManager repManager = ReputationManager.get(level);

            // 查找村庄
            var villageManager = com.jasoncian.millenaire_rewrite.village.VillageManager.get(level);
            Village village = villageManager.getVillage(quest.getVillageId());

            if (village != null) {
                repManager.onQuestComplete(player, village, quest.getReputationReward());
            }
        }
    }

    // ================ 进度更新 ================

    /**
     * 更新物品收集进度
     */
    public void updateItemProgress(Player player, Item item, int count) {
        List<Quest> activeQuests = getActiveQuests(player);
        boolean changed = false;

        for (Quest quest : activeQuests) {
            if (quest.getStatus() == Quest.QuestStatus.ACTIVE) {
                quest.updateItemProgress(item, count);
                changed = true;
            }
        }

        if (changed) {
            setDirty();
        }
    }

    /**
     * 更新击杀进度
     */
    public void updateKillProgress(Player player, String entityType, int count) {
        List<Quest> activeQuests = getActiveQuests(player);
        boolean changed = false;

        for (Quest quest : activeQuests) {
            if (quest.getStatus() == Quest.QuestStatus.ACTIVE) {
                quest.updateKillProgress(entityType, count);
                changed = true;
            }
        }

        if (changed) {
            setDirty();
        }
    }

    /**
     * 更新位置进度
     */
    public void updateLocationProgress(Player player, net.minecraft.core.BlockPos pos) {
        List<Quest> activeQuests = getActiveQuests(player);
        boolean changed = false;

        for (Quest quest : activeQuests) {
            if (quest.getStatus() == Quest.QuestStatus.ACTIVE) {
                quest.updateLocationProgress(pos);
                changed = true;
            }
        }

        if (changed) {
            setDirty();
        }
    }

    // ================ 任务生成 ================

    /**
     * 为村庄生成新任务
     */
    public void generateQuestsForVillage(Village village) {
        UUID villageId = village.getVillageId();
        List<Quest> currentQuests = villageAvailableQuests.computeIfAbsent(villageId, k -> new ArrayList<>());

        // 移除过期或已接受的任务
        currentQuests.removeIf(q -> q.getStatus() != Quest.QuestStatus.AVAILABLE);

        // 生成新任务直到达到上限
        while (currentQuests.size() < MAX_AVAILABLE_QUESTS_PER_VILLAGE) {
            Quest newQuest = CultureQuests.generateRandomQuest(village.getCulture(), village);
            if (newQuest != null) {
                newQuest.setVillageId(villageId);
                newQuest.setCulture(village.getCulture());
                currentQuests.add(newQuest);
            } else {
                break; // 无法生成更多任务
            }
        }

        setDirty();
    }

    /**
     * 定期刷新所有村庄任务
     */
    public void tick(ServerLevel level) {
        long currentTime = level.getGameTime();

        // 检查是否需要刷新
        if (currentTime - lastRefreshTime >= QUEST_REFRESH_INTERVAL) {
            lastRefreshTime = currentTime;

            // 刷新所有村庄任务
            var villageManager = com.jasoncian.millenaire_rewrite.village.VillageManager.get(level);
            for (Village village : villageManager.getAllVillages()) {
                generateQuestsForVillage(village);
            }

            // 检查玩家任务是否超时
            for (var entry : playerActiveQuests.entrySet()) {
                for (Quest quest : entry.getValue()) {
                    if (quest.isExpired(currentTime)) {
                        quest.fail();
                    }
                }
                // 移除失败的任务
                entry.getValue().removeIf(q -> q.getStatus() == Quest.QuestStatus.FAILED);
            }

            setDirty();
        }
    }

    // ================ 统计 ================

    /**
     * 获取玩家完成的任务数量
     */
    public int getCompletedQuestCount(Player player) {
        Set<UUID> completed = playerCompletedQuests.get(player.getUUID());
        return completed != null ? completed.size() : 0;
    }

    /**
     * 获取玩家活跃任务数量
     */
    public int getActiveQuestCount(Player player) {
        List<Quest> active = playerActiveQuests.get(player.getUUID());
        return active != null ? active.size() : 0;
    }

    /**
     * 获取特定文化完成的任务数量
     */
    public int getCompletedQuestsForCulture(Player player, Culture culture) {
        // 需要更详细的跟踪来实现这个
        // 暂时返回0
        return 0;
    }
}
