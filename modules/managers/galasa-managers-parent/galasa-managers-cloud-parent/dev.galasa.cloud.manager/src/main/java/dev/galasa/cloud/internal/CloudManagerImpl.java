/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cloud.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.cloud.CloudContainer;
import dev.galasa.cloud.CloudContainerEnvProp;
import dev.galasa.cloud.CloudContainerPort;
import dev.galasa.cloud.CloudManagerException;
import dev.galasa.cloud.CloudManagerField;
import dev.galasa.cloud.ICloudContainer;
import dev.galasa.cloud.internal.properties.CloudPropertiesSingleton;
import dev.galasa.cloud.internal.properties.ContainerOverrideImage;
import dev.galasa.cloud.internal.properties.ContainerOverridePlatform;
import dev.galasa.cloud.internal.properties.DefaultPlatform;
import dev.galasa.cloud.spi.ICloudContainerPort;
import dev.galasa.cloud.spi.ICloudContainerProvider;
import dev.galasa.cloud.spi.ICloudManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

/**
 * The Cloud Manager
 * 
 *  
 *
 */
@Component(service = { IManager.class })
public class CloudManagerImpl extends AbstractManager implements ICloudManagerSpi {

	public final static String                  NAMESPACE = "cloud";
	private final Log                           logger = LogFactory.getLog(getClass());
	private IDynamicStatusStoreService          dss;
	
	private final HashSet<ICloudContainerProvider> containerProviders = new HashSet<>();
	
	private final HashMap<String, ICloudContainer> cloudContainers = new HashMap<>();

	private boolean                             required           = false;

	/**
	 * Initialise the Manager if the Cloud TPI is included in the test class
	 */
	@Override
	public void initialise(@NotNull IFramework framework, 
			@NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, 
			@NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);

		if(galasaTest.isJava()) {
			//*** Check to see if we are needed
			if (!required) {
				List<AnnotatedField> fields = findAnnotatedFields(CloudManagerField.class);
				if (!fields.isEmpty()) {
					required = true;
				}
			}

			if (!required) {
				return;
			}
		}

		youAreRequired(allManagers, activeManagers, galasaTest);

		try {
			CloudPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
		} catch (ConfigurationPropertyStoreException e) {
			throw new CloudManagerException("Failed to set the CPS with the Cloud namespace", e);
		}

		try {
			this.dss = this.getFramework().getDynamicStatusStoreService(NAMESPACE);
		} catch(DynamicStatusStoreException e) {
			throw new CloudManagerException("Unable to provide the DSS for the Cloud Manager", e);
		}

