package com.jasoncian.millenaire_rewrite.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 库存物品包装类 - 用于高效的物品标识和缓存
 *
 * 基于OldSource InvItem实现，适配NeoForge 1.21.1
 * 特点：
 * - 实例缓存避免重复创建
 * - 支持通配符匹配（任意变体）
 * - 高效的哈希和相等性比较
 *
 * @author Based on OldSource InvItem
 * @version 1.0.0
 */
public class InvItem {

    // ================ 缓存 ================

    /** 实例缓存，通过哈希值快速查找 */
    private static final Map<Integer, InvItem> CACHE = new HashMap<>();

    // ================ 特殊代码 ================

    /** 无特殊属性 */
    public static final int SPECIAL_NONE = 0;

    /** 任意附魔 */
    public static final int SPECIAL_ANY_ENCHANTED = 1;

    /** 附魔剑 */
    public static final int SPECIAL_ENCHANTED_SWORD = 2;

    /** 附魔弓 */
    public static final int SPECIAL_ENCHANTED_BOW = 3;

    /** 附魔盔甲 */
    public static final int SPECIAL_ENCHANTED_ARMOR = 4;

    // ================ 通配符元数据 ================

    /** 通配符元数据值（匹配任意变体） */
    public static final int META_WILDCARD = -1;

    // ================ 字段 ================

    /** 物品类型 */
    private final Item item;

    /** 元数据/变体（-1 = 通配符） */
    private final int meta;

    /** 特殊代码 */
    private final int special;

    /** 预计算的哈希值 */
    private final int hashCode;

    // ================ 构造函数 ================

    private InvItem(Item item, int meta, int special) {
        this.item = item;
        this.meta = meta;
        this.special = special;
        this.hashCode = computeHash(item, meta, special);
    }

    // ================ 工厂方法 ================

    /**
     * 从Item创建InvItem（无元数据）
     */
    public static InvItem create(Item item) {
        return create(item, 0, SPECIAL_NONE);
    }

    /**
     * 从Item和元数据创建InvItem
     */
    public static InvItem create(Item item, int meta) {
        return create(item, meta, SPECIAL_NONE);
    }

    /**
     * 从Item、元数据和特殊代码创建InvItem
     */
    public static InvItem create(Item item, int meta, int special) {
        if (item == null) {
            return null;
        }

        int hash = computeHash(item, meta, special);

        // 检查缓存
        InvItem cached = CACHE.get(hash);
        if (cached != null && cached.item == item && cached.meta == meta && cached.special == special) {
            return cached;
        }

        // 创建新实例并缓存
        InvItem newItem = new InvItem(item, meta, special);
        CACHE.put(hash, newItem);
        return newItem;
    }

    /**
     * 从ItemStack创建InvItem
     */
    public static InvItem create(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        // 在1.21中，元数据概念已被组件系统取代
        // 我们使用0作为默认元数据
        return create(stack.getItem(), 0, SPECIAL_NONE);
    }

    /**
     * 从Block创建InvItem
     */
    public static InvItem create(Block block) {
        if (block == null) {
            return null;
        }
        return create(block.asItem(), 0, SPECIAL_NONE);
    }

    /**
     * 创建通配符InvItem（匹配任意元数据）
     */
    public static InvItem createWildcard(Item item) {
        return create(item, META_WILDCARD, SPECIAL_NONE);
    }

    // ================ 哈希计算 ================

    private static int computeHash(Item item, int meta, int special) {
        return Objects.hashCode(item) + (meta << 8) + (special << 16);
    }

    // ================ 匹配方法 ================

    /**
     * 检查是否匹配指定的ItemStack
     * 通配符元数据匹配任意变体
     */
    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == this.item;
    }

    /**
     * 检查是否匹配另一个InvItem
     * 通配符支持：如果任一方元数据为-1，则忽略元数据比较
     */
    public boolean matches(InvItem other) {
        if (other == null) {
            return false;
        }
        if (this.item != other.item) {
            return false;
        }
        // 通配符匹配
        if (this.meta == META_WILDCARD || other.meta == META_WILDCARD) {
            return true;
        }
        return this.meta == other.meta;
    }

    // ================ 转换方法 ================

    /**
     * 创建单个物品的ItemStack
     */
    public ItemStack toItemStack() {
        return toItemStack(1);
    }

    /**
     * 创建指定数量的ItemStack
     */
    public ItemStack toItemStack(int count) {
        return new ItemStack(item, count);
    }

    // ================ Getters ================

    public Item getItem() {
        return item;
    }

    public int getMeta() {
        return meta;
    }

    public int getSpecial() {
        return special;
    }

    public boolean isWildcard() {
        return meta == META_WILDCARD;
    }

    /**
     * 获取物品的注册名称
     */
    public ResourceLocation getRegistryName() {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    /**
     * 获取用于显示的名称
     */
    public String getDisplayName() {
        return toItemStack().getHoverName().getString();
    }

    // ================ Object方法 ================

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InvItem other)) return false;
        return this.item == other.item &&
               this.meta == other.meta &&
               this.special == other.special;
    }

    @Override
    public String toString() {
        String name = getRegistryName().toString();
        if (meta == META_WILDCARD) {
            return name + ":*";
        } else if (meta != 0) {
            return name + ":" + meta;
        }
        return name;
    }

    // ================ 缓存管理 ================

    /**
     * 清除缓存（通常在资源重载时调用）
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * 获取缓存大小（调试用）
     */
    public static int getCacheSize() {
        return CACHE.size();
    }
}
