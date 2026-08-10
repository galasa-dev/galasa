/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package {{.PackageName}}.internal.properties;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import {{.PackageName}}.{{.CapitalizedManagerName}}ManagerException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

/**
 * Configuration Property Store (CPS) Properties Singleton
 * 
 * This singleton class provides access to the CPS for retrieving configuration
 * properties for the {{.CapitalizedManagerName}} Manager. All property classes should
 * use this singleton to access the CPS.
 * 
 * The CPS allows managers to retrieve configuration values that can be set by
 * administrators or test developers, enabling flexible configuration without
 * code changes.
 * 
 * @see {{.CapitalizedManagerName}}ExampleProperty
 */
@Component(service = {{.CapitalizedManagerName}}PropertiesSingleton.class, immediate = true)
public class {{.CapitalizedManagerName}}PropertiesSingleton {

    private static {{.CapitalizedManagerName}}PropertiesSingleton singletonInstance;

    private static void setInstance({{.CapitalizedManagerName}}PropertiesSingleton instance) {
        singletonInstance = instance;
    }

    private IConfigurationPropertyStoreService cps;

    /**
     * Called by OSGi when this component is activated.
     * Sets this instance as the singleton.
     */
    @Activate
    public void activate() {
        setInstance(this);
    }

    /**
     * Called by OSGi when this component is deactivated.
     * Clears the singleton instance.
     */
    @Deactivate
    public void deactivate() {
        setInstance(null);
    }

    /**
     * Returns the CPS instance for accessing configuration properties.
     * 
     * @return the Configuration Property Store Service
     * @throws {{.CapitalizedManagerName}}ManagerException if the CPS has not been initialized
     */
    public static IConfigurationPropertyStoreService cps() throws {{.CapitalizedManagerName}}ManagerException {
        if (singletonInstance != null) {
            return singletonInstance.cps;
        }

        throw new {{.CapitalizedManagerName}}ManagerException(
                "Attempt to access manager CPS before it has been initialised");
    }

    /**
     * Sets the CPS instance.
     * 
     * @param cps the Configuration Property Store Service to set
     * @throws {{.CapitalizedManagerName}}ManagerException if the singleton has not been created
     */
    public static void setCps(IConfigurationPropertyStoreService cps) throws {{.CapitalizedManagerName}}ManagerException {
        if (singletonInstance != null) {
            singletonInstance.cps = cps;
            return;
        }

        throw new {{.CapitalizedManagerName}}ManagerException(
                "Attempt to set manager CPS before instance created");
    }
}


