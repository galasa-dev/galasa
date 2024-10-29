/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestCeciExecInterfaceBlockImpl {

    public static CeciExecInterfaceBlockImpl eib;
    private static String eibText = 
            "  LINK PROG(CUSTPGM)                                                            \r\n" + 
            "  EXEC INTERFACE BLOCK                                                          \r\n" + 
            "    EIBTIME      = +0103340                                                     \r\n" + 
            "    EIBDATE      = +0120064                                                     \r\n" + 
            "    EIBTRNID     = 'CECI'                                                       \r\n" + 
            "    EIBTASKN     = +0000245                                                     \r\n" + 
            "    EIBTRMID     = 'T104'                                                       \r\n" + 
            "    EIBCPOSN     = +00020                                                       \r\n" + 
            "    EIBCALEN     = +00000                                                       \r\n" + 
            "    EIBAID       = X'7D'                                                        \r\n" + 
            "    EIBFN        = X'0E08'          (LINK)                                      \r\n" + 
            "    EIBRCODE     = X'E00000000000'                                              \r\n" + 
            "    EIBDS        = '........'                                                   \r\n" + 
            "    EIBREQID     = '........'                                                   \r\n" + 
            "    EIBRSRCE     = '        '                                                   \r\n" + 
            "    EIBSYNC      = X'00'                                                        \r\n" + 
            "    EIBFREE      = X'00'                                                        \r\n" + 
            "    EIBRECV      = X'00'                                                        \r\n" + 
            "    EIBATT       = X'00'                                                        \r\n" + 
            "    EIBEOC       = X'00'                                                        \r\n" + 
            " +  EIBFMH       = X'00'                                                        \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            " PF 1 HELP 2 HEX 3 END 4 EIB 5 VAR 6 USER 7 SBH 8 SFH 9 MSG 10 SB 11 SF         " +
            "  LINK PROG(CUSTPGM)                                                            \r\n" + 
            "  EXEC INTERFACE BLOCK                                                          \r\n" + 
            " +  EIBCOMPL     = X'00'                                                        \r\n" + 
            "    EIBSIG       = X'00'                                                        \r\n" + 
            "    EIBCONF      = X'00'                                                        \r\n" + 
            "    EIBERR       = X'00'                                                        \r\n" + 
            "    EIBERRCD     = X'00000000'                                                  \r\n" + 
            "    EIBSYNRB     = X'00'                                                        \r\n" + 
            "    EIBNODAT     = X'00'                                                        \r\n" + 
            "    EIBRESP      = +0000000016      (ABEND AEIP)                                \r\n" + 
            "    EIBRESP2     = +0000000002                                                  \r\n" + 
            "    EIBRLDBK     = X'00'                                                        \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            " PF 1 HELP 2 HEX 3 END 4 EIB 5 VAR 6 USER 7 SBH 8 SFH 9 MSG 10 SB 11 SF         ";
    private static String eibHex =
            "  LINK PROG(CUSTPGM)                                                            \r\n" + 
            "  EXEC INTERFACE BLOCK                                                          \r\n" + 
            "    EIBTIME      = X'0103340C'                                                  \r\n" + 
            "    EIBDATE      = X'0120064F'                                                  \r\n" + 
            "    EIBTRNID     = X'C3C5C3C9'                                                  \r\n" + 
            "    EIBTASKN     = X'0000245C'                                                  \r\n" + 
            "    EIBTRMID     = X'E3F1F0F4'                                                  \r\n" + 
            "    EIBCPOSN     = X'0014'                                                      \r\n" + 
            "    EIBCALEN     = X'0000'                                                      \r\n" + 
            "    EIBAID       = X'7D'                                                        \r\n" + 
            "    EIBFN        = X'0E08'          (LINK)                                      \r\n" + 
            "    EIBRCODE     = X'E00000000000'                                              \r\n" + 
            "    EIBDS        = X'0000000000000000'                                          \r\n" + 
            "    EIBREQID     = X'0000000000000000'                                          \r\n" + 
            "    EIBRSRCE     = X'4040404040404040'                                          \r\n" + 
            "    EIBSYNC      = X'00'                                                        \r\n" + 
            "    EIBFREE      = X'00'                                                        \r\n" + 
            "    EIBRECV      = X'00'                                                        \r\n" + 
            "    EIBATT       = X'00'                                                        \r\n" + 
            "    EIBEOC       = X'00'                                                        \r\n" + 
            " +  EIBFMH       = X'00'                                                        \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            " PF 1 HELP 2 HEX 3 END 4 EIB 5 VAR 6 USER 7 SBH 8 SFH 9 MSG 10 SB 11 SF         " +
            "  LINK PROG(CUSTPGM)                                                            \r\n" + 
            "  EXEC INTERFACE BLOCK                                                          \r\n" + 
            " +  EIBCOMPL     = X'00'                                                        \r\n" + 
            "    EIBSIG       = X'00'                                                        \r\n" + 
            "    EIBCONF      = X'00'                                                        \r\n" + 
            "    EIBERR       = X'00'                                                        \r\n" + 
            "    EIBERRCD     = X'00000000'                                                  \r\n" + 
            "    EIBSYNRB     = X'00'                                                        \r\n" + 
            "    EIBNODAT     = X'00'                                                        \r\n" + 
            "    EIBRESP      = X'00000010'      (ABEND AEIP)                                \r\n" + 
            "    EIBRESP2     = X'00000002'                                                  \r\n" + 
            "    EIBRLDBK     = X'00'                                                        \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            "                                                                                \r\n" + 
            " PF 1 HELP 2 HEX 3 END 4 EIB 5 VAR 6 USER 7 SBH 8 SFH 9 MSG 10 SB 11 SF         ";
    
    @BeforeClass
    public static void beforeClass() {
        eib = new CeciExecInterfaceBlockImpl(eibText, eibHex);
    }

    @Test
    public void testGetResponse() {
        Assert.assertEquals("Unxpected result", "ABEND AEIP", eib.getResponse());
    }

    @Test
    public void testGetEIBTIME() {
        Assert.assertEquals("Unxpected result", 103340, eib.getEIBTIME());
    }

    @Test
    public void testGetEIBDATE() {
        Assert.assertEquals("Unxpected result", 120064, eib.getEIBDATE());
    }

    @Test
    public void testGetEIBTRNID() {
        Assert.assertEquals("Unxpected result", "CECI", eib.getEIBTRNID(false));
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x0C3, 0x0C5, 0x0C3, 0x0C9}, eib.getEIBTRNID(true).toCharArray()));  
    }

    @Test
    public void testGetEIBTASKN() {
        Assert.assertEquals("Unxpected result", 245, eib.getEIBTASKN());
    }

    @Test
    public void testGetEIBTRMID() {
        Assert.assertEquals("Unxpected result", "T104", eib.getEIBTRMID(false));
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x0E3, 0x0F1, 0x0F0, 0x0F4}, eib.getEIBTRMID(true).toCharArray()));
    }

    @Test
    public void testGetEIBCPOSN() {
        Assert.assertEquals("Unxpected result", 20, eib.getEIBCPOSN());
    }

    @Test
    public void testGetEIBCALEN() {
        Assert.assertEquals("Unxpected result", 0, eib.getEIBCALEN());
    }

    @Test
    public void testGetEIBAID() {
        Assert.assertEquals("Unxpected result", 0x07D, eib.getEIBAID());
    }

    @Test
    public void testGetEIBFN() {
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x00E, 0x008}, eib.getEIBFN()));
    }

    @Test
    public void testGetEIBFNText() {
        Assert.assertEquals("Unxpected result", "LINK", eib.getEIBFNText());
    }

    @Test
    public void testGetEIBRCODE() {
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x0E0, 0x000, 0x000, 0x000, 0x000, 0x000}, eib.getEIBRCODE()));
    }

    @Test
    public void testGetEIBDS() {
        Assert.assertEquals("Unxpected result", "........", eib.getEIBDS(false));
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000}, eib.getEIBDS(true).toCharArray()));
    }

    @Test
    public void testGetEIBREQID() {
        Assert.assertEquals("Unxpected result", "........", eib.getEIBREQID(false));        
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000, 0x000}, eib.getEIBREQID(true).toCharArray()));
    }

    @Test
    public void testGetEIBRSRCE() {
        Assert.assertEquals("Unxpected result", "        ", eib.getEIBRSRCE(false));
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x040, 0x040, 0x040, 0x040, 0x040, 0x040, 0x040, 0x040}, eib.getEIBRSRCE(true).toCharArray()));
    }

    @Test
    public void testGetEIBSYNC() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBSYNC());
    }

    @Test
    public void testGetEIBFREE() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBFREE());
    }

    @Test
    public void testGetEIBRECV() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBRECV());
    }

    @Test
    public void testGetEIBATT() {
        Assert.assertEquals("Unxpected result", 0, eib.getEIBATT());
    }

    @Test
    public void testGetEIBEOC() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBEOC());
    }

    @Test
    public void testGetEIBFMH() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBFMH());
    }

    @Test
    public void testGetEIBCOMPL() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBCOMPL());
    }

    @Test
    public void testGetEIBSIG() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBSIG());
    }

    @Test
    public void testGetEIBCONF() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBCONF());
    }

    @Test
    public void testGetEIBERR() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBERR());
    }

    @Test
    public void testGetEIBERRCD() {
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x000, 0x000, 0x000, 0x000}, eib.getEIBERRCD()));
    }

    @Test
    public void testGetEIBSYNRB() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBSYNRB());
    }

    @Test
    public void testGetEIBNODAT() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBNODAT());
    }

    @Test
    public void testGetEIBRESP() {
        Assert.assertEquals("Unxpected result", 16, eib.getEIBRESP());
    }

    @Test
    public void testGetEIBRESP2() {
        Assert.assertEquals("Unxpected result", 2, eib.getEIBRESP2());
    }

    @Test
    public void testGetEIBRLDBK() {
        Assert.assertEquals("Unxpected result", 0x000, eib.getEIBRLDBK());
    }
    
    @Test
    public void testMoreCoverage() {
        String text = "    EIBRESP      = +0000000000                                                 \r\n";
        CeciExecInterfaceBlockImpl dummyEib = new CeciExecInterfaceBlockImpl(text , " ");
        Assert.assertEquals("Unxpected result", "NORMAL", dummyEib.getResponse());
        
        text = "    EIBRESP      = +0000000001                                                  \r\n";
        dummyEib = new CeciExecInterfaceBlockImpl(text , " ");
        Assert.assertEquals("Unxpected result", "", dummyEib.getResponse());
        
        text = "    EIBFN        = X'FFFF'                                                      \r\n";
        dummyEib = new CeciExecInterfaceBlockImpl(text , " ");
        Assert.assertTrue("Unxpected result", Arrays.equals(new char[] {0x0FF, 0x0FF}, dummyEib.getEIBFN()));
    }
}
