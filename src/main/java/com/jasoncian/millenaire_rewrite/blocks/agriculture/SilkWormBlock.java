package com.jasoncian.millenaire_rewrite.blocks.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * 蚕室方块 - 基于OldSource BlockSilkWorm实现
 *
 * 用于丝绸生产的蚕养殖系统。
 * 需要低光照环境才能进展。
 *
 * 进展阶段：
 * - EMPTY: 空蚕室
 * - PROGRESS_1: 养殖进行中（阶段1）
 * - PROGRESS_2: 养殖进行中（阶段2）
 * - FULL: 完成，可收获丝绸
 *
 * @author Based on OldSource BlockSilkWorm
 * @version 1.0.0
 */
public class SilkWormBlock extends Block {

    public static final EnumProperty<SilkWormProgress> PROGRESS = EnumProperty.create("progress", SilkWormProgress.class);

    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 10.0, 14.0);

    /** 光照阈值 - 需要低于此值才能进展 */
    private static final int LIGHT_THRESHOLD = 7;

    private final Supplier<Item> silkSupplier;

    /**
     * 蚕养殖进展枚举
     */
    public enum SilkWormProgress implements StringRepresentable {
        EMPTY("empty", 0),
        PROGRESS_1("progress_1", 1),
        PROGRESS_2("progress_2", 2),
        FULL("full", 3);

        private final String name;
        private final int index;

        SilkWormProgress(String name, int index) {
            this.name = name;
            this.index = index;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public SilkWormProgress next() {
            return switch (this) {
                case EMPTY -> PROGRESS_1;
                case PROGRESS_1 -> PROGRESS_2;
                case PROGRESS_2 -> FULL;
                case FULL -> FULL;
            };
        }

        public boolean isHarvestable() {
            return this == FULL;
        }
    }

    public SilkWormBlock(BlockBehaviour.Properties properties, Supplier<Item> silkSupplier) {
        super(properties);
        this.silkSupplier = silkSupplier;
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(PROGRESS, SilkWormProgress.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROGRESS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(PROGRESS) != SilkWormProgress.FULL;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        SilkWormProgress progress = state.getValue(PROGRESS);

        // 已完成不处理
        if (progress == SilkWormProgress.FULL) {
            return;
        }

        // 检查光照 - 需要低光照环境
        int light = level.getMaxLocalRawBrightness(pos);
        if (light >= LIGHT_THRESHOLD) {
            // 光照太亮，蚕不活动
            return;
        }

        // 50%几率进展
        if (random.nextInt(2) == 0) {
            level.setBlock(pos, state.setValue(PROGRESS, progress.next()), 2);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        SilkWormProgress progress = state.getValue(PROGRESS);

        if (progress.isHarvestable()) {
            if (!level.isClientSide()) {
                // 掉落丝绸
                Block.popResource(level, pos, new ItemStack(silkSupplier.get()));
                // 重置状态
                level.setBlock(pos, state.setValue(PROGRESS, SilkWormProgress.EMPTY), 2);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    /**
     * 获取当前进展
     */
    public SilkWormProgress getProgress(BlockState state) {
        return state.getValue(PROGRESS);
    }

    /**
     * 检查是否可收获
     */
    public boolean canHarvest(BlockState state) {
        return getProgress(state).isHarvestable();
    }

    /**
     * 获取丝绸物品
     */
    public Item getSilk() {
        return silkSupplier.get();
    }
}
