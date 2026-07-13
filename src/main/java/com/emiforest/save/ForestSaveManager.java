package com.emiforest.save;

import com.emiforest.forest.ForestManager;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.bom.MaterialTree;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ForestSaveManager {

    private static File getFile() {
        String worldId = WorldSaveIdentifier.getCurrentId();
        File folder = new File(Minecraft.getInstance().gameDirectory, "emiforest/saves/" + worldId);
        if (!folder.exists()) folder.mkdirs();
        return new File(folder, "trees.json");
    }

    public static void saveTrees() {
        List<SavedTree> toSave = new ArrayList<>();
        List<MaterialTree> trees = ForestManager.getTrees();

        for (int i = 0; i < trees.size(); i++) {
            MaterialTree tree = trees.get(i);
            if (tree.goal == null || tree.goal.recipe == null) continue;

            String recipeId = tree.goal.recipe.getId().toString();
            long batches = tree.batches;
            String customName = ForestManager.getCustomName(i);

            toSave.add(new SavedTree("Tree", customName, recipeId, batches));
        }

        ForestSerializer.save(getFile(), toSave);
        System.out.println("[EMI Forest] Guardados " + toSave.size() + " árboles en " + getFile().getAbsolutePath());
    }

    public static void loadAndPopulate() {
        File file = getFile();
        List<SavedTree> savedTrees = ForestSerializer.load(file);
        System.out.println("[EMI Forest] Intentando cargar " + savedTrees.size() + " árboles desde " + file.getAbsolutePath());

        ForestManager.deleteAll();

        if (savedTrees.isEmpty()) {
            ForestManager.refreshBoM();
            return;
        }

        for (SavedTree saved : savedTrees) {
            ResourceLocation id = ResourceLocation.tryParse(saved.getRecipeId());
            if (id == null) {
                System.err.println("[EMI Forest] ID inválido: " + saved.getRecipeId());
                continue;
            }

            EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
            if (recipe == null) {
                System.err.println("[EMI Forest] Receta no encontrada: " + id);
                continue;
            }

            MaterialTree tree = new MaterialTree(recipe);
            // Restaurar batches guardados
            tree.batches = saved.getBatches();
            ForestManager.addTree(tree);

            if (saved.getCustomName() != null) {
                int index = ForestManager.getTrees().size() - 1;
                ForestManager.setCustomName(index, saved.getCustomName());
            }
        }

        if (!ForestManager.getTrees().isEmpty()) {
            ForestManager.select(ForestManager.getTrees().size() - 1);
        }
        ForestManager.refreshBoM();
        System.out.println("[EMI Forest] Arboles restaurados: " + ForestManager.getTrees().size());
    }
}