/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.internal.properties;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.zosfile.ZosFileManagerException;

/**
 * zOS File the maximum number of items from a UNIX directory list
 * 
 * @galasa.cps.property
 * 
 * @galasa.name zosfile.unix.[imageid].directory.list.max.items
 * 
 * @galasa.description The maximum number of items the server (e.g. zOSMF, RSE API, etc) returns when listing the content of a UNIX directory
 * 
 * @galasa.required No
 * 
 * @galasa.default 1000
 * 
 * @galasa.valid_values 
 * 
 * @galasa.examples 
 * <code>zosfile.unix.[imageid].directory.list.max.items=1000</code><br>
 *
 */
public class DirectoryListMaxItems extends CpsProperties {

    private static final int MAX_ITEMS = 1000;

    public static int get(String imageId) throws ZosFileManagerException {
        try {
            String maxItemsString = getStringNulled(ZosFilePropertiesSingleton.cps(), "unix", "directory.list.max.items", imageId);

            if (maxItemsString == null) {
                return MAX_ITEMS;
            } else {
                int maxItems = Integer.parseInt(maxItemsString);
                if (maxItems <=0 ) {
                    throw new ZosFileManagerException("Directory list max items property must be greater than 0");
                }
                return maxItems;
            }
        } catch (ConfigurationPropertyStoreException | NumberFormatException e) {
            throw new ZosFileManagerException("Problem asking the CPS for the directory list max items property for zOS image "  + imageId, e);
        }
    }

}
