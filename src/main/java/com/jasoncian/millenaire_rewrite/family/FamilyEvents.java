package com.jasoncian.millenaire_rewrite.family;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.core.ModEntities;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.village.Village;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

/**
 * 家庭事件处理器 - 处理婚礼、出生等家庭相关事件
 *
 * @author Based on OldSource family events
 * @version 1.0.0
 */
public class FamilyEvents {

    private static final Random random = new Random();

    // ================ 生育常量 ================

    /** 每次tick检查生育的概率 */
    private static final float BIRTH_CHECK_CHANCE = 0.0001f; // 约每3小时一次

    /** 双胞胎概率 */
    private static final float TWINS_CHANCE = 0.02f;

    /** 男孩概率 */
    private static final float BOY_CHANCE = 0.5f;

    // ================ 婚礼事件 ================

    /**
     * 举办婚礼
     */
    public static boolean performWedding(ServerLevel level, MillVillager bride, MillVillager groom, Village village) {
        FamilyManager familyManager = FamilyManager.get(level);
        long currentTime = level.getGameTime();

        // 获取家庭数据
        VillagerFamily brideFamily = familyManager.getOrCreateFamily(bride.getVillagerId(), false);
        VillagerFamily groomFamily = familyManager.getOrCreateFamily(groom.getVillagerId(), true);

        // 检查是否可以结婚
        if (!brideFamily.canMarry(groomFamily)) {
            MillenaireRewrite.LOGGER.warn("Wedding failed: {} and {} cannot marry",
                bride.getVillagerId(), groom.getVillagerId());
            return false;
        }

        // 执行结婚
        boolean success = familyManager.marry(groom.getVillagerId(), bride.getVillagerId(), currentTime);

        if (success) {
            // 婚礼效果
            onWeddingSuccess(level, bride, groom, village);
            return true;
        }

        return false;
    }

    /**
     * 婚礼成功后的效果
     */
    private static void onWeddingSuccess(ServerLevel level, MillVillager bride, MillVillager groom, Village village) {
        // 播放烟花或粒子效果
        BlockPos weddingPos = bride.blockPosition();

        // 通知附近玩家
        MillenaireRewrite.LOGGER.info("Wedding celebrated in {} between villagers {} and {}",
            village.getName(), groom.getVillagerId(), bride.getVillagerId());

        // TODO: 增加村庄幸福度
        // TODO: 播放音效
    }

    /**
     * 处理订婚
     */
    public static boolean performEngagement(ServerLevel level, MillVillager villagerA, MillVillager villagerB) {
        FamilyManager familyManager = FamilyManager.get(level);
        long currentTime = level.getGameTime();

        return familyManager.engage(villagerA.getVillagerId(), villagerB.getVillagerId(), currentTime);
    }

    // ================ 出生事件 ================

    /**
     * 检查并处理生育
     */
    public static void checkBirth(ServerLevel level, MillVillager mother, Village village) {
        // 概率检查
        if (random.nextFloat() > BIRTH_CHECK_CHANCE) {
            return;
        }

        FamilyManager familyManager = FamilyManager.get(level);
        VillagerFamily motherFamily = familyManager.getFamily(mother.getVillagerId());

        if (motherFamily == null || !motherFamily.isMarried()) {
            return;
        }

        Long fatherId = motherFamily.getSpouseId();
        if (fatherId == null) {
            return;
        }

        long currentTime = level.getGameTime();

        // 检查是否可以生育
        if (!familyManager.canHaveChild(mother.getVillagerId(), fatherId, currentTime)) {
            return;
        }

        // 生育！
        boolean isBoy = random.nextFloat() < BOY_CHANCE;
        performBirth(level, mother, fatherId, isBoy, village);

        // 双胞胎检查
        if (random.nextFloat() < TWINS_CHANCE) {
            boolean twinIsBoy = random.nextFloat() < BOY_CHANCE;
            performBirth(level, mother, fatherId, twinIsBoy, village);
            MillenaireRewrite.LOGGER.info("Twins born in village {}!", village.getName());
        }
    }

    /**
     * 执行出生
     */
    @Nullable
    public static MillVillager performBirth(ServerLevel level, MillVillager mother, long fatherId, boolean isBoy, Village village) {
        FamilyManager familyManager = FamilyManager.get(level);
        long currentTime = level.getGameTime();

        // 创建新村民
        MillVillager child = createChildVillager(level, mother, isBoy, village);
        if (child == null) {
            return null;
        }

        // 设置出生位置（母亲附近）
        BlockPos birthPos = mother.blockPosition();
        child.setPos(birthPos.getX() + 0.5, birthPos.getY(), birthPos.getZ() + 0.5);

        // 添加到世界
        level.addFreshEntity(child);

        // 创建家庭数据
        VillagerFamily childFamily = familyManager.birthChild(
            fatherId, mother.getVillagerId(), isBoy, child.getVillagerId(), currentTime
        );

        if (childFamily != null) {
            // 设置姓氏
            VillagerFamily fatherFamily = familyManager.getFamily(fatherId);
            if (fatherFamily != null) {
                childFamily.setFamilyName(fatherFamily.getFamilyName());
            }
        }

        // 注册到村庄
        village.registerVillager(child);

        MillenaireRewrite.LOGGER.info("Child {} born in village {} to mother {}",
            child.getVillagerId(), village.getName(), mother.getVillagerId());

        return child;
    }

