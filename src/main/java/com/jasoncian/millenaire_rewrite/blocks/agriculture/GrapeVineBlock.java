package com.jasoncian.millenaire_rewrite.blocks.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * 葡萄藤方块 - 基于OldSource BlockGrapeVine实现
 *
 * 双层高度作物，支持8个生长阶段。
 * 种植时自动生成上半部分。
 *
 * @author Based on OldSource BlockGrapeVine
 * @version 1.0.0
 */
public class GrapeVineBlock extends Block {

    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    private final Supplier<Item> seedSupplier;
    private final Supplier<Item> harvestSupplier;

    public GrapeVineBlock(BlockBehaviour.Properties properties, Supplier<Item> seedSupplier,
                          Supplier<Item> harvestSupplier) {
        super(properties);
        this.seedSupplier = seedSupplier;
        this.harvestSupplier = harvestSupplier;
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(AGE, 0)
            .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public int getAge(BlockState state) {
        return state.getValue(AGE);
    }

    public boolean isMaxAge(BlockState state) {
        return getAge(state) >= MAX_AGE;
    }

    public BlockState getStateForAge(int age) {
        return this.defaultBlockState().setValue(AGE, age);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && !isMaxAge(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 只在下半部分处理生长
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) return;
        if (!level.isAreaLoaded(pos, 1)) return;

        // 检查光照
        if (level.getRawBrightness(pos.above(), 0) >= 9) {
            int age = this.getAge(state);
            if (age < MAX_AGE) {
                float growthChance = getGrowthChance(level, pos);

                if (random.nextInt((int)(25.0F / growthChance) + 1) == 0) {
                    // 更新下半部分
                    level.setBlock(pos, state.setValue(AGE, age + 1), 2);
                    // 更新上半部分
                    BlockPos upperPos = pos.above();
                    BlockState upperState = level.getBlockState(upperPos);
                    if (upperState.is(this) && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                        level.setBlock(upperPos, upperState.setValue(AGE, age + 1), 2);
                    }
                }
            }
        }
    }

    protected float getGrowthChance(Level level, BlockPos pos) {
        float chance = 1.0F;
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);

        if (soil.is(Blocks.FARMLAND)) {
            chance += 1.0F;
        }

        return chance;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        } else {
            BlockState soil = level.getBlockState(pos.below());
            return soil.is(Blocks.FARMLAND) || soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // 放置时创建上半部分
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos upperPos = pos.above();
            if (level.getBlockState(upperPos).isAir()) {
                level.setBlock(upperPos, this.defaultBlockState()
                    .setValue(HALF, DoubleBlockHalf.UPPER)
                    .setValue(AGE, state.getValue(AGE)), 3);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this)) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, otherPos, Block.getId(otherState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this)) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public Item getSeed() {
        return seedSupplier.get();
    }

    public Item getHarvestItem() {
        return harvestSupplier.get();
    }
}
