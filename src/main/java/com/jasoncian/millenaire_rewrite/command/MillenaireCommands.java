package com.jasoncian.millenaire_rewrite.command;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.entity.culture.Culture;
import com.jasoncian.millenaire_rewrite.village.Village;
import com.jasoncian.millenaire_rewrite.village.VillageManager;
import com.jasoncian.millenaire_rewrite.worldgen.VillageGenerator;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Millenaire模组命令 - 提供管理和测试命令
 *
 * 命令列表：
 * - /millenaire spawn [culture] - 在当前位置生成村庄
 * - /millenaire list - 列出所有村庄
 * - /millenaire info - 显示最近村庄信息
 * - /millenaire tp <村庄名> - 传送到村庄
 * - /millenaire remove <村庄名> - 删除村庄
 *
 * @author Based on command patterns
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID)
public class MillenaireCommands {

    // ================ 建议提供器 ================

    /** 文化名称建议 */
    private static final SuggestionProvider<CommandSourceStack> CULTURE_SUGGESTIONS = (context, builder) ->
        SharedSuggestionProvider.suggest(
            Arrays.stream(Culture.values()).map(c -> c.getId().toLowerCase()),
            builder
        );

    /** 村庄名称建议 */
    private static final SuggestionProvider<CommandSourceStack> VILLAGE_SUGGESTIONS = (context, builder) -> {
        ServerLevel level = context.getSource().getLevel();
        VillageManager manager = VillageManager.get(level);
        return SharedSuggestionProvider.suggest(
            manager.getAllVillages().stream().map(Village::getName),
            builder
        );
    };

    // ================ 命令注册 ================

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("millenaire")
            .requires(source -> source.hasPermission(2)) // 需要OP权限

            // /millenaire spawn [culture]
            .then(Commands.literal("spawn")
                .executes(ctx -> spawnVillage(ctx, null))
                .then(Commands.argument("culture", StringArgumentType.word())
                    .suggests(CULTURE_SUGGESTIONS)
                    .executes(ctx -> spawnVillage(ctx, StringArgumentType.getString(ctx, "culture")))
                )
            )

            // /millenaire list
            .then(Commands.literal("list")
                .executes(MillenaireCommands::listVillages)
            )

            // /millenaire info
            .then(Commands.literal("info")
                .executes(MillenaireCommands::showNearestVillageInfo)
            )

            // /millenaire tp <村庄名>
            .then(Commands.literal("tp")
                .then(Commands.argument("village", StringArgumentType.greedyString())
                    .suggests(VILLAGE_SUGGESTIONS)
                    .executes(ctx -> teleportToVillage(ctx, StringArgumentType.getString(ctx, "village")))
                )
            )

            // /millenaire remove <村庄名>
            .then(Commands.literal("remove")
                .then(Commands.argument("village", StringArgumentType.greedyString())
                    .suggests(VILLAGE_SUGGESTIONS)
                    .executes(ctx -> removeVillage(ctx, StringArgumentType.getString(ctx, "village")))
                )
            )

            // /millenaire stats
            .then(Commands.literal("stats")
                .executes(MillenaireCommands::showStats)
            )
        );

        // 简短别名
        dispatcher.register(Commands.literal("mill")
            .requires(source -> source.hasPermission(2))
            .redirect(dispatcher.getRoot().getChild("millenaire"))
        );

        MillenaireRewrite.LOGGER.info("Millenaire commands registered");
    }

    // ================ 命令实现 ================

    /**
     * 生成村庄命令
     */
    private static int spawnVillage(CommandContext<CommandSourceStack> ctx, String cultureStr) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        BlockPos blockPos = BlockPos.containing(pos);

        // 解析文化
        Culture culture = null;
        if (cultureStr != null && !cultureStr.isEmpty()) {
            culture = Culture.fromId(cultureStr);
            if (culture == null) {
                source.sendFailure(Component.literal("Unknown culture: " + cultureStr));
                source.sendFailure(Component.literal("Available cultures: " +
                    Arrays.stream(Culture.values())
                        .map(c -> c.getId().toLowerCase())
                        .collect(Collectors.joining(", "))));
                return 0;
            }
        }

        // 生成村庄
        Village village = VillageGenerator.forceGenerateVillage(level, blockPos, culture);

        if (village != null) {
            source.sendSuccess(() -> Component.literal(
                "§aGenerated " + village.getCulture().getDisplayName() +
                " village '" + village.getName() + "' at " +
                village.getTownHallPos().toShortString()
            ), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to generate village"));
            return 0;
        }
    }

    /**
     * 列出村庄命令
     */
    private static int listVillages(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        VillageManager manager = VillageManager.get(level);

        Collection<Village> villages = manager.getAllVillages();

        if (villages.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§7No villages exist in this world"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== Millenaire Villages (" + villages.size() + ") ==="), false);

        for (Village village : villages) {
            BlockPos pos = village.getTownHallPos();
            String posStr = pos != null ? pos.toShortString() : "unknown";
            source.sendSuccess(() -> Component.literal(
                "§e" + village.getName() + " §7(" + village.getCulture().getDisplayName() +
                ") at §f" + posStr +
                " §7[" + village.getActiveVillagerCount() + " villagers]"
            ), false);
        }

        return villages.size();
    }

    /**
     * 显示最近村庄信息
     */
    private static int showNearestVillageInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();
        BlockPos blockPos = BlockPos.containing(pos);

        VillageManager manager = VillageManager.get(level);
        Village nearest = manager.getNearestVillage(blockPos);

        if (nearest == null) {
            source.sendSuccess(() -> Component.literal("§7No villages nearby"), false);
            return 0;
        }

        BlockPos villagePos = nearest.getTownHallPos();
        double distance = Math.sqrt(villagePos.distSqr(blockPos));

        source.sendSuccess(() -> Component.literal("§6=== " + nearest.getName() + " ==="), false);
        source.sendSuccess(() -> Component.literal("§7Culture: §f" + nearest.getCulture().getDisplayName()), false);
        source.sendSuccess(() -> Component.literal("§7Position: §f" + villagePos.toShortString()), false);
        source.sendSuccess(() -> Component.literal("§7Distance: §f" + String.format("%.1f", distance) + " blocks"), false);
        source.sendSuccess(() -> Component.literal("§7Villagers: §f" + nearest.getActiveVillagerCount()), false);
        source.sendSuccess(() -> Component.literal("§7Buildings: §f" + nearest.getBuildingCount()), false);
        source.sendSuccess(() -> Component.literal("§7Deniers: §e" + nearest.getDeniers()), false);
        source.sendSuccess(() -> Component.literal("§7Level: §f" + nearest.getLevel()), false);

        return 1;
    }

    /**
     * 传送到村庄
     */
    private static int teleportToVillage(CommandContext<CommandSourceStack> ctx, String villageName) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getLevel();
        VillageManager manager = VillageManager.get(level);

        Village village = findVillageByName(manager, villageName);

        if (village == null) {
            source.sendFailure(Component.literal("Village not found: " + villageName));
            return 0;
        }

        BlockPos pos = village.getTownHallPos();
        if (pos == null) {
            source.sendFailure(Component.literal("Village has no valid position"));
            return 0;
        }

        player.teleportTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        source.sendSuccess(() -> Component.literal("§aTeleported to " + village.getName()), true);

        return 1;
    }

    /**
     * 删除村庄
     */
    private static int removeVillage(CommandContext<CommandSourceStack> ctx, String villageName) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        VillageManager manager = VillageManager.get(level);

        Village village = findVillageByName(manager, villageName);

        if (village == null) {
            source.sendFailure(Component.literal("Village not found: " + villageName));
            return 0;
        }

        String name = village.getName();
        manager.removeVillage(village.getVillageId());

        source.sendSuccess(() -> Component.literal("§cRemoved village: " + name), true);

        return 1;
    }

    /**
     * 显示统计信息
     */
    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();
        VillageManager manager = VillageManager.get(level);

        Collection<Village> villages = manager.getAllVillages();

        int totalVillagers = 0;
        int totalBuildings = 0;
        long totalDeniers = 0;

        for (Village village : villages) {
            totalVillagers += village.getActiveVillagerCount();
            totalBuildings += village.getBuildingCount();
            totalDeniers += village.getDeniers();
        }

        final int fVillagers = totalVillagers;
        final int fBuildings = totalBuildings;
        final long fDeniers = totalDeniers;

        source.sendSuccess(() -> Component.literal("§6=== Millenaire Statistics ==="), false);
        source.sendSuccess(() -> Component.literal("§7Total Villages: §f" + villages.size()), false);
        source.sendSuccess(() -> Component.literal("§7Total Villagers: §f" + fVillagers), false);
        source.sendSuccess(() -> Component.literal("§7Total Buildings: §f" + fBuildings), false);
        source.sendSuccess(() -> Component.literal("§7Total Deniers: §e" + fDeniers), false);

        // 按文化统计
        source.sendSuccess(() -> Component.literal("§6--- By Culture ---"), false);
        for (Culture culture : Culture.values()) {
            long count = villages.stream()
                .filter(v -> v.getCulture() == culture)
                .count();
            if (count > 0) {
                source.sendSuccess(() -> Component.literal(
                    "§7" + culture.getDisplayName() + ": §f" + count
                ), false);
            }
        }

        return 1;
    }

    // ================ 辅助方法 ================

    /**
     * 根据名称查找村庄（支持部分匹配）
     */
    private static Village findVillageByName(VillageManager manager, String name) {
        String lowerName = name.toLowerCase();

        // 精确匹配
        for (Village village : manager.getAllVillages()) {
            if (village.getName().equalsIgnoreCase(name)) {
                return village;
            }
        }

        // 部分匹配
        for (Village village : manager.getAllVillages()) {
            if (village.getName().toLowerCase().contains(lowerName)) {
                return village;
            }
        }

        return null;
    }
}
