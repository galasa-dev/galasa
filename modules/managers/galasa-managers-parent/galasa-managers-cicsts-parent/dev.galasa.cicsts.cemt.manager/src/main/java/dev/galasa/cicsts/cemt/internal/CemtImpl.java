/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.cemt.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import dev.galasa.cicsts.CemtException;
import dev.galasa.cicsts.CemtManagerException;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICemt;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.cemt.internal.properties.DefaultResourceTimeout;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

public class CemtImpl implements ICemt {

    private static final Log logger = LogFactory.getLog(CemtImpl.class);
    
    private ICicsRegion cicsRegion;

    private long defaultResourceTimeoutMilliseconds;
    
    public CemtImpl(ICicsRegion cicsRegion) throws CemtManagerException {
        this.cicsRegion = cicsRegion;
        this.defaultResourceTimeoutMilliseconds = DefaultResourceTimeout.get(cicsRegion.getZosImage())*1000;
    }

    protected CicstsHashMap getAttributes(String string, String resourceName, CicstsHashMap map) throws Exception {

        Pattern pattern = Pattern.compile("\\w*\\(\\s*[a-zA-z0-9.#:// ]*\\s*\\)");

        Matcher matcher = pattern.matcher(string);

        try {

            while(matcher.find()) {

                String matchedString = matcher.group();

                if(!matchedString.contains("INQUIRE")) {

                    String newString = matchedString.substring(0, matchedString.length() -1);

                    String[] parts = newString.split("\\(");

                    String key = parts[0].toLowerCase();

                    String value = null;

                    if(parts.length == 2) {
                        value = parts[1].trim();
                    }

                    if(value == null && !map.containsKey(key)) {

                        map.put(key, "");

                    }else if(map.containsKey(key) && value != null) {
                        if(!map.get(key).equals(value)) {
                            String mapValue = map.get(key);
                            map.put(key, (mapValue + value));
                        }

                    }else if(value != null){
                        map.put(key, value);
                    }

                }


            }
        }catch(Exception e) {
            throw new Exception("Error creating map", e);
        }

        return map;
    }

