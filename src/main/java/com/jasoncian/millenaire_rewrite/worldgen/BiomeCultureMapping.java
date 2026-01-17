package com.jasoncian.millenaire_rewrite.worldgen;

import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 生物群系-文化映射 - 决定哪种文化在哪些生物群系中生成
 *
 * 映射规则：
 * - Norman: 温带森林、平原
 * - Japanese: 樱花林、竹林、山地
 * - Indian: 热带草原、丛林边缘
 * - Mayan: 丛林、沼泽
 * - Byzantine: 温带平原、稀树草原
 * - Inuit: 雪地、冰原、针叶林
 * - Seljuk: 沙漠、恶地、干旱地区
 *
 * @author Based on OldSource biome mapping
 * @version 1.0.0
 */
public class BiomeCultureMapping {

    // ================ 映射表 ================

    /** 生物群系 -> 可用文化列表 */
    private static final Map<ResourceKey<Biome>, List<Culture>> BIOME_CULTURES = new HashMap<>();

    /** 文化 -> 优先生物群系列表 */
    private static final Map<Culture, List<ResourceKey<Biome>>> CULTURE_PREFERRED_BIOMES = new EnumMap<>(Culture.class);

    // ================ 初始化 ================

    static {
        initializeMappings();
    }

    /**
     * 初始化所有映射
     */
    private static void initializeMappings() {
        // Norman - 欧洲温带气候
        addCultureBiomes(Culture.NORMAN,
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.FOREST,
            Biomes.FLOWER_FOREST,
            Biomes.BIRCH_FOREST,
            Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.DARK_FOREST,
            Biomes.MEADOW
        );

        // Japanese - 东亚气候
        addCultureBiomes(Culture.JAPANESE,
            Biomes.CHERRY_GROVE,
            Biomes.BAMBOO_JUNGLE,
            Biomes.WINDSWEPT_HILLS,
            Biomes.WINDSWEPT_FOREST,
            Biomes.STONY_PEAKS,
            Biomes.FOREST,
            Biomes.TAIGA
        );

        // Indian - 热带/亚热带
        addCultureBiomes(Culture.INDIAN,
            Biomes.SAVANNA,
            Biomes.SAVANNA_PLATEAU,
            Biomes.WINDSWEPT_SAVANNA,
            Biomes.SPARSE_JUNGLE,
            Biomes.PLAINS
        );

        // Mayan - 热带丛林
        addCultureBiomes(Culture.MAYAN,
            Biomes.JUNGLE,
            Biomes.SPARSE_JUNGLE,
            Biomes.BAMBOO_JUNGLE,
            Biomes.SWAMP,
            Biomes.MANGROVE_SWAMP
        );

        // Byzantine - 地中海气候
        addCultureBiomes(Culture.BYZANTINE,
            Biomes.PLAINS,
            Biomes.SUNFLOWER_PLAINS,
            Biomes.SAVANNA,
            Biomes.FOREST,
            Biomes.MEADOW,
            Biomes.STONY_SHORE
        );

        // Inuit - 寒冷气候
        addCultureBiomes(Culture.INUIT,
            Biomes.SNOWY_PLAINS,
            Biomes.ICE_SPIKES,
            Biomes.SNOWY_TAIGA,
            Biomes.FROZEN_RIVER,
            Biomes.SNOWY_BEACH,
            Biomes.GROVE,
            Biomes.FROZEN_PEAKS,
            Biomes.JAGGED_PEAKS,
            Biomes.SNOWY_SLOPES
        );

        // Seljuk - 干旱气候
        addCultureBiomes(Culture.SELJUK,
            Biomes.DESERT,
            Biomes.BADLANDS,
            Biomes.ERODED_BADLANDS,
            Biomes.WOODED_BADLANDS,
            Biomes.SAVANNA,
            Biomes.WINDSWEPT_SAVANNA
        );
    }

