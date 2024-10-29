/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.tests;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

/**
 * Build a Galasa test project
 */
public class TestsPlugin implements Plugin<Project> {
    
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        
        createTestCatalogBuildTask(project);
        
        
    }

    private void createTestCatalogBuildTask(Project project) {
        // Create the new Task, called gentestcatalog
        project.getTasks().create("gentestcatalog", TestCatalogBuildTask.class, tcTask -> {
            tcTask.apply();
        });
    }

}
