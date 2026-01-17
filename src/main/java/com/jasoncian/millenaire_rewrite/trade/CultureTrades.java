package com.jasoncian.millenaire_rewrite.trade;

import com.jasoncian.millenaire_rewrite.core.ModItems;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 文化交易定义 - 定义每种文化的交易内容
 *
 * 交易内容根据：
 * - 村民文化
 * - 村民职业
 * - 村庄发展等级
 *
 * @author Based on OldSource trading
 * @version 1.0.0
 */
public class CultureTrades {

    // ================ 交易生成器注册表 ================

    private static final Map<String, TradeGenerator> CULTURE_TRADES = new HashMap<>();

    static {
        // 注册各文化的交易
        registerNormanTrades();
        registerJapaneseTrades();
        registerIndianTrades();
        registerMayanTrades();
        registerByzantineTrades();
        registerInuitTrades();
        registerSeljukTrades();
    }

    // ================ 诺曼交易 ================

    private static void registerNormanTrades() {
        CULTURE_TRADES.put("norman", (profession, villageLevel) -> {
            TradeOfferList offers = new TradeOfferList();

            switch (profession) {
                case MERCHANT -> {
                    // 基础商品
                    offers.add(new TradeOffer(new ItemStack(Items.BREAD, 4), 8));
                    offers.add(new TradeOffer(new ItemStack(Items.APPLE, 4), 6));
                    offers.add(new TradeOffer(new ItemStack(Items.COAL, 8), 16));
                    offers.add(new TradeOffer(new ItemStack(Items.IRON_INGOT, 1), 32));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.CIDER.get(), 1), 24));
                        offers.add(new TradeOffer(new ItemStack(ModItems.BOUDIN_NOIR.get(), 2), 20));
                    }
                    if (villageLevel >= 3) {
                        offers.add(new TradeOffer(new ItemStack(Items.DIAMOND, 1), 640));
                    }
                }
                case BLACKSMITH -> {
                    offers.add(new TradeOffer(new ItemStack(Items.IRON_SWORD, 1), 128));
                    offers.add(new TradeOffer(new ItemStack(Items.IRON_PICKAXE, 1), 192));
                    offers.add(new TradeOffer(new ItemStack(Items.IRON_AXE, 1), 192));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.NORMAN_SWORD.get(), 1), 256));
                        offers.add(new TradeOffer(new ItemStack(Items.IRON_CHESTPLATE, 1), 384));
                    }
                }
                case FARMER -> {
                    offers.add(new TradeOffer(new ItemStack(Items.WHEAT, 16), 12));
                    offers.add(new TradeOffer(new ItemStack(Items.CARROT, 8), 8));
                    offers.add(new TradeOffer(new ItemStack(Items.POTATO, 8), 8));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.CIDER_APPLE.get(), 4), 16));
                    }
                }
                case BAKER -> {
                    offers.add(new TradeOffer(new ItemStack(Items.BREAD, 8), 12));
                    offers.add(new TradeOffer(new ItemStack(Items.CAKE, 1), 64));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(Items.COOKIE, 8), 16));
                    }
                }
                default -> {
                    // 默认基础交易
                    offers.add(new TradeOffer(new ItemStack(Items.BREAD, 2), 4));
                }
            }

            return offers;
        });
    }

    // ================ 日本交易 ================

    private static void registerJapaneseTrades() {
        CULTURE_TRADES.put("japanese", (profession, villageLevel) -> {
            TradeOfferList offers = new TradeOfferList();

            switch (profession) {
                case MERCHANT -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.RICE.get(), 8), 12));
                    offers.add(new TradeOffer(new ItemStack(ModItems.SAKE.get(), 1), 32));
                    offers.add(new TradeOffer(new ItemStack(Items.BAMBOO, 16), 8));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.UDON.get(), 4), 24));
                        offers.add(new TradeOffer(new ItemStack(ModItems.CHERRY_BLOSSOM.get(), 4), 16));
                    }
                }
                case SAMURAI -> {
                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.JAPANESE_SWORD.get(), 1), 320));
                    }
                }
                case FARMER -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.RICE.get(), 16), 16));
                    offers.add(new TradeOffer(new ItemStack(Items.BAMBOO, 32), 12));
                }
                default -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.RICE.get(), 4), 6));
                }
            }

            return offers;
        });
    }

    // ================ 印度交易 ================

    private static void registerIndianTrades() {
        CULTURE_TRADES.put("indian", (profession, villageLevel) -> {
            TradeOfferList offers = new TradeOfferList();

            switch (profession) {
                case MERCHANT -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.TURMERIC.get(), 4), 16));
                    offers.add(new TradeOffer(new ItemStack(ModItems.RICE.get(), 8), 12));
                    offers.add(new TradeOffer(new ItemStack(ModItems.COTTON.get(), 8), 20));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.VEG_CURRY.get(), 2), 28));
                        offers.add(new TradeOffer(new ItemStack(ModItems.MURGH_CURRY.get(), 2), 36));
                        offers.add(new TradeOffer(new ItemStack(ModItems.SILK.get(), 1), 128));
                    }
                }
                case BRICK_MAKER -> {
                    offers.add(new TradeOffer(new ItemStack(Items.BRICK, 16), 24));
                    offers.add(new TradeOffer(new ItemStack(Items.TERRACOTTA, 8), 20));
                }
                default -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.RICE.get(), 4), 6));
                }
            }

            return offers;
        });
    }

    // ================ 玛雅交易 ================

    private static void registerMayanTrades() {
        CULTURE_TRADES.put("mayan", (profession, villageLevel) -> {
            TradeOfferList offers = new TradeOfferList();

            switch (profession) {
                case MERCHANT -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.MAIZE.get(), 8), 10));
                    offers.add(new TradeOffer(new ItemStack(Items.COCOA_BEANS, 8), 16));
                    offers.add(new TradeOffer(new ItemStack(ModItems.OBSIDIAN_FLAKE.get(), 4), 24));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.MASA.get(), 4), 20));
                        offers.add(new TradeOffer(new ItemStack(ModItems.CACAUHAA.get(), 2), 32));
                    }
                }
                case HUNTER -> {
                    offers.add(new TradeOffer(new ItemStack(Items.LEATHER, 4), 16));
                    offers.add(new TradeOffer(new ItemStack(Items.FEATHER, 8), 12));
                }
                default -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.MAIZE.get(), 4), 5));
                }
            }

            return offers;
        });
    }

    // ================ 拜占庭交易 ================

    private static void registerByzantineTrades() {
        CULTURE_TRADES.put("byzantine", (profession, villageLevel) -> {
            TradeOfferList offers = new TradeOfferList();

            switch (profession) {
                case MERCHANT -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.OLIVES.get(), 8), 12));
                    offers.add(new TradeOffer(new ItemStack(ModItems.GRAPES.get(), 8), 14));
                    offers.add(new TradeOffer(new ItemStack(ModItems.FETA.get(), 4), 24));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.OLIVE_OIL.get(), 2), 32));
                        offers.add(new TradeOffer(new ItemStack(ModItems.MALVASIA_WINE.get(), 1), 48));
                        offers.add(new TradeOffer(new ItemStack(ModItems.SOUVLAKI.get(), 4), 28));
                    }
                }
                case SILK_FARMER -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.SILK.get(), 2), 96));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.SILK_CLOTHES.get(), 1), 256));
                    }
                }
                default -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.OLIVES.get(), 4), 6));
                }
            }

            return offers;
        });
    }

    // ================ 因纽特交易 ================

    private static void registerInuitTrades() {
        CULTURE_TRADES.put("inuit", (profession, villageLevel) -> {
            TradeOfferList offers = new TradeOfferList();

            switch (profession) {
                case MERCHANT -> {
                    offers.add(new TradeOffer(new ItemStack(Items.COD, 8), 12));
                    offers.add(new TradeOffer(new ItemStack(Items.SALMON, 8), 14));
                    offers.add(new TradeOffer(new ItemStack(ModItems.TANNED_HIDE.get(), 2), 32));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.INUIT_MEATY_STEW.get(), 2), 36));
                        offers.add(new TradeOffer(new ItemStack(ModItems.INUIT_BEAR_STEW.get(), 2), 48));
                    }
                }
                case FISHER -> {
                    offers.add(new TradeOffer(new ItemStack(Items.COD, 16), 16));
                    offers.add(new TradeOffer(new ItemStack(Items.SALMON, 16), 20));
                    offers.add(new TradeOffer(new ItemStack(ModItems.SEAFOOD_COOKED.get(), 4), 24));
                }
                case HUNTER -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.BEAR_MEAT_COOKED.get(), 4), 32));
                    offers.add(new TradeOffer(new ItemStack(ModItems.TANNED_HIDE.get(), 4), 48));
                }
                default -> {
                    offers.add(new TradeOffer(new ItemStack(Items.COD, 4), 6));
                }
            }

            return offers;
        });
    }

    // ================ 塞尔柱交易 ================

    private static void registerSeljukTrades() {
        CULTURE_TRADES.put("seljuk", (profession, villageLevel) -> {
            TradeOfferList offers = new TradeOfferList();

            switch (profession) {
                case MERCHANT -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.YOGURT.get(), 4), 12));
                    offers.add(new TradeOffer(new ItemStack(ModItems.PISTACHIOS.get(), 8), 16));
                    offers.add(new TradeOffer(new ItemStack(Items.WHITE_WOOL, 8), 20));

                    if (villageLevel >= 2) {
                        offers.add(new TradeOffer(new ItemStack(ModItems.PIDE.get(), 4), 24));
                        offers.add(new TradeOffer(new ItemStack(ModItems.LOKUM.get(), 4), 28));
                        offers.add(new TradeOffer(new ItemStack(ModItems.HELVA.get(), 4), 32));
                        offers.add(new TradeOffer(new ItemStack(ModItems.AYRAN.get(), 4), 16));
                    }
                }
                case SHEPHERD -> {
                    offers.add(new TradeOffer(new ItemStack(Items.WHITE_WOOL, 16), 24));
                    offers.add(new TradeOffer(new ItemStack(Items.MUTTON, 8), 20));
                }
                default -> {
                    offers.add(new TradeOffer(new ItemStack(ModItems.YOGURT.get(), 2), 6));
                }
            }

            return offers;
        });
    }

    // ================ 交易获取 ================

    /**
     * 获取村民的交易列表
     *
     * @param culture 文化
     * @param profession 职业
     * @param villageLevel 村庄等级 (1-5)
     * @return 交易列表
     */
    public static TradeOfferList getTradesFor(Culture culture, VillagerProfession profession, int villageLevel) {
        String cultureId = culture.getId().toLowerCase();
        TradeGenerator generator = CULTURE_TRADES.get(cultureId);

        if (generator != null) {
            return generator.generate(profession, villageLevel);
        }

        // 默认返回基础交易
        TradeOfferList defaultOffers = new TradeOfferList();
        defaultOffers.add(new TradeOffer(new ItemStack(Items.BREAD, 2), 4));
        return defaultOffers;
    }

    // ================ 交易生成器接口 ================

    @FunctionalInterface
    public interface TradeGenerator {
        TradeOfferList generate(VillagerProfession profession, int villageLevel);
    }
}
