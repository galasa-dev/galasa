/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.ProductVersion;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.SdvManagerField;
import dev.galasa.sdv.SdvUser;
import dev.galasa.sdv.internal.properties.SdvHlq;
import dev.galasa.sdv.internal.properties.SdvPort;
import dev.galasa.sdv.internal.properties.SdvPropertiesSingleton;
import dev.galasa.sdv.internal.properties.SdvRole;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

/**
  * This class acts as the entry points to the SDV manager
  * by the Galasa framework, and implements various functions
  * at various points throughout the test framework lifecycle.
  *
  * <p>It initialises the SDV manager, gathers all required
  * config, assess which regions to record and what users, and
  * intercepts and implements behaviour for test creation and
  * teardown.
  *
  */
@Component(service = {IManager.class})
public class SdvManagerImpl extends AbstractManager {

    public static final String NAMESPACE = "sdv";

    private static final Log LOG = LogFactory.getLog(SdvManagerImpl.class);

    // Dependencies on other managers
    private ICicstsManagerSpi cicsManager;
    private IZosBatchSpi batchManager;
    private IHttpManagerSpi httpManager;
    private IArtifactManager artifactManager;
    private IDynamicStatusStoreService dss;

    private IConfidentialTextService cts;

    // User Pool management
    private SdvUserPool sdvUserPool;

    // Local store of tested CICS regions, linked to their users and associated roles, each
    // of which being recorded independently
    private Map<ICicsRegion, RecordingRegion> recordingRegions = new HashMap<>();

    private Path storedArtifactRoot;
    private SdvRecorderImpl sdvRecorder;
    private List<ISdvUser> sdvUsersToRecordList = new ArrayList<>();

    /** Define CF structure to be used when creating logstreams. */
    private static final String CFstructure = "LOG_GENERAL_001";

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        // If this is not a java galasa test then exit
        if (!galasaTest.isJava()) {
            LOG.info("SDV recording is requested but is not eligible as this is not a Java test");
            return;
        }

        // Check if SDV specific annotations exist.
        // If not, don't use this manager.
        List<AnnotatedField> ourFields = findAnnotatedFields(SdvManagerField.class);
        if (ourFields.isEmpty()) {
            return;
        }

        // Get access to the CPS so we can configure ourself
        IConfigurationPropertyStoreService cps;
        try {
            cps = getFramework().getConfigurationPropertyService(NAMESPACE);
            SdvPropertiesSingleton.setCps(cps);
        } catch (ConfigurationPropertyStoreException e1) {
            throw new SdvManagerException(
                "Unable to access 'sdv' CPS namespace from framework services",
                e1
            );
        }

        try {
            this.dss = this.getFramework().getDynamicStatusStoreService(NAMESPACE);
        } catch (DynamicStatusStoreException e) {
            throw new SdvManagerException(
                "Unable to access 'sdv' DSS namespace from framework services",
                e
            );
        }

        this.cts = framework.getConfidentialTextService();

        // Initialise the SDV User pool manager
        this.sdvUserPool = new SdvUserPool(this.getFramework(), dss,
                this.getFramework().getResourcePoolingService());

        this.storedArtifactRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot()
                .resolve(NAMESPACE);

        // if we get here then we are required so add ourself to the list of active
        // managers
        youAreRequired(allManagers, activeManagers, galasaTest);
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {

        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);

        cicsManager = addDependentManager(allManagers, activeManagers, galasaTest,
                ICicstsManagerSpi.class);

        if (cicsManager == null) {
            throw new SdvManagerException("The CICS Manager is not available");
        }

        batchManager =
                addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
        if (batchManager == null) {
            throw new SdvManagerException("The z/OS Batch Manager is not available");
        }

        artifactManager = addDependentManager(allManagers, activeManagers, galasaTest,
                IArtifactManager.class);
        if (artifactManager == null) {
            throw new SdvManagerException("The Artifact Manager is not available");
        }

        httpManager =
                addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (httpManager == null) {
            throw new SdvManagerException("The HTTP Manager is not available");
        }
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if (otherManager instanceof ICicstsManagerSpi || otherManager instanceof IArtifactManager
                || otherManager instanceof IHttpManagerSpi) {
            return true;
        }

