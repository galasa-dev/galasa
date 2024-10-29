/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.windows.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.spi.IJavaManagerSpi;
import dev.galasa.java.windows.IJavaWindowsInstallation;
import dev.galasa.java.windows.JavaWindowsInstallation;
import dev.galasa.java.windows.JavaWindowsManagerException;
import dev.galasa.java.windows.JavaWindowsManagerField;
import dev.galasa.java.windows.spi.IJavaWindowsManagerSpi;
import dev.galasa.java.windows.spi.JavaWindowsInstallationImpl;
import dev.galasa.windows.spi.IWindowsManagerSpi;

@Component(service = { IManager.class })
public class JavaWindowsManagerImpl extends AbstractManager implements IJavaWindowsManagerSpi {
    public final static String              NAMESPACE    = "javawindows";

    private final static Log                   logger       = LogFactory.getLog(JavaWindowsManagerImpl.class);

    private IJavaManagerSpi     javaManager;
    private IWindowsManagerSpi  windowsManager;
    
    private final HashMap<String, JavaWindowsInstallationImpl> installations = new HashMap<>();
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.
     * IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            // *** Check to see if any of our annotations are present in the test class
            // *** If there is, we need to activate
            List<AnnotatedField> ourFields = findAnnotatedFields(JavaWindowsManagerField.class);
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
        
        this.javaManager = addDependentManager(allManagers, activeManagers, galasaTest, IJavaManagerSpi.class);
        if (this.javaManager == null) {
            throw new JavaWindowsManagerException("The Java Manager is not available");
        }
        
        this.windowsManager = addDependentManager(allManagers, activeManagers, galasaTest, IWindowsManagerSpi.class);
        if (this.windowsManager == null) {
            throw new JavaWindowsManagerException("The Windows Manager is not available");
        }
    }
    
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        
        if (otherManager == this.windowsManager) {
            return true;
        }
        
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {

        // *** Auto generate the remaining fields
        generateAnnotatedFields(JavaWindowsManagerField.class);
    }
    
    @GenerateAnnotatedField(annotation = JavaWindowsInstallation.class)
    public IJavaWindowsInstallation createJavaInstallationField(Field field, List<Annotation> annotations) throws JavaWindowsManagerException {
        
        JavaWindowsInstallation annotation = field.getAnnotation(JavaWindowsInstallation.class);
        
        JavaWindowsInstallationImpl installation = this.installations.get(annotation.javaTag());
        if (installation != null) {
            return installation;
        }
        
        try {
            installation = new JavaWindowsInstallationImpl(this,
                    annotation.javaType(),
                    annotation.javaVersion(),
                    annotation.javaJvm(),
                    annotation.javaTag(),
                    annotation.imageTag());
        } catch (JavaManagerException e) {
            throw new JavaWindowsManagerException("Problem generating a Java Windows installation", e);
        }
        
        this.installations.put(annotation.javaTag(), installation);
        
        return installation;
    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        for(JavaWindowsInstallationImpl installation : this.installations.values()) {
            installation.build();
        }
    }
    
    @Override
    public void provisionDiscard() {
        for(JavaWindowsInstallationImpl installation : this.installations.values()) {
            installation.discard();
        }
    }

    public IJavaManagerSpi getJavaManager() {
        return this.javaManager;
    }

    public IWindowsManagerSpi getWindowsManager() {
        return this.windowsManager;
    }

}
