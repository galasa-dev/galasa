/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zostsocommand.ssh.manager.internal;

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
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zostsocommand.IZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommand;
import dev.galasa.zostsocommand.ZosTSOCommandField;
import dev.galasa.zostsocommand.ZosTSOCommandManagerException;
import dev.galasa.zostsocommand.spi.IZosTSOCommandSpi;
import dev.galasa.zostsocommand.ssh.manager.internal.properties.TsocmdPath;
import dev.galasa.zostsocommand.ssh.manager.internal.properties.ZosTSOCommandSshPropertiesSingleton;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;

/**
 * zOS TSO Command Manager implemented using ssh
 *
 */
@Component(service = { IManager.class })
public class ZosTSOCommandManagerImpl extends AbstractManager implements IZosTSOCommandSpi {
    protected static final String NAMESPACE = "zostsocommand";

    protected IZosManagerSpi zosManager;
    public void setZosManager(IZosManagerSpi zosManager) {
        this.zosManager = zosManager;
    }

    protected IZosUNIXCommandSpi zosUnixCommandManager;
    public void setZosUnixCommandManager(IZosUNIXCommandSpi zosUnixCommandManager) {
        this.zosUnixCommandManager = zosUnixCommandManager;
    }

    private final HashMap<String, ZosTSOCommandImpl> taggedZosTSOCommands = new HashMap<>();
    private final HashMap<IZosImage, ZosTSOCommandImpl> zosTSOCommands = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            ZosTSOCommandSshPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosTSOCommandManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosTSOCommandField.class);
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
        generateAnnotatedFields(ZosTSOCommandField.class);
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
            throw new ZosTSOCommandManagerException("The zOS Manager is not available");
        }
        setZosUnixCommandManager(addDependentManager(allManagers, activeManagers, galasaTest, IZosUNIXCommandSpi.class));
        if (zosUnixCommandManager == null) {
            throw new ZosTSOCommandManagerException("The zOS UNIX Command Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi || otherManager instanceof IZosUNIXCommandSpi;
    }

    @GenerateAnnotatedField(annotation=ZosTSOCommand.class)
    public IZosTSOCommand generateZosTSOCommand(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosTSOCommand annotationZosTSOCommand = field.getAnnotation(ZosTSOCommand.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosTSOCommand.imageTag(), "PRIMARY").toUpperCase();

        //*** Have we already generated this tag
        if (this.taggedZosTSOCommands.containsKey(tag)) {
            return this.taggedZosTSOCommands.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosTSOCommand zosTSOCommand = new ZosTSOCommandImpl(getZosUNIXCommand(image), getTsocmdPath(image));
        this.taggedZosTSOCommands.put(tag, (ZosTSOCommandImpl) zosTSOCommand);
        
        return zosTSOCommand;
    }

    @Override
    public @NotNull IZosTSOCommand getZosTSOCommand(IZosImage image) throws ZosTSOCommandManagerException {
        if (this.zosTSOCommands.containsKey(image)) {
            return this.zosTSOCommands.get(image);
        }

        ZosTSOCommandImpl zosTSO = new ZosTSOCommandImpl(getZosUNIXCommand(image), getTsocmdPath(image));
        this.zosTSOCommands.put(image, zosTSO);
        
        return zosTSO;
    }
    
	protected IZosUNIXCommand getZosUNIXCommand(IZosImage image) {
		return this.zosUnixCommandManager.getZosUNIXCommand(image);
	}

	protected String getTsocmdPath(IZosImage image) throws ZosTSOCommandManagerException {
		return TsocmdPath.get(image.getImageID());
	}
}
