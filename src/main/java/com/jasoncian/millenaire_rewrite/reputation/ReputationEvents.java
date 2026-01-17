package com.jasoncian.millenaire_rewrite.reputation;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 声望事件处理 - 自动处理影响声望的游戏事件
 *
 * 监听：
 * - 村民被攻击
 * - 村民死亡
 * - 方块破坏（在村庄范围内）
 *
 * @author Based on OldSource reputation events
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID)
public class ReputationEvents {

    /**
     * 村民受到伤害时
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // 检查是否是Millenaire村民
        if (!(event.getEntity() instanceof MillVillager villager)) {
            return;
        }

        // 检查攻击者是否是玩家
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // 获取村庄
        Village village = getVillageForVillager(villager);
        if (village == null) {
            return;
        }

        // 获取声望管理器
        if (!(villager.level() instanceof ServerLevel level)) {
            return;
        }

        ReputationManager manager = ReputationManager.get(level);
        manager.onAttackVillager(player, village);

        MillenaireRewrite.LOGGER.debug("Player {} attacked villager in {}",
            player.getName().getString(), village.getName());
    }

    /**
     * 村民死亡时
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 检查是否是Millenaire村民
        if (!(event.getEntity() instanceof MillVillager villager)) {
            return;
        }

        // 检查是否是玩家造成的死亡
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        // 获取村庄
        Village village = getVillageForVillager(villager);
        if (village == null) {
            return;
        }

        // 获取声望管理器
        if (!(villager.level() instanceof ServerLevel level)) {
            return;
        }

        ReputationManager manager = ReputationManager.get(level);
        manager.onKillVillager(player, village);

        MillenaireRewrite.LOGGER.info("Player {} killed villager in {} - major reputation loss!",
            player.getName().getString(), village.getName());
    }

    /**
     * 获取村民所属的村庄
     */
    private static Village getVillageForVillager(MillVillager villager) {
        if (!(villager.level() instanceof ServerLevel level)) {
            return null;
        }

        VillageManager villageManager = VillageManager.get(level);
        if (villageManager == null) {
            return null;
        }

        // 通过村民的townHallPos获取村庄
        var townHallPos = villager.getTownHallPos();
        if (townHallPos == null) {
            return null;
        }

        return villageManager.getVillageAt(townHallPos);
    }

    /**
     * 处理玩家保护村庄事件（手动调用）
     * 当玩家击杀攻击村庄的敌人时调用
     */
    public static void onPlayerDefendVillage(ServerPlayer player, Village village, int enemiesKilled) {
        if (player.level() instanceof ServerLevel level) {
            ReputationManager manager = ReputationManager.get(level);
            manager.onDefendVillage(player, village, enemiesKilled);

            MillenaireRewrite.LOGGER.info("Player {} defended {} from {} enemies",
                player.getName().getString(), village.getName(), enemiesKilled);
        }
    }

    /**
     * 处理玩家捐赠事件（手动调用）
     */
    public static void onPlayerDonate(ServerPlayer player, Village village, int value) {
        if (player.level() instanceof ServerLevel level) {
            ReputationManager manager = ReputationManager.get(level);
            manager.onDonation(player, village, value);
        }
    }

    /**
     * 处理玩家完成任务事件（手动调用）
     */
    public static void onPlayerCompleteQuest(ServerPlayer player, Village village, int bonusRep) {
        if (player.level() instanceof ServerLevel level) {
            ReputationManager manager = ReputationManager.get(level);
            manager.onQuestComplete(player, village, bonusRep);
        }
    }

    /**
     * 处理交易完成事件（手动调用）
     */
    public static void onTradeComplete(ServerPlayer player, Village village) {
        if (player.level() instanceof ServerLevel level) {
            ReputationManager manager = ReputationManager.get(level);
            manager.onTradeComplete(player, village);
        }
    }
}
