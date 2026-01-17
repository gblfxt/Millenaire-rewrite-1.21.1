package com.jasoncian.millenaire_rewrite.menu;

import com.jasoncian.millenaire_rewrite.core.ModMenuTypes;
import com.jasoncian.millenaire_rewrite.entity.MillVillager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 村民交互菜单 - 用于玩家与Millenaire村民的交互
 *
 * 提供功能：
 * - 查看村民信息
 * - 交易功能
 * - 雇佣功能
 * - 好感度显示
 *
 * @author Based on OldSource VillagerInteraction
 * @version 1.0.0
 */
public class VillagerInteractionMenu extends AbstractContainerMenu {

    /** 关联的村民实体 */
    @Nullable
    private final MillVillager villager;

    /** 玩家 */
    private final Player player;

    // ================ 构造函数 ================

    /**
     * 服务端构造函数
     */
    public VillagerInteractionMenu(int containerId, Inventory playerInventory, MillVillager villager) {
        super(ModMenuTypes.VILLAGER_INTERACTION.get(), containerId);
        this.player = playerInventory.player;
        this.villager = villager;
    }

    /**
     * 客户端构造函数 - 从网络数据包创建
     */
    public VillagerInteractionMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(ModMenuTypes.VILLAGER_INTERACTION.get(), containerId);
        this.player = playerInventory.player;

        // 从数据包读取村民ID
        int villagerEntityId = data.readInt();
        var entity = playerInventory.player.level().getEntity(villagerEntityId);
        this.villager = entity instanceof MillVillager mv ? mv : null;
    }

    // ================ 菜单功能 ================

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        // 此菜单没有物品槽，返回空
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        // 检查村民是否仍然有效且在范围内
        if (villager == null || !villager.isAlive()) {
            return false;
        }
        return player.distanceToSqr(villager) <= 64.0D;
    }

    // ================ Getters ================

    @Nullable
    public MillVillager getVillager() {
        return villager;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * 获取村民名字
     */
    public String getVillagerName() {
        return villager != null ? villager.getFullName() : "";
    }

    /**
     * 获取村民文化
     */
    public String getVillagerCulture() {
        return villager != null ? villager.getCulture().getDisplayName() : "";
    }

    /**
     * 获取村民职业
     */
    public String getVillagerProfession() {
        return villager != null ? villager.getProfession().getDisplayName() : "";
    }

    /**
     * 检查村民是否可被雇佣
     */
    public boolean canHire() {
        if (villager == null) return false;
        return villager.getProfession().isCombatProfession() && !villager.isHired();
    }

    /**
     * 检查村民是否可交易
     */
    public boolean canTrade() {
        if (villager == null) return false;
        return villager.getProfession().isMerchantProfession();
    }
}
