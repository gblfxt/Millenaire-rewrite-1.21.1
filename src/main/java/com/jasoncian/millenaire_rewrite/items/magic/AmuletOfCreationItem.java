package com.jasoncian.millenaire_rewrite.items.magic;

import com.jasoncian.millenaire_rewrite.util.ItemNBTHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Amulet of Creation - The ultimate quest reward
 *
 * This legendary artifact is obtained by completing the 3-chapter Creation Quest.
 * It combines the powers of all other amulets and grants additional abilities:
 * - Passive regeneration when held
 * - Right-click to activate a powerful buff (with cooldown)
 * - Glowing effect to show its legendary status
 *
 * Lore: Forged from Galianite and imbued with the wisdom of the Sadhu,
 * the knowledge of the Alchemist, and the power of the Fallen King.
 *
 * @author JasonCian
 * @version 0.1.0-alpha
 */
public class AmuletOfCreationItem extends Item {

    private static final int COOLDOWN_TICKS = 6000; // 5 minutes cooldown
    private static final int BUFF_DURATION = 600; // 30 seconds of buffs
    private static final int PASSIVE_REGEN_INTERVAL = 100; // Regen every 5 seconds

    public AmuletOfCreationItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }

        // Passive regeneration when in inventory
        long gameTime = level.getGameTime();
        if (gameTime % PASSIVE_REGEN_INTERVAL == 0) {
            // Small passive heal
            if (player.getHealth() < player.getMaxHealth()) {
                player.heal(1.0F);
            }
        }

        // Update cooldown display in NBT
        CompoundTag nbt = ItemNBTHelper.getOrCreateTag(stack);
        long lastUse = nbt.getLong("last_use");
        long cooldownRemaining = Math.max(0, COOLDOWN_TICKS - (gameTime - lastUse));
        nbt.putLong("cooldown_remaining", cooldownRemaining);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            CompoundTag nbt = ItemNBTHelper.getOrCreateTag(stack);
            long lastUse = nbt.getLong("last_use");
            long gameTime = level.getGameTime();

            // Check cooldown
            if (gameTime - lastUse < COOLDOWN_TICKS && lastUse != 0) {
                long remainingSeconds = (COOLDOWN_TICKS - (gameTime - lastUse)) / 20;
                player.sendSystemMessage(Component.translatable(
                    "item.millenaire_rewrite.amulet_creation.cooldown", remainingSeconds)
                    .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(stack);
            }

            // Activate the amulet's power
            activateCreationPower(level, player, stack);

            // Set cooldown
            nbt.putLong("last_use", gameTime);

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return InteractionResultHolder.pass(stack);
    }

    private void activateCreationPower(Level level, Player player, ItemStack stack) {
        // Grant powerful buffs
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF_DURATION, 1)); // Regen II
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BUFF_DURATION, 1)); // Resistance II
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_DURATION, 1)); // Strength II
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, BUFF_DURATION, 1)); // Haste II
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, BUFF_DURATION, 0)); // Night Vision
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, BUFF_DURATION, 0)); // Fire Resistance

        // Visual and sound effects
        if (level instanceof ServerLevel serverLevel) {
            BlockPos pos = player.blockPosition();

            // Spawn particles around the player
            for (int i = 0; i < 50; i++) {
                double offsetX = (level.random.nextDouble() - 0.5) * 2;
                double offsetY = level.random.nextDouble() * 2;
                double offsetZ = (level.random.nextDouble() - 0.5) * 2;
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1, 0, 0.1, 0, 0.05);
            }

            // Play activation sound
            level.playSound(null, pos, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        // Send activation message
        player.sendSystemMessage(Component.translatable("item.millenaire_rewrite.amulet_creation.activated")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // Lore description
        tooltip.add(Component.translatable("item.millenaire_rewrite.amulet_creation.lore1")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.millenaire_rewrite.amulet_creation.lore2")
            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        tooltip.add(Component.empty());

        // Abilities
        tooltip.add(Component.translatable("item.millenaire_rewrite.amulet_creation.passive")
            .withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.translatable("item.millenaire_rewrite.amulet_creation.active")
            .withStyle(ChatFormatting.GOLD));

        // Cooldown status
        CompoundTag nbt = ItemNBTHelper.getTag(stack);
        if (nbt != null) {
            long cooldownRemaining = nbt.getLong("cooldown_remaining");
            if (cooldownRemaining > 0) {
                long seconds = cooldownRemaining / 20;
                long minutes = seconds / 60;
                seconds = seconds % 60;
                tooltip.add(Component.translatable("item.millenaire_rewrite.amulet_creation.cooldown_display",
                    minutes, seconds)
                    .withStyle(ChatFormatting.RED));
            } else {
                tooltip.add(Component.translatable("item.millenaire_rewrite.amulet_creation.ready")
                    .withStyle(ChatFormatting.AQUA));
            }
        }

        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Always show enchantment glint for this legendary item
        return true;
    }
}
