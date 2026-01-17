package com.jasoncian.millenaire_rewrite.menu;

import com.jasoncian.millenaire_rewrite.core.ModMenuTypes;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.trade.TradeOffer;
import com.jasoncian.millenaire_rewrite.trade.TradeOfferList;
import com.jasoncian.millenaire_rewrite.trade.VillagerTrader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 交易菜单 - 用于与村民进行交易
 *
 * 提供功能：
 * - 显示可用交易
 * - 执行交易
 * - 货币显示
 *
 * @author Based on OldSource Trading
 * @version 1.0.0
 */
public class TradingMenu extends AbstractContainerMenu {

    // ================ 数据 ================

    /** 关联的村民实体 */
    @Nullable
    private final MillVillager villager;

    /** 玩家 */
    private final Player player;

    /** 客户端同步的交易列表 */
    private TradeOfferList clientOffers;

    /** 选中的交易索引 */
    private int selectedOffer = -1;

    // ================ 构造函数 ================

    /**
     * 服务端构造函数
     */
    public TradingMenu(int containerId, Inventory playerInventory, MillVillager villager) {
        super(ModMenuTypes.TRADING.get(), containerId);
        this.player = playerInventory.player;
        this.villager = villager;
        this.clientOffers = new TradeOfferList();

        // 开始交易
        if (villager != null) {
            villager.getTrader().startTrading(player);
        }
    }

    /**
     * 客户端构造函数 - 从网络数据包创建
     */
    public TradingMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        super(ModMenuTypes.TRADING.get(), containerId);
        this.player = playerInventory.player;

        // 读取村民ID
        int villagerEntityId = buf.readInt();
        var entity = playerInventory.player.level().getEntity(villagerEntityId);
        this.villager = entity instanceof MillVillager mv ? mv : null;

        // 读取交易数量
        int offerCount = buf.readInt();
        this.clientOffers = new TradeOfferList();

        for (int i = 0; i < offerCount; i++) {
            // 读取物品信息
            String itemIdStr = buf.readUtf();
            int itemCount = buf.readInt();
            int price = buf.readInt();
            int maxUses = buf.readInt();
            int uses = buf.readInt();
            boolean disabled = buf.readBoolean();

            // 重建ItemStack
            ItemStack item = ItemStack.EMPTY;
            ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);
            if (itemId != null) {
                Item itemType = BuiltInRegistries.ITEM.get(itemId);
                if (itemType != null) {
                    item = new ItemStack(itemType, itemCount > 0 ? itemCount : 1);
                }
            }

            TradeOffer offer = new TradeOffer(item, price, maxUses);
            if (disabled) {
                offer.setDisabled(true);
            }
            clientOffers.add(offer);
        }
    }

    /**
     * 写入网络数据（服务端调用）
     */
    public static void writeToBuffer(FriendlyByteBuf buf, MillVillager villager) {
        buf.writeInt(villager.getId());

        VillagerTrader trader = villager.getTrader();
        TradeOfferList offers = trader.getOffers();

        buf.writeInt(offers.size());

        for (TradeOffer offer : offers) {
            // 写入物品信息
            ItemStack sellingItem = offer.getSellingItem();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(sellingItem.getItem());
            buf.writeUtf(itemId.toString());
            buf.writeInt(sellingItem.getCount());

            buf.writeInt(offer.getPriceInDeniers());
            buf.writeInt(offer.getMaxUses());
            buf.writeInt(offer.getUses());
            buf.writeBoolean(offer.isDisabled());
        }
    }

    // ================ 菜单功能 ================

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (villager == null || !villager.isAlive()) {
            return false;
        }
        return player.distanceToSqr(villager) <= 64.0D;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        // 停止交易
        if (villager != null) {
            villager.getTrader().stopTrading();
        }
    }

    // ================ 交易操作 ================

    /**
     * 选择交易
     */
    public void selectOffer(int index) {
        if (index >= 0 && index < getOfferCount()) {
            this.selectedOffer = index;
        } else {
            this.selectedOffer = -1;
        }
    }

    /**
     * 执行选中的交易
     */
    public boolean executeSelectedTrade() {
        if (selectedOffer < 0) {
            return false;
        }

        return executeTrade(selectedOffer);
    }

    /**
     * 执行指定交易
     */
    public boolean executeTrade(int offerIndex) {
        if (villager == null) {
            return false;
        }

        // 服务端执行
        if (player instanceof ServerPlayer) {
            VillagerTrader trader = villager.getTrader();
            boolean success = trader.executeTrade(offerIndex, player);

            if (success) {
                // 通知客户端更新
                broadcastChanges();
            }

            return success;
        }

        return false;
    }

    /**
     * 请求执行交易（客户端调用）
     */
    public void requestTrade(int offerIndex) {
        // 这会被网络包处理
        if (villager != null && !player.level().isClientSide()) {
            executeTrade(offerIndex);
        }
    }

    // ================ Getters ================

    @Nullable
    public MillVillager getVillager() {
        return villager;
    }

    public Player getPlayer() {
        return player;
    }

    public String getVillagerName() {
        return villager != null ? villager.getFullName() : "";
    }

    public String getVillagerProfession() {
        return villager != null ? villager.getProfession().getDisplayName() : "";
    }

    public String getVillagerCulture() {
        return villager != null ? villager.getCulture().getDisplayName() : "";
    }

    public TradeOfferList getOffers() {
        // 服务端返回实际交易，客户端返回同步的交易
        if (villager != null && !player.level().isClientSide()) {
            return villager.getTrader().getOffers();
        }
        return clientOffers;
    }

    public int getOfferCount() {
        return getOffers().size();
    }

    public TradeOffer getOffer(int index) {
        return getOffers().get(index);
    }

    public int getSelectedOffer() {
        return selectedOffer;
    }

    /**
     * 获取玩家持有的货币总额（铜第纳尔）
     */
    public int getPlayerMoney() {
        int total = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            String itemId = stack.getItem().toString();
            if (itemId.contains("denier_or")) {
                total += stack.getCount() * 6400;
            } else if (itemId.contains("denier_argent")) {
                total += stack.getCount() * 64;
            } else if (itemId.contains("denier")) {
                total += stack.getCount();
            }
        }

        return total;
    }

    /**
     * 格式化货币显示
     */
    public String formatMoney(int deniers) {
        int gold = deniers / 6400;
        int silver = (deniers % 6400) / 64;
        int copper = deniers % 64;

        StringBuilder sb = new StringBuilder();
        if (gold > 0) {
            sb.append(gold).append("g ");
        }
        if (silver > 0 || gold > 0) {
            sb.append(silver).append("s ");
        }
        sb.append(copper).append("c");

        return sb.toString().trim();
    }
}
