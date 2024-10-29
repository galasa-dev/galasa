/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosfile.rseapi.manager.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.galasa.zosfile.ZosFileManagerException;
import dev.galasa.zosrseapi.IRseapi.RseapiRequestType;
import dev.galasa.zosrseapi.IRseapiResponse;
import dev.galasa.zosrseapi.IRseapiRestApiProcessor;
import dev.galasa.zosrseapi.RseapiException;

public class RseapiZosUnixCommand {
	private RseapiZosFileHandlerImpl zosFileHandler;
	private static final String PROP_INVOCATION = "invocation";
	private static final String PROP_PATH = "path";
	private static final String PROP_EXIT_CODE = "exit code";
	
	private static final String SLASH = "/";
    
	private static final String RESTUNIXCOMMANDS_PATH = SLASH + "rseapi" + SLASH + "api" + SLASH + "v1" + SLASH + "unixcommands";
    
    private static final Log logger = LogFactory.getLog(RseapiZosUnixCommand.class);
	
    public RseapiZosUnixCommand(RseapiZosFileHandlerImpl zosFileHandler) {
    	this.zosFileHandler = zosFileHandler;
    }
    
    public JsonObject execute(IRseapiRestApiProcessor rseapiApiProcessor, String command) throws ZosFileManagerException {
        IRseapiResponse response;
        try {
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty(PROP_INVOCATION, command);
            requestBody.addProperty(PROP_PATH, "/usr/bin");
			response = rseapiApiProcessor.sendRequest(RseapiRequestType.POST_JSON, RESTUNIXCOMMANDS_PATH, null, requestBody, RseapiZosFileHandlerImpl.VALID_STATUS_CODES, false);
        } catch (RseapiException e) {
            throw new ZosFileManagerException(e);
        }

        if (response.getStatusCode() != HttpStatus.SC_OK) {
        	// Error case
            String displayMessage = this.zosFileHandler.buildErrorString("zOS UNIX command", response); 
            logger.error(displayMessage);
            throw new ZosFileManagerException(displayMessage);
        }
        
        JsonObject responseBody;
        try {
            responseBody = response.getJsonContent();
        } catch (RseapiException e) {
            throw new ZosFileManagerException("Issue command failed", e);
        }
        
        logger.trace(responseBody);
        JsonElement exitCode = responseBody.get(PROP_EXIT_CODE);
        if (exitCode == null || exitCode.getAsInt() != 0) {
        	String displayMessage = "Command failed. Response body:\n" + responseBody;
            logger.error(displayMessage);
            throw new ZosFileManagerException(displayMessage);
        }
        
        return responseBody;
    }
}
