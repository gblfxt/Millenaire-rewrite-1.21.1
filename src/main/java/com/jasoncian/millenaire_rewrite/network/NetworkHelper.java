package com.jasoncian.millenaire_rewrite.network;

import com.jasoncian.millenaire_rewrite.reputation.ReputationManager;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 网络辅助类 - 提供创建和发送网络包的便捷方法
 *
 * @author Based on networking patterns
 * @version 1.0.0
 */
public class NetworkHelper {

    // ================ 村庄同步 ================

    /**
     * 向玩家同步单个村庄
     */
    public static void syncVillageToPlayer(ServerPlayer player, Village village) {
        PacketDistributor.sendToPlayer(player, VillageSyncPacket.fromVillage(village));
    }

    /**
     * 向所有玩家同步村庄
     */
    public static void syncVillageToAll(Village village) {
        PacketDistributor.sendToAllPlayers(VillageSyncPacket.fromVillage(village));
    }

    /**
     * 向玩家同步所有村庄列表
     */
    public static void syncVillageListToPlayer(ServerPlayer player, VillageManager manager) {
        PacketDistributor.sendToPlayer(player, createVillageListPacket(manager));
    }

    /**
     * 创建村庄列表包
     */
    public static VillageListSyncPacket createVillageListPacket(VillageManager manager) {
        List<VillageListSyncPacket.VillageEntry> entries = new ArrayList<>();

        for (Village village : manager.getAllVillages()) {
            entries.add(new VillageListSyncPacket.VillageEntry(
                village.getVillageId(),
                village.getName(),
                village.getCulture().getId(),
                village.getTownHallPos(),
                village.getActiveVillagerCount(),
                village.getLevel(),
                true // isKnown - could be based on player's discovery
            ));
        }

        return new VillageListSyncPacket(entries);
    }

    // ================ 经济同步 ================

    /**
     * 创建经济同步包
     */
    public static EconomySyncPacket createEconomyPacket(Village village) {
        List<EconomySyncPacket.ResourceEntry> resources = new ArrayList<>();
        List<EconomySyncPacket.TradeEntry> trades = new ArrayList<>();

        // 添加村庄资源
        var villageResources = village.getAllResources();
        if (villageResources != null) {
            for (var entry : villageResources.entrySet()) {
                resources.add(new EconomySyncPacket.ResourceEntry(
                    entry.getKey().toString(),
                    entry.getValue(),
                    1000 // 默认最大存储
                ));
            }
        }

        // 交易暂时留空，后续可以添加
        return new EconomySyncPacket(
            village.getVillageId(),
            village.getDeniers(),
            resources,
            trades
        );
    }

    /**
     * 向玩家同步经济数据
     */
    public static void syncEconomyToPlayer(ServerPlayer player, Village village) {
        PacketDistributor.sendToPlayer(player, createEconomyPacket(village));
    }

    // ================ 声望同步 ================

    /**
     * 创建声望同步包
     */
    public static ReputationSyncPacket createReputationPacket(ServerPlayer player, VillageManager manager) {
        List<ReputationSyncPacket.VillageReputation> reputations = new ArrayList<>();

        ReputationManager repManager = ReputationManager.get(player.serverLevel());
        UUID playerId = player.getUUID();

        for (Village village : manager.getAllVillages()) {
            int points = repManager.getReputationPoints(player, village);
            var level = repManager.getReputationLevel(player, village);

            reputations.add(new ReputationSyncPacket.VillageReputation(
                village.getVillageId(),
                village.getName(),
                village.getCulture().getDisplayName(),
                points,
                level.getDisplayName()
            ));
        }

        return new ReputationSyncPacket(playerId, reputations);
    }

    /**
     * 向玩家同步声望数据
     */
    public static void syncReputationToPlayer(ServerPlayer player, VillageManager manager) {
        PacketDistributor.sendToPlayer(player, createReputationPacket(player, manager));
    }

    // ================ 村民同步 ================

    /**
     * 创建村民同步包
     */
    public static VillagerSyncPacket createVillagerPacket(
        int entityId,
        UUID villagerUuid,
        String name,
        String profession,
        int gender,
        Village village,
        int health,
        int maxHealth,
        boolean isWorking,
        String currentTask
    ) {
        return new VillagerSyncPacket(
            entityId,
            villagerUuid,
            name,
            profession,
            gender,
            village.getVillageId(),
            village.getName(),
            health,
            maxHealth,
            isWorking,
            currentTask
        );
    }

    // ================ 批量同步 ================

    /**
     * 同步玩家附近的所有数据
     */
    public static void syncNearbyDataToPlayer(ServerPlayer player, VillageManager manager, double radius) {
        // 筛选附近的村庄
        net.minecraft.core.BlockPos playerPos = player.blockPosition();

        for (Village village : manager.getAllVillages()) {
            net.minecraft.core.BlockPos villagePos = village.getTownHallPos();
            if (villagePos != null) {
                double dist = Math.sqrt(villagePos.distSqr(playerPos));
                if (dist <= radius) {
                    syncVillageToPlayer(player, village);
                    syncEconomyToPlayer(player, village);
                }
            }
        }

        syncReputationToPlayer(player, manager);
    }

    /**
     * 同步所有数据到玩家
     */
    public static void syncAllDataToPlayer(ServerPlayer player, VillageManager manager) {
        // 同步村庄列表
        syncVillageListToPlayer(player, manager);

        // 同步声望
        syncReputationToPlayer(player, manager);

        // 同步各村庄详情
        for (Village village : manager.getAllVillages()) {
            syncVillageToPlayer(player, village);
        }
    }
}
