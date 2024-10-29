/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.internal.properties.SdvHlq;
import dev.galasa.sdv.internal.properties.SdvSdcActivation;
import dev.galasa.sdv.internal.properties.SdvSrrLogstreamRemoval;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is the abstract base class for all SDV recorders.
 *
 * <p>It contains all common implementations for any SDV records.
 *
 * <p>It expects start/end recording functions to be overridden and implemented by specific child
 * classes, but provides common resource creation/teardown, and YAML job generation.
 *
 */
public abstract class SdvRecorderImpl {

    private static final Log LOG = LogFactory.getLog(SdvRecorderImpl.class);

    protected Map<ICicsRegion, RecordingRegion> recordingRegions;

    private Path storedArtifactRoot;
    private IArtifactManager artifactManager;
    private IZosBatchSpi batchManager;
    private IDynamicStatusStoreService dss;
    private IFramework framework;

    protected static final String CICS_RESOURCES_GROUP_NAME = "SDVGRP";

    private static final String DSS_MANAGER_PREFIX = "manager.";
    private static final String DSS_RUNNING_MANAGERS_TAG = "runningManagers.";
    private static final String DSS_SDC_LIVE_TAG = ".sdcLive";

    /**
     * SdvRecorderImpl constructor.
     *
     * @param framework - Galasa framework
     * @param recordingRegions - A unique Map of CICS regions under test.
     * @param artifactManager - Galasa Artifact Manager.
     * @param batchManager - Galasa Batch Manager.
     * @param storedArtifactRoot - The path where files should be stored to be included as test
     *        artifacts.
     * @param dss - Galasa DSS.
     */
    public SdvRecorderImpl(IFramework framework, Map<ICicsRegion, RecordingRegion> recordingRegions,
            IArtifactManager artifactManager, IZosBatchSpi batchManager, Path storedArtifactRoot,
            IDynamicStatusStoreService dss) {
        this.framework = framework;
        this.recordingRegions = recordingRegions;
        this.artifactManager = artifactManager;
        this.batchManager = batchManager;
        this.storedArtifactRoot = storedArtifactRoot;
        this.dss = dss;
    }

    abstract void startRecording() throws SdvManagerException;

    abstract void endRecording() throws SdvManagerException;

