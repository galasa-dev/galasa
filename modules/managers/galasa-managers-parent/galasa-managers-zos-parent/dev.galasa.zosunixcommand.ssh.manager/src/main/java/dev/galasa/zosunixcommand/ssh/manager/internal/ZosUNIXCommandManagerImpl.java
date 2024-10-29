/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosunixcommand.ssh.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.ipnetwork.spi.IIpNetworkManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosunixcommand.ssh.manager.internal.properties.ZosUNIXCommandSshPropertiesSingleton;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandManagerException;
import dev.galasa.zosunixcommand.ZosUNIXCommandField;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;

/**
 * zOS UNIX Manager implemented using ssh
 *
 */
@Component(service = { IManager.class })
public class ZosUNIXCommandManagerImpl extends AbstractManager implements IZosUNIXCommandSpi {
    protected static final String NAMESPACE = "zosunixcommand";

    protected IZosManagerSpi zosManager;
    public void setZosManager(IZosManagerSpi zosManager) {
        this.zosManager = zosManager;
    }

    protected IIpNetworkManagerSpi ipNetworkManager;
    public void setIpNetworkManager(IIpNetworkManagerSpi ipNetworkManager) {
        this.ipNetworkManager = ipNetworkManager;
    }

    private final HashMap<String, ZosUNIXCommandImpl> taggedZosUNIXCommands = new HashMap<>();
    private final HashMap<IZosImage, ZosUNIXCommandImpl> zosUNIXCommands = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosUNIXCommandSshPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosUNIXCommandManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosUNIXCommandField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }
    

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosUNIXCommandField.class);
    }


    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#youAreRequired()
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        setZosManager(addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class));
        if (zosManager == null) {
            throw new ZosUNIXCommandManagerException("The zOS Manager is not available");
        }
        setIpNetworkManager(addDependentManager(allManagers, activeManagers, galasaTest, IIpNetworkManagerSpi.class));
        if (ipNetworkManager == null) {
            throw new ZosUNIXCommandManagerException("The IP Network Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi || otherManager instanceof IIpNetworkManagerSpi;
    }

    @GenerateAnnotatedField(annotation=ZosUNIXCommand.class)
    public IZosUNIXCommand generateZosUNIXCommand(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosUNIXCommand annotationZosUNIXCommand = field.getAnnotation(ZosUNIXCommand.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosUNIXCommand.imageTag(), "PRIMARY").toUpperCase();;

        //*** Have we already generated this tag
        if (this.taggedZosUNIXCommands.containsKey(tag)) {
            return this.taggedZosUNIXCommands.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosUNIXCommand zosUNIXCommand = new ZosUNIXCommandImpl(this.ipNetworkManager, image);
        this.taggedZosUNIXCommands.put(tag, (ZosUNIXCommandImpl) zosUNIXCommand);
        
        return zosUNIXCommand;
    }


    @Override
    public @NotNull IZosUNIXCommand getZosUNIXCommand(IZosImage image) {
        //*** Have we already generated this image
        if (this.zosUNIXCommands.containsKey(image)) {
            return this.zosUNIXCommands.get(image);
        }

        IZosUNIXCommand zosUNIXCommand = new ZosUNIXCommandImpl(this.ipNetworkManager, image);
        this.zosUNIXCommands.put(image, (ZosUNIXCommandImpl) zosUNIXCommand);
        
        return zosUNIXCommand;
    }
}
