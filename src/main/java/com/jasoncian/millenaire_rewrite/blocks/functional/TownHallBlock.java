package com.jasoncian.millenaire_rewrite.blocks.functional;

import com.jasoncian.millenaire_rewrite.blockentity.TownHallBlockEntity;
import com.jasoncian.millenaire_rewrite.core.ModBlockEntities;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.menu.TownHallMenu;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 市政厅方块 - 村庄核心建筑
 *
 * 功能：
 * - 放置时创建新村庄
 * - 右键打开村庄管理界面
 * - 破坏时删除村庄
 *
 * @author Based on OldSource TownHall
 * @version 1.0.0
 */
public class TownHallBlock extends BaseEntityBlock {

    public static final MapCodec<TownHallBlock> CODEC = simpleCodec(TownHallBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** 关联的文化（不同文化有不同的市政厅） */
    private final Culture culture;

    public TownHallBlock(Properties properties) {
        this(properties, Culture.NORMAN);
    }

    public TownHallBlock(Properties properties, Culture culture) {
        super(properties);
        this.culture = culture;
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ================ 方块实体 ================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TownHallBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(blockEntityType, ModBlockEntities.TOWN_HALL.get(),
            TownHallBlockEntity::serverTick);
    }

    // ================ 放置与破坏 ================

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // 创建新村庄
            VillageManager manager = VillageManager.get(serverLevel);
            Village village = manager.createVillage(pos, culture);

            if (village != null) {
                // 将市政厅信息同步到BlockEntity
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof TownHallBlockEntity townHallBE) {
                    townHallBE.setVillageId(village.getVillageId());
                }

                // 通知玩家
                if (placer instanceof Player player) {
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "message.millenaire_rewrite.village_created",
                            village.getName()),
                        false);
                }
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // 删除村庄
            VillageManager manager = VillageManager.get(serverLevel);
            Village village = manager.getVillageAt(pos);

            if (village != null) {
                manager.removeVillage(village);

                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                        "message.millenaire_rewrite.village_destroyed",
                        village.getName()),
                    false);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    // ================ 交互 ================

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TownHallBlockEntity townHallBE) {
                // 打开村庄管理GUI
                player.openMenu(townHallBE, buf -> TownHallMenu.writeToBuffer(buf, townHallBE));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // ================ Getter ================

    public Culture getCulture() {
        return culture;
    }
}
