package com.jasoncian.millenaire_rewrite.building;

import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 建筑蓝图 - 定义建筑的结构和需求
 *
 * 包含：
 * - 建筑尺寸和方块布局
 * - 所需资源
 * - 功能位置（床、工作台等）
 * - 建筑升级路径
 *
 * @author Based on OldSource BuildingPlan
 * @version 1.0.0
 */
public class BuildingBlueprint {

    // ================ 基础属性 ================

    /** 蓝图唯一键 */
    private final String key;

    /** 显示名称 */
    private String displayName;

    /** 所属文化 */
    private Culture culture;

    /** 建筑类型 */
    private BuildingType type;

    /** 建筑等级（用于升级系统） */
    private int level = 1;

    // ================ 尺寸 ================

    /** 建筑长度（X轴） */
    private int length = 5;

    /** 建筑宽度（Z轴） */
    private int width = 5;

    /** 建筑高度（Y轴） */
    private int height = 4;

    // ================ 方块布局 ================

    /** 方块布局（相对于原点的位置 -> 方块状态） */
    private final Map<BlockPos, BlockState> blockLayout = new HashMap<>();

    // ================ 功能位置 ================

    /** 睡眠位置（相对坐标） */
    private final List<BlockPos> sleepingSpots = new ArrayList<>();

    /** 工作位置（相对坐标） */
    private final List<BlockPos> workSpots = new ArrayList<>();

    /** 储物位置（相对坐标） */
    private final List<BlockPos> storageSpots = new ArrayList<>();

    /** 入口位置（相对坐标） */
    @Nullable
    private BlockPos entranceSpot;

    // ================ 资源需求 ================

    /** 建造所需资源 */
    private final Map<Item, Integer> requiredResources = new LinkedHashMap<>();

    // ================ 村民容量 ================

    /** 可容纳的村民数量 */
    private int villagerCapacity = 2;

    /** 居住村民的职业要求（null表示无限制） */
    @Nullable
    private VillagerProfession requiredProfession;

    // ================ 升级 ================

    /** 升级后的蓝图键（null表示无法升级） */
    @Nullable
    private String upgradeKey;

    /** 升级所需资源 */
    private final Map<Item, Integer> upgradeResources = new LinkedHashMap<>();

    // ================ 优先级 ================

    /** 建造优先级（越低越优先） */
    private int buildPriority = 50;

    /** 最低村庄等级要求 */
    private int minVillageLevel = 1;

    // ================ 构造函数 ================

    public BuildingBlueprint(String key) {
        this.key = key;
        this.displayName = key;
        this.culture = Culture.NORMAN;
        this.type = BuildingType.HOUSE;
    }

    // ================ 方块布局管理 ================

    /**
     * 添加方块到布局
     */
    public void setBlock(int x, int y, int z, BlockState state) {
        blockLayout.put(new BlockPos(x, y, z), state);
    }

    /**
     * 添加方块到布局（使用默认状态）
     */
    public void setBlock(int x, int y, int z, Block block) {
        setBlock(x, y, z, block.defaultBlockState());
    }

    /**
     * 获取指定位置的方块状态
     */
    @Nullable
    public BlockState getBlock(int x, int y, int z) {
        return blockLayout.get(new BlockPos(x, y, z));
    }

    /**
     * 获取所有方块布局
     */
    public Map<BlockPos, BlockState> getBlockLayout() {
        return Collections.unmodifiableMap(blockLayout);
    }

