/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.obr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resource;
import org.apache.felix.bundlerepository.impl.DataModelHelperImpl;
import org.apache.felix.bundlerepository.impl.RepositoryImpl;
import org.apache.felix.bundlerepository.impl.ResourceImpl;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.file.RegularFile;
import org.gradle.api.logging.Logger;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskAction;

public class ObrBuildTask extends DefaultTask {

    private Path pathObr;

    private Project project = getProject();

    private Field requirementsField;

    public Logger logger = getLogger();

    @TaskAction
    public void genobr() throws Exception {
        logger.debug("Building OBR");

        try {
            this.requirementsField = ResourceImpl.class.getDeclaredField("m_reqList");
            this.requirementsField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new Exception("Unable to adjust the ResourceImpl class for a workaround a bug in Felix", e);
        }

        DataModelHelper obrDataModelHelper = new DataModelHelperImpl();
        RepositoryImpl newRepository = new RepositoryImpl();

        Configuration config = project.getConfigurations().getByName("bundle");
        for(ResolvedDependency dependency : config.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            processBundle(obrDataModelHelper, newRepository, dependency);
        }
        
        config = project.getConfigurations().getByName("obr");
        for(ResolvedDependency dependency : config.getResolvedConfiguration().getFirstLevelModuleDependencies()) {
            processObr(obrDataModelHelper, newRepository, dependency);
        }
        
        if (newRepository.getResources().length == 0) {
            throw new ObrException("No resources were added to the OBR");
        }   
        
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        newRepository.setLastModified(sdf.format(Calendar.getInstance().getTime()));
        newRepository.setName(project.getGroup().toString()+":"+project.getName()+":"+project.getVersion().toString());
        try (BufferedWriter writer = Files.newBufferedWriter(this.pathObr)) {
            obrDataModelHelper.writeRepository(newRepository, writer);
        } catch (Exception e) {
            throw new ObrException("Problem with writing OBR", e);
        }
        
        if (newRepository.getResources().length == 1) {
            logger.info("Repository created with " + newRepository.getResources().length
                    + " resource stored in " + this.pathObr.toAbsolutePath().toString());
        } else {
            logger.info("Repository created with " + newRepository.getResources().length
                    + " resource stored in " + this.pathObr.toAbsolutePath().toString());
        }
    }

    private void processBundle(DataModelHelper obrDataModelHelper, RepositoryImpl newRepository, ResolvedDependency dependency) throws ObrException {
        String id = dependency.getName();
        logger.debug("Processing bundle " + id);
        
        Iterator<ResolvedArtifact> artifactIterator = dependency.getAllModuleArtifacts().iterator();
        if (!artifactIterator.hasNext()) {
            logger.warn("No artifacts found for " + id);
            return;
        }
        
        ResolvedArtifact artifact = artifactIterator.next();

        File file = artifact.getFile();
        
        String fileName = file.getName();
        if (!fileName.endsWith(".jar")) {
            logger.warn("Bundle " + id + " does not end with .jar, ignoring");
            return;
        }
        
        try {
            URI location = new URI("mvn:" + dependency.getModuleGroup() + "/" + dependency.getModuleName() + "/"
                    + dependency.getModuleVersion() + "/" + artifact.getType());
            
            ResourceImpl newResource = (ResourceImpl)obrDataModelHelper.createResource(file.toURI().toURL());
            if (newResource == null) {
                throw new ObrException("Problem with file '" + id + ". Not an OSGi bundle?");
            }

            // Cloned from the maven plugin in order to ensure consistancy in obrs
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
                        }
                    }
                }
            } catch(Throwable t) {
                throw new Exception("Unable to remove execution environment requirement", t);
            }

            newResource.put(Resource.URI, location);
            newRepository.addResource(newResource);

        } catch(ObrException e) {
            throw e;
        } catch(Exception e) {
            throw new ObrException("Unable to process bundle '" + id + "'", e);
        }
        
        if (artifactIterator.hasNext()) {
            logger.warn("Dependency " + id + " resolved to more than one artifact");
        }
        
    }
    
    
    private void processObr(DataModelHelper obrDataModelHelper, RepositoryImpl newRepository, ResolvedDependency dependency) throws ObrException {
        String id = dependency.getName();
        logger.warn("Processing OBR " + id);
        
        Iterator<ResolvedArtifact> artifactIterator = dependency.getAllModuleArtifacts().iterator();
        if (!artifactIterator.hasNext()) {
            logger.warn("No artifacts found for " + id);
            return;
        }
        
        ResolvedArtifact artifact = artifactIterator.next();

        File file = artifact.getFile();
        
        String fileName = file.getName();
        if (!fileName.endsWith(".obr")) {
            logger.warn("OBR " + id + " does end with .obr, ignoring");
            return;
        }
        
        try {
            try (FileReader fr = new FileReader(artifact.getFile())) {
                Repository mergeRepository = obrDataModelHelper.readRepository(fr);

                for (Resource resource : mergeRepository.getResources()) {
                    newRepository.addResource(resource);
                    logger.warn("Merged bundle " + resource.getPresentationName() + " - "
                            + resource.getId() + " to repository");
                }
            }
        } catch(ObrException e) {
            throw e;
        } catch(Exception e) {
            throw new ObrException("Unable to process OBR '" + id + "'", e);
        }
        
        if (artifactIterator.hasNext()) {
            logger.warn("Dependency " + id + " resolved to more than one artifact");
        }
    }



    public void apply() {
        // Run during apply phase, need to decide on the output file name
        Provider<RegularFile> obrFile = project.getLayout().getBuildDirectory().file("galasa.obr");
        this.pathObr = obrFile.get().getAsFile().toPath();
        getOutputs().file(pathObr);
        
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration bundleConfiguration = configurations.findByName("bundle");
        Configuration obrConfiguration = configurations.findByName("obr");
        
        getDependsOn().add(bundleConfiguration);
        getDependsOn().add(obrConfiguration);
    }

}
