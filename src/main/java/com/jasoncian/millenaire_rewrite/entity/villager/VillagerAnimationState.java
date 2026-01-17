package com.jasoncian.millenaire_rewrite.entity.villager;

/**
 * 村民动画状态枚举
 *
 * 定义村民可能的动画状态
 * 用于模型动画和渲染系统
 *
 * @author Based on OldSource animation system
 * @version 1.0.0
 */
public enum VillagerAnimationState {

    /** 空闲状态 - 站立不动 */
    IDLE("idle"),

    /** 行走状态 */
    WALKING("walking"),

    /** 跑步状态 */
    RUNNING("running"),

    /** 工作状态 - 通用工作动画 */
    WORKING("working"),

    /** 采矿状态 - 挥动镐子 */
    MINING("mining"),

    /** 砍伐状态 - 挥动斧头 */
    CHOPPING("chopping"),

    /** 耕作状态 - 使用锄头 */
    FARMING("farming"),

    /** 建造状态 - 放置方块 */
    BUILDING("building"),

    /** 攻击状态 - 战斗挥动 */
    ATTACKING("attacking"),

    /** 防御状态 - 举盾 */
    DEFENDING("defending"),

    /** 进食状态 */
    EATING("eating"),

    /** 睡眠状态 */
    SLEEPING("sleeping"),

    /** 社交状态 - 与其他村民交谈 */
    SOCIALIZING("socializing"),

    /** 受伤状态 */
    HURT("hurt"),

    /** 死亡状态 */
    DYING("dying");

    private final String id;

    VillagerAnimationState(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * 检查是否为工作类动画
     */
    public boolean isWorkAnimation() {
        return this == WORKING || this == MINING || this == CHOPPING || this == FARMING || this == BUILDING;
    }

    /**
     * 检查是否为战斗类动画
     */
    public boolean isCombatAnimation() {
        return this == ATTACKING || this == DEFENDING;
    }

    /**
     * 检查是否为移动类动画
     */
    public boolean isMovementAnimation() {
        return this == WALKING || this == RUNNING;
    }

    /**
     * 根据移动速度获取适当的移动动画状态
     */
    public static VillagerAnimationState fromMovementSpeed(float speed) {
        if (speed > 0.2f) {
            return RUNNING;
        } else if (speed > 0.01f) {
            return WALKING;
        }
        return IDLE;
    }
}
