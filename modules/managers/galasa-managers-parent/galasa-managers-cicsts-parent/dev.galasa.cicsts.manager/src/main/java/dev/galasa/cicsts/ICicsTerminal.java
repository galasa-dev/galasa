/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

import dev.galasa.zos3270.ITerminal;

public interface ICicsTerminal extends ITerminal {

    ICicsRegion getCicsRegion();

    boolean connectToCicsRegion() throws CicstsManagerException;
    
    ICicsTerminal resetAndClear() throws CicstsManagerException;
    
    /**
     * Use the CEOT transaction to set the Uppercase Translation status of this CICS TS terminal
     * @param ucctran true for UCCTRAN or false for NOUCCTRAN
     * @throws CicstsManagerException
     */
    public void setUppercaseTranslation(boolean ucctran) throws CicstsManagerException;
    
    /**
     * Use the CEOT transaction to determine the Uppercase Translation status of this CICS TS terminal<p>
     * <b>NOTE: </b>TRANIDONLY will return <code>false</code>
     * @return true if UCCTRAN or false if NOUCCTRAN/TRANIDONLY
     * @throws CicstsManagerException
     */
    public boolean isUppercaseTranslation() throws CicstsManagerException;

    String getLoginCredentialsTag();
    
    /**
     * Connect to a CICS region by APPLID. This method will:
     * 1. Connect to the host belonging to this terminal's z/OS image
     * 2. Detect the VAMP or USS screen
     * 3. Attempt to logon to the specified APPLID
     *
     * The method will retry for up to the default wait time if the welcome screen is not detected.
     *
     * @param host the hostname to connect to
     * @param applid the CICS APPLID to logon to
     * @throws CicstsManagerException if unable to locate welcome screen
     */
    void connectApplid(String applid) throws CicstsManagerException;

}
