package com.jasoncian.millenaire_rewrite.core;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.menu.FirePitMenu;
import com.jasoncian.millenaire_rewrite.menu.ImportTableMenu;
import com.jasoncian.millenaire_rewrite.menu.LockedChestMenu;
import com.jasoncian.millenaire_rewrite.menu.TownHallMenu;
import com.jasoncian.millenaire_rewrite.menu.TradingMenu;
import com.jasoncian.millenaire_rewrite.menu.VillagerInteractionMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组菜单类型注册器
 *
 * 负责注册所有Millenaire mod的菜单类型
 * 用于GUI容器系统
 *
 * 菜单类型：
 * - 火坑 (FirePit) - 多槽位烹饪GUI
 * - 锁定箱子 (LockedChest) - 村庄存储GUI（待实现）
 * - 交易 (Trading) - 村民交易GUI（待实现）
 */
public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(BuiltInRegistries.MENU, MillenaireRewrite.MOD_ID);

    // ================ 功能方块菜单 ================

    /** 火坑菜单 */
    public static final DeferredHolder<MenuType<?>, MenuType<FirePitMenu>> FIRE_PIT =
        MENUS.register("fire_pit", () -> IMenuTypeExtension.create(FirePitMenu::new));

    /** 锁定箱子菜单 */
    public static final DeferredHolder<MenuType<?>, MenuType<LockedChestMenu>> LOCKED_CHEST =
        MENUS.register("locked_chest", () -> IMenuTypeExtension.create(LockedChestMenu::new));

    /** 导入桌菜单 */
    public static final DeferredHolder<MenuType<?>, MenuType<ImportTableMenu>> IMPORT_TABLE =
        MENUS.register("import_table", () -> IMenuTypeExtension.create(ImportTableMenu::new));

    // ================ 村庄管理菜单 ================

    /** 市政厅菜单 */
    public static final DeferredHolder<MenuType<?>, MenuType<TownHallMenu>> TOWN_HALL =
        MENUS.register("town_hall", () -> IMenuTypeExtension.create(TownHallMenu::new));

    // ================ 村民交互菜单 ================

    /** 村民交互菜单 */
    public static final DeferredHolder<MenuType<?>, MenuType<VillagerInteractionMenu>> VILLAGER_INTERACTION =
        MENUS.register("villager_interaction", () -> IMenuTypeExtension.create(VillagerInteractionMenu::new));

    /** 交易菜单 */
    public static final DeferredHolder<MenuType<?>, MenuType<TradingMenu>> TRADING =
        MENUS.register("trading", () -> IMenuTypeExtension.create(TradingMenu::new));

    /**
     * 注册所有菜单类型到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
