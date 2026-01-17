package com.jasoncian.millenaire_rewrite.trade;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 村民交易者 - 管理村民的交易功能
 *
 * 处理：
 * - 交易列表生成
 * - 交易执行
 * - 货币处理
 * - 交易刷新
 *
 * @author Based on OldSource trading
 * @version 1.0.0
 */
public class VillagerTrader {

    // ================ 数据 ================

    /** 所属村民 */
    private final MillVillager villager;

    /** 交易列表 */
    private TradeOfferList offers;

    /** 当前交易玩家 */
    @Nullable
    private Player tradingPlayer;

    /** 交易刷新时间（tick） */
    private int refreshTimer;

    /** 刷新间隔（默认24000 tick = 1游戏天） */
    private static final int REFRESH_INTERVAL = 24000;

    // ================ 构造函数 ================

    public VillagerTrader(MillVillager villager) {
        this.villager = villager;
        this.offers = new TradeOfferList();
        this.refreshTimer = 0;
    }

    // ================ 交易管理 ================

    /**
     * 初始化交易列表
     */
    public void initializeTrades() {
        Culture culture = villager.getCulture();
        VillagerProfession profession = villager.getProfession();
        int villageLevel = getVillageLevel();

        this.offers = CultureTrades.getTradesFor(culture, profession, villageLevel);
    }

    /**
     * 获取村庄等级
     */
    private int getVillageLevel() {
        Village village = villager.getHomeVillage();
        if (village != null) {
            // 根据建筑数量计算等级
            int buildings = village.getBuildingCount();
            if (buildings >= 20) return 5;
            if (buildings >= 15) return 4;
            if (buildings >= 10) return 3;
            if (buildings >= 5) return 2;
        }
        return 1;
    }

    /**
     * 刷新交易列表
     */
    public void refreshTrades() {
        // 重置现有交易
        offers.resetAll();

        // 可能添加新交易
        int villageLevel = getVillageLevel();
        TradeOfferList newOffers = CultureTrades.getTradesFor(
            villager.getCulture(),
            villager.getProfession(),
            villageLevel
        );

        // 合并新交易（避免重复）
        for (TradeOffer newOffer : newOffers) {
            boolean exists = false;
            for (TradeOffer existing : offers) {
                if (ItemStack.isSameItem(existing.getSellingItem(), newOffer.getSellingItem())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                offers.add(newOffer);
            }
        }

        refreshTimer = 0;
    }

    // ================ 交易执行 ================

    /**
     * 执行交易
     *
     * @param offerIndex 交易索引
     * @param player 玩家
     * @return 是否成功
     */
    public boolean executeTrade(int offerIndex, Player player) {
        TradeOffer offer = offers.get(offerIndex);
        if (offer == null || !offer.canUse()) {
            return false;
        }

        int price = offer.getPriceInDeniers();

        // 检查玩家货币
        if (!hasEnoughMoney(player, price)) {
            return false;
        }

        // 扣除货币
        if (!deductMoney(player, price)) {
            return false;
        }

        // 给予物品
        ItemStack result = offer.use();
        if (!result.isEmpty()) {
            if (!player.getInventory().add(result)) {
                // 背包满了，掉落物品
                player.drop(result, false);
            }

            // 将货币添加到村庄
            Village village = villager.getHomeVillage();
            if (village != null) {
                village.addDeniers(price);
            }

            return true;
        }

        return false;
    }

    /**
     * 检查玩家是否有足够货币
     */
    private boolean hasEnoughMoney(Player player, int amount) {
        // TODO: 实现真正的货币系统检查
        // 目前简化处理：检查经验值或特定物品

        // 临时实现：检查铜第纳尔物品数量
        int totalDeniers = countPlayerDeniers(player);
        return totalDeniers >= amount;
    }

    /**
     * 计算玩家持有的第纳尔总数
     */
    private int countPlayerDeniers(Player player) {
        int total = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // 检查是否为货币物品
            String itemId = stack.getItem().toString();
            if (itemId.contains("denier_or")) {
                total += stack.getCount() * 6400; // 金币 = 6400铜
            } else if (itemId.contains("denier_argent")) {
                total += stack.getCount() * 64; // 银币 = 64铜
            } else if (itemId.contains("denier")) {
                total += stack.getCount(); // 铜币 = 1
            }
        }

        return total;
    }

    /**
     * 从玩家扣除货币
     */
    private boolean deductMoney(Player player, int amount) {
        // TODO: 实现真正的货币扣除系统
        // 临时实现：扣除第纳尔物品

        int remaining = amount;

        // 优先扣除铜币，然后银币，最后金币
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String itemId = stack.getItem().toString();
            int value = 0;

            if (itemId.contains("denier_or")) {
                value = 6400;
            } else if (itemId.contains("denier_argent")) {
                value = 64;
            } else if (itemId.contains("denier")) {
                value = 1;
            }

            if (value > 0) {
                int toRemove = Math.min(stack.getCount(), (remaining + value - 1) / value);
                remaining -= toRemove * value;
                stack.shrink(toRemove);

                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
            }
        }

        // 如果有多扣的（因为货币面值问题），应该找零
        // 简化处理：暂时忽略找零

        return remaining <= 0;
    }

    // ================ Tick更新 ================

    /**
     * 每tick更新
     */
    public void tick() {
        refreshTimer++;

        // 自动刷新交易
        if (refreshTimer >= REFRESH_INTERVAL) {
            refreshTrades();
        }

        // 检查交易玩家是否还在附近
        if (tradingPlayer != null) {
            if (tradingPlayer.isRemoved() ||
                tradingPlayer.distanceToSqr(villager) > 64.0) {
                stopTrading();
            }
        }
    }

    // ================ 交易会话 ================

    /**
     * 开始与玩家交易
     */
    public void startTrading(Player player) {
        this.tradingPlayer = player;

        // 如果没有交易，初始化
        if (offers.isEmpty()) {
            initializeTrades();
        }
    }

    /**
     * 停止交易
     */
    public void stopTrading() {
        this.tradingPlayer = null;
    }

    /**
     * 检查是否正在交易
     */
    public boolean isTrading() {
        return tradingPlayer != null;
    }

    // ================ NBT序列化 ================

    public void save(CompoundTag tag) {
        tag.put("Offers", offers.save());
        tag.putInt("RefreshTimer", refreshTimer);
    }

    public void load(CompoundTag tag) {
        if (tag.contains("Offers")) {
            this.offers = TradeOfferList.load(tag.getCompound("Offers"));
        }
        this.refreshTimer = tag.getInt("RefreshTimer");
    }

    // ================ Getters ================

    public TradeOfferList getOffers() {
        return offers;
    }

    @Nullable
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    public MillVillager getVillager() {
        return villager;
    }
}
