package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.quest.Quest;
import com.jasoncian.millenaire_rewrite.quest.QuestObjective;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 任务同步包 - 同步玩家任务进度到客户端
 *
 * 包含：
 * - 任务ID和标题
 * - 任务状态
 * - 目标进度
 * - 奖励信息
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record QuestSyncPacket(
    String questId,
    String title,
    String description,
    String status,
    UUID villageId,
    String villageName,
    int denierReward,
    int reputationReward,
    List<ObjectiveData> objectives
) implements CustomPacketPayload {

    // ================ 类型定义 ================

    public static final Type<QuestSyncPacket> TYPE = new Type<>(ModNetworking.id("quest_sync"));

    public static final StreamCodec<FriendlyByteBuf, QuestSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public QuestSyncPacket decode(FriendlyByteBuf buf) {
            String questId = buf.readUtf();
            String title = buf.readUtf();
            String description = buf.readUtf();
            String status = buf.readUtf();
            UUID villageId = UUID.fromString(buf.readUtf());
            String villageName = buf.readUtf();
            int denierReward = buf.readVarInt();
            int reputationReward = buf.readVarInt();

            int objectiveCount = buf.readVarInt();
            List<ObjectiveData> objectives = new ArrayList<>(objectiveCount);
            for (int i = 0; i < objectiveCount; i++) {
                objectives.add(new ObjectiveData(
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean()
                ));
            }

            return new QuestSyncPacket(questId, title, description, status, villageId,
                villageName, denierReward, reputationReward, objectives);
        }

        @Override
        public void encode(FriendlyByteBuf buf, QuestSyncPacket packet) {
            buf.writeUtf(packet.questId());
            buf.writeUtf(packet.title());
            buf.writeUtf(packet.description());
            buf.writeUtf(packet.status());
            buf.writeUtf(packet.villageId().toString());
            buf.writeUtf(packet.villageName());
            buf.writeVarInt(packet.denierReward());
            buf.writeVarInt(packet.reputationReward());

            buf.writeVarInt(packet.objectives().size());
            for (ObjectiveData obj : packet.objectives()) {
                buf.writeUtf(obj.description());
                buf.writeVarInt(obj.currentProgress());
                buf.writeVarInt(obj.targetAmount());
                buf.writeBoolean(obj.isComplete());
            }
        }
    };

    // ================ 内部类 ================

    /**
     * 目标数据记录
     */
    public record ObjectiveData(
        String description,
        int currentProgress,
        int targetAmount,
        boolean isComplete
    ) {}

    // ================ 工厂方法 ================

    /**
     * 从Quest对象创建同步包
     */
    public static QuestSyncPacket fromQuest(Quest quest, String villageName) {
        List<ObjectiveData> objectiveData = new ArrayList<>();
        for (QuestObjective obj : quest.getObjectives()) {
            objectiveData.add(new ObjectiveData(
                obj.getDescription(),
                obj.getCurrentProgress(),
                obj.getTargetAmount(),
                obj.isComplete()
            ));
        }

        return new QuestSyncPacket(
            quest.getQuestId().toString(),
            quest.getTitle(),
            quest.getDescription(),
            quest.getStatus().name(),
            quest.getVillageId(),
            villageName,
            quest.getDenierReward(),
            quest.getReputationReward(),
            objectiveData
        );
    }

    // ================ 处理方法 ================

    public static void handle(QuestSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientVillageCache.updateQuest(packet);
            MillenaireRewrite.LOGGER.debug("Received quest sync: {} ({})",
                packet.title(), packet.status());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
