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

}
