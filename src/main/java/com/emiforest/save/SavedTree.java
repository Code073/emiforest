package com.emiforest.save;

public class SavedTree {

    private String name;        // "Tree" o similar, podrías usarlo como nombre por defecto
    private String customName;  // NUEVO: nombre puesto por el usuario, puede ser null
    private String recipeId;
    private long batches;

    public SavedTree(String name, String customName, String recipeId, long batches) {
        this.name = name;
        this.customName = customName;
        this.recipeId = recipeId;
        this.batches = batches;
    }

    // getters
    public String getName() { return name; }
    public String getCustomName() { return customName; }
    public String getRecipeId() { return recipeId; }
    public long getBatches() { return batches; }
}