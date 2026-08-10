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

import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import {{.PackageName}}.{{.CapitalizedManagerName}}ManagerException;
import {{.PackageName}}.{{.CapitalizedManagerName}}Resource;
import {{.PackageName}}.I{{.CapitalizedManagerName}}Manager;
import {{.PackageName}}.I{{.CapitalizedManagerName}}Resource;
import {{.PackageName}}.internal.properties.{{.CapitalizedManagerName}}PropertiesSingleton;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;

/**
 * {{.CapitalizedManagerName}} Manager Implementation
 *
 * This manager provides {{.CapitalizedManagerName}} resources to Galasa tests through the
 * @{{.CapitalizedManagerName}}Resource annotation. It handles the lifecycle of
 * {{.CapitalizedManagerName}} resources including provisioning, allocation, and cleanup.
 *
 * The manager follows the standard Galasa manager lifecycle:
 * 1. initialise - Called during framework initialization to detect if this manager is needed
 * 2. youAreRequired - Registers this manager as active if resources are requested
 * 3. provisionGenerate - Provisions resources before test execution
 * 4. Test execution - Resources are available to the test
 * 5. provisionDiscard - Cleans up resources after test execution
 *
 * This is an OSGi component that is automatically discovered and registered by the Galasa framework.
 *
 * @see I{{.CapitalizedManagerName}}Manager
 * @see {{.CapitalizedManagerName}}Resource
 * @see I{{.CapitalizedManagerName}}Resource
 */
@Component(service = { IManager.class })
public class {{.CapitalizedManagerName}}ManagerImpl extends AbstractManager implements I{{.CapitalizedManagerName}}Manager {
    
    /**
     * The Configuration Property Store (CPS) namespace for this manager.
     * All configuration properties for this manager should be prefixed with this namespace.
     */
    protected static final String NAMESPACE = "{{.ManagerNamespace}}";

    /**
     * Initialize the manager during framework startup.
     *
     * This method is called by the Galasa framework during initialization. It checks if any
     * test fields are annotated with @{{.CapitalizedManagerName}}Resource. If so, it calls
     * youAreRequired to register this manager as active.
     *
     * This is the first phase of the manager lifecycle and determines whether this manager
     * needs to be activated for the current test run.
     *
     * @param framework the Galasa framework instance
     * @param allManagers list of all available managers
     * @param activeManagers list of currently active managers
     * @param galasaTest the test class being executed
     * @throws ManagerException if initialization fails
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        
        // Initialize the Configuration Property Store for this manager
        try {
            {{.CapitalizedManagerName}}PropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (Exception e) {
            throw new ManagerException("Unable to initialise CPS for {{.CapitalizedManagerName}} Manager", e);
        }
        
        if (galasaTest.isJava()) {
            List<AnnotatedField> ourFields = findAnnotatedFields({{.CapitalizedManagerName}}ManagerField.class);
            if (!ourFields.isEmpty()) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }

    /**
     * Register this manager as active and required for the test run.
     *
     * This method is called when the manager is needed, either because test fields are annotated
     * with @{{.CapitalizedManagerName}}Resource or because another manager depends on this one.
     * It adds this manager to the active managers list if not already present.
     *
     * This method is idempotent - calling it multiple times has no additional effect.
     *
     * @param allManagers list of all available managers
     * @param activeManagers list of currently active managers (this manager will be added to this list)
     * @param galasaTest the test class being executed
     * @throws ManagerException if registration fails
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
            @NotNull GalasaTest galasaTest) throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }
        activeManagers.add(this);
    }

    /**
     * Generate a {{.CapitalizedManagerName}} resource instance for an annotated field.
     *
     * This method is called by the Galasa framework when it encounters a field annotated with
     * @{{.CapitalizedManagerName}}Resource. It creates and returns a new resource instance
     * that will be injected into the test field.
     *
     * The resource tag from the annotation is used to identify and configure the resource.
     * Multiple fields can request different resources by using different tags.
     *
     * Example usage in a test:
     * <pre>
     * @{{.CapitalizedManagerName}}Resource(resourceTag = "PRIMARY")
     * public I{{.CapitalizedManagerName}}Resource resource;
     * </pre>
     *
     * @param field the field to be populated with the resource
     * @param annotations all annotations on the field
     * @return a new {{.CapitalizedManagerName}} resource instance
     * @throws {{.CapitalizedManagerName}}ManagerException if resource generation fails
     */
    @GenerateAnnotatedField(annotation = {{.CapitalizedManagerName}}Resource.class)
    public I{{.CapitalizedManagerName}}Resource generate{{.CapitalizedManagerName}}Resource(Field field, List<Annotation> annotations)
            throws {{.CapitalizedManagerName}}ManagerException {
        {{.CapitalizedManagerName}}Resource annotation = field.getAnnotation({{.CapitalizedManagerName}}Resource.class);
        return new {{.CapitalizedManagerName}}ResourceImpl(annotation.resourceTag());
    }

    /**
     * Provision and allocate resources before test execution.
     *
     * This method is called by the Galasa framework after all managers are initialized but before
     * the test begins execution. It should allocate and prepare any resources that the test will need.
     *
     * This is where you should:
     * - Allocate resources from pools
     * - Establish connections to external systems
     * - Perform any setup required before the test runs
     * - Validate that required resources are available
     *
     * If resources cannot be provisioned, this method should throw an exception to prevent
     * the test from running with incomplete resources.
     *
     * @throws ManagerException if provisioning fails due to a manager error
     * @throws ResourceUnavailableException if required resources are not available
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields({{.CapitalizedManagerName}}ManagerField.class);
    }

    /**
     * Clean up and release resources after test execution.
     *
     * This method is called by the Galasa framework after the test completes (whether it passes,
     * fails, or is aborted). It should release all resources and perform cleanup operations.
     *
     * This is where you should:
     * - Close connections to external systems
     * - Return resources to pools for reuse
     * - Clean up any temporary data or state
     * - Perform any necessary logging or reporting
     *
     * This method should not throw exceptions. If cleanup fails, log the error but allow
     * the framework to continue with other cleanup operations.
     */
    @Override
    public void provisionDiscard() {
        // TODO: Clean up resources after test execution
        // Example: Close connections, return resources to pool, etc.
    }
}

