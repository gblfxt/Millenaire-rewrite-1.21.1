package com.jasoncian.millenaire_rewrite.entity.ai.goals;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalInformation;
import com.jasoncian.millenaire_rewrite.entity.ai.MillGoal;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * 采矿目标 - 矿工开采矿石和石头
 *
 * 支持：
 * - 开采矿石（煤、铁、金等）
 * - 开采石头
 * - 收集掉落物到库存
 *
 * @author Based on OldSource GoalMine
 * @version 1.0.0
 */
public class GoalMine extends MillGoal {

    /** 搜索范围 */
    private int searchRadius = 12;

    /** 目标方块位置 */
    @Nullable
    private BlockPos targetBlock;

    /** 挖掘动画时长（ticks） */
    private int miningDuration = 40;

    /** 当前挖掘计时 */
    private int miningTicks = 0;

    /** 可开采的方块集合 */
    private static final Set<Block> MINEABLE_BLOCKS = new HashSet<>();

    static {
        // 矿石
        MINEABLE_BLOCKS.add(Blocks.COAL_ORE);
        MINEABLE_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
        MINEABLE_BLOCKS.add(Blocks.IRON_ORE);
        MINEABLE_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
        MINEABLE_BLOCKS.add(Blocks.GOLD_ORE);
        MINEABLE_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
        MINEABLE_BLOCKS.add(Blocks.COPPER_ORE);
        MINEABLE_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
        MINEABLE_BLOCKS.add(Blocks.LAPIS_ORE);
        MINEABLE_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
        MINEABLE_BLOCKS.add(Blocks.REDSTONE_ORE);
        MINEABLE_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);

        // 石头类
        MINEABLE_BLOCKS.add(Blocks.STONE);
        MINEABLE_BLOCKS.add(Blocks.COBBLESTONE);
        MINEABLE_BLOCKS.add(Blocks.DEEPSLATE);
        MINEABLE_BLOCKS.add(Blocks.COBBLED_DEEPSLATE);
        MINEABLE_BLOCKS.add(Blocks.ANDESITE);
        MINEABLE_BLOCKS.add(Blocks.DIORITE);
        MINEABLE_BLOCKS.add(Blocks.GRANITE);
    }

    public GoalMine() {
        super("mine");
        this.leisure = false;
        this.tags.add("work");
        this.tags.add("profession");
        this.tags.add("miner");
        this.range = 2.5;
        this.canBeDoneInDayTime = true;
        this.canBeDoneAtNight = false;
        this.minimumHour = 0;
        this.maximumHour = 12500;
    }

    @Override
    public int priority(MillVillager villager) {
        if (villager.getProfession() != VillagerProfession.MINER) {
            return 0;
        }

        if (villager.level().isDay()) {
            return 500;
        }

        return 0;
    }

    @Nullable
    @Override
    public GoalInformation getDestination(MillVillager villager) {
        // 优先寻找矿石
        targetBlock = findOre(villager);
        if (targetBlock != null) {
            return GoalInformation.ofDestination(targetBlock);
        }

        // 其次寻找石头
        targetBlock = findStone(villager);
        if (targetBlock != null) {
            return GoalInformation.ofDestination(targetBlock);
        }

        return null;
    }

    /**
     * 查找矿石
     */
    @Nullable
    private BlockPos findOre(MillVillager villager) {
        BlockPos center = villager.blockPosition();
        BlockPos closestOre = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = villager.level().getBlockState(checkPos);

                    // 检查是否是矿石（优先级高）
                    if (isOre(state.getBlock())) {
                        // 检查是否可以到达（相邻有空气）
                        if (hasAccessibleSide(villager, checkPos)) {
                            double dist = villager.distanceToSqr(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                            if (dist < closestDist) {
                                closestDist = dist;
                                closestOre = checkPos;
                            }
                        }
                    }
                }
            }
        }

        return closestOre;
    }

    /**
     * 查找石头
     */
    @Nullable
    private BlockPos findStone(MillVillager villager) {
        BlockPos center = villager.blockPosition();
        BlockPos closestStone = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = villager.level().getBlockState(checkPos);

                    if (isStone(state.getBlock())) {
                        if (hasAccessibleSide(villager, checkPos)) {
                            double dist = villager.distanceToSqr(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                            if (dist < closestDist) {
                                closestDist = dist;
                                closestStone = checkPos;
                            }
                        }
                    }
                }
            }
        }

        return closestStone;
    }

    /**
     * 检查是否是矿石
     */
    private boolean isOre(Block block) {
        return block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE;
    }

    /**
     * 检查是否是石头
     */
    private boolean isStone(Block block) {
        return block == Blocks.STONE || block == Blocks.COBBLESTONE ||
               block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE ||
               block == Blocks.ANDESITE || block == Blocks.DIORITE || block == Blocks.GRANITE;
    }

    /**
     * 检查方块是否有可访问的面（相邻有空气或液体）
     */
    private boolean hasAccessibleSide(MillVillager villager, BlockPos pos) {
        return villager.level().getBlockState(pos.above()).isAir() ||
               villager.level().getBlockState(pos.below()).isAir() ||
               villager.level().getBlockState(pos.north()).isAir() ||
               villager.level().getBlockState(pos.south()).isAir() ||
               villager.level().getBlockState(pos.east()).isAir() ||
               villager.level().getBlockState(pos.west()).isAir();
    }

    @Override
    public void onAccept(MillVillager villager) {
        miningTicks = 0;
        villager.setAnimationState(VillagerAnimationState.WALKING);
    }

    @Override
    public boolean performAction(MillVillager villager) {
        if (targetBlock == null) {
            return true;
        }

        // 设置采矿动画
        villager.setAnimationState(VillagerAnimationState.MINING);

        miningTicks++;

        // 采矿粒子效果
        if (miningTicks % 5 == 0 && villager.level() instanceof ServerLevel serverLevel) {
            BlockState state = serverLevel.getBlockState(targetBlock);
            serverLevel.levelEvent(2001, targetBlock, Block.getId(state));
        }

        if (miningTicks < miningDuration) {
            return false;
        }

        // 完成挖掘
        if (villager.level() instanceof ServerLevel serverLevel) {
            performMining(villager, serverLevel);
        }

        return true;
    }

    /**
     * 执行挖掘
     */
    private void performMining(MillVillager villager, ServerLevel level) {
        if (targetBlock == null) return;

        BlockState state = level.getBlockState(targetBlock);
        if (state.isAir()) return;

        // 获取掉落物
        var drops = Block.getDrops(state, level, targetBlock, null);
        for (ItemStack drop : drops) {
            villager.addToInventory(drop, drop.getCount());
        }

        // 破坏方块
        level.destroyBlock(targetBlock, false);
    }

    @Override
    public void onComplete(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        targetBlock = null;
    }

    @Override
    public void onInterrupt(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        targetBlock = null;
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        if (!villager.level().isDay()) {
            return false;
        }
        return villager.getProfession() == VillagerProfession.MINER;
    }

    @Override
    protected boolean isPossibleSpecific(MillVillager villager) {
        if (!villager.level().isDay()) {
            return false;
        }

        if (villager.getProfession() != VillagerProfession.MINER) {
            return false;
        }

        // 检查是否有可开采的方块
        return findOre(villager) != null || findStone(villager) != null;
    }

    @Nullable
    @Override
    public ItemStack[] getHeldItemsDestination(MillVillager villager) {
        return new ItemStack[]{ new ItemStack(Items.IRON_PICKAXE) };
    }

    /**
     * 设置搜索范围
     */
    public GoalMine withSearchRadius(int radius) {
        this.searchRadius = radius;
        return this;
    }
}
