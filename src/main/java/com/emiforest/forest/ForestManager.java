package com.emiforest.forest;

import com.emiforest.save.ForestAutoSaver;
import com.emiforest.save.ForestSaveManager;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.MaterialTree;
import dev.emi.emi.screen.BoMScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ForestManager {
    private static final List<MaterialTree> TREES = new ArrayList<>();
    private static int currentIndex = -1;
    private static final List<String> CUSTOM_NAMES = new ArrayList<>();
    private static boolean bulkLoading = false; // NUEVO

    public static void setBulkLoading(boolean value) {
        bulkLoading = value;
    }
    public static void addTree(MaterialTree tree) {
        TREES.add(tree);
        CUSTOM_NAMES.add(null);
        currentIndex = TREES.size() - 1;
        if (!bulkLoading) {
            ForestSaveManager.saveTrees();
        }
    }
    public static List<MaterialTree> getTrees() {
        return TREES;
    }
    public static int getCurrentIndex() {
        return currentIndex;
    }
    public static String getDisplayName(int index) {
        if (index < 0 || index >= TREES.size()) return "";
        String custom = CUSTOM_NAMES.get(index);
        return custom != null ? custom : Component.translatable("emi_forest.gui.tree_entry", index + 1).getString();
    }
    public static void setCustomName(int index, String name) {
        if (index >= 0 && index < CUSTOM_NAMES.size()) {
            CUSTOM_NAMES.set(index, name);
        }
        if (!bulkLoading) {
            ForestSaveManager.saveTrees();
        }
    }
    public static void select(int index) {
        if (index < 0 || index >= TREES.size()) {
            return;
        }
        currentIndex = index;
        BoM.tree = TREES.get(currentIndex);
        ForestAutoSaver.resync(); // evita guardado falso al cambiar de árbol
    }
    public static void delete(int index) {
        if (index < 0 || index >= TREES.size()) return;
        TREES.remove(index);
        CUSTOM_NAMES.remove(index);

        if (TREES.isEmpty()) {
            currentIndex = -1;
            BoM.tree = null;
        } else {
            if (index < currentIndex) {
                currentIndex--;
            } else if (index == currentIndex) {
                currentIndex = Math.min(currentIndex, TREES.size() - 1);
            }
            BoM.tree = TREES.get(currentIndex);
        }
        refreshBoM();
        if (!bulkLoading) {
            ForestSaveManager.saveTrees(true); // intentional deletion, allow saving an empty list
        }
    }
    public static void deleteAll() {
        TREES.clear();
        CUSTOM_NAMES.clear();
        currentIndex = -1;
        BoM.tree = null;
        refreshBoM();
        if (!bulkLoading) {
            ForestSaveManager.saveTrees(true);
        }
    }

    public static void next() {
        if (TREES.isEmpty()) return;
        select((currentIndex + 1) % TREES.size());
        refreshBoM();
    }
    public static void previous() {
        if (TREES.isEmpty()) return;
        select((currentIndex - 1 + TREES.size()) % TREES.size());
        refreshBoM();
    }
    public static void delTree() {
        if (TREES.isEmpty()) return;
        int oldCurrent = currentIndex;
        delete(oldCurrent);
        if (!TREES.isEmpty()) {
            int newIndex = (oldCurrent - 1 + TREES.size()) % TREES.size();
            select(newIndex);
        }
        refreshBoM();
    }
    public static void refreshBoM() {
        Minecraft client = Minecraft.getInstance();
        if (client.screen instanceof BoMScreen) {
            ((BoMScreen) client.screen).recalculateTree();
        }
    }

    // En ForestManager
    public static String getCustomName(int index) {
        if (index >= 0 && index < CUSTOM_NAMES.size()) {
            return CUSTOM_NAMES.get(index);
        }
        return null;
    }
}