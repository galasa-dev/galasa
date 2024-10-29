/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.internal.properties;

import javax.validation.constraints.NotNull;

import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.cps.CpsProperties;
import dev.galasa.vtp.manager.VtpManagerException;

/**
 * Transactions that should be recorded for this CICS region
 * 
 * @galasa.cps.property
 * 
 * @galasa.name vtp.cics.[instanceid].transactions
 * 
 * @galasa.description A list of transactions that we should record in this CICS region
 * 
 * @galasa.required No
 * 
 * @galasa.default None
 * 
 * @galasa.valid_values trx1,trx2
 * 
 * @galasa.examples 
 * <code>vtp.cics.PRIMARY.transactions=TSQT,TSQD</code><br>
 *
 */
public class TransactionNamesForTag extends CpsProperties {
    
    public static String get(@NotNull String instanceid) throws VtpManagerException {
        try {
            return getStringNulled(VtpPropertiesSingleton.cps(), "cics", "transactions", instanceid);
        } catch (ConfigurationPropertyStoreException e) {
            throw new VtpManagerException("Problem asking the CPS for the queue manager name for instance '"  + instanceid + "'", e);
        }
    }

}
