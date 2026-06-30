package com.emiforest.manager;

import com.emiforest.tree.ForestTree;
import dev.emi.emi.api.recipe.EmiRecipe;

import java.util.UUID;

public class ForestProject {

    private final UUID id;
    private String name;
    private ForestTree tree;
    private EmiRecipe goal;

    public ForestProject(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ForestTree getTree() {
        return tree;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTree(ForestTree tree) {
        this.tree = tree;
    }
}