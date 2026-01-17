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
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 砍伐目标 - 伐木工砍伐树木
 *
 * 支持：
 * - 砍伐原木
 * - 收集掉落的原木到库存
 * - 可选择性清理树叶
 *
 * @author Based on OldSource GoalChop
 * @version 1.0.0
 */
public class GoalChop extends MillGoal {

    /** 搜索范围 */
    private int searchRadius = 20;

    /** 目标原木位置 */
    @Nullable
    private BlockPos targetBlock;

    /** 当前树的所有原木位置（从下到上） */
    private final List<BlockPos> treeBlocks = new ArrayList<>();

    /** 砍伐动画时长（ticks） */
    private int choppingDuration = 35;

    /** 当前砍伐计时 */
    private int choppingTicks = 0;

    /** 是否清理树叶 */
    private boolean clearLeaves = false;

    public GoalChop() {
        super("chop");
        this.leisure = false;
        this.tags.add("work");
        this.tags.add("profession");
        this.tags.add("lumberjack");
        this.range = 2.5;
        this.canBeDoneInDayTime = true;
        this.canBeDoneAtNight = false;
        this.minimumHour = 0;
        this.maximumHour = 12500;
    }

    @Override
    public int priority(MillVillager villager) {
        if (villager.getProfession() != VillagerProfession.LUMBERJACK) {
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
        // 寻找树木底部
        targetBlock = findTreeBase(villager);
        if (targetBlock != null) {
            // 扫描整棵树
            scanTree(villager, targetBlock);
            return GoalInformation.ofDestination(targetBlock);
        }

        return null;
    }

    /**
     * 查找树木底部（最低的原木）
     */
    @Nullable
    private BlockPos findTreeBase(MillVillager villager) {
        BlockPos center = villager.blockPosition();
        BlockPos closestTree = null;
        double closestDist = Double.MAX_VALUE;

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                // 从地面向上搜索
                for (int y = -2; y <= 5; y++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = villager.level().getBlockState(checkPos);

                    if (isLog(state)) {
                        // 检查下方是否是泥土/草方块（确认是自然树）
                        BlockState below = villager.level().getBlockState(checkPos.below());
                        if (isTreeBase(below) || isLog(below)) {
                            // 找到树底
                            BlockPos treeBase = findLowestLog(villager, checkPos);
                            double dist = villager.distanceToSqr(treeBase.getX(), treeBase.getY(), treeBase.getZ());
                            if (dist < closestDist) {
                                closestDist = dist;
                                closestTree = treeBase;
                            }
                        }
                        break; // 只检查这一列的最低原木
                    }
                }
            }
        }

