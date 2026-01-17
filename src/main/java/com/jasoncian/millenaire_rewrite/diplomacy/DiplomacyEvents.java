package com.jasoncian.millenaire_rewrite.diplomacy;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 外交事件处理器 - 自动更新村庄间关系
 *
 * 监听：
 * - 村民被攻击/杀死（村庄间）
 * - 世界tick（定期更新）
 *
 * @author Based on OldSource diplomacy events
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID)
public class DiplomacyEvents {

    // ================ 村民伤害/死亡事件 ================

    /**
     * 处理村民被攻击事件（村庄间冲突）
     */
    @SubscribeEvent
    public static void onVillagerDamage(LivingDamageEvent.Post event) {
        // 检查受害者是否为Millenaire村民
        if (!(event.getEntity() instanceof MillVillager victim)) {
            return;
        }

        // 检查攻击者是否为另一个Millenaire村民
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof MillVillager attackerVillager)) {
            return;
        }

        // 检查是否来自不同村庄
        UUID victimVillageId = getVillageId(victim);
        UUID attackerVillageId = getVillageId(attackerVillager);

        if (victimVillageId == null || attackerVillageId == null) {
            return;
        }

        if (victimVillageId.equals(attackerVillageId)) {
            return; // 同一村庄内的冲突不影响外交
        }

        // 更新外交关系
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }

        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        long currentTime = level.getGameTime();

        diplomacyManager.onAttackVillager(attackerVillageId, victimVillageId, currentTime);

        MillenaireRewrite.LOGGER.debug("Villager from {} attacked villager from {}, updating diplomacy",
            attackerVillageId, victimVillageId);
    }

    /**
     * 处理村民死亡事件（村庄间冲突）
     */
    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        // 检查死者是否为Millenaire村民
        if (!(event.getEntity() instanceof MillVillager victim)) {
            return;
        }

        // 检查击杀者是否为另一个Millenaire村民
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof MillVillager killerVillager)) {
            return;
        }

        // 检查是否来自不同村庄
        UUID victimVillageId = getVillageId(victim);
        UUID killerVillageId = getVillageId(killerVillager);

        if (victimVillageId == null || killerVillageId == null) {
            return;
        }

        if (victimVillageId.equals(killerVillageId)) {
            return; // 同一村庄内的死亡不影响外交
        }

        // 更新外交关系
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }

        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        long currentTime = level.getGameTime();

        diplomacyManager.onKillVillager(killerVillageId, victimVillageId, currentTime);

        MillenaireRewrite.LOGGER.info("Villager from {} killed villager from {}, diplomacy severely affected",
            killerVillageId, victimVillageId);

        // 检查是否应该宣战
        checkWarDeclaration(level, killerVillageId, victimVillageId, currentTime);
    }

    // ================ 世界Tick事件 ================

    /**
     * 处理世界tick（定期更新外交）
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        // 限制更新频率
        if (level.getGameTime() % 1000 != 0) {
            return;
        }

        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        diplomacyManager.tick(level);
    }

    // ================ 辅助方法 ================

    /**
     * 获取村民所属村庄的UUID
     */
    @Nullable
    private static UUID getVillageId(MillVillager villager) {
        Village village = villager.getHomeVillage();
        return village != null ? village.getVillageId() : null;
    }

    /**
     * 检查是否应该自动宣战
     */
    private static void checkWarDeclaration(ServerLevel level, UUID aggressorId, UUID victimId, long currentTime) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        VillageDiplomacy diplomacy = diplomacyManager.getRelation(aggressorId, victimId);

        // 如果关系已经是战争或足够敌对，自动宣战
        if (!diplomacy.isAtWar() && diplomacy.getCurrentRelation() == VillageRelation.WAR) {
            diplomacy.declareWar(currentTime);

            MillenaireRewrite.LOGGER.info("Villages {} and {} are now at war!", aggressorId, victimId);

            // 通知盟友
            notifyAllies(level, aggressorId, victimId, currentTime);
        }
    }

    /**
     * 通知盟友关于战争
     */
    private static void notifyAllies(ServerLevel level, UUID villageA, UUID villageB, long currentTime) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);

        // 获取双方的盟友
        var alliesA = diplomacyManager.getAllies(villageA);
        var alliesB = diplomacyManager.getAllies(villageB);

        // 盟友关系可能导致他们对敌方也变得敌对
        for (UUID allyA : alliesA) {
            if (!allyA.equals(villageB)) {
                // 盟友对敌人关系恶化
                diplomacyManager.modifyRelation(allyA, villageB, -50,
                    "Allied village at war", currentTime);
            }
        }

        for (UUID allyB : alliesB) {
            if (!allyB.equals(villageA)) {
                diplomacyManager.modifyRelation(allyB, villageA, -50,
                    "Allied village at war", currentTime);
            }
        }
    }

    // ================ 公共API ================

    /**
     * 手动触发村庄间贸易事件
     */
    public static void onVillageTrade(ServerLevel level, Village villageA, Village villageB) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        long currentTime = level.getGameTime();

        diplomacyManager.onTrade(villageA, villageB, currentTime);
    }

    /**
     * 手动触发村庄援助事件
     */
    public static void onVillageAssist(ServerLevel level, Village helper, Village receiver) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        long currentTime = level.getGameTime();

        diplomacyManager.onHelpBuild(helper, receiver, currentTime);
    }

    /**
     * 手动触发共同防御事件
     */
    public static void onJointDefense(ServerLevel level, Village villageA, Village villageB) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        long currentTime = level.getGameTime();

        diplomacyManager.onJointDefense(villageA, villageB, currentTime);
    }

    /**
     * 手动触发村庄袭击事件
     */
    public static void onVillageRaid(ServerLevel level, UUID attackerVillage, UUID victimVillage) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        long currentTime = level.getGameTime();

        diplomacyManager.onRaid(attackerVillage, victimVillage, currentTime);
    }

    /**
     * 初始化新村庄的外交关系
     */
    public static void onVillageCreated(ServerLevel level, Village newVillage) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        diplomacyManager.initializeVillageRelations(newVillage, level);

        MillenaireRewrite.LOGGER.info("Initialized diplomatic relations for new village: {}",
            newVillage.getName());
    }

    /**
     * 清理被移除村庄的外交关系
     */
    public static void onVillageRemoved(ServerLevel level, UUID villageId) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        diplomacyManager.removeVillageRelations(villageId);

        MillenaireRewrite.LOGGER.info("Cleaned up diplomatic relations for removed village: {}", villageId);
    }

    /**
     * 检查两个村庄是否可以进行贸易
     */
    public static boolean canVillagesTrade(ServerLevel level, UUID villageA, UUID villageB) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        return diplomacyManager.canTrade(villageA, villageB);
    }

    /**
     * 获取两个村庄之间的贸易价格修正
     */
    public static float getTradeModifier(ServerLevel level, UUID villageA, UUID villageB) {
        DiplomacyManager diplomacyManager = DiplomacyManager.get(level);
        VillageRelation relation = diplomacyManager.getRelationLevel(villageA, villageB);
        return relation.getTradeModifier();
    }
}
