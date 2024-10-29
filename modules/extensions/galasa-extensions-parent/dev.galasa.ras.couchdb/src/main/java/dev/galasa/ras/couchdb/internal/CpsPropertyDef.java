/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ras.couchdb.internal;

import java.text.MessageFormat;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

// The name of a cps flag, which controls the storage strategy for artifacts.
// TODO: This could be moved into framework so that that api code can access it also.
public enum CpsPropertyDef {
    // If true, then couchdb creates one artifact document per artifact.
    ONE_ARTIFACT_PER_DOCUMENT("couchdb","one.artifact.per.document"),

    // If true, then couchdb puts the data inline. ie: Within a json property inside the json document.
    // Default value is 0, ie: No in-lining of attachments.
    // Only applicable when ONE_ARTIFACT_PER_DOCUMENT feature flag is enabled.
    INLINE_ARTIFACT_MAX_SIZE("couchdb","inline.artifact.max.size")
    ;

    private String namespace;
    private String propertyName ;

    private CpsPropertyDef(String namespace, String propertyName) {
        this.namespace = namespace;
        this.propertyName = propertyName;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    public int getCpsIntValue(org.apache.commons.logging.Log logger, IConfigurationPropertyStoreService cps) throws CouchdbRasException {
        String featurePropertyName = this.getPropertyName();
        int firstDotIndex = featurePropertyName.indexOf('.');
        String prefix = featurePropertyName.substring(0, firstDotIndex);
        String suffix = featurePropertyName.substring(firstDotIndex+1);
        String valueStr ;
        try {
            valueStr = cps.getProperty(prefix, suffix);
        } catch( ConfigurationPropertyStoreException ex) {
            throw new CouchdbRasException(
                MessageFormat.format("Failed to get the value of property {0} from the cps.",featurePropertyName),
                ex
            );
        }

        int value ;
        if (valueStr == null) {
            logger.trace(MessageFormat.format("Couchdb setting %s is not set. Defaulting.",this.getPropertyName()));
            value = 0;
        } else {
            value = Integer.parseInt(valueStr);
        }
        
        return value;
    }

}
