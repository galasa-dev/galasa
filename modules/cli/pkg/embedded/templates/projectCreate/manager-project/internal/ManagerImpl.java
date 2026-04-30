/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}}.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import {{.PackageName}}.{{.CapitalizedManagerName}}ManagerException;
import {{.PackageName}}.{{.CapitalizedManagerName}}Resource;
import {{.PackageName}}.I{{.CapitalizedManagerName}}Manager;
import {{.PackageName}}.I{{.CapitalizedManagerName}}Resource;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

/**
 * {{.CapitalizedManagerName}}ManagerImpl
 * 
 * Main implementation of the {{.CapitalizedManagerName}} Manager.
 * This class handles the lifecycle of {{.CapitalizedManagerName}} resources and
 * injects them into test classes.
 */
@Component(service = { IManager.class })
public class {{.CapitalizedManagerName}}ManagerImpl extends AbstractManager implements I{{.CapitalizedManagerName}}Manager {
    
    protected static final String NAMESPACE = "{{.PackageName}}";
    private static final Log logger = LogFactory.getLog({{.CapitalizedManagerName}}ManagerImpl.class);
    
    private IFramework framework;
    private boolean required = false;

    /**
     * Initialize the manager
     * 
     * @param framework the Galasa framework
     * @param allManagers list of all managers
     * @param activeManagers list of active managers
     * @param galasaTest the test class
     * @throws ManagerException if initialization fails
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        
        if (galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields({{.CapitalizedManagerName}}ManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
        
        this.framework = framework;
        logger.info("{{.CapitalizedManagerName}} Manager initialized");
    }

    /**
     * Mark this manager as required and add it to active managers
     * 
     * @param allManagers list of all managers
     * @param activeManagers list of active managers
     * @param galasaTest the test class
     * @throws ManagerException if activation fails
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
            @NotNull GalasaTest galasaTest) throws ManagerException {
        
        this.required = true;
        
        if (activeManagers.contains(this)) {
            return;
        }
        
        activeManagers.add(this);
        
        // Add any dependent managers here
        // Example:
        // httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        // if (httpManager == null) {
        //     throw new {{.CapitalizedManagerName}}ManagerException("The HTTP Manager is not available");
        // }
    }

    /**
     * Generate and inject a {{.CapitalizedManagerName}} resource into a test field
     * 
     * @param field the field to inject into
     * @param annotations the annotations on the field
     * @return the generated resource
     * @throws {{.CapitalizedManagerName}}ManagerException if resource generation fails
     */
    @GenerateAnnotatedField(annotation = {{.CapitalizedManagerName}}Resource.class)
    public I{{.CapitalizedManagerName}}Resource generate{{.CapitalizedManagerName}}Resource(Field field, List<Annotation> annotations) 
            throws {{.CapitalizedManagerName}}ManagerException {
        
        {{.CapitalizedManagerName}}Resource annotation = field.getAnnotation({{.CapitalizedManagerName}}Resource.class);
        String tag = annotation.resourceTag();
        
        logger.info("Generating {{.CapitalizedManagerName}} resource with tag: " + tag);
        
        // Create and return the resource implementation
        return new {{.CapitalizedManagerName}}ResourceImpl(tag);
    }

    /**
     * Provision resources before the test runs
     * 
     * @throws ManagerException if provisioning fails
     * @throws ResourceUnavailableException if resources are not available
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        // Provision any resources needed before the test runs
        logger.info("Provisioning {{.CapitalizedManagerName}} resources");
    }

    /**
     * Clean up resources after the test completes
     * 
     * @throws ManagerException if cleanup fails
     */
    @Override
    public void provisionDiscard() {
        // Clean up resources after the test
        logger.info("Cleaning up {{.CapitalizedManagerName}} resources");
    }

    /**
     * Get manager information
     * 
     * @return manager information string
     */
    @Override
    public String getManagerInfo() {
        return "{{.CapitalizedManagerName}} Manager v1.0.0";
    }
}

