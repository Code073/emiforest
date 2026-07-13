package com.emiforest.save;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.level.storage.LevelResource;

public class WorldSaveIdentifier {

    public static String getCurrentId() {
        Minecraft mc = Minecraft.getInstance();

        // Singleplayer: usamos el nombre real de la carpeta de guardado
        if (mc.hasSingleplayerServer() && mc.getSingleplayerServer() != null) {
            try {
                String folderName = mc.getSingleplayerServer()
                        .getWorldPath(LevelResource.ROOT)
                        .toAbsolutePath()
                        .normalize()
                        .getFileName()
                        .toString();
                return sanitize("sp_" + folderName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Multiplayer: usamos la IP del servidor
        ServerData server = mc.getCurrentServer();
        if (server != null && server.ip != null) {
            return sanitize("mp_" + server.ip);
        }

        return "unknown_world";
    }

    private static String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9_.-]", "_");
    }
}