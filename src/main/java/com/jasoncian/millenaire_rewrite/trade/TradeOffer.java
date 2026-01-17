package com.jasoncian.millenaire_rewrite.trade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 交易报价 - 表示单个交易选项
 *
 * 包含：
 * - 村民出售的物品
 * - 玩家需要支付的价格（以铜第纳尔计）
 * - 交易次数限制
 * - 是否可用
 *
 * @author Based on OldSource trading
 * @version 1.0.0
 */
public class TradeOffer {

    // ================ 数据 ================

    /** 出售的物品 */
    private final ItemStack sellingItem;

    /** 价格（铜第纳尔） */
    private int priceInDeniers;

    /** 最大交易次数（-1表示无限） */
    private int maxUses;

    /** 已使用次数 */
    private int uses;

    /** 是否禁用 */
    private boolean disabled;

    /** 特殊标签（如文化限定） */
    private String specialTag;

    // ================ 构造函数 ================

    public TradeOffer(ItemStack sellingItem, int priceInDeniers) {
        this(sellingItem, priceInDeniers, -1);
    }

    public TradeOffer(ItemStack sellingItem, int priceInDeniers, int maxUses) {
        this.sellingItem = sellingItem.copy();
        this.priceInDeniers = priceInDeniers;
        this.maxUses = maxUses;
        this.uses = 0;
        this.disabled = false;
        this.specialTag = "";
    }

    // ================ 交易逻辑 ================

    /**
     * 检查是否可以交易
     */
    public boolean canUse() {
        if (disabled) return false;
        if (maxUses > 0 && uses >= maxUses) return false;
        return true;
    }

    /**
     * 执行交易
     * @return 出售的物品副本
     */
    public ItemStack use() {
        if (!canUse()) {
            return ItemStack.EMPTY;
        }

        uses++;

        // 检查是否达到上限
        if (maxUses > 0 && uses >= maxUses) {
            disabled = true;
        }

        return sellingItem.copy();
    }

    /**
     * 重置交易（用于刷新）
     */
    public void reset() {
        uses = 0;
        disabled = false;
    }

    /**
     * 增加价格（通胀）
     */
    public void increasePrice(int amount) {
        this.priceInDeniers = Math.max(1, this.priceInDeniers + amount);
    }

    /**
     * 降低价格（折扣）
     */
    public void decreasePrice(int amount) {
        this.priceInDeniers = Math.max(1, this.priceInDeniers - amount);
    }

    // ================ NBT序列化 ================

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // 保存物品信息（不使用ItemStack.save以避免需要Provider）
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(sellingItem.getItem());
        tag.putString("ItemId", itemId.toString());
        tag.putInt("ItemCount", sellingItem.getCount());

        tag.putInt("Price", priceInDeniers);
        tag.putInt("MaxUses", maxUses);
        tag.putInt("Uses", uses);
        tag.putBoolean("Disabled", disabled);
        tag.putString("SpecialTag", specialTag);

        return tag;
    }

    public static TradeOffer load(CompoundTag tag) {
        // 加载物品信息
        String itemIdStr = tag.getString("ItemId");
        int count = tag.getInt("ItemCount");

        ResourceLocation itemId = ResourceLocation.tryParse(itemIdStr);
        ItemStack item = ItemStack.EMPTY;
        if (itemId != null) {
            Item itemType = BuiltInRegistries.ITEM.get(itemId);
            if (itemType != null) {
                item = new ItemStack(itemType, count > 0 ? count : 1);
            }
        }

        int price = tag.getInt("Price");
        int maxUses = tag.getInt("MaxUses");

        TradeOffer offer = new TradeOffer(item, price, maxUses);
        offer.uses = tag.getInt("Uses");
        offer.disabled = tag.getBoolean("Disabled");
        offer.specialTag = tag.getString("SpecialTag");

        return offer;
    }

    // ================ Getters/Setters ================

    public ItemStack getSellingItem() {
        return sellingItem.copy();
    }

    public int getPriceInDeniers() {
        return priceInDeniers;
    }

    public void setPriceInDeniers(int price) {
        this.priceInDeniers = Math.max(1, price);
    }

    public int getMaxUses() {
        return maxUses;
    }

    public int getUses() {
        return uses;
    }

    public int getRemainingUses() {
        if (maxUses < 0) return -1;
        return Math.max(0, maxUses - uses);
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getSpecialTag() {
        return specialTag;
    }

    public void setSpecialTag(String tag) {
        this.specialTag = tag != null ? tag : "";
    }

    /**
     * 获取价格显示字符串
     */
    public String getPriceDisplay() {
        int gold = priceInDeniers / 6400;
        int silver = (priceInDeniers % 6400) / 64;
        int copper = priceInDeniers % 64;

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

    @Override
    public String toString() {
        return String.format("TradeOffer{item=%s, price=%d, uses=%d/%d}",
            sellingItem.getDisplayName().getString(),
            priceInDeniers, uses, maxUses);
    }
}
