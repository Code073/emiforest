package com.emiforest.tree;

import dev.emi.emi.bom.MaterialTree;

public class ForestTree {

    private final MaterialTree materialTree;

    public ForestTree(MaterialTree materialTree) {
        this.materialTree = materialTree;
    }

    public MaterialTree getMaterialTree() {
        return materialTree;
    }
}