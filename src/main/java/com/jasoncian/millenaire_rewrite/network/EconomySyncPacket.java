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
 * 经济同步包 - 同步村庄经济和交易数据到客户端
 *
 * 包含：
 * - 村庄货币
 * - 资源库存
 * - 当前交易
 *
 * @author Based on NeoForge packet patterns
 * @version 1.0.0
 */
public record EconomySyncPacket(
    UUID villageId,
    long deniers,
    List<ResourceEntry> resources,
    List<TradeEntry> availableTrades
) implements CustomPacketPayload {

    // ================ 类型定义 ================

    public static final Type<EconomySyncPacket> TYPE = new Type<>(ModNetworking.id("economy_sync"));

    public static final StreamCodec<FriendlyByteBuf, EconomySyncPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public EconomySyncPacket decode(FriendlyByteBuf buf) {
            UUID villageId = UUID.fromString(buf.readUtf());
            long deniers = buf.readVarLong();

            int resourceCount = buf.readVarInt();
            List<ResourceEntry> resources = new ArrayList<>(resourceCount);
            for (int i = 0; i < resourceCount; i++) {
                resources.add(new ResourceEntry(
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt()
                ));
            }

            int tradeCount = buf.readVarInt();
            List<TradeEntry> trades = new ArrayList<>(tradeCount);
            for (int i = 0; i < tradeCount; i++) {
                trades.add(new TradeEntry(
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readBoolean()
                ));
            }

            return new EconomySyncPacket(villageId, deniers, resources, trades);
        }

        @Override
        public void encode(FriendlyByteBuf buf, EconomySyncPacket packet) {
            buf.writeUtf(packet.villageId().toString());
            buf.writeVarLong(packet.deniers());

            buf.writeVarInt(packet.resources().size());
            for (ResourceEntry res : packet.resources()) {
                buf.writeUtf(res.resourceId());
                buf.writeVarInt(res.amount());
                buf.writeVarInt(res.maxAmount());
            }

            buf.writeVarInt(packet.availableTrades().size());
            for (TradeEntry trade : packet.availableTrades()) {
                buf.writeUtf(trade.itemId());
                buf.writeVarInt(trade.buyPrice());
                buf.writeVarInt(trade.sellPrice());
                buf.writeVarInt(trade.stock());
                buf.writeBoolean(trade.canBuy());
            }
        }
    };

    // ================ 内部类 ================

    /**
     * 资源条目
     */
    public record ResourceEntry(
        String resourceId,
        int amount,
        int maxAmount
    ) {}

    /**
     * 交易条目
     */
    public record TradeEntry(
        String itemId,
        int buyPrice,
        int sellPrice,
        int stock,
        boolean canBuy
    ) {}

    // ================ 处理方法 ================

    public static void handle(EconomySyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientVillageCache.updateEconomy(packet);
            MillenaireRewrite.LOGGER.debug("Received economy sync for village: {} ({} deniers)",
                packet.villageId(), packet.deniers());
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
