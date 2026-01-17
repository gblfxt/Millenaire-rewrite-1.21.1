package com.jasoncian.millenaire_rewrite.village;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 建筑位置数据 - 存储单个建筑的位置和功能信息
 *
 * 基于OldSource BuildingLocation.java
 * 包含：
 * - 建筑位置和尺寸
 * - 建筑模板引用
 * - 功能位置（睡眠、交易、工作等）
 * - 建筑等级和状态
 *
 * @author Based on OldSource BuildingLocation
 * @version 1.0.0
 */
public class BuildingLocation {

    // ================ 基础属性 ================

    /** 建筑中心位置 */
    private BlockPos pos;

    /** 建筑模板键（引用BuildingPlan） */
    private String planKey;

    /** 建筑显示名称 */
    private String displayName;

    /** 建筑等级（0=未建造，1+=已建造等级） */
    private int level = 0;

    /** 建筑朝向（0-3，表示90度旋转） */
    private int orientation = 0;

    // ================ 尺寸信息 ================

    /** 建筑长度（X轴） */
    private int length = 1;

    /** 建筑宽度（Z轴） */
    private int width = 1;

    /** 建筑高度（Y轴） */
    private int height = 1;

    /** 边界框最小点 */
    private BlockPos minPos;

    /** 边界框最大点 */
    private BlockPos maxPos;

    // ================ 功能位置 ================

    /** 睡眠位置（床） */
    private final List<BlockPos> sleepingPositions = new ArrayList<>();

    /** 交易位置 */
    private final List<BlockPos> sellingPositions = new ArrayList<>();

    /** 工作/制作位置 */
    private final List<BlockPos> craftingPositions = new ArrayList<>();

    /** 储物箱位置 */
    private final List<BlockPos> chestPositions = new ArrayList<>();

    /** 防御位置 */
    private final List<BlockPos> defendingPositions = new ArrayList<>();

    /** 入口位置 */
    @Nullable
    private BlockPos entrancePos;

    // ================ 属性 ================

    /** 建筑价格（购买/建造成本） */
    private int price = 0;

    /** 建筑声望要求 */
    private int reputationRequired = 0;

    /** 移入优先级 */
    private int priorityMoveIn = 0;

    /** 是否是市政厅 */
    private boolean isTownHall = false;

    /** 是否是自定义建筑 */
    private boolean isCustomBuilding = false;

    /** 建造完成度（0-100） */
    private int constructionProgress = 0;

    // ================ 引用 ================

    /** 所属村庄 */
    @Nullable
    private Village village;

    /** 子建筑列表（扩展/附属建筑） */
    private final List<String> subBuildings = new ArrayList<>();

    // ================ 构造函数 ================

    public BuildingLocation(BlockPos pos, String planKey) {
        this.pos = pos;
        this.planKey = planKey;
        this.displayName = planKey;
        calculateBounds();
    }

    /**
     * 从NBT恢复的构造函数
     */
    private BuildingLocation() {
    }

    // ================ 边界计算 ================

    /**
     * 计算建筑边界框
     */
    private void calculateBounds() {
        int halfLength = length / 2;
        int halfWidth = width / 2;

        // 根据朝向旋转
        if (orientation == 0 || orientation == 2) {
            minPos = pos.offset(-halfLength, 0, -halfWidth);
            maxPos = pos.offset(halfLength, height, halfWidth);
        } else {
            minPos = pos.offset(-halfWidth, 0, -halfLength);
            maxPos = pos.offset(halfWidth, height, halfLength);
        }
    }

    /**
     * 设置建筑尺寸
     */
    public void setDimensions(int length, int width, int height) {
        this.length = length;
        this.width = width;
        this.height = height;
        calculateBounds();
    }

    // ================ 位置检查 ================

    /**
     * 检查位置是否在建筑范围内
     */
    public boolean containsPos(BlockPos checkPos) {
        return checkPos.getX() >= minPos.getX() && checkPos.getX() <= maxPos.getX() &&
               checkPos.getY() >= minPos.getY() && checkPos.getY() <= maxPos.getY() &&
               checkPos.getZ() >= minPos.getZ() && checkPos.getZ() <= maxPos.getZ();
    }

    /**
     * 获取到建筑中心的距离平方
     */
    public double distanceToSqr(BlockPos from) {
        return pos.distSqr(from);
    }

