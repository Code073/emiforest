package com.emiforest.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = com.emiforest.EmiForest.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModKeybinds {
    public static final String CATEGORY = "key.categories.emiforest";

    public static final KeyMapping NEXT_TREE = new KeyMapping(
            "key.emiforest.next_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_N,
            CATEGORY
    );

    public static final KeyMapping PREVIOUS_TREE = new KeyMapping(
            "key.emiforest.previous_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            CATEGORY
    );

    public static final KeyMapping DELETE_TREE = new KeyMapping(
            "key.emiforest.delete_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            CATEGORY
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(NEXT_TREE);
        event.register(PREVIOUS_TREE);
        event.register(DELETE_TREE);
    }
}