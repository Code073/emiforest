package com.emiforest;

import com.emiforest.manager.ForestProject;
import com.emiforest.manager.ForestWorkspace;
import com.emiforest.manager.ForestWorkspaceManager;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod(EmiForest.MOD_ID)
public class EmiForest {
    public static final String MOD_ID = "emiforest";

    public EmiForest() {

        ForestWorkspaceManager manager = new ForestWorkspaceManager();

        ForestProject project = new ForestProject(
                UUID.randomUUID(),
                "Steel Factory"
        );

        manager.getWorkspace().addProject(project);

        ForestWorkspace workspace = manager.getWorkspace();

        workspace.createProject("Steel");
        workspace.createProject("Motors");
        workspace.createProject("Circuits");
        workspace.createProject("MV");
        workspace.createProject("MV");
        workspace.createProject("LV");
        workspace.createProject("HV");
        workspace.createProject("EV");

        workspace.printWorkspace();

    }
}