/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosconsole.zosmf.manager.internal;

import java.util.ArrayList;
import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import com.google.gson.JsonObject;

import dev.galasa.zos.IZosImage;
import dev.galasa.zosconsole.IZosConsoleCommand;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosmf.IZosmf.ZosmfRequestType;
import dev.galasa.zosmf.IZosmfResponse;
import dev.galasa.zosmf.IZosmfRestApiProcessor;
import dev.galasa.zosmf.ZosmfException;

/**
 * Implementation of {@link IZosConsoleCommand} using zOS/MF
 *
 */
public class ZosmfZosConsoleCommandImpl implements IZosConsoleCommand {
    
    private IZosmfRestApiProcessor zosmfApiProcessor;

    private IZosImage image;
    private String consoleName;
    private String command;    
    private String commandImmediateResponse;
    private String commandResponseKey;
    private String commandDelayedResponse = "";
    
    private static final String SLASH = "/";
    private static final String RESTCONSOLE_PATH = SLASH + "zosmf" + SLASH + "restconsoles" + SLASH + "consoles" + SLASH;
    
    private static final Log logger = LogFactory.getLog(ZosmfZosConsoleCommandImpl.class);

    public ZosmfZosConsoleCommandImpl(IZosmfRestApiProcessor zosmfApiProcessor, String command, String consoleName, IZosImage image) {
        this.zosmfApiProcessor = zosmfApiProcessor;
        this.image = image;
        this.consoleName = consoleName;
        this.command = command;
    }

    public @NotNull IZosConsoleCommand issueCommand() throws ZosConsoleException {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("cmd", this.command);
        if (!isRouteCommand()) {
            requestBody.addProperty("system", this.image.getSysname());
        }
        
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.PUT_JSON, RESTCONSOLE_PATH + this.consoleName, null, requestBody, 
                    new ArrayList<>(Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_INTERNAL_SERVER_ERROR)), true);
        } catch (ZosmfException e) {
            throw new ZosConsoleException(e);
        }
        
        JsonObject content;
        try {
            content = response.getJsonContent();
        } catch (ZosmfException e) {
            throw new ZosConsoleException(logUnableToIsuueCommand());
        }
        
        logger.trace(content);
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            this.commandImmediateResponse = content.get("cmd-response").getAsString();
            this.commandResponseKey = content.get("cmd-response-key").getAsString();
        } else {
            // Error case - BAD_REQUEST or INTERNAL_SERVER_ERROR
            this.commandImmediateResponse = content.get("reason").getAsString();
            int commandErrorReturnCode = content.get("return-code").getAsInt();
            int commandErrorReasonCode = content.get("reason-code").getAsInt();
            logger.error("Command \"" + this.command + "\" failed. Reason=" + this.commandImmediateResponse + 
                    " Return Code=" + commandErrorReturnCode + " Reason Code=" + commandErrorReasonCode);
            throw new ZosConsoleException("Console command \"" + this.command + "\" failed. Reason \"" + this.commandImmediateResponse + "\"");
        }
        logger.info("Issued command: " + this.command);
                
        return this;
    }

    @Override
    public String getResponse() throws ZosConsoleException {
        return this.commandImmediateResponse;
    }

    @Override
    public String requestResponse() throws ZosConsoleException {
        IZosmfResponse response;
        try {
            response = this.zosmfApiProcessor.sendRequest(ZosmfRequestType.GET, RESTCONSOLE_PATH + this.consoleName + "/solmsgs/" + this.commandResponseKey, null, null, new ArrayList<>(Arrays.asList(HttpStatus.SC_OK)), true);
        } catch (ZosmfException e) {
            throw new ZosConsoleException(e);
        }
        
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            JsonObject content;
            try {
                content = response.getJsonContent();
            } catch (ZosmfException e) {
                throw new ZosConsoleException(logUnableToIsuueCommand());
            }
        
            logger.trace(content);
            this.commandDelayedResponse = content.get("cmd-response").getAsString();
        }
        return this.commandDelayedResponse;
    }

    @Override
    public String getCommand() {
        return this.command;
    }
    
    protected String logUnableToIsuueCommand() {
        return "Unable to issue console command \"" + this.command + "\"";
    }

    protected boolean isRouteCommand() {
        return this.command.startsWith("RO ") || this.command.startsWith("ROUTE ");
    }

    @Override
    public String toString() {
        String cir = this.commandImmediateResponse != null ? " RESPONSE:\n " + this.commandImmediateResponse : "";
        return "COMMAND=" + this.command + (this.image != null ? " IMAGE=" +  this.image.getImageID() : "") + cir;
    }
}