    /**
     * Implements the full configuration of SDC on CICS regions under test.
     *
     * <p>It firstly consults the DSS to see if any other managers are running on a
     * given CICS region, which will have already configured SDC. If no regions are found, and
     * the SDV config states SDC should be configured, this manager adds an entry to the DSS to
     * claim it will configure SDC. A list of running managers in the DSS is then created and
     * updated. If another manager is found, this manager will only add to the list of running
     * managers on the region and will skip SDC configuration.
     *
     * <p>See <tt>cleanUpEnvironments</tt> to see how the running manager list is used.
     *
     * @param cfStructure - Coupling Facility structure name to use when creating logstream.
     * @throws SdvManagerException general error encountered.
     */
    public void prepareEnvironments(String cfStructure) throws SdvManagerException {
        for (Map.Entry<ICicsRegion, RecordingRegion> entry : recordingRegions.entrySet()) {
            try {
                // Attempt to see if we are the first SDV manager working on the region, then
                // create the runningManagers prop. Then configure SDC, if necessary.
                if (LOG.isInfoEnabled()) {
                    LOG.info("Attempting to find other SDV managers running on "
                            + entry.getKey().getApplid());
                }

                if (dss.putSwap(
                        DSS_MANAGER_PREFIX + DSS_RUNNING_MANAGERS_TAG + entry.getKey().getApplid(),
                        null, framework.getTestRunName())) {
                    dss.put(DSS_MANAGER_PREFIX + entry.getKey().getApplid() + DSS_SDC_LIVE_TAG,
                            "false");

                    if (LOG.isInfoEnabled()) {
                        LOG.info("No other SDV managers found running on "
                                + entry.getKey().getApplid() + ". Created runningManagers list.");
                    }

                    if (SdvSdcActivation.get(entry.getKey().getTag())) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                    "Activating SDC on CICS region: " + entry.getKey().getApplid());
                        }

                        createSrrLogstream(entry.getKey(), cfStructure);
                        createCicsResources(entry.getKey(),
                                entry.getValue().getMaintenanceTerminal());
                    }

                    dss.put(DSS_MANAGER_PREFIX + entry.getKey().getApplid() + DSS_SDC_LIVE_TAG,
                            "true");
                } else {
                    // We are not the first SDV manager running on the region, therefore
                    // SDC will already be set up (or is currently in the process of being set up),
                    // if necessary.
                    // Add to the running manager list only, and wait for SDC to be reported as live
                    // before continuing.
                    Set<String> runningManagers = new HashSet<String>(
                        Arrays.asList(dss.get(DSS_MANAGER_PREFIX
                            + DSS_RUNNING_MANAGERS_TAG + entry.getKey().getApplid()).split(",")));

                    if (LOG.isInfoEnabled()) {
                        LOG.info(runningManagers.size() + " other SDV managers found running on "
                                + entry.getKey().getApplid() + ". Adding new manager to list.");
                    }

                    runningManagers.add(framework.getTestRunName());
                    dss.put(
                        DSS_MANAGER_PREFIX + DSS_RUNNING_MANAGERS_TAG + entry.getKey().getApplid(),
                        String.join(",", runningManagers));

                    // Wait for sdcLive to become True before continuing
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Will wait for SDC to become live on "
                                + entry.getKey().getApplid());
                    }

                    int i = 0;
                    while (!"true".equals(dss.get(
                            DSS_MANAGER_PREFIX + entry.getKey().getApplid() + DSS_SDC_LIVE_TAG))
                            && i++ < 20) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("SDC not live, reattempting on " + entry.getKey().getApplid()
                                    + "...");
                        }
                        Thread.sleep(1000);
                    }

                    if (LOG.isInfoEnabled()) {
                        LOG.info(
                            "SDC made live by another SDV manager, continuing on "
                            + entry.getKey().getApplid()
                        );
                    }
                }
            } catch (DynamicStatusStoreException | InterruptedException e) {
                throw new SdvManagerException("Unable interact with DSS for SDV.", e);
            }
        }
    }

    /**
     * Implements the teardown of SDC on CICS regions under test.
     *
     * <p>It firstly consults the DSS to see if it is the last manager running on the given CICS
     * region. If it is, and the SDV config states SDC should be configured, this manager will
     * deconfigure SDC on the region and remove all resources. It will then remove entries from the
     * DSS related to this. If more than one manager is found, this manager will only decrement the
     * count of running managers on the region.
     *
     * <p>SRR Logstream removal is not done by default but can be configured to do so via the CPS
     * properties. It would be essential to do this if provisioning a CICS region for the lifespan
     * of the test run, to ensure no artifacts are left behind.
     *
     * @throws SdvManagerException general error encountered.
     */
    public void cleanUpEnvironments() throws SdvManagerException {
        for (Map.Entry<ICicsRegion, RecordingRegion> entry : recordingRegions.entrySet()) {

            try {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Attempting to find other SDV managers running on "
                            + entry.getKey().getApplid());
                }

                // Get number of running SDV managers.
                Set<String> runningManagers = new HashSet<String>(
                        Arrays.asList(dss.get(DSS_MANAGER_PREFIX
                            + DSS_RUNNING_MANAGERS_TAG + entry.getKey().getApplid()).split(",")));

                if (LOG.isInfoEnabled()) {
                    LOG.info(runningManagers + " SDV managers found running on "
                            + entry.getKey().getApplid() + ".");
                }

                // If we are the last running for a
                // region, clean up the resources on your way out, if necessary.
                if (runningManagers.size() == 1) {
                    // Remove entry from DSS
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Removing DSS entry, as this is the final SDV manager on "
                                + entry.getKey().getApplid() + ".");
                    }

                    dss.delete(
                        DSS_MANAGER_PREFIX + DSS_RUNNING_MANAGERS_TAG + entry.getKey().getApplid()
                    );

                    if (SdvSdcActivation.get(entry.getKey().getTag())) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Cleaning up CICS region after SDV recording: "
                                    + entry.getKey().getApplid());
                        }

                        deleteCicsResources(entry.getKey(),
                                entry.getValue().getMaintenanceTerminal());
                    }

                    if (SdvSrrLogstreamRemoval.get(entry.getKey().getTag())) {
                        deleteSrrLogstream(entry.getKey());
                    }

                    dss.delete(DSS_MANAGER_PREFIX + entry.getKey().getApplid() + DSS_SDC_LIVE_TAG);
                } else {
                    // We are not the SDV manager running, so just remove yourself
                    // from the equation & leave the last manager to do the tidying up
                    if (runningManagers != null) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info(
                                "Removing " + framework.getTestRunName()
                                + " from running SDV manager list on " + entry.getKey().getApplid()
                            );
                        }

                        runningManagers.remove(framework.getTestRunName());

                        dss.put(
                            DSS_MANAGER_PREFIX
                            + DSS_RUNNING_MANAGERS_TAG + entry.getKey().getApplid(),
                            String.join(",", runningManagers)
                        );
                    }
                }
            } catch (DynamicStatusStoreException e) {
                throw new SdvManagerException("Unable interact with DSS for SDV.", e);
            }
        }
    }

    /**
     * Orchestrates the generation and storage of the Security YAML for each CICS region under test.
     *
     * <p>It loops through each CICS region under test, obtains the combined YAML
     * for all users under test, then stores it as part of the test runs stored artifacts.
     *
     */
    public void exportRecordings(String testBundleName, String testClassName)
            throws SdvManagerException {
        for (Map.Entry<ICicsRegion, RecordingRegion> entry : recordingRegions.entrySet()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Exporting SDV Security YAML for CICS region: "
                        + entry.getKey().getApplid());
            }

            String regionYaml = getRegionSecurityYaml(entry.getKey(), entry.getValue());
            storeYaml(entry.getKey(), regionYaml, testBundleName, testClassName);
        }
    }

    private void createSrrLogstream(ICicsRegion region, String cfStructure)
            throws SdvManagerException {

        if (LOG.isInfoEnabled()) {
            LOG.info("Attepmting to create LOGSTREAM on " + region.getApplid());
        }

        try {
            Map<String, Object> attrs = new HashMap<String, Object>();
            attrs.put("OWNER", region.getRegionJob().getOwner());
            attrs.put("APPLID", region.getApplid());
            attrs.put("CFSTRUCT", cfStructure);

            String jcl = artifactManager.getBundleResources(this.getClass())
                    .retrieveSkeletonFileAsString("/jcl/definelogstream.jcl", attrs).trim();

            IZosBatchJob job = batchManager.getZosBatch(region.getZosImage()).submitJob(jcl, null);
            int rc = job.waitForJob();
            if (rc == 12) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        "Logstream not created, using existing, on CICS Region "
                        + region.getApplid()
                        + "."
                    );
                }
            } else if (rc > 4) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(
                        "JCL to define logstreams fail for CICS Region "
                        + region.getApplid()
                        + ", check artifacts for more details"
                    );
                }
            }
        } catch (ZosBatchException | TestBundleResourceException | IOException
                | CicstsManagerException e) {
            throw new SdvManagerException(
                "Unable to run JCL to define logstreams for CICS Region "
                + region.getApplid(),
                e
            );
        }
    }

    private void deleteSrrLogstream(ICicsRegion region) throws SdvManagerException {

        if (LOG.isInfoEnabled()) {
            LOG.info("Deleting LOGSTREAM on " + region.getApplid());
        }

        try {
            Map<String, Object> attrs = new HashMap<String, Object>();
            attrs.put("OWNER", region.getRegionJob().getOwner());
            attrs.put("APPLID", region.getApplid());

            String jcl = artifactManager.getBundleResources(this.getClass())
                    .retrieveSkeletonFileAsString("/jcl/deletelogstreams.jcl", attrs).trim();

            IZosBatchJob job = batchManager.getZosBatch(region.getZosImage()).submitJob(jcl, null);
            int rc = job.waitForJob();
            if (rc > 4) {
                throw new SdvManagerException(
                        "JCL to delete logstreams fail on CICS Region "
                        + region.getApplid()
                        + ", check artifacts for more details."
                );
            }
        } catch (ZosBatchException | TestBundleResourceException | IOException
                | CicstsManagerException e) {
            throw new SdvManagerException(
                "Unable to run JCL to delete logstreams for CICS Region "
                + region.getApplid(),
                e
            );
        }

    }

    protected void createCicsResources(ICicsRegion region, ICicsTerminal terminal)
            throws SdvManagerException {

        // JOURNALMODEL
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating JOURNALMODEL on " + region.getApplid());
        }
        try {
            region.ceda().createResource(terminal, "JOURNALMODEL", "SRR", CICS_RESOURCES_GROUP_NAME,
                    "JOURNALNAME(DFHSECR) DESCRIPTION(SRR JOURNAL) TYPE(MVS) STREAMNAME("
                            + region.getRegionJob().getOwner() + "." + region.getApplid()
                            + ".DFHSECR)");
        } catch (CicstsManagerException e) {
            throw new SdvManagerException(
                "Could not create SRR JOURNALMODEL definition on CICS Region "
                + region.getApplid()
                + ".",
                e
            );
        }

        try {
            region.ceda().installGroup(terminal, "DFHXSD");
        } catch (CicstsManagerException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info(
                    "Couldn't install DFHXSD, already installed on CICS Region "
                    + region.getApplid()
                );
            }
        }
    }

    protected void deleteCicsResources(ICicsRegion region, ICicsTerminal terminal) {

        if (LOG.isInfoEnabled()) {
            LOG.info("Deleting the " + CICS_RESOURCES_GROUP_NAME + " group on "
                    + region.getApplid());
        }
        try {
            region.ceda().deleteGroup(terminal, CICS_RESOURCES_GROUP_NAME);
        } catch (CicstsManagerException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    "Could not delete the "
                    + CICS_RESOURCES_GROUP_NAME
                    + " on CICS Region "
                    + region.getApplid(),
                    e
                );
            }
        }
    }

    private String getRegionSecurityYaml(ICicsRegion region, RecordingRegion recordingRegionData)
            throws SdvManagerException {

        String yaml = "";
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("Building Security metadata job JCL for " + region.getApplid());
            }
            Map<String, Object> attrs = new HashMap<String, Object>();
            attrs.put("OWNER", region.getRegionJob().getOwner());
            attrs.put("APPLID", region.getApplid());
            attrs.put("CICSHLQ", SdvHlq.get(region.getTag()));

            String jcl = artifactManager.getBundleResources(this.getClass())
                    .retrieveSkeletonFileAsString("/jcl/getYaml.jcl", attrs).trim();

            // Append to the JCL user and their role recorded on the region
            // for the test
            Boolean srrFound = false;
            List<String> srrIds = new ArrayList<>();
            for (ISdvUser recordingUser : recordingRegionData.getRecordingUsers()) {
                if (recordingUser.getSrrId() != null) {
                    srrFound = true;
                    srrIds.add(recordingUser.getSrrId());
                    jcl = jcl + "\nMATCHID=" + recordingUser.getSrrId() + "\nUSERID="
                            + recordingUser.getUsername() + ",ROLE=" + recordingUser.getRole();
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("No SDC registered for user " + recordingUser.getUsername()
                                + " on region " + region.getApplid()
                                + ", skipping YAML generation.");
                    }
                }
            }

            if (!srrFound) {
                return null;
            }

            // Add final line indicating the end of the DDIN
            jcl = jcl + "\n/*\n//";

            if (LOG.isInfoEnabled()) {
                LOG.info("Submitting Security metadata job JCL for " + region.getApplid());
            }
            IZosBatchJob job = batchManager.getZosBatch(region.getZosImage()).submitJob(jcl, null);
            int rc = job.waitForJob();

            if (rc > 4) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        "JCL to get Security metadata fail on CICS Region "
                        + region.getApplid()
                        + ", check artifacts for more details"
                    );
                }
            }

            if (LOG.isInfoEnabled()) {
                LOG.info(
                    "Grabbing Security metadata job output on CICS Region "
                    + region.getApplid()
                );
            }
            List<IZosBatchJobOutputSpoolFile> spoolFiles;
            spoolFiles = job.retrieveOutput().getSpoolFiles();

            IZosBatchJobOutputSpoolFile yamlFile = spoolFiles.stream()
                    .filter(file -> "YAML".equals(file.getDdname())).findAny().orElse(null);

            if (yamlFile != null) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("YAML output obtained on " + region.getApplid());
                }
                yaml = yamlFile.getRecords();
            } else {
                throw new SdvManagerException(
                    "Security metadata job did not return any YAML for CICS Region "
                    + region.getApplid()
                    + ", containing SRR IDs: "
                    + String.join(",", srrIds)
                );
            }

        } catch (ZosBatchException | TestBundleResourceException | IOException
                | CicstsManagerException e) {
            throw new SdvManagerException(
                "Unable to run JCL to get Security metadata on CICS Region "
                + region.getApplid(),
                e
            );
        }

        return yaml;

    }

    private void storeYaml(ICicsRegion region, String regionYaml, String testBundleName,
            String testClassName) throws SdvManagerException {

        if (regionYaml != null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Storing YAML as test artifact for " + region.getApplid());
            }

            // Add to Galasa run
            Path f = storedArtifactRoot.resolve(testBundleName + "/" + testClassName + "."
                    + region.getTag() + ".cics-security.yaml");
            try {
                Files.write(f, regionYaml.getBytes("utf8"), StandardOpenOption.CREATE);
            } catch (Exception e) {
                throw new SdvManagerException(
                    "Unable to add YAML to Galasa run for CICS Region "
                    + region.getApplid()
                    + ". Attempting to save to path: " + f.toString(),
                    e
                );
            }
        }
    }
}
