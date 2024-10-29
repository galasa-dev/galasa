/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.SharedEnvironment;
import dev.galasa.Test;
import dev.galasa.framework.IAnnotationExtractor;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.SharedEnvironmentRunType;



public class RunTypeDetails {

    private RunType detectedRunType ;
    private int expireAfterHours = 8 ;

    private Log logger = LogFactory.getLog(RunTypeDetails.class);

    public RunTypeDetails( 
        IAnnotationExtractor annotationExtractor, 
        Class<?> testClass ,
        String testBundleName, 
        String testClassName,
        IFramework framework
    ) throws TestRunException {

        logger.debug("Getting test annotations..");
        
        Test testAnnotation = annotationExtractor.getAnnotation( testClass , Test.class);
        logger.debug("Test annotations.. got");

        SharedEnvironment sharedEnvironmentAnnotation = annotationExtractor.getAnnotation( testClass, SharedEnvironment.class);

        logger.debug("Checking testAnnotation and sharedEnvironmentAnnotation");
        if (testAnnotation == null && sharedEnvironmentAnnotation == null) {
            logger.debug("Test annotation is null and it's not a shared environment. Throwing TestRunException...");
            throw new TestRunException("Class " + testBundleName + "/" + testClassName + " is not annotated with either the dev.galasa @Test or @SharedEnvironment annotations");
        } else if (testAnnotation != null && sharedEnvironmentAnnotation != null) {
            logger.debug("Test annotation is non-null and shared environment annotation is non-null. Throwing TestRunException...");
            throw new TestRunException("Class " + testBundleName + "/" + testClassName + " is annotated with both the dev.galasa @Test and @SharedEnvironment annotations");
        }
        

        if (testAnnotation != null) {
            logger.info("Run test: " + testBundleName + "/" + testClassName);
            detectedRunType = RunType.TEST;
        } else {
            logger.info("Shared Environment class: " + testBundleName + "/" + testClassName);
        }


        if (sharedEnvironmentAnnotation != null) {
            try {
                SharedEnvironmentRunType seType = framework.getSharedEnvironmentRunType();
                if (seType != null) {
                    switch(seType) {
                        case BUILD:
                            detectedRunType = RunType.SHARED_ENVIRONMENT_BUILD;
                            break;
                        case DISCARD:
                            detectedRunType = RunType.SHARED_ENVIRONMENT_DISCARD;
                            break;
                        default:
                            String msg = "Unknown Shared Environment phase, '" + seType + "', needs to be BUILD or DISCARD";
                            logger.error(msg);
                            throw new TestRunException(msg);
                    }
                } else {
                    String msg = "Unknown Shared Environment phase, needs to be BUILD or DISCARD";
                    logger.error(msg);
                    throw new TestRunException(msg);
                }
            } catch(TestRunException e) {
                String msg = "TestRunException caught. "+e.getMessage()+" Re-throwing.";
                logger.error(msg);
                throw e;
            } catch(Exception e) {
                String msg = "Exception caught. "+e.getMessage()+" Re-throwing.";
                logger.error(msg);
                throw new TestRunException("Unable to determine the phase of the shared environment", e);
            }
        }
    }    

    public RunType getDetectedRunType() {
        return this.detectedRunType;
    }

    public int getSharedEnvironmentExpireAfterHours() {
        return this.expireAfterHours;
    }

}
