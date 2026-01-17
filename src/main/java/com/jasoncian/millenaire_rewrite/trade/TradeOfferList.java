package com.jasoncian.millenaire_rewrite.trade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 交易报价列表 - 管理村民的所有交易选项
 *
 * @author Based on OldSource trading
 * @version 1.0.0
 */
public class TradeOfferList implements Iterable<TradeOffer> {

    private final List<TradeOffer> offers = new ArrayList<>();

    // ================ 列表操作 ================

    public void add(TradeOffer offer) {
        offers.add(offer);
    }

    public void remove(int index) {
        if (index >= 0 && index < offers.size()) {
            offers.remove(index);
        }
    }

    public TradeOffer get(int index) {
        if (index >= 0 && index < offers.size()) {
            return offers.get(index);
        }
        return null;
    }

    public int size() {
        return offers.size();
    }

    public boolean isEmpty() {
        return offers.isEmpty();
    }

    public void clear() {
        offers.clear();
    }

    @Override
    public Iterator<TradeOffer> iterator() {
        return offers.iterator();
    }

    // ================ 批量操作 ================

    /**
     * 获取所有可用的交易
     */
    public List<TradeOffer> getAvailableOffers() {
        List<TradeOffer> available = new ArrayList<>();
        for (TradeOffer offer : offers) {
            if (offer.canUse()) {
                available.add(offer);
            }
        }
        return available;
    }

    /**
     * 重置所有交易
     */
    public void resetAll() {
        for (TradeOffer offer : offers) {
            offer.reset();
        }
    }

    /**
     * 检查是否有可用交易
     */
    public boolean hasAvailableOffers() {
        for (TradeOffer offer : offers) {
            if (offer.canUse()) {
                return true;
            }
        }
        return false;
    }

    // ================ NBT序列化 ================

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        ListTag listTag = new ListTag();
        for (TradeOffer offer : offers) {
            listTag.add(offer.save());
        }
        tag.put("Offers", listTag);

        return tag;
    }

    public static TradeOfferList load(CompoundTag tag) {
        TradeOfferList list = new TradeOfferList();

        if (tag.contains("Offers", Tag.TAG_LIST)) {
            ListTag listTag = tag.getList("Offers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                TradeOffer offer = TradeOffer.load(listTag.getCompound(i));
                list.add(offer);
            }
        }

        return list;
    }

    /**
     * 创建副本（用于客户端同步）
     */
    public TradeOfferList copy() {
        TradeOfferList copy = new TradeOfferList();
        for (TradeOffer offer : offers) {
            copy.add(new TradeOffer(
                offer.getSellingItem(),
                offer.getPriceInDeniers(),
                offer.getMaxUses()
            ));
        }
        return copy;
    }
}
