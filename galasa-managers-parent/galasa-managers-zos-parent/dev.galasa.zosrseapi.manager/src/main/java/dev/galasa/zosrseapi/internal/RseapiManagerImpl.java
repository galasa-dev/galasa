/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.Rseapi;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.internal.properties.ImageServers;
import dev.galasa.zosrseapi.internal.properties.RseapiPropertiesSingleton;
import dev.galasa.zosrseapi.internal.properties.SysplexServers;
import dev.galasa.zosrseapi.spi.IRseapiManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class })
public class RseapiManagerImpl extends AbstractManager implements IRseapiManagerSpi {
    
    private static final Log logger = LogFactory.getLog(RseapiManagerImpl.class);
    
    protected static final String NAMESPACE = "rseapi";
    
    protected IZosManagerSpi zosManager;
    public void setZosManager(IZosManagerSpi zosManager) {
	    this.zosManager = zosManager;
	}
	public IZosManagerSpi getZosManager() {
		return this.zosManager;
	}

	protected IHttpManagerSpi httpManager;    
    public void setHttpManager(IHttpManagerSpi httpManager) {
        this.httpManager = httpManager;
    }
    public IHttpManagerSpi getHttpManager() {
		return this.httpManager;
	}


	private final HashMap<String, IRseapi> taggedRseapis = new HashMap<>();
    private final HashMap<String, IRseapi> rseapis = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            RseapiPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new RseapiManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(RseapiManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }
    

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        setZosManager(addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class));
        if (zosManager == null) {
            throw new RseapiManagerException("The zOS Manager is not available");
        }
        setHttpManager(addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class));
        if (httpManager == null) {
            throw new RseapiManagerException("The HTTP Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi ||
               otherManager instanceof IHttpManagerSpi;
    }
    
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(RseapiManagerField.class);
    }
    
    @GenerateAnnotatedField(annotation=Rseapi.class)
    public IRseapi generateRseapi(Field field, List<Annotation> annotations) throws RseapiManagerException {
        Rseapi annotationRseapi = field.getAnnotation(Rseapi.class);

      //*** Default the tag to primary
        String tag = defaultString(annotationRseapi.imageTag(), "PRIMARY").toUpperCase();

        //*** Have we already generated this tag
        if (taggedRseapis.containsKey(tag)) {
            return taggedRseapis.get(tag);
        }

     // TODO this needs to be proper DSEd
        IZosImage zosImage = null;
        try {
            zosImage = getZosManager().getImageForTag(tag);
        } catch(Exception e) {
            throw new RseapiManagerException("Unable to locate z/OS image for tag '" + tag + "'", e);
        }

        // TODO should be the DSE server id or provision one

        Map<String, IRseapi> possibleRseapis = getRseapis(zosImage);
        if (possibleRseapis.isEmpty()) {
            throw new RseapiManagerException("Unable to provision RSE API server, no RSE API server defined for image tag '" + tag + "'");
        }

        IRseapi selected = possibleRseapis.values().iterator().next();  // TODO do we want to randomise this?
        taggedRseapis.put(tag, selected);

        return selected;
    }

    @Override
    public IRseapi newRseapi(String serverId) throws RseapiException {
        if (rseapis.containsKey(serverId)) {
            return this.rseapis.get(serverId);
        }
        IRseapi rseapi = new RseapiImpl(this, serverId);
        this.rseapis.put(serverId, rseapi);
        return rseapi;
    }

    public Map<String, IRseapi> getRseapis(@NotNull IZosImage zosImage) throws RseapiManagerException {
        HashMap<String, IRseapi> possibleRseapis = new HashMap<>();

        try {
            List<String> possibleServers = ImageServers.get(zosImage);
            if (possibleServers.isEmpty()) {
                possibleServers = SysplexServers.get(zosImage);
                if (possibleServers.isEmpty()) {
                    // Default to assume there is a RSE API server running on the same image on port 6800
                    possibleServers = new ArrayList<String>(1);
                    possibleServers.add(zosImage.getImageID());
                }
            }


            for (String serverId : possibleServers) {
            	IRseapi actualRseapi = this.rseapis.get(serverId);

                if (actualRseapi == null) {
                    logger.trace("Retrieving RSE API server " + serverId);
                    actualRseapi = newRseapi(serverId);
                    this.rseapis.put(serverId, actualRseapi);
                }
                possibleRseapis.put(serverId, actualRseapi);
            }
        } catch (ZosManagerException e) {
            throw new RseapiManagerException("Unable to get RSE API servers for image \"" + zosImage.getImageID() + "\"", e);
        }
        return possibleRseapis;
    }

    @Override
    public IRseapiRestApiProcessor newRseapiRestApiProcessor(IZosImage image, boolean restrictToImage) throws RseapiManagerException {
    	if (restrictToImage) {
            Map<String, IRseapi> rseapiMap = getRseapis(image);
            for(IRseapi rseapi : rseapiMap.values()) {
                if (rseapi.getImage().getImageID().equals(image.getImageID())) {
                    return new RseapiRestApiProcessor(rseapiMap);
                }
            }
            throw new RseapiManagerException("No RSE API server configured on " + image.getImageID());
        }
        return new RseapiRestApiProcessor(getRseapis(image));
    }
}
