package com.jasoncian.millenaire_rewrite.building;

import com.jasoncian.millenaire_rewrite.village.BuildingLocation;
import com.jasoncian.millenaire_rewrite.village.BuildingProject;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 建筑建造管理器 - 处理建筑的实际建造过程
 *
 * 管理：
 * - 方块放置
 * - 建造进度
 * - 资源消耗
 * - 建筑完成处理
 *
 * @author Based on OldSource building placement
 * @version 1.0.0
 */
public class BuildingConstructionManager {

    // ================ 数据 ================

    /** 所属村庄 */
    private final Village village;

    /** 当前建造项目队列 */
    private final List<BuildingProject> projectQueue = new ArrayList<>();

    /** 当前活跃项目 */
    @Nullable
    private BuildingProject activeProject;

    /** 当前建造的蓝图 */
    @Nullable
    private BuildingBlueprint activeBlueprint;

    /** 当前建造位置索引 */
    private int currentBlockIndex = 0;

    /** 建造顺序 */
    private List<BlockPos> buildOrder = new ArrayList<>();

    // ================ 常量 ================

    /** 每tick最大放置方块数 */
    private static final int MAX_BLOCKS_PER_TICK = 3;

    /** 建造冷却（tick） */
    private int buildCooldown = 0;

    /** 建造间隔（tick） */
    private static final int BUILD_INTERVAL = 5;

    // ================ 构造函数 ================

    public BuildingConstructionManager(Village village) {
        this.village = village;
    }

    // ================ 项目管理 ================

    /**
     * 添加建造项目
     */
    public void addProject(BuildingProject project) {
        projectQueue.add(project);
        sortProjects();
    }

    /**
     * 取消项目
     */
    public void cancelProject(long projectId) {
        projectQueue.removeIf(p -> p.getProjectId() == projectId);
        if (activeProject != null && activeProject.getProjectId() == projectId) {
            activeProject.cancel();
            activeProject = null;
            activeBlueprint = null;
        }
    }

    /**
     * 排序项目队列
     */
    private void sortProjects() {
        projectQueue.sort(BuildingProject::comparePriority);
    }

    /**
     * 获取下一个项目
     */
    @Nullable
    private BuildingProject getNextProject() {
        for (BuildingProject project : projectQueue) {
            if (!project.isStarted() && !project.isCancelled() && project.hasEnoughResources()) {
                return project;
            }
        }
        return null;
    }

    // ================ Tick更新 ================

    /**
     * 每tick更新
     */
    public void tick(ServerLevel level) {
        // 冷却
        if (buildCooldown > 0) {
            buildCooldown--;
            return;
        }

        // 检查是否需要开始新项目
        if (activeProject == null) {
            activeProject = getNextProject();
            if (activeProject != null) {
                startProject(activeProject);
            }
            return;
        }

        // 检查项目是否被取消
        if (activeProject.isCancelled()) {
            activeProject = null;
            activeBlueprint = null;
            return;
        }

        // 检查项目是否完成
        if (activeProject.isCompleted()) {
            finishProject();
            return;
        }

        // 执行建造
        doBuild(level);
    }

    /**
     * 开始项目
     */
    private void startProject(BuildingProject project) {
        // 获取蓝图
        activeBlueprint = BuildingRegistry.getInstance().getBlueprint(project.getPlanKey());
        if (activeBlueprint == null) {
            project.cancel();
            activeProject = null;
            return;
        }

        // 开始项目
        project.start();

        // 设置建造顺序
        buildOrder = activeBlueprint.getBuildOrder();
        currentBlockIndex = 0;

        // 清理建造区域
        ServerLevel level = village.getServerLevel();
        if (level != null) {
            clearBuildArea(level, project.getTargetPos());
        }
    }

