package com.emiforest.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ForestWorkspace {

    private final List<ForestProject> projects;
    private ForestProject activeProject;

    public ForestWorkspace() {
        this.projects = new ArrayList<>();
    }

    public void addProject(ForestProject project) {
        projects.add(project);
    }

    public void removeProject(ForestProject project) {
        projects.remove(project);
    }

    public List<ForestProject> getProjects() {
        return Collections.unmodifiableList(projects);
    }

    public ForestProject getActiveProject() {
        return activeProject;
    }

    public void setActiveProject(ForestProject activeProject) {
        this.activeProject = activeProject;
    }

    public ForestProject createProject(String name) {
        ForestProject project = new ForestProject(UUID.randomUUID(), name);
        projects.add(project);
        return project;
    }

    public void printWorkspace() {

        System.out.println("===== EMI Forest Workspace =====");

        for (ForestProject project : projects) {
            System.out.println("- " + project.getName());
        }

        System.out.println("===== EMI Forest Workspace =====");

    }
}