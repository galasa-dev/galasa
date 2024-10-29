/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import dev.galasa.cicsts.IExecInterfaceBlock;

public class CeciExecInterfaceBlockImpl implements IExecInterfaceBlock {

    private int eibtime;
    private int eibdate;
    private String eibtrnid;
    private int eibtaskn;
    private String eibtrmid;
    private int eibcposn;
    private int eibcalen;
    private char eibaid;
    private char[] eibfn;
    private String eibfnString = "";
    private char[] eibrcode;
    private String eibds;
    private String eibreqid;
    private String eibrsrce;
    private char eibsync;
    private char eibfree;
    private char eibrecv;
    private char eibatt;
    private char eibeoc;
    private char eibfmh;
    private char eibcompl;
    private char eibsig;
    private char eibconf;
    private char eiberr;
    private char[] eiberrcd;
    private char eibsynrb;
    private char eibnodat;
    private int eibresp;
    private String eibrespString = "";
    private int eibresp2;
    private char eibrldbk;
    private char[] eibtrnidHex;
    private char[] eibtrmidHex;
    private char[] eibdsHex;
    private char[] eibreqidHex;
    private char[] eibrsrceHex;
    
    public CeciExecInterfaceBlockImpl(String eibText, String eibHex) {
        parseText(eibText);
        parseHex(eibHex);
    }

    @Override
    public String getResponse() {
        return eibrespString;
    }

    @Override
    public int getEIBTIME() {
        return eibtime;
    }

    @Override
    public int getEIBDATE() {
        return eibdate;
    }

    @Override
    public String getEIBTRNID(boolean hex) {
        if (hex) {
            return new String(eibtrnidHex);
        }
        return eibtrnid;
    }

    @Override
    public int getEIBTASKN() {
        return eibtaskn;
    }

    @Override
    public String getEIBTRMID(boolean hex) {
        if (hex) {
            return new String(eibtrmidHex);
        }
        return eibtrmid;
    }

    @Override
    public int getEIBCPOSN() {
        return eibcposn;
    }

    @Override
    public int getEIBCALEN() {
        return eibcalen;
    }

    @Override
    public char getEIBAID() {
        return eibaid;
    }

    @Override
    public char[] getEIBFN() {
        return eibfn;
    }

    public String getEIBFNText() {
        return eibfnString;
    }

    @Override
    public char[] getEIBRCODE() {
        return eibrcode;
    }

    @Override
    public String getEIBDS(boolean hex) {
        if (hex) {
            return new String(eibdsHex);
        }
        return eibds;
    }

    @Override
    public String getEIBREQID(boolean hex) {
        if (hex) {
            return new String(eibreqidHex);
        }
        return eibreqid;
    }

    @Override
    public String getEIBRSRCE(boolean hex) {
        if (hex) {
            return new String(eibrsrceHex);
        }
        return eibrsrce;
    }

    @Override
    public char getEIBSYNC() {
        return eibsync;
    }

    @Override
    public char getEIBFREE() {
        return eibfree;
    }

    @Override
    public char getEIBRECV() {
        return eibrecv;
    }

    @Override
    public char getEIBATT() {
        return eibatt;
    }

    @Override
    public char getEIBEOC() {
        return eibeoc;
    }

    @Override
    public char getEIBFMH() {
        return eibfmh;
    }

    @Override
    public char getEIBCOMPL() {
        return eibcompl;
    }

    @Override
    public char getEIBSIG() {
        return eibsig;
    }

    @Override
    public char getEIBCONF() {
        return eibconf;
    }

    @Override
    public char getEIBERR() {
        return eiberr;
    }

    @Override
    public char[] getEIBERRCD() {
        return eiberrcd;
    }

    @Override
    public char getEIBSYNRB() {
        return eibsynrb;
    }

    @Override
    public char getEIBNODAT() {
        return eibnodat;
    }

    @Override
    public int getEIBRESP() {
        return eibresp;
    }

    @Override
    public int getEIBRESP2() {
        return eibresp2;
    }

    @Override
    public char getEIBRLDBK() {
        return eibrldbk;
    }

