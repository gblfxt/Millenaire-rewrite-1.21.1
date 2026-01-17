package com.jasoncian.millenaire_rewrite.entity.ai.goals;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalInformation;
import com.jasoncian.millenaire_rewrite.entity.ai.MillGoal;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import com.jasoncian.millenaire_rewrite.item.InvItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 农耕目标 - 农民种植和收割作物
 *
 * 支持：
 * - 耕作土地
 * - 播种种子
 * - 收割成熟作物
 *
 * @author Based on OldSource GoalFarm
 * @version 1.0.0
 */
public class GoalFarm extends MillGoal {

    /** 搜索农田范围 */
    private int searchRadius = 16;

    /** 当前工作类型 */
    private FarmAction currentAction = FarmAction.NONE;

    /** 目标方块位置 */
    @Nullable
    private BlockPos targetBlock;

    /** 工作动画时长（ticks） */
    private int workDuration = 30;

    /** 当前工作计时 */
    private int workTicks = 0;

    private enum FarmAction {
        NONE,
        TILL,      // 耕作
        PLANT,     // 播种
        HARVEST    // 收割
    }

    public GoalFarm() {
        super("farm");
        this.leisure = false;
        this.tags.add("work");
        this.tags.add("profession");
        this.tags.add("farmer");
        this.range = 2.0;
        this.canBeDoneInDayTime = true;
        this.canBeDoneAtNight = false;
        this.minimumHour = 0;
        this.maximumHour = 12500;
    }

    @Override
    public int priority(MillVillager villager) {
        // 检查是否是农民
        VillagerProfession profession = villager.getProfession();
        if (profession != VillagerProfession.FARMER && profession != VillagerProfession.SILK_FARMER) {
            return 0;
        }

        // 日间工作高优先级
        if (villager.level().isDay()) {
            return 500;
        }

        return 0;
    }

    @Nullable
    @Override
    public GoalInformation getDestination(MillVillager villager) {
        // 优先收割成熟作物
        targetBlock = findMatureCrop(villager);
        if (targetBlock != null) {
            currentAction = FarmAction.HARVEST;
            return GoalInformation.ofDestination(targetBlock);
        }

        // 其次寻找可耕作的土地
        if (hasSeeds(villager)) {
            targetBlock = findTillableGround(villager);
            if (targetBlock != null) {
                currentAction = FarmAction.TILL;
                return GoalInformation.ofDestination(targetBlock);
            }

            // 寻找可播种的农田
            targetBlock = findPlantableGround(villager);
            if (targetBlock != null) {
                currentAction = FarmAction.PLANT;
                return GoalInformation.ofDestination(targetBlock);
            }
        }

        return null;
    }