        return closestTree;
    }

    /**
     * 找到同一列的最低原木
     */
    private BlockPos findLowestLog(MillVillager villager, BlockPos pos) {
        BlockPos current = pos;
        while (isLog(villager.level().getBlockState(current.below()))) {
            current = current.below();
        }
        return current;
    }

    /**
     * 扫描整棵树的原木
     */
    private void scanTree(MillVillager villager, BlockPos base) {
        treeBlocks.clear();

        // 向上扫描主干
        BlockPos current = base;
        while (isLog(villager.level().getBlockState(current))) {
            treeBlocks.add(current);

            // 检查相邻的原木（分支）
            for (BlockPos adjacent : getAdjacentPositions(current)) {
                if (isLog(villager.level().getBlockState(adjacent)) && !treeBlocks.contains(adjacent)) {
                    scanBranch(villager, adjacent);
                }
            }

            current = current.above();
        }
    }

    /**
     * 扫描分支
     */
    private void scanBranch(MillVillager villager, BlockPos start) {
        if (treeBlocks.contains(start) || treeBlocks.size() > 100) {
            return; // 防止无限递归和过大的树
        }

        if (!isLog(villager.level().getBlockState(start))) {
            return;
        }

        treeBlocks.add(start);

        // 递归检查相邻原木
        for (BlockPos adjacent : getAdjacentPositions(start)) {
            scanBranch(villager, adjacent);
        }
    }

    /**
     * 获取相邻位置（不包括正下方）
     */
    private List<BlockPos> getAdjacentPositions(BlockPos pos) {
        List<BlockPos> adjacent = new ArrayList<>();
        adjacent.add(pos.above());
        adjacent.add(pos.north());
        adjacent.add(pos.south());
        adjacent.add(pos.east());
        adjacent.add(pos.west());
        adjacent.add(pos.above().north());
        adjacent.add(pos.above().south());
        adjacent.add(pos.above().east());
        adjacent.add(pos.above().west());
        return adjacent;
    }

    /**
     * 检查是否是原木
     */
    private boolean isLog(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG ||
               block == Blocks.BIRCH_LOG || block == Blocks.JUNGLE_LOG ||
               block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG ||
               block == Blocks.MANGROVE_LOG || block == Blocks.CHERRY_LOG ||
               state.is(BlockTags.LOGS);
    }

    /**
     * 检查是否是树底（泥土/草方块）
     */
    private boolean isTreeBase(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK ||
               block == Blocks.PODZOL || block == Blocks.ROOTED_DIRT ||
               block == Blocks.MUD || block == Blocks.MUDDY_MANGROVE_ROOTS;
    }

    @Override
    public void onAccept(MillVillager villager) {
        choppingTicks = 0;
        villager.setAnimationState(VillagerAnimationState.WALKING);
    }

    @Override
    public boolean performAction(MillVillager villager) {
        if (treeBlocks.isEmpty()) {
            return true;
        }

        // 设置砍伐动画
        villager.setAnimationState(VillagerAnimationState.CHOPPING);

        choppingTicks++;

        // 砍伐粒子效果
        if (choppingTicks % 5 == 0 && !treeBlocks.isEmpty() && villager.level() instanceof ServerLevel serverLevel) {
            BlockPos currentTarget = treeBlocks.get(0);
            BlockState state = serverLevel.getBlockState(currentTarget);
            if (!state.isAir()) {
                serverLevel.levelEvent(2001, currentTarget, Block.getId(state));
            }
        }

        if (choppingTicks < choppingDuration) {
            return false;
        }

        // 砍倒一个原木
        if (villager.level() instanceof ServerLevel serverLevel) {
            chopOneBlock(villager, serverLevel);
        }

        // 重置计时器准备砍下一个
        choppingTicks = 0;

        // 如果还有原木，继续砍
        return treeBlocks.isEmpty();
    }

    /**
     * 砍倒一个方块
     */
    private void chopOneBlock(MillVillager villager, ServerLevel level) {
        if (treeBlocks.isEmpty()) return;

        // 从底部开始砍
        BlockPos pos = treeBlocks.remove(0);
        BlockState state = level.getBlockState(pos);

        if (isLog(state)) {
            // 获取掉落物
            var drops = Block.getDrops(state, level, pos, null);
            for (ItemStack drop : drops) {
                villager.addToInventory(drop, drop.getCount());
            }

            // 破坏原木
            level.destroyBlock(pos, false);
        }
    }

    @Override
    public void onComplete(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        targetBlock = null;
        treeBlocks.clear();
    }

    @Override
    public void onInterrupt(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        targetBlock = null;
        treeBlocks.clear();
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        if (!villager.level().isDay()) {
            return false;
        }
        return villager.getProfession() == VillagerProfession.LUMBERJACK;
    }

    @Override
    protected boolean isPossibleSpecific(MillVillager villager) {
        if (!villager.level().isDay()) {
            return false;
        }

        if (villager.getProfession() != VillagerProfession.LUMBERJACK) {
            return false;
        }

        // 检查是否有可砍伐的树
        return findTreeBase(villager) != null;
    }

    @Nullable
    @Override
    public ItemStack[] getHeldItemsDestination(MillVillager villager) {
        return new ItemStack[]{ new ItemStack(Items.IRON_AXE) };
    }

    /**
     * 设置搜索范围
     */
    public GoalChop withSearchRadius(int radius) {
        this.searchRadius = radius;
        return this;
    }

    /**
     * 设置是否清理树叶
     */
    public GoalChop withClearLeaves(boolean clear) {
        this.clearLeaves = clear;
        return this;
    }
}