    // ================ 功能位置管理 ================

    public void addSleepingPosition(BlockPos pos) {
        if (!sleepingPositions.contains(pos)) {
            sleepingPositions.add(pos);
        }
    }

    public void addSellingPosition(BlockPos pos) {
        if (!sellingPositions.contains(pos)) {
            sellingPositions.add(pos);
        }
    }

    public void addCraftingPosition(BlockPos pos) {
        if (!craftingPositions.contains(pos)) {
            craftingPositions.add(pos);
        }
    }

    public void addChestPosition(BlockPos pos) {
        if (!chestPositions.contains(pos)) {
            chestPositions.add(pos);
        }
    }

    public void addDefendingPosition(BlockPos pos) {
        if (!defendingPositions.contains(pos)) {
            defendingPositions.add(pos);
        }
    }

    /**
     * 获取一个空闲的睡眠位置
     */
    @Nullable
    public BlockPos getFreeSleepingPosition() {
        // TODO: 检查哪些位置未被占用
        return sleepingPositions.isEmpty() ? null : sleepingPositions.get(0);
    }

    // ================ 建造管理 ================

    /**
     * 检查建筑是否已建造完成
     */
    public boolean isBuilt() {
        return level > 0 && constructionProgress >= 100;
    }

    /**
     * 检查建筑是否正在建造中
     */
    public boolean isUnderConstruction() {
        return constructionProgress > 0 && constructionProgress < 100;
    }

    /**
     * 增加建造进度
     */
    public void addConstructionProgress(int amount) {
        constructionProgress = Math.min(100, constructionProgress + amount);
        if (constructionProgress >= 100 && level == 0) {
            level = 1;
        }
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // 基础属性
        tag.putInt("PosX", pos.getX());
        tag.putInt("PosY", pos.getY());
        tag.putInt("PosZ", pos.getZ());
        tag.putString("PlanKey", planKey);
        tag.putString("DisplayName", displayName);
        tag.putInt("Level", level);
        tag.putInt("Orientation", orientation);

        // 尺寸
        tag.putInt("Length", length);
        tag.putInt("Width", width);
        tag.putInt("Height", height);

        // 属性
        tag.putInt("Price", price);
        tag.putInt("ReputationRequired", reputationRequired);
        tag.putInt("PriorityMoveIn", priorityMoveIn);
        tag.putBoolean("IsTownHall", isTownHall);
        tag.putBoolean("IsCustomBuilding", isCustomBuilding);
        tag.putInt("ConstructionProgress", constructionProgress);

        // 入口
        if (entrancePos != null) {
            tag.putInt("EntranceX", entrancePos.getX());
            tag.putInt("EntranceY", entrancePos.getY());
            tag.putInt("EntranceZ", entrancePos.getZ());
        }

        // 功能位置
        tag.put("SleepingPositions", savePositionList(sleepingPositions));
        tag.put("SellingPositions", savePositionList(sellingPositions));
        tag.put("CraftingPositions", savePositionList(craftingPositions));
        tag.put("ChestPositions", savePositionList(chestPositions));
        tag.put("DefendingPositions", savePositionList(defendingPositions));

        // 子建筑
        ListTag subTag = new ListTag();
        for (String sub : subBuildings) {
            CompoundTag subCompound = new CompoundTag();
            subCompound.putString("Key", sub);
            subTag.add(subCompound);
        }
        tag.put("SubBuildings", subTag);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static BuildingLocation load(CompoundTag tag) {
        BuildingLocation building = new BuildingLocation();

        // 基础属性
        building.pos = new BlockPos(
            tag.getInt("PosX"),
            tag.getInt("PosY"),
            tag.getInt("PosZ"));
        building.planKey = tag.getString("PlanKey");
        building.displayName = tag.getString("DisplayName");
        building.level = tag.getInt("Level");
        building.orientation = tag.getInt("Orientation");

        // 尺寸
        building.length = tag.getInt("Length");
        building.width = tag.getInt("Width");
        building.height = tag.getInt("Height");
        building.calculateBounds();

        // 属性
        building.price = tag.getInt("Price");
        building.reputationRequired = tag.getInt("ReputationRequired");
        building.priorityMoveIn = tag.getInt("PriorityMoveIn");
        building.isTownHall = tag.getBoolean("IsTownHall");
        building.isCustomBuilding = tag.getBoolean("IsCustomBuilding");
        building.constructionProgress = tag.getInt("ConstructionProgress");

        // 入口
        if (tag.contains("EntranceX")) {
            building.entrancePos = new BlockPos(
                tag.getInt("EntranceX"),
                tag.getInt("EntranceY"),
                tag.getInt("EntranceZ"));
        }

        // 功能位置
        loadPositionList(tag.getList("SleepingPositions", 10), building.sleepingPositions);
        loadPositionList(tag.getList("SellingPositions", 10), building.sellingPositions);
        loadPositionList(tag.getList("CraftingPositions", 10), building.craftingPositions);
        loadPositionList(tag.getList("ChestPositions", 10), building.chestPositions);
        loadPositionList(tag.getList("DefendingPositions", 10), building.defendingPositions);

        // 子建筑
        ListTag subTag = tag.getList("SubBuildings", 10);
        for (int i = 0; i < subTag.size(); i++) {
            building.subBuildings.add(subTag.getCompound(i).getString("Key"));
        }

        return building;
    }

    private ListTag savePositionList(List<BlockPos> positions) {
        ListTag tag = new ListTag();
        for (BlockPos p : positions) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("X", p.getX());
            posTag.putInt("Y", p.getY());
            posTag.putInt("Z", p.getZ());
            tag.add(posTag);
        }
        return tag;
    }

