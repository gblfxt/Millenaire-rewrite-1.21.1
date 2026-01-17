package com.jasoncian.millenaire_rewrite;

import com.jasoncian.millenaire_rewrite.core.ModBlocks;
import com.jasoncian.millenaire_rewrite.core.ModItems;
import com.jasoncian.millenaire_rewrite.core.ModBlockItems;
import com.jasoncian.millenaire_rewrite.core.ModEntities;
import com.jasoncian.millenaire_rewrite.core.ModBlockEntities;
import com.jasoncian.millenaire_rewrite.core.ModToolMaterials;
import com.jasoncian.millenaire_rewrite.core.MillCreativeTabs;
import com.jasoncian.millenaire_rewrite.config.MillenaireConfig;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Millenaire Rewrite - Modern rewrite main class
 *
 * Features:
 * - Mod initialization and registration management
 * - Creative mode tab configuration
 * - Mod event handling
 * - Logging management
 * - Modern mod architecture design
 * - Complete registration system management
 * - Client and server separation
 * - Data generation system integration
 * - Configuration system support
 *
 * @author JasonCian, gblfxt
 * @version 0.2.0-alpha
 * @since 1.21.1
 */
@Mod(MillenaireRewrite.MOD_ID)
public class MillenaireRewrite {

    public static final String MOD_ID = "millenaire_rewrite";
    public static final String MOD_NAME = "Millenaire Rewrite";
    public static final String VERSION = "0.2.0-alpha";

    public static final Logger LOGGER = LogUtils.getLogger();

    public MillenaireRewrite(IEventBus modEventBus, ModContainer modContainer) {
        // Register core components
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        // ModBlockItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        MillCreativeTabs.register(modEventBus);

        // Register event listeners
        modEventBus.addListener(this::commonSetup);

        // Register NeoForge event bus
        NeoForge.EVENT_BUS.addListener(MillenaireRewrite::onServerStarting);

        // Register config
        modEventBus.addListener(MillenaireConfig::onLoad);
        modEventBus.addListener(MillenaireConfig::onReload);

        LOGGER.info("Millenaire Rewrite mod initialized!");
    }

    /**
     * Common setup phase
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Millenaire Rewrite common setup starting...");

        event.enqueueWork(() -> {
            // Initialize tool tier sorting
            ModToolMaterials.initializeTierSorting();
        });

        LOGGER.info("Millenaire Rewrite common setup completed!");
    }

    /**
     * Server starting event handler
     */
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Millenaire Rewrite server is starting...");
    }

    /**
     * Client-only event handling
     */
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Millenaire Rewrite client setup starting...");

            event.enqueueWork(() -> {
                // Client-specific initialization
            });

            LOGGER.info("Millenaire Rewrite client setup completed!");
        }
    }
}
