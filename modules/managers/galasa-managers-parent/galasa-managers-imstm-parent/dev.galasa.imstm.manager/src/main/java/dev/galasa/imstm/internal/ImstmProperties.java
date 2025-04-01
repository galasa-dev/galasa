/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal;

import dev.galasa.imstm.ImstmManagerException;

import java.util.HashMap;
import java.util.Map;

import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.cps.CpsProperties;

public class ImstmProperties extends CpsProperties {

    private static final String NAMESPACE = "imstm";
    private final IConfigurationPropertyStoreService cps;

    private Map<String, String> dseApplids;
    private Map<String, ProductVersion> dseVersions;
    private String provisionType;
    
    ImstmProperties(IFramework framework) throws ConfigurationPropertyStoreException {
        cps = framework.getConfigurationPropertyService(NAMESPACE);
    }
    
    /**
     * Developer Supplied Environment - IMS TM System - Applid
     * 
     * @galasa.cps.property
     * 
     * @galasa.name imstm.dse.tag.[TAG].applid
     * 
     * @galasa.description Provides the applid of the IMS TM system for the DSE provisioner.  The applid setting
     * is mandatory for a DSE region.
     * 
     * @galasa.required Yes if you want a DSE region, otherwise not required
     * 
     * @galasa.default None
     * 
     * @galasa.valid_values A valid VTAM applid
     * 
     * @galasa.examples 
     * <code>imstm.dse.tag.PRIMARY.applid=IMS1</code><br>
     *
     */
    public String getDseApplid(String tag) throws ImstmManagerException {
        if (dseApplids == null) {
            dseApplids = new HashMap<String, String>();
        }

        if (dseApplids.containsKey(tag)) {
            return dseApplids.get(tag);
        } else {
            try {
                String dseApplid = getStringNulled(cps, "dse.tag." + tag, "applid");
                if (dseApplid != null) {
                    dseApplid = dseApplid.toUpperCase().trim();
                }
                dseApplids.put(tag, dseApplid);
                return dseApplid;
            } catch (ConfigurationPropertyStoreException e) {
                throw new ImstmManagerException("Problem asking CPS for the DSE applid for tag " + tag, e); 
            }
        }
    }

    /**
     * Developer Supplied Environment - IMS TM System - Version
    * 
     * @galasa.cps.property
     * 
     * @galasa.name imstm.dse.tag.version<br>imstm.dse.tag.[TAG].version
     * 
     * @galasa.description Provides the version of the IMS TM system to the DSE provisioner.  
     * 
     * @galasa.required Only requires setting if the test requests it.
     * 
     * @galasa.default None
     * 
     * @galasa.valid_values A valid V.R.M version format, eg 15.5.0
     * 
     * @galasa.examples 
     * <code>imstm.dse.tag.PRIMARY.version=15.5.0</code><br>
     *
     */
    public ProductVersion getDseVersion(String tag) throws ImstmManagerException {
        if (dseVersions == null) {
            dseVersions = new HashMap<String, ProductVersion>();
        }

        if (dseVersions.containsKey(tag)) {
            return dseVersions.get(tag);
        } else {
            String version = null;
            try {
                version = getStringNulled(cps, "dse.tag", "version", tag);
                ProductVersion dseVersion = null;
                if (version != null) {
                    dseVersion = ProductVersion.parse(version);
                }
                dseVersions.put(tag, dseVersion);
                return dseVersion;
            } catch (ManagerException e) {
                throw new ImstmManagerException("Failed to parse the IMS version '" + version + "' for tag '" + tag + "', should be a valid V.R.M version format, for example 15.5.0", e); 
            } catch (ConfigurationPropertyStoreException e) {
                throw new ImstmManagerException("Problem accessing the CPS for the IMS version, for tag " + tag, e);
            }
        }
    }

    /**
     * Developer Supplied Environment - IMS TM System - Version
    * 
     * @galasa.cps.property
     * 
     * @galasa.name imstm.provision.type
     * 
     * @galasa.description Specifies the type of system provisioning to be used for the tests.  
     * 
     * @galasa.required No
     * 
     * @galasa.default dse
     * 
     * @galasa.valid_values Any value recognized by available IMS System provisioners. Only <code>dse</code>
     * and <code>mixed</code> (case-insensitive) are recognized by the default DSE provisioner.
     * 
     * @galasa.examples 
     * <code>imstm.provision.type=dse</code><br>
     *
     */
    public String getProvisionType() throws ImstmManagerException {
        if (provisionType == null) {
            provisionType = getStringWithDefault(cps, "dse", "provision", "type").toUpperCase();
        }

        return provisionType;
    }
}
