/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.k8s.controller;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class CPSFacade {

    public static final long KUBE_LAUNCH_INTERVAL_MILLISECOND_DEFAULT_VALUE = 1000L;

    public static final String KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_PREFIX = "framework";
    public static final String KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_INFIX = "kube.launch.interval.milliseconds" ;

    private IConfigurationPropertyStoreService cps;
    private final Log logger = LogFactory.getLog(getClass());

    public CPSFacade(IConfigurationPropertyStoreService cps) {
        this.cps = cps ;
    }


    /**
     * The CPS property `framework.kube.launch.interval.milliseconds controls` the number of milliseconds which the test pod 
     * scheduler should delay between successive launches of test pods.
     * 
     * The default is KUBE_LAUNCH_INTERVAL_MILLISECOND_DEFAULT_VALUE
     * 
     * Any failure in the CPS will be logged and ignored, resulting in the default value being returned.
     * 
     * This CPS property is dynamic, in that you don't need to re-start the test launcher pod for it to take effect.
     * It gets read every time the value is needed, and is not cached.
     * 
     * @return The number of milliseconds which the test pod scheduler should delay between successive launches of test pods.
     */
    public long getKubeLaunchIntervalMilliseconds() {
        long intervalMs = KUBE_LAUNCH_INTERVAL_MILLISECOND_DEFAULT_VALUE;

        if (cps==null) {
            logger.error("getKubeLaunchIntervalMilliseconds: Null CPS. Internal server logic error.");
        } else {

            try {
                String cpsRawValue = cps.getProperty(KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_PREFIX, null, KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_INFIX);
                if (cpsRawValue == null) {
                    String msg = MessageFormat.format(
                        "Info: Could not get launch interval value from the CPS (Property {0}.{1}). Using default value of {2}. CPS property is empty.",
                        KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_PREFIX,
                        KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_INFIX,
                        Long.toString(intervalMs)
                    );
                    logger.info(msg);
                } else {
                    String trimmedValue = cpsRawValue.trim();
                    try {
                        intervalMs = Long.parseLong(trimmedValue);
                    } catch(NumberFormatException ex) {
                        String msg = MessageFormat.format(
                            "Info: Could not get launch interval value from the CPS (Property {0}.{1}). Using default value of {2}. CPS Value '{3}' is not a number.",
                            KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_PREFIX,
                            KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_INFIX,
                            Long.toString(intervalMs),
                            trimmedValue
                        );
                        logger.info(msg);
                    }
                }

            } catch( ConfigurationPropertyStoreException ex) {
                String msg = MessageFormat.format(
                    "Error: Could not get launch interval value from the CPS (Property {0}.{1}). Using default value of {2}. CPS Failure {3}",
                    KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_PREFIX,
                    KUBE_LAUNCH_INTERVAL_CPS_PROPERTY_INFIX,
                    Long.toString(intervalMs),
                    ex
                    );
                logger.error(msg);
            }
        }

        return intervalMs ;
    }
}