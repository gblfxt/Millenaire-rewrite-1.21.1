package com.jasoncian.millenaire_rewrite.village;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.building.BuildingConstructionManager;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.item.InvItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 村庄数据类 - 管理一个Millenaire村庄的所有数据
 *
 * 基于OldSource Building.java（当作为townhall时的功能）
 * 管理：
 * - 村庄基础信息（名称、文化、位置）
 * - 建筑列表
 * - 村民记录
 * - 资源存储
 * - 建造项目
 *
 * @author Based on OldSource Building (townhall mode)
 * @version 1.0.0
 */
public class Village {

    // ================ 基础信息 ================

    /** 村庄唯一ID */
    private final UUID villageId;

    /** 村庄名称 */
    private String name;

    /** 村庄文化 */
    private Culture culture;

    /** 市政厅位置（村庄中心） */
    private BlockPos townHallPos;

    /** 村庄所在维度 */
    private String dimension;

    /** 村庄创建时间 */
    private long createdTime;

    // ================ 建筑管理 ================

    /** 所有建筑位置列表 */
    private final CopyOnWriteArrayList<BlockPos> buildingPositions = new CopyOnWriteArrayList<>();

    /** 建筑数据映射（位置 -> 建筑数据） */
    private final ConcurrentHashMap<BlockPos, BuildingLocation> buildings = new ConcurrentHashMap<>();

    // ================ 村民管理 ================

    /** 活跃村民实体列表 */
    private final Set<MillVillager> activeVillagers = Collections.synchronizedSet(new LinkedHashSet<>());

    /** 村民记录（持久化数据，包括离线村民） */
    private final ConcurrentHashMap<Long, VillagerRecord> villagerRecords = new ConcurrentHashMap<>();

    // ================ 资源管理 ================

    /** 村庄公共资源库存 */
    private final ConcurrentHashMap<InvItem, Integer> resources = new ConcurrentHashMap<>();

    /** 村庄金币 */
    private long deniers = 0;

    // ================ 建造项目 ================

    /** 待建造项目列表 */
    private final CopyOnWriteArrayList<BuildingProject> buildingProjects = new CopyOnWriteArrayList<>();

    // ================ 村民生成 ================

    /** 村民生成器 */
    private transient VillagerSpawner villagerSpawner;

    /** 建筑建造管理器 */
    private transient BuildingConstructionManager constructionManager;

    /** 所在世界引用 */
    private transient ServerLevel level;

    // ================ 状态标志 ================

    /** 是否已加载 */
    private boolean loaded = false;

    /** 是否需要保存 */
    private boolean dirty = false;

    // ================ 构造函数 ================

    public Village(BlockPos townHallPos, Culture culture) {
        this.villageId = UUID.randomUUID();
        this.townHallPos = townHallPos;
        this.culture = culture;
        this.name = generateVillageName(culture);
        this.createdTime = System.currentTimeMillis();
        this.villagerSpawner = new VillagerSpawner(this);
        this.constructionManager = new BuildingConstructionManager(this);
    }

    /**
     * 从NBT恢复的构造函数
     */
    public Village(UUID id) {
        this.villageId = id;
        this.villagerSpawner = new VillagerSpawner(this);
        this.constructionManager = new BuildingConstructionManager(this);
    }

    // ================ 名称生成 ================

    /**
     * 生成村庄名称
     */
    private String generateVillageName(Culture culture) {
        // TODO: 从文化名称库中生成
        return culture.getDisplayName() + " Village";
    }

    // ================ 建筑管理方法 ================

    /**
     * 添加建筑到村庄
     */
    public void addBuilding(BuildingLocation building) {
        BlockPos pos = building.getPos();
        if (!buildings.containsKey(pos)) {
            buildings.put(pos, building);
            buildingPositions.add(pos);
            building.setVillage(this);
            markDirty();

            MillenaireRewrite.LOGGER.debug("Added building {} to village {} at {}",
                building.getPlanKey(), name, pos);
        }
    }

