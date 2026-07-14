package com.emiforest.save;

import com.emiforest.forest.ForestManager;
import com.google.gson.JsonElement;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.serializer.EmiIngredientSerializer;
import dev.emi.emi.bom.MaterialTree;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForestSaveManager {

    private static File getFile() {
        String worldId = WorldSaveIdentifier.getCurrentId();
        File folder = new File(Minecraft.getInstance().gameDirectory, "emiforest/saves/" + worldId);
        if (!folder.exists()) folder.mkdirs();
        return new File(folder, "trees.json");
    }

    public static void saveTrees() {
        saveTrees(false);
    }

    public static void saveTrees(boolean force) {
        List<SavedTree> toSave = new ArrayList<>();
        List<MaterialTree> trees = ForestManager.getTrees();

        // Safety guard: if memory has no trees but the file on disk does,
        // something is off (e.g. an event firing before state is ready) —
        // don't blindly overwrite existing data with an empty list, UNLESS
        // this is an intentional deletion (force = true).
        if (trees.isEmpty() && !force) {
            File existing = getFile();
            List<SavedTree> onDisk = ForestSerializer.load(existing);
            if (!onDisk.isEmpty()) {
                System.err.println("[EMI Forest] saveTrees() called with 0 trees in memory, but file has "
                        + onDisk.size() + ". Aborting to avoid data loss.");
                return;
            }
        }

        for (int i = 0; i < trees.size(); i++) {
            MaterialTree tree = trees.get(i);
            if (tree.goal == null || tree.goal.recipe == null) continue;

            String recipeId = tree.goal.recipe.getId().toString();
            long batches = tree.batches;
            String customName = ForestManager.getCustomName(i);

            List<SavedTree.SavedResolution> resolutions = new ArrayList<>();
            for (Map.Entry<EmiIngredient, EmiRecipe> entry : tree.resolutions.entrySet()) {
                EmiRecipe res = entry.getValue();
                if (res == null || res.getId() == null) continue;

                JsonElement ingredientJson = EmiIngredientSerializer.getSerialized(entry.getKey());
                if (ingredientJson == null) continue;

                resolutions.add(new SavedTree.SavedResolution(ingredientJson, res.getId().toString()));
            }

            toSave.add(new SavedTree("Tree", customName, recipeId, batches, resolutions));
        }

        ForestSerializer.save(getFile(), toSave);
    }

    /**
     * Intenta cargar y restaurar los árboles guardados.
     * @return true si la carga fue exitosa (o no había nada que cargar),
     *         false si falló y no se debe reintentar aún (por ejemplo, EMI no cargó recetas).
     *         En caso de false, NO se modifica el estado de ForestManager ni el archivo.
     */
    public static boolean loadAndPopulate() {
        File file = getFile();
        List<SavedTree> savedTrees = ForestSerializer.load(file);
        System.out.println("[EMI Forest] Intentando cargar " + savedTrees.size() + " árboles desde " + file.getAbsolutePath());

        if (savedTrees.isEmpty()) {
            return true;
        }

        List<MaterialTree> restoredTrees = new ArrayList<>();
        List<String> restoredNames = new ArrayList<>();

        for (SavedTree saved : savedTrees) {
            ResourceLocation id = ResourceLocation.tryParse(saved.getRecipeId());
            if (id == null) continue;

            EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(id);
            if (recipe == null) {
                System.err.println("[EMI Forest] Receta no encontrada: " + id);
                continue;
            }

            MaterialTree tree = new MaterialTree(recipe);
            tree.batches = saved.getBatches();

            if (saved.getResolutions() != null) {
                for (SavedTree.SavedResolution savedRes : saved.getResolutions()) {
                    EmiIngredient ingredient = EmiIngredientSerializer.getDeserialized(savedRes.getIngredient());
                    if (ingredient == null || ingredient.isEmpty()) {
                        System.err.println("[EMI Forest] Ingrediente inválido en resolución: " + savedRes.getIngredient());
                        continue;
                    }
                    ResourceLocation resId = ResourceLocation.tryParse(savedRes.getRecipeId());
                    if (resId == null) continue;
                    EmiRecipe resRecipe = EmiApi.getRecipeManager().getRecipe(resId);
                    if (resRecipe == null) {
                        System.err.println("[EMI Forest] Receta de resolución no encontrada: " + resId);
                        continue;
                    }
                    tree.addResolution(ingredient, resRecipe);
                }
            }
            System.out.println("[EMI Forest] Arbol '" + recipeId(id) + "' restaurado con " + tree.resolutions.size() + " resoluciones");

            restoredTrees.add(tree);
            restoredNames.add(saved.getCustomName());
        }

        if (restoredTrees.isEmpty()) {
            System.err.println("[EMI Forest] 0/" + savedTrees.size() + " recetas encontradas. Abortando sin modificar nada.");
            return false;
        }

        // CLAVE: activar modo bulk para que addTree NO guarde en cada iteración
        ForestManager.setBulkLoading(true);
        ForestManager.deleteAll();
        for (int i = 0; i < restoredTrees.size(); i++) {
            ForestManager.addTree(restoredTrees.get(i));
            if (restoredNames.get(i) != null) {
                ForestManager.setCustomName(ForestManager.getTrees().size() - 1, restoredNames.get(i));
            }
        }
        ForestManager.setBulkLoading(false);

        if (!ForestManager.getTrees().isEmpty()) {
            ForestManager.select(ForestManager.getTrees().size() - 1);
        }
        ForestManager.refreshBoM();
        System.out.println("[EMI Forest] Arboles restaurados: " + ForestManager.getTrees().size() + "/" + savedTrees.size());
        return true;
    }

    private static String recipeId(ResourceLocation id) {
        return id.toString();
    }
}