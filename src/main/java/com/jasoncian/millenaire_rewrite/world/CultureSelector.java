package com.jasoncian.millenaire_rewrite.world;

import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 文化选择器 - 根据生物群系选择适合的村庄文化
 *
 * 文化与生物群系映射：
 * - Norman: 平原、森林、温带
 * - Japanese: 竹林、樱花林
 * - Indian: 热带草原、丛林
 * - Mayan: 丛林、热带
 * - Byzantine: 山地、沙漠边缘
 * - Inuit: 雪地、冰原
 * - Seljuk: 沙漠、热带草原
 *
 * @author Based on OldSource culture biome logic
 * @version 1.0.0
 */
public class CultureSelector {

    // ================ 生物群系映射 ================

    /** 文化到生物群系的映射 */
    private static final Map<Culture, Set<String>> CULTURE_BIOMES = new EnumMap<>(Culture.class);

    /** 生物群系到文化权重的映射 */
    private static final Map<String, Map<Culture, Integer>> BIOME_CULTURE_WEIGHTS = new HashMap<>();

    static {
        initializeBiomeMappings();
    }

    /**
     * 初始化生物群系映射
     */
    private static void initializeBiomeMappings() {
        // Norman - 欧洲温带风格
        Set<String> normanBiomes = new HashSet<>();
        normanBiomes.add("minecraft:plains");
        normanBiomes.add("minecraft:sunflower_plains");
        normanBiomes.add("minecraft:forest");
        normanBiomes.add("minecraft:flower_forest");
        normanBiomes.add("minecraft:birch_forest");
        normanBiomes.add("minecraft:old_growth_birch_forest");
        normanBiomes.add("minecraft:dark_forest");
        normanBiomes.add("minecraft:meadow");
        normanBiomes.add("minecraft:river");
        CULTURE_BIOMES.put(Culture.NORMAN, normanBiomes);

        // Japanese - 东亚风格
        Set<String> japaneseBiomes = new HashSet<>();
        japaneseBiomes.add("minecraft:bamboo_jungle");
        japaneseBiomes.add("minecraft:cherry_grove");
        japaneseBiomes.add("minecraft:forest");
        japaneseBiomes.add("minecraft:taiga");
        japaneseBiomes.add("minecraft:old_growth_pine_taiga");
        japaneseBiomes.add("minecraft:old_growth_spruce_taiga");
        CULTURE_BIOMES.put(Culture.JAPANESE, japaneseBiomes);

        // Indian - 南亚热带风格
        Set<String> indianBiomes = new HashSet<>();
        indianBiomes.add("minecraft:jungle");
        indianBiomes.add("minecraft:sparse_jungle");
        indianBiomes.add("minecraft:savanna");
        indianBiomes.add("minecraft:savanna_plateau");
        indianBiomes.add("minecraft:plains");
        CULTURE_BIOMES.put(Culture.INDIAN, indianBiomes);

        // Mayan - 中美洲丛林风格
        Set<String> mayanBiomes = new HashSet<>();
        mayanBiomes.add("minecraft:jungle");
        mayanBiomes.add("minecraft:bamboo_jungle");
        mayanBiomes.add("minecraft:sparse_jungle");
        mayanBiomes.add("minecraft:mangrove_swamp");
        mayanBiomes.add("minecraft:swamp");
        CULTURE_BIOMES.put(Culture.MAYAN, mayanBiomes);

        // Byzantine - 地中海/山地风格
        Set<String> byzantineBiomes = new HashSet<>();
        byzantineBiomes.add("minecraft:stony_peaks");
        byzantineBiomes.add("minecraft:jagged_peaks");
        byzantineBiomes.add("minecraft:frozen_peaks");
        byzantineBiomes.add("minecraft:windswept_hills");
        byzantineBiomes.add("minecraft:windswept_gravelly_hills");
        byzantineBiomes.add("minecraft:windswept_forest");
        byzantineBiomes.add("minecraft:badlands");
        byzantineBiomes.add("minecraft:wooded_badlands");
        byzantineBiomes.add("minecraft:eroded_badlands");
        CULTURE_BIOMES.put(Culture.BYZANTINE, byzantineBiomes);

        // Inuit - 北极/寒冷风格
        Set<String> inuitBiomes = new HashSet<>();
        inuitBiomes.add("minecraft:snowy_plains");
        inuitBiomes.add("minecraft:ice_spikes");
        inuitBiomes.add("minecraft:snowy_taiga");
        inuitBiomes.add("minecraft:snowy_beach");
        inuitBiomes.add("minecraft:snowy_slopes");
        inuitBiomes.add("minecraft:grove");
        inuitBiomes.add("minecraft:frozen_river");
        inuitBiomes.add("minecraft:frozen_ocean");
        inuitBiomes.add("minecraft:deep_frozen_ocean");
        CULTURE_BIOMES.put(Culture.INUIT, inuitBiomes);

        // Seljuk - 中东/沙漠风格
        Set<String> seljukBiomes = new HashSet<>();
        seljukBiomes.add("minecraft:desert");
        seljukBiomes.add("minecraft:badlands");
        seljukBiomes.add("minecraft:wooded_badlands");
        seljukBiomes.add("minecraft:eroded_badlands");
        seljukBiomes.add("minecraft:savanna");
        seljukBiomes.add("minecraft:windswept_savanna");
        CULTURE_BIOMES.put(Culture.SELJUK, seljukBiomes);

        // 设置生物群系到文化的权重
        initializeBiomeWeights();
    }

