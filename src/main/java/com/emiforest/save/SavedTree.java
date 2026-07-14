package com.emiforest.save;

import com.google.gson.JsonElement;

import java.util.List;

public class SavedTree {

    private String name;
    private String customName;
    private String recipeId;
    private long batches;
    private List<SavedResolution> resolutions; // NUEVO

    public SavedTree(String name, String customName, String recipeId, long batches, List<SavedResolution> resolutions) {
        this.name = name;
        this.customName = customName;
        this.recipeId = recipeId;
        this.batches = batches;
        this.resolutions = resolutions;
    }

    public String getName() { return name; }
    public String getCustomName() { return customName; }
    public String getRecipeId() { return recipeId; }
    public long getBatches() { return batches; }
    public List<SavedResolution> getResolutions() { return resolutions; }

    public static class SavedResolution {
        private JsonElement ingredient;
        private String recipeId;

        public SavedResolution(JsonElement ingredient, String recipeId) {
            this.ingredient = ingredient;
            this.recipeId = recipeId;
        }

        public JsonElement getIngredient() { return ingredient; }
        public String getRecipeId() { return recipeId; }
    }
}