/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts;

/**
 * Represents the fields in the CICS/TS EXEC Interface Block (EIB).
 *
 */
public interface IExecInterfaceBlock {
    
    /**
     * @return a {@link String} representation of the EIBRESP field if available
     */
    public String getResponse();
    
    /**
     * @return the value of the EIBTIME field in the EIB.
     *
     */
    public int getEIBTIME();

    /**
     * @return the value of the EIBDATE field in the EIB.
     * 
     */
    public int getEIBDATE();

    /**
     * @return the value of the EIBTRNID field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * 
     */
    public String getEIBTRNID(boolean hex);

    /**
     * @return the value of the EIBTASKN field in the EIB.
     * 
     */
    public int getEIBTASKN();

    /**
     * @return the value of the EIBTRMID field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * 
     */
    public String getEIBTRMID(boolean hex);

    /**
     * @return the value of the EIBCPOSN field in the EIB.
     * 
     */
    public int getEIBCPOSN();
    
    /**
     * @return the value of the EIBCALEN field in the EIB.
     * 
     */
    public int getEIBCALEN();
    
    /**
     * @return the value of the EIBAID field in the EIB.
     * 
     */
    public char getEIBAID();

    /**
     * @return the value of the EIBFN field in the EIB.
     * 
     */
    public char[] getEIBFN();

    /**
     * @return the value of the EIBRCODE  field in the EIB.
     * 
     */
    public char[] getEIBRCODE();

    /**
     * @return the value of the EIBDS  field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * 
     */
    public String getEIBDS(boolean hex);

    /**
     * @return the value of the EIBREQID field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * 
     */
    public String getEIBREQID(boolean hex);

    /**
     * @return the value of the EIBRSRCE field in the EIB.
     * @param hex return a char array containing the hex values of the field
     * 
     */
    public String getEIBRSRCE(boolean hex);

    /**
     * @return the value of the EIBSYNC field in the EIB.
     * 
     */
    public char getEIBSYNC();

    /**
     * @return the value of the EIBFREE field in the EIB.
     * 
     */
    public char getEIBFREE();

    /**
     * @return the value of the EIBRECV field in the EIB.
     * 
     */
    public char getEIBRECV();
    
    /**
     * @return the value of the EIBATT field in the EIB.
     * 
     */
    public char getEIBATT();

    /**
     * @return the value of the EIBEOC field in the EIB.
     * 
     */
    public char getEIBEOC();

    /**
     * @return the value of the  field in the EIB.
     * 
     */
    public char getEIBFMH();

    /**
     * @return the value of the EIBCOMPL field in the EIB.
     * 
     */
    public char getEIBCOMPL();

    /**
     * @return the value of the EIBSIG field in the EIB.
     * 
     */
    public char getEIBSIG();

    /**
     * @return the value of the EIBCONF field in the EIB.
     * 
     */
    public char getEIBCONF();

    /**
     * @return the value of the EIBERR field in the EIB.
     * 
     */
    public char getEIBERR();

    /**
     * @return the value of the EIBERRCD field in the EIB.
     * 
     */
    public char[] getEIBERRCD();

    /**
     * @return the value of the EIBSYNRB field in the EIB.
     * 
     */
    public char getEIBSYNRB();

    /**
     * @return the value of the EIBNODAT field in the EIB.
     * 
     */
    public char getEIBNODAT();

    /**
     * @return the value of the EIBRESP field in the EIB.
     * 
     */
    public int getEIBRESP();

    /**
     * @return the value of the EIBRESP2 field in the EIB.
     * 
     */
    public int getEIBRESP2();

    /**
     * @return the value of the EIBRLDBK field in the EIB.
     * 
     */
    public char getEIBRLDBK();
}
