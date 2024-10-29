/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.cicsts.CeciException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeci;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.ICeciResponseOutputValue;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.IExecInterfaceBlock;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;

/**
 * Implementation of {@link ICeci}
 */
public class CeciImpl implements ICeci {
    
    private static final Log logger = LogFactory.getLog(CeciImpl.class);
    private static final String INITIAL_SCREEN_ID = "STATUS:  ENTER ONE OF THE FOLLOWING";
    private static final String COMMAND_BEFORE_SCREEN_ID = "STATUS:  ABOUT TO EXECUTE COMMAND";
    private static final String COMMAND_AFTER_SCREEN_ID = "STATUS:  COMMAND EXECUTION COMPLETE";
    private static final String HELP_SCREEN_ID = "GENERAL HELP INFORMATION";
    private static final String EIB_SCREEN_ID = "EXEC INTERFACE BLOCK";
    private static final String VAR_SCREEN_ID = "VARIABLES   LENGTH   DATA";
    private static final String MSG_SCREEN_ID = "SYNTAX MESSAGES";
    private static final String VAR_EXPANSION_SCREEN_ID = "EXPANSION OF:";
    private static final String NO_SYNTAX_MESSAGES = "THERE ARE NO MESSAGES";
    private static final String COMMAND_EXECUTION_COMPLETE = "STATUS:  COMMAND EXECUTION COMPLETE";
    private static final String COMMAND_VARIABLE_NAME = "&@COMMAND";
    private static final String CONTAINER_DATA_VARIABLE_NAME = "&@CONTDATA";
    private static final String COMMAREA_DATA_VARIABLE_NAME = "&@COMMDATA";
    private static final String VARIABLE_TYPE_DOUBLE_WORD = "FD";
    private static final String VARIABLE_TYPE_FULL_WORD = "F";
    private static final String VARIABLE_TYPE_HALF_WORD = "H";
    private static final String VARIABLE_TYPE_4_BYTE_PACKED = "P";
    private static final String VARIABLE_TYPE_8_BYTE_PACKED = "D";
    private static final String MESSAGE_DFHAC2206 = "DFHAC2206";
    
    private static final String WRONG_CICS_REGION = "Provided terminal does not belong to the correct CICS TS Region";
    
    private String command;
    private ICicsTerminal terminal;
    private final ICicsRegion cicsRegion;
    
    public CeciImpl(CeciManagerImpl manager, ICicsRegion cicsRegion) {
        this.cicsRegion = cicsRegion;
    }
    

