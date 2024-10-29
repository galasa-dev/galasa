/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.docker.DockerContainer;
import dev.galasa.docker.DockerContainerConfig;
import dev.galasa.docker.DockerManagerException;
import dev.galasa.docker.DockerProvisionException;
import dev.galasa.docker.DockerVolume;
import dev.galasa.docker.DockerEngine;
import dev.galasa.docker.IDockerContainer;
import dev.galasa.docker.IDockerContainerConfig;
import dev.galasa.docker.IDockerManager;
import dev.galasa.docker.IDockerVolume;
import dev.galasa.docker.IDockerEngine;
import dev.galasa.docker.internal.properties.DockerPropertiesSingleton;
import dev.galasa.docker.internal.properties.DockerRegistry;
import dev.galasa.docker.spi.IDockerManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.IHttpManager;
import dev.galasa.http.spi.IHttpManagerSpi;

/**
 * Docker manager implementation.
 * 
 * Extracting from the test class the two current annotations of @DockerEngine and @DockerContainer
 * 
 * @DockerEngine - where the containers will be running, set value in CPS (see properties)
 * @DockerContainer - define what container is to be run, image names mus be defined, tag can be set
 * 
 *   
 */
@Component(service = { IManager.class })
public class DockerManagerImpl extends AbstractManager implements IDockerManagerSpi {
    protected final String               NAMESPACE = "docker";
    private final static Log                    logger = LogFactory.getLog(DockerManagerImpl.class);
    private IFramework                          framework;
    private IHttpManagerSpi                   httpManager;
    private IArtifactManager                    artifactManager;
    private IDockerEnvironment                  dockerEnvironment;
    private List<DockerRegistryImpl>            registries = new ArrayList<DockerRegistryImpl>();
    private boolean                             required = false;

    /**
     * Initialies the DockerManager, adding the requirement of the HttpManager
     * 
     * Docker Environment is generated at this stage
     * 
     * @param framework - the galasa framework
     * @param allManagers - list of all the managers
     * @param activeManagers - list of all the active managers

     * @throws ManagerException
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {

        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields(DockerManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
        try {
            DockerPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new DockerManagerException("Failed to set the CPS with the Docker namespace", e);
        }

        this.framework = framework;
        dockerEnvironment = new DockerEnvironment(framework, this);
        logger.info("Docker manager intialised");

    }
    
    /**
     * Makes sure that the docker manager is added to the list of active managers, and adds the dependency on http manager.
     * 
     * @param allManagers - list of all the managers
     * @param activeManagers - list of the active managers
     * @throws ManagerException
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        this.required = true;

        if (activeManagers.contains(this)) {
			return;
		}
        activeManagers.add(this);
        httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (httpManager == null) {
            throw new DockerManagerException("The http manager is not available");
        }
        artifactManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
        if (artifactManager == null) {
            throw new DockerManagerException("The Artifact manager is not available");
        }
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if(otherManager instanceof IHttpManager) {
            return true;
        }
        return false;
    }

    /**
     * Generates the docker containers defined in the annotation inside the test class.
     * 
     * @param field
     * @param annotations
     * @return IDockerContainer
     * @throws DockerManagerException
     */
    @GenerateAnnotatedField(annotation = DockerContainer.class)
    public IDockerContainer generateDockerContainer(Field field, List<Annotation> annotations) throws DockerManagerException {
        DockerContainer annotationContainer = field.getAnnotation(DockerContainer.class);
        return this.provisionContainer(annotationContainer.dockerContainerTag(),
                                        annotationContainer.image(),
                                        annotationContainer.start(),
                                        annotationContainer.dockerEngineTag()); 
    }

    /**
     * Private method to provision the dockerContainer described by the generateDockerContainer() in the provisioned docker environment.
     *  
     * @param dockerContainerTag
     * @param image
     * @param start
     * @return
     * @throws DockerManagerException
     */
    @Override
    public IDockerContainer provisionContainer(String dockerContainerTag, String image, boolean start, String dockerEngineTag) throws DockerManagerException {
        try {
            return dockerEnvironment.provisionDockerContainer(dockerContainerTag, image, start, dockerEngineTag);
        } catch (DockerProvisionException e) {
            throw new DockerManagerException("Failed to provision Docker container tag: "+ dockerContainerTag, e);
        }
    }

    /**
     * Directs to the docker engine where the containers will be provisioned.
     * 
     * @param field
     * @param annotations
     * @return IDockerEngine
     * @throws DockerManagerException
     */
    @GenerateAnnotatedField(annotation = DockerEngine.class)
    public IDockerEngine generateDockerEngine(Field field, List<Annotation> annotations) throws DockerManagerException {
        DockerEngine annotationServer = field.getAnnotation(DockerEngine.class);
        return this.getDockerEngine(annotationServer.dockerEngineTag());
    }

    @GenerateAnnotatedField(annotation = DockerContainerConfig.class)
    public IDockerContainerConfig generateDockerContainerConfig(Field field, List<Annotation> annotations) throws DockerManagerException {
        List<IDockerVolume> volumes = new ArrayList<>();
        DockerContainerConfig config = field.getAnnotation(DockerContainerConfig.class);

        for (DockerVolume volumeAnnotation : config.dockerVolumes()) {
            volumes.add(generateDockerVolumme(volumeAnnotation));
        }
        return new DockerContainerConfigImpl(volumes);
    }

