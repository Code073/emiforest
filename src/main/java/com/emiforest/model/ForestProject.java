package com.emiforest.model;
import dev.emi.emi.bom.MaterialTree;

import java.util.UUID;

public class ForestProject {

    private final UUID id;

    private String name;

    private MaterialTree tree;

    public ForestProject(UUID id, String name, MaterialTree tree) {
        this.id = id;
        this.name = name;
        this.tree = tree;
    }
}