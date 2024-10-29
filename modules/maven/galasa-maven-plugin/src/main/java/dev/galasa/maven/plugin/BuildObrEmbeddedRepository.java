/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "obrembedded", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true, requiresDependencyCollection = ResolutionScope.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class BuildObrEmbeddedRepository extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File         outputDirectory;

    private Path         outputRepositoryDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        DataModelHelper obrDataModelHelper = new DataModelHelperImpl();

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
        outputRepositoryDirectory = Paths.get(outputDirectory.toURI());

        RepositoryImpl newRepository = new RepositoryImpl();

        Set<Artifact> artifacts = project.getArtifacts();

        // *** Scan through looking for OBRs and extract all the valid bundle symbolic
        // names
        HashSet<String> validSymbolicNames = new HashSet<>();
        for (Artifact artifact : artifacts) {
            if (artifact.getScope().equals(Artifact.SCOPE_COMPILE) && "obr".equals(artifact.getType())) {
                processObr(obrDataModelHelper, artifact, validSymbolicNames);
            }
        }

        // *** Now process all dependencies and copy them to target, if they are a valid
        // symbolicname
        for (Artifact artifact : artifacts) {
            if (artifact.getScope().equals(Artifact.SCOPE_COMPILE) && !"obr".equals(artifact.getType())) {
                processBundle(obrDataModelHelper, newRepository, artifact, validSymbolicNames);
            }
        }

        Path repositoryFile = outputRepositoryDirectory.resolve("galasa.obr");
        try (FileWriter fw = new FileWriter(repositoryFile.toFile())) {
            obrDataModelHelper.writeRepository(newRepository, fw);
        } catch (Exception e) {
            throw new MojoExecutionException("Problem with writing repository.xml", e);
        }

    }

    private void processBundle(DataModelHelper obrDataModelHelper, RepositoryImpl newRepository, Artifact artifact,
            HashSet<String> validSymbolicNames) throws MojoExecutionException {

        ResourceImpl newResource;
        try {
            newResource = (ResourceImpl) obrDataModelHelper.createResource(artifact.getFile().toURI().toURL());
        } catch (Exception e) {
            getLog().warn("Ignoring artifact " + artifact.toString() + " as not a valid OSGi bundle");
            return;
        }

        if (newResource == null) {
            getLog().warn("Ignoring artifact " + artifact.toString() + " as not a valid OSGi bundle");
            return;
        }

        if (!validSymbolicNames.contains(newResource.getSymbolicName())) {
            getLog().warn("Ignoring artifact " + artifact.toString() + " as not on the valid symbolicname list");
            return;
        }

        try {
            Path artifactFile = Paths.get(artifact.getFile().toURI());
            Path targetFile = outputRepositoryDirectory.resolve(artifactFile.getFileName());

            Files.copy(artifactFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

            URI uri = new URI("file:" + targetFile.getFileName());
            newResource.put(Resource.URI, uri);

            newRepository.addResource(newResource);

            getLog().info("Added bundle " + newResource.getSymbolicName());
        } catch (Exception e) {
            throw new MojoExecutionException("Problem adding bundle to OBR reposiroty", e);
        }

    }

    private void processObr(DataModelHelper obrDataModelHelper, Artifact artifact, HashSet<String> validSymbolicNames)
            throws MojoExecutionException {
        try (FileReader fr = new FileReader(artifact.getFile())) {
            Repository mergeRepository = obrDataModelHelper.readRepository(fr);

            for (Resource resource : mergeRepository.getResources()) {
                validSymbolicNames.add(resource.getSymbolicName());
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read existing OBR", e);
        }
    }

}
