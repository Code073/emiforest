package com.emiforest.events;

import com.emiforest.forest.ForestManager;
import com.emiforest.save.ForestAutoSaver;
import com.emiforest.save.ForestSaveManager;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "emiforest", value = Dist.CLIENT)
public class ClientEvents {

    private static boolean pendingLoad = false;
    private static int waitTicks = 0;
    private static final int MAX_WAIT_TICKS = 1200; // 60s como margen de seguridad absoluto

    @SubscribeEvent
    public static void onLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        pendingLoad = true;
        waitTicks = 0;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (pendingLoad) {
            waitTicks++;

            boolean emiReady = EmiReloadManager.isLoaded();
            boolean safetyTimeout = waitTicks > MAX_WAIT_TICKS;

            if (emiReady || safetyTimeout) {
                pendingLoad = false;
                String reason = emiReady ? "EMI reporto carga completa" : "timeout de seguridad tras " + waitTicks + " ticks";
                System.out.println("[EMI Forest] Cargando (" + reason + ")");

                boolean success = ForestSaveManager.loadAndPopulate();
                if (success) {
                    ForestAutoSaver.resync();
                    ForestAutoSaver.loadCompletedSuccessfully = true;
                } else {
                    System.err.println("[EMI Forest] Carga fallida, reintentando...");
                    pendingLoad = true;
                    waitTicks = 0; // reintenta desde cero, EmiReloadManager podría recargar de nuevo
                }
            }
            return;
        }

        ForestAutoSaver.checkForChanges();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        System.out.println("[EMI Forest] onLoggingOut disparado. No se hace guardado adicional.");
        ForestAutoSaver.loadCompletedSuccessfully = false;
    }
}