/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.ubuntu.internal;

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
import dev.galasa.java.ubuntu.IJavaUbuntuInstallation;
import dev.galasa.java.ubuntu.JavaUbuntuInstallation;
import dev.galasa.java.ubuntu.JavaUbuntuManagerException;
import dev.galasa.java.ubuntu.JavaUbuntuManagerField;
import dev.galasa.java.ubuntu.spi.IJavaUbuntuManagerSpi;
import dev.galasa.java.ubuntu.spi.JavaUbuntuInstallationImpl;
import dev.galasa.linux.spi.ILinuxManagerSpi;


@Component(service = { IManager.class })
public class JavaUbuntuManagerImpl extends AbstractManager implements IJavaUbuntuManagerSpi {
    public final static String              NAMESPACE    = "javaubuntu";

    private final static Log                   logger       = LogFactory.getLog(JavaUbuntuManagerImpl.class);

    private IJavaManagerSpi   javaManager;
    private ILinuxManagerSpi  linuxManager;
    
    private final HashMap<String, JavaUbuntuInstallationImpl> installations = new HashMap<>();
    
    private Class<?> test;
    
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
            List<AnnotatedField> ourFields = findAnnotatedFields(JavaUbuntuManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
            this.test = galasaTest.getJavaTestClass();
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
            throw new JavaUbuntuManagerException("The Java Manager is not available");
        }
        
        this.linuxManager = addDependentManager(allManagers, activeManagers, galasaTest, ILinuxManagerSpi.class);
        if (this.linuxManager == null) {
            throw new JavaUbuntuManagerException("The Linux Manager is not available");
        }
    }
    
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        
        if (otherManager == this.linuxManager) {
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
        generateAnnotatedFields(JavaUbuntuManagerField.class);
    }
    
    @GenerateAnnotatedField(annotation = JavaUbuntuInstallation.class)
    public IJavaUbuntuInstallation createJavaInstallationField(Field field, List<Annotation> annotations) throws JavaUbuntuManagerException {
        
        JavaUbuntuInstallation annotation = field.getAnnotation(JavaUbuntuInstallation.class);
        
        JavaUbuntuInstallationImpl installation = this.installations.get(annotation.javaTag());
        if (installation != null) {
            return installation;
        }
        
        try {
            installation = new JavaUbuntuInstallationImpl(this,
                    annotation.javaType(),
                    annotation.javaVersion(),
                    annotation.javaJvm(),
                    annotation.javaTag(),
                    annotation.imageTag());
        } catch (JavaManagerException e) {
            throw new JavaUbuntuManagerException("Problem generating a Java Ubuntu installation", e);
        }
        
        this.installations.put(annotation.javaTag(), installation);
        
        return installation;
    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        for(JavaUbuntuInstallationImpl installation : this.installations.values()) {
            installation.build();
        }
    }
    
    @Override
    public void provisionDiscard() {
        for(JavaUbuntuInstallationImpl installation : this.installations.values()) {
            installation.discard();
        }
    }

    public IJavaManagerSpi getJavaManager() {
        return this.javaManager;
    }

    public ILinuxManagerSpi getLinuxManager() {
        return this.linuxManager;
    }

    public String getTestClassName() {
        if (this.test == null) {
            return "unknown";
        }
        return this.test.getName();
    }

}
