package com.jasoncian.millenaire_rewrite.blocks.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Millenaire作物方块 - 基于OldSource BlockMillCrops实现
 *
 * 支持8个生长阶段的自定义作物。
 * 提供灌溉需求和生长速度选项。
 *
 * 功能特性：
 * - 8阶段生长系统
 * - 可配置灌溉需求
 * - 可配置生长速度
 * - 与村庄农业系统集成
 *
 * @author Based on OldSource BlockMillCrops
 * @version 1.0.0
 */
public class MillCropBlock extends CropBlock {

    public static final int MAX_AGE = 7;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    /** 是否需要灌溉才能生长 */
    private final boolean requireIrrigation;

    /** 是否减缓生长速度 */
    private final boolean slowGrowth;

    /** 种子物品供应商 */
    private final Supplier<Item> seedSupplier;

    /** 收获物品供应商（可选，默认与种子相同） */
    private final Supplier<Item> harvestSupplier;

    // 生长阶段的形状
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
        Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0),
        Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0)
    };

    /**
     * 创建Millenaire作物方块
     *
     * @param properties 方块属性
     * @param seedSupplier 种子物品供应商
     * @param requireIrrigation 是否需要灌溉
     * @param slowGrowth 是否减缓生长
     */
    public MillCropBlock(BlockBehaviour.Properties properties, Supplier<Item> seedSupplier,
                         boolean requireIrrigation, boolean slowGrowth) {
        this(properties, seedSupplier, seedSupplier, requireIrrigation, slowGrowth);
    }

    /**
     * 创建Millenaire作物方块（带独立收获物品）
     *
     * @param properties 方块属性
     * @param seedSupplier 种子物品供应商
     * @param harvestSupplier 收获物品供应商
     * @param requireIrrigation 是否需要灌溉
     * @param slowGrowth 是否减缓生长
     */
    public MillCropBlock(BlockBehaviour.Properties properties, Supplier<Item> seedSupplier,
                         Supplier<Item> harvestSupplier, boolean requireIrrigation, boolean slowGrowth) {
        super(properties);
        this.seedSupplier = seedSupplier;
        this.harvestSupplier = harvestSupplier;
        this.requireIrrigation = requireIrrigation;
        this.slowGrowth = slowGrowth;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[this.getAge(state)];
    }

    @Override
    protected IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seedSupplier.get();
    }

    /**
     * 获取收获物品
     */
    public Item getHarvestItem() {
        return harvestSupplier.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !this.isMaxAge(state);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;

        // 检查光照
        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = this.getAge(state);
            if (age < this.getMaxAge()) {
                // 计算生长率
                float growthChance = getGrowthChance(level, pos, state);

                // 灌溉检查
                if (requireIrrigation && !hasIrrigation(level, pos)) {
                    // 需要灌溉但没有灌溉，不生长
                    return;
                }

                // 慢速生长减半生长率
                if (slowGrowth) {
                    growthChance *= 0.5f;
                }

                // 随机生长
                if (random.nextInt((int)(25.0F / growthChance) + 1) == 0) {
                    level.setBlock(pos, this.getStateForAge(age + 1), 2);
                }
            }
        }
    }

    /**
     * 计算生长概率
     */
    protected float getGrowthChance(Level level, BlockPos pos, BlockState state) {
        float chance = 1.0F;
        BlockPos below = pos.below();
        BlockState soil = level.getBlockState(below);

        // 农田提供基础加成
        if (soil.is(Blocks.FARMLAND)) {
            chance += 1.0F;
        }

        // 检查周围方块
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos checkPos = pos.offset(x, -1, z);
                BlockState checkState = level.getBlockState(checkPos);
                if (checkState.is(Blocks.FARMLAND)) {
                    chance += 0.25F;
                }
            }
        }

        return chance;
    }

    /**
     * 检查是否有灌溉
     * TODO: 与村庄灌溉系统集成
     */
    protected boolean hasIrrigation(Level level, BlockPos pos) {
        // 简单实现：检查附近是否有水
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                for (int y = 0; y <= 1; y++) {
                    BlockPos checkPos = pos.offset(x, y - 1, z);
                    if (level.getBlockState(checkPos).is(Blocks.WATER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查是否需要灌溉
     */
    public boolean requiresIrrigation() {
        return requireIrrigation;
    }

    /**
     * 检查是否慢速生长
     */
    public boolean hasSlowGrowth() {
        return slowGrowth;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }
}
