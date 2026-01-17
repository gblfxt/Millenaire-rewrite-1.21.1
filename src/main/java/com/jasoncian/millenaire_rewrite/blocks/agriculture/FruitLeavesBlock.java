package com.jasoncian.millenaire_rewrite.blocks.agriculture;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Supplier;

/**
 * 水果树叶方块 - 基于OldSource BlockFruitLeaves实现
 *
 * 可以生产水果的树叶方块。
 * 使用基于时间的生长系统（昼夜循环）。
 *
 * 生长阶段：
 * - age 0: 无果实
 * - age 1: 小果实
 * - age 2: 中等果实
 * - age 3: 成熟可收获
 *
 * @author Based on OldSource BlockFruitLeaves
 * @version 1.0.0
 */
public class FruitLeavesBlock extends LeavesBlock {

    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);

    private final Supplier<Item> fruitSupplier;
    private final Supplier<Item> saplingSupplier;

    /**
     * 创建水果树叶方块
     *
     * @param properties 方块属性
     * @param fruitSupplier 果实物品供应商
     * @param saplingSupplier 树苗物品供应商
     */
    public FruitLeavesBlock(BlockBehaviour.Properties properties, Supplier<Item> fruitSupplier,
                            Supplier<Item> saplingSupplier) {
        super(properties);
        this.fruitSupplier = fruitSupplier;
        this.saplingSupplier = saplingSupplier;
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(DISTANCE, 7)
            .setValue(PERSISTENT, false)
            .setValue(WATERLOGGED, false)
            .setValue(AGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true; // 总是tick以检查时间和衰减
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 首先处理树叶衰减
        super.randomTick(state, level, pos, random);

        // 如果是持久树叶或已被移除，不处理果实
        if (state.getValue(PERSISTENT) || !level.getBlockState(pos).is(this)) {
            return;
        }

        // 基于时间的果实生长
        updateFruitAge(state, level, pos);
    }

    /**
     * 基于世界时间更新果实年龄
     * 模拟OldSource的昼夜循环果实生长
     */
    private void updateFruitAge(BlockState state, ServerLevel level, BlockPos pos) {
        long dayTime = level.getDayTime() % 24000;
        int currentAge = state.getValue(AGE);
        int targetAge;

        // 基于一天中的时间确定目标年龄
        if (dayTime < 3000) {
            targetAge = 0; // 凌晨：无果实
        } else if (dayTime < 5000) {
            targetAge = 1; // 早晨：小果实
        } else if (dayTime < 6000) {
            targetAge = 2; // 上午：中等果实
        } else if (dayTime < 10000) {
            targetAge = 3; // 中午到下午：成熟
        } else {
            targetAge = 0; // 傍晚到夜晚：重置
        }

        // 更新年龄
        if (currentAge != targetAge) {
            level.setBlock(pos, state.setValue(AGE, targetAge), 2);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        if (age >= MAX_AGE) {
            // 收获果实
            if (!level.isClientSide()) {
                // 掉落果实
                Block.popResource(level, pos, new ItemStack(fruitSupplier.get()));
                // 重置年龄
                level.setBlock(pos, state.setValue(AGE, 0), 2);
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,
                    SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                               BlockPos pos, Player player, InteractionHand hand,
                                               BlockHitResult hitResult) {
        // 剪刀采集整个方块
        if (stack.is(Items.SHEARS)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    /**
     * 获取当前果实年龄
     */
    public int getAge(BlockState state) {
        return state.getValue(AGE);
    }

    /**
     * 检查果实是否成熟
     */
    public boolean isRipe(BlockState state) {
        return getAge(state) >= MAX_AGE;
    }

    /**
     * 获取果实物品
     */
    public Item getFruit() {
        return fruitSupplier.get();
    }

    /**
     * 获取树苗物品
     */
    public Item getSapling() {
        return saplingSupplier.get();
    }
}