    /**
     * 初始化生物群系权重
     */
    private static void initializeBiomeWeights() {
        // 为每个生物群系设置文化权重
        for (Map.Entry<Culture, Set<String>> entry : CULTURE_BIOMES.entrySet()) {
            Culture culture = entry.getKey();
            for (String biome : entry.getValue()) {
                BIOME_CULTURE_WEIGHTS.computeIfAbsent(biome, k -> new EnumMap<>(Culture.class))
                    .put(culture, 100); // 基础权重100
            }
        }

        // 特殊权重调整
        // 丛林中Mayan优先
        adjustWeight("minecraft:jungle", Culture.MAYAN, 150);
        adjustWeight("minecraft:bamboo_jungle", Culture.MAYAN, 150);

        // 樱花林中Japanese优先
        adjustWeight("minecraft:cherry_grove", Culture.JAPANESE, 200);

        // 沙漠中Seljuk优先
        adjustWeight("minecraft:desert", Culture.SELJUK, 150);

        // 雪地中Inuit优先
        adjustWeight("minecraft:snowy_plains", Culture.INUIT, 150);
        adjustWeight("minecraft:ice_spikes", Culture.INUIT, 200);

        // 平原中Norman最常见
        adjustWeight("minecraft:plains", Culture.NORMAN, 120);
    }

    /**
     * 调整权重
     */
    private static void adjustWeight(String biome, Culture culture, int weight) {
        BIOME_CULTURE_WEIGHTS.computeIfAbsent(biome, k -> new EnumMap<>(Culture.class))
            .put(culture, weight);
    }

    // ================ 公共方法 ================

    /**
     * 根据生物群系选择文化
     *
     * @param level 服务器世界
     * @param pos 位置
     * @return 选择的文化，如果无法确定返回默认文化
     */
    @Nullable
    public static Culture selectCultureForBiome(ServerLevel level, BlockPos pos) {
        // 获取生物群系
        Holder<Biome> biomeHolder = level.getBiome(pos);
        String biomeName = getBiomeName(biomeHolder);

        // 获取该生物群系的文化权重
        Map<Culture, Integer> weights = BIOME_CULTURE_WEIGHTS.get(biomeName);

        if (weights == null || weights.isEmpty()) {
            // 检查生物群系标签来推断文化
            return selectCultureByBiomeTags(level, pos, biomeHolder);
        }

        // 加权随机选择
        return weightedRandomSelect(weights, level.random.nextInt(1000));
    }

