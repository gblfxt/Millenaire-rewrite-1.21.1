package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 村庄列表同步包 - 同步所有已知村庄的基本信息到客户端
 *
 * 用于地图显示和村庄选择界面
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record VillageListSyncPacket(
    List<VillageEntry> villages
) implements CustomPacketPayload {

    // ================ 类型定义 ================

    public static final Type<VillageListSyncPacket> TYPE = new Type<>(ModNetworking.id("village_list_sync"));

    public static final StreamCodec<FriendlyByteBuf, VillageListSyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public VillageListSyncPacket decode(FriendlyByteBuf buf) {
            int count = buf.readVarInt();
            List<VillageEntry> villages = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                villages.add(new VillageEntry(
                    UUID.fromString(buf.readUtf()),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readBlockPos(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean()
                ));
            }
            return new VillageListSyncPacket(villages);
        }

        @Override
        public void encode(FriendlyByteBuf buf, VillageListSyncPacket packet) {
            buf.writeVarInt(packet.villages().size());
            for (VillageEntry entry : packet.villages()) {
                buf.writeUtf(entry.villageId().toString());
                buf.writeUtf(entry.name());
                buf.writeUtf(entry.cultureId());
                buf.writeBlockPos(entry.position());
                buf.writeVarInt(entry.villagerCount());
                buf.writeVarInt(entry.level());
                buf.writeBoolean(entry.isKnown());
            }
        }
    };

    // ================ 内部类 ================

    /**
     * 村庄条目（简化数据）
     */
    public record VillageEntry(
        UUID villageId,
        String name,
        String cultureId,
        BlockPos position,
        int villagerCount,
        int level,
        boolean isKnown
    ) {}

    // ================ 处理方法 ================

    public static void handle(VillageListSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientVillageCache.updateVillageList(packet);
            MillenaireRewrite.LOGGER.debug("Received village list sync: {} villages",
                packet.villages().size());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
