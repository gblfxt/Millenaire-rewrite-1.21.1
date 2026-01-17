package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/**
 * 村庄同步包 - 同步单个村庄的完整数据到客户端
 *
 * 包含：
 * - 村庄ID和名称
 * - 位置和维度
 * - 文化类型
 * - 村民数量
 * - 建筑数量
 * - 经济数据
 * - 等级
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record VillageSyncPacket(
    UUID villageId,
    String name,
    BlockPos townHallPos,
    String dimension,
    String cultureId,
    int villagerCount,
    int buildingCount,
    long deniers,
    int level,
    boolean isActive
) implements CustomPacketPayload {

    // ================ 类型定义 ================

    public static final Type<VillageSyncPacket> TYPE = new Type<>(ModNetworking.id("village_sync"));

    public static final StreamCodec<FriendlyByteBuf, VillageSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public VillageSyncPacket decode(FriendlyByteBuf buf) {
            return new VillageSyncPacket(
                UUID.fromString(buf.readUtf()),
                buf.readUtf(),
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarLong(),
                buf.readVarInt(),
                buf.readBoolean()
            );
        }

        @Override
        public void encode(FriendlyByteBuf buf, VillageSyncPacket packet) {
            buf.writeUtf(packet.villageId().toString());
            buf.writeUtf(packet.name());
            buf.writeBlockPos(packet.townHallPos());
            buf.writeUtf(packet.dimension());
            buf.writeUtf(packet.cultureId());
            buf.writeVarInt(packet.villagerCount());
            buf.writeVarInt(packet.buildingCount());
            buf.writeVarLong(packet.deniers());
            buf.writeVarInt(packet.level());
            buf.writeBoolean(packet.isActive());
        }
    };

    // ================ 构造方法 ================

    /**
     * 从Village对象创建同步包
     */
    public static VillageSyncPacket fromVillage(Village village) {
        return new VillageSyncPacket(
            village.getVillageId(),
            village.getName(),
            village.getTownHallPos(),
            village.getDimension() != null ? village.getDimension() : "minecraft:overworld",
            village.getCulture().getId(),
            village.getActiveVillagerCount(),
            village.getBuildingCount(),
            village.getDeniers(),
            village.getLevel(),
            village.isLoaded()
        );
    }

    // ================ 处理方法 ================

    public static void handle(VillageSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 在客户端处理村庄数据
            ClientVillageCache.updateVillage(packet);
            MillenaireRewrite.LOGGER.debug("Received village sync: {} at {}",
                packet.name(), packet.townHallPos());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
