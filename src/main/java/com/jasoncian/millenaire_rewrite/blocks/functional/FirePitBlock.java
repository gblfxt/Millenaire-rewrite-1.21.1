package com.jasoncian.millenaire_rewrite.blocks.functional;

import com.jasoncian.millenaire_rewrite.blockentity.FirePitBlockEntity;
import com.jasoncian.millenaire_rewrite.core.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * 火坑方块 - 基于OldSource BlockFirePit实现
 *
 * 火坑是多输入烹饪方块，可以同时烹饪3个物品。
 * 使用燃料来加热，类似于熔炉但支持多槽位烹饪。
 *
 * 功能特性：
 * - 3个输入槽位，3个输出槽位
 * - 1个燃料槽位
 * - 燃烧时发光和产生粒子
 * - 玩家放置方向控制
 * - 自动化兼容（漏斗等）
 *
 * @author Based on OldSource BlockFirePit
 * @version 1.0.0
 */
public class FirePitBlock extends BaseEntityBlock {

    /** Block codec for serialization */
    public static final MapCodec<FirePitBlock> CODEC = simpleCodec(FirePitBlock::new);

    /** 朝向属性 */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    /** 燃烧状态属性 */
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    /** 碰撞形状 - 无碰撞 */
    protected static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public FirePitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
            .setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(LIT, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // 火坑没有碰撞，可以走过
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FirePitBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.FIRE_PIT.get(), FirePitBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FirePitBlockEntity firePitBlockEntity) {
                ((ServerPlayer) player).openMenu(firePitBlockEntity, pos);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FirePitBlockEntity firePitBlockEntity) {
                ((ServerPlayer) player).openMenu(firePitBlockEntity, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FirePitBlockEntity firePitBlockEntity) {
                // 掉落所有物品
                Containers.dropContents(level, pos, firePitBlockEntity);
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.4;
            double z = pos.getZ() + 0.5;

            // 随机播放火焰声音
            if (random.nextDouble() < 0.1) {
                level.playLocalSound(x, y, z, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
                    1.0F, 1.0F, false);
            }

            // 产生烟雾和火焰粒子
            double offsetX = (random.nextDouble() - 0.5) * 0.5;
            double offsetZ = (random.nextDouble() - 0.5) * 0.5;
            level.addParticle(ParticleTypes.SMOKE, x + offsetX, y + 0.2, z + offsetZ, 0.0, 0.05, 0.0);
            level.addParticle(ParticleTypes.FLAME, x + offsetX * 0.3, y, z + offsetZ * 0.3, 0.0, 0.0, 0.0);
        }
    }

    /**
     * 设置火坑燃烧状态
     */
    public static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        level.setBlock(pos, state.setValue(LIT, lit), 3);
    }
}
