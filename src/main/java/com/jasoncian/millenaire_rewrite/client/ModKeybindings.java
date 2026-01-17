package com.jasoncian.millenaire_rewrite.client;

import com.jasoncian.millenaire_rewrite.MillenaireRewrite;
import com.jasoncian.millenaire_rewrite.client.gui.DiplomacyMapScreen;
import com.jasoncian.millenaire_rewrite.client.gui.QuestLogScreen;
import com.jasoncian.millenaire_rewrite.client.gui.ReputationScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * 模组快捷键绑定
 *
 * 注册的快捷键：
 * - M: 打开任务日志
 * - N: 打开声望界面
 * - B: 打开外交地图
 *
 * @author Based on NeoForge keybinding patterns
 * @version 1.0.0
 */
@EventBusSubscriber(modid = MillenaireRewrite.MOD_ID, value = Dist.CLIENT)
public class ModKeybindings {

    // ================ 快捷键定义 ================

    /** 打开任务日志 */
    public static final KeyMapping KEY_QUEST_LOG = new KeyMapping(
        "key.millenaire_rewrite.quest_log",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_J, // J键
        "key.categories.millenaire_rewrite"
    );

    /** 打开声望界面 */
    public static final KeyMapping KEY_REPUTATION = new KeyMapping(
        "key.millenaire_rewrite.reputation",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K, // K键
        "key.categories.millenaire_rewrite"
    );

    /** 打开外交地图 */
    public static final KeyMapping KEY_DIPLOMACY = new KeyMapping(
        "key.millenaire_rewrite.diplomacy",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_L, // L键
        "key.categories.millenaire_rewrite"
    );

    // ================ 注册 ================

    /**
     * 注册快捷键到事件总线
     * 在客户端模组总线上调用
     */
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(KEY_QUEST_LOG);
        event.register(KEY_REPUTATION);
        event.register(KEY_DIPLOMACY);

        MillenaireRewrite.LOGGER.info("Registered Millenaire keybindings");
    }

    // ================ 快捷键处理 ================

    /**
     * 客户端tick时检查快捷键
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        // 只在游戏中且没有其他GUI打开时响应
        if (mc.player == null || mc.screen != null) {
            return;
        }

        // 任务日志
        if (KEY_QUEST_LOG.consumeClick()) {
            mc.setScreen(new QuestLogScreen());
        }

        // 声望界面
        if (KEY_REPUTATION.consumeClick()) {
            mc.setScreen(new ReputationScreen());
        }

        // 外交地图
        if (KEY_DIPLOMACY.consumeClick()) {
            mc.setScreen(new DiplomacyMapScreen());
        }
    }
}
