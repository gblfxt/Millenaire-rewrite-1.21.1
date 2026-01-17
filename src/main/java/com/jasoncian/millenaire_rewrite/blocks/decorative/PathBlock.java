package com.jasoncian.millenaire_rewrite.blocks.decorative;

import com.jasoncian.millenaire_rewrite.blocks.system.BasicBuildingMaterial;
import com.jasoncian.millenaire_rewrite.blocks.system.BuildingMaterial;
import com.jasoncian.millenaire_rewrite.blocks.system.CulturalBlockFamily;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 路径方块 - 基于OldSource BlockPath实现
 *
 * 路径方块具有特殊的高度（15/16方块高度），
 * 与OldSource保持一致的渲染和碰撞特性。
 *
 * 功能特性：
 * - 15/16方块高度 (0.9375)
 * - 稳定属性（可选，用于控制AI行为）
 * - 文化特色支持
 * - 与建筑系统集成
 *
 * @author Based on OldSource BlockPath
 * @version 1.0.0
 */
public class PathBlock extends Block {

    /** 路径高度：15/16方块 = 0.9375 */
    private static final double PATH_HEIGHT = 0.9375;

    /** 稳定属性 - 标记路径是否为稳定路径（影响村民AI） */
    public static final BooleanProperty STABLE = BooleanProperty.create("stable");

    /** 碰撞形状 */
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

    protected final BasicBuildingMaterial baseMaterial;
    protected final CulturalBlockFamily culture;

    /**
     * 构造函数
     *
     * @param baseMaterial 基础建筑材料
     * @param culture 文化系列
     */
    public PathBlock(BasicBuildingMaterial baseMaterial, CulturalBlockFamily culture) {
        super(createProperties(baseMaterial.getMaterialProperties()));
        this.baseMaterial = baseMaterial;
        this.culture = culture;
        this.registerDefaultState(this.stateDefinition.any().setValue(STABLE, false));
    }

    /**
     * 根据建筑材料创建方块属性
     *
     * @param material 建筑材料
     * @return 方块属性
     */
    public static BlockBehaviour.Properties createProperties(BuildingMaterial material) {
        return BlockBehaviour.Properties.of()
            .strength(material.getHardness(), material.getExplosionResistance())
            .sound(material.getSoundType())
            .mapColor(material.getMapColor())
            .pushReaction(material.getPushReaction());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STABLE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(STABLE, false);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                   LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state;
    }

    // ================ 访问器方法 ================

    /** 获取基础建筑材料 */
    public BasicBuildingMaterial getBaseMaterial() {
        return baseMaterial;
    }

    /** 获取文化系列 */
    public CulturalBlockFamily getCulture() {
        return culture;
    }

    /** 获取路径高度 */
    public static double getPathHeight() {
        return PATH_HEIGHT;
    }

    /**
     * 检查路径是否稳定
     *
     * @param state 方块状态
     * @return 是否稳定
     */
    public boolean isStable(BlockState state) {
        return state.getValue(STABLE);
    }

    /**
     * 设置路径稳定状态
     *
     * @param state 方块状态
     * @param stable 是否稳定
     * @return 更新后的方块状态
     */
    public BlockState setStable(BlockState state, boolean stable) {
        return state.setValue(STABLE, stable);
    }
}
