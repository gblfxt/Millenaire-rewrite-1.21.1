package com.jasoncian.millenaire_rewrite.entity.ai.goals;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalInformation;
import com.jasoncian.millenaire_rewrite.entity.ai.MillGoal;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import com.jasoncian.millenaire_rewrite.item.InvItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 进食目标 - 村民在饥饿时寻找并消耗食物
 *
 * 基于村民的生命值判断饥饿程度
 * 优先从库存中取食物，找不到时暂停
 *
 * @author Based on OldSource GoalEat
 * @version 1.0.0
 */
public class GoalEat extends MillGoal {

    /** 进食动画时长（ticks） */
    private int eatingDuration = 40;

    /** 当前进食计时 */
    private int eatingTicks = 0;

    /** 正在吃的食物 */
    @Nullable
    private InvItem foodToEat;

    public GoalEat() {
        super("eat");
        this.leisure = false;
        this.tags.add("generic");
        this.tags.add("survival");
        this.range = 0.5;
    }

    @Override
    public int priority(MillVillager villager) {
        // 生命值越低，优先级越高
        float healthPercent = villager.getHealth() / villager.getMaxHealth();

        if (healthPercent < 0.3f) {
            return 900; // 非常饿
        } else if (healthPercent < 0.5f) {
            return 700; // 比较饿
        } else if (healthPercent < 0.7f) {
            return 400; // 有点饿
        } else if (healthPercent < 0.9f) {
            return 100; // 稍微饿
        }

        return 0; // 不饿
    }

    @Nullable
    @Override
    public GoalInformation getDestination(MillVillager villager) {
        // 检查库存是否有食物
        foodToEat = findFoodInInventory(villager);

        if (foodToEat != null) {
            // 就地吃
            return GoalInformation.ofDestination(villager.blockPosition());
        }

        // TODO: 去建筑/存储处找食物

        return null;
    }

    /**
     * 在库存中查找食物
     */
    @Nullable
    private InvItem findFoodInInventory(MillVillager villager) {
        Map<InvItem, Integer> inventory = villager.getInventoryCopy();

        InvItem bestFood = null;
        int bestNutrition = 0;

        for (InvItem item : inventory.keySet()) {
            Item mcItem = item.getItem();
            FoodProperties food = mcItem.components().get(net.minecraft.core.component.DataComponents.FOOD);

            if (food != null) {
                int nutrition = food.nutrition();
                if (nutrition > bestNutrition) {
                    bestNutrition = nutrition;
                    bestFood = item;
                }
            }
        }

        return bestFood;
    }

    @Override
    public void onAccept(MillVillager villager) {
        eatingTicks = 0;
        villager.setAnimationState(VillagerAnimationState.EATING);
    }

    @Override
    public boolean performAction(MillVillager villager) {
        if (foodToEat == null) {
            return true; // 没有食物，结束
        }

        eatingTicks++;

        if (eatingTicks >= eatingDuration) {
            // 完成进食
            consumeFood(villager);
            return true;
        }

        return false;
    }

    /**
     * 消耗食物并恢复生命值
     */
    private void consumeFood(MillVillager villager) {
        if (foodToEat == null) return;

        Item mcItem = foodToEat.getItem();
        FoodProperties food = mcItem.components().get(net.minecraft.core.component.DataComponents.FOOD);

        if (food != null) {
            // 从库存移除
            villager.takeFromInventory(foodToEat, 1);

            // 恢复生命值（基于营养值）
            float heal = food.nutrition() * 0.5f;
            villager.heal(heal);
        }

        foodToEat = null;
    }

    @Override
    public void onComplete(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        foodToEat = null;
    }

    @Override
    public void onInterrupt(MillVillager villager) {
        villager.setAnimationState(VillagerAnimationState.IDLE);
        foodToEat = null;
    }

    @Override
    public boolean isStillValid(MillVillager villager) {
        // 如果生命值满了，不再需要吃
        return villager.getHealth() < villager.getMaxHealth();
    }

    @Override
    protected boolean isPossibleSpecific(MillVillager villager) {
        // 只有当生命值不满且有食物时才能执行
        if (villager.getHealth() >= villager.getMaxHealth()) {
            return false;
        }

        return findFoodInInventory(villager) != null;
    }

    @Nullable
    @Override
    public ItemStack[] getHeldItemsDestination(MillVillager villager) {
        if (foodToEat != null) {
            return new ItemStack[]{ foodToEat.toItemStack() };
        }
        return null;
    }
}
