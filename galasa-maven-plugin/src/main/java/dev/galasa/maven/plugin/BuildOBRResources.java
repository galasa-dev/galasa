/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Set;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "obrresources", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true, requiresDependencyCollection = ResolutionScope.COMPILE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class BuildOBRResources extends AbstractMojo {

    public enum OBR_URL_TYPE {
        file,
        mvn
    }

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File         outputDirectory;

    @Parameter(defaultValue = "${project.build.directory}", property = "targerDir", required = true)
    private File         projectTargetDirectory;

    @Parameter(defaultValue = "${galasa.obr.url.type}", property = "obrUrlType", required = false)
    private OBR_URL_TYPE obrUrlType;

    @Parameter(defaultValue = "false", property = "includeSelf", required = false)
    private boolean      includeSelf;

    private Field requirementsField;

    @SuppressWarnings("deprecation")
    public void execute() throws MojoExecutionException, MojoFailureException {

        // give access to the requirements field in the ResourceImpl so we can remove the 
        // execution environment requirement as Felix has an outstanding bug
        try {
            this.requirementsField = ResourceImpl.class.getDeclaredField("m_reqList");
            this.requirementsField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new MojoExecutionException("Unable to adjust the ResourceImpl class for a workaround a bug in Felix", e);
        }

        getLog().info("BuildOBRResources: Building project " + project.getName() + ". includeSelf=" + includeSelf);
        if (obrUrlType == null) {
            obrUrlType = OBR_URL_TYPE.mvn;
        }

        DataModelHelper obrDataModelHelper = new DataModelHelperImpl();

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        File repositoryFile = new File(outputDirectory, "repository.obr");
        project.getArtifact().setFile(repositoryFile);

        if (includeSelf) {
            Artifact selfArtifact = new DefaultArtifact(project.getGroupId(), project.getArtifactId(),
                    project.getVersion(), Artifact.SCOPE_COMPILE, "jar", null,
                    project.getArtifact().getArtifactHandler());
            getLog().info("BuildOBRResources: Adding artifact " + selfArtifact.getId() + "as a dependency to "
                    + project.getName());
            selfArtifact.setResolved(true);
            selfArtifact.setFile(new File(projectTargetDirectory.getAbsolutePath() + "/" + project.getArtifactId() + "-"
                    + project.getVersion() + ".jar"));
            Set<Artifact> dependencyArtifacts = project.getDependencyArtifacts();
            dependencyArtifacts.add(selfArtifact);
            project.setDependencyArtifacts(dependencyArtifacts);
        }

        RepositoryImpl newRepository = new RepositoryImpl();

        for (Object dependency : project.getDependencyArtifacts()) {
            if (dependency instanceof DefaultArtifact) {
                DefaultArtifact artifact = (DefaultArtifact) dependency;

                if (artifact.isResolved() && artifact.getScope().equals("compile")) {

                    getLog().info("BuildOBRResources: Artifact resolved, and scope is compile. "+
                            " id:"+artifact.getId()+
                            " classifier:"+artifact.getClassifier()
                    );

                    File file = artifact.getFile();
                    if (file == null) {
                        throw new MojoFailureException("BuildOBRResources: Failed to process artifact. Null file handle."+
                            " id:"+artifact.getId()+
                            " classifier:"+artifact.getClassifier()
                        );
                    }

                    String name = file.getName();
                    if (name == null) {
                        throw new MojoFailureException("BuildOBRResources: Failed to process artifact. Null name"+
                            " id:"+artifact.getId()+
                            " classifier:"+artifact.getClassifier()
                        );
                    }

                    if (name.endsWith(".jar")) {
                        processBundle(artifact, newRepository, obrDataModelHelper);
                    } else if (name.endsWith(".obr")) {
                        processObr(artifact, newRepository, obrDataModelHelper);
                    }
                }
            }
        }

        if (newRepository.getResources() == null || newRepository.getResources().length == 0) {
            throw new MojoFailureException("No resources have been added to the repository");
        }

        getLog().info("BuildOBRResources: OBR Artifact ID is " + project.getArtifact().getId());

        newRepository.setName(project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        newRepository.setLastModified(sdf.format(Calendar.getInstance().getTime()));

        try {
            FileWriter fw = new FileWriter(repositoryFile);
            obrDataModelHelper.writeRepository(newRepository, fw);
            fw.close();
        } catch (Exception e) {
            throw new MojoExecutionException("Problem with writing repository.xml", e);
        }

        if (newRepository.getResources().length == 1) {
            getLog().info("BuildOBRResources: Repository created with " + newRepository.getResources().length
                    + " resource stored in " + repositoryFile.getAbsolutePath());
        } else {
            getLog().info("BuildOBRResources: Repository created with " + newRepository.getResources().length
                    + " resources stored in " + repositoryFile.getAbsolutePath());
        }
    }

    private void processObr(Artifact artifact, RepositoryImpl newRepository, DataModelHelper obrDataModelHelper)
            throws MojoExecutionException {

        try (FileReader fr = new FileReader(artifact.getFile())) {
            Repository mergeRepository = obrDataModelHelper.readRepository(fr);

            for (Resource resource : mergeRepository.getResources()) {
                newRepository.addResource(resource);
                getLog().info("BuildOBRResources: Merged bundle " + resource.getPresentationName() + " - "
                        + resource.getId() + " to repository");
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to read existing OBR", e);
        }
    }

    private void processBundle(DefaultArtifact artifact, RepositoryImpl repository, DataModelHelper obrDataModelHelper)
            throws MojoExecutionException {

        try {
            getLog().info("BuildOBRResources: Processing artifact " + artifact.getId());
            ResourceImpl newResource = (ResourceImpl) obrDataModelHelper
                    .createResource(artifact.getFile().toURI().toURL());
            if (newResource == null) {
                throw new MojoExecutionException("Problem with jar file. Not an OSGi bundle?");
            }

            // **** Extremely dodgy,  but no other way to do this at the moment due to 
            // **** https://issues.apache.org/jira/browse/FELIX-575
            try {
                @SuppressWarnings("unchecked")
                ArrayList<Requirement> requirements = (ArrayList<Requirement>) this.requirementsField.get(newResource);
                
                if (requirements != null) {
                    Iterator<Requirement> requirementi = requirements.iterator();
                    while(requirementi.hasNext()) {
                        Requirement requirement = requirementi.next();
                        if ("ee".equals(requirement.getName())) {
                            requirementi.remove();
                            getLog().info("Removed requirement from bundle - " + requirement.toString() + " due to https://issues.apache.org/jira/browse/FELIX-57");
                        }
                    }
                }
            } catch(Throwable t) {
                throw new MojoExecutionException("Unable to remove execution environment requirement", t);
            }

            URI name = null;
            switch (obrUrlType) {
                case mvn:
                    name = new URI("mvn:" + artifact.getGroupId() + "/" + artifact.getArtifactId() + "/"
                            + artifact.getBaseVersion() + "/" + artifact.getType());
                    break;
                case file:
                default:
                    name = artifact.getFile().toURI();
                    break;
            }
            newResource.put(Resource.URI, name);

            repository.addResource(newResource);

            getLog().info("BuildOBRResources: Added bundle " + newResource.getPresentationName() + " - "
                    + newResource.getId() + " to repository");
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to process dependency " + artifact.getGroupId() + ":"
                    + artifact.getArtifactId() + ":" + artifact.getVersion(), e);
        }
    }

}