    /**
     * 获取建造顺序（从底层到顶层）
     */
    public List<BlockPos> getBuildOrder() {
        List<BlockPos> order = new ArrayList<>(blockLayout.keySet());
        // 按Y坐标排序，底层先建
        order.sort(Comparator.<BlockPos>comparingInt(BlockPos::getY)
            .thenComparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getZ));
        return order;
    }

    // ================ 功能位置管理 ================

    public void addSleepingSpot(int x, int y, int z) {
        sleepingSpots.add(new BlockPos(x, y, z));
    }

    public void addWorkSpot(int x, int y, int z) {
        workSpots.add(new BlockPos(x, y, z));
    }

    public void addStorageSpot(int x, int y, int z) {
        storageSpots.add(new BlockPos(x, y, z));
    }

    public void setEntranceSpot(int x, int y, int z) {
        this.entranceSpot = new BlockPos(x, y, z);
    }

    // ================ 资源管理 ================

    /**
     * 添加建造资源需求
     */
    public void addResource(Item item, int count) {
        requiredResources.merge(item, count, Integer::sum);
    }

    /**
     * 添加升级资源需求
     */
    public void addUpgradeResource(Item item, int count) {
        upgradeResources.merge(item, count, Integer::sum);
    }

    /**
     * 自动计算资源需求（基于方块布局）
     */
    public void calculateResourcesFromLayout() {
        requiredResources.clear();

        for (BlockState state : blockLayout.values()) {
            Block block = state.getBlock();

            // 跳过空气
            if (block == Blocks.AIR) continue;

            // 映射方块到资源
            Item resource = blockToResource(block);
            if (resource != Items.AIR) {
                requiredResources.merge(resource, 1, Integer::sum);
            }
        }
    }

    /**
     * 将方块映射到所需资源
     */
    private Item blockToResource(Block block) {
        // 基础映射
        if (block == Blocks.OAK_PLANKS || block == Blocks.SPRUCE_PLANKS ||
            block == Blocks.BIRCH_PLANKS || block == Blocks.JUNGLE_PLANKS) {
            return Items.OAK_PLANKS;
        }
        if (block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG ||
            block == Blocks.BIRCH_LOG || block == Blocks.JUNGLE_LOG) {
            return Items.OAK_LOG;
        }
        if (block == Blocks.COBBLESTONE || block == Blocks.STONE) {
            return Items.COBBLESTONE;
        }
        if (block == Blocks.STONE_BRICKS) {
            return Items.STONE_BRICKS;
        }
        if (block == Blocks.GLASS || block == Blocks.GLASS_PANE) {
            return Items.GLASS;
        }
        if (block == Blocks.TERRACOTTA) {
            return Items.TERRACOTTA;
        }
        if (block == Blocks.BRICKS) {
            return Items.BRICK;
        }

        // 默认返回方块物品
        return block.asItem();
    }

    // ================ 生成简单建筑 ================

    /**
     * 生成简单的矩形房屋布局
     */
    public void generateSimpleHouse(int width, int depth, int height, Block wallBlock, Block floorBlock, Block roofBlock) {
        this.length = width;
        this.width = depth;
        this.height = height;

        blockLayout.clear();

        // 地板
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                setBlock(x, 0, z, floorBlock);
            }
        }

        // 墙壁
        for (int y = 1; y < height - 1; y++) {
            for (int x = 0; x < width; x++) {
                setBlock(x, y, 0, wallBlock);
                setBlock(x, y, depth - 1, wallBlock);
            }
            for (int z = 1; z < depth - 1; z++) {
                setBlock(0, y, z, wallBlock);
                setBlock(width - 1, y, z, wallBlock);
            }
        }

        // 门（在前墙中间）
        int doorX = width / 2;
        setBlock(doorX, 1, 0, Blocks.AIR);
        setBlock(doorX, 2, 0, Blocks.AIR);
        setEntranceSpot(doorX, 1, -1);

        // 屋顶
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                setBlock(x, height - 1, z, roofBlock);
            }
        }

        // 床（在角落）
        addSleepingSpot(1, 1, depth - 2);
        addSleepingSpot(width - 2, 1, depth - 2);
    }

    // ================ Getters/Setters ================

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Culture getCulture() {
        return culture;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public BuildingType getType() {
        return type;
    }

    public void setType(BuildingType type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setDimensions(int length, int width, int height) {
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public List<BlockPos> getSleepingSpots() {
        return Collections.unmodifiableList(sleepingSpots);
    }

    public List<BlockPos> getWorkSpots() {
        return Collections.unmodifiableList(workSpots);
    }

    public List<BlockPos> getStorageSpots() {
        return Collections.unmodifiableList(storageSpots);
    }

    @Nullable
    public BlockPos getEntranceSpot() {
        return entranceSpot;
    }

    public Map<Item, Integer> getRequiredResources() {
        return Collections.unmodifiableMap(requiredResources);
    }

    public Map<Item, Integer> getUpgradeResources() {
        return Collections.unmodifiableMap(upgradeResources);
    }

    public int getVillagerCapacity() {
        return villagerCapacity;
    }

    public void setVillagerCapacity(int villagerCapacity) {
        this.villagerCapacity = villagerCapacity;
    }

    @Nullable
    public VillagerProfession getRequiredProfession() {
        return requiredProfession;
    }

    public void setRequiredProfession(@Nullable VillagerProfession requiredProfession) {
        this.requiredProfession = requiredProfession;
    }

    @Nullable
    public String getUpgradeKey() {
        return upgradeKey;
    }

    public void setUpgradeKey(@Nullable String upgradeKey) {
        this.upgradeKey = upgradeKey;
    }

    public int getBuildPriority() {
        return buildPriority;
    }

    public void setBuildPriority(int buildPriority) {
        this.buildPriority = buildPriority;
    }

    public int getMinVillageLevel() {
        return minVillageLevel;
    }

    public void setMinVillageLevel(int minVillageLevel) {
        this.minVillageLevel = minVillageLevel;
    }

    public int getTotalBlockCount() {
        return blockLayout.size();
    }

    @Override
    public String toString() {
        return "BuildingBlueprint{" +
            "key='" + key + '\'' +
            ", type=" + type +
            ", size=" + length + "x" + width + "x" + height +
            ", blocks=" + blockLayout.size() +
            '}';
    }
}
