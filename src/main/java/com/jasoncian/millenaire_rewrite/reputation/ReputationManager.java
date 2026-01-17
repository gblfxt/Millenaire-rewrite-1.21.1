package com.jasoncian.millenaire_rewrite.reputation;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 声望管理器 - 管理所有玩家与村庄/文化的声望关系
 *
 * 功能：
 * - 存储和加载声望数据
 * - 提供声望查询和修改接口
 * - 处理声望变化事件
 * - 计算交易价格修正
 *
 * @author Based on OldSource reputation management
 * @version 1.0.0
 */
public class ReputationManager extends SavedData {

    // ================ 常量 ================

    private static final String DATA_NAME = MillenaireRewrite.MOD_ID + "_reputation";

    // ================ 声望变化值 ================

    /** 交易完成 */
    public static final int REP_TRADE_COMPLETE = 1;

    /** 帮助村民完成任务 */
    public static final int REP_QUEST_COMPLETE = 10;

    /** 捐赠资源 */
    public static final int REP_DONATION = 5;

    /** 帮助建造 */
    public static final int REP_HELP_BUILD = 3;

    /** 保护村庄 */
    public static final int REP_DEFEND_VILLAGE = 15;

    /** 攻击村民 */
    public static final int REP_ATTACK_VILLAGER = -50;

    /** 杀死村民 */
    public static final int REP_KILL_VILLAGER = -200;

    /** 偷窃 */
    public static final int REP_THEFT = -30;

    /** 破坏建筑 */
    public static final int REP_VANDALISM = -20;

    // ================ 数据 ================

    /** 所有玩家的声望数据（玩家UUID -> 声望数据） */
    private final Map<UUID, PlayerReputation> playerReputations = new HashMap<>();

    // ================ 构造函数 ================

    public ReputationManager() {
        // 默认构造函数
    }

    // ================ SavedData实现 ================

    /**
     * 从NBT加载
     */
    public static ReputationManager load(CompoundTag tag, HolderLookup.Provider registries) {
        ReputationManager manager = new ReputationManager();

        ListTag playerList = tag.getList("PlayerReputations", 10);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag playerTag = playerList.getCompound(i);
            PlayerReputation reputation = PlayerReputation.fromNbt(playerTag);
            manager.playerReputations.put(reputation.getPlayerId(), reputation);
        }

        MillenaireRewrite.LOGGER.info("Loaded reputation data for {} players", manager.playerReputations.size());
        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag playerList = new ListTag();
        for (PlayerReputation reputation : playerReputations.values()) {
            playerList.add(reputation.save());
        }
        tag.put("PlayerReputations", playerList);