    /**
     * 移除建筑
     */
    public void removeBuilding(BlockPos pos) {
        BuildingLocation building = buildings.remove(pos);
        if (building != null) {
            buildingPositions.remove(pos);
            building.setVillage(null);
            markDirty();
        }
    }

    /**
     * 获取建筑
     */
    @Nullable
    public BuildingLocation getBuilding(BlockPos pos) {
        return buildings.get(pos);
    }

    /**
     * 获取所有建筑
     */
    public Collection<BuildingLocation> getAllBuildings() {
        return buildings.values();
    }

    /**
     * 获取建筑数量
     */
    public int getBuildingCount() {
        return buildings.size();
    }

    /**
     * 检查位置是否在村庄范围内
     */
    public boolean isInVillageArea(BlockPos pos) {
        // 简单距离检查（可以后续优化为更复杂的边界检测）
        double distance = townHallPos.distSqr(pos);
        return distance <= getVillageRadius() * getVillageRadius();
    }

    /**
     * 获取村庄半径（基于建筑数量动态增长）
     */
    public int getVillageRadius() {
        return 50 + buildings.size() * 5;
    }

    // ================ 村民管理方法 ================

    /**
     * 注册村民到村庄
     */
    public void registerVillager(MillVillager villager) {
        activeVillagers.add(villager);

        // 确保有村民记录
        long villagerId = villager.getVillagerId();
        if (!villagerRecords.containsKey(villagerId)) {
            VillagerRecord record = new VillagerRecord(villager);
            villagerRecords.put(villagerId, record);
        }

        markDirty();
    }

    /**
     * 注销村民
     */
    public void unregisterVillager(MillVillager villager) {
        activeVillagers.remove(villager);

        // 更新记录但不删除（保留死亡/离开的村民记录）
        VillagerRecord record = villagerRecords.get(villager.getVillagerId());
        if (record != null) {
            record.updateFromVillager(villager);
        }

        markDirty();
    }

    /**
     * 获取活跃村民数量
     */
    public int getActiveVillagerCount() {
        return activeVillagers.size();
    }

    /**
     * 获取所有村民记录数量（包括死亡的）
     */
    public int getTotalVillagerRecordCount() {
        return villagerRecords.size();
    }

    /**
     * 获取村民记录
     */
    @Nullable
    public VillagerRecord getVillagerRecord(long villagerId) {
        return villagerRecords.get(villagerId);
    }

    /**
     * 获取所有活跃村民
     */
    public Set<MillVillager> getActiveVillagers() {
        return Collections.unmodifiableSet(activeVillagers);
    }

    // ================ 资源管理方法 ================

    /**
     * 添加资源到村庄库存
     */
    public void addResource(InvItem item, int count) {
        if (item == null || count <= 0) return;

        resources.merge(item, count, Integer::sum);
        markDirty();
    }

    /**
     * 添加资源（Item版本）
     */
    public void addResource(Item item, int count) {
        addResource(InvItem.create(item), count);
    }

    /**
     * 从村庄库存取出资源
     * @return 实际取出的数量
     */
    public int takeResource(InvItem item, int count) {
        if (item == null || count <= 0) return 0;

        Integer current = resources.get(item);
        if (current == null || current <= 0) return 0;

        int taken = Math.min(count, current);
        int remaining = current - taken;

        if (remaining <= 0) {
            resources.remove(item);
        } else {
            resources.put(item, remaining);
        }

        markDirty();
        return taken;
    }

    /**
     * 获取资源数量
     */
    public int getResourceCount(InvItem item) {
        if (item == null) return 0;
        return resources.getOrDefault(item, 0);
    }

    /**
     * 检查是否有足够资源
     */
    public boolean hasResource(InvItem item, int count) {
        return getResourceCount(item) >= count;
    }

    /**
     * 获取所有资源
     */
    public Map<InvItem, Integer> getAllResources() {
        return Collections.unmodifiableMap(resources);
    }

    /**
     * 获取金币
     */
    public long getDeniers() {
        return deniers;
    }