    /**
     * 添加文化-生物群系映射
     */
    @SafeVarargs
    private static void addCultureBiomes(Culture culture, ResourceKey<Biome>... biomes) {
        List<ResourceKey<Biome>> biomeList = Arrays.asList(biomes);
        CULTURE_PREFERRED_BIOMES.put(culture, biomeList);

        for (ResourceKey<Biome> biome : biomes) {
            BIOME_CULTURES.computeIfAbsent(biome, k -> new ArrayList<>()).add(culture);
        }
    }

    // ================ 查询方法 ================

    /**
     * 获取指定生物群系可用的文化列表
     */
    public static List<Culture> getCulturesForBiome(ResourceKey<Biome> biome) {
        return BIOME_CULTURES.getOrDefault(biome, Collections.emptyList());
    }

    /**
     * 获取指定生物群系的随机文化
     */
    @Nullable
    public static Culture getRandomCultureForBiome(ResourceKey<Biome> biome, Random random) {
        List<Culture> cultures = getCulturesForBiome(biome);
        if (cultures.isEmpty()) {
            return null;
        }
        return cultures.get(random.nextInt(cultures.size()));
    }

    /**
     * 检查文化是否可以在指定生物群系生成
     */
    public static boolean canCultureSpawnInBiome(Culture culture, ResourceKey<Biome> biome) {
        List<Culture> cultures = BIOME_CULTURES.get(biome);
        return cultures != null && cultures.contains(culture);
    }

    /**
     * 获取文化的首选生物群系
     */
    public static List<ResourceKey<Biome>> getPreferredBiomes(Culture culture) {
        return CULTURE_PREFERRED_BIOMES.getOrDefault(culture, Collections.emptyList());
    }

    /**
     * 根据Biome Holder获取随机文化
     */
    @Nullable
    public static Culture getCultureForBiome(Holder<Biome> biomeHolder, Random random) {
        // 尝试直接匹配
        ResourceKey<Biome> biomeKey = biomeHolder.unwrapKey().orElse(null);
        if (biomeKey != null) {
            Culture culture = getRandomCultureForBiome(biomeKey, random);
            if (culture != null) {
                return culture;
            }
        }

        // 根据生物群系标签推断
        return inferCultureFromBiomeTags(biomeHolder, random);
    }

    /**
     * 根据生物群系标签推断合适的文化
     */
    @Nullable
    private static Culture inferCultureFromBiomeTags(Holder<Biome> biome, Random random) {
        List<Culture> candidates = new ArrayList<>();

        // 雪地生物群系 -> Inuit
        if (biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.SNOW_GOLEM_MELTS)) {
            candidates.add(Culture.INUIT);
        }

        // 丛林 -> Mayan
        if (biome.is(BiomeTags.IS_JUNGLE)) {
            candidates.add(Culture.MAYAN);
        }

        // 森林 -> Norman 或 Japanese
        if (biome.is(BiomeTags.IS_FOREST)) {
            candidates.add(Culture.NORMAN);
            candidates.add(Culture.JAPANESE);
        }

        // 稀树草原 -> Indian, Byzantine, Seljuk
        if (biome.is(BiomeTags.IS_SAVANNA)) {
            candidates.add(Culture.INDIAN);
            candidates.add(Culture.BYZANTINE);
            candidates.add(Culture.SELJUK);
        }

        // 恶地 -> Seljuk
        if (biome.is(BiomeTags.IS_BADLANDS)) {
            candidates.add(Culture.SELJUK);
        }

        // 山地 -> Japanese
        if (biome.is(BiomeTags.IS_MOUNTAIN)) {
            candidates.add(Culture.JAPANESE);
        }

        if (candidates.isEmpty()) {
            // 默认提供所有文化
            candidates.addAll(Arrays.asList(Culture.values()));
        }

        return candidates.get(random.nextInt(candidates.size()));
    }

    /**
     * 获取所有已映射的生物群系
     */
    public static Set<ResourceKey<Biome>> getAllMappedBiomes() {
        return Collections.unmodifiableSet(BIOME_CULTURES.keySet());
    }

    /**
     * 检查生物群系是否有任何文化映射
     */
    public static boolean hasCultureMapping(ResourceKey<Biome> biome) {
        return BIOME_CULTURES.containsKey(biome) && !BIOME_CULTURES.get(biome).isEmpty();
    }
}
