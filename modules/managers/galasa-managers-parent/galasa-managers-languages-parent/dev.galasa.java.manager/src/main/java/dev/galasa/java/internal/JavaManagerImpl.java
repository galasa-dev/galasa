/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.java.internal;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.internal.properties.JavaPropertiesSingleton;
import dev.galasa.java.spi.IJavaManagerSpi;

@Component(service = { IManager.class })
public class JavaManagerImpl extends AbstractManager implements IJavaManagerSpi {
    protected final static String              NAMESPACE    = "java";

    private final static Log                   logger       = LogFactory.getLog(JavaManagerImpl.class);
    
    private IHttpManagerSpi    httpManager;
    
    private final HashMap<String, IJavaInstallation> registeredInstallations = new HashMap<>();
    
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

//        if(galasaTest.isJava()) {
//            // *** Check to see if any of our annotations are present in the test class
//            // *** If there is, we need to activate
//            List<AnnotatedField> ourFields = findAnnotatedFields(JavaUbuntuManagerField.class);
//            if (!ourFields.isEmpty()) {
//                youAreRequired(allManagers, activeManagers);
//            }
//        }
        
        try {
            JavaPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new JavaManagerException("Failed to set the CPS with the Java namespace", e);
        }
        
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        
        this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new JavaManagerException("The HTTP Manager is not available");
        }
        
    }
    
    public IHttpManagerSpi getHttpManager() {
        return this.httpManager;
    }

    @Override
    public IJavaInstallation getInstallationForTag(String tag) throws JavaManagerException {
        IJavaInstallation installation = this.registeredInstallations.get(tag);
        if (installation == null) {
            throw new JavaManagerException("Unable to locate Java installation with tag " + tag);
        }
        return installation;
    }

    @Override
    public void registerJavaInstallationForTag(String tag, IJavaInstallation javaInstallation)
            throws JavaManagerException {
        if (this.registeredInstallations.containsKey(tag)) {
            throw new JavaManagerException("Java installation tag " + tag + " has already been registered");
        }
        
        this.registeredInstallations.put(tag, javaInstallation);
    }
    
}
