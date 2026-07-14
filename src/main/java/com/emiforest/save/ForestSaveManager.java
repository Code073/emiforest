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
        System.out.println("=== [EMI Forest] saveTrees() INVOCADO ===");
        new Exception("Stacktrace de origen").printStackTrace();

        List<SavedTree> toSave = new ArrayList<>();
        List<MaterialTree> trees = ForestManager.getTrees();

        // Guardia: si no hay árboles en memoria pero el archivo actual SI tiene árboles guardados,
        // es sospechoso (posible evento fantasma o timing raro) — no sobrescribimos a ciegas.
        if (trees.isEmpty()) {
            File existing = getFile();
            List<SavedTree> onDisk = ForestSerializer.load(existing);
            if (!onDisk.isEmpty()) {
                System.err.println("[EMI Forest] saveTrees() llamado con 0 árboles en memoria, pero el archivo tiene " + onDisk.size() + ". Abortando guardado para evitar perdida de datos.");
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

            System.out.println("[DEBUG] Arbol '" + recipeId + "' -> resolutions.size() = " + tree.resolutions.size());
            for (Map.Entry<EmiIngredient, EmiRecipe> entry : tree.resolutions.entrySet()) {
                EmiRecipe res = entry.getValue();
                System.out.println("[DEBUG]   ingrediente=" + entry.getKey() + " receta=" + res + " id=" + (res != null ? res.getId() : "NULL"));
            }

            for (Map.Entry<EmiIngredient, EmiRecipe> entry : tree.resolutions.entrySet()) {
                EmiRecipe res = entry.getValue();
                if (res == null || res.getId() == null) continue; // resoluciones sin id (ej: EmiResolutionRecipe) no se pueden restaurar por id
                JsonElement ingredientJson = EmiIngredientSerializer.getSerialized(entry.getKey());
                if (ingredientJson == null) continue;
                resolutions.add(new SavedTree.SavedResolution(ingredientJson, res.getId().toString()));
            }

            toSave.add(new SavedTree("Tree", customName, recipeId, batches, resolutions));
        }

        ForestSerializer.save(getFile(), toSave);
        System.out.println("[EMI Forest] Guardados " + toSave.size() + " arboles en " + getFile().getAbsolutePath());
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