    /**
     * 清理建造区域
     */
    private void clearBuildArea(ServerLevel level, BlockPos origin) {
        if (activeBlueprint == null) return;

        int length = activeBlueprint.getLength();
        int width = activeBlueprint.getWidth();
        int height = activeBlueprint.getHeight();

        // 简单清理：将区域内的非固体方块替换为空气
        for (int x = 0; x < length; x++) {
            for (int z = 0; z < width; z++) {
                for (int y = 0; y < height; y++) {
                    BlockPos worldPos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(worldPos);

                    // 只清理可替换的方块
                    if (state.canBeReplaced() || state.is(Blocks.GRASS_BLOCK) ||
                        state.is(Blocks.DIRT) || state.is(Blocks.TALL_GRASS)) {
                        level.setBlock(worldPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * 执行建造
     */
    private void doBuild(ServerLevel level) {
        if (activeProject == null || activeBlueprint == null) return;

        int blocksPlaced = 0;

        while (blocksPlaced < MAX_BLOCKS_PER_TICK && currentBlockIndex < buildOrder.size()) {
            BlockPos relativePos = buildOrder.get(currentBlockIndex);
            BlockState state = activeBlueprint.getBlockLayout().get(relativePos);

            if (state != null && state.getBlock() != Blocks.AIR) {
                // 计算世界坐标
                BlockPos worldPos = activeProject.getTargetPos().offset(relativePos);

                // 放置方块
                if (placeBlock(level, worldPos, state)) {
                    blocksPlaced++;
                }
            }

            currentBlockIndex++;
        }

        // 更新进度
        if (buildOrder.size() > 0) {
            int progress = (currentBlockIndex * 100) / buildOrder.size();
            while (activeProject.getProgress() < progress) {
                activeProject.addProgress(1);
            }
        }

        // 检查是否完成
        if (currentBlockIndex >= buildOrder.size()) {
            activeProject.complete();
        }

        // 设置冷却
        buildCooldown = BUILD_INTERVAL;
    }

    /**
     * 放置单个方块
     */
    private boolean placeBlock(ServerLevel level, BlockPos pos, BlockState state) {
        // 检查位置是否可以放置
        BlockState current = level.getBlockState(pos);
        if (!current.canBeReplaced() && current.getBlock() != Blocks.AIR) {
            // 位置被占用，跳过
            return false;
        }

        // 放置方块
        level.setBlock(pos, state, 3);
        return true;
    }

    /**
     * 完成项目
     */
    private void finishProject() {
        if (activeProject == null || activeBlueprint == null) return;

        // 创建建筑位置
        BuildingLocation location = new BuildingLocation(
            activeProject.getTargetPos(),
            activeProject.getPlanKey()
        );

        location.setDisplayName(activeBlueprint.getDisplayName());
        location.setLevel(activeProject.getTargetLevel());
        location.setOrientation(activeProject.getOrientation());
        location.setDimensions(
            activeBlueprint.getLength(),
            activeBlueprint.getWidth(),
            activeBlueprint.getHeight()
        );

        // 添加功能位置
        BlockPos origin = activeProject.getTargetPos();
        for (BlockPos spot : activeBlueprint.getSleepingSpots()) {
            location.addSleepingPosition(origin.offset(spot));
        }
        for (BlockPos spot : activeBlueprint.getWorkSpots()) {
            location.addCraftingPosition(origin.offset(spot));
        }
        for (BlockPos spot : activeBlueprint.getStorageSpots()) {
            location.addChestPosition(origin.offset(spot));
        }
        if (activeBlueprint.getEntranceSpot() != null) {
            location.setEntrancePos(origin.offset(activeBlueprint.getEntranceSpot()));
        }

        // 设置建筑类型相关属性
        if (activeBlueprint.getType() == BuildingType.TOWN_HALL) {
            location.setTownHall(true);
        }

        // 添加到村庄
        village.addBuilding(location);

        // 从队列移除
        projectQueue.remove(activeProject);

        // 重置
        activeProject = null;
        activeBlueprint = null;
        currentBlockIndex = 0;
        buildOrder.clear();
    }

    // ================ 建造请求 ================

    /**
     * 请求建造建筑
     *
     * @param blueprintKey 蓝图键
     * @param position 建造位置
     * @return 建造项目，如果无法建造返回null
     */
    @Nullable
    public BuildingProject requestBuild(String blueprintKey, BlockPos position) {
        BuildingBlueprint blueprint = BuildingRegistry.getInstance().getBlueprint(blueprintKey);
        if (blueprint == null) {
            return null;
        }

        // 创建项目
        BuildingProject project = new BuildingProject(blueprintKey, position);
        project.setDisplayName(blueprint.getDisplayName());
        project.setTier(BuildingProject.ProjectTier.PLAYER);

        // 添加资源需求
        for (Map.Entry<net.minecraft.world.item.Item, Integer> entry : blueprint.getRequiredResources().entrySet()) {
            project.addRequiredResource(entry.getKey(), entry.getValue());
        }

        // 添加到队列
        addProject(project);

        return project;
    }

    /**
     * 自动选择下一个建筑
     */
    @Nullable
    public BuildingProject autoSelectNextBuilding() {
        // 获取可用蓝图
        List<BuildingBlueprint> available = BuildingRegistry.getInstance()
            .getAvailableBlueprints(village.getCulture(), village.getLevel());

        // 检查哪些建筑还没建
        for (BuildingBlueprint blueprint : available) {
            if (!village.hasBuildingOfType(blueprint.getKey())) {
                // 找一个合适的位置
                BlockPos buildPos = findBuildPosition(blueprint);
                if (buildPos != null) {
                    return requestBuild(blueprint.getKey(), buildPos);
                }
            }
        }

        return null;
    }

    /**
     * 寻找建造位置
     */
    @Nullable
    private BlockPos findBuildPosition(BuildingBlueprint blueprint) {
        BlockPos center = village.getCenterPos();
        ServerLevel level = village.getServerLevel();
        if (level == null) return null;

        int radius = village.getRadius();
        int length = blueprint.getLength();
        int width = blueprint.getWidth();

        // 简单实现：在村庄范围内寻找空位
        for (int attempt = 0; attempt < 20; attempt++) {
            int offsetX = level.random.nextInt(radius * 2) - radius;
            int offsetZ = level.random.nextInt(radius * 2) - radius;

            BlockPos candidate = center.offset(offsetX, 0, offsetZ);

            // 找到地面高度
            candidate = findGroundLevel(level, candidate);
            if (candidate == null) continue;

            // 检查是否与现有建筑重叠
            if (!isAreaClear(candidate, length, width)) continue;

            return candidate;
        }

        return null;
    }

    /**
     * 找到地面高度
     */
    @Nullable
    private BlockPos findGroundLevel(ServerLevel level, BlockPos pos) {
        // 从高处向下找
        int maxY = level.getMaxBuildHeight() - 1;
        int minY = level.getMinBuildHeight();
        for (int y = maxY; y > minY; y--) {
            BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
            BlockState state = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());

            if (state.isSolid() && !above.isSolid()) {
                return check.above();
            }
        }
        return null;
    }

    /**
     * 检查区域是否空闲
     */
    private boolean isAreaClear(BlockPos origin, int length, int width) {
        for (BuildingLocation building : village.getBuildings()) {
            // 简单距离检查
            if (building.getPos().distSqr(origin) < (length + width) * (length + width)) {
                return false;
            }
        }
        return true;
    }

    // ================ Getters ================

    public List<BuildingProject> getProjectQueue() {
        return Collections.unmodifiableList(projectQueue);
    }

    @Nullable
    public BuildingProject getActiveProject() {
        return activeProject;
    }

    public boolean hasActiveProject() {
        return activeProject != null;
    }

    public int getPendingProjectCount() {
        return projectQueue.size();
    }
}
