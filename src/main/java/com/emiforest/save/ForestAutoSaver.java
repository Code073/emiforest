package com.emiforest.save;

import java.util.Timer;
import java.util.TimerTask;

public class ForestAutoSaver {

    private static final long DEBOUNCE_MS = 500;
    private static Timer pendingTimer = null;

    public static synchronized void scheduleSave() {
        cancelPending();
        pendingTimer = new Timer("EMIForest-Debounce", true);
        pendingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (WorldSaveIdentifier.hasValidWorld()) {
                    ForestSaveManager.saveTrees();
                }
            }
        }, DEBOUNCE_MS);
    }

    public static synchronized void cancelPending() {
        if (pendingTimer != null) {
            pendingTimer.cancel();
            pendingTimer = null;
        }
    }
}