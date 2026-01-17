package com.jasoncian.millenaire_rewrite.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;

/**
 * Helper class for NBT access in 1.21+
 *
 * Minecraft 1.21 moved ItemStack data from NBT to DataComponents.
 * This helper provides backwards-compatible methods.
 */
public class ItemNBTHelper {

    /**
     * Get the custom NBT tag from an ItemStack
     * Returns null if no custom data exists
     */
    @Nullable
    public static CompoundTag getTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        return data.copyTag();
    }

    /**
     * Get or create the custom NBT tag for an ItemStack
     */
    public static CompoundTag getOrCreateTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return new CompoundTag();
        }
        return data.copyTag();
    }

    /**
     * Set the custom NBT tag on an ItemStack
     */
    public static void setTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Check if stack has custom NBT data
     */
    public static boolean hasTag(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /**
     * Get a string from the item's custom data
     */
    public static String getString(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        if (tag != null && tag.contains(key)) {
            return tag.getString(key);
        }
        return "";
    }

    /**
     * Set a string in the item's custom data
     */
    public static void setString(ItemStack stack, String key, String value) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putString(key, value);
        setTag(stack, tag);
    }

    /**
     * Get an int from the item's custom data
     */
    public static int getInt(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        if (tag != null && tag.contains(key)) {
            return tag.getInt(key);
        }
        return 0;
    }

    /**
     * Set an int in the item's custom data
     */
    public static void setInt(ItemStack stack, String key, int value) {
        CompoundTag tag = getOrCreateTag(stack);
        tag.putInt(key, value);
        setTag(stack, tag);
    }

    /**
     * Check if item has a specific key
     */
    public static boolean contains(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        return tag != null && tag.contains(key);
    }
}