    @Override
	public void startCECISession(@NotNull ICicsTerminal ceciTerminal) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
	    try {
	    	if (!this.terminal.isClearScreen()) {
                this.terminal.resetAndClear();
	    	}
            this.terminal.type("CEOT TR").enter().waitForKeyboard();
        	this.terminal.pf3().waitForKeyboard();
        	this.terminal.resetAndClear();
        	this.terminal.type("CECI").enter().waitForKeyboard();
        } catch (CicstsManagerException | TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException | FieldNotFoundException e) {
            throw new CeciException("Problem starting CECI session", e);
        }
		if (!isInitialScreen(this.terminal.retrieveScreen())) {
			throw new CeciException("Not on CECI initial screen");			
		}
	}


	@Override
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command) throws CeciException {
        return issueCommand(ceciTerminal, command, true);
    }

    @Override
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command, boolean parseOutput) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        
        String commandVariable = COMMAND_VARIABLE_NAME;
        try {
            if (command.startsWith("&")) {
                commandVariable = command;
                this.command = retrieveVariableText(ceciTerminal, command);
                logger.info("Issue command: " + command + " - " + this.command);
            } else {
                this.command = command;
                logger.info("Issue command: " + this.command);
                // Save the command as a variable
                defineVariableText(ceciTerminal, COMMAND_VARIABLE_NAME, command);
            }
            // Enter the command variable on the command line
            initialScreen().type(commandVariable).enter().waitForKeyboard();
            // Check for messages
            terminal.pf9().waitForKeyboard();
            checkForSyntaxMessages();
            // Issue the command
            terminal.enter().waitForKeyboard();
            // Check we didn't abend
            if (terminal.retrieveScreen().contains(MESSAGE_DFHAC2206)) {
                terminal.reportScreenWithCursor();
                throw new CeciException("Command abended - see previous screen");
            }
            
            // If on user screen then need enter to return to the command
            if (!terminal.retrieveScreen().contains(COMMAND_EXECUTION_COMPLETE)) {
                terminal.enter().waitForKeyboard();
                if (!terminal.retrieveScreen().contains(COMMAND_EXECUTION_COMPLETE)) {
                    terminal.reportScreenWithCursor();
                    throw new CeciException("Command failed - see previous screen");
                }
            }
            
            // Return the response
            return newCeciResponse(parseOutput);
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Error issuing CECI command", e);
        }
    }
    
    @Override
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command, HashMap<String, Object> options) throws CeciException {
        return issueCommand(ceciTerminal, command, options, true);
    }


    @Override
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command, HashMap<String, Object> options, boolean parseOutput) throws CeciException {
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        if (options != null && !options.isEmpty()) {
            for(Entry<String, Object> entry : options.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value == null || "".equals(value)) {
                    sb.append(" ");
                    sb.append(key);
                } else {
                    sb.append(" ");
                    sb.append(key);
                    sb.append("(");
                    sb.append(value);
                    sb.append(")");
                }
            }
        }

        return issueCommand(ceciTerminal, sb.toString(), parseOutput);
    }

    @Override
    public int defineVariableText(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull String value) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        name = validateVariable(name, value.toCharArray(), null);
        return setVariable(name, value, null);
    }
        
        
    @Override
    public int defineVariableBinary(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull char[] value) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        name = validateVariable(name, value, null);
        return setVariableHex(name, value);
    }

    @Override
    public int defineVariableDoubleWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull long value) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        String format = "%+0" + getLength(VARIABLE_TYPE_DOUBLE_WORD) + "d";
        String valueString = String.format(format, value);
        name = validateVariable(name, valueString.toCharArray(), VARIABLE_TYPE_DOUBLE_WORD);
        return setVariable(name, valueString, VARIABLE_TYPE_DOUBLE_WORD);
    }

    @Override
    public int defineVariableFullWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        String format = "%+0" + getLength(VARIABLE_TYPE_FULL_WORD) + "d";
        String valueString = String.format(format, value);
        name = validateVariable(name, valueString.toCharArray(), VARIABLE_TYPE_FULL_WORD);
        return setVariable(name, String.format("%+011d",value), VARIABLE_TYPE_FULL_WORD);
    }

    @Override
    public int defineVariableHalfWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        String format = "%+0" + getLength(VARIABLE_TYPE_HALF_WORD) + "d";
        String valueString = String.format(format, value);
        name = validateVariable(name, valueString.toCharArray(), VARIABLE_TYPE_HALF_WORD);
        return setVariable(name, String.format("%+06d",value), VARIABLE_TYPE_HALF_WORD);
    }

    @Override
    public int defineVariable4BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        String format = "%+0" + getLength(VARIABLE_TYPE_4_BYTE_PACKED) + "d";
        String valueString = String.format(format, value);
        name = validateVariable(name, valueString.toCharArray(), VARIABLE_TYPE_4_BYTE_PACKED);
        return setVariable(name, String.format("%+08d",value), VARIABLE_TYPE_4_BYTE_PACKED);
    }

    @Override
	public int defineVariable8BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull long value) throws CeciException {
	    this.terminal = ceciTerminal;
	    if (ceciTerminal.getCicsRegion() != cicsRegion) {
	        throw new CeciException(WRONG_CICS_REGION);
	    }
	    String format = "%+0" + getLength(VARIABLE_TYPE_8_BYTE_PACKED) + "d";
	    String valueString = String.format(format, value);
	    name = validateVariable(name, valueString.toCharArray(), VARIABLE_TYPE_8_BYTE_PACKED);
	    return setVariable(name, valueString, VARIABLE_TYPE_8_BYTE_PACKED);
	}


	@Override
    public String retrieveVariableText(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
        this.terminal = ceciTerminal;
	    if (ceciTerminal.getCicsRegion() != cicsRegion) {
	        throw new CeciException(WRONG_CICS_REGION);
	    }
        name = validateVariable(name, null, null);
        return getVariable(name, null);
    }

    @Override
    public char[] retrieveVariableBinary(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
        this.terminal = ceciTerminal;
	    if (ceciTerminal.getCicsRegion() != cicsRegion) {
	        throw new CeciException(WRONG_CICS_REGION);
	    }
        name = validateVariable(name, null, null);
        return getVariableHex(name);
    }
    
    @Override
    public long retrieveVariableDoubleWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
        this.terminal = ceciTerminal;
	    if (ceciTerminal.getCicsRegion() != cicsRegion) {
	        throw new CeciException(WRONG_CICS_REGION);
	    }
        name = validateVariable(name, null, null);
        return Long.valueOf(getVariable(name, VARIABLE_TYPE_DOUBLE_WORD));
    }

    @Override
    public int retrieveVariableFullWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
        this.terminal = ceciTerminal;
	    if (ceciTerminal.getCicsRegion() != cicsRegion) {
	        throw new CeciException(WRONG_CICS_REGION);
	    }
        name = validateVariable(name, null, null);
        return Integer.valueOf(getVariable(name, VARIABLE_TYPE_FULL_WORD));
    }

    @Override
    public int retrieveVariableHalfWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
        this.terminal = ceciTerminal;
	    if (ceciTerminal.getCicsRegion() != cicsRegion) {
	        throw new CeciException(WRONG_CICS_REGION);
	    }
        name = validateVariable(name, null, null);
        return Integer.valueOf(getVariable(name, VARIABLE_TYPE_HALF_WORD));
    }

    @Override
    public int retrieveVariable4BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        name = validateVariable(name, null, null);
        return Integer.valueOf(getVariable(name, VARIABLE_TYPE_4_BYTE_PACKED));
    }

    @Override
	public long retrieveVariable8BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
	    this.terminal = ceciTerminal;
	    if (ceciTerminal.getCicsRegion() != cicsRegion) {
	        throw new CeciException(WRONG_CICS_REGION);
	    }
	    name = validateVariable(name, null, null);
	    return Long.valueOf(getVariable(name, VARIABLE_TYPE_8_BYTE_PACKED));
	}


	@Override
    public void deleteVariable(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException {
        if (!name.startsWith("&")){
            name = "&" + name;
        }
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        try {
            hexOff();
            if (variableScreen().retrieveScreen().contains(name + " ")) {
                // Find the variable and delete it
                String fieldValue = terminal.tab().retrieveFieldAtCursor().trim();
                while (!fieldValue.equals(name)) {
                    if (fieldValue.equals("PF")) {
                        throw new CeciException("Unable to find variable to delete");
                    }
                    fieldValue = terminal.newLine().retrieveFieldAtCursor().trim();
                }
                terminal.eraseEof().tab().eraseEof().enter().waitForKeyboard();
                if (variableScreen().retrieveScreen().contains(name + " ")) {
                    throw new CeciException("Delete variable failed");
                }
            }
        } catch (FieldNotFoundException | KeyboardLockedException | TimeoutException | TerminalInterruptedException | NetworkException e) {
            throw new CeciException("Unable to delete variable", e);
        }
    }

    @Override
    public void deleteAllVariables(@NotNull ICicsTerminal ceciTerminal) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        try {
            hexOff();
            // Find the variables and delete them
            String fieldValue = variableScreen().tab().retrieveFieldAtCursor().trim();
            int tabCount = 1;
            while (tabCount < 55 && !fieldValue.equals("PF")) {
                fieldValue = terminal.eraseEof().tab().retrieveFieldAtCursor().trim();
                tabCount++;
            }
            terminal.enter().waitForKeyboard();
        } catch (TimeoutException | KeyboardLockedException | FieldNotFoundException | TerminalInterruptedException | NetworkException e) {
            throw new CeciException("Unable to delete all variables", e);
        }
    }

    @Override
    public IExecInterfaceBlock getEIB(@NotNull ICicsTerminal ceciTerminal) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        try {
            hexOn();
            String eibHex = terminal.pf4().waitForKeyboard().retrieveScreen() + terminal.pf11().waitForKeyboard().retrieveScreen();
            hexOff();
            String eibText = terminal.pf4().waitForKeyboard().retrieveScreen() + terminal.pf11().waitForKeyboard().retrieveScreen();
            return new CeciExecInterfaceBlockImpl(eibText, eibHex);
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException e) {
            throw new CeciException("Unable to navigate to EIB screen", e);
        }
    }

    @Override
    public ICeciResponse linkProgram(@NotNull ICicsTerminal ceciTerminal, @NotNull String programName, String commarea, String sysid, String transid, boolean synconreturn) throws CeciException {
        this.terminal = ceciTerminal;       
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        StringBuilder commandBuffer = new StringBuilder();
        commandBuffer.append("LINK PROGRAM(");
        commandBuffer.append(programName);
        commandBuffer.append(")");
        if (commarea != null) {
            commandBuffer.append(" COMMAREA(");
            if (commarea.startsWith("&")) {
                commandBuffer.append(commarea);
            } else {
                defineVariableText(ceciTerminal, COMMAREA_DATA_VARIABLE_NAME, commarea);
                commandBuffer.append(COMMAREA_DATA_VARIABLE_NAME);
            }
            commandBuffer.append(")");
        }
        if (sysid != null) {
            commandBuffer.append(" SYSID(");
            commandBuffer.append(sysid);
            commandBuffer.append(")");
        }
        if (transid != null) {
            commandBuffer.append(" TRANSID(");
            commandBuffer.append(transid);
            commandBuffer.append(")");
        }
        if (synconreturn) {
            commandBuffer.append(" SYNCONRETURN");
        }
        return issueCommand(terminal, commandBuffer.toString());
    }

    @Override
    public ICeciResponse linkProgramWithChannel(@NotNull ICicsTerminal ceciTerminal, @NotNull String programName, @NotNull String channelName, String sysid, String transid, boolean synconreturn) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        StringBuilder commandBuffer = new StringBuilder();
        commandBuffer.append("LINK PROGRAM(");
        commandBuffer.append(programName);
        commandBuffer.append(") ");
        commandBuffer.append("CHANNEL(");
        commandBuffer.append(channelName);
        commandBuffer.append(")");
        if (sysid != null) {
            commandBuffer.append(" SYSID(");
            commandBuffer.append(sysid);
            commandBuffer.append(")");
        }
        if (transid != null) {
            commandBuffer.append(" TRANSID(");
            commandBuffer.append(transid);
            commandBuffer.append(")");
        }
        if (synconreturn) {
            commandBuffer.append(" SYNCONRETURN");
        }
        return issueCommand(terminal, commandBuffer.toString());
    }

    @Override
    public ICeciResponse putContainer(@NotNull ICicsTerminal ceciTerminal, @NotNull String channelName, @NotNull String containerName, @NotNull String content, String dataType, String fromCcsid, String fromCodepage) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        String dataVariableName;
        if (content.startsWith("&")) {
            dataVariableName = content;
        } else {
            setVariable(CONTAINER_DATA_VARIABLE_NAME, content, null);
            dataVariableName = CONTAINER_DATA_VARIABLE_NAME;
        }
        StringBuilder commandBuffer = new StringBuilder();
        commandBuffer.append("PUT CONTAINER(");
        commandBuffer.append(containerName);
        commandBuffer.append(") CHANNEL(");
        commandBuffer.append(channelName);
        commandBuffer.append(") FROM(");
        commandBuffer.append(dataVariableName);
        commandBuffer.append(")");
        if (dataType != null) {
            commandBuffer.append(" ");
            commandBuffer.append(dataType);
        }
        if (fromCcsid != null) {
            commandBuffer.append(" FROMCCID(");
            commandBuffer.append(fromCcsid);
            commandBuffer.append(")");
        }
        if (fromCodepage != null) {
            commandBuffer.append(" FROMCODEPAGE(");
            commandBuffer.append(fromCodepage);
            commandBuffer.append(")");
        }
        return issueCommand(terminal, commandBuffer.toString());
    }

    @Override
    public ICeciResponse getContainer(@NotNull ICicsTerminal ceciTerminal, @NotNull String channelName, @NotNull String containerName, @NotNull String variableName, String intoCcsid, String intoCodepage) throws CeciException {
        this.terminal = ceciTerminal;
        if (ceciTerminal.getCicsRegion() != cicsRegion) {
            throw new CeciException(WRONG_CICS_REGION);
        }
        if (!variableName.startsWith("&")){
            variableName = "&" + variableName;
        }
        StringBuilder commandBuffer = new StringBuilder();
        commandBuffer.append("GET CONTAINER(");
        commandBuffer.append(containerName);
        commandBuffer.append(") CHANNEL(");
        commandBuffer.append(channelName);
        commandBuffer.append(") INTO(");
        commandBuffer.append(variableName);
        commandBuffer.append(")");
        if (intoCcsid != null) {
            commandBuffer.append(" INTOCCSID(");
            commandBuffer.append(intoCcsid);
            commandBuffer.append(")");
        }
        if (intoCodepage != null) {
            commandBuffer.append(" INTOCODEPAGE(");
            commandBuffer.append(intoCodepage);
            commandBuffer.append(")");
        }
        return issueCommand(terminal, commandBuffer.toString());
    }
    
    protected ICicsTerminal initialScreen() throws CeciException {
        try {
            if (!isCeciScreen()) {
                // Might be on the USER screen. Send enter and try again
                terminal.enter().waitForKeyboard();
                if (!isCeciScreen()) {
                    throw new CeciException("Cannot identify terminal as CECI session");
                }
                
            }
            if (isHelpScreen(terminal.retrieveScreen())) {
            	terminal.enter().waitForKeyboard();
            } else {
            	terminal.home().eraseEof().home().enter().waitForKeyboard();
            }
    		for (int i = 0; i <= 10; i++) {
    			if (isInitialScreen(terminal.retrieveScreen())) {
    				break;
    			}
    			sleep(10);
    		}
    		if (!isInitialScreen(terminal.retrieveScreen())) {
    			terminal.reportScreenWithCursor();
    			throw new CeciException("Unable to navigate to CECI initial screen");
    		}
            return terminal;
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to navigate to CECI initial screen", e);
        }
    }


    protected ICicsTerminal variableScreen() throws CeciException {
        try {
        	initialScreen().pf5().waitForKeyboard();
    		for (int i = 0; i <= 10; i++) {
    			if (isVariablesScreen(terminal.retrieveScreen())) {
    				break;
    			}
    			sleep(10);
    		}
    		if (!isVariablesScreen(terminal.retrieveScreen())) {
    			terminal.reportScreenWithCursor();
    			throw new CeciException("Unable to navigate to CECI variables screen");
    		}
            return terminal;
        } catch (TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException e) {
            throw new CeciException("Unable to navigate to CECI variables screen", e);
        }
    }

    protected void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			logger.debug(e);
			Thread.currentThread().interrupt();
		}
	}


	protected boolean isCeciScreen() {
        String screen = terminal.retrieveScreen(); 
        return isInitialScreen(screen) ||
               isHelpScreen(screen) ||
               isCommandBeforeScreen(screen) ||
               isCommandAfterScreen(screen) ||
               isEibScreen(screen) ||
               isVariablesScreen(screen) ||
               isVariablesExpansionScreen(screen) ||
               isMsgScreen(screen);
    }

    protected boolean isInitialScreen(String screen) {
        return screen.contains(INITIAL_SCREEN_ID);
    }

    protected boolean isCommandBeforeScreen(String screen) {
        return screen.contains(COMMAND_BEFORE_SCREEN_ID);
    }

    protected boolean isCommandAfterScreen(String screen) {
        return screen.contains(COMMAND_AFTER_SCREEN_ID);
    }

    protected boolean isHelpScreen(String screen) {
        return screen.contains(HELP_SCREEN_ID);
    }

    protected boolean isEibScreen(String screen) {
        return screen.contains(EIB_SCREEN_ID);
    }

    protected boolean isVariablesScreen(String screen) {
        return screen.contains(VAR_SCREEN_ID);
    }

    protected boolean isMsgScreen(String screen) {
        return screen.contains(MSG_SCREEN_ID);
    }

    protected boolean isVariablesExpansionScreen(String screen) {
        return screen.contains(VAR_EXPANSION_SCREEN_ID);
    }

    protected ICicsTerminal multipleTab(int times) throws FieldNotFoundException, KeyboardLockedException {
        for (int i = 0; i < times; i++) {
            terminal.tab();
        }
        return terminal;
    }

    protected ICicsTerminal multipleNewLine(int times) throws FieldNotFoundException, KeyboardLockedException {
        for (int i = 0; i < times; i++) {
            terminal.newLine();
        }
        return terminal;
    }

    protected void checkForSyntaxMessages() throws CeciException {
        try {
            String screen = terminal.pf9().waitForKeyboard().retrieveScreen();
            if (!screen.contains(NO_SYNTAX_MESSAGES)) {
                throw new CeciException("Command failed syntax check. \nCommand:\n  " + command + "\nSyntax Error Screen:\n" + screen);
            }
            terminal.enter().waitForKeyboard();
        } catch (TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException e) {
            throw new CeciException("Unable to check for syntax messages", e);
        }
    }
    
    protected String validateVariable(String name, char[] value, String type) throws CeciException {
        name = name.trim();
        if (!name.startsWith("&")){
            name = "&" + name;
        }
        if (name.length() > 10) {
            throw new CeciException("CECI variable name \"" + name + "\" greater than maximum length of 10 characters including the leading \"&\"");
        }
        final String expr = "^[&][a-zA-Z0-9@#]*";
        if (!name.matches(expr)) {
            throw new CeciException("CECI variable name \"" + name + "\" invalid must. Must start with \"&\" and can contain one of more [a-zA-Z0-9@#]");
        }
        if (value != null) {
            if (type != null) {
                int maxLength = getLength(type);
                if (value.length != maxLength) {
                    throw new CeciException("CECI variable value length " + value.length + " greater than maximum of " + maxLength +  " for type \"" + type + "\"" );
                }
                
            } else if (value.length > 32767) {
                throw new CeciException("CECI variable value length " + value.length + " greater than maximum 32767");
            }
        }
        
        return name;
    }
    
    protected int setVariable(String name, String value, String type) throws CeciException {
        try {
            deleteVariable(terminal, name);
    
            // Set Hex off
            hexOff();
            
            // Go to the first variable on the variable screen
            variableScreen().newLine();
            // Find an empty variable field
            String fieldValue = terminal.reportScreenWithCursor().retrieveFieldAtCursor();
            while (!fieldValue.replace(" ", "").isEmpty()) {
                if (fieldValue.equals("PF")) {
                    throw new CeciException("No space on CECI variable screen for new variables");
                }
                fieldValue = terminal.newLine().retrieveFieldAtCursor();
            } 
            // Enter variable name and variable length
            String lengthString = type;
            if (lengthString == null) {
                lengthString = String.valueOf(value.length());
            }
            
            terminal.type(name);
            if (name.length() < 10) {
                terminal.tab();
            }
            terminal.type(lengthString).enter().waitForKeyboard();
            terminal.enter().waitForKeyboard().pf5().waitForKeyboard();
            
            // Tab to variable, expand it variable and move to the first data field
            moveToVariable(name).enter().waitForKeyboard();
            multipleNewLine(2);
            
            // Data is written in 64 character chunks and if the length > and we need to write data in pages
            String[] chunks = value.split("(?<=\\G.{64})");
            
            // Write the first page 
            int next = setVariableOnPage(chunks, 0, 20);
            while (next < chunks.length) {
                // Go to next page and tab to the first entry field
                terminal.home().pf11().waitForKeyboard();
                multipleNewLine(6);
                next = setVariableOnPage(chunks, next, 16);
            }
            
            terminal.home().enter().waitForKeyboard();
            logger.info("New CECI variable \"" + name + "\" defined");
            
            return value.length();
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to set CECI variable", e);
        }
    }
    
    protected int setVariableOnPage(String[] chunks, int start, int numberOfLines) throws CeciException {
        int chunkPos = start;        
        // Enter the page of data 
        for (int i = 0; i < numberOfLines; i++) {
            try {
                terminal.type(chunks[chunkPos]);
                if (chunks[chunkPos].length() < 64) {
                    terminal.tab();
                }
            } catch (FieldNotFoundException | KeyboardLockedException e) {
                throw new CeciException("Unable enter variable data", e);
            }
            chunkPos++;
            if (chunkPos == chunks.length) {
                break;
            }
        }                    

        return chunkPos;
    }

    protected int setVariableHex(String name, char[] value) throws CeciException {
        try {
            deleteVariable(terminal, name);
            
            // Go to the first variable on the variable screen
            variableScreen().tab();
            // Find an empty variable field
            String fieldValue = terminal.retrieveFieldAtCursor();
            while (!fieldValue.replace(" ", "").isEmpty()) {
                if (fieldValue.equals("PF")) {
                    throw new CeciException("No space on CECI variable screen for new variables");
                }
                fieldValue = terminal.newLine().retrieveFieldAtCursor();
            } 
            // Enter variable name and variable length
            String lengthString = String.valueOf(value.length);
            
            terminal.type(name);
            if (name.length() < 10) {
                terminal.tab();
            }
            terminal.type(lengthString).enter().waitForKeyboard();
            terminal.enter().waitForKeyboard().pf5().waitForKeyboard();
            
            // Tab to variable, expand it, set hex on and move to the first data field
            moveToVariable(name).enter().waitForKeyboard().pf2().waitForKeyboard();
            multipleNewLine(2);
            
            // Data is written in 4 byte chunks and we need to write data in pages            
            // Write the first page 
            int next = setVariableHexOnPage(value, 0, 20);
            while (next < value.length) {
                // Go to next page and tab to the first entry field
                terminal.home().pf11().waitForKeyboard().home();
                multipleNewLine(6);
                next = setVariableHexOnPage(value, next, 16);
            }
            
            terminal.enter().waitForKeyboard();
            logger.info("CECI HEX variable \"" + name + "\" defined");
            
            return Integer.parseInt(lengthString);
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to set CECI binary variable", e);
        }
    }

    protected int setVariableHexOnPage(char[] value, int start, int numberOfLines) throws CeciException {
        try {
            int pos = start;
            int lineCount = 0;
            while (pos < value.length && lineCount < numberOfLines) {
                int blockCount = 0;
                while (pos < value.length && blockCount < 4) {
                    int byteCount = 0;
                    StringBuilder sb = new StringBuilder();
                    while (pos < value.length && byteCount < 4) {
                        String hex = Integer.toHexString(value[pos]);
                    	hex = String.format("%2S", hex).replaceAll(" ", "0");
                        sb.append(hex.toUpperCase());   
                        pos++;
                        byteCount++;
                    }
                    terminal.type(sb.toString());
                    blockCount++;
                }
                lineCount++;
                terminal.tab();
            }                   
    
            return pos;
        } catch (FieldNotFoundException | KeyboardLockedException e) {
            terminal.reportScreenWithCursor();
            throw new CeciException("Unable enter variable data", e);
        }
    }

    protected String getVariable(String name, String type) throws CeciException {
        try {            
            // Find the variable, expand it and move to the length field and get it's value
            String lengthString = moveToVariable(name).enter().waitForKeyboard().tab().retrieveFieldAtCursor().trim();
            if (type != null && !lengthString.equals(type)) {
                throw new CeciException("Unexpected variable type \"" + lengthString + "\" for \"" + name + "\"");
            }
            
            int valueLength = getLength(lengthString);
            
            StringBuilder sb = new StringBuilder();
            
            // Move to the first data field
            terminal.newLine();
            
            // We need to retrieve data in pages
            sb.append(getVariableFromPage(valueLength, 20));
            while (sb.length() < valueLength) {
                // Go to next page and tab to the first entry field
                terminal.home().pf11().waitForKeyboard();
                multipleNewLine(6);
                sb.append(getVariableFromPage(valueLength-sb.length(), 16));                
            }
            
            logger.info("CECI variable \"" + name + "\" retreived");
            return sb.toString();
        } catch (NumberFormatException e) {
            throw new CeciException("Unable to determine variable field length");
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to get CECI variable", e);
        }
    }
    
    protected int getLength(String lengthString) {
        if (lengthString.equals(VARIABLE_TYPE_DOUBLE_WORD)) {
            return 20;
        } else if (lengthString.equals(VARIABLE_TYPE_FULL_WORD)) {
            return 11;
        } else if (lengthString.equals(VARIABLE_TYPE_HALF_WORD)) {
            return 6;
        } else  if (lengthString.equals(VARIABLE_TYPE_4_BYTE_PACKED)) {
            return 8;
        } else if (lengthString.equals(VARIABLE_TYPE_8_BYTE_PACKED)) {
            return 16;
        } else {
            return Integer.parseInt(lengthString);
        }
    }

    protected String getVariableFromPage(int valueLength, int numberOfLines) throws CeciException {
        StringBuilder sb = new StringBuilder();
        int lineCount = 0;
        while (sb.length() < valueLength && lineCount < numberOfLines) {
            sb.append(terminal.retrieveFieldAtCursor());
            lineCount++;
            try {
                terminal.tab();
            } catch (FieldNotFoundException | KeyboardLockedException e) {
                throw new CeciException("Unable to get variable from page", e);
            }
        }
        return sb.toString();
    }

    protected char[] getVariableHex(String name) throws CeciException {
        try {            
            // Find the variable, expand it, set hex on, move to the length field and get it's value
            String lengthString = moveToVariable(name).enter().waitForKeyboard().pf2().waitForKeyboard().tab().retrieveFieldAtCursor();
            
            int valueLength = Integer.parseInt(lengthString);
            
            StringBuilder sb = new StringBuilder();
            
            // Move to the first data field
            terminal.newLine();
            
            // We need to retrieve data in pages
            sb.append(getVariableHexFromPage(valueLength, 20));
            while (sb.length() < valueLength) {
                // Go to next page and tab to the first entry field
                terminal.home().pf11().waitForKeyboard();
                multipleNewLine(6);
                sb.append(getVariableHexFromPage(valueLength-sb.length(), 16));                
            }
            
            logger.info("CECI variable \"" + name + "\" retreived");
            return sb.toString().toCharArray();
        } catch (NumberFormatException e) {
            throw new CeciException("Unable to determine variable field length");
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to get CECI binary variable", e);
        }
    }
    
    protected String getVariableHexFromPage(int valueLength, int numberOfLines) throws CeciException {
        StringBuilder sb = new StringBuilder();
        int lineCount = 0;
        try {
            while (sb.length() < valueLength && lineCount < numberOfLines) {
                int blockCount = 0;
                while (sb.length() < valueLength && blockCount < 4) {
                    String hexField = terminal.retrieveFieldAtCursor();
                    String[] hexArray = hexField.split("(?<=\\G.{2})");
                    for (String hexString : hexArray) {
                        sb.append((char) Long.parseLong(hexString, 16));
                    }
                    terminal.tab();
                    blockCount++;
                }
                terminal.tab();
                lineCount++;
            }
        } catch (FieldNotFoundException | KeyboardLockedException e) {
            throw new CeciException("Unable to get binary variable from page", e);
        }
        return sb.toString();
    }

    protected ICicsTerminal moveToVariable(String name) throws CeciException {
        try {
            // Set Hex off
            hexOff();
        
            // Confirm variable exists
            if (!variableScreen().retrieveScreen().contains(name + " ")) {
                throw new CeciException("Unable to find variable " + name);
            }
            // Go to the first variable on the variable screen 
            variableScreen().newLine();
            // Find an variable name field
            String fieldValue = terminal.retrieveFieldAtCursor().trim();
            while (!fieldValue.equals(name.trim())) {
                if (fieldValue.equals("PF")) {
                    throw new CeciException("Unable to find variable " + name);
                }
                fieldValue = terminal.newLine().retrieveFieldAtCursor().trim();
            }
        } catch (FieldNotFoundException | KeyboardLockedException e) {
            throw new CeciException("Problem serching for variable " + name, e);
        }
        return terminal;
    }

    /**
     * Is HEX on. Disruptive, returns the EIB screen  
     * @return
     * @throws CeciException
     */
    protected boolean isHexOn() throws CeciException {
        try {
            return terminal.pf4().waitForKeyboard().retrieveScreen().contains("EIBTIME      = X'");
        } catch (TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException e) {
            throw new CeciException("Unable to determine if CECI is in HEX mode", e);
        }
    }
    
    /**
     * Set HEX on. Disruptive, returns the EIB screen  
     * @return
     * @throws CeciException
     */
    protected ICicsTerminal hexOn() throws CeciException {
        try {
            if (!isHexOn()) {
                return (ICicsTerminal) terminal.pf2().waitForKeyboard();
            }
        } catch (TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException e) {
            throw new CeciException("Unable to set CECI HEX ON", e);
        }
        return terminal;
    }
    
    /**
     * Set HEX off. Disruptive, returns the EIB screen  
     * @return
     * @throws CeciException
     */
    protected ICicsTerminal hexOff() throws CeciException {
        try {
            if (isHexOn()) {
                return (ICicsTerminal) terminal.pf2().waitForKeyboard();
            }
        } catch (TimeoutException | KeyboardLockedException | TerminalInterruptedException | NetworkException e) {
            throw new CeciException("Unable to set CECI HEX OFF", e);
        }
        return terminal;
    }

    protected ICeciResponse newCeciResponse(boolean parseOutput) throws CeciException {
        String screen = terminal.retrieveScreen();

        String response = getFieldAfter(screen, "RESPONSE: ", "EIBRESP").trim();
        int eibresp = Integer.parseInt(getFieldAfter(screen, "EIBRESP="));
        int eibresp2 = Integer.parseInt(getFieldAfter(screen, "EIBRESP2="));        

        CeciResponseImpl ceciResponse = new CeciResponseImpl(response, eibresp, eibresp2);
        if (parseOutput) {
            ceciResponse.setResponseOutput(parseResponseOutput());
        } else {
            ceciResponse.setResponseOutput(new LinkedHashMap<>());
        }
        return ceciResponse;
    }

    protected Map<String, ICeciResponseOutputValue> parseResponseOutput() throws CeciException {
        Map<String, ICeciResponseOutputValue> responseOutput = new LinkedHashMap<>();
        try {
            // The first option is 2 new lines from the command line
            int optionCounter = 2;
            String fieldValue = multipleNewLine(2).retrieveFieldAtCursor();
            boolean done = false;
            while (!fieldValue.equals("PF")) {
                while (!fieldValue.equals("PF")) {
                    // Expand the option value
                    terminal.enter().waitForKeyboard();
                    
                    String screen = terminal.retrieveScreen();
                    String option = getFieldAfter(screen, "OPTION= ");
                    if (responseOutput.containsKey(option)) {
                        done = true;
                        break;
                    }
                    responseOutput.put(option, getOptionValue(screen));
                    // Return the the command page
                    terminal.enter().waitForKeyboard();
                    // Get the next option
                    optionCounter++;
                    fieldValue = multipleTab(optionCounter).retrieveFieldAtCursor();
                }
                if (done) {
                    break;
                }
                // Next page
                terminal.pf11().waitForKeyboard();
                optionCounter = 2;
                fieldValue = multipleTab(optionCounter).retrieveFieldAtCursor();
            }
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to parse command output", e);
        }
        
        return responseOutput;
    }

    protected ICeciResponseOutputValue getOptionValue(String screen) throws CeciException {
        try {
            String lengthString = getFieldAfter(screen, "LENGTH= ");
            int length = getLength(lengthString);
            // Move to data value
            terminal.home();
            multipleNewLine(2);
            StringBuilder sb = new StringBuilder();
            // We need to retrieve data in pages
            sb.append(getVariableFromPage(length, 20));
            int pf11Count = 0;
            while (sb.length() < length) {
                // Go to next page and tab to the data field
                terminal.home().pf11().waitForKeyboard();
                pf11Count++;
                multipleNewLine(6);
                sb.append(getVariableFromPage(length-sb.length(), 16));                
            }
            if (lengthString.startsWith("+")) {
                // Also get the value in hex
                String hex = getOptionValueInHex(length, pf11Count);
               
                String[] characterValue = {sb.toString(), hex};
                // Set hex off
                terminal.pf2().waitForKeyboard();
                return new ResponseOutputValueImpl(characterValue);
            } else {
                return new ResponseOutputValueImpl(sb.toString());                    
            }
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to parse command output option value", e);
        }
    }

    protected String getOptionValueInHex(int length, int pf11Count) throws CeciException {
        try {
            int pf10Count = 0;
            // Back to first page
            while (pf10Count < pf11Count) {
                terminal.home().pf10().waitForKeyboard();
                pf10Count++;
            }
            // Set hex on and tab to the data field
            terminal.pf2().waitForKeyboard().home();
            multipleNewLine(2);
            // Get the data in hex
            StringBuilder sb = new StringBuilder();
            sb.append(getVariableHexFromPage(length, 20));
            // We need to retrieve data in pages
            while (sb.length() < length) {
                // Go to next page and tab to the data field
                terminal.home().pf11().waitForKeyboard();
                pf11Count++;
                multipleNewLine(2);
                sb.append(getVariableHexFromPage(length-sb.length(), 16));                
            }
            return sb.toString();
        } catch (TimeoutException | KeyboardLockedException | NetworkException | TerminalInterruptedException | FieldNotFoundException e) {
            throw new CeciException("Unable to parse command output binary option value", e);
        }
    }

    protected String getFieldAfter(String screen, String field) {
        return getFieldAfter(screen, field, " ");
    }

    protected String getFieldAfter(String screen, String field, String nextField) {
        int start = screen.indexOf(field) + field.length();
        int end = screen.indexOf(nextField, start);
        return screen.substring(start, end);
    }

    protected ICicsRegion getCicsRegion() {
    	return this.cicsRegion;
    }
}
