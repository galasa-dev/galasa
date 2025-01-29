/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.framework.spi.DssDelete;
import dev.galasa.framework.spi.DssUpdate;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreMatchException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IResourcePoolingService;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.internal.properties.SdvPoolUsers;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides the implementation for querying available
 * users in a pool matching a role, and then picks one, and marks
 * it as being in use.
 *
 * <p>It additonally implements the ability to release users back to
 * the pool.
 */
public class SdvUserPool {

    private static final Log LOG = LogFactory.getLog(SdvUserPool.class);

    private final IDynamicStatusStoreService dss;
    private final IResourcePoolingService rps;
    private final IFramework framework;

    /**
     * SdvUserPool contructor.
     *
     * @param framework - Galasa framework object.
     * @param dss - Galasa DSS.
     * @param rps - Galasa Resource Pooling Service.
     * @throws SdvManagerException general error encountered.
     */
    public SdvUserPool(IFramework framework, IDynamicStatusStoreService dss,
            IResourcePoolingService rps) throws SdvManagerException {

        this.framework = framework;
        this.dss = dss;
        this.rps = rps;
    }

    /**
     * Will return an available user for a given role, if one is
     * available. The user will then be marked as unavailable.
     *
     * <p>If a user is not available, an exception will be thrown.
     *
     * @param role - The role name the returned user must belong to.
     * @param cicsRegion - The CICS Region which the user will interact with.
     * @return - The Galasa credentials tag string for the allocated user.
     * @throws SdvManagerException general error encountered.
     * @throws ResourceUnavailableException no available users for that role currently.
     */
    public String allocateUser(String role, ICicsRegion cicsRegion)
            throws SdvManagerException, ResourceUnavailableException {

        List<String> availableSdvUsers = null;
        String theSdvUser = null;

        // Get the full pool of user credential tags for the
        // specified zOS image and role
        List<String> fullZosImageUserListForRole =
                SdvPoolUsers.get(cicsRegion.getZosImage().getImageID(), role);

        if (fullZosImageUserListForRole.isEmpty()) {
            throw new SdvManagerException(
                "No user credential tags provided for role '" + role
                        + "' on z/OS image '"
                        + cicsRegion.getZosImage().getImageID()
                        + "'. Create or update CPS property 'sdv.zosImage."
                        + cicsRegion.getZosImage().getImageID()
                        + ".role."
                        + role
                        + ".credTags'."
            );
        }

        // Retrieve available users from the pool, not being used by the region
        try {
            availableSdvUsers = this.rps.obtainResources(fullZosImageUserListForRole, null, 1, 1,
                    dss, "sdvuser." + cicsRegion.getApplid() + ".");
        } catch (InsufficientResourcesAvailableException e) {
            // There are no users available, inform the framework that we should go into wait state
            // and try again later
            // All resources (from other managers) will be discarded by using this
            throw new ResourceUnavailableException(
                    "Could not obtain a user from the SDV user pool for image "
                            + cicsRegion.getZosImage().getImageID() + ", and role " + role,
                    e);
        }

        // Allocate the user retrieved from the pool
        try {
            // There should only be a single user in the list allocated
            theSdvUser = availableSdvUsers.get(0);

            // Allocate the user in the DSS
            this.dss.performActions(
                    new DssUpdate("sdvuser." + cicsRegion.getApplid() + "." + theSdvUser,
                            this.framework.getTestRunName()),
                    new DssUpdate("run." + this.framework.getTestRunName() + ".sdvuser."
                            + cicsRegion.getApplid() + "." + theSdvUser, "active"));

            if (LOG.isTraceEnabled()) {
                LOG.trace("Allocated SDV User " + theSdvUser + " on image "
                        + cicsRegion.getZosImage().getImageID() + " for CICS Applid "
                        + cicsRegion.getApplid() + " from SDV User pool allocation");
            }

        } catch (DynamicStatusStoreException e) {
            throw new SdvManagerException(
                    "Could not update the DSS for user allocation of SDV User " + theSdvUser
                            + " on image " + cicsRegion.getZosImage().getImageID(),
                    e);
        }
        return theSdvUser;
    }

    public static void deleteDss(String user, String cicsApplid, String run,
            IDynamicStatusStoreService dss)
            throws DynamicStatusStoreMatchException, DynamicStatusStoreException {
        dss.performActions(new DssDelete("sdvuser." + cicsApplid + "." + user, run),
                new DssDelete("run." + run + ".sdvuser." + cicsApplid + "." + user, "active"));
    }
}