    @GenerateAnnotatedField(annotation =  DockerVolume.class)
    public IDockerVolume generateDockerVolumme(DockerVolume annotation) throws DockerManagerException {
        try {
            return dockerEnvironment.allocateDockerVolume(  annotation.existingVolumeName(), 
                                                            annotation.volumeTag(),
                                                            annotation.mountPath(),
                                                            annotation.dockerEngineTag(),
                                                            annotation.readOnly());
        } catch (DockerProvisionException e) {
            throw new DockerManagerException("Failed to allocate Docker volume", e);
        }
    }

    /**
     * Private method for retrieving the docker sever used by the docker environment
     * 
     * @return
     * @throws DockerManagerException
     */
    private IDockerEngine getDockerEngine(String dockerEngineTag) throws DockerManagerException {
        return dockerEnvironment.getDockerEngineImpl(dockerEngineTag);
    }
    
    protected IHttpManagerSpi getHttpManager() {
    	return this.httpManager;
    }

    /**
     * Provision generate step. Extracts the docker manager realted annotations and generates the resources
     * 
     * @throws ManagerException
     */
    @Override
    public void provisionGenerate() throws ResourceUnavailableException, ManagerException {
        logger.info("Registering Docker registries");
        registerDockerRegistires();
        logger.info("Finding all Docker related annotations");
        generateDockerFields();
    }

    /**
     * Used in the provision generate step to extract annotations from the test class.
     * 
     * @throws ManagerException
     */
    private void generateDockerFields() throws ResourceUnavailableException, ManagerException {
        List<AnnotatedField> annotatedFields = findAnnotatedFields(DockerManagerField.class);

        for (AnnotatedField annotatedField: annotatedFields) {
            final Field field = annotatedField.getField();
            final List<Annotation> annotations = annotatedField.getAnnotations();

            if (field.getType() == IDockerManager.class) {
                registerAnnotatedField(field, this);
            } else if (field.getType() == IDockerEngine.class) {
                DockerEngine annotation = field.getAnnotation(DockerEngine.class);
                if (annotation != null) {
                    IDockerEngine dockerEngine = generateDockerEngine(field, annotations);
                    registerAnnotatedField(field, dockerEngine);
                }
            } else if (field.getType() == IDockerContainer.class) {
                DockerContainer annotation = field.getAnnotation(DockerContainer.class);
                if (annotation != null) {
                    IDockerContainer dockerContainer = generateDockerContainer(field, annotations);
                    registerAnnotatedField(field, dockerContainer);
                }
            } else if (field.getType() == IDockerContainerConfig.class) {
                DockerContainerConfig annotation = field.getAnnotation(DockerContainerConfig.class);
                if (annotation != null) {
                    IDockerContainerConfig config = generateDockerContainerConfig(field, annotations);
                    registerAnnotatedField(field, config);
                }
            }
        }
        generateAnnotatedFields(DockerManagerField.class);
    }
    
    /**
     * Used in the provision generate step to register all the docker registries from the CPS, if non specified docker hub is registered.
     * 
     * @throws DockerProvisionException
     */
    private void registerDockerRegistires() throws DockerProvisionException {
        try {
            String[] registryIds = DockerRegistry.get();

            for (String id: registryIds) {
                registries.add(new DockerRegistryImpl(framework, this, id));
            }
        } catch (Exception e) {
            throw new DockerProvisionException("Unable to resolve Docker registries: ", e);
        }
    }

    /**
     * Cleans and discards the docker environment, deleting docker containers not flagged to keep running.
     */
    @Override
    public void provisionStop() {
        try {
            dockerEnvironment.discard();
        } catch (DockerManagerException e) {
            logger.error("Unable to discard Docker environment", e);
        }

    }

    /**
     * Gets any container running in the docker environment via the image tag
     * 
     * @throws DockerManagerException
     */
    public IDockerContainer getDockerContainer(String dockerContainerTag) throws DockerManagerException {
        return dockerEnvironment.getDockerContainerImpl(dockerContainerTag);
    }

    /**
     * Frees up any slot in use from a docker container.
     * 
     * @param slot
     * @throws DockerProvisionException
     */
    public void freeDockerSlot(DockerSlotImpl slot) throws DockerProvisionException {
        dockerEnvironment.freeDockerSlot(slot);
    }

    /**
     * Returns a list of all the registered registries used by the docker container.
     * 
     * @return registries
     */
	public List<DockerRegistryImpl> getRegistries() {
		return this.registries;
	}

    @Override
    public @NotNull String getEngineHostname(String dockerEngineTag) throws DockerManagerException {
    	try {
        URI dockerEngine = dockerEnvironment.getDockerEngineImpl(dockerEngineTag).getURI();
        return dockerEngine.getScheme() + "://" + dockerEngine.getHost();
    	} catch (URISyntaxException e) {
    		throw new DockerManagerException("Failed to parse the found URI", e);
    	}
    }

    public IArtifactManager getArtifactManager() {
        return this.artifactManager;
    }

}