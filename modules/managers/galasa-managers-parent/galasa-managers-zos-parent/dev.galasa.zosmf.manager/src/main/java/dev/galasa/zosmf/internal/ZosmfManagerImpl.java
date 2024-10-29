/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosmf.internal;

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
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.zosmf.IZosmf;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.Zosmf;
import dev.galasa.zosmf.ZosmfException;
import dev.galasa.zosmf.ZosmfManagerException;
import dev.galasa.zosmf.internal.properties.ImageServers;
import dev.galasa.zosmf.internal.properties.SysplexServers;
import dev.galasa.zosmf.internal.properties.ZosmfPropertiesSingleton;
import dev.galasa.zosmf.spi.IZosmfManagerSpi;

@Component(service = { IManager.class })
public class ZosmfManagerImpl extends AbstractManager implements IZosmfManagerSpi {

    private static final Log logger = LogFactory.getLog(ZosmfManagerImpl.class);

    protected static final String NAMESPACE = "zosmf";

    private IZosManagerSpi  zosManager;
    private IHttpManagerSpi httpManager;    

    private final HashMap<String, IZosmf> taggedZosmfs = new HashMap<>();
    private final HashMap<String, IZosmf> zosmfs = new HashMap<>();

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosmfPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosmfManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosmfManagerField.class);
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
        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new ZosmfManagerException("The zOS Manager is not available");
        }
        this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (httpManager == null) {
            throw new ZosmfManagerException("The HTTP Manager is not available");
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
        generateAnnotatedFields(ZosmfManagerField.class);
    }

    @GenerateAnnotatedField(annotation=Zosmf.class)
    public IZosmf generateZosmf(Field field, List<Annotation> annotations) throws ZosmfManagerException {
        Zosmf annotationZosmf = field.getAnnotation(Zosmf.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosmf.imageTag(), "PRIMARY").toUpperCase();

        //*** Have we already generated this tag
        if (taggedZosmfs.containsKey(tag)) {
            return taggedZosmfs.get(tag);
        }

        // TODO this needs to be proper DSEd
        IZosImage zosImage = null;
        try {
            zosImage = getZosManager().getImageForTag(tag);
        } catch(Exception e) {
            throw new ZosmfManagerException("Unable to locate z/OS image for tag '" + tag + "'", e);
        }

        // TODO should be the DSE server id or provision one

        Map<String, IZosmf> possibleZosmfs = getZosmfs(zosImage);
        if (possibleZosmfs.isEmpty()) {
            throw new ZosmfManagerException("Unable to provision zOS/MF, no zOS/MF server defined for image tag '" + tag + "'");
        }

        IZosmf selected = possibleZosmfs.values().iterator().next();  // TODO do we want to randomise this?
        taggedZosmfs.put(tag, selected);

        return selected;
    }


    @Override
    public IZosmf newZosmf(String serverId) throws ZosmfException {
        if (zosmfs.containsKey(serverId)) {
            return this.zosmfs.get(serverId);
        }
        IZosmf zosmf = new ZosmfImpl(this, serverId);
        this.zosmfs.put(serverId, zosmf);
        return zosmf;
    }


    public Map<String, IZosmf> getZosmfs(@NotNull IZosImage zosImage) throws ZosmfManagerException {
        HashMap<String, IZosmf> possibleZosmfs = new HashMap<>();

        try {
            List<String> possibleServers = ImageServers.get(zosImage);
            if (possibleServers.isEmpty()) {
                possibleServers = SysplexServers.get(zosImage);
                if (possibleServers.isEmpty()) {
                    // Default to assume there is a zOS/MF server running on the same image on port 443
                    possibleServers = new ArrayList<String>(1);
                    possibleServers.add(zosImage.getImageID());
                }
            }


            for (String serverId : possibleServers) {
                IZosmf actualZosmf = this.zosmfs.get(serverId);

                if (actualZosmf == null) {
                    logger.trace("Retrieving zOS server " + serverId);
                    actualZosmf = newZosmf(serverId);
                    this.zosmfs.put(serverId, actualZosmf);
                }
                possibleZosmfs.put(serverId, actualZosmf);
            }
        } catch (ZosManagerException e) {
            throw new ZosmfManagerException("Unable to get zOSMF servers for image \"" + zosImage.getImageID() + "\"", e);
        }
        return possibleZosmfs;
    }


    @Override
    public IZosmfRestApiProcessor newZosmfRestApiProcessor(IZosImage image, boolean restrictToImage) throws ZosmfManagerException {
        if (restrictToImage) {
            Map<String, IZosmf> zosmfMap = getZosmfs(image);
            for(IZosmf zosmf : zosmfMap.values()) {
                if (zosmf.getImage().getImageID().equals(image.getImageID())) {
                    return new ZosmfRestApiProcessor(zosmfMap);
                }
            }
            throw new ZosmfManagerException("No zOSMF server configured on " + image.getImageID());
        }
        return new ZosmfRestApiProcessor(getZosmfs(image));
    }


    public IZosManagerSpi getZosManager() {
        return this.zosManager;
    }


    public IHttpManagerSpi getHttpManager() {
        return this.httpManager;
    }


}
