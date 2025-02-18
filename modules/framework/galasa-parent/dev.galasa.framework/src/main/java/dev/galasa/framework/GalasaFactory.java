/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;

import dev.galasa.framework.internal.init.DefaultInitStrategy;
import dev.galasa.framework.internal.init.ResourceManagerInitStrategy;
import dev.galasa.framework.internal.init.TestRunInitStrategy;
import dev.galasa.framework.spi.Environment;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFrameworkInitialisation;


public class GalasaFactory implements IGalasaFactory {

    private static IGalasaFactory instance ;
    private static final String lock = "aConcurrencyLock";

    public static IGalasaFactory getInstance() {
        IGalasaFactory instanceToReturn = instance;
        if (instanceToReturn==null) {
            synchronized(lock) {   
                if (instance== null) {
                    instance = new GalasaFactory();
                }
                instanceToReturn = instance;
            }
        }
        return instanceToReturn ;
    }

    protected static void setInstance(IGalasaFactory newInstance) {
        instance = newInstance ;
    }
    
    public IFrameworkInitialisationStrategy newTestRunInitStrategy() {
        return new TestRunInitStrategy();
    }

    public IFrameworkInitialisationStrategy newResourceManagerInitStrategy() {
        return new ResourceManagerInitStrategy();
    }

    public IFrameworkInitialisationStrategy newDefaultInitStrategy() {
        return new DefaultInitStrategy();
    }

    public IFrameworkInitialisation newFrameworkInitialisation(
        Properties bootstrapProperties, 
        Properties overrideProperties, 
        Log initLogger,
        BundleContext bundleContext , 
        IFileSystem fileSystem,
        Environment env,
        IFrameworkInitialisationStrategy initStrategy
    ) throws URISyntaxException, InvalidSyntaxException, FrameworkException {
        IFrameworkInitialisation framworkInit = new FrameworkInitialisation(
            bootstrapProperties, 
            overrideProperties, 
            initLogger,
            bundleContext , 
            fileSystem,
            env,
            initStrategy
        );
        return framworkInit;
    }

}