        return tag;
    }

    // ================ 静态访问 ================

    /**
     * 获取维度的声望管理器
     */
    public static ReputationManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(ReputationManager::new, ReputationManager::load),
            DATA_NAME
        );
    }

    // ================ 玩家声望访问 ================

    /**
     * 获取玩家的声望数据（如果不存在则创建）
     */
    public PlayerReputation getOrCreatePlayerReputation(Player player) {
        return getOrCreatePlayerReputation(player.getUUID(), player.getName().getString());
    }

    /**
     * 获取玩家的声望数据（通过UUID）
     */
    public PlayerReputation getOrCreatePlayerReputation(UUID playerId, String playerName) {
        PlayerReputation reputation = playerReputations.get(playerId);
        if (reputation == null) {
            reputation = new PlayerReputation(playerId, playerName);
            playerReputations.put(playerId, reputation);
            setDirty();
        } else if (!reputation.getPlayerName().equals(playerName)) {
            reputation.setPlayerName(playerName);
            setDirty();
        }
        return reputation;
    }

    /**
     * 获取玩家的声望数据（可能返回null）
     */
    @Nullable
    public PlayerReputation getPlayerReputation(UUID playerId) {
        return playerReputations.get(playerId);
    }

    // ================ 声望查询 ================

    /**
     * 获取玩家与村庄的声望等级
     */
    public ReputationLevel getReputationLevel(Player player, Village village) {
        PlayerReputation reputation = getOrCreatePlayerReputation(player);
        return reputation.getEffectiveReputationLevel(village.getVillageId(), village.getCulture());
    }

    /**
     * 获取玩家与村庄的声望点数
     */
    public int getReputationPoints(Player player, Village village) {
        PlayerReputation reputation = getOrCreatePlayerReputation(player);
        return reputation.getEffectiveReputation(village.getVillageId(), village.getCulture());
    }

    /**
     * 获取玩家与文化的声望等级
     */
    public ReputationLevel getCultureReputationLevel(Player player, Culture culture) {
        PlayerReputation reputation = getOrCreatePlayerReputation(player);
        return reputation.getCultureReputationLevel(culture);
    }

    /**
     * 检查玩家是否可以与村庄交易
     */
    public boolean canTrade(Player player, Village village) {
        return getReputationLevel(player, village).canTrade();
    }

    /**
     * 检查村庄是否对玩家敌对
     */
    public boolean isHostile(Player player, Village village) {
        return getReputationLevel(player, village).isHostile();
    }

    // ================ 声望修改 ================

    /**
     * 增加与村庄的声望（也会影响文化声望）
     */
    public PlayerReputation.ReputationChangeResult addReputation(
            Player player, Village village, int amount, String reason) {

        PlayerReputation reputation = getOrCreatePlayerReputation(player);
        PlayerReputation.ReputationChangeResult result =
            reputation.addVillageReputation(village.getVillageId(), amount, reason);

        // 同时影响文化声望（一半的值）
        reputation.addCultureReputation(village.getCulture(), amount / 2, reason);

        setDirty();

        // 检查等级变化并通知玩家
        if (result.levelChanged() && player instanceof ServerPlayer serverPlayer) {
            notifyReputationChange(serverPlayer, village, result);
        }

        return result;
    }

    /**
     * 增加与文化的声望
     */
    public PlayerReputation.ReputationChangeResult addCultureReputation(
            Player player, Culture culture, int amount, String reason) {

        PlayerReputation reputation = getOrCreatePlayerReputation(player);
        PlayerReputation.ReputationChangeResult result =
            reputation.addCultureReputation(culture, amount, reason);

        setDirty();

        return result;
    }

    // ================ 预定义声望事件 ================

    /**
     * 交易完成时增加声望
     */
    public void onTradeComplete(Player player, Village village) {
        addReputation(player, village, REP_TRADE_COMPLETE, "Completed trade");
    }

    /**
     * 完成任务时增加声望
     */
    public void onQuestComplete(Player player, Village village, int bonusRep) {
        addReputation(player, village, REP_QUEST_COMPLETE + bonusRep, "Completed quest");
    }

    /**
     * 捐赠资源时增加声望
     */
    public void onDonation(Player player, Village village, int value) {
        // 根据捐赠价值计算声望
        int rep = Math.min(REP_DONATION + value / 10, 50);
        addReputation(player, village, rep, "Donated resources");
    }

    /**
     * 帮助建造时增加声望
     */
    public void onHelpBuild(Player player, Village village) {
        addReputation(player, village, REP_HELP_BUILD, "Helped with construction");
    }

    /**
     * 保护村庄时增加声望
     */
    public void onDefendVillage(Player player, Village village, int enemiesKilled) {
        int rep = REP_DEFEND_VILLAGE + enemiesKilled * 5;
        addReputation(player, village, rep, "Defended village");
    }

    /**
     * 攻击村民时减少声望
     */
    public void onAttackVillager(Player player, Village village) {
        addReputation(player, village, REP_ATTACK_VILLAGER, "Attacked villager");
    }

    /**
     * 杀死村民时大幅减少声望
     */
    public void onKillVillager(Player player, Village village) {
        addReputation(player, village, REP_KILL_VILLAGER, "Killed villager");
    }

    /**
     * 偷窃时减少声望
     */
    public void onTheft(Player player, Village village) {
        addReputation(player, village, REP_THEFT, "Stole from village");
    }

    /**
     * 破坏建筑时减少声望
     */
    public void onVandalism(Player player, Village village) {
        addReputation(player, village, REP_VANDALISM, "Vandalized building");
    }

    // ================ 价格计算 ================

    /**
     * 计算交易价格修正系数
     * 返回值 > 1 表示加价，< 1 表示折扣
     */
    public double getPriceModifier(Player player, Village village) {
        ReputationLevel level = getReputationLevel(player, village);
        // priceModifier是折扣百分比，需要转换为价格系数
        // 例如：0.10 折扣 -> 价格系数 0.90
        return 1.0 - level.getPriceModifier();
    }

    /**
     * 计算调整后的价格
     */
    public int getAdjustedPrice(Player player, Village village, int basePrice) {
        double modifier = getPriceModifier(player, village);
        return Math.max(1, (int) Math.round(basePrice * modifier));
    }

    // ================ 通知 ================

    /**
     * 通知玩家声望变化
     */
    private void notifyReputationChange(ServerPlayer player, Village village,
                                        PlayerReputation.ReputationChangeResult result) {
        // 这里可以发送聊天消息或显示UI通知
        if (result.levelIncreased()) {
            MillenaireRewrite.LOGGER.info("Player {} reputation with {} increased to {}",
                player.getName().getString(), village.getName(), result.newLevel().getDisplayName());
        } else if (result.levelDecreased()) {
            MillenaireRewrite.LOGGER.info("Player {} reputation with {} decreased to {}",
                player.getName().getString(), village.getName(), result.newLevel().getDisplayName());
        }
    }

    // ================ 统计信息 ================

    /**
     * 获取已记录的玩家数量
     */
    public int getTrackedPlayerCount() {
        return playerReputations.size();
    }

    /**
     * 获取所有玩家的声望数据（只读）
     */
    public Collection<PlayerReputation> getAllPlayerReputations() {
        return Collections.unmodifiableCollection(playerReputations.values());
    }

    /**
     * 获取与特定村庄有交互的所有玩家
     */
    public List<PlayerReputation> getPlayersWithVillageReputation(UUID villageId) {
        return playerReputations.values().stream()
            .filter(rep -> rep.getVillageReputation(villageId) != 0)
            .toList();
    }

    /**
     * 获取特定声望等级的玩家（对于某村庄）
     */
    public List<PlayerReputation> getPlayersAtLevel(UUID villageId, ReputationLevel level) {
        return playerReputations.values().stream()
            .filter(rep -> rep.getVillageReputationLevel(villageId) == level)
            .toList();
    }
}
