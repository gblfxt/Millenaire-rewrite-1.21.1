package com.jasoncian.millenaire_rewrite.entity;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerAnimationState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.core.NonNullList;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.jasoncian.millenaire_rewrite.menu.VillagerInteractionMenu;
import com.jasoncian.millenaire_rewrite.item.InvItem;
import com.jasoncian.millenaire_rewrite.entity.ai.GoalManager;
import com.jasoncian.millenaire_rewrite.entity.ai.goals.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Millenaire村民实体 - 基于OldSource MillVillager实现
 *
 * 核心村民实体类，管理：
 * - 身份与文化归属
 * - 库存系统
 * - AI目标系统
 * - 外观与渲染
 * - 与村庄系统的集成
 *
 * @author Based on OldSource MillVillager
 * @version 1.0.0
 */
public class MillVillager extends PathfinderMob {

    // ================ 同步数据访问器 ================

    private static final EntityDataAccessor<String> DATA_CULTURE =
        SynchedEntityData.defineId(MillVillager.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_PROFESSION =
        SynchedEntityData.defineId(MillVillager.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_GENDER =
        SynchedEntityData.defineId(MillVillager.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_FIRST_NAME =
        SynchedEntityData.defineId(MillVillager.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_FAMILY_NAME =
        SynchedEntityData.defineId(MillVillager.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHILD =
        SynchedEntityData.defineId(MillVillager.class, EntityDataSerializers.BOOLEAN);

    // ================ 常量 ================

    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;

    // ================ 身份数据 ================

    /** 唯一村民ID（用于持久化追踪） */
    private long villagerId;

    /** 村民的家位置 */
    @Nullable
    private BlockPos housePos;

    /** 村民所属村庄的市政厅位置 */
    @Nullable
    private BlockPos townHallPos;

    // ================ 库存系统 ================

    /** 自定义库存（InvItem->数量映射） */
    private final Map<InvItem, Integer> inventory = new HashMap<>();

    /** 主手持有物品 */
    private ItemStack heldItem = ItemStack.EMPTY;

    /** 副手持有物品 */
    private ItemStack heldItemOffHand = ItemStack.EMPTY;

    /** 持有物品计数器（用于动画/轮换） */
    private int heldItemCount = 0;

    /** 当前主手物品索引 */
    private int heldItemIndex = -1;

    /** 当前副手物品索引 */
    private int heldItemOffHandIndex = -1;

    // ================ AI与目标 ================

    /** Millenaire目标管理器 */
    private final GoalManager goalManager;

    /** 当前目标键 */
    private String currentGoalKey = "";

    /** 目标开始时间 */
    private long goalStarted = 0;

    // ================ 雇佣系统 ================

    /** 雇佣者玩家名 */
    @Nullable
    private String hiredBy;

    /** 雇佣到期时间 */
    private long hiredUntil = 0;

    /** 是否处于攻击姿态 */
    private boolean aggressiveStance = false;

    // ================ 外观 ================

    /** 衣服纹理名称 */
    private String clothName = "";

    /** 衣服纹理数组（支持2层） */
    @Nullable
    private ResourceLocation[] clothTextures = new ResourceLocation[2];

    /** 身高比例（0-1范围，影响模型缩放） */
    private float villagerScale = 1.0f;

    /** 当前动画状态 */
    private VillagerAnimationState animationState = VillagerAnimationState.IDLE;

    /** 动画计时器（用于循环动画） */
    private int animationTicks = 0;

    // ================ 构造函数 ================

    public MillVillager(EntityType<? extends MillVillager> entityType, Level level) {
        super(entityType, level);
        this.villagerId = UUID.randomUUID().getMostSignificantBits();
        this.goalManager = new GoalManager(this);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);

        // 初始化Millenaire目标系统
        initializeGoals();
    }

    /**
     * 初始化Millenaire目标系统
     * 添加基础通用目标和职业特定目标
     */
    private void initializeGoals() {
        // 通用目标（所有村民都有）
        goalManager.addGoal(new GoalIdle());
        goalManager.addGoal(new GoalWander());
        goalManager.addGoal(new GoalSleep());
        goalManager.addGoal(new GoalEat());

        // 职业特定目标会在tick中根据当前职业动态添加
    }

    /**
     * 根据职业刷新工作目标
     * 当职业变化时调用
     */
    public void refreshWorkGoals() {
        // 移除现有的工作目标
        goalManager.getAvailableGoals().removeIf(goal ->
            goal.getTags().contains("work"));

        // 根据当前职业添加对应目标
        VillagerProfession profession = getProfession();

        switch (profession) {
            case FARMER, SILK_FARMER -> goalManager.addGoal(new GoalFarm());
            case MINER -> goalManager.addGoal(new GoalMine());
            case LUMBERJACK -> goalManager.addGoal(new GoalChop());
            // TODO: 添加更多职业的工作目标
            default -> {
                // 无特定工作目标
            }
        }
    }

    // ================ 属性定义 ================

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    // ================ 数据同步 ================

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CULTURE, Culture.NORMAN.getId());
        builder.define(DATA_PROFESSION, VillagerProfession.FARMER.getId());
        builder.define(DATA_GENDER, GENDER_MALE);
        builder.define(DATA_FIRST_NAME, "");
        builder.define(DATA_FAMILY_NAME, "");
        builder.define(DATA_IS_CHILD, false);
    }

    // ================ AI目标 ================

    @Override
    protected void registerGoals() {
        // 基础生存目标
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));

        // TODO: 添加自定义Millenaire目标系统
        // - GoalSleep
        // - GoalEat
        // - GoalWork (profession-specific)
        // - GoalSocialize
    }

    // ================ Tick更新 ================

    @Override
    public void tick() {
        super.tick();

        // 服务端更新Millenaire目标系统
        if (!this.level().isClientSide()) {
            // 更新目标管理器
            goalManager.tick();

            // 如果有目标目的地，更新导航
            updateNavigationFromGoal();
        }

        // 更新动画状态
        updateAnimationState();
        animationTicks++;
    }

    /**
     * 根据当前目标更新导航
     */
    private void updateNavigationFromGoal() {
        if (!goalManager.hasGoal()) {
            return;
        }

        BlockPos destination = goalManager.getDestination();
        if (destination == null) {
            return;
        }

        // 如果导航没有路径或目标改变，重新设置导航
        if (!this.getNavigation().isInProgress()) {
            this.getNavigation().moveTo(
                destination.getX() + 0.5,
                destination.getY(),
                destination.getZ() + 0.5,
                1.0
            );
        }
    }

    // ================ 死亡处理 ================

    @Override
    public void die(DamageSource damageSource) {
        // 掉落所有库存物品
        dropAllInventory();

        // 掉落持有物品
        if (!heldItem.isEmpty()) {
            this.spawnAtLocation(heldItem);
            heldItem = ItemStack.EMPTY;
        }
        if (!heldItemOffHand.isEmpty()) {
            this.spawnAtLocation(heldItemOffHand);
            heldItemOffHand = ItemStack.EMPTY;
        }

        super.die(damageSource);
    }

    // ================ 生成 ================

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                         MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);

        // 随机分配性别
        this.setGender(this.random.nextBoolean() ? GENDER_MALE : GENDER_FEMALE);

        // 生成名字
        this.generateName();

        return spawnGroupData;
    }

    /**
     * 生成随机名字
     */
    private void generateName() {
        Culture culture = getCulture();
        int gender = getGender();

        // TODO: 从文化名字库中选择名字
        // 临时实现
        if (gender == GENDER_MALE) {
            setFirstName("John");
        } else {
            setFirstName("Jane");
        }
        setFamilyName("Smith");
    }

    // ================ 交互 ================

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide()) {
            // 打开村民交互GUI
            player.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new VillagerInteractionMenu(containerId, playerInventory, this),
                Component.translatable("gui.millenaire_rewrite.villager_interaction")
            ), buf -> buf.writeInt(this.getId()));
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide());
    }

    // ================ 保存/加载 ================

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        tag.putLong("VillagerId", villagerId);
        tag.putString("Culture", getCultureId());
        tag.putString("Profession", getProfessionId());
        tag.putInt("Gender", getGender());
        tag.putString("FirstName", getFirstName());
        tag.putString("FamilyName", getFamilyName());
        tag.putBoolean("IsChild", isChild());

        if (housePos != null) {
            tag.putInt("HouseX", housePos.getX());
            tag.putInt("HouseY", housePos.getY());
            tag.putInt("HouseZ", housePos.getZ());
        }

        if (townHallPos != null) {
            tag.putInt("TownHallX", townHallPos.getX());
            tag.putInt("TownHallY", townHallPos.getY());
            tag.putInt("TownHallZ", townHallPos.getZ());
        }

        if (hiredBy != null) {
            tag.putString("HiredBy", hiredBy);
            tag.putLong("HiredUntil", hiredUntil);
        }

        tag.putBoolean("AggressiveStance", aggressiveStance);
        tag.putString("ClothName", clothName);
        tag.putFloat("VillagerScale", villagerScale);
        tag.putString("CurrentGoalKey", currentGoalKey);
        tag.putLong("GoalStarted", goalStarted);

        // 保存库存
        ListTag inventoryTag = new ListTag();
        for (Map.Entry<InvItem, Integer> entry : inventory.entrySet()) {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putString("Item", entry.getKey().getRegistryName().toString());
            itemTag.putInt("Meta", entry.getKey().getMeta());
            itemTag.putInt("Count", entry.getValue());
            inventoryTag.add(itemTag);
        }
        tag.put("Inventory", inventoryTag);

        // 保存持有物品
        if (!heldItem.isEmpty()) {
            tag.put("HeldItem", heldItem.save(this.registryAccess()));
        }
        if (!heldItemOffHand.isEmpty()) {
            tag.put("HeldItemOffHand", heldItemOffHand.save(this.registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        villagerId = tag.getLong("VillagerId");
        setCultureId(tag.getString("Culture"));
        setProfessionId(tag.getString("Profession"));
        setGender(tag.getInt("Gender"));
        setFirstName(tag.getString("FirstName"));
        setFamilyName(tag.getString("FamilyName"));
        setChild(tag.getBoolean("IsChild"));

        if (tag.contains("HouseX")) {
            housePos = new BlockPos(
                tag.getInt("HouseX"),
                tag.getInt("HouseY"),
                tag.getInt("HouseZ"));
        }

        if (tag.contains("TownHallX")) {
            townHallPos = new BlockPos(
                tag.getInt("TownHallX"),
                tag.getInt("TownHallY"),
                tag.getInt("TownHallZ"));
        }

        if (tag.contains("HiredBy")) {
            hiredBy = tag.getString("HiredBy");
            hiredUntil = tag.getLong("HiredUntil");
        }

        aggressiveStance = tag.getBoolean("AggressiveStance");
        clothName = tag.getString("ClothName");
        villagerScale = tag.getFloat("VillagerScale");
        currentGoalKey = tag.getString("CurrentGoalKey");
        goalStarted = tag.getLong("GoalStarted");

        // 加载库存
        inventory.clear();
        if (tag.contains("Inventory")) {
            ListTag inventoryTag = tag.getList("Inventory", 10); // 10 = CompoundTag
            for (int i = 0; i < inventoryTag.size(); i++) {
                CompoundTag itemTag = inventoryTag.getCompound(i);
                String itemId = itemTag.getString("Item");
                int meta = itemTag.getInt("Meta");
                int count = itemTag.getInt("Count");

                ResourceLocation loc = ResourceLocation.tryParse(itemId);
                if (loc != null) {
                    Item item = BuiltInRegistries.ITEM.get(loc);
                    if (item != null) {
                        InvItem invItem = InvItem.create(item, meta);
                        if (invItem != null && count > 0) {
                            inventory.put(invItem, count);
                        }
                    }
                }
            }
        }

        // 加载持有物品
        if (tag.contains("HeldItem")) {
            heldItem = ItemStack.parse(this.registryAccess(), tag.getCompound("HeldItem")).orElse(ItemStack.EMPTY);
        }
        if (tag.contains("HeldItemOffHand")) {
            heldItemOffHand = ItemStack.parse(this.registryAccess(), tag.getCompound("HeldItemOffHand")).orElse(ItemStack.EMPTY);
        }
    }

    // ================ Getters/Setters ================

    public long getVillagerId() {
        return villagerId;
    }

    public String getCultureId() {
        return this.entityData.get(DATA_CULTURE);
    }

    public void setCultureId(String cultureId) {
        this.entityData.set(DATA_CULTURE, cultureId);
    }

    public Culture getCulture() {
        return Culture.fromId(getCultureId());
    }

    public void setCulture(Culture culture) {
        setCultureId(culture.getId());
    }

    public String getProfessionId() {
        return this.entityData.get(DATA_PROFESSION);
    }

    public void setProfessionId(String professionId) {
        String oldProfession = getProfessionId();
        this.entityData.set(DATA_PROFESSION, professionId);

        // 如果职业变化，刷新工作目标
        if (!oldProfession.equals(professionId)) {
            refreshWorkGoals();
        }
    }

    public VillagerProfession getProfession() {
        return VillagerProfession.fromId(getProfessionId());
    }

    public void setProfession(VillagerProfession profession) {
        setProfessionId(profession.getId());
    }

    public int getGender() {
        return this.entityData.get(DATA_GENDER);
    }

    public void setGender(int gender) {
        this.entityData.set(DATA_GENDER, gender);
    }

    public boolean isMale() {
        return getGender() == GENDER_MALE;
    }

    public boolean isFemale() {
        return getGender() == GENDER_FEMALE;
    }

    public String getFirstName() {
        return this.entityData.get(DATA_FIRST_NAME);
    }

    public void setFirstName(String firstName) {
        this.entityData.set(DATA_FIRST_NAME, firstName);
    }

    public String getFamilyName() {
        return this.entityData.get(DATA_FAMILY_NAME);
    }

    public void setFamilyName(String familyName) {
        this.entityData.set(DATA_FAMILY_NAME, familyName);
    }

    public String getFullName() {
        return getFirstName() + " " + getFamilyName();
    }

    public boolean isChild() {
        return this.entityData.get(DATA_IS_CHILD);
    }

    public void setChild(boolean isChild) {
        this.entityData.set(DATA_IS_CHILD, isChild);
    }

    @Nullable
    public BlockPos getHousePos() {
        return housePos;
    }

    public void setHousePos(@Nullable BlockPos pos) {
        this.housePos = pos;
    }

    @Nullable
    public BlockPos getTownHallPos() {
        return townHallPos;
    }

    public void setTownHallPos(@Nullable BlockPos pos) {
        this.townHallPos = pos;
    }

    public boolean isHired() {
        return hiredBy != null && hiredUntil > System.currentTimeMillis();
    }

    @Nullable
    public String getHiredBy() {
        return hiredBy;
    }

    public void setHired(@Nullable String playerName, long durationMs) {
        this.hiredBy = playerName;
        this.hiredUntil = playerName != null ? System.currentTimeMillis() + durationMs : 0;
    }

    public boolean isAggressiveStance() {
        return aggressiveStance;
    }

    public void setAggressiveStance(boolean aggressive) {
        this.aggressiveStance = aggressive;
    }

    public float getVillagerScale() {
        return villagerScale;
    }

    public void setVillagerScale(float scale) {
        this.villagerScale = scale;
    }

    // ================ AI目标方法 ================

    /**
     * 获取目标管理器
     */
    public GoalManager getGoalManager() {
        return goalManager;
    }

    /**
     * 获取当前目标键
     */
    public String getCurrentGoalKey() {
        return currentGoalKey;
    }

    /**
     * 设置当前目标键
     */
    public void setCurrentGoalKey(String key) {
        this.currentGoalKey = key != null ? key : "";
    }

    /**
     * 获取目标开始时间
     */
    public long getGoalStarted() {
        return goalStarted;
    }

    /**
     * 设置目标开始时间
     */
    public void setGoalStarted(long time) {
        this.goalStarted = time;
    }

    // ================ 衣服纹理方法 ================

    /**
     * 获取衣服纹理名称
     */
    public String getClothName() {
        return clothName;
    }

    /**
     * 设置衣服纹理名称
     */
    public void setClothName(String clothName) {
        this.clothName = clothName;
        updateClothTextures();
    }

    /**
     * 获取指定层的衣服纹理
     * @param layer 层索引（0或1）
     */
    @Nullable
    public ResourceLocation getClothTexture(int layer) {
        if (layer < 0 || layer >= clothTextures.length) {
            return null;
        }
        return clothTextures[layer];
    }

    /**
     * 设置指定层的衣服纹理
     */
    public void setClothTexture(int layer, @Nullable ResourceLocation texture) {
        if (layer >= 0 && layer < clothTextures.length) {
            clothTextures[layer] = texture;
        }
    }

    /**
     * 更新衣服纹理路径
     * 基于当前文化、职业、性别和clothName选择纹理
     */
    public void updateClothTextures() {
        // 如果没有设置clothName，使用默认纹理或清除
        if (clothName == null || clothName.isEmpty()) {
            clothTextures[0] = null;
            clothTextures[1] = null;
            return;
        }

        // 构建纹理路径
        // 格式: textures/entity/{culture}/{gender}/clothes/{clothName}_{layer}.png
        Culture culture = getCulture();
        String gender = isMale() ? "male" : "female";

        for (int layer = 0; layer < 2; layer++) {
            String path = "textures/entity/" + culture.getId() + "/" + gender +
                         "/clothes/" + clothName + "_" + layer + ".png";

            // 创建资源位置（实际纹理存在性检查需要在客户端进行）
            clothTextures[layer] = ResourceLocation.fromNamespaceAndPath(
                MillenaireRewrite.MOD_ID, path);
        }
    }

    /**
     * 检查是否有衣服纹理
     */
    public boolean hasClothTexture() {
        return clothTextures[0] != null || clothTextures[1] != null;
    }

    // ================ 动画状态方法 ================

    /**
     * 获取当前动画状态
     */
    public VillagerAnimationState getAnimationState() {
        return animationState;
    }

    /**
     * 设置动画状态
     */
    public void setAnimationState(VillagerAnimationState state) {
        if (state != this.animationState) {
            this.animationState = state;
            this.animationTicks = 0; // 重置动画计时器
        }
    }

    /**
     * 获取动画计时器
     */
    public int getAnimationTicks() {
        return animationTicks;
    }

    /**
     * 自动更新动画状态（基于当前行为）
     */
    private void updateAnimationState() {
        // 基于移动速度确定动画
        double speed = this.getDeltaMovement().horizontalDistance();

        if (this.isDeadOrDying()) {
            setAnimationState(VillagerAnimationState.DYING);
        } else if (this.isSleeping()) {
            setAnimationState(VillagerAnimationState.SLEEPING);
        } else if (this.hurtTime > 0) {
            setAnimationState(VillagerAnimationState.HURT);
        } else if (this.isAggressive() || aggressiveStance) {
            setAnimationState(VillagerAnimationState.ATTACKING);
        } else if (speed > 0.15) {
            setAnimationState(VillagerAnimationState.RUNNING);
        } else if (speed > 0.01) {
            setAnimationState(VillagerAnimationState.WALKING);
        } else {
            // 根据职业和当前目标决定工作动画或空闲
            if (!currentGoalKey.isEmpty() && isWorkGoal(currentGoalKey)) {
                setAnimationState(getWorkAnimationForProfession());
            } else {
                setAnimationState(VillagerAnimationState.IDLE);
            }
        }
    }

    /**
     * 检查目标是否为工作目标
     */
    private boolean isWorkGoal(String goalKey) {
        return goalKey.contains("work") || goalKey.contains("farm") ||
               goalKey.contains("mine") || goalKey.contains("chop") ||
               goalKey.contains("build");
    }

    /**
     * 根据职业获取工作动画
     */
    private VillagerAnimationState getWorkAnimationForProfession() {
        VillagerProfession profession = getProfession();

        return switch (profession) {
            case FARMER, SILK_FARMER -> VillagerAnimationState.FARMING;
            case MINER -> VillagerAnimationState.MINING;
            case LUMBERJACK -> VillagerAnimationState.CHOPPING;
            default -> VillagerAnimationState.WORKING;
        };
    }

    // ================ 库存方法 ================

    /**
     * 添加物品到库存
     */
    public void addToInventory(Item item, int count) {
        addToInventory(InvItem.create(item), count);
    }

    /**
     * 添加方块到库存
     */
    public void addToInventory(Block block, int count) {
        addToInventory(InvItem.create(block), count);
    }

    /**
     * 添加ItemStack到库存
     */
    public void addToInventory(ItemStack stack, int count) {
        if (stack == null || stack.isEmpty()) return;
        addToInventory(InvItem.create(stack), count);
    }

    /**
     * 添加InvItem到库存
     */
    public void addToInventory(InvItem item, int count) {
        if (item == null || count <= 0) return;

        int current = inventory.getOrDefault(item, 0);
        inventory.put(item, current + count);
    }

    /**
     * 从库存取出物品
     * @return 实际取出的数量
     */
    public int takeFromInventory(Item item, int count) {
        return takeFromInventory(InvItem.create(item), count);
    }

    /**
     * 从库存取出方块
     * @return 实际取出的数量
     */
    public int takeFromInventory(Block block, int count) {
        return takeFromInventory(InvItem.create(block), count);
    }

    /**
     * 从库存取出ItemStack
     * @return 实际取出的数量
     */
    public int takeFromInventory(ItemStack stack, int count) {
        if (stack == null || stack.isEmpty()) return 0;
        return takeFromInventory(InvItem.create(stack), count);
    }

    /**
     * 从库存取出InvItem
     * @return 实际取出的数量
     */
    public int takeFromInventory(InvItem item, int count) {
        if (item == null || count <= 0) return 0;

        int current = inventory.getOrDefault(item, 0);
        if (current <= 0) return 0;

        int taken = Math.min(count, current);
        int remaining = current - taken;

        if (remaining <= 0) {
            inventory.remove(item);
        } else {
            inventory.put(item, remaining);
        }

        return taken;
    }

    /**
     * 获取库存中物品数量
     */
    public int countInInventory(Item item) {
        return countInInventory(InvItem.create(item));
    }

    /**
     * 获取库存中方块数量
     */
    public int countInInventory(Block block) {
        return countInInventory(InvItem.create(block));
    }

    /**
     * 获取库存中ItemStack数量
     */
    public int countInInventory(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        return countInInventory(InvItem.create(stack));
    }

    /**
     * 获取库存中InvItem数量
     * 支持通配符匹配（meta = -1）
     */
    public int countInInventory(InvItem item) {
        if (item == null) return 0;

        // 通配符匹配：遍历所有匹配项
        if (item.isWildcard()) {
            int total = 0;
            for (Map.Entry<InvItem, Integer> entry : inventory.entrySet()) {
                if (item.matches(entry.getKey())) {
                    total += entry.getValue();
                }
            }
            return total;
        }

        return inventory.getOrDefault(item, 0);
    }

    /**
     * 设置库存中物品数量（直接覆盖）
     */
    public void setInInventory(InvItem item, int count) {
        if (item == null) return;

        if (count <= 0) {
            inventory.remove(item);
        } else {
            inventory.put(item, count);
        }
    }

    /**
     * 检查库存是否有指定物品
     */
    public boolean hasInInventory(InvItem item) {
        return countInInventory(item) > 0;
    }

    /**
     * 检查库存是否有足够数量的物品
     */
    public boolean hasInInventory(InvItem item, int minCount) {
        return countInInventory(item) >= minCount;
    }

    /**
     * 获取库存所有物品键
     */
    public java.util.Set<InvItem> getInventoryKeys() {
        return inventory.keySet();
    }

    /**
     * 获取库存副本
     */
    public Map<InvItem, Integer> getInventoryCopy() {
        return new HashMap<>(inventory);
    }

    /**
     * 清空库存
     */
    public void clearInventory() {
        inventory.clear();
    }

    /**
     * 检查库存是否为空
     */
    public boolean isInventoryEmpty() {
        return inventory.isEmpty();
    }

    /**
     * 获取库存中不同物品的数量
     */
    public int getInventorySize() {
        return inventory.size();
    }

    /**
     * 掉落所有库存物品（死亡时调用）
     */
    public void dropAllInventory() {
        if (this.level().isClientSide()) return;

        for (Map.Entry<InvItem, Integer> entry : inventory.entrySet()) {
            InvItem item = entry.getKey();
            int count = entry.getValue();

            while (count > 0) {
                int stackSize = Math.min(count, item.getItem().getDefaultMaxStackSize());
                ItemStack stack = item.toItemStack(stackSize);
                this.spawnAtLocation(stack);
                count -= stackSize;
            }
        }
        inventory.clear();
    }

    // ================ 持有物品方法 ================

    /**
     * 获取主手持有物品
     */
    public ItemStack getHeldItem() {
        return heldItem;
    }

    /**
     * 设置主手持有物品
     */
    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * 获取副手持有物品
     */
    public ItemStack getHeldItemOffHand() {
        return heldItemOffHand;
    }

    /**
     * 设置副手持有物品
     */
    public void setHeldItemOffHand(ItemStack stack) {
        this.heldItemOffHand = stack != null ? stack : ItemStack.EMPTY;
    }

    /**
     * 更新持有物品（从物品数组轮换）
     * 由tick方法调用
     */
    public void updateHeldItems(ItemStack[] mainHandItems, ItemStack[] offHandItems) {
        heldItemCount++;

        // 每20 tick轮换一次
        if (heldItemCount > 20) {
            heldItemCount = 0;

            // 轮换主手
            if (mainHandItems != null && mainHandItems.length > 0) {
                heldItemIndex = (heldItemIndex + 1) % mainHandItems.length;
                heldItem = mainHandItems[heldItemIndex];
            }

            // 轮换副手
            if (offHandItems != null && offHandItems.length > 0) {
                heldItemOffHandIndex = (heldItemOffHandIndex + 1) % offHandItems.length;
                heldItemOffHand = offHandItems[heldItemOffHandIndex];
            }
        }
    }

    /**
     * 重置持有物品状态（目标改变时调用）
     */
    public void resetHeldItems() {
        heldItemCount = 21; // 触发立即轮换
        heldItemIndex = -1;
        heldItemOffHandIndex = -1;
    }

    // ================ 装备槽位方法（用于渲染） ================

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> heldItem;
            case OFFHAND -> heldItemOffHand;
            default -> super.getItemBySlot(slot);
        };
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        switch (slot) {
            case MAINHAND -> heldItem = stack;
            case OFFHAND -> heldItemOffHand = stack;
            default -> super.setItemSlot(slot, stack);
        }
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return java.util.List.of(heldItem, heldItemOffHand);
    }

    // ================ 显示名称 ================

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        String name = getFirstName();
        if (!name.isEmpty()) {
            return net.minecraft.network.chat.Component.literal(name);
        }
        return super.getDisplayName();
    }
}
