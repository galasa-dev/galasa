/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosrseapi.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosrseapi.IRseapi;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;
import dev.galasa.zosrseapi.RseapiManagerException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;

public class RseapiRestApiProcessor implements IRseapiRestApiProcessor {
    
    private IRseapi currentRseapi;
    private String currentRseapiImageId;
    
    private final HashMap<String, IRseapi> rseapis = new LinkedHashMap<>();
    
    private static final Log logger = LogFactory.getLog(RseapiRestApiProcessor.class);
    
    public RseapiRestApiProcessor(Map<String, IRseapi> rseapis) {
        this.rseapis.putAll(rseapis);
        this.currentRseapiImageId = this.rseapis.entrySet().iterator().next().getKey();
        this.currentRseapi = this.rseapis.get(this.currentRseapiImageId);
    }
    
    
    /**
     * Send RSE API request
     * @param requestType
     * @param path
     * @param headers
     * @param body
     * @param validStatusCodes
     * @param convert
     * @return
     * @throws RseapiException
     */
    public @NotNull IRseapiResponse sendRequest(RseapiRequestType requestType, String path, 
        Map<String, String> headers, Object body, List<Integer> validStatusCodes, 
        boolean convert) throws RseapiException {

        if (validStatusCodes == null) {
            validStatusCodes = new ArrayList<>(Arrays.asList(HttpStatus.SC_OK));
        }
        IRseapiResponse response = null;
        for (int i = 0; i <= ((RseapiImpl) currentRseapi).getRequestRetry(); i++) {
            try {
                IRseapi rseapiServer = getCurrentRseapiServer();
                if (headers != null) {
                    for (Entry<String, String> entry : headers.entrySet()) {
                        rseapiServer.setHeader(entry.getKey(), entry.getValue());
                    }
                }
            	rseapiServer.setHeader("accept", "*/*");
                switch (requestType) {
	                case GET:
	                    response = rseapiServer.get(path, validStatusCodes, convert);
	                    break;
	                case PUT_JSON:
	                    response = rseapiServer.putJson(path, (JsonObject) body, validStatusCodes);
	                    break;
	                case PUT_TEXT:
	                    response = rseapiServer.putText(path, (String) body, validStatusCodes);
	                    break;
	                case PUT_BINARY:
	                    response = rseapiServer.putBinary(path, (byte[]) body, validStatusCodes);
	                    break;
	                case POST_JSON:
	                    response = rseapiServer.postJson(path, (JsonObject) body, validStatusCodes);
	                    break;
	                case DELETE:
	                    response = rseapiServer.delete(path, validStatusCodes);
	                    break; 
	                default:
	                    throw new RseapiException("Invalid request type");
                }
    
                if (validStatusCodes.contains(response.getStatusCode())) {
                    return response;
                } else {
                    logger.error("Expected HTTP status codes: " + validStatusCodes);
                    getNextRseapi();
                }
            } catch (RseapiManagerException e) {
                logger.error("Problem with RSE API request", e);
                getNextRseapi();
            }
        }
        throw new RseapiException("Unable to get valid response from RSE API server");
    }
    
    protected IRseapi getCurrentRseapiServer() {
        logger.debug("Using RSE API server on " + this.currentRseapi);
        this.currentRseapi.clearHeaders();
        return this.currentRseapi;
    }

    protected void getNextRseapi() {
        if (this.rseapis.size() == 1) {
            logger.debug("Only one RSE API server available");
            return;
        }
        Iterator<Entry<String, IRseapi>> rseapisIterator = this.rseapis.entrySet().iterator();
        while (rseapisIterator.hasNext()) {
            if (rseapisIterator.next().getKey().equals(this.currentRseapiImageId)) {
                Entry<String, IRseapi> entry;
                if (rseapisIterator.hasNext()) {
                    entry = rseapisIterator.next();
                } else {
                    entry = this.rseapis.entrySet().iterator().next();
                }
                this.currentRseapiImageId = entry.getKey();
                this.currentRseapi = this.rseapis.get(this.currentRseapiImageId);
                return;
            }
        }
        logger.debug("No alternate RSE API server available");
    }
}