        return super.areYouProvisionalDependentOn(otherManager);
    }

    /**
     * Provides what is returned by the SdvUser annotation.
     *
     * <p>It will obtain an available zOS user from a pool users, which
     * matches the specified role. If no users are availble, this
     * function will throw an <tt>ResourceUnavailableException</tt> exception
     * which will result in the Galasa framework cancelling the test run, and
     * requeuing it to reattempt the test again after a period of time.
     *
     * <p>The user will be added to the list of SDV Users within the
     * RecordingRegion object.
     *
     * @param field -
     * @param annotations -
     * @return - ISdvUser
     * @throws SdvManagerException generic errors encountered.
     * @throws ResourceUnavailableException no available users in the pool. Galasa framework
     *      will handle.
     */
    @GenerateAnnotatedField(annotation = SdvUser.class)
    public ISdvUser getSdvUser(Field field, List<Annotation> annotations)
            throws SdvManagerException, ResourceUnavailableException {
        SdvUser annotation = field.getAnnotation(SdvUser.class);

        String cicsTag = defaultString(annotation.cicsTag(), "PRIMARY").toUpperCase(Locale.ROOT);
        String roleTag = annotation.roleTag().toUpperCase(Locale.ROOT);

        if (roleTag.isBlank()) {
            throw new SdvManagerException(
                    "SdvUser " + field.getName() + " cannot have a blank RoleTag.");
        }

        String role = SdvRole.get(roleTag);
        if (role == null) {
            throw new SdvManagerException(
                    "Cannot find role. Please create or update CPS Property 'sdv.roleTag."
                    + roleTag + ".role'.");
        }

        ICicsRegionProvisioned region = this.cicsManager.getTaggedCicsRegions().get(cicsTag);
        if (region == null) {
            throw new SdvManagerException(
                    "Unable to setup SDV User '" + field.getName() + "', for region with tag '"
                            + cicsTag + "' as a region with a matching 'cicsTag' tag was not found"
                            + ", or the region was not provisioned.");
        }

        // Check a port has been given for the CICS region under test.
        String port = SdvPort.get(region.getTag());
        if (port == null) {
            throw new SdvManagerException(
                "Could not find port. Please create or update CPS property 'sdv.cicsTag."
                + region.getTag() + ".port'.");
        }

        // Check an HLQ has been given for the CICS region under test.
        String hlq = SdvHlq.get(region.getTag());
        if (hlq == null) {
            throw new SdvManagerException(
                "Could not find HLQ. Please create or update CPS property 'sdv.cicsTag."
                + region.getTag() + ".hlq'.");
        }

        // This can throw a ResourceUnavailableException, do not capture this
        // let the framework handle it as it will re-queue the test for re-run
        // later
        String credentialTag = "";
        credentialTag = this.sdvUserPool.allocateUser(role, region);

        ICredentialsUsernamePassword credsObj = null;
        try {
            credsObj = (ICredentialsUsernamePassword) getFramework().getCredentialsService()
                    .getCredentials(credentialTag);
        } catch (CredentialsException e) {
            throw new SdvManagerException(
                    "No credentials were found with the tag: " + credentialTag, e);
        }

        if (credsObj == null) {
            throw new SdvManagerException(
                    "No credentials were found with the tag: " + credentialTag);
        }

        cts.registerText(credsObj.getPassword(),
                "Password for credential tag: " + credentialTag);

        SdvUserImpl newSdvUser = new SdvUserImpl(credentialTag, credsObj, cicsTag, role);
        sdvUsersToRecordList.add(newSdvUser);

        return newSdvUser;

    }

    private void releaseUsers() throws CicstsManagerException {
        // Release the users back to the pool
        for (ISdvUser sdvUser : sdvUsersToRecordList) {
            try {
                SdvUserPool.deleteDss(
                    sdvUser.getCredentialsTag(),
                    cicsManager.locateCicsRegion(sdvUser.getCicsTag()).getApplid(),
                    getFramework().getTestRunName(),
                    this.dss
                );
            } catch (DynamicStatusStoreException | CicstsManagerException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not release SDV User:  " + sdvUser.getCredentialsTag()
                            + ", on CICS region "
                            + cicsManager.locateCicsRegion(sdvUser.getCicsTag()).getApplid()
                            + ", for test run " + getFramework().getTestRunName(), e);
                }
            }
        }
    }

    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {

        // *** Auto generate the fields
        generateAnnotatedFields(SdvManagerField.class);

        // Create a list of regions, and their associated recordings
        // per user
        LOG.info("Populating list of CICS regions under test and recordings "
                + "required for each user.");

        try {
            for (Map.Entry<String, ICicsRegionProvisioned> entry :
                cicsManager.getTaggedCicsRegions()
                    .entrySet()) {

                // Check CICS region is running version 750 or greater
                if (entry.getValue().getVersion().isEarlierThan(ProductVersion.v(750))) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("SDV recording will not take place on CICS region '"
                                + entry.getValue().getApplid()
                                + "'. Running version earlier than 750.");
                    }
                    continue;
                }

                // Check CICS region has SEC=YES set
                if (entry.getValue().getRegionJob().retrieveOutputAsString()
                    .contains("DFHXS1102I")) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("SDV recording will not take place on CICS region '"
                                + entry.getValue().getApplid() + "'. Security is not active.");
                    }
                    continue;
                }

                // Get list of users for this CICS region
                List<ISdvUser> listOfUsersForRegion =
                        sdvUsersToRecordList.stream()
                            .filter(u -> entry.getKey().equals(u.getCicsTag()))
                                .collect(Collectors.toList());

                if (!listOfUsersForRegion.isEmpty()) {
                    // Create maintenance terminal for CICS region as
                    // we know we definitely plan to record something there
                    // and will need this to create resources
                    ICicsTerminal terminal;
                    terminal = cicsManager.generateCicsTerminal(entry.getKey());
                    RecordingRegion rr = new RecordingRegion(terminal);
                    recordingRegions.put(entry.getValue(), rr);

                    // Add each SdvUser associated to the region to the
                    // list of recording Users on that region
                    for (ISdvUser sdvUser : listOfUsersForRegion) {
                        if (recordingRegions.get(entry.getValue()).addUserToRecord(sdvUser)) {
                            if (LOG.isInfoEnabled()) {
                                LOG.info("Will record - CICS Region: "
                                        + entry.getValue().getApplid()
                                        + ", User: " + sdvUser.getUsername() + ", Role: "
                                        + sdvUser.getRole());
                            }
                        }
                    }
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(
                            "No users have been listed for recording via the "
                            + "SdvUser annotation for cicsTag '"
                            + entry.getValue().getTag()
                            + "'.");
                    }
                }
            }
            LOG.info("Finished populating list of tested CICS regions and recordings "
                    + "required for each user/role.");

            // Create instance of the HTTP SDV recording tool
            this.sdvRecorder = new SdvHttpRecorderImpl(getFramework(), recordingRegions,
                    artifactManager, batchManager, storedArtifactRoot, dss, httpManager);
        } catch (ManagerException e) {
            // If anything fails in here, we've already allocated the users, in
            // which case, release them.
            releaseUsers();
            throw e;
        }

    }

    @Override
    public void provisionStart() throws SdvManagerException {
        // Create necessary resources to run SDV
        sdvRecorder.prepareEnvironments(CFstructure);
    }

    @Override
    public void provisionStop() {
        // SDV is ending, possibly via an exception
        // so stop any known recordings & remove
        // all resources
        try {
            sdvRecorder.endRecording();
        } catch (SdvManagerException e) {
            LOG.error("Could not stop known SDC recordings in provisionStop.", e);
        }

        try {
            releaseUsers();
        } catch (CicstsManagerException e) {
            LOG.error("Could not release SDV SdvUsers in provisionStop.", e);
        }

        try {
            sdvRecorder.cleanUpEnvironments();
        } catch (SdvManagerException e) {
            LOG.error("Could not cleanup SDV environments in provisionStop.", e);
        }

    }

    @Override
    public void startOfTestClass() throws SdvManagerException {
        // Start recording before test run
        sdvRecorder.startRecording();
    }

    @Override
    public Result endOfTestClass(@NotNull Result currentResult, Throwable currentException)
            throws ManagerException {

        // Stop recording after test run
        sdvRecorder.endRecording();

        if (currentResult.isPassed()) {
            sdvRecorder.exportRecordings(getFramework().getTestRun().getTestBundleName(),
                    getFramework().getTestRun().getTestClassName());
        }

        // We are not going to change the result
        return super.endOfTestClass(currentResult, currentException);
    }
}