    private static void loadPositionList(ListTag tag, List<BlockPos> target) {
        target.clear();
        for (int i = 0; i < tag.size(); i++) {
            CompoundTag posTag = tag.getCompound(i);
            target.add(new BlockPos(
                posTag.getInt("X"),
                posTag.getInt("Y"),
                posTag.getInt("Z")));
        }
    }

    // ================ Getters/Setters ================

    public BlockPos getPos() {
        return pos;
    }

    public String getPlanKey() {
        return planKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
        calculateBounds();
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

    public BlockPos getMinPos() {
        return minPos;
    }

    public BlockPos getMaxPos() {
        return maxPos;
    }

    public List<BlockPos> getSleepingPositions() {
        return Collections.unmodifiableList(sleepingPositions);
    }

    public List<BlockPos> getSellingPositions() {
        return Collections.unmodifiableList(sellingPositions);
    }

    public List<BlockPos> getCraftingPositions() {
        return Collections.unmodifiableList(craftingPositions);
    }

    public List<BlockPos> getChestPositions() {
        return Collections.unmodifiableList(chestPositions);
    }

    public List<BlockPos> getDefendingPositions() {
        return Collections.unmodifiableList(defendingPositions);
    }

    @Nullable
    public BlockPos getEntrancePos() {
        return entrancePos;
    }

    public void setEntrancePos(@Nullable BlockPos entrancePos) {
        this.entrancePos = entrancePos;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getReputationRequired() {
        return reputationRequired;
    }

    public void setReputationRequired(int reputationRequired) {
        this.reputationRequired = reputationRequired;
    }

    public int getPriorityMoveIn() {
        return priorityMoveIn;
    }

    public void setPriorityMoveIn(int priorityMoveIn) {
        this.priorityMoveIn = priorityMoveIn;
    }

    public boolean isTownHall() {
        return isTownHall;
    }

    public void setTownHall(boolean townHall) {
        isTownHall = townHall;
    }

    public boolean isCustomBuilding() {
        return isCustomBuilding;
    }

    public void setCustomBuilding(boolean customBuilding) {
        isCustomBuilding = customBuilding;
    }

    public int getConstructionProgress() {
        return constructionProgress;
    }

    @Nullable
    public Village getVillage() {
        return village;
    }

    public void setVillage(@Nullable Village village) {
        this.village = village;
    }

    public List<String> getSubBuildings() {
        return Collections.unmodifiableList(subBuildings);
    }

    public void addSubBuilding(String key) {
        if (!subBuildings.contains(key)) {
            subBuildings.add(key);
        }
    }

    @Override
    public String toString() {
        return "BuildingLocation{" +
            "planKey='" + planKey + '\'' +
            ", pos=" + pos +
            ", level=" + level +
            ", built=" + isBuilt() +
            '}';
    }
}