    /**
     * 获取适合指定生物群系的所有文化
     */
    public static List<Culture> getCulturesForBiome(String biomeName) {
        List<Culture> cultures = new ArrayList<>();

        for (Map.Entry<Culture, Set<String>> entry : CULTURE_BIOMES.entrySet()) {
            if (entry.getValue().contains(biomeName)) {
                cultures.add(entry.getKey());
            }
        }

        // 如果没有匹配，返回所有文化
        if (cultures.isEmpty()) {
            cultures.addAll(Arrays.asList(Culture.values()));
        }

        return cultures;
    }

    /**
     * 检查文化是否适合生物群系
     */
    public static boolean isCultureValidForBiome(Culture culture, String biomeName) {
        Set<String> validBiomes = CULTURE_BIOMES.get(culture);
        return validBiomes != null && validBiomes.contains(biomeName);
    }

    // ================ 私有方法 ================

    /**
     * 获取生物群系名称
     */
    private static String getBiomeName(Holder<Biome> biomeHolder) {
        return biomeHolder.unwrapKey()
            .map(key -> key.location().toString())
            .orElse("minecraft:plains");
    }

    /**
     * 根据生物群系标签选择文化
     */
    private static Culture selectCultureByBiomeTags(ServerLevel level, BlockPos pos, Holder<Biome> biomeHolder) {
        // 检查各种标签
        if (biomeHolder.is(BiomeTags.IS_JUNGLE)) {
            return level.random.nextBoolean() ? Culture.MAYAN : Culture.INDIAN;
        }
        if (biomeHolder.is(BiomeTags.IS_TAIGA)) {
            return level.random.nextBoolean() ? Culture.JAPANESE : Culture.NORMAN;
        }
        if (biomeHolder.is(BiomeTags.IS_FOREST)) {
            return Culture.NORMAN;
        }
        if (biomeHolder.is(BiomeTags.IS_SAVANNA)) {
            return level.random.nextBoolean() ? Culture.INDIAN : Culture.SELJUK;
        }
        if (biomeHolder.is(BiomeTags.IS_BADLANDS)) {
            return level.random.nextBoolean() ? Culture.BYZANTINE : Culture.SELJUK;
        }
        if (biomeHolder.is(BiomeTags.IS_MOUNTAIN)) {
            return Culture.BYZANTINE;
        }

        // 检查温度
        Biome biome = biomeHolder.value();
        float temp = biome.getBaseTemperature();

        if (temp < 0.2f) {
            return Culture.INUIT;
        } else if (temp > 1.5f) {
            return level.random.nextBoolean() ? Culture.SELJUK : Culture.INDIAN;
        }

        // 默认返回Norman
        return Culture.NORMAN;
    }

    /**
     * 加权随机选择
     */
    private static Culture weightedRandomSelect(Map<Culture, Integer> weights, int randomValue) {
        int totalWeight = weights.values().stream().mapToInt(Integer::intValue).sum();

        if (totalWeight <= 0) {
            return Culture.NORMAN;
        }

        int targetWeight = randomValue % totalWeight;
        int currentWeight = 0;

        for (Map.Entry<Culture, Integer> entry : weights.entrySet()) {
            currentWeight += entry.getValue();
            if (currentWeight > targetWeight) {
                return entry.getKey();
            }
        }

        // 默认返回第一个
        return weights.keySet().iterator().next();
    }

    /**
     * 获取文化的描述性标签
     */
    public static String getCultureBiomeDescription(Culture culture) {
        return switch (culture) {
            case NORMAN -> "Temperate forests, plains, meadows";
            case JAPANESE -> "Bamboo forests, cherry groves, taiga";
            case INDIAN -> "Jungles, savannas, plains";
            case MAYAN -> "Dense jungles, swamps";
            case BYZANTINE -> "Mountains, highlands, badlands";
            case INUIT -> "Snowy plains, ice spikes, frozen areas";
            case SELJUK -> "Deserts, badlands, dry savannas";
        };
    }
}
