package com.emiforest.manager;

public class ForestWorkspaceManager {

    private final ForestWorkspace workspace;

    public ForestWorkspaceManager() {
        this.workspace = new ForestWorkspace();
    }

    public ForestWorkspace getWorkspace() {
        return workspace;
    }
}