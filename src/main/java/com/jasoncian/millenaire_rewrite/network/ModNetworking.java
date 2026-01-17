package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络包注册 - 注册所有客户端-服务端同步包
 *
 * 包类型：
 * - VillageSyncPacket: 村庄数据同步
 * - VillagerSyncPacket: 村民数据同步
 * - QuestSyncPacket: 任务进度同步
 * - EconomySyncPacket: 经济数据同步
 * - ReputationSyncPacket: 声望数据同步
 *
 * @author Based on NeoForge networking patterns
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    /** 网络协议版本 */
    public static final String PROTOCOL_VERSION = "1";

    // ================ 包注册 ================

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MillenaireRewrite.MOD_ID)
            .versioned(PROTOCOL_VERSION);

        // 服务端 -> 客户端 包
        registrar.playToClient(
            VillageSyncPacket.TYPE,
            VillageSyncPacket.STREAM_CODEC,
            VillageSyncPacket::handle
        );

        registrar.playToClient(
            VillagerSyncPacket.TYPE,
            VillagerSyncPacket.STREAM_CODEC,
            VillagerSyncPacket::handle
        );

        registrar.playToClient(
            QuestSyncPacket.TYPE,
            QuestSyncPacket.STREAM_CODEC,
            QuestSyncPacket::handle
        );

        registrar.playToClient(
            EconomySyncPacket.TYPE,
            EconomySyncPacket.STREAM_CODEC,
            EconomySyncPacket::handle
        );

        registrar.playToClient(
            ReputationSyncPacket.TYPE,
            ReputationSyncPacket.STREAM_CODEC,
            ReputationSyncPacket::handle
        );

        registrar.playToClient(
            VillageListSyncPacket.TYPE,
            VillageListSyncPacket.STREAM_CODEC,
            VillageListSyncPacket::handle
        );

        // 客户端 -> 服务端 包
        registrar.playToServer(
            RequestVillageDataPacket.TYPE,
            RequestVillageDataPacket.STREAM_CODEC,
            RequestVillageDataPacket::handle
        );

        registrar.playToServer(
            QuestActionPacket.TYPE,
            QuestActionPacket.STREAM_CODEC,
            QuestActionPacket::handle
        );

        MillenaireRewrite.LOGGER.info("Millenaire network packets registered");
    }

    // ================ 工具方法 ================

    /**
     * 创建资源位置
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MillenaireRewrite.MOD_ID, path);
    }
}