    /**
     * 查找成熟的作物
     */
    @Nullable
    private BlockPos findMatureCrop(MillVillager villager) {
        BlockPos center = villager.blockPosition();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = villager.level().getBlockState(checkPos);
                    Block block = state.getBlock();

                    if (block instanceof CropBlock cropBlock) {
                        if (cropBlock.isMaxAge(state)) {
                            return checkPos;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 查找可耕作的土地（草方块或泥土）
     */
    @Nullable
    private BlockPos findTillableGround(MillVillager villager) {
        BlockPos center = villager.blockPosition();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                for (int y = 2; y >= -2; y--) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = villager.level().getBlockState(checkPos);
                    Block block = state.getBlock();

                    // 检查是否可以耕作
                    if (block instanceof GrassBlock || block == Blocks.DIRT) {
                        // 确保上方是空气
                        if (villager.level().getBlockState(checkPos.above()).isAir()) {
                            // 确保附近有水（8格范围）
                            if (hasWaterNearby(villager, checkPos)) {
                                return checkPos;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 查找可播种的农田
     */
    @Nullable
    private BlockPos findPlantableGround(MillVillager villager) {
        BlockPos center = villager.blockPosition();

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
                for (int y = 2; y >= -2; y--) {
                    BlockPos checkPos = center.offset(x, y, z);
                    BlockState state = villager.level().getBlockState(checkPos);

                    // 检查是否是空的农田
                    if (state.getBlock() == Blocks.FARMLAND) {
                        BlockState above = villager.level().getBlockState(checkPos.above());
                        if (above.isAir()) {
                            return checkPos.above(); // 返回作物位置（农田上方）
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * 检查附近是否有水
     */
    private boolean hasWaterNearby(MillVillager villager, BlockPos pos) {
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                BlockPos waterCheck = pos.offset(x, 0, z);
                if (villager.level().getBlockState(waterCheck).getBlock() == Blocks.WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查是否有种子
     */
    private boolean hasSeeds(MillVillager villager) {
        return villager.countInInventory(Items.WHEAT_SEEDS) > 0 ||
               villager.countInInventory(Items.CARROT) > 0 ||
               villager.countInInventory(Items.POTATO) > 0 ||
               villager.countInInventory(Items.BEETROOT_SEEDS) > 0;
    }

    @Override
    public void onAccept(MillVillager villager) {
        workTicks = 0;
        villager.setAnimationState(VillagerAnimationState.WALKING);
    }

    @Override
    public boolean performAction(MillVillager villager) {
        if (targetBlock == null || currentAction == FarmAction.NONE) {
            return true;
        }

        // 设置工作动画
        villager.setAnimationState(VillagerAnimationState.FARMING);

        workTicks++;

        if (workTicks < workDuration) {
            return false;
        }

        // 执行动作
        if (villager.level() instanceof ServerLevel serverLevel) {
            switch (currentAction) {
                case TILL -> performTill(villager, serverLevel);
                case PLANT -> performPlant(villager, serverLevel);
                case HARVEST -> performHarvest(villager, serverLevel);
            }
        }

        return true;
    }

    /**
     * 执行耕作
     */
    private void performTill(MillVillager villager, ServerLevel level) {
        if (targetBlock == null) return;

        BlockState state = level.getBlockState(targetBlock);
        if (state.getBlock() instanceof GrassBlock || state.getBlock() == Blocks.DIRT) {
            level.setBlock(targetBlock, Blocks.FARMLAND.defaultBlockState(), 3);

            // 播放声音和粒子
            level.levelEvent(2001, targetBlock, Block.getId(state));
        }
    }

    /**
     * 执行播种
     */
    private void performPlant(MillVillager villager, ServerLevel level) {
        if (targetBlock == null) return;

        // 选择种子并播种
        Block cropToPlant = null;
        Item seedItem = null;

        if (villager.countInInventory(Items.WHEAT_SEEDS) > 0) {
            cropToPlant = Blocks.WHEAT;
            seedItem = Items.WHEAT_SEEDS;
        } else if (villager.countInInventory(Items.CARROT) > 0) {
            cropToPlant = Blocks.CARROTS;
            seedItem = Items.CARROT;
        } else if (villager.countInInventory(Items.POTATO) > 0) {
            cropToPlant = Blocks.POTATOES;
            seedItem = Items.POTATO;
        } else if (villager.countInInventory(Items.BEETROOT_SEEDS) > 0) {
            cropToPlant = Blocks.BEETROOTS;
            seedItem = Items.BEETROOT_SEEDS;
        }

        if (cropToPlant != null && seedItem != null) {
            if (level.getBlockState(targetBlock).isAir()) {
                level.setBlock(targetBlock, cropToPlant.defaultBlockState(), 3);
                villager.takeFromInventory(seedItem, 1);
            }
        }
    }

    /**
     * 执行收割
     */
    private void performHarvest(MillVillager villager, ServerLevel level) {
        if (targetBlock == null) return;

        BlockState state = level.getBlockState(targetBlock);
        if (state.getBlock() instanceof CropBlock) {
            // 获取掉落物
            var drops = Block.getDrops(state, level, targetBlock, null);
            for (ItemStack drop : drops) {
                villager.addToInventory(drop, drop.getCount());
            }

            // 破坏作物
            level.destroyBlock(targetBlock, false);
        }
    }

    @Override
    public void onComplete(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        targetBlock = null;
        currentAction = FarmAction.NONE;
    }

    @Override
    public void onInterrupt(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        targetBlock = null;
        currentAction = FarmAction.NONE;
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        if (!villager.level().isDay()) {
            return false;
        }

        VillagerProfession profession = villager.getProfession();
        return profession == VillagerProfession.FARMER || profession == VillagerProfession.SILK_FARMER;
    }

    @Override
    protected boolean isPossibleSpecific(MillVillager villager) {
        if (!villager.level().isDay()) {
            return false;
        }

        VillagerProfession profession = villager.getProfession();
        if (profession != VillagerProfession.FARMER && profession != VillagerProfession.SILK_FARMER) {
            return false;
        }

        // 检查是否有可做的工作
        return findMatureCrop(villager) != null ||
               (hasSeeds(villager) && (findTillableGround(villager) != null || findPlantableGround(villager) != null));
    }

    @Nullable
    @Override
    public ItemStack[] getHeldItemsDestination(MillVillager villager) {
        // 持有锄头
        return new ItemStack[]{ new ItemStack(Items.IRON_HOE) };
    }

    /**
     * 设置搜索范围
     */
    public GoalFarm withSearchRadius(int radius) {
        this.searchRadius = radius;
        return this;
    }
}
