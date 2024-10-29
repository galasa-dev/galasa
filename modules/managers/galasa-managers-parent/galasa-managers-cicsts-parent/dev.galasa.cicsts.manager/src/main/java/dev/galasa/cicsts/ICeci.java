/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import java.util.HashMap;

import javax.validation.constraints.NotNull;

/**
 * CICS/TS Command-level Interpreter (CECI) Interface.
 *
 */
public interface ICeci {
	
	/**
	 * Start a new active CECI session. Sets terminal to mixed case and input (<code>CEOT TRANIDONLY</code>) and starts the CECI transaction
	 * @param ceciTerminal an {@link ICicsTerminal}
	 * @throws CeciException
	 */
	public void startCECISession(@NotNull ICicsTerminal ceciTerminal) throws CeciException;
    
    /**
     * Issue a CECI command. The command will be stored and executed from a CECI variable. 
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param command a {@link String} containing the CECI command
     * @return an {@link ICeciResponse} object containing the command's response and output values.
     * @throws CeciException 
     */
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command) throws CeciException;
    
    /**
     * Issue a CECI command. The command will be stored and executed from a CECI variable. 
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * 
     * @param command a {@link String} containing the CECI command
     * @param parseOutput parse the command output and store in {@link ICeciResponse}. Setting to false can improve performance on commands
     * that contain a lot of output fields, e.g. <code>ASSIGN</code>.
     * 
     * The following examples shows how to retrieve a specific returned value:<br>
     * <code>
     * issueCommand(ICicsTerminal, "ASSIGN USERID(&amp;VAR)", false)<br>
     * retrieveVariableText(ICicsTerminal, "&amp;VAR")
     * </code>
     * @return an {@link ICeciResponse} object containing the command's response.
     * @throws CeciException 
     */
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command, boolean parseOutput) throws CeciException;

    /**
     * Issue a CECI command. The command will be stored and executed from a CECI variable. 
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param command a {@link String} containing the CECI command
     * @Param options options of the command, eg QUEUE=A would result in QUEUE(A) being appended to the command
     * @return an {@link ICeciResponse} object containing the command's response and output values.
     * @throws CeciException 
     */
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command, HashMap<String, Object> options) throws CeciException;
    
    /**
     * Issue a CECI command. The command will be stored and executed from a CECI variable. 
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param command a {@link String} containing the CECI command
     * @Param options options of the command, eg QUEUE=A would result in QUEUE(A) being appended to the command
     * @param parseOutput parse the command output and store in {@link ICeciResponse}. Setting to false can improve performance on commands
     * that contain a lot of output fields, e.g. <code>ASSIGN</code>.<br><br>
     * The following examples shows how to retrieve a specific returned value:<br><code>
     * issueCommand(ICicsTerminal, "ASSIGN USERID(&amp;VAR)", false)<br>
     * retrieveVariableText(ICicsTerminal, "ASSIGN USERID(&amp;VAR)", false)
     * </code>
     * @return an {@link ICeciResponse} object containing the command's response.
     * @throws CeciException 
     */
    public ICeciResponse issueCommand(@NotNull ICicsTerminal ceciTerminal, @NotNull String command, HashMap<String, Object> options, boolean parseOutput) throws CeciException;

    /**
     * Define a CECI text variable.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @param value text string with a maximum length of 32767 characters.
     * @return the length of the defined variable
     * @throws CeciException
     */
    public int defineVariableText(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull String value) throws CeciException;

    /**
     * Define a CECI binary variable.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @param value binary char array with a maximum length of 32767 characters.
     * @return the length of the defined variable
     * @throws CeciException
     */
    public int defineVariableBinary(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull char[] value) throws CeciException;
    
    /**
     * Define a double word CECI variable (CECI variable type <b>FD</b>). 
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @param value a long representing a double word (8 bytes) with a decimal integer value from -9223372036854775808D to +9223372036854775807D (0x80000000 00000000 to 0x7FFFFFFF FFFFFFFF)
     * @return the length of the defined variable
     * @throws CeciException
     */
    public int defineVariableDoubleWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull long value) throws CeciException;
    
    /**
     * Define a full word CECI variable (CECI variable type <b>F</b>).
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @param value an integer representing a full word (4 bytes) with a decimal value from -2147483648 to +2147483647 (0x80000000 to 0x7FFFFFFF)
     * @return the length of the defined variable 
     * @throws CeciException
     */
    public int defineVariableFullWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CeciException;
    
    /**
     * Define a half word CECI variable (CECI variable type <b>H</b>).
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @param value an integer representing a full word (2 bytes) with a decimal value from -32768 to +32767 (0x8000 to 0x7FFF)
     * @return the length of the defined variable 
     * @throws CeciException
     */
    public int defineVariableHalfWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CeciException;
    
    /**
     * Define a packed decimal CECI variable (CECI variable type <b>P</b>).
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @param value an integer representing 4 byte decimal value from -9999999 to +9999999 (0x9999999D to 0x9999999C)
     * @return the length of the defined variable 
     * @throws CeciException
     */
    public int defineVariable4BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull int value) throws CeciException;

    /**
	 * Define a full double word CECI variable (CECI variable type <b>D</b>).
	 * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
	 * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
	 * @param value a long representing 8 byte decimal with a decimal integer value from -999999999999999 to +999999999999999 (0x99999999 9999999D to 0x99999999 9999999F)
	 * @return the length of the defined variable
	 * @throws CeciException
	 */
	public int defineVariable8BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name, @NotNull long value) throws CeciException;

	/**
     * Retrieve a CECI text variable.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @return variable value
     * @throws CeciException
     */
    public String retrieveVariableText(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;

    /**
     * Retrieve a CECI binary variable.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @return variable value
     * @throws CeciException
     */
    public char[] retrieveVariableBinary(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;

    /**
     * Retrieve a double word CECI variable (CECI variable type <b>FD</b>).
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @return variable value
     * @throws CeciException
     */
    public long retrieveVariableDoubleWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;

    /**
     * Retrieve a full word CECI variable (CECI variable type <b>F</b>).
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @return variable value
     * @throws CeciException
     */
    public int retrieveVariableFullWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;

    /**
     * Retrieve a half word CECI variable (CECI variable type <b>H</b>).
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @return variable value
     * @throws CeciException
     */
    public int retrieveVariableHalfWord(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;

    /**
     * Retrieve a packed decimal CECI variable (CECI variable type <b>P</b>).
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @return variable value
     * @throws CeciException
     */
    public int retrieveVariable4BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;
    
    /**
	 * Retrieve a double word CECI variable (CECI variable type <b>D</b>).
	 * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
	 * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
	 * @return variable value
	 * @throws CeciException
	 */
	public long retrieveVariable8BytePacked(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;

	/**
     * Delete a single CECI variable.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param name variable name. CECI variable names have a maximum length of 10 characters including leading {@literal &}.
     * @throws CeciException
     */
    public void deleteVariable(@NotNull ICicsTerminal ceciTerminal, @NotNull String name) throws CeciException;

    /**
     * Delete all variables in this CECI session.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @throws CeciException
     */
    public void deleteAllVariables(@NotNull ICicsTerminal ceciTerminal) throws CeciException;

    /**
     * Retrieve the content of the current EXEC Interface Block (EIB)
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @return the {@link IExecInterfaceBlock} 
     * @throws CeciException
     */
    public IExecInterfaceBlock getEIB(@NotNull ICicsTerminal ceciTerminal) throws CeciException;

    /**
     * EXEC CICS LINK to a PROGRAM.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param programName the name of the PROGRAM
     * @param commarea a string representing the COMMAREA. If null, COMMAREA will be omitted from the command. Can be CECI variable name populated with
     *  (<b>&amp;</b>)name set via {@link #defineVariableText(ICicsTerminal, String, String)}) or the actual data. The value of DATALENGTH in the command will be 
     * be allowed to default.
     * @param sysid the system name where the CICS region where the link request is to be routed. If null, SYSID will be omitted from the command.
     * @param transid the name of the mirror transaction on the remote region. If null, TRANSID will be omitted from the command.
     * @param synconreturn the remote system should take a sync point at program end. If false, SYNCONRETURN will be omitted from the command.
     * @return an {@link ICeciResponse} object containing the command's response.
     * @throws CeciException
     */
    public ICeciResponse linkProgram(@NotNull ICicsTerminal ceciTerminal, @NotNull String programName, String commarea, String sysid, String transid, boolean synconreturn) throws CeciException;

    /**
     * EXEC CICS LINK to a PROGRAM with a CHANNEL. Use {@link #putContainer(ICicsTerminal, String, String, String, String, String, String)} to create the container(s) on the CHANNEL 
     * and {@link #getContainer(ICicsTerminal, String, String, String, String, String)} to retrieve the content after the LINK.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>  
     * @param programName the name of the PROGRAM
     * @param channelName the name of the CHANNEL.
     * @throws CeciExceptionan an {@link ICeciResponse} object containing the command's response.
     */
    public ICeciResponse linkProgramWithChannel(@NotNull ICicsTerminal ceciTerminal, @NotNull String programName, @NotNull String channelName, String sysid, String transid, boolean synconreturn) throws CeciException;
    
    /**
     * Puts data in a CONTAINER with an associated CHANNEL.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * @param channelName the CHANNELNAME
     * @param containerName the COTAINER name
     * @param content a string representing the container contents. Can be CECI variable name populated with (<b>&amp;</b>)name set via {@link #defineVariableText(ICicsTerminal, String, String)}) 
     * or the actual data. The value of FLENGTH in the command will be set to the data length.
     * @param dataType BIT or CHAR. If null, DATATYPE will be omitted from the command.
     * @param fromCcsid provides a value for FROMCCSID. If null, will be omitted from the command.
     * @param fromCodepage provides a value for FROMCODEPAGE. If null, will be omitted from the command.
     * @return an {@link ICeciResponse} object containing the command's response.
     * @throws CeciException
     */
    public ICeciResponse putContainer(@NotNull ICicsTerminal ceciTerminal, @NotNull String channelName, @NotNull String containerName, @NotNull String content, String dataType, String fromCcsid, String fromCodepage) throws CeciException;
    
    /**
     * Gets the data in a CONTAINER with an associated CHANNEL into a CECI variable.
     * @param ceciTerminal an {@link ICicsTerminal} object logged on to the CICS region and in an active CECI session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param channelName the CHANNELNAME
     * @param containerName the CONTAINER name
     * @param variableName the CECI variable name. Data can be retrieved using {@link #retrieveVariableText(ICicsTerminal, String)}
     * @param intoCcsid provides a value for INTOCCSID. If null, will be omitted from the command.
     * @param intoCodepage provides a value for INTOCODEPAGE. If null, will be omitted from the command.
     * @return an {@link ICeciResponse} object containing the command's response.
     * @throws CeciException
     */
    public ICeciResponse getContainer(@NotNull ICicsTerminal ceciTerminal, @NotNull String channelName, @NotNull String containerName, @NotNull String variableName, String intoCcsid, String intoCodepage) throws CeciException;

}

