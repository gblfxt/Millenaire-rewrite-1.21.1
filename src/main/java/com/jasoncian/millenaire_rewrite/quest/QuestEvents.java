package com.jasoncian.millenaire_rewrite.quest;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 任务事件处理器 - 自动跟踪任务进度
 *
 * 监听：
 * - 生物死亡（击杀进度）
 * - 物品拾取（收集进度）
 * - 玩家移动（位置进度）
 *
 * @author Based on OldSource quest events
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID)
public class QuestEvents {

    // ================ 击杀事件 ================

    /**
     * 处理生物死亡事件
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 检查击杀者是否为玩家
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        LivingEntity killed = event.getEntity();

        // 获取被击杀生物的类型
        ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(killed.getType());
        String entityType = entityId.toString();

        // 更新任务进度
        QuestManager questManager = QuestManager.get(level);
        questManager.updateKillProgress(player, entityType, 1);

        MillenaireRewrite.LOGGER.debug("Player {} killed {}, updating quest progress",
            player.getName().getString(), entityType);
    }

    // ================ 物品拾取事件 ================

    /**
     * 处理物品拾取事件
     */
    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        ItemStack stack = event.getOriginalStack();

        if (stack.isEmpty()) {
            return;
        }

        // 更新任务进度
        QuestManager questManager = QuestManager.get(level);
        questManager.updateItemProgress(player, stack.getItem(), stack.getCount());

        MillenaireRewrite.LOGGER.debug("Player {} picked up {}x {}, updating quest progress",
            player.getName().getString(), stack.getCount(),
            BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    // ================ 位置更新事件 ================

    /** 位置检查间隔（tick） */
    private static final int LOCATION_CHECK_INTERVAL = 20; // 每秒检查一次

    /**
     * 处理玩家移动（每tick）
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // 限制检查频率
        if (player.tickCount % LOCATION_CHECK_INTERVAL != 0) {
            return;
        }

        ServerLevel level = serverPlayer.serverLevel();
        QuestManager questManager = QuestManager.get(level);

        // 更新位置进度
        questManager.updateLocationProgress(player, player.blockPosition());
    }

    // ================ 玩家登录事件 ================

    /**
     * 玩家登录时检查任务状态
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();
        QuestManager questManager = QuestManager.get(level);

        // 记录玩家当前任务
        int activeCount = questManager.getActiveQuestCount(player);
        int completedCount = questManager.getCompletedQuestCount(player);

        MillenaireRewrite.LOGGER.info("Player {} logged in with {} active quests, {} completed",
            player.getName().getString(), activeCount, completedCount);
    }

    // ================ 辅助方法 ================

    /**
     * 手动触发物品收集进度更新
     * 用于玩家合成、交易等非拾取方式获得物品
     */
    public static void triggerItemCollect(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        ServerLevel level = player.serverLevel();
        QuestManager questManager = QuestManager.get(level);
        questManager.updateItemProgress(player, stack.getItem(), stack.getCount());
    }

    /**
     * 手动触发击杀进度更新
     * 用于特殊击杀方式（如陷阱）
     */
    public static void triggerKill(ServerPlayer player, String entityType) {
        ServerLevel level = player.serverLevel();
        QuestManager questManager = QuestManager.get(level);
        questManager.updateKillProgress(player, entityType, 1);
    }

    /**
     * 检查玩家是否有特定物品的收集任务
     */
    public static boolean hasItemQuest(ServerPlayer player, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        QuestManager questManager = QuestManager.get(level);

        for (Quest quest : questManager.getActiveQuests(player)) {
            if (quest.getStatus() != Quest.QuestStatus.ACTIVE) {
                continue;
            }

            for (QuestObjective objective : quest.getObjectives()) {
                if (objective.getType() == QuestObjective.ObjectiveType.COLLECT_ITEM ||
                    objective.getType() == QuestObjective.ObjectiveType.DELIVER_ITEM) {
                    if (objective.getTargetItem() == stack.getItem() && !objective.isComplete()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查玩家是否有特定生物的击杀任务
     */
    public static boolean hasKillQuest(ServerPlayer player, String entityType) {
        ServerLevel level = player.serverLevel();
        QuestManager questManager = QuestManager.get(level);

        for (Quest quest : questManager.getActiveQuests(player)) {
            if (quest.getStatus() != Quest.QuestStatus.ACTIVE) {
                continue;
            }

            for (QuestObjective objective : quest.getObjectives()) {
                if (objective.getType() == QuestObjective.ObjectiveType.KILL_ENTITY) {
                    if (entityType.equals(objective.getTargetEntityType()) && !objective.isComplete()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
