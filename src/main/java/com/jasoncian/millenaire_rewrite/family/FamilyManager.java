package com.jasoncian.millenaire_rewrite.family;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 家庭管理器 - 管理所有村民的家庭关系
 *
 * 功能：
 * - 存储和加载家庭数据
 * - 管理婚姻和订婚
 * - 处理出生和死亡
 * - 生成家族姓氏
 *
 * @author Based on OldSource family management
 * @version 1.0.0
 */
public class FamilyManager extends SavedData {

    // ================ 常量 ================

    private static final String DATA_NAME = MillenaireRewrite.MOD_ID + "_families";

    /** 订婚到结婚的最短时间（tick，约3游戏天） */
    public static final long MIN_ENGAGEMENT_DURATION = 72000;

    /** 最小结婚年龄（游戏tick，约等于18岁） */
    public static final long MIN_MARRIAGE_AGE = 432000; // 6小时真实时间

    /** 最小生育年龄 */
    public static final long MIN_CHILDBEARING_AGE = MIN_MARRIAGE_AGE;

    /** 最大生育年龄 */
    public static final long MAX_CHILDBEARING_AGE = 1728000; // 24小时真实时间

    /** 生育间隔（最少） */
    public static final long MIN_BIRTH_INTERVAL = 144000; // 2小时真实时间

    // ================ 数据 ================

    /** 所有村民家庭数据（村民ID -> 家庭数据） */
    private final Map<Long, VillagerFamily> families = new HashMap<>();

    /** 家族姓氏池（文化 -> 姓氏列表） */
    private final Map<Culture, List<String>> familyNamePools = new EnumMap<>(Culture.class);

    /** 随机数生成器 */
    private final Random random = new Random();

    // ================ 构造函数 ================

    public FamilyManager() {
        initializeFamilyNames();
    }

    /**
     * 初始化家族姓氏池
     */
    private void initializeFamilyNames() {
        // 诺曼姓氏
        familyNamePools.put(Culture.NORMAN, Arrays.asList(
            "Dupont", "Martin", "Bernard", "Dubois", "Thomas",
            "Robert", "Richard", "Petit", "Durand", "Leroy",
            "Moreau", "Simon", "Laurent", "Lefebvre", "Michel"
        ));

        // 日本姓氏
        familyNamePools.put(Culture.JAPANESE, Arrays.asList(
            "Tanaka", "Suzuki", "Yamamoto", "Watanabe", "Sato",
            "Kobayashi", "Takahashi", "Ito", "Nakamura", "Yamada",
            "Matsumoto", "Inoue", "Kimura", "Hayashi", "Shimizu"
        ));

        // 印度姓氏
        familyNamePools.put(Culture.INDIAN, Arrays.asList(
            "Sharma", "Patel", "Singh", "Kumar", "Gupta",
            "Reddy", "Rao", "Nair", "Menon", "Iyer",
            "Verma", "Joshi", "Mehta", "Shah", "Desai"
        ));

        // 玛雅姓氏
        familyNamePools.put(Culture.MAYAN, Arrays.asList(
            "Balam", "Ek", "Kan", "Chac", "Itzamna",
            "Kukulkan", "Ix", "Ah", "Noh", "Yax",
            "Chel", "Pol", "Tun", "Cab", "Mul"
        ));

        // 拜占庭姓氏
        familyNamePools.put(Culture.BYZANTINE, Arrays.asList(
            "Komnenos", "Palaiologos", "Doukas", "Angelos", "Laskaris",
            "Kantakouzenos", "Bryennios", "Phokas", "Skleros", "Botaneiates",
            "Monomachos", "Diogenes", "Strategopoulos", "Tarchaneiotes", "Makrembolites"
        ));

        // 因纽特姓氏
        familyNamePools.put(Culture.INUIT, Arrays.asList(
            "Anaq", "Nanuq", "Tulugaq", "Amarok", "Akiak",
            "Siku", "Qimmiq", "Tiriaq", "Umiak", "Qayaq",
            "Iqaluk", "Ukpik", "Tuktu", "Kiviuq", "Sedna"
        ));

        // 塞尔柱姓氏
        familyNamePools.put(Culture.SELJUK, Arrays.asList(
            "Arslan", "Tugrul", "Alp", "Malik", "Sultan",
            "Nizam", "Kara", "Ak", "Gok", "Demir",
            "Yildiz", "Ay", "Gunes", "Bulut", "Dogan"
        ));
    }

    // ================ SavedData实现 ================

    /**
     * 从NBT加载
     */
    public static FamilyManager load(CompoundTag tag, HolderLookup.Provider registries) {
        FamilyManager manager = new FamilyManager();

        ListTag familyList = tag.getList("Families", 10);
        for (int i = 0; i < familyList.size(); i++) {
            VillagerFamily family = VillagerFamily.fromNbt(familyList.getCompound(i));
            manager.families.put(family.getVillagerId(), family);
        }

        MillenaireRewrite.LOGGER.info("Loaded family data for {} villagers", manager.families.size());

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag familyList = new ListTag();
        for (VillagerFamily family : families.values()) {
            familyList.add(family.save());
        }
        tag.put("Families", familyList);

        return tag;
    }

