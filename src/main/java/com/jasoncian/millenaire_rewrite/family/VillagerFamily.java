package com.jasoncian.millenaire_rewrite.family;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 村民家庭数据 - 存储单个村民的所有家庭关系
 *
 * 包含：
 * - 所有亲属关系
 * - 婚姻状态
 * - 子女列表
 *
 * @author Based on OldSource family data
 * @version 1.0.0
 */
public class VillagerFamily {

    // ================ 婚姻状态枚举 ================

    public enum MaritalStatus {
        /** 单身 */
        SINGLE("single", "Single"),
        /** 已订婚 */
        ENGAGED("engaged", "Engaged"),
        /** 已婚 */
        MARRIED("married", "Married"),
        /** 丧偶 */
        WIDOWED("widowed", "Widowed");

        private final String id;
        private final String displayName;

        MaritalStatus(String id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static MaritalStatus fromId(String id) {
            for (MaritalStatus status : values()) {
                if (status.id.equals(id)) {
                    return status;
                }
            }
            return SINGLE;
        }
    }

    // ================ 数据 ================

    /** 村民ID */
    private final long villagerId;

    /** 村民性别（true为男性） */
    private boolean male;

    /** 婚姻状态 */
    private MaritalStatus maritalStatus = MaritalStatus.SINGLE;

    /** 配偶ID */
    @Nullable
    private Long spouseId;

    /** 订婚时间 */
    private long engagementTime = 0;

    /** 结婚时间 */
    private long marriageTime = 0;

    /** 父亲ID */
    @Nullable
    private Long fatherId;

    /** 母亲ID */
    @Nullable
    private Long motherId;

    /** 子女ID列表 */
    private final List<Long> childrenIds = new ArrayList<>();

    /** 所有亲属关系（村民ID -> 关系） */
    private final Map<Long, FamilyRelation> relatives = new HashMap<>();

    /** 家族姓氏 */
    private String familyName = "";

    // ================ 构造函数 ================

    public VillagerFamily(long villagerId, boolean male) {
        this.villagerId = villagerId;
        this.male = male;
    }

    // ================ 婚姻管理 ================

    /**
     * 订婚
     */
    public boolean engage(long partnerId, long currentTime) {
        if (maritalStatus != MaritalStatus.SINGLE && maritalStatus != MaritalStatus.WIDOWED) {
            return false;
        }

        this.spouseId = partnerId;
        this.maritalStatus = MaritalStatus.ENGAGED;
        this.engagementTime = currentTime;
        addRelative(partnerId, FamilyRelation.FIANCE);

        return true;
    }

    /**
     * 结婚
     */
    public boolean marry(long partnerId, long currentTime) {
        if (maritalStatus != MaritalStatus.ENGAGED || partnerId != spouseId) {
            // 也允许直接结婚（跳过订婚）
            if (maritalStatus != MaritalStatus.SINGLE && maritalStatus != MaritalStatus.WIDOWED) {
                return false;
            }
        }

        this.spouseId = partnerId;
        this.maritalStatus = MaritalStatus.MARRIED;
        this.marriageTime = currentTime;
        addRelative(partnerId, FamilyRelation.SPOUSE);

        return true;
    }

    /**
     * 丧偶
     */
    public void becomeWidowed() {
        if (maritalStatus == MaritalStatus.MARRIED) {
            maritalStatus = MaritalStatus.WIDOWED;
            if (spouseId != null) {
                relatives.remove(spouseId);
            }
            spouseId = null;
        }
    }

    /**
     * 取消订婚
     */
    public void cancelEngagement() {
        if (maritalStatus == MaritalStatus.ENGAGED && spouseId != null) {
            relatives.remove(spouseId);
            spouseId = null;
            maritalStatus = MaritalStatus.SINGLE;
            engagementTime = 0;
        }
    }

    /**
     * 检查是否可以与指定村民结婚
     */
    public boolean canMarry(VillagerFamily other) {
        // 检查婚姻状态
        if (maritalStatus == MaritalStatus.MARRIED || other.maritalStatus == MaritalStatus.MARRIED) {
            return false;
        }

        // 检查是否为同性（如果需要限制）
        // 目前允许同性婚姻

        // 检查是否为近亲
        FamilyRelation relation = getRelationTo(other.villagerId);
        if (relation.prohibitsMarriage()) {
            return false;
        }

        return true;
    }

    // ================ 亲属管理 ================

    /**
     * 添加亲属关系
     */
    public void addRelative(long relativeId, FamilyRelation relation) {
        relatives.put(relativeId, relation);
    }

    /**
     * 移除亲属关系
     */
    public void removeRelative(long relativeId) {
        relatives.remove(relativeId);
    }

    /**
     * 获取与指定村民的关系
     */
    public FamilyRelation getRelationTo(long relativeId) {
        return relatives.getOrDefault(relativeId, FamilyRelation.NONE);
    }

    /**
     * 检查是否有亲属关系
     */
    public boolean isRelatedTo(long relativeId) {
        return relatives.containsKey(relativeId) && relatives.get(relativeId) != FamilyRelation.NONE;
    }

    /**
     * 设置父母
     */
    public void setParents(@Nullable Long fatherId, @Nullable Long motherId) {
        this.fatherId = fatherId;
        this.motherId = motherId;

        if (fatherId != null) {
            addRelative(fatherId, FamilyRelation.FATHER);
        }
        if (motherId != null) {
            addRelative(motherId, FamilyRelation.MOTHER);
        }
    }

    /**
     * 添加子女
     */
    public void addChild(long childId, boolean childIsMale) {
        if (!childrenIds.contains(childId)) {
            childrenIds.add(childId);
            addRelative(childId, childIsMale ? FamilyRelation.SON : FamilyRelation.DAUGHTER);
        }
    }

    /**
     * 添加兄弟姐妹
     */
    public void addSibling(long siblingId, boolean siblingIsMale) {
        addRelative(siblingId, siblingIsMale ? FamilyRelation.BROTHER : FamilyRelation.SISTER);
    }

    /**
     * 获取所有指定关系的亲属
     */
    public List<Long> getRelativesOfType(FamilyRelation relation) {
        List<Long> result = new ArrayList<>();
        for (Map.Entry<Long, FamilyRelation> entry : relatives.entrySet()) {
            if (entry.getValue() == relation) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * 获取所有亲属
     */
    public Map<Long, FamilyRelation> getAllRelatives() {
        return Collections.unmodifiableMap(relatives);
    }

    // ================ 姻亲关系 ================

    /**
     * 建立姻亲关系（结婚时调用）
     */
    public void establishInLawRelations(VillagerFamily spouse) {
        // 配偶的父母成为岳父母/公婆
        if (spouse.fatherId != null) {
            addRelative(spouse.fatherId, FamilyRelation.FATHER_IN_LAW);
        }
        if (spouse.motherId != null) {
            addRelative(spouse.motherId, FamilyRelation.MOTHER_IN_LAW);
        }

        // 配偶的兄弟姐妹成为姻亲
        for (Long siblingId : spouse.getRelativesOfType(FamilyRelation.BROTHER)) {
            addRelative(siblingId, FamilyRelation.BROTHER_IN_LAW);
        }
        for (Long siblingId : spouse.getRelativesOfType(FamilyRelation.SISTER)) {
            addRelative(siblingId, FamilyRelation.SISTER_IN_LAW);
        }
    }

    // ================ 家族信息 ================

    /**
     * 获取家族大小（直系亲属数）
     */
    public int getFamilySize() {
        int size = 1; // 自己
        if (spouseId != null && maritalStatus == MaritalStatus.MARRIED) size++;
        size += childrenIds.size();
        return size;
    }

    /**
     * 获取所有直系亲属ID
     */
    public Set<Long> getImmediateFamily() {
        Set<Long> family = new HashSet<>();
        if (spouseId != null) family.add(spouseId);
        if (fatherId != null) family.add(fatherId);
        if (motherId != null) family.add(motherId);
        family.addAll(childrenIds);
        return family;
    }

    /**
     * 检查是否有活着的家人
     */
    public boolean hasLivingFamily() {
        return spouseId != null || !childrenIds.isEmpty() ||
               fatherId != null || motherId != null;
    }

    // ================ NBT序列化 ================

    /**
     * 保存到NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putLong("VillagerId", villagerId);
        tag.putBoolean("Male", male);
        tag.putString("MaritalStatus", maritalStatus.getId());
        tag.putString("FamilyName", familyName);

        if (spouseId != null) {
            tag.putLong("SpouseId", spouseId);
        }
        tag.putLong("EngagementTime", engagementTime);
        tag.putLong("MarriageTime", marriageTime);

        if (fatherId != null) {
            tag.putLong("FatherId", fatherId);
        }
        if (motherId != null) {
            tag.putLong("MotherId", motherId);
        }

        // 保存子女
        ListTag childList = new ListTag();
        for (Long childId : childrenIds) {
            CompoundTag childTag = new CompoundTag();
            childTag.putLong("Id", childId);
            childList.add(childTag);
        }
        tag.put("Children", childList);

        // 保存所有亲属关系
        ListTag relativeList = new ListTag();
        for (Map.Entry<Long, FamilyRelation> entry : relatives.entrySet()) {
            CompoundTag relTag = new CompoundTag();
            relTag.putLong("Id", entry.getKey());
            relTag.putString("Relation", entry.getValue().getId());
            relativeList.add(relTag);
        }
        tag.put("Relatives", relativeList);

        return tag;
    }

    /**
     * 从NBT加载
     */
    public static VillagerFamily fromNbt(CompoundTag tag) {
        long villagerId = tag.getLong("VillagerId");
        boolean male = tag.getBoolean("Male");

        VillagerFamily family = new VillagerFamily(villagerId, male);
        family.maritalStatus = MaritalStatus.fromId(tag.getString("MaritalStatus"));
        family.familyName = tag.getString("FamilyName");

        if (tag.contains("SpouseId")) {
            family.spouseId = tag.getLong("SpouseId");
        }
        family.engagementTime = tag.getLong("EngagementTime");
        family.marriageTime = tag.getLong("MarriageTime");

        if (tag.contains("FatherId")) {
            family.fatherId = tag.getLong("FatherId");
        }
        if (tag.contains("MotherId")) {
            family.motherId = tag.getLong("MotherId");
        }

        // 加载子女
        ListTag childList = tag.getList("Children", 10);
        for (int i = 0; i < childList.size(); i++) {
            family.childrenIds.add(childList.getCompound(i).getLong("Id"));
        }

        // 加载亲属关系
        ListTag relativeList = tag.getList("Relatives", 10);
        for (int i = 0; i < relativeList.size(); i++) {
            CompoundTag relTag = relativeList.getCompound(i);
            long relId = relTag.getLong("Id");
            FamilyRelation relation = FamilyRelation.fromId(relTag.getString("Relation"));
            family.relatives.put(relId, relation);
        }

        return family;
    }

    // ================ Getters/Setters ================

    public long getVillagerId() {
        return villagerId;
    }

    public boolean isMale() {
        return male;
    }

    public void setMale(boolean male) {
        this.male = male;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    @Nullable
    public Long getSpouseId() {
        return spouseId;
    }

    public long getEngagementTime() {
        return engagementTime;
    }

    public long getMarriageTime() {
        return marriageTime;
    }

    @Nullable
    public Long getFatherId() {
        return fatherId;
    }

    @Nullable
    public Long getMotherId() {
        return motherId;
    }

    public List<Long> getChildrenIds() {
        return Collections.unmodifiableList(childrenIds);
    }

    public int getChildCount() {
        return childrenIds.size();
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public boolean isMarried() {
        return maritalStatus == MaritalStatus.MARRIED;
    }

    public boolean isEngaged() {
        return maritalStatus == MaritalStatus.ENGAGED;
    }

    public boolean isSingle() {
        return maritalStatus == MaritalStatus.SINGLE || maritalStatus == MaritalStatus.WIDOWED;
    }

    @Override
    public String toString() {
        return "VillagerFamily{" +
            "villagerId=" + villagerId +
            ", status=" + maritalStatus +
            ", spouse=" + spouseId +
            ", children=" + childrenIds.size() +
            ", relatives=" + relatives.size() +
            '}';
    }
}