    private void parseText(String eibText) {
        String[] lines = eibText.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("+")) {
                line = line.replace("+  ", "");
            }
            if (line.contains("=")) {
                String[] fields = line.trim().split("=");
                String name = fields[0].trim();
                String value = fields[1].trim();
                switch(name) { 
                    case "EIBTIME" : eibtime  = Integer.parseInt(value); break;
                    case "EIBDATE" : eibdate  = Integer.parseInt(value); break;
                    case "EIBTRNID": eibtrnid = value.substring(1, value.lastIndexOf('\'')); break;
                    case "EIBTASKN": eibtaskn = Integer.parseInt(value); break;
                    case "EIBTRMID": eibtrmid = value.substring(1, value.lastIndexOf('\'')); break;
                    case "EIBCPOSN": eibcposn = Integer.parseInt(value); break;
                    case "EIBCALEN": eibcalen = Integer.parseInt(value); break;
                    case "EIBAID"  : eibaid   = toCharArray(value)[0]; break;
                    case "EIBFN"   : setEibfn(value); break;
                    case "EIBRCODE": eibrcode = toCharArray(value); break;
                    case "EIBDS"   : eibds    = value.substring(1, value.lastIndexOf('\'')); break;
                    case "EIBREQID": eibreqid = value.substring(1, value.lastIndexOf('\'')); break;
                    case "EIBRSRCE": eibrsrce = value.substring(1, value.lastIndexOf('\'')); break;
                    case "EIBSYNC" : eibsync  = toCharArray(value)[0]; break;
                    case "EIBFREE" : eibfree  = toCharArray(value)[0]; break;
                    case "EIBRECV" : eibrecv  = toCharArray(value)[0]; break;
                    case "EIBATT"  : eibatt   = toCharArray(value)[0]; break;
                    case "EIBEOC"  : eibeoc   = toCharArray(value)[0]; break;
                    case "EIBFMH"  : eibfmh   = toCharArray(value)[0]; break;
                    case "EIBCOMPL": eibcompl = toCharArray(value)[0]; break;
                    case "EIBSIG"  : eibsig   = toCharArray(value)[0]; break;
                    case "EIBCONF" : eibconf  = toCharArray(value)[0]; break;
                    case "EIBERR"  : eiberr   = toCharArray(value)[0]; break;
                    case "EIBERRCD": eiberrcd = toCharArray(value); break;
                    case "EIBSYNRB": eibsynrb = toCharArray(value)[0]; break;
                    case "EIBNODAT": eibnodat = toCharArray(value)[0]; break;
                    case "EIBRESP" : setEibresp(value); break;
                    case "EIBRESP2": eibresp2 = Integer.parseInt(value); break;
                    case "EIBRLDBK": eibrldbk = toCharArray(value)[0]; break;
                    default:
                }
            }
        }
    }

    private void setEibfn(String value) { 
        if (value.contains("(")) {
            eibfnString = value.substring(value.indexOf('(')+1, value.indexOf(')'));
            value = value.split(" ")[0];
        }
        eibfn = toCharArray(value);
    }

    private void setEibresp(String value) {
        if (value.contains("(")) {
            eibrespString = value.substring(value.indexOf('(')+1, value.indexOf(')'));
            value = value.split(" ")[0];
        }
        eibresp = Integer.parseInt(value);
        if (eibrespString.isEmpty() && eibresp == 0) {
            eibrespString = "NORMAL";
        }
    }

    private void parseHex(String eibHex) {
        String[] lines = eibHex.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("=")) {
                String[] fields = line.trim().split("=");
                String name = fields[0].trim();
                String value = fields[1].trim();
                boolean done = false;
                switch(name) { 
                    case "EIBTRNID": eibtrnidHex = toCharArray(value); break;
                    case "EIBTRMID": eibtrmidHex = toCharArray(value); break;
                    case "EIBDS"   : eibdsHex    = toCharArray(value); break;
                    case "EIBREQID": eibreqidHex = toCharArray(value); break;
                    case "EIBRSRCE": eibrsrceHex = toCharArray(value); break;
                    case "EIBSYNC" : done  = true; break;
                    default:
                }
                if (done) {
                    break;
                }
            }
        }
    }

    private char[] toCharArray(String value) {
        StringBuilder sb = new StringBuilder();
        String hexField = value.substring(2, value.lastIndexOf('\''));
        String[] hexArray = hexField.split("(?<=\\G.{2})");
        for (String hexString : hexArray) {
            char ccc = (char) Long.parseLong(hexString, 16);
            sb.append(ccc);
        }
        return sb.toString().toCharArray();
    }

}