    @Override
    public CicstsHashMap inquireResource(@NotNull ICicsTerminal terminal,
            @NotNull String resourceType,
            @NotNull String resourceName) throws CemtException{

        if(cicsRegion != terminal.getCicsRegion()) {
            throw new CemtException("Terminal provided does not match CICS region.");
        }

        CicstsHashMap returnMap = new CicstsHashMap();


        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CemtException("Problem reset and clearing screen for CEMT transaction", e);
            }
        }

        try {
            terminal.type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();

            if(!terminal.retrieveScreen().contains("E " + "'" + resourceType + "' is not valid and is ignored.")) {
                terminal.waitForTextInField("STATUS: ");
            }else {
                throw new CemtException();
            }

        }catch(Exception e) {
            throw new CemtException("Problem with starting CEMT transaction");
        }


        try {
            if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
                terminal.pf9();
                terminal.waitForKeyboard();
                terminal.pf3();
                terminal.waitForKeyboard();
                terminal.clear();
                terminal.waitForKeyboard();
                return null;  
            }
        }catch(Exception e){
            throw new CemtException("Problem determining the result of the CEMT command", e);
        }

        try {
            terminal.tab().waitForKeyboard().enter().waitForKeyboard();

            if(!terminal.retrieveScreen().contains("RESULT - OVERTYPE TO MODIFY")) {
                throw new CemtException("Problem finding properties");
            }
        }catch(Exception e) {
            throw new CemtException("Problem retrieving properties for resource", e);
        }

        try {

            String terminalString = terminal.retrieveScreen();

            returnMap = getAttributes(terminalString, resourceName, returnMap);

            boolean pageDown = terminalString.contains("+");

            while(pageDown) {

                terminal.pf11().waitForKeyboard();
                terminalString = terminal.retrieveScreen();
                returnMap = getAttributes(terminalString, resourceName, returnMap);

                if(terminalString.indexOf("+") == terminalString.lastIndexOf("+")) {
                    pageDown = false;
                }

            }

        }catch(Exception e) {
            throw new CemtException("Problem whilst adding resource properties", e);
        }


        try {
            terminal.pf3();
            terminal.waitForKeyboard();
            terminal.clear();
            terminal.waitForKeyboard();
        }catch(Exception e) {
            throw new CemtException("Unable to return terminal back into reset state", e);
        }

        return returnMap;

    }

    
    @Override
    public boolean inquireResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType,
            @NotNull String resourceName, @NotNull String searchText) throws CemtException {
        if (terminal == null || resourceType == null || searchText == null) {
            throw new CemtException("Terminal or resourcetype or search text is not valid.");
        }
        if (cicsRegion != terminal.getCicsRegion()) {
            throw new CemtException("Terminal provided does not match CICS region.");
        }
        boolean found = false;
        try {
            terminal.resetAndClear().wfk();
            if (resourceName == null || resourceName.isBlank()) {
                terminal.type("CEMT INQUIRE " + resourceType).enter().waitForKeyboard();
            } else {
                terminal.type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
            }
            // checking whether resource type is valid or not
            if (!terminal.retrieveScreen().contains("E " + "'" + resourceType + "' is not valid and is ignored.")) {
                terminal.waitForTextInField("STATUS: ");
            } else {
                throw new CemtException();
            }
            if (!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
                terminal.pf9();
                terminal.waitForKeyboard();
                terminal.pf3();
                terminal.waitForKeyboard();
                terminal.clear();
                terminal.waitForKeyboard();
                terminal.resetAndClear();
                throw new CemtException("Normal response was not found.");
            }
            if(terminal.retrieveScreen().contains(searchText)) {
                found=true;
            }
            if(!found) {
                terminal.tab().waitForKeyboard().enter().waitForKeyboard();
                String terminalString = terminal.retrieveScreen();
                if (!terminalString.contains("RESULT - OVERTYPE TO MODIFY")) {
                    throw new CemtException("Problem finding properties");
                }          
                if(terminalString.contains(searchText)) {
                    found=true;
                }
                if(!found) {
                    terminalString = terminal.retrieveScreen();
                    boolean pageDown = terminalString.contains("+");
                    while (pageDown) {
                        terminal.pf11().waitForKeyboard();
                        terminalString = terminal.retrieveScreen();
                        found = terminalString.contains(searchText);
                        if (found || terminalString.indexOf("+") == terminalString.lastIndexOf("+")) {
                            pageDown = false;
                        }
                    }
                }
            }
            terminal.pf3();
            terminal.waitForKeyboard();
            terminal.clear();
            terminal.waitForKeyboard();
        } catch (CicstsManagerException e) {
            throw new CemtException("Problem reset and clearing screen for CEMT transaction", e);
        } catch (Exception e) {
            throw new CemtException("Problem with starting CEMT transaction");
        }
        return found;
    }


    @Override
    public CicstsHashMap setResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType, String resourceName,
            @NotNull String action) throws CemtException {

        if(cicsRegion != terminal.getCicsRegion()) {
            throw new CemtException("Terminal provided does not match CICS region.");
        }

        CicstsHashMap returnMap = new CicstsHashMap();

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CemtException("Problem reset and clearing screen for CEMT transaction", e);
            }
        }

        try {
            if(resourceName == null) {
                terminal.type("CEMT SET ALL " + resourceType + " " + action);
                terminal.enter();
                terminal.waitForKeyboard();
            }else {
                terminal.type("CEMT SET " + resourceType + "(" + resourceName + ") " + action);
                terminal.enter();
                terminal.waitForKeyboard();
            }

            terminal.waitForTextInField("STATUS: ");
        }catch(Exception e) {
            throw new CemtException("Problem with starting the CEMT transaction", e);
        }

        try {
            if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
                terminal.pf9().waitForKeyboard();
                throw new CemtException("Errors detected whilst setting resource");
            }
        }catch(Exception e) {
            throw new CemtException("Problem determining the result from the CEMT command", e);
        }

        try {
            terminal.tab().waitForKeyboard().enter().waitForKeyboard();
        }catch(Exception e) {
            throw new CemtException("Problem retrieving properties for resource", e);
        }

        try {

            String terminalString = terminal.retrieveScreen();

            returnMap = getAttributes(terminalString, resourceName, returnMap);

            boolean pageDown = terminalString.contains("+");

            while(pageDown) {

                terminal.pf11().waitForKeyboard();
                terminalString = terminal.retrieveScreen();
                returnMap = getAttributes(terminalString, resourceName, returnMap);

                if(terminalString.indexOf("+") == terminalString.lastIndexOf("+")) {
                    pageDown = false;
                }

            }

        }catch(Exception e) {
            throw new CemtException("Problem whilst adding resource properties", e);
        }

        try {
            terminal.pf3();
            terminal.waitForKeyboard();
            terminal.clear();
            terminal.waitForKeyboard();
        }catch(Exception e) {
            throw new CemtException("Unable to return terminal back into reset state", e);
        }

        return returnMap;

    }


    @Override
    public void discardResource(@NotNull ICicsTerminal terminal, @NotNull String resourceType,
            @NotNull String resourceName) throws CemtException {

        if(cicsRegion != terminal.getCicsRegion()) {
            throw new CemtException("Terminal provided does not match CICS region.");
        }

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CemtException("Problem reset and clearing screen for CEMT transaction", e);
            }
        }


        try {
            if(resourceName == null) {
                terminal.type("CEMT DISCARD " + resourceType).enter().waitForKeyboard();
            }else {
                terminal.type("CEMT DISCARD " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
            }

            terminal.waitForTextInField("STATUS: ");
        }catch(Exception e) {
            throw new CemtException("Problem with starting the CEMT transaction", e);
        }

        try {

            if(!terminal.retrieveScreen().contains("RESPONSE: NORMAL")) {
                terminal.pf9();
                terminal.pf3();
                terminal.waitForKeyboard();
                terminal.clear();
                terminal.waitForKeyboard();
                throw new CemtException("Errors detected whilst setting resource");
            }
        }catch(Exception e) {
            throw new CemtException("Problem determining the result from the CEMT command");
        }

        try {
            terminal.pf3();
            terminal.waitForKeyboard();
            terminal.clear();
            terminal.waitForKeyboard();
        }catch(Exception e) {
            throw new CemtException("Unable to return terminal back into reset state", e);
        }


    }

    @Override
    public boolean performSystemProperty(@NotNull ICicsTerminal terminal, @NotNull String systemArea,
            @NotNull String setRequest, @NotNull String expectedResponse) throws CemtException {

        if(cicsRegion != terminal.getCicsRegion()) {
            throw new CemtException("Terminal provided does not match CICS region.");
        }

        if (!terminal.isClearScreen()) {
            try {
                terminal.resetAndClear();
            } catch (CicstsManagerException e) {
                throw new CemtException("Problem reset and clearing screen for CEMT transaction", e);
            }
        }

        String cemtCmd = "CEMT PERFORM " + systemArea + " ";
        cemtCmd += setRequest;

        try {
            terminal.type(cemtCmd).enter().waitForKeyboard();
            boolean success = terminal.retrieveScreen().contains(expectedResponse);
            if(!success) {
                throw new CemtException("Expected Response from CEMT PERFORM not found. Expected: "
                        + expectedResponse);
            }else { 

                try {
                    terminal.pf3();
                    terminal.waitForKeyboard();
                    terminal.clear();
                    terminal.waitForKeyboard();
                }catch(Exception e) {
                    throw new CemtException("Unable to return terminal back into reset state", e);
                }

                return success;

            }

        }catch(Exception e) {
            throw new CemtException(e);
        }


    }

    @Override
    public void waitForDisabledResource(ICicsTerminal terminal, String resourceType, String resourceName,
        long defaultResourceTimeoutMilliseconds)
            throws CemtException {

        try {
            long timeoutTimeInMilliseconds = System.currentTimeMillis() + defaultResourceTimeoutMilliseconds;
            while (System.currentTimeMillis() < timeoutTimeInMilliseconds) {
                terminal.resetAndClear();
                terminal.type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
                if (terminal.retrieveScreen().contains(" Dis ")) {
                    return;
                } 
                if (System.currentTimeMillis() >= timeoutTimeInMilliseconds) {
                    break;
                }
                Thread.sleep(1000);
            }
        } catch (CicstsManagerException e) {
            throw new CemtException(
                "Problem with starting the CEMT transaction", e);

        }
        catch (Exception e) {
            throw new CemtException("Unable to prepare for the CEMT inquire resource", e);
        }
        
        throw new CemtException("Timeout of " + defaultResourceTimeoutMilliseconds
            + "ms exceeded while waiting for " + resourceType + "("
            + resourceName + ") to be disabled");
    }

    @Override
    public void waitForEnabledResource(ICicsTerminal terminal, String resourceType, String resourceName)
        throws CemtException
    {
        waitForEnabledResource(terminal, resourceType, resourceName, this.defaultResourceTimeoutMilliseconds);
    }

    @Override
    public void waitForEnabledResource(ICicsTerminal terminal, String resourceType, String resourceName,
        long defaultResourceTimeoutMilliseconds) throws CemtException
    {
        // Calculate when we should timeout
        long timeoutTimeInMilliseconds = System.currentTimeMillis() + defaultResourceTimeoutMilliseconds;

        // Keep going until we reach timeout time
        while (System.currentTimeMillis() < timeoutTimeInMilliseconds) {
          try {
            // Clear the terminal and issue the inquire command
            terminal.resetAndClear();
            terminal.type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")").enter().waitForKeyboard();
            
            if (terminal.retrieveScreen().contains(" Ena "))
            {
                return;
            }

            // Double check the timeout time, just in case the the send text
            // took ages
            if (System.currentTimeMillis() >= timeoutTimeInMilliseconds)
            {
                break;
            }
            
            // Sleep before we try again
            Thread.sleep(500);
          } catch (FieldNotFoundException | KeyboardLockedException | CicstsManagerException | 
                   TerminalInterruptedException | NetworkException | TimeoutException | InterruptedException e) {
            throw new CemtException("Problem with starting the CEMT transaction", e);
          }
        }
        // Will only get here if we timed out
        throw new CemtException("Timeout of " + defaultResourceTimeoutMilliseconds
            + "ms exceeded while waiting for " + resourceType + "("
            + resourceName + ") to be enabled");
    }
    
    @Override
    public boolean inquireResourceNotFound(ICicsTerminal terminal,
            String resourceType, String resourceName, String searchText) throws CemtException
             {
        return inquireResourceNotFound(terminal, resourceType, resourceName,
                 searchText,null);
    }
    
    @Override
    public boolean inquireResourceNotFound(ICicsTerminal terminal, String resourceType, String resourceName,
        String searchText, String state) throws CemtException
    {
        boolean found = false;
        try
        {
            try
            {
                terminal.resetAndClear().wfk();
            }
            catch (CicstsManagerException e)
            {
                throw new CemtException("Problem reset and clearing screen for CEMT transaction", e);
            }

            terminal
                .type("CEMT INQUIRE " + resourceType + "(" + resourceName + ")" + ((state != null) ? " " + state : ""))
                .enter().waitForKeyboard();
            try
            {
                if (!terminal.searchText("NOT FOUND"))
                {
                    terminal.waitForKeyboard();
                    throw new CemtException("Errors detected while inquiring resource, see terminal log");
                }
            }
            catch (CicstsManagerException e)
            {
                throw new CemtException("Problem determine the result from the CEMT command", e);
            }

            if (searchText == null)
            {
                found = true;
            }
            else
            {
                found = terminal.searchText(searchText);
            }
            try
            {
                terminal.wfk();
                terminal.resetAndClear();
            }
            catch (CicstsManagerException e)
            {
                throw new CemtException("Unable to return terminal back into reset state", e);
            }
        }
        catch (FieldNotFoundException | KeyboardLockedException | CicstsManagerException | TerminalInterruptedException
            | NetworkException | TimeoutException e)
        {
            throw new CemtException("Problem with starting the CEMT transaction", e);
        }
        return found;
    }

    @Override
    public void waitForDisabledResource(ICicsTerminal terminal, String resourceType, String resourceName) throws CemtException
    {
        waitForDisabledResource(terminal, resourceType, resourceName, this.defaultResourceTimeoutMilliseconds);
    }
    
    @Override
    public boolean isResourceEnabled(ICicsTerminal terminal, String resourceType,
            String resourceName) throws CemtException 
    {
        // Use the inquire to search for " Ena "
        return this.inquireResource(terminal, resourceType, resourceName, " Ena ");
    }


}