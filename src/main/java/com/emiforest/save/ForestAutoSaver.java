package com.emiforest.save;

import dev.emi.emi.bom.BoM;

import java.util.Map;

public class ForestAutoSaver {

    private static long lastKnownBatches = -1;
    private static int lastKnownResolutionsHash = 0;

    public static boolean loadCompletedSuccessfully = false;

    // Se llama desde el hilo principal (client tick) - ya estamos en el hilo correcto, no hace falta Timer ni execute()
    public static synchronized void checkForChanges() {
        if (!loadCompletedSuccessfully) return;
        if (BoM.tree == null) return;

        boolean changed = false;

        long currentBatches = BoM.tree.batches;
        if (currentBatches != lastKnownBatches) {
            lastKnownBatches = currentBatches;
            changed = true;
        }

        int currentResolutionsHash = computeResolutionsHash();
        if (currentResolutionsHash != lastKnownResolutionsHash) {
            lastKnownResolutionsHash = currentResolutionsHash;
            changed = true;
        }

        if (changed && WorldSaveIdentifier.hasValidWorld()) {
            ForestSaveManager.saveTrees();
        }
    }

    private static int computeResolutionsHash() {
        if (BoM.tree == null || BoM.tree.resolutions == null) return 0;
        int hash = BoM.tree.resolutions.size();
        for (Map.Entry<?, ?> entry : BoM.tree.resolutions.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            hash = 31 * hash + (key != null ? key.hashCode() : 0);
            hash = 31 * hash + (value != null ? value.hashCode() : 0);
        }
        return hash;
    }

    public static synchronized void resync() {
        lastKnownBatches = (BoM.tree != null) ? BoM.tree.batches : -1;
        lastKnownResolutionsHash = computeResolutionsHash();
    }

}