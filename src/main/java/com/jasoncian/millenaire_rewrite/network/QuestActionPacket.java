package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.quest.Quest;
import com.jasoncian.millenaire_rewrite.quest.QuestManager;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.network.FriendlyByteBuf;
import java.util.UUID;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 任务操作包 - 客户端发送任务相关操作请求
 *
 * 操作类型：
 * - ACCEPT: 接受任务
 * - ABANDON: 放弃任务
 * - COMPLETE: 完成任务（领取奖励）
 * - REQUEST_LIST: 请求任务列表
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record QuestActionPacket(
    String action,
    String questId
) implements CustomPacketPayload {

    // ================ 常量 ================

    public static final String ACTION_ACCEPT = "ACCEPT";
    public static final String ACTION_ABANDON = "ABANDON";
    public static final String ACTION_COMPLETE = "COMPLETE";
    public static final String ACTION_REQUEST_LIST = "REQUEST_LIST";

    // ================ 类型定义 ================

    public static final Type<QuestActionPacket> TYPE = new Type<>(ModNetworking.id("quest_action"));

    public static final StreamCodec<FriendlyByteBuf, QuestActionPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, QuestActionPacket::action,
        ByteBufCodecs.STRING_UTF8, QuestActionPacket::questId,
        QuestActionPacket::new
    );

    // ================ 工厂方法 ================

    public static QuestActionPacket accept(String questId) {
        return new QuestActionPacket(ACTION_ACCEPT, questId);
    }

    public static QuestActionPacket abandon(String questId) {
        return new QuestActionPacket(ACTION_ABANDON, questId);
    }

    public static QuestActionPacket complete(String questId) {
        return new QuestActionPacket(ACTION_COMPLETE, questId);
    }

    public static QuestActionPacket requestList() {
        return new QuestActionPacket(ACTION_REQUEST_LIST, "");
    }

    // ================ 处理方法 ================

    public static void handle(QuestActionPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            QuestManager questManager = QuestManager.get(player.serverLevel());
            VillageManager villageManager = VillageManager.get(player.serverLevel());

            switch (packet.action()) {
                case ACTION_ACCEPT -> {
                    // 在可用任务中查找
                    Quest quest = findQuestById(questManager, packet.questId());
                    if (quest != null && quest.getStatus() == Quest.QuestStatus.AVAILABLE) {
                        questManager.acceptQuest(player, quest);
                        String villageName = getVillageName(villageManager, quest.getVillageId());
                        PacketDistributor.sendToPlayer(player, QuestSyncPacket.fromQuest(quest, villageName));
                        MillenaireRewrite.LOGGER.debug("Player {} accepted quest: {}",
                            player.getName().getString(), quest.getTitle());
                    }
                }

                case ACTION_ABANDON -> {
                    Quest quest = findActiveQuest(questManager, player, packet.questId());
                    if (quest != null) {
                        questManager.abandonQuest(player, quest);
                        String villageName = getVillageName(villageManager, quest.getVillageId());
                        PacketDistributor.sendToPlayer(player, QuestSyncPacket.fromQuest(quest, villageName));
                        MillenaireRewrite.LOGGER.debug("Player {} abandoned quest: {}",
                            player.getName().getString(), quest.getTitle());
                    }
                }

                case ACTION_COMPLETE -> {
                    Quest quest = findActiveQuest(questManager, player, packet.questId());
                    if (quest != null && quest.areAllObjectivesComplete()) {
                        questManager.completeQuest(player, quest);
                        String villageName = getVillageName(villageManager, quest.getVillageId());
                        PacketDistributor.sendToPlayer(player, QuestSyncPacket.fromQuest(quest, villageName));
                        MillenaireRewrite.LOGGER.debug("Player {} completed quest: {}",
                            player.getName().getString(), quest.getTitle());
                    }
                }

                case ACTION_REQUEST_LIST -> {
                    // 发送所有活跃任务
                    for (Quest quest : questManager.getActiveQuests(player)) {
                        String villageName = getVillageName(villageManager, quest.getVillageId());
                        PacketDistributor.sendToPlayer(player, QuestSyncPacket.fromQuest(quest, villageName));
                    }
                }

                default -> MillenaireRewrite.LOGGER.warn("Unknown quest action: {}", packet.action());
            }
        });
    }

    private static Quest findQuestById(QuestManager manager, String questId) {
        try {
            UUID id = UUID.fromString(questId);
            // 搜索所有村庄的可用任务
            // 简化实现：返回null，实际应该遍历所有可用任务
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Quest findActiveQuest(QuestManager manager, ServerPlayer player, String questId) {
        try {
            UUID id = UUID.fromString(questId);
            for (Quest quest : manager.getActiveQuests(player)) {
                if (quest.getQuestId().equals(id)) {
                    return quest;
                }
            }
        } catch (IllegalArgumentException e) {
            // Invalid UUID
        }
        return null;
    }

    private static String getVillageName(VillageManager manager, UUID villageId) {
        if (villageId == null) return "Unknown";
        Village village = manager.getVillage(villageId);
        return village != null ? village.getName() : "Unknown";
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
