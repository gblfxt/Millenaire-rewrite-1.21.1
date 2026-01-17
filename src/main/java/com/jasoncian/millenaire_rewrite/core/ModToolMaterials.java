package com.jasoncian.millenaire_rewrite.core;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * Tool Materials for Millenaire civilizations
 *
 * Each culture has unique tool characteristics and repair materials
 *
 * @author JasonCian, gblfxt
 * @version 0.2.0-alpha
 */
public class ModToolMaterials {

    // ================ Norman Tools ================

    /** Norman Steel - Balanced medieval tools */
    public static final Tier NORMAN = new SimpleTier(
        BlockTags.INCORRECT_FOR_IRON_TOOL,  // Harvest level (iron level)
        400,                                 // Uses/Durability
        7.0F,                                // Speed
        2.5F,                                // Attack damage bonus
        12,                                  // Enchantability
        () -> Ingredient.of(Items.IRON_INGOT)
    );

    // ================ Mayan Obsidian Tools ================

    /** Mayan Obsidian - Sharp but fragile volcanic glass tools */
    public static final Tier MAYAN_OBSIDIAN = new SimpleTier(
        BlockTags.INCORRECT_FOR_STONE_TOOL,  // Harvest level (stone level)
        180,                                  // Uses/Durability (fragile)
        8.5F,                                 // Speed (very fast)
        3.0F,                                 // Attack damage bonus (sharp)
        5,                                    // Enchantability (low)
        () -> Ingredient.of(Items.OBSIDIAN)
    );

    // ================ Byzantine Steel Tools ================

    /** Byzantine Steel - Advanced medieval metalwork */
    public static final Tier BYZANTINE = new SimpleTier(
        BlockTags.INCORRECT_FOR_IRON_TOOL,  // Harvest level (iron level)
        450,                                 // Uses/Durability
        6.5F,                                // Speed (balanced)
        2.8F,                                // Attack damage bonus
        15,                                  // Enchantability (good)
        () -> Ingredient.of(Items.GOLD_INGOT)
    );

    // ================ Japanese Steel Tools ================

    /** Japanese Steel - Master-crafted folded steel weapons */
    public static final Tier JAPANESE = new SimpleTier(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL,  // Harvest level (diamond level)
        500,                                    // Uses/Durability
        8.0F,                                   // Speed (fast)
        3.5F,                                   // Attack damage bonus
        18,                                     // Enchantability
        () -> Ingredient.of(Items.DIAMOND)
    );

    // ================ Inuit Survival Tools ================

    /** Inuit Tools - Traditional arctic survival tools */
    public static final Tier INUIT = new SimpleTier(
        BlockTags.INCORRECT_FOR_STONE_TOOL,  // Harvest level (stone level)
        250,                                  // Uses/Durability
        5.0F,                                 // Speed
        2.0F,                                 // Attack damage bonus
        10,                                   // Enchantability
        () -> Ingredient.of(Items.BONE)
    );

    // ================ Seljuk Steel Tools ================

    /** Seljuk Steel - High quality Damascus steel weapons */
    public static final Tier SELJUK = new SimpleTier(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL,  // Harvest level (diamond level)
        600,                                    // Uses/Durability
        7.5F,                                   // Speed
        4.0F,                                   // Attack damage bonus
        20,                                     // Enchantability
        () -> Ingredient.of(Items.GOLD_INGOT)
    );

    /**
     * Initialize tier sorting relationships
     */
    public static void initializeTierSorting() {
        MillenaireRewrite.LOGGER.info("Millenaire tool tiers initialized");
    }

    /**
     * Simple Tier implementation for NeoForge 1.21.1
     */
    public record SimpleTier(
        TagKey<Block> incorrectBlocksForDrops,
        int uses,
        float speed,
        float attackDamageBonus,
        int enchantmentValue,
        Supplier<Ingredient> repairIngredient
    ) implements Tier {

        @Override
        public int getUses() {
            return uses;
        }

        @Override
        public float getSpeed() {
            return speed;
        }

        @Override
        public float getAttackDamageBonus() {
            return attackDamageBonus;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return incorrectBlocksForDrops;
        }

        @Override
        public int getEnchantmentValue() {
            return enchantmentValue;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return repairIngredient.get();
        }
    }
}