    /**
     * 创建子女村民实体
     */
    @Nullable
    private static MillVillager createChildVillager(ServerLevel level, MillVillager mother, boolean isBoy, Village village) {
        // 创建新的村民实体
        MillVillager child = ModEntities.MILL_VILLAGER.get().create(level);

        if (child == null) {
            return null;
        }

        // 设置基本属性
        child.setCulture(mother.getCulture());
        child.setTownHallPos(village.getTownHallPos());
        child.setGender(isBoy ? MillVillager.GENDER_MALE : MillVillager.GENDER_FEMALE);
        child.setChild(true);

        // 生成名字
        String childName = generateChildName(mother.getCulture(), isBoy);
        child.setCustomName(net.minecraft.network.chat.Component.literal(childName));

        return child;
    }

    /**
     * 生成子女名字
     */
    private static String generateChildName(Culture culture, boolean isBoy) {
        // 文化特定名字池
        String[] maleNames = getMaleNames(culture);
        String[] femaleNames = getFemaleNames(culture);

        String[] names = isBoy ? maleNames : femaleNames;
        return names[random.nextInt(names.length)];
    }

    /**
     * 获取男性名字池
     */
    private static String[] getMaleNames(Culture culture) {
        return switch (culture) {
            case NORMAN -> new String[]{
                "Guillaume", "Henri", "Robert", "Richard", "Jean",
                "Pierre", "Louis", "Charles", "Philippe", "Jacques"
            };
            case JAPANESE -> new String[]{
                "Takeshi", "Hiroshi", "Kenji", "Masaru", "Yuki",
                "Akira", "Haruki", "Ryu", "Ken", "Taro"
            };
            case INDIAN -> new String[]{
                "Raj", "Arjun", "Vikram", "Ravi", "Amit",
                "Sanjay", "Deepak", "Krishna", "Rahul", "Anil"
            };
            case MAYAN -> new String[]{
                "Itzamna", "Kukulkan", "Chaac", "Kinich", "Ahau",
                "Balam", "Ek", "Kan", "Noh", "Yax"
            };
            case BYZANTINE -> new String[]{
                "Constantine", "Michael", "John", "Alexios", "Theodore",
                "Manuel", "Isaac", "Andronikos", "George", "Basil"
            };
            case INUIT -> new String[]{
                "Nanuk", "Amarok", "Tulugaq", "Siku", "Qimmiq",
                "Akiak", "Nanuq", "Umiak", "Kiviuq", "Tuktu"
            };
            case SELJUK -> new String[]{
                "Alp", "Tugrul", "Malik", "Suleiman", "Arslan",
                "Kilij", "Kadir", "Mehmed", "Osman", "Orhan"
            };
        };
    }

    /**
     * 获取女性名字池
     */
    private static String[] getFemaleNames(Culture culture) {
        return switch (culture) {
            case NORMAN -> new String[]{
                "Marie", "Jeanne", "Catherine", "Anne", "Marguerite",
                "Isabelle", "Louise", "Claire", "Sophie", "Charlotte"
            };
            case JAPANESE -> new String[]{
                "Yuki", "Sakura", "Hana", "Aiko", "Mei",
                "Haruka", "Rin", "Sora", "Miku", "Akemi"
            };
            case INDIAN -> new String[]{
                "Priya", "Lakshmi", "Ananya", "Devi", "Sita",
                "Radha", "Meera", "Gita", "Kavita", "Asha"
            };
            case MAYAN -> new String[]{
                "Ixchel", "Xquic", "Xtabay", "Chel", "Ix",
                "Nicte", "Sacnikte", "Xunaan", "Zazil", "Itzayana"
            };
            case BYZANTINE -> new String[]{
                "Anna", "Maria", "Theodora", "Irene", "Zoe",
                "Helena", "Eudocia", "Euphrosyne", "Sophia", "Anastasia"
            };
            case INUIT -> new String[]{
                "Sedna", "Nuliajuk", "Arnaaluk", "Pinga", "Asiaq",
                "Malina", "Ahnah", "Nuka", "Kaya", "Siku"
            };
            case SELJUK -> new String[]{
                "Fatima", "Aisha", "Zainab", "Khadija", "Leyla",
                "Safiye", "Nilüfer", "Gülbahar", "Hafsa", "Mahidevran"
            };
        };
    }

    // ================ 村民死亡处理 ================

    /**
     * 处理村民死亡对家庭的影响
     */
    public static void onVillagerDeath(ServerLevel level, MillVillager deceased) {
        FamilyManager familyManager = FamilyManager.get(level);
        familyManager.onVillagerDeath(deceased.getVillagerId());
    }

    // ================ 成长处理 ================

    /**
     * 处理子女成长为成人
     */
    public static void onChildGrowUp(ServerLevel level, MillVillager child, Village village) {
        child.setChild(false);

        // 分配职业
        // 这将在VillagerProfession系统中处理

        MillenaireRewrite.LOGGER.info("Child {} has grown up in village {}",
            child.getVillagerId(), village.getName());
    }

    // ================ 配对系统 ================

    /**
     * 为单身村民寻找合适的配偶
     */
    @Nullable
    public static MillVillager findSuitablePartner(ServerLevel level, MillVillager villager, Village village) {
        FamilyManager familyManager = FamilyManager.get(level);
        VillagerFamily family = familyManager.getFamily(villager.getVillagerId());

        if (family == null || !family.isSingle()) {
            return null;
        }

        // 在村庄中寻找合适的单身村民
        for (MillVillager candidate : village.getActiveVillagers()) {
            if (candidate.getVillagerId() == villager.getVillagerId()) {
                continue;
            }

            VillagerFamily candidateFamily = familyManager.getFamily(candidate.getVillagerId());
            if (candidateFamily == null) continue;

            // 检查是否单身且可以结婚
            if (candidateFamily.isSingle() && family.canMarry(candidateFamily)) {
                // 简单的配对逻辑：异性优先
                if (villager.isMale() != candidate.isMale()) {
                    return candidate;
                }
            }
        }

        return null;
    }
}
