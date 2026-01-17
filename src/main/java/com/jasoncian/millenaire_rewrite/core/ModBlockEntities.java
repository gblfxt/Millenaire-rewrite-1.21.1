package com.jasoncian.millenaire_rewrite.core;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.blockentity.FirePitBlockEntity;
import com.jasoncian.millenaire_rewrite.blockentity.ImportTableBlockEntity;
import com.jasoncian.millenaire_rewrite.blockentity.LockedChestBlockEntity;
import com.jasoncian.millenaire_rewrite.blockentity.TownHallBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * 模组BlockEntity注册器
 *
 * 负责注册所有Millenaire mod的BlockEntity类型
 * BlockEntity是1.20.1中TileEntity的现代化替代
 *
 * 功能方块实体：
 * - 火坑 (FirePit) - 多槽位烹饪
 * - 锁定箱子 (LockedChest) - 村庄存储（待实现）
 * - 导入桌 (ImportTable) - 建筑导入导出（待实现）
 */
public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MillenaireRewrite.MOD_ID);

    // ================ 功能方块实体 ================

    /** 火坑方块实体 - 多槽位烹饪 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FirePitBlockEntity>> FIRE_PIT =
        BLOCK_ENTITIES.register("fire_pit", () -> BlockEntityType.Builder
            .of(FirePitBlockEntity::new, ModBlocks.FIRE_PIT.get())
            .build(null));

    /** 锁定箱子方块实体 - 村庄存储 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LockedChestBlockEntity>> LOCKED_CHEST =
        BLOCK_ENTITIES.register("locked_chest", () -> BlockEntityType.Builder
            .of(LockedChestBlockEntity::new, ModBlocks.LOCKED_CHEST.get())
            .build(null));

    /** 导入桌方块实体 - 建筑模板导入导出 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImportTableBlockEntity>> IMPORT_TABLE =
        BLOCK_ENTITIES.register("import_table", () -> BlockEntityType.Builder
            .of(ImportTableBlockEntity::new, ModBlocks.IMPORT_TABLE.get())
            .build(null));

    // ================ 村庄核心方块实体 ================

    /** 市政厅方块实体 - 村庄管理中心 */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TownHallBlockEntity>> TOWN_HALL =
        BLOCK_ENTITIES.register("town_hall", () -> BlockEntityType.Builder
            .of(TownHallBlockEntity::new,
                ModBlocks.TOWN_HALL_NORMAN.get(),
                ModBlocks.TOWN_HALL_JAPANESE.get(),
                ModBlocks.TOWN_HALL_INDIAN.get(),
                ModBlocks.TOWN_HALL_MAYAN.get(),
                ModBlocks.TOWN_HALL_BYZANTINE.get())
            .build(null));

    /**
     * 注册所有BlockEntity类型到模组事件总线
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
