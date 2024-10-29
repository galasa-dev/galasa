/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.oeconsol.manager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.ICredentials;
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
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsole;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosconsole.ZosConsoleField;
import dev.galasa.zosconsole.ZosConsoleManagerException;
import dev.galasa.zosconsole.oeconsol.manager.internal.properties.OeconsolPropertiesSingleton;
import dev.galasa.zosconsole.spi.IZosConsoleSpi;
import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.spi.IZosUNIXCommandSpi;

/**
 * zOS Console Manager implemented using zOS/MF
 *
 */
@Component(service = { IManager.class })
public class OeconsolZosConsoleManagerImpl extends AbstractManager implements IZosConsoleSpi {
    protected static final String NAMESPACE = "zosconsole";

    private IZosManagerSpi zosManager;
    public void setZosManager(IZosManagerSpi zosManager) {
        this.zosManager = zosManager;
    }
    public IZosManagerSpi getZosManager() {
    	return this.zosManager;
    }
    
    private IZosUNIXCommandSpi zosUnixCommandManager;
	public void setZosUnixCommandManager(IZosUNIXCommandSpi zosUnixCommandManager) {
		this.zosUnixCommandManager = zosUnixCommandManager;
	}
	
    public IZosUNIXCommand getZosUNIXCommand(IZosImage image) {
    	return this.zosUnixCommandManager.getZosUNIXCommand(image);
    }

	private final HashMap<String, OeconsolZosConsoleImpl> taggedZosConsoles = new HashMap<>();
    private final HashMap<String, OeconsolZosConsoleImpl> zosConsoles = new HashMap<>();
    
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            OeconsolPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new ZosConsoleManagerException("Unable to request framework services", e);
        }
        
        if(galasaTest.isJava()) {
            //*** Check to see if any of our annotations are present in the test class
            //*** If there is,  we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(ZosConsoleField.class);
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
        generateAnnotatedFields(ZosConsoleField.class);
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
            throw new ZosConsoleManagerException("The zOS Manager is not available");
        }
        setZosUnixCommandManager(addDependentManager(allManagers, activeManagers, galasaTest, IZosUNIXCommandSpi.class));
        if (zosUnixCommandManager == null) {
            throw new ZosFileManagerException("The zOS UNIX Command Manager is not available");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * io.ejat.framework.spi.IManager#areYouProvisionalDependentOn(io.ejat.framework
     * .spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi || otherManager instanceof IZosUNIXCommandSpi;
    }

    @GenerateAnnotatedField(annotation=ZosConsole.class)
    public IZosConsole generateZosConsole(Field field, List<Annotation> annotations) throws ZosManagerException {
        ZosConsole annotationZosConsole = field.getAnnotation(ZosConsole.class);

        //*** Default the tag to primary
        String tag = defaultString(annotationZosConsole.imageTag(), "PRIMARY").toUpperCase();

        //*** Have we already generated this tag
        if (this.taggedZosConsoles.containsKey(tag)) {
            return this.taggedZosConsoles.get(tag);
        }

        IZosImage image = zosManager.getImageForTag(tag);
        IZosConsole zosConsole = new OeconsolZosConsoleImpl(this, image);
        this.taggedZosConsoles.put(tag, (OeconsolZosConsoleImpl) zosConsole);
        
        return zosConsole;
    }

    @Override
    public @NotNull IZosConsole getZosConsole(IZosImage image) throws ZosConsoleManagerException {
        if (zosConsoles.containsKey(image.getImageID())) {
            return zosConsoles.get(image.getImageID());
        } else {
            OeconsolZosConsoleImpl zosConsole = new OeconsolZosConsoleImpl(this, image);
            zosConsoles.put(image.getImageID(), zosConsole);
            return zosConsole;
        }
    }
    
	public ICredentials getCredentials(String credentialsId, IZosImage image) throws ZosConsoleException {
		try {
			return this.zosManager.getCredentials(credentialsId, image.getImageID());
		} catch (ZosManagerException e) {
			throw new ZosConsoleException("oeconsol requires 'Console Name' to be a valid credentials id", e);
		}
	}
}
