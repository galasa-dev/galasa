/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.sdv.internal;

import com.google.gson.JsonObject;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.http.HttpClientException;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.internal.properties.SdvPort;
import dev.galasa.zosbatch.spi.IZosBatchSpi;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class contains HTTP specific implementations of the SdvRecorderImpl abstract class.
 *
 * <p>A number of functions from SdvRecorderImpl are overridden to provide HTTP recorder-only
 * functionality. In particular, the creation and teardown of HTTP specific resources, and the
 * start/end of SDC recording via HTTP endpoints.
 *
 */
public class SdvHttpRecorderImpl extends SdvRecorderImpl {

    private static final String TCPIPSERVICE_TEXT = "TCPIPSERVICE";
    private static final String SDV_TCPIPSERVICE_RESOURCE_NAME = "SDVXSDT";
    private static final String URIMAP_TEXT = "URIMAP";
    private static final String SDV_URIMAP_RESOURCE_NAME = "SDVXSDU";
    private static final String SDV_HTTP_ENDPOINT_PATH = "/DFHSDC";
    private static final String ON_CICS_REGION_MSG = "', on CICS Region ";
    private static final String STATUS_CODE_MSG = ". Status code: ";

    private static final Log LOG = LogFactory.getLog(SdvHttpRecorderImpl.class);

    private IHttpManagerSpi httpManager;

    /**
     * The HTTP recorder constructor, which instantiates super class SdvRecorderImpl, then
     * additionally takes an http manager.
     *
     * @param framework - Galasa framework
     * @param recordingRegions - A unique Map of CICS regions under test.
     * @param artifactManager - Galasa Artifact Manager.
     * @param batchManager - Galasa Batch Manager.
     * @param storedArtifactRoot - The path where files should be stored to be included as test
     *        artifacts.
     * @param dss - Galasa DSS.
     * @param httpManager - Galasa HTTP Manager.
     */
    public SdvHttpRecorderImpl(IFramework framework, Map<ICicsRegion,
            RecordingRegion> recordingRegions, IArtifactManager artifactManager,
            IZosBatchSpi batchManager, Path storedArtifactRoot,
            IDynamicStatusStoreService dss, IHttpManagerSpi httpManager) {
        super(framework, recordingRegions, artifactManager, batchManager, storedArtifactRoot, dss);

        this.httpManager = httpManager;
    }

    @Override
    protected void createCicsResources(ICicsRegion region, ICicsTerminal terminal)
            throws SdvManagerException {

        // Deleting possible remains of a previous run
        if (LOG.isInfoEnabled()) {
            LOG.info("Deleting any existing resources on " + region.getApplid());
        }
        try {
            if (region.ceda().resourceExists(terminal, TCPIPSERVICE_TEXT,
                    SDV_TCPIPSERVICE_RESOURCE_NAME, CICS_RESOURCES_GROUP_NAME)) {
                region.cemt().setResource(terminal, TCPIPSERVICE_TEXT,
                        SDV_TCPIPSERVICE_RESOURCE_NAME, "CLOSED");
            }
            if (region.ceda().resourceExists(terminal, URIMAP_TEXT, SDV_URIMAP_RESOURCE_NAME,
                    CICS_RESOURCES_GROUP_NAME)) {
                region.cemt().setResource(terminal, URIMAP_TEXT, SDV_URIMAP_RESOURCE_NAME,
                        "DISABLED");
            }
            region.ceda().deleteGroup(terminal, CICS_RESOURCES_GROUP_NAME);
        } catch (CicstsManagerException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("CICS resource disabling expectedly failed on " + region.getApplid());
            }
        }

        // Run common SDC pre-reqs
        super.createCicsResources(region, terminal);