    /**
     * 添加金币
     */
    public void addDeniers(long amount) {
        this.deniers += amount;
        markDirty();
    }

    /**
     * 扣除金币
     * @return 是否成功扣除
     */
    public boolean spendDeniers(long amount) {
        if (deniers >= amount) {
            deniers -= amount;
            markDirty();
            return true;
        }
        return false;
    }

    // ================ 建造项目管理 ================

    /**
     * 添加建造项目
     */
    public void addBuildingProject(BuildingProject project) {
        buildingProjects.add(project);
        markDirty();
    }

    /**
     * 移除建造项目
     */
    public void removeBuildingProject(BuildingProject project) {
        buildingProjects.remove(project);
        markDirty();
    }

    /**
     * 获取所有建造项目
     */
    public List<BuildingProject> getBuildingProjects() {
        return Collections.unmodifiableList(buildingProjects);
    }

    /**
     * 获取下一个待建造项目
     */
    @Nullable
    public BuildingProject getNextProject() {
        return buildingProjects.isEmpty() ? null : buildingProjects.get(0);
    }

    // ================ Tick更新 ================

    /**
     * 每tick更新村庄
     * 由VillageManager调用
     */
    public void tick(ServerLevel level) {
        // 保存世界引用
        this.level = level;

        // 清理无效的村民引用
        activeVillagers.removeIf(v -> v.isRemoved() || !v.isAlive());

        // 更新村民生成
        if (villagerSpawner != null) {
            villagerSpawner.tick(level);
        }

        // 更新建筑建造
        if (constructionManager != null) {
            constructionManager.tick(level);
        }

        // TODO: 更新村庄经济
        // TODO: 触发村庄事件
    }

    // ================ 村民生成方法 ================

    /**
     * 获取村民生成器
     */
    public VillagerSpawner getVillagerSpawner() {
        if (villagerSpawner == null) {
            villagerSpawner = new VillagerSpawner(this);
        }
        return villagerSpawner;
    }

    /**
     * 获取建筑建造管理器
     */
    public BuildingConstructionManager getConstructionManager() {
        if (constructionManager == null) {
            constructionManager = new BuildingConstructionManager(this);
        }
        return constructionManager;
    }

    /**
     * 获取村庄所在世界
     */
    @Nullable
    public ServerLevel getServerLevel() {
        return level;
    }

    /**
     * 获取村庄等级（基于建筑数量和类型）
     * Used by BuildingRegistry to determine available buildings
     */
    public int getLevel() {
        int buildingCount = buildings.size();

        // 基于建筑数量计算等级
        if (buildingCount >= 20) return 5;
        if (buildingCount >= 15) return 4;
        if (buildingCount >= 10) return 3;
        if (buildingCount >= 5) return 2;
        return 1;
    }

    /**
     * 获取村庄半径
     */
    public int getRadius() {
        return getVillageRadius();
    }

    /**
     * 获取村庄中心位置
     */
    public BlockPos getCenterPos() {
        return townHallPos;
    }

    /**
     * 获取所有建筑（别名方法）
     */
    public Collection<BuildingLocation> getBuildings() {
        return getAllBuildings();
    }

    /**
     * 检查村庄是否已有某类型建筑
     */
    public boolean hasBuildingOfType(String blueprintKey) {
        for (BuildingLocation building : buildings.values()) {
            if (blueprintKey.equals(building.getPlanKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取某类型建筑的数量
     */
    public int countBuildingsOfType(String blueprintKey) {
        int count = 0;
        for (BuildingLocation building : buildings.values()) {
            if (blueprintKey.equals(building.getPlanKey())) {
                count++;
            }
        }
        return count;
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // 基础信息
        tag.putUUID("VillageId", villageId);
        tag.putString("Name", name);
        tag.putString("Culture", culture.getId());
        tag.putInt("TownHallX", townHallPos.getX());
        tag.putInt("TownHallY", townHallPos.getY());
        tag.putInt("TownHallZ", townHallPos.getZ());
        tag.putString("Dimension", dimension != null ? dimension : "");
        tag.putLong("CreatedTime", createdTime);
        tag.putLong("Deniers", deniers);

        // 保存建筑
        ListTag buildingsTag = new ListTag();
        for (BuildingLocation building : buildings.values()) {
            buildingsTag.add(building.save());
        }
        tag.put("Buildings", buildingsTag);

        // 保存村民记录
        ListTag villagersTag = new ListTag();
        for (VillagerRecord record : villagerRecords.values()) {
            villagersTag.add(record.save());
        }
        tag.put("VillagerRecords", villagersTag);

        // 保存资源
        ListTag resourcesTag = new ListTag();
        for (Map.Entry<InvItem, Integer> entry : resources.entrySet()) {
            CompoundTag resTag = new CompoundTag();
            resTag.putString("Item", entry.getKey().getRegistryName().toString());
            resTag.putInt("Meta", entry.getKey().getMeta());
            resTag.putInt("Count", entry.getValue());
            resourcesTag.add(resTag);
        }
        tag.put("Resources", resourcesTag);

        // 保存建造项目
        ListTag projectsTag = new ListTag();
        for (BuildingProject project : buildingProjects) {
            projectsTag.add(project.save());
        }
        tag.put("BuildingProjects", projectsTag);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public void load(CompoundTag tag) {
        name = tag.getString("Name");
        culture = Culture.fromId(tag.getString("Culture"));
        townHallPos = new BlockPos(
            tag.getInt("TownHallX"),
            tag.getInt("TownHallY"),
            tag.getInt("TownHallZ"));
        dimension = tag.getString("Dimension");
        createdTime = tag.getLong("CreatedTime");
        deniers = tag.getLong("Deniers");

        // 加载建筑
        buildings.clear();
        buildingPositions.clear();
        ListTag buildingsTag = tag.getList("Buildings", 10);
        for (int i = 0; i < buildingsTag.size(); i++) {
            BuildingLocation building = BuildingLocation.load(buildingsTag.getCompound(i));
            building.setVillage(this);
            buildings.put(building.getPos(), building);
            buildingPositions.add(building.getPos());
        }

        // 加载村民记录
        villagerRecords.clear();
        ListTag villagersTag = tag.getList("VillagerRecords", 10);
        for (int i = 0; i < villagersTag.size(); i++) {
            VillagerRecord record = VillagerRecord.load(villagersTag.getCompound(i));
            villagerRecords.put(record.getVillagerId(), record);
        }

        // 加载资源
        resources.clear();
        ListTag resourcesTag = tag.getList("Resources", 10);
        for (int i = 0; i < resourcesTag.size(); i++) {
            CompoundTag resTag = resourcesTag.getCompound(i);
            String itemId = resTag.getString("Item");
            int meta = resTag.getInt("Meta");
            int count = resTag.getInt("Count");

            net.minecraft.resources.ResourceLocation loc =
                net.minecraft.resources.ResourceLocation.tryParse(itemId);
            if (loc != null) {
                Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(loc);
                if (item != null && item != Items.AIR) {
                    InvItem invItem = InvItem.create(item, meta);
                    if (invItem != null) {
                        resources.put(invItem, count);
                    }
                }
            }
        }

        // 加载建造项目
        buildingProjects.clear();
        ListTag projectsTag = tag.getList("BuildingProjects", 10);
        for (int i = 0; i < projectsTag.size(); i++) {
            BuildingProject project = BuildingProject.load(projectsTag.getCompound(i));
            buildingProjects.add(project);
        }

        loaded = true;
        dirty = false;
    }

    // ================ 状态管理 ================

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    // ================ Getters ================

    public UUID getVillageId() {
        return villageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        markDirty();
    }

    public Culture getCulture() {
        return culture;
    }

    public BlockPos getTownHallPos() {
        return townHallPos;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public String toString() {
        return "Village{" +
            "name='" + name + '\'' +
            ", culture=" + culture.getId() +
            ", pos=" + townHallPos +
            ", buildings=" + buildings.size() +
            ", villagers=" + activeVillagers.size() +
            '}';
    }
}
