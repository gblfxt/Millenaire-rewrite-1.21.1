package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 声望同步包 - 同步玩家与村庄的声望数据到客户端
 *
 * 包含：
 * - 玩家UUID
 * - 各村庄的声望值
 * - 声望等级
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record ReputationSyncPacket(
    UUID playerId,
    List<VillageReputation> reputations
) implements CustomPacketPayload {

    // ================ 类型定义 ================

    public static final Type<ReputationSyncPacket> TYPE = new Type<>(ModNetworking.id("reputation_sync"));

    public static final StreamCodec<FriendlyByteBuf, ReputationSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ReputationSyncPacket decode(FriendlyByteBuf buf) {
            UUID playerId = UUID.fromString(buf.readUtf());

            int count = buf.readVarInt();
            List<VillageReputation> reputations = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                reputations.add(new VillageReputation(
                    UUID.fromString(buf.readUtf()),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readUtf()
                ));
            }

            return new ReputationSyncPacket(playerId, reputations);
        }

        @Override
        public void encode(FriendlyByteBuf buf, ReputationSyncPacket packet) {
            buf.writeUtf(packet.playerId().toString());

            buf.writeVarInt(packet.reputations().size());
            for (VillageReputation rep : packet.reputations()) {
                buf.writeUtf(rep.villageId().toString());
                buf.writeUtf(rep.villageName());
                buf.writeUtf(rep.cultureName());
                buf.writeVarInt(rep.reputationPoints());
                buf.writeUtf(rep.reputationLevel());
            }
        }
    };

    // ================ 内部类 ================

    /**
     * 村庄声望数据
     */
    public record VillageReputation(
        UUID villageId,
        String villageName,
        String cultureName,
        int reputationPoints,
        String reputationLevel
    ) {}

    // ================ 处理方法 ================

    public static void handle(ReputationSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientVillageCache.updateReputation(packet);
            MillenaireRewrite.LOGGER.debug("Received reputation sync for player: {} ({} villages)",
                packet.playerId(), packet.reputations().size());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
