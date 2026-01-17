package com.jasoncian.millenaire_rewrite.diplomacy;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 外交管理器 - 管理所有村庄间的外交关系
 *
 * 功能：
 * - 存储和加载外交数据
 * - 管理村庄间关系
 * - 处理外交事件
 * - 文化默认关系
 *
 * @author Based on OldSource diplomacy management
 * @version 1.0.0
 */
public class DiplomacyManager extends SavedData {

    // ================ 常量 ================

    private static final String DATA_NAME = MillenaireRewrite.MOD_ID + "_diplomacy";

    /** 外交点数自然衰减/恢复速率（每游戏天） */
    public static final int DAILY_DECAY_RATE = 1;

    /** 外交更新间隔（tick，约1小时） */
    public static final int UPDATE_INTERVAL = 72000;

    // ================ 关系变化值 ================

    /** 贸易完成 */
    public static final int RELATION_TRADE = 1;

    /** 援助建造 */
    public static final int RELATION_HELP_BUILD = 5;

    /** 共同防御 */
    public static final int RELATION_JOINT_DEFENSE = 10;

    /** 攻击对方村民 */
    public static final int RELATION_ATTACK_VILLAGER = -20;

    /** 杀死对方村民 */
    public static final int RELATION_KILL_VILLAGER = -50;

    /** 袭击村庄 */
    public static final int RELATION_RAID = -100;

    /** 抢夺资源 */
    public static final int RELATION_THEFT = -15;

    // ================ 数据 ================

    /** 所有外交关系（关系键 -> 外交数据） */
    private final Map<String, VillageDiplomacy> relations = new HashMap<>();

    /** 上次更新时间 */
    private long lastUpdateTime = 0;

    // ================ 文化默认关系 ================

    /** 文化间默认关系点数 */
    private static final Map<CulturePair, Integer> CULTURE_DEFAULT_RELATIONS = new HashMap<>();

    static {
        // 设置文化间的默认关系
        // 正数表示友好，负数表示敌对

        // 同文化村庄天然友好
        // 这在运行时处理

        // 诺曼与拜占庭历史冲突
        CULTURE_DEFAULT_RELATIONS.put(new CulturePair(Culture.NORMAN, Culture.BYZANTINE), -50);

        // 塞尔柱与拜占庭历史冲突
        CULTURE_DEFAULT_RELATIONS.put(new CulturePair(Culture.SELJUK, Culture.BYZANTINE), -100);

        // 玛雅与印度作为贸易伙伴
        CULTURE_DEFAULT_RELATIONS.put(new CulturePair(Culture.MAYAN, Culture.INDIAN), 25);

        // 日本与印度佛教联系
        CULTURE_DEFAULT_RELATIONS.put(new CulturePair(Culture.JAPANESE, Culture.INDIAN), 50);

        // 因纽特相对孤立
        CULTURE_DEFAULT_RELATIONS.put(new CulturePair(Culture.INUIT, Culture.NORMAN), -25);
        CULTURE_DEFAULT_RELATIONS.put(new CulturePair(Culture.INUIT, Culture.SELJUK), -25);
    }

    // ================ 构造函数 ================

    public DiplomacyManager() {
        // 默认构造
    }

    // ================ SavedData实现 ================

    /**
     * 从NBT加载
     */
    public static DiplomacyManager load(CompoundTag tag, HolderLookup.Provider registries) {
        DiplomacyManager manager = new DiplomacyManager();

        ListTag relationList = tag.getList("Relations", 10);
        for (int i = 0; i < relationList.size(); i++) {
            VillageDiplomacy diplomacy = VillageDiplomacy.fromNbt(relationList.getCompound(i));
            String key = makeKey(diplomacy.getVillageA(), diplomacy.getVillageB());
            manager.relations.put(key, diplomacy);
        }

        manager.lastUpdateTime = tag.getLong("LastUpdateTime");

        MillenaireRewrite.LOGGER.info("Loaded diplomacy data for {} village pairs", manager.relations.size());

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag relationList = new ListTag();
        for (VillageDiplomacy diplomacy : relations.values()) {
            relationList.add(diplomacy.save());
        }
        tag.put("Relations", relationList);
        tag.putLong("LastUpdateTime", lastUpdateTime);

        return tag;
    }

    // ================ 静态访问 ================

    /**
     * 获取维度的外交管理器
     */
    public static DiplomacyManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(DiplomacyManager::new, DiplomacyManager::load),
            DATA_NAME
        );
    }

    // ================ 关系查询 ================

    /**
     * 获取两个村庄之间的外交关系
     */
    public VillageDiplomacy getRelation(UUID villageA, UUID villageB) {
        String key = makeKey(villageA, villageB);
        return relations.computeIfAbsent(key, k -> new VillageDiplomacy(villageA, villageB));
    }

    /**
     * 获取两个村庄之间的关系等级
     */
    public VillageRelation getRelationLevel(UUID villageA, UUID villageB) {
        return getRelation(villageA, villageB).getCurrentRelation();
    }

    /**
     * 获取两个村庄之间的关系等级（使用Village对象）
     */
    public VillageRelation getRelationLevel(Village villageA, Village villageB) {
        return getRelationLevel(villageA.getVillageId(), villageB.getVillageId());
    }

    /**
     * 检查两个村庄是否敌对
     */
    public boolean areHostile(UUID villageA, UUID villageB) {
        return getRelationLevel(villageA, villageB).isHostile();
    }

    /**
     * 检查两个村庄是否处于战争
     */
    public boolean areAtWar(UUID villageA, UUID villageB) {
        return getRelation(villageA, villageB).isAtWar();
    }

    /**
     * 检查两个村庄是否可以贸易
     */
    public boolean canTrade(UUID villageA, UUID villageB) {
        return getRelationLevel(villageA, villageB).allowsTrade();
    }

    /**
     * 检查两个村庄是否是同盟
     */
    public boolean areAllied(UUID villageA, UUID villageB) {
        return getRelationLevel(villageA, villageB) == VillageRelation.ALLIED;
    }

    /**
     * 获取村庄的所有关系
     */
    public List<VillageDiplomacy> getRelationsFor(UUID villageId) {
        return relations.values().stream()
            .filter(d -> d.involves(villageId))
            .collect(Collectors.toList());
    }

    /**
     * 获取村庄的所有盟友
     */
    public List<UUID> getAllies(UUID villageId) {
        return relations.values().stream()
            .filter(d -> d.involves(villageId))
            .filter(d -> d.getCurrentRelation() == VillageRelation.ALLIED)
            .map(d -> d.getOtherVillage(villageId))
            .collect(Collectors.toList());
    }

    /**
     * 获取村庄的所有敌人
     */
    public List<UUID> getEnemies(UUID villageId) {
        return relations.values().stream()
            .filter(d -> d.involves(villageId))
            .filter(d -> d.getCurrentRelation().isHostile())
            .map(d -> d.getOtherVillage(villageId))
            .collect(Collectors.toList());
    }

    // ================ 关系修改 ================

    /**
     * 修改两个村庄之间的关系
     */
    public VillageDiplomacy.RelationChangeResult modifyRelation(
            UUID villageA, UUID villageB, int amount, String reason, long timestamp) {

        VillageDiplomacy diplomacy = getRelation(villageA, villageB);
        VillageDiplomacy.RelationChangeResult result = diplomacy.modifyRelation(amount, reason, timestamp);

        setDirty();

        if (result.relationChanged()) {
            MillenaireRewrite.LOGGER.info("Relation between villages {} and {} changed from {} to {}",
                villageA, villageB, result.oldRelation(), result.newRelation());
        }

        return result;
    }

    /**
     * 处理贸易事件
     */
    public void onTrade(Village villageA, Village villageB, long timestamp) {
        modifyRelation(villageA.getVillageId(), villageB.getVillageId(),
            RELATION_TRADE, "Trade completed", timestamp);
    }

    /**
     * 处理援助建造事件
     */
    public void onHelpBuild(Village helper, Village receiver, long timestamp) {
        modifyRelation(helper.getVillageId(), receiver.getVillageId(),
            RELATION_HELP_BUILD, "Construction assistance", timestamp);
    }

    /**
     * 处理共同防御事件
     */
    public void onJointDefense(Village villageA, Village villageB, long timestamp) {
        modifyRelation(villageA.getVillageId(), villageB.getVillageId(),
            RELATION_JOINT_DEFENSE, "Joint defense", timestamp);
    }

    /**
     * 处理攻击村民事件
     */
    public void onAttackVillager(UUID attackerVillage, UUID victimVillage, long timestamp) {
        modifyRelation(attackerVillage, victimVillage,
            RELATION_ATTACK_VILLAGER, "Villager attacked", timestamp);
    }

    /**
     * 处理杀死村民事件
     */
    public void onKillVillager(UUID attackerVillage, UUID victimVillage, long timestamp) {
        modifyRelation(attackerVillage, victimVillage,
            RELATION_KILL_VILLAGER, "Villager killed", timestamp);
    }

    /**
     * 处理袭击事件
     */
    public void onRaid(UUID attackerVillage, UUID victimVillage, long timestamp) {
        modifyRelation(attackerVillage, victimVillage,
            RELATION_RAID, "Village raided", timestamp);

        // 袭击可能导致战争
        VillageDiplomacy diplomacy = getRelation(attackerVillage, victimVillage);
        if (diplomacy.getCurrentRelation() == VillageRelation.WAR ||
            diplomacy.getRelationPoints() <= VillageRelation.WAR.getMinPoints()) {
            diplomacy.declareWar(timestamp);
            setDirty();
        }
    }

    // ================ 外交行动 ================

    /**
     * 签订贸易协定
     */
    public boolean signTradeAgreement(UUID villageA, UUID villageB, long duration, long currentTime) {
        VillageDiplomacy diplomacy = getRelation(villageA, villageB);

        // 需要至少中立关系
        if (diplomacy.getCurrentRelation().isHostile()) {
            return false;
        }

        diplomacy.signTradeAgreement(duration, currentTime);
        setDirty();
        return true;
    }

    /**
     * 签订互不侵犯条约
     */
    public boolean signNonAggressionPact(UUID villageA, UUID villageB, long duration, long currentTime) {
        VillageDiplomacy diplomacy = getRelation(villageA, villageB);

        diplomacy.signNonAggressionPact(duration, currentTime);
        setDirty();
        return true;
    }

    /**
     * 宣战
     */
    public void declareWar(UUID aggressor, UUID target, long currentTime) {
        VillageDiplomacy diplomacy = getRelation(aggressor, target);
        diplomacy.declareWar(currentTime);
        setDirty();

        MillenaireRewrite.LOGGER.info("Village {} declared war on village {}", aggressor, target);
    }

    /**
     * 议和
     */
    public void makePeace(UUID villageA, UUID villageB, long currentTime) {
        VillageDiplomacy diplomacy = getRelation(villageA, villageB);

        if (diplomacy.isAtWar()) {
            diplomacy.endWar(currentTime);
            setDirty();

            MillenaireRewrite.LOGGER.info("Villages {} and {} made peace", villageA, villageB);
        }
    }

    /**
     * 建立同盟
     */
    public boolean formAlliance(UUID villageA, UUID villageB, long currentTime) {
        VillageDiplomacy diplomacy = getRelation(villageA, villageB);

        if (diplomacy.getRelationPoints() < VillageRelation.FRIENDLY.getMinPoints()) {
            return false;
        }

        diplomacy.formAlliance(currentTime);
        setDirty();

        MillenaireRewrite.LOGGER.info("Villages {} and {} formed an alliance", villageA, villageB);
        return true;
    }

    // ================ 文化关系 ================

    /**
     * 获取文化间的默认关系修正
     */
    public int getCultureRelationModifier(Culture cultureA, Culture cultureB) {
        if (cultureA == cultureB) {
            return 100; // 同文化天然友好
        }

        CulturePair pair = new CulturePair(cultureA, cultureB);
        return CULTURE_DEFAULT_RELATIONS.getOrDefault(pair, 0);
    }

    /**
     * 初始化新村庄的外交关系
     */
    public void initializeVillageRelations(Village newVillage, ServerLevel level) {
        VillageManager villageManager = VillageManager.get(level);
        Culture newCulture = newVillage.getCulture();
        long currentTime = level.getGameTime();

        for (Village otherVillage : villageManager.getAllVillages()) {
            if (otherVillage.getVillageId().equals(newVillage.getVillageId())) {
                continue;
            }

            // 获取或创建外交关系
            VillageDiplomacy diplomacy = getRelation(newVillage.getVillageId(), otherVillage.getVillageId());

            // 应用文化修正
            int cultureMod = getCultureRelationModifier(newCulture, otherVillage.getCulture());
            if (cultureMod != 0) {
                diplomacy.modifyRelation(cultureMod, "Cultural affinity", currentTime);
            }

            // 距离修正（近的村庄更可能友好或敌对）
            double distance = newVillage.getCenterPos().distSqr(otherVillage.getCenterPos());
            if (distance < 10000) { // 100格内
                // 根据文化决定是友好还是敌对
                int distanceMod = newCulture == otherVillage.getCulture() ? 25 : -10;
                diplomacy.modifyRelation(distanceMod, "Proximity", currentTime);
            }
        }

        setDirty();
    }

    // ================ Tick更新 ================

    /**
     * 定期更新外交状态
     */
    public void tick(ServerLevel level) {
        long currentTime = level.getGameTime();

        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }

        lastUpdateTime = currentTime;

        // 更新所有关系
        for (VillageDiplomacy diplomacy : relations.values()) {
            // 检查协定过期
            diplomacy.isTradeAgreementActive(currentTime);
            diplomacy.isNonAggressionPactActive(currentTime);

            // 自然衰减/恢复（向中立靠拢）
            int points = diplomacy.getRelationPoints();
            if (points > 0) {
                diplomacy.modifyRelation(-DAILY_DECAY_RATE, "Natural decay", currentTime);
            } else if (points < 0 && !diplomacy.isAtWar()) {
                diplomacy.modifyRelation(DAILY_DECAY_RATE, "Natural recovery", currentTime);
            }
        }

        setDirty();
    }

    // ================ 工具方法 ================

    /**
     * 生成关系键
     */
    private static String makeKey(UUID villageA, UUID villageB) {
        // 确保顺序一致
        if (villageA.compareTo(villageB) > 0) {
            return villageB.toString() + "_" + villageA.toString();
        }
        return villageA.toString() + "_" + villageB.toString();
    }

    /**
     * 清除涉及特定村庄的所有关系
     */
    public void removeVillageRelations(UUID villageId) {
        relations.entrySet().removeIf(entry -> entry.getValue().involves(villageId));
        setDirty();
    }

    /**
     * 获取统计信息
     */
    public Map<VillageRelation, Integer> getRelationStats() {
        Map<VillageRelation, Integer> stats = new EnumMap<>(VillageRelation.class);
        for (VillageRelation relation : VillageRelation.values()) {
            stats.put(relation, 0);
        }
        for (VillageDiplomacy diplomacy : relations.values()) {
            VillageRelation rel = diplomacy.getCurrentRelation();
            stats.put(rel, stats.get(rel) + 1);
        }
        return stats;
    }

    // ================ 内部类 ================

    /**
     * 文化对（用于默认关系映射）
     */
    private record CulturePair(Culture a, Culture b) {
        CulturePair {
            // 确保顺序一致
            if (a.ordinal() > b.ordinal()) {
                Culture temp = a;
                a = b;
                b = temp;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CulturePair that = (CulturePair) o;
            return (a == that.a && b == that.b) || (a == that.b && b == that.a);
        }

        @Override
        public int hashCode() {
            return a.hashCode() + b.hashCode();
        }
    }
}
