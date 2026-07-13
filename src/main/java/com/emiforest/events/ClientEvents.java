package com.emiforest.events;

import com.emiforest.save.ForestAutoSaver;
import com.emiforest.save.ForestSaveManager;
import dev.emi.emi.api.EmiApi;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "emiforest", value = Dist.CLIENT)
public class ClientEvents {

    private static boolean pendingLoad = false;
    private static int waitTicks = 0;
    private static int lastRecipeCount = -1;
    private static int stableTicks = 0;

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        pendingLoad = true;
        waitTicks = 0;
        lastRecipeCount = -1;
        stableTicks = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !pendingLoad) return;

        waitTicks++;

        int currentCount = getRecipeCountSafe();

        // Consideramos "listo" cuando el número de recetas deja de cambiar
        // durante varios ticks seguidos (EMI ya terminó de indexar)
        if (currentCount == lastRecipeCount && currentCount > 0) {
            stableTicks++;
        } else {
            stableTicks = 0;
        }
        lastRecipeCount = currentCount;

        boolean stable = stableTicks >= 5; // 5 ticks iguales seguidos = estable
        boolean timeout = waitTicks > 200; // margen de 10s por si acaso

        if (stable || timeout) {
            pendingLoad = false;
            System.out.println("[EMI Forest] Cargando tras " + waitTicks + " ticks. Recetas EMI detectadas: " + currentCount + (timeout ? " (timeout)" : " (estable)"));
            ForestSaveManager.loadAndPopulate();
        }
    }

    private static int getRecipeCountSafe() {
        try {
            var manager = EmiApi.getRecipeManager();
            if (manager == null) return 0;
            return manager.getRecipes().size();
        } catch (Exception e) {
            return 0;
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ForestAutoSaver.cancelPending();
        ForestSaveManager.saveTrees();
        System.out.println("[EMI Forest] Arboles guardados al salir del mundo/servidor.");
    }
}