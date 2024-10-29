/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.hashes;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

public class GitHashBuildTask extends DefaultTask {

    private SourceSet sourceSet;
    private File gitHash;

    @TaskAction
    public void buildHash() throws Exception {

        //*** Ensure the meta dir exists
        gitHash.getParentFile().mkdirs();
        
        HashesExtension extension = getProject().getExtensions().findByType(HashesExtension.class);
        
        String gitHash = "unknown";
        if (extension != null && extension.gitHash != null && !extension.gitHash.trim().isEmpty()) {
            gitHash = extension.gitHash;
        }
        
        Path path = Paths.get(this.gitHash.toURI());
        Files.write(path, gitHash.getBytes(StandardCharsets.UTF_8));
        
    }

    public void apply() {
        
        SourceSetContainer sourceSets = getProject().getExtensions().getByType(SourceSetContainer.class);
        this.sourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

        //*** If any source file has changed, we must rerun this task
        getInputs().files(this.sourceSet.getAllSource().getFiles());
        
        //*** We are dependent on the generic Classes task
        Task classes = getProject().getTasks().getByName("classes");
        getDependsOn().add(classes);

        //*** Make sure the jar task is dependent on us
        Jar jarTask = (Jar) getProject().getTasks().getByName("jar");
        jarTask.getDependsOn().add(this);

        //*** Create the directories so Gradle does not moan)
        Directory dirHashes = getProject().getLayout().getBuildDirectory().dir("hashes").get();
        Directory dirHashesMeta = dirHashes.dir("META-INF");

        this.gitHash = dirHashesMeta.file("git.hash").getAsFile();

        //*** Tell the JAR task we want to it to include the meta-inf and json file
        jarTask.from(dirHashes);
        //*** Mark the meta-inf file as output for caching purposes
        getOutputs().dir(dirHashes);
    }
}
