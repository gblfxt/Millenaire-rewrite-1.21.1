package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * 村庄数据请求包 - 客户端请求特定村庄的详细数据
 *
 * 请求类型：
 * - FULL: 完整村庄数据
 * - ECONOMY: 经济数据
 * - VILLAGERS: 村民列表
 * - ALL_VILLAGES: 所有村庄列表
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record RequestVillageDataPacket(
    String requestType,
    String villageId
) implements CustomPacketPayload {

    // ================ 常量 ================

    public static final String TYPE_FULL = "FULL";
    public static final String TYPE_ECONOMY = "ECONOMY";
    public static final String TYPE_VILLAGERS = "VILLAGERS";
    public static final String TYPE_ALL_VILLAGES = "ALL_VILLAGES";
    public static final String TYPE_REPUTATION = "REPUTATION";

    // ================ 类型定义 ================

    public static final Type<RequestVillageDataPacket> TYPE = new Type<>(ModNetworking.id("request_village_data"));

    public static final StreamCodec<FriendlyByteBuf, RequestVillageDataPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, RequestVillageDataPacket::requestType,
        ByteBufCodecs.STRING_UTF8, RequestVillageDataPacket::villageId,
        RequestVillageDataPacket::new
    );

    // ================ 工厂方法 ================

    public static RequestVillageDataPacket forVillage(UUID villageId) {
        return new RequestVillageDataPacket(TYPE_FULL, villageId.toString());
    }

    public static RequestVillageDataPacket forEconomy(UUID villageId) {
        return new RequestVillageDataPacket(TYPE_ECONOMY, villageId.toString());
    }

    public static RequestVillageDataPacket forAllVillages() {
        return new RequestVillageDataPacket(TYPE_ALL_VILLAGES, "");
    }

    public static RequestVillageDataPacket forReputation() {
        return new RequestVillageDataPacket(TYPE_REPUTATION, "");
    }

    // ================ 处理方法 ================

    public static void handle(RequestVillageDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            ServerLevel level = player.serverLevel();
            VillageManager manager = VillageManager.get(level);

            switch (packet.requestType()) {
                case TYPE_FULL -> {
                    try {
                        UUID id = UUID.fromString(packet.villageId());
                        Village village = manager.getVillage(id);
                        if (village != null) {
                            PacketDistributor.sendToPlayer(player, VillageSyncPacket.fromVillage(village));
                        }
                    } catch (IllegalArgumentException e) {
                        MillenaireRewrite.LOGGER.warn("Invalid village ID requested: {}", packet.villageId());
                    }
                }

                case TYPE_ECONOMY -> {
                    try {
                        UUID id = UUID.fromString(packet.villageId());
                        Village village = manager.getVillage(id);
                        if (village != null) {
                            PacketDistributor.sendToPlayer(player,
                                NetworkHelper.createEconomyPacket(village));
                        }
                    } catch (IllegalArgumentException e) {
                        MillenaireRewrite.LOGGER.warn("Invalid village ID for economy: {}", packet.villageId());
                    }
                }

                case TYPE_ALL_VILLAGES -> {
                    PacketDistributor.sendToPlayer(player,
                        NetworkHelper.createVillageListPacket(manager));
                }

                case TYPE_REPUTATION -> {
                    PacketDistributor.sendToPlayer(player,
                        NetworkHelper.createReputationPacket(player, manager));
                }

                default -> MillenaireRewrite.LOGGER.warn("Unknown request type: {}", packet.requestType());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
