package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * 村民同步包 - 同步村民数据到客户端
 *
 * 包含：
 * - 村民实体ID
 * - 名字
 * - 职业
 * - 性别
 * - 所属村庄
 * - 家庭关系
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record VillagerSyncPacket(
    int entityId,
    UUID villagerUuid,
    String name,
    String profession,
    int gender,
    UUID villageId,
    String villageName,
    int health,
    int maxHealth,
    boolean isWorking,
    String currentTask
) implements CustomPacketPayload {

    // ================ 类型定义 ================

    public static final Type<VillagerSyncPacket> TYPE = new Type<>(ModNetworking.id("villager_sync"));

    public static final StreamCodec<FriendlyByteBuf, VillagerSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public VillagerSyncPacket decode(FriendlyByteBuf buf) {
            return new VillagerSyncPacket(
                buf.readVarInt(),
                UUID.fromString(buf.readUtf()),
                buf.readUtf(),
                buf.readUtf(),
                buf.readVarInt(),
                UUID.fromString(buf.readUtf()),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readUtf()
            );
        }

        @Override
        public void encode(FriendlyByteBuf buf, VillagerSyncPacket packet) {
            buf.writeVarInt(packet.entityId());
            buf.writeUtf(packet.villagerUuid().toString());
            buf.writeUtf(packet.name());
            buf.writeUtf(packet.profession());
            buf.writeVarInt(packet.gender());
            buf.writeUtf(packet.villageId().toString());
            buf.writeUtf(packet.villageName());
            buf.writeVarInt(packet.health());
            buf.writeVarInt(packet.maxHealth());
            buf.writeBoolean(packet.isWorking());
            buf.writeUtf(packet.currentTask());
        }
    };

    // ================ 处理方法 ================

    public static void handle(VillagerSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 在客户端处理村民数据
            ClientVillageCache.updateVillager(packet);
            MillenaireRewrite.LOGGER.debug("Received villager sync: {} ({})",
                packet.name(), packet.profession());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