        // Run HTTP specific pre-reqs
        // TCPIPSERVICE
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating TCPIPSERVICE on " + region.getApplid());
        }

        String port = SdvPort.get(region.getTag());
        if (port == null) {
            throw new SdvManagerException(
                    "Could not find SDC port in CPS properties for CICS tag: " + region.getTag()
            );
        }

        try {
            region.ceda().createResource(terminal, TCPIPSERVICE_TEXT,
                    SDV_TCPIPSERVICE_RESOURCE_NAME, CICS_RESOURCES_GROUP_NAME,
                    "TRANSACTION(CWXN) PORTNUMBER(" + port + ") AUTHENTICATE(BASIC)"
                            + "PROTOCOL(HTTP)");
        } catch (CicstsManagerException e) {
            throw new SdvManagerException(
                "Could not create TCPIPSERVICE on CICS Region " + region.getApplid(), e
            );
        }

        // URIMAP
        if (LOG.isInfoEnabled()) {
            LOG.info("Creating URIMAP on " + region.getApplid());
        }
        try {
            region.ceda().createResource(terminal, URIMAP_TEXT, SDV_URIMAP_RESOURCE_NAME,
                    CICS_RESOURCES_GROUP_NAME,
                    "USAGE(SERVER) SCHEME(HTTP) PATH(" + SDV_HTTP_ENDPOINT_PATH + "*) TCPIPSERVICE("
                            + SDV_TCPIPSERVICE_RESOURCE_NAME + ") "
                            + "PROGRAM(DFHXSJH) HOST(*) TRANSACTION(CXSD)");
        } catch (CicstsManagerException e) {
            throw new SdvManagerException(
                "Could not create URIMAP on CICS region " + region.getApplid(), e
            );
        }

        try {
            region.ceda().installGroup(terminal, CICS_RESOURCES_GROUP_NAME);
        } catch (CicstsManagerException e) {
            throw new SdvManagerException(
                "Could not install SDV resource group on CICS region " + region.getApplid(), e
            );
        }
    }

    @Override
    protected void deleteCicsResources(ICicsRegion region, ICicsTerminal terminal) {

        // URIMAP
        if (LOG.isInfoEnabled()) {
            LOG.info("Disabling URIMAP on " + region.getApplid());
        }
        try {
            region.cemt().setResource(terminal, URIMAP_TEXT, SDV_URIMAP_RESOURCE_NAME, "DISABLED");
        } catch (CicstsManagerException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create URIMAP on CICS Region " + region.getApplid());
            }
        }

        // TCPIPSERVICE
        if (LOG.isInfoEnabled()) {
            LOG.info("Disabling TCPIPSERVICE on " + region.getApplid());
        }
        try {
            region.cemt().setResource(terminal, TCPIPSERVICE_TEXT, SDV_TCPIPSERVICE_RESOURCE_NAME,
                    "CLOSED");
        } catch (CicstsManagerException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not create TCPIPSERVICE on CICS Region " + region.getApplid());
            }
        }

        // Delete common SDC resources
        super.deleteCicsResources(region, terminal);
    }

    @Override
    void startRecording() throws SdvManagerException {
        for (Map.Entry<ICicsRegion, RecordingRegion> entry : recordingRegions.entrySet()) {
            for (ISdvUser recordingUser : entry.getValue().getRecordingUsers()) {

                if (LOG.isInfoEnabled()) {
                    LOG.info("Starting SDV Recording for CICS region: " + entry.getKey().getApplid()
                            + ", User: " + recordingUser.getUsername());
                }
                try {
                    startRecordingUsingHttp(entry.getKey(), recordingUser);

                    if (LOG.isInfoEnabled()) {
                        LOG.info("Recording CICS region: " + entry.getKey().getApplid() + ", User: "
                                + recordingUser.getUsername() + ", SRR ID: "
                                + recordingUser.getSrrId());
                    }
                } catch (SdvManagerException e) {
                    throw new SdvManagerException(
                            "Was unable to start recording for user '"
                            + recordingUser.getUsername()
                            + ON_CICS_REGION_MSG
                            + entry.getKey().getApplid(), e);
                }
            }
        }
    }

    private void startRecordingUsingHttp(ICicsRegion cicsRegion, ISdvUser recordingUser)
            throws SdvManagerException {

        IHttpClient httpClient = this.httpManager.newHttpClient();

        try {
            httpClient.setURI(new URI("http://" + cicsRegion.getZosImage().getIpHost().getHostname()
                    + ":" + SdvPort.get(cicsRegion.getTag())));
        } catch (URISyntaxException e) {
            throw new SdvManagerException(
                "Badly formed URI for SDC service for CICS Region " + cicsRegion.getApplid(), e
            );
        }

        HttpClientResponse<JsonObject> response;
        httpClient.setAuthorisation(recordingUser.getUsername(), recordingUser.getPassword());

        // Check that an SDC isn't already running
        Boolean sdcAlreadyRunning = false;
        try {
            response = httpClient.getJson(SDV_HTTP_ENDPOINT_PATH);

            if (response.getStatusCode() == 200) {
                sdcAlreadyRunning = true;
            } else if (response.getStatusCode() == 404) {
                sdcAlreadyRunning = false;
            } else {
                JsonObject payload = response.getContent();
                String payloadStr = "";
                if (payload != null) {
                    payloadStr = payload.toString();
                }

                throw new SdvManagerException(
                        "Error whilst obtaining current SDC status for user '"
                        + recordingUser.getUsername()
                        + ON_CICS_REGION_MSG
                        + cicsRegion.getApplid()
                        + STATUS_CODE_MSG
                                + response.getStatusCode() + "\n" + response.getStatusMessage()
                                + "\n" + payloadStr);
            }
        } catch (HttpClientException e) {
            throw new SdvManagerException(
                    "Could not check status SDC recording status for user '"
                    + recordingUser.getUsername()
                    + ON_CICS_REGION_MSG
                    + cicsRegion.getApplid()
                    + ". Is SDC activated?", e);
        }

        // If an SDC is already running, stop it
        if (sdcAlreadyRunning) {
            try {
                JsonObject body = new JsonObject();
                body.addProperty("submit", false);
                response = httpClient.deleteJson(SDV_HTTP_ENDPOINT_PATH, body);

                if (response.getStatusCode() != 200) {
                    JsonObject payload = response.getContent();
                    String payloadStr = "";
                    if (payload != null) {
                        payloadStr = payload.toString();
                    }
                    throw new SdvManagerException(
                            "Could not stop SDC recording for user '"
                            + recordingUser.getUsername()
                            + ON_CICS_REGION_MSG
                            + cicsRegion.getApplid()
                            + STATUS_CODE_MSG
                            + response.getStatusCode()
                            + "\n" + response.getStatusMessage()
                            + "\n" + payloadStr);
                }
            } catch (HttpClientException e) {
                throw new SdvManagerException("Could not stop existing SDC recording for user '"
                + recordingUser.getUsername()
                + ON_CICS_REGION_MSG
                + cicsRegion.getApplid() + ".", e);
            }
        }

        try {
            JsonObject body = new JsonObject();
            response = httpClient.postJson(SDV_HTTP_ENDPOINT_PATH, body);

            if (response.getStatusCode() == 201) {
                if (response.getContent().has("srr_id")
                        && !response.getContent().get("srr_id").getAsString().isBlank()) {
                    recordingUser.setSrrId(response.getContent().get("srr_id").getAsString());
                } else {
                    throw new SdvManagerException(
                        "SDC recording did not return an SRR ID for user '"
                        + recordingUser.getUsername()
                        + ON_CICS_REGION_MSG
                        + cicsRegion.getApplid()
                    );
                }
            } else {
                JsonObject payload = response.getContent();
                String payloadStr = "";
                if (payload != null) {
                    payloadStr = payload.toString();
                }
                throw new SdvManagerException(
                        "Could not start SDC recording for user '"
                        + recordingUser.getUsername()
                        + ON_CICS_REGION_MSG
                        + cicsRegion.getApplid()
                        + STATUS_CODE_MSG
                        + response.getStatusCode()
                        + "\n" + response.getStatusMessage() + "\n" + payloadStr
                );
            }
        } catch (HttpClientException e) {
            throw new SdvManagerException(
                "Could not start SDC recording for user '"
                + recordingUser.getUsername()
                + ON_CICS_REGION_MSG
                + cicsRegion.getApplid(), e
            );
        }
    }

    @Override
    void endRecording() throws SdvManagerException {
        for (Map.Entry<ICicsRegion, RecordingRegion> entry : recordingRegions.entrySet()) {
            for (ISdvUser recordingUser : entry.getValue().getRecordingUsers()) {
                if (recordingUser.isRecording()) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Ending SDV Recording for region: " + entry.getKey().getApplid()
                                + ", User: " + recordingUser.getUsername() + ", SRR ID: "
                                + recordingUser.getSrrId());
                    }
                    try {
                        stopRecordingUsingHttp(entry.getKey(), recordingUser);
                    } catch (SdvManagerException e) {
                        throw new SdvManagerException(
                                "Unable to stop SRR recording "
                                + recordingUser.getSrrId()
                                + ", for user '"
                                + recordingUser.getUsername()
                                + ON_CICS_REGION_MSG
                                + entry.getKey().getApplid(), e
                        );
                    }
                }
            }
        }
    }

    private void stopRecordingUsingHttp(ICicsRegion cicsRegion, ISdvUser recordingUser)
            throws SdvManagerException {

        IHttpClient httpClient = this.httpManager.newHttpClient();

        try {
            httpClient.setURI(new URI("http://" + cicsRegion.getZosImage().getIpHost().getHostname()
                    + ":" + SdvPort.get(cicsRegion.getTag())));
        } catch (URISyntaxException e) {
            throw new SdvManagerException(
                "Badly formed URI for SDC service for CICS Region " + cicsRegion.getApplid(), e
            );
        }

        HttpClientResponse<JsonObject> response;
        httpClient.setAuthorisation(recordingUser.getUsername(), recordingUser.getPassword());

        try {
            JsonObject body = new JsonObject();
            body.addProperty("submit", false);
            response = httpClient.deleteJson(SDV_HTTP_ENDPOINT_PATH, body);

            if (response.getStatusCode() != 200) {
                JsonObject payload = response.getContent();
                String payloadStr = "";
                if (payload != null) {
                    payloadStr = payload.toString();
                }
                throw new SdvManagerException(
                        "Could not stop SDC recording for user '"
                        + recordingUser.getUsername()
                        + ON_CICS_REGION_MSG
                        + cicsRegion.getApplid()
                        + STATUS_CODE_MSG
                        + response.getStatusCode()
                        + "\n" + response.getStatusMessage()
                        + "\n" + payloadStr
                );
            }

            recordingUser.setNotRecording();
        } catch (HttpClientException e) {
            throw new SdvManagerException(
                "Could not stop existing SDC recording for user '"
                + recordingUser.getUsername()
                + ON_CICS_REGION_MSG
                + cicsRegion.getApplid(), e
            );
        }
    }

}
