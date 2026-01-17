package com.jasoncian.millenaire_rewrite.entity.ai;

/**
 * 目标状态枚举 - 定义目标的生命周期状态
 *
 * @author Based on OldSource Goal system
 * @version 1.0.0
 */
public enum GoalState {

    /** 目标未激活，等待选择 */
    INACTIVE,

    /** 目标已被选择，正在前往目的地 */
    TRAVELING,

    /** 已到达目的地，正在执行动作 */
    PERFORMING,

    /** 目标成功完成 */
    COMPLETED,

    /** 目标失败（无法到达、条件不满足等） */
    FAILED,

    /** 目标被中断（更高优先级目标介入） */
    INTERRUPTED;

    /**
     * 检查是否为活跃状态
     */
    public boolean isActive() {
        return this == TRAVELING || this == PERFORMING;
    }

    /**
     * 检查是否已结束
     */
    public boolean isTerminated() {
        return this == COMPLETED || this == FAILED || this == INTERRUPTED;
    }
}
