package com.emiforest.forest;

import dev.emi.emi.bom.BoM;
import dev.emi.emi.bom.MaterialNode;
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


    public static void addTree(MaterialTree tree) {

        TREES.add(tree);
        CUSTOM_NAMES.add(null);
        currentIndex = TREES.size() - 1;

        System.out.println("========================================");
        System.out.println("[EMI Forest] Arbol añadido");
        System.out.println("Trees   : " + TREES.size());
        System.out.println("Current : " + currentIndex);
        System.out.println("========================================");

        printCurrent();
    }

    public static MaterialTree getCurrentTree() {

        if (currentIndex < 0 || currentIndex >= TREES.size()) {
            return null;
        }

        return TREES.get(currentIndex);
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
    }

    public static void select(int index) {

        if (index < 0 || index >= TREES.size()) {
            return;
        }

        currentIndex = index;

        BoM.tree = TREES.get(currentIndex);

        printCurrent();
    }

    public static void delete(int index) {
        if (index < 0 || index >= TREES.size()) return;

        // 1. Eliminar el árbol de la lista
        TREES.remove(index);
        CUSTOM_NAMES.remove(index);   //

        // 2. Ajustar el índice actual
        if (TREES.isEmpty()) {
            currentIndex = -1;
            BoM.tree = null;
        } else {
            // Si el índice borrado estaba antes del actual, el actual se desplaza -1
            if (index < currentIndex) {
                currentIndex--;
            } else if (index == currentIndex) {
                // El árbol actual se ha borrado: mover al siguiente disponible (o último)
                currentIndex = Math.min(currentIndex, TREES.size() - 1);
            }
            // Si index > currentIndex, no hay que modificar currentIndex
            BoM.tree = TREES.get(currentIndex);
        }

        // 3. Refrescar la interfaz si procede
        refreshBoM();
        printCurrent();
    }

    public static void deleteAll() {
        TREES.clear();
        CUSTOM_NAMES.clear();
        currentIndex = -1;
        BoM.tree = null;
        refreshBoM();
        System.out.println("[EMI Forest] Todos los árboles han sido eliminados.");
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

        int oldCurrent = currentIndex;          // guardamos el índice antes de borrar
        delete(oldCurrent);                     // eliminamos el árbol actual

        if (!TREES.isEmpty()) {
            // Queremos mostrar el árbol anterior al que acabamos de borrar
            int newIndex = (oldCurrent - 1 + TREES.size()) % TREES.size();
            select(newIndex);
        }
        // refreshBoM ya se llamó dentro de delete, pero por seguridad:
        refreshBoM();
    }

    public static void refreshBoM() {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.screen instanceof BoMScreen) {
            ((BoMScreen) client.screen).recalculateTree();
        }
    }

    public static void printCurrent() {

        MaterialTree tree = getCurrentTree();

        if (tree == null) {
            System.out.println("[EMI Forest] No hay árbol seleccionado.");
            return;
        }

        System.out.println();
        System.out.println("========================================");
        System.out.println("[EMI Forest] Arbol seleccionado");
        System.out.println("========================================");

        System.out.println("Index            : " + currentIndex);
        System.out.println("Tree Hash        : " + System.identityHashCode(tree));
        System.out.println("Goal Hash        : " + System.identityHashCode(tree.goal));

        if (tree.goal.children != null) {
            System.out.println("Children Hash    : " + System.identityHashCode(tree.goal.children));
            System.out.println("Children Count   : " + tree.goal.children.size());
        } else {
            System.out.println("Children         : null");
        }

        if (tree.goal.recipe != null) {
            System.out.println("Recipe           : " + tree.goal.recipe.getId());
        }

        System.out.println("Ingredient       : " + tree.goal.ingredient);
        System.out.println("Batches          : " + tree.batches);
        System.out.println("Crafting Mode    : " + BoM.craftingMode);

        System.out.println();
        System.out.println("============= TREE =============");

        printNode(tree.goal, 0);

        System.out.println("========== END TREE ============");
        System.out.println();
    }

    private static void printNode(MaterialNode node, int depth) {

        for (int i = 0; i < depth; i++) {
            System.out.print("    ");
        }

        String recipe = "null";

        if (node.recipe != null && node.recipe.getId() != null) {
            recipe = node.recipe.getId().toString();
        }

        System.out.println(
                "- " + node.ingredient +
                        " | amount=" + node.amount +
                        " | divisor=" + node.divisor +
                        " | recipe=" + recipe
        );

        if (node.children == null) {
            return;
        }

        for (MaterialNode child : node.children) {
            printNode(child, depth + 1);
        }
    }
}