package com.emiforest;

import com.emiforest.client.ModKeybinds;
import com.emiforest.forest.ForestManager;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod(EmiForest.MOD_ID)
public class EmiForest {
    public static final String MOD_ID = "emiforest";

    public EmiForest() {
        MinecraftForge.EVENT_BUS.addListener(this::onKeyInput);
    }

    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) return;

        Minecraft client = Minecraft.getInstance();
        if (client.screen == null) return;
        if (!(client.screen instanceof BoMScreen)) return;

        int key = event.getKey();
        if (key == ModKeybinds.NEXT_TREE.getKey().getValue()) {
            ForestManager.next();
        } else if (key == ModKeybinds.PREVIOUS_TREE.getKey().getValue()) {
            ForestManager.previous();
        } else if (key == ModKeybinds.DELETE_TREE.getKey().getValue()) {
            ForestManager.delTree();
        }
    }
}