package com.jasoncian.millenaire_rewrite.core;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/**
 * Armor Materials for Millenaire civilizations
 *
 * @author JasonCian, gblfxt
 * @version 0.2.0-alpha
 */
public class ModArmorMaterials {

    // ================ Norman Armor ================

    public static final Holder<ArmorMaterial> NORMAN = register("norman",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.HELMET, 2);
            }),
            12, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(Items.IRON_INGOT), 0.0F, 0.0F);

    // ================ Byzantine Armor ================

    public static final Holder<ArmorMaterial> BYZANTINE = register("byzantine",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 7);
                map.put(ArmorItem.Type.HELMET, 3);
            }),
            15, SoundEvents.ARMOR_EQUIP_GOLD, () -> Ingredient.of(Items.GOLD_INGOT), 1.0F, 0.0F);

    // ================ Japanese Armor ================

    public static final Holder<ArmorMaterial> JAPANESE_GUARD = register("japanese_guard",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 5);
                map.put(ArmorItem.Type.CHESTPLATE, 6);
                map.put(ArmorItem.Type.HELMET, 2);
            }),
            10, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.IRON_INGOT), 0.5F, 0.0F);

    public static final Holder<ArmorMaterial> JAPANESE_BLUE = register("japanese_blue",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
            }),
            18, SoundEvents.ARMOR_EQUIP_DIAMOND, () -> Ingredient.of(Items.DIAMOND), 2.0F, 0.05F);

    public static final Holder<ArmorMaterial> JAPANESE_RED = register("japanese_red",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 7);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 4);
            }),
            20, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> Ingredient.of(Items.NETHERITE_INGOT), 3.0F, 0.1F);

    // ================ Mayan Armor ================

    public static final Holder<ArmorMaterial> MAYAN_CEREMONIAL = register("mayan_ceremonial",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);
                map.put(ArmorItem.Type.LEGGINGS, 2);
                map.put(ArmorItem.Type.CHESTPLATE, 3);
                map.put(ArmorItem.Type.HELMET, 4);
            }),
            25, SoundEvents.ARMOR_EQUIP_GOLD, () -> Ingredient.of(Items.GOLD_BLOCK), 0.0F, 0.0F);

    // ================ Inuit Armor ================

    public static final Holder<ArmorMaterial> FUR = register("fur",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 2);
                map.put(ArmorItem.Type.LEGGINGS, 4);
                map.put(ArmorItem.Type.CHESTPLATE, 5);
                map.put(ArmorItem.Type.HELMET, 2);
            }),
            8, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.LEATHER), 0.0F, 0.0F);

    // ================ Seljuk Armor ================

    public static final Holder<ArmorMaterial> SELJUK = register("seljuk",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.LEGGINGS, 7);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.HELMET, 3);
            }),
            16, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(Items.GOLD_INGOT), 1.5F, 0.05F);

    public static final Holder<ArmorMaterial> SELJUK_WOOL = register("seljuk_wool",
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.BOOTS, 1);
                map.put(ArmorItem.Type.LEGGINGS, 2);
                map.put(ArmorItem.Type.CHESTPLATE, 3);
                map.put(ArmorItem.Type.HELMET, 1);
            }),
            5, SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.of(Items.WHITE_WOOL), 0.0F, 0.0F);

    // ================ Registration Helper ================

    private static Holder<ArmorMaterial> register(String name,
                                                   EnumMap<ArmorItem.Type, Integer> defense,
                                                   int enchantmentValue,
                                                   Holder<SoundEvent> equipSound,
                                                   Supplier<Ingredient> repairIngredient,
                                                   float toughness,
                                                   float knockbackResistance) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(MillenaireRewrite.MOD_ID, name);

        // Create the layers list for armor textures
        List<ArmorMaterial.Layer> layers = List.of(new ArmorMaterial.Layer(loc));

        // Create the armor material
        ArmorMaterial material = new ArmorMaterial(defense, enchantmentValue, equipSound, repairIngredient, layers, toughness, knockbackResistance);

        // Register it to the built-in registry
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, loc, material);
    }
}