		this.logger.info("Cloud Manager initialised");
	}

	/**
	 * This or another manager has indicated that this manager is required
	 */
	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
			throws ManagerException {
		super.youAreRequired(allManagers, activeManagers, galasaTest);

		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
	}

	@Override
	public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
		return super.areYouProvisionalDependentOn(otherManager);
	}

	/*
	 * By default we need to load any managers that could provision cloud resources for
	 * us, eg kubernetes
	 */
	@Override
	public List<String> extraBundles(@NotNull IFramework framework) throws CloudManagerException {
		String extraBundles = null;
		// Can't use our property singleton at this point as the Manager has not been initialised yet.
		try {    	
			IConfigurationPropertyStoreService cps = framework.getConfigurationPropertyService(NAMESPACE);    	
			extraBundles = cps.getProperty("bundle", "extra.managers");
		} catch(Exception e) {
			throw new CloudManagerException("Problem accessing the CPS for cloud.bundle.extra.managers", e);
		}
		List<String> bundles = AbstractManager.split(extraBundles);

		if (bundles.isEmpty()) {
			return null;
		}

		return bundles;
	}


	@Override
	public boolean doYouSupportSharedEnvironments() {
		return false; // Saying no until fully written and understand the problems
	}

	/**
	 * Generate all the annotated fields, uses the standard generate by method mechanism
	 */
	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		generateAnnotatedFields(CloudManagerField.class);
	}

	/**
	 * Generate a Cloud Container
	 * 
	 * @param field The test field
	 * @param annotations any annotations with the ecosystem
	 * @return a {@link dev.galasa.galasaecosystem.IKubernetesEcosystem} ecosystem
	 * @throws InsufficientResourcesAvailableException 
	 * @throws KubernetesManagerException if there is a problem generating a ecosystem
	 */
	@GenerateAnnotatedField(annotation = CloudContainer.class)
	public ICloudContainer generateKubernetesEcosystem(Field field, List<Annotation> annotations) throws CloudManagerException, InsufficientResourcesAvailableException {
		CloudContainer annotation = field.getAnnotation(CloudContainer.class);

		String tag = annotation.cloudContainerTag().trim().toUpperCase();
		if (tag.isEmpty()) {
			tag = "PRIMARY";
		}
		
		ICloudContainer cloudContainer = this.cloudContainers.get(tag);
		if (cloudContainer != null) {
			return cloudContainer;
		}
		
		// Get the platform we want to provision on
		String platform = ContainerOverridePlatform.get(tag);
		if (platform == null) {
			platform = DefaultPlatform.get();
		}
		
		// Get the image to use
		String image = ContainerOverrideImage.get(tag);
		if (image == null) {
			image = annotation.image();
		}
		
		// Convert the port numbers
		ArrayList<ICloudContainerPort> ports = new ArrayList<>();
		for(CloudContainerPort exposedPort : annotation.exposedPorts()) {
			CloudContainerPortImpl port = new CloudContainerPortImpl(exposedPort.name(),
					exposedPort.port(),
					exposedPort.type());
			ports.add(port);
		}
		
		CloudContainerPortImpl[] passPorts = ports.toArray(new CloudContainerPortImpl[ports.size()]);
		
		// Convert the environment properties
		Properties envProps = new Properties();
		for(CloudContainerEnvProp annEnvProp : annotation.environmentProperties()) {
			envProps.put(annEnvProp.name(), annEnvProp.value());
		}
		
		
		// Ask each of the providers to see if they can provision the cloud container
		for(ICloudContainerProvider provider : this.containerProviders) {
			try {
				ICloudContainer newContainer = provider.generateCloudContainer(tag, 
						platform, 
						image, 
						passPorts, 
						envProps, 
						annotation.runArguments());
				
				if (newContainer != null) {
					this.cloudContainers.put(tag, cloudContainer);
					logger.info("Cloud Container tagged " + tag + " has been provisioned in platform " + newContainer.getPlatform() + " by " + provider.getName());
					return cloudContainer;
				}
			} catch(InsufficientResourcesAvailableException e) {
				throw e;
			} catch(ManagerException e) {
				throw new CloudManagerException("Problem generating Cloud Container for tag " + tag, e);
			}
		}
		
		// If we get here then no provider has volunteered to provide the cloud container
		throw new CloudManagerException("Unable to locate a provider for Cloud Container tagged " + tag + " on platform " + platform);
	}

	@Override
	public void provisionBuild() throws ManagerException, ResourceUnavailableException {
		super.provisionBuild();
	}

	@Override
	public void provisionStop() {
		super.provisionStop();
	}

	@Override
	public void provisionDiscard() {
		super.provisionDiscard();
	}

	public IDynamicStatusStoreService getDss() {
		return this.dss;
	}

	@Override
	public ICloudContainer getCloudContainerByTag(@NotNull String tag) throws CloudManagerException {
		ICloudContainer container = this.cloudContainers.get(tag);
		if (container == null) {
			throw new CloudManagerException("Unknown tagged '" + tag + "' Cloud Container");
		}
		return container;
	}

	@Override
	public ICloudContainer generateCloudContainer(String tag, 
			@NotNull String platform, 
			@NotNull String image,
			@NotNull ICloudContainerPort[] ports, 
			Properties environmentProperties, 
			String[] runArguments, 
			boolean autoStart,
			int startOrder) throws CloudManagerException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Code has not been written yet");
	}

	@Override
	public void registerCloudContainerProvider(ICloudContainerProvider containerProvider) {
		this.containerProviders.add(containerProvider);
		logger.trace("The Cloud Container provider " + containerProvider.getName() + " has been registered");
	}

}
