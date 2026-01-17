package com.jasoncian.millenaire_rewrite.village;

import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.entity.villager.VillagerProfession;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * 村民记录 - 持久化存储村民数据
 *
 * 基于OldSource VillagerRecord.java
 * 用于：
 * - 保存离线村民数据
 * - 追踪村民历史（家庭关系、出生、死亡）
 * - 恢复村民状态
 *
 * @author Based on OldSource VillagerRecord
 * @version 1.0.0
 */
public class VillagerRecord {

    // ================ 身份信息 ================

    /** 村民唯一ID */
    private long villagerId;

    /** 名 */
    private String firstName = "";

    /** 姓 */
    private String familyName = "";

    /** 性别 */
    private int gender = MillVillager.GENDER_MALE;

    /** 是否是儿童 */
    private boolean isChild = false;

    /** 文化ID */
    private String cultureId = Culture.NORMAN.getId();

    /** 职业ID */
    private String professionId = VillagerProfession.FARMER.getId();

    // ================ 位置信息 ================

    /** 家的位置 */
    @Nullable
    private BlockPos housePos;

    /** 所属市政厅位置 */
    @Nullable
    private BlockPos townHallPos;

    /** 最后已知位置 */
    @Nullable
    private BlockPos lastKnownPos;

    // ================ 家庭关系 ================

    /** 配偶ID（-1表示无） */
    private long spouseId = -1;

    /** 父亲ID（-1表示无/未知） */
    private long fatherId = -1;

    /** 母亲ID（-1表示无/未知） */
    private long motherId = -1;

    // ================ 状态 ================

    /** 是否存活 */
    private boolean alive = true;

    /** 出生时间 */
    private long birthTime = 0;

    /** 死亡时间（0表示未死亡） */
    private long deathTime = 0;

    /** 死亡原因 */
    private String deathCause = "";

    // ================ 属性 ================

    /** 最大生命值 */
    private float maxHealth = 20.0f;

    /** 当前生命值（上次保存时） */
    private float health = 20.0f;

    /** 声望/经验 */
    private int experience = 0;

    // ================ 构造函数 ================

    public VillagerRecord(MillVillager villager) {
        this.villagerId = villager.getVillagerId();
        updateFromVillager(villager);
    }

    /**
     * 从NBT恢复的构造函数
     */
    private VillagerRecord() {
    }

    // ================ 同步方法 ================

    /**
     * 从活跃村民更新记录
     */
    public void updateFromVillager(MillVillager villager) {
        this.firstName = villager.getFirstName();
        this.familyName = villager.getFamilyName();
        this.gender = villager.getGender();
        this.isChild = villager.isChild();
        this.cultureId = villager.getCultureId();
        this.professionId = villager.getProfessionId();
        this.housePos = villager.getHousePos();
        this.townHallPos = villager.getTownHallPos();
        this.lastKnownPos = villager.blockPosition();
        this.health = villager.getHealth();
        this.maxHealth = villager.getMaxHealth();
        this.alive = villager.isAlive();
    }

    /**
     * 将记录应用到村民实体
     */
    public void applyToVillager(MillVillager villager) {
        villager.setFirstName(firstName);
        villager.setFamilyName(familyName);
        villager.setGender(gender);
        villager.setChild(isChild);
        villager.setCultureId(cultureId);
        villager.setProfessionId(professionId);
        villager.setHousePos(housePos);
        villager.setTownHallPos(townHallPos);
    }

    /**
     * 标记村民死亡
     */
    public void markDead(String cause) {
        this.alive = false;
        this.deathTime = System.currentTimeMillis();
        this.deathCause = cause;
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // 身份信息
        tag.putLong("VillagerId", villagerId);
        tag.putString("FirstName", firstName);
        tag.putString("FamilyName", familyName);
        tag.putInt("Gender", gender);
        tag.putBoolean("IsChild", isChild);
        tag.putString("CultureId", cultureId);
        tag.putString("ProfessionId", professionId);

        // 位置信息
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
        if (lastKnownPos != null) {
            tag.putInt("LastX", lastKnownPos.getX());
            tag.putInt("LastY", lastKnownPos.getY());
            tag.putInt("LastZ", lastKnownPos.getZ());
        }

        // 家庭关系
        tag.putLong("SpouseId", spouseId);
        tag.putLong("FatherId", fatherId);
        tag.putLong("MotherId", motherId);

        // 状态
        tag.putBoolean("Alive", alive);
        tag.putLong("BirthTime", birthTime);
        tag.putLong("DeathTime", deathTime);
        tag.putString("DeathCause", deathCause);

        // 属性
        tag.putFloat("MaxHealth", maxHealth);
        tag.putFloat("Health", health);
        tag.putInt("Experience", experience);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static VillagerRecord load(CompoundTag tag) {
        VillagerRecord record = new VillagerRecord();

        // 身份信息
        record.villagerId = tag.getLong("VillagerId");
        record.firstName = tag.getString("FirstName");
        record.familyName = tag.getString("FamilyName");
        record.gender = tag.getInt("Gender");
        record.isChild = tag.getBoolean("IsChild");
        record.cultureId = tag.getString("CultureId");
        record.professionId = tag.getString("ProfessionId");

        // 位置信息
        if (tag.contains("HouseX")) {
            record.housePos = new BlockPos(
                tag.getInt("HouseX"),
                tag.getInt("HouseY"),
                tag.getInt("HouseZ"));
        }
        if (tag.contains("TownHallX")) {
            record.townHallPos = new BlockPos(
                tag.getInt("TownHallX"),
                tag.getInt("TownHallY"),
                tag.getInt("TownHallZ"));
        }
        if (tag.contains("LastX")) {
            record.lastKnownPos = new BlockPos(
                tag.getInt("LastX"),
                tag.getInt("LastY"),
                tag.getInt("LastZ"));
        }

        // 家庭关系
        record.spouseId = tag.getLong("SpouseId");
        record.fatherId = tag.getLong("FatherId");
        record.motherId = tag.getLong("MotherId");

        // 状态
        record.alive = tag.getBoolean("Alive");
        record.birthTime = tag.getLong("BirthTime");
        record.deathTime = tag.getLong("DeathTime");
        record.deathCause = tag.getString("DeathCause");

        // 属性
        record.maxHealth = tag.getFloat("MaxHealth");
        record.health = tag.getFloat("Health");
        record.experience = tag.getInt("Experience");

        return record;
    }

    // ================ Getters/Setters ================

    public long getVillagerId() {
        return villagerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFullName() {
        return firstName + " " + familyName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public boolean isMale() {
        return gender == MillVillager.GENDER_MALE;
    }

    public boolean isFemale() {
        return gender == MillVillager.GENDER_FEMALE;
    }

    public boolean isChild() {
        return isChild;
    }

    public void setChild(boolean child) {
        isChild = child;
    }

    public String getCultureId() {
        return cultureId;
    }

    public void setCultureId(String cultureId) {
        this.cultureId = cultureId;
    }

    public String getProfessionId() {
        return professionId;
    }

    public void setProfessionId(String professionId) {
        this.professionId = professionId;
    }

    @Nullable
    public BlockPos getHousePos() {
        return housePos;
    }

    public void setHousePos(@Nullable BlockPos housePos) {
        this.housePos = housePos;
    }

    @Nullable
    public BlockPos getTownHallPos() {
        return townHallPos;
    }

    public void setTownHallPos(@Nullable BlockPos townHallPos) {
        this.townHallPos = townHallPos;
    }

    @Nullable
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    public void setLastKnownPos(@Nullable BlockPos lastKnownPos) {
        this.lastKnownPos = lastKnownPos;
    }

    public long getSpouseId() {
        return spouseId;
    }

    public void setSpouseId(long spouseId) {
        this.spouseId = spouseId;
    }

    public boolean hasSpouse() {
        return spouseId >= 0;
    }

    public long getFatherId() {
        return fatherId;
    }

    public void setFatherId(long fatherId) {
        this.fatherId = fatherId;
    }

    public long getMotherId() {
        return motherId;
    }

    public void setMotherId(long motherId) {
        this.motherId = motherId;
    }

    public boolean isAlive() {
        return alive;
    }

    public long getBirthTime() {
        return birthTime;
    }

    public void setBirthTime(long birthTime) {
        this.birthTime = birthTime;
    }

    public long getDeathTime() {
        return deathTime;
    }

    public String getDeathCause() {
        return deathCause;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public void addExperience(int amount) {
        this.experience += amount;
    }

    @Override
    public String toString() {
        return "VillagerRecord{" +
            "id=" + villagerId +
            ", name='" + getFullName() + '\'' +
            ", profession=" + professionId +
            ", alive=" + alive +
            '}';
    }
}