    // ================ 静态访问 ================

    /**
     * 获取维度的家庭管理器
     */
    public static FamilyManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(FamilyManager::new, FamilyManager::load),
            DATA_NAME
        );
    }

    // ================ 家庭数据访问 ================

    /**
     * 获取或创建村民的家庭数据
     */
    public VillagerFamily getOrCreateFamily(long villagerId, boolean male) {
        return families.computeIfAbsent(villagerId, id -> {
            VillagerFamily family = new VillagerFamily(id, male);
            setDirty();
            return family;
        });
    }

    /**
     * 获取村民的家庭数据
     */
    @Nullable
    public VillagerFamily getFamily(long villagerId) {
        return families.get(villagerId);
    }

    /**
     * 移除村民的家庭数据
     */
    public void removeFamily(long villagerId) {
        families.remove(villagerId);
        setDirty();
    }

    // ================ 婚姻管理 ================

    /**
     * 订婚
     */
    public boolean engage(long villagerA, long villagerB, long currentTime) {
        VillagerFamily familyA = families.get(villagerA);
        VillagerFamily familyB = families.get(villagerB);

        if (familyA == null || familyB == null) {
            return false;
        }

        // 检查是否可以订婚
        if (!familyA.canMarry(familyB)) {
            return false;
        }

        // 执行订婚
        boolean success = familyA.engage(villagerB, currentTime) &&
                          familyB.engage(villagerA, currentTime);

        if (success) {
            setDirty();
            MillenaireRewrite.LOGGER.info("Villagers {} and {} are now engaged", villagerA, villagerB);
        }

        return success;
    }

    /**
     * 结婚
     */
    public boolean marry(long villagerA, long villagerB, long currentTime) {
        VillagerFamily familyA = families.get(villagerA);
        VillagerFamily familyB = families.get(villagerB);

        if (familyA == null || familyB == null) {
            return false;
        }

        // 检查订婚时间（如果已订婚）
        if (familyA.isEngaged() && familyB.isEngaged()) {
            if (currentTime - familyA.getEngagementTime() < MIN_ENGAGEMENT_DURATION) {
                return false; // 订婚时间不够
            }
        } else if (!familyA.canMarry(familyB)) {
            return false;
        }

        // 执行结婚
        boolean success = familyA.marry(villagerB, currentTime) &&
                          familyB.marry(villagerA, currentTime);

        if (success) {
            // 建立姻亲关系
            familyA.establishInLawRelations(familyB);
            familyB.establishInLawRelations(familyA);

            // 统一家族姓氏（传统上跟随男方）
            if (familyA.isMale()) {
                familyB.setFamilyName(familyA.getFamilyName());
            } else {
                familyA.setFamilyName(familyB.getFamilyName());
            }

            setDirty();
            MillenaireRewrite.LOGGER.info("Villagers {} and {} are now married", villagerA, villagerB);
        }

        return success;
    }

    /**
     * 取消订婚
     */
    public void cancelEngagement(long villagerA, long villagerB) {
        VillagerFamily familyA = families.get(villagerA);
        VillagerFamily familyB = families.get(villagerB);

        if (familyA != null) familyA.cancelEngagement();
        if (familyB != null) familyB.cancelEngagement();

        setDirty();
    }

    /**
     * 处理村民死亡
     */
    public void onVillagerDeath(long villagerId) {
        VillagerFamily deceased = families.get(villagerId);
        if (deceased == null) return;

        // 配偶变为丧偶
        Long spouseId = deceased.getSpouseId();
        if (spouseId != null) {
            VillagerFamily spouse = families.get(spouseId);
            if (spouse != null) {
                spouse.becomeWidowed();
            }
        }

        // 更新所有亲属关系
        for (Map.Entry<Long, FamilyRelation> entry : deceased.getAllRelatives().entrySet()) {
            VillagerFamily relative = families.get(entry.getKey());
            if (relative != null) {
                relative.removeRelative(villagerId);
            }
        }

        setDirty();
    }

    // ================ 出生管理 ================

    /**
     * 生育子女
     */
    public VillagerFamily birthChild(long fatherId, long motherId, boolean childIsMale, long childId, long currentTime) {
        VillagerFamily father = families.get(fatherId);
        VillagerFamily mother = families.get(motherId);

        if (father == null || mother == null) {
            return null;
        }

        // 创建子女家庭数据
        VillagerFamily child = new VillagerFamily(childId, childIsMale);
        child.setParents(fatherId, motherId);
        child.setFamilyName(father.getFamilyName()); // 继承父亲姓氏

        // 更新父母数据
        father.addChild(childId, childIsMale);
        mother.addChild(childId, childIsMale);

        // 添加兄弟姐妹关系
        for (Long siblingId : father.getChildrenIds()) {
            if (siblingId != childId) {
                VillagerFamily sibling = families.get(siblingId);
                if (sibling != null) {
                    child.addSibling(siblingId, sibling.isMale());
                    sibling.addSibling(childId, childIsMale);
                }
            }
        }

        // 添加祖父母关系
        if (father.getFatherId() != null) {
            child.addRelative(father.getFatherId(), FamilyRelation.GRANDFATHER);
        }
        if (father.getMotherId() != null) {
            child.addRelative(father.getMotherId(), FamilyRelation.GRANDMOTHER);
        }
        if (mother.getFatherId() != null) {
            child.addRelative(mother.getFatherId(), FamilyRelation.GRANDFATHER);
        }
        if (mother.getMotherId() != null) {
            child.addRelative(mother.getMotherId(), FamilyRelation.GRANDMOTHER);
        }

        // 保存
        families.put(childId, child);
        setDirty();

        MillenaireRewrite.LOGGER.info("Child {} born to parents {} and {}", childId, fatherId, motherId);

        return child;
    }

    /**
     * 检查夫妻是否可以生育
     */
    public boolean canHaveChild(long villagerA, long villagerB, long currentTime) {
        VillagerFamily familyA = families.get(villagerA);
        VillagerFamily familyB = families.get(villagerB);

        if (familyA == null || familyB == null) {
            return false;
        }

        // 必须已婚
        if (!familyA.isMarried() || !familyB.isMarried()) {
            return false;
        }

        // 必须是彼此的配偶
        Long spouseA = familyA.getSpouseId();
        Long spouseB = familyB.getSpouseId();
        if (spouseA == null || spouseB == null || spouseA != villagerB || spouseB != villagerA) {
            return false;
        }

        // 检查生育间隔
        // 这需要追踪上次生育时间（简化处理）

        return true;
    }

    // ================ 姓氏管理 ================

    /**
     * 生成随机家族姓氏
     */
    public String generateFamilyName(Culture culture) {
        List<String> names = familyNamePools.get(culture);
        if (names == null || names.isEmpty()) {
            return "Unknown";
        }
        return names.get(random.nextInt(names.size()));
    }

    /**
     * 为新村民分配姓氏
     */
    public void assignFamilyName(long villagerId, Culture culture) {
        VillagerFamily family = families.get(villagerId);
        if (family != null && family.getFamilyName().isEmpty()) {
            family.setFamilyName(generateFamilyName(culture));
            setDirty();
        }
    }

    // ================ 查询 ================

    /**
     * 查找适合结婚的村民
     */
    public List<Long> findEligiblePartners(long villagerId) {
        VillagerFamily seeker = families.get(villagerId);
        if (seeker == null) {
            return Collections.emptyList();
        }

        List<Long> eligible = new ArrayList<>();
        for (VillagerFamily candidate : families.values()) {
            if (candidate.getVillagerId() == villagerId) continue;
            if (seeker.canMarry(candidate) && candidate.isSingle()) {
                eligible.add(candidate.getVillagerId());
            }
        }
        return eligible;
    }

    /**
     * 获取家族所有成员
     */
    public Set<Long> getFamilyClan(long villagerId) {
        Set<Long> clan = new HashSet<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> toVisit = new LinkedList<>();

        toVisit.add(villagerId);

        while (!toVisit.isEmpty()) {
            Long current = toVisit.poll();
            if (visited.contains(current)) continue;
            visited.add(current);
            clan.add(current);

            VillagerFamily family = families.get(current);
            if (family != null) {
                for (Long relativeId : family.getImmediateFamily()) {
                    if (!visited.contains(relativeId)) {
                        toVisit.add(relativeId);
                    }
                }
            }
        }

        return clan;
    }

    /**
     * 获取两个村民之间的关系
     */
    public FamilyRelation getRelation(long villagerA, long villagerB) {
        VillagerFamily family = families.get(villagerA);
        if (family == null) return FamilyRelation.NONE;
        return family.getRelationTo(villagerB);
    }

    // ================ 统计 ================

    /**
     * 获取已婚村民数量
     */
    public int getMarriedCount() {
        return (int) families.values().stream()
            .filter(VillagerFamily::isMarried)
            .count();
    }

    /**
     * 获取平均家庭大小
     */
    public double getAverageFamilySize() {
        if (families.isEmpty()) return 0;

        long totalSize = families.values().stream()
            .mapToLong(VillagerFamily::getFamilySize)
            .sum();

        return (double) totalSize / families.size();
    }

    /**
     * 获取总村民数
     */
    public int getTotalVillagerCount() {
        return families.size();
    }
}
