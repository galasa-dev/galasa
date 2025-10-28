/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.ITerminal;

public interface ICemt {

   

    /** 
     * Inquire a CEMT resource using the resource type and name.
     * This does not support inquiries of multiple resources at once. 
     * @return null if the resource is not found.
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @return a {@link CicstsHashMap} object containing all of the properties of the resource.
     * @throws CemtException.
     */
   
    public CicstsHashMap inquireResource(@NotNull ICicsTerminal cemtTerminal,
                                                   @NotNull String resourceType,
                                                   @NotNull String resourceName
                                                   ) throws CemtException;
    
    /**
     * Inquire a CEMT resource using the resource type and name.
     * This methods searches the text and give the response back as boolean value (true/false).
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @param searchText
     * @return boolean value
     * @throws CemtException
     */
    public boolean inquireResource(
            @NotNull ICicsTerminal cemtTerminal,
            @NotNull String resourceType,
            @NotNull String resourceName,
            @NotNull String searchText) throws CemtException;

    /**
     * Wait for a CICS resource to become disabled. This method will issue a
     * CEMT INQUIRE every 500ms for as long as the resourceTimeout parameter
     * specifies. This method looks for "Dis" to indicate the resource is
     * disabled.
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @param resourceTimeout
     * @throws CemtException
     */
    public void waitForDisabledResource(ICicsTerminal terminal,
            String resourceType, String resourceName,
            int resourceTimeout) throws CemtException;
    
    /**
     * See {@link #waitForEnabledResource(ITerminal, String, String, int)} for
     * explanation, the default resource timeout is used.
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @throws CEMTException
     */
    public void waitForEnabledResource(ICicsTerminal terminal, String resourceType,
        String resourceName) throws CemtException;

    /**
     * Wait for a CICS resource to become enabled. This method will issue a CEMT
     * INQUIRE every 500ms for as long as the resourceTimeout parameter
     * specifies. This method looks for " Ena " to indicate the resource is
     * enabled.
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @param resourceTimeout
     *            - The timeout in milliseconds.
     * @throws CEMTException
     */
    public void waitForEnabledResource(ICicsTerminal terminal, String resourceType,
        String resourceName, int resourceTimeout) throws CemtException;

    /** 
     * Set the state of a CEMT resource using the resource type and name.
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param resourceType a {@link String} of the type of resource you want to set.
     * @param resourceName a {@link String} of the name of the resource you want to set. Can be {@code null} for example {@code SET DB2CONN ...}.
     * @param action a {@link String} of the action you want to perform on the resource.
     * @return a {@link CicstsHashMap} object containing all of the properties of the resource.
     * @throws CemtException
     */

    public CicstsHashMap setResource(@NotNull ICicsTerminal cemtTerminal,
                            @NotNull String resourceType,
                                     String resourceName,
                            @NotNull String action)throws CemtException;
    
    
    /**
     * Discards a specified resource and throws an exception if the specified search text is not found on the terminal.
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param resourceType a {@link String} of the type of resource you want to discard.
     * @param resourceName a {@link String} of the name of the resource you want to discard.
     * @throws CemtException
     */

    public void discardResource(@NotNull ICicsTerminal cemtTerminal,
                               @NotNull String resourceType,
                               @NotNull String resourceName) throws CemtException;
    
    /**
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status. 
     * For example, the test could first issue <code>CEOT TRANIDONLY</code>
     * @param systemArea a {@link String} the specifies the system area.
     * @param setRequest
     * @param expectedResponse
     * @return boolean
     * @throws CemtException
     */

    public boolean performSystemProperty(@NotNull ICicsTerminal cemtTerminal,
                                         @NotNull String systemArea,
                                         @NotNull String setRequest,
                                         @NotNull String expectedResponse)throws CemtException;

    /**
     * Does a CEMT Inquire and expects it to be NOTFOUND.
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @param searchText - text to be searched on terminal           
     * @throws CemtException - throws when error occurs in the CEMT transaction 
     */
    public boolean inquireResourceNotFound(ICicsTerminal terminal, String resourceType, String resourceName, String searchText) throws CemtException;

    /**
     * Does a CEMT Inquire and expects it to be NOTFOUND.
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @param searchText - text to be searched on terminal
     * @param state - the status of the resource
     * @throws CemtException -throws when error occurs in the CEMT transaction 
     */
    public boolean inquireResourceNotFound(ICicsTerminal terminal, String resourceType, String resourceName, String searchText,
        String state) throws CemtException;

    /**
     * Wait for a CICS resource to become disabled. This method will issue a
     * CEMT INQUIRE . This method looks for "Dis" to indicate the resource is
     * disabled.
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @throws CemtException - throws when error occurs in the CEMT transaction.
     */
    public void waitForDisabledResource(ICicsTerminal terminal, String resourceType, String resourceName) throws CemtException;

    /**
     * Does a CEMT Inquire and expects it to be ENABLED.
     * 
     * @param cemtTerminal an {@link ITerminal} object logged on to the CICS region and in an active CEMT session.
     * If mixed case is required, the terminal should be presented with no upper case translate status.
     * @param resourceType a {@link String} of the resource type you are looking for.
     * @param resourceName a {@link String} of the name of the resource you are looking for.
     * @throws CemtException - throws when error occurs in the CEMT transaction.
     */
    boolean isResourceEnabled(ICicsTerminal terminal, String resourceType, String resourceName)
        throws CemtException;

}