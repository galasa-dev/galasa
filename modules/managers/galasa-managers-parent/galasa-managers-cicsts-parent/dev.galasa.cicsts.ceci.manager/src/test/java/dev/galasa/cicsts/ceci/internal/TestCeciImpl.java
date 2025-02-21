/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.ceci.internal;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import dev.galasa.cicsts.CeciException;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.spi.NetworkException;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestCeciImpl {
    private CeciImpl ceci;

    private CeciImpl ceciSpy;

    // Static fields in CeciImpl
    private static final String INITIAL_SCREEN_ID = "STATUS:  ENTER ONE OF THE FOLLOWING";
    private static final String VAR_SCREEN_ID = "VARIABLES   LENGTH   DATA";
    private static final String COMMAND_EXECUTION_COMPLETE = "STATUS:  COMMAND EXECUTION COMPLETE";
    private static final String MESSAGE_DFHAC2206 = "DFHAC2206";
    private static final String NO_SYNTAX_MESSAGES = "THERE ARE NO MESSAGES";
    private static final String WRONG_CICS_REGION = "Provided terminal does not belong to the correct CICS TS Region";

    private static final String COMMAND_VALUE = "COMMAND";
    private static final String TEXT_VARIABLE_NAME = "&VARIABLE";
    private static final String TEXT_VARIABLE_VALUE = "VARIABLE";
    private static final String PROGRAM_NAME = "MYPROG";
    private static final String CHANNEL_NAME = "CHANNEL";
    private static final String CONTAINER_NAME = "CONTAINER";
    private static final String SPACES = "        ";

    @Mock
    private ICicsTerminal ceciTerminalMock;

    @Mock
    private ICicsTerminal wrongCeciTerminalMock;

    @Mock
    private ICicsRegion  cicsRegionMock;

    @Mock
    private ICicsRegion  wrongCicsRegionMock;

    @Mock
    private ICeciResponse ceciResponseMock;

    @Before
    public void setup() throws FieldNotFoundException, KeyboardLockedException, NetworkException, TerminalInterruptedException, TimeoutException {
        ceci = new CeciImpl(null, cicsRegionMock);
        ceciSpy = Mockito.spy(ceci);

        // Mock all terminal function
        Mockito.lenient().when(ceciTerminalMock.type(Mockito.any())).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.enter()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.pf2()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.pf3()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.pf4()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.pf5()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.pf9()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.pf10()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.pf11()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.tab()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.home()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.newLine()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.eraseEof()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenReturn(ceciTerminalMock);
        Mockito.lenient().when(ceciTerminalMock.reportScreenWithCursor()).thenReturn(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.getCicsRegion()).thenReturn(cicsRegionMock);
        Mockito.when(wrongCeciTerminalMock.getCicsRegion()).thenReturn(wrongCicsRegionMock);
    }

    @Test
    public void teststartCECISession() throws Exception {
        setupTestIssueCommand();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(INITIAL_SCREEN_ID);
        Mockito.when(ceciTerminalMock.isClearScreen()).thenReturn(true);
        ceciSpy.startCECISession(ceciTerminalMock);

        Mockito.when(ceciTerminalMock.isClearScreen()).thenReturn(false);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("NOT_INITIAL_SCREEN_ID");
        String expectedMessage = "Not on CECI initial screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.startCECISession(ceciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());

        Mockito.when(ceciTerminalMock.resetAndClear()).thenThrow(new CicstsManagerException());
        expectedMessage = "Problem starting CECI session";
        expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.startCECISession(ceciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());

        expectedMessage = WRONG_CICS_REGION;
        expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.startCECISession(wrongCeciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testIssueCommand() throws Exception {
        setupTestIssueCommand();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(COMMAND_EXECUTION_COMPLETE);
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE));

        Mockito.doReturn(COMMAND_VALUE).when(ceciSpy).retrieveVariableText(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, TEXT_VARIABLE_NAME));

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("USER SCREEN").thenReturn("USER SCREEN").thenReturn(COMMAND_EXECUTION_COMPLETE);
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE));

        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE, null));

        HashMap<String, Object> options = new HashMap<>();
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE, options));

        options.put("KEY1", null);
        options.put("KEY2", "");
        options.put("KEY3", "VALUE");
        Assert.assertEquals("Error in issueCommand() method", ceciResponseMock, ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE, options));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.issueCommand(wrongCeciTerminalMock, COMMAND_VALUE, true);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testIssueCommandException1() throws Exception {
        setupTestIssueCommand();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(MESSAGE_DFHAC2206);
        String expectedMessage = "Command abended - see previous screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testIssueCommandException2() throws Exception {
        setupTestIssueCommand();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("USER SCREEN").thenReturn("USER SCREEN").thenReturn("USER SCREEN");
        String expectedMessage = "Command failed - see previous screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());

    }

    @Test
    public void testIssueCommandException3() throws Exception {
        setupTestIssueCommand();

        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Error issuing CECI command";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());

    }

    @Test
    public void testIssueCommandException4() throws Exception {
        setupTestIssueCommand();

        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Error issuing CECI command";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.issueCommand(ceciTerminalMock, COMMAND_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());

    }

    private void setupTestIssueCommand() throws Exception {
        Mockito.doReturn(0).when(ceciSpy).defineVariableText(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).initialScreen();
        Mockito.doNothing().when(ceciSpy).checkForSyntaxMessages();
        Mockito.doReturn(ceciResponseMock).when(ceciSpy).newCeciResponse(Mockito.anyBoolean());
    }

    @Test
    public void testDefineVariableText() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy).setVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in defineVariableText() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.defineVariableText(ceciTerminalMock, TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.defineVariableText(wrongCeciTerminalMock, TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDefineVariableBinary() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy).setVariableHex(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in defineVariableBinary() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.defineVariableBinary(ceciTerminalMock, TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray()));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.defineVariableBinary(wrongCeciTerminalMock, TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray());
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDefineVariableDoubleWord() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(99).when(ceciSpy).setVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in defineVariableDoubleWord() method", 99, ceciSpy.defineVariableDoubleWord(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.defineVariableDoubleWord(wrongCeciTerminalMock, TEXT_VARIABLE_NAME, 0);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDefineVariableFullWord() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(99).when(ceciSpy).setVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in defineVariableFullWord() method", 99, ceciSpy.defineVariableFullWord(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.defineVariableFullWord(wrongCeciTerminalMock, TEXT_VARIABLE_NAME, 0);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDefineVariableHalfWord() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(99).when(ceciSpy).setVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in defineVariableHalfWord() method", 99, ceciSpy.defineVariableHalfWord(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.defineVariableHalfWord(wrongCeciTerminalMock, TEXT_VARIABLE_NAME, 0);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDefineVariable4BytePacked() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(99).when(ceciSpy).setVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in defineVariablePacked() method", 99, ceciSpy.defineVariable4BytePacked(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.defineVariable4BytePacked(wrongCeciTerminalMock, TEXT_VARIABLE_NAME, 0);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDefineVariable8BytePacked() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(99).when(ceciSpy).setVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in defineVariablePacked() method", 99, ceciSpy.defineVariable8BytePacked(ceciTerminalMock, TEXT_VARIABLE_NAME, 0));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.defineVariable8BytePacked(wrongCeciTerminalMock, TEXT_VARIABLE_NAME, 0);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testRetrieveVariableText() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(TEXT_VARIABLE_VALUE).when(ceciSpy).getVariable(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in retrieveVariableText() method", TEXT_VARIABLE_VALUE, ceciSpy.retrieveVariableText(ceciTerminalMock, TEXT_VARIABLE_NAME));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.retrieveVariableText(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testRetrieveVariableBinary() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn(TEXT_VARIABLE_VALUE.toCharArray()).when(ceciSpy).getVariableHex(Mockito.any());
        Assert.assertTrue("Error in retrieveVariableBinary() method", Arrays.equals(TEXT_VARIABLE_VALUE.toCharArray(), ceciSpy.retrieveVariableBinary(ceciTerminalMock, TEXT_VARIABLE_NAME)));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.retrieveVariableBinary(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testRetrieveVariableDoubleWord() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn("99").when(ceciSpy).getVariable(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in retrieveVariableDoubleWord() method", 99L, ceciSpy.retrieveVariableDoubleWord(ceciTerminalMock, TEXT_VARIABLE_NAME));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.retrieveVariableDoubleWord(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testRetrieveVariableFullWord() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn("99").when(ceciSpy).getVariable(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in retrieveVariableFullWord() method", 99, ceciSpy.retrieveVariableFullWord(ceciTerminalMock, TEXT_VARIABLE_NAME));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.retrieveVariableFullWord(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testRetrieveVariableHalfWord()  throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn("99").when(ceciSpy).getVariable(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in retrieveVariableHalfWord() method", 99, ceciSpy.retrieveVariableHalfWord(ceciTerminalMock, TEXT_VARIABLE_NAME));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.retrieveVariableHalfWord(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testRetrieveVariable4BytePacked() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn("99").when(ceciSpy).getVariable(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in retrieveVariablePacked() method", 99, ceciSpy.retrieveVariable4BytePacked(ceciTerminalMock, TEXT_VARIABLE_NAME));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.retrieveVariable4BytePacked(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testRetrieveVariable8BytePacked() throws Exception {
        Mockito.doReturn(TEXT_VARIABLE_NAME).when(ceciSpy).validateVariable(Mockito.any(),Mockito.any(),Mockito.any());
        Mockito.doReturn("99").when(ceciSpy).getVariable(Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in retrieveVariablePacked() method", 99, ceciSpy.retrieveVariable8BytePacked(ceciTerminalMock, TEXT_VARIABLE_NAME));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.retrieveVariable8BytePacked(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDeleteVariable() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(SPACES);
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        Mockito.verify(ceciTerminalMock,Mockito.times(1)).retrieveScreen();

        Mockito.clearInvocations(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE))
                                                        .thenReturn(SPACES);
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        Mockito.verify(ceciTerminalMock,Mockito.times(2)).retrieveScreen();

        Mockito.clearInvocations(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE))
                                                       .thenReturn(SPACES);
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "XXXX"))
                                                              .thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        Mockito.verify(ceciTerminalMock,Mockito.times(2)).retrieveScreen();

        ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME.substring(1));
        Mockito.verify(ceciTerminalMock,Mockito.times(3)).retrieveScreen();

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteVariable(wrongCeciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDeleteVariableException1() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "XXXX"))
                                                              .thenReturn("PF");
        String expectedMessage = "Unable to find variable to delete";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testDeleteVariableException2() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "XXXX"))
                                                              .thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        String expectedMessage = "Delete variable failed";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testDeleteVariableException3() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to delete variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testDeleteVariableException4() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(String.format(" %-10s   %+06d   %s", TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.length(), TEXT_VARIABLE_VALUE));
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to delete variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteVariable(ceciTerminalMock, TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testDeleteAllVariables() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("");
        ceciSpy.deleteAllVariables(ceciTerminalMock);
        Mockito.verify(ceciTerminalMock,Mockito.times(55)).tab();

        Mockito.clearInvocations(ceciTerminalMock);
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("PF");
        ceciSpy.deleteAllVariables(ceciTerminalMock);
        Mockito.verify(ceciTerminalMock,Mockito.times(1)).tab();

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteAllVariables(wrongCeciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testDeleteAllVariablesException1() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to delete all variables";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteAllVariables(ceciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testDeleteAllVariablesException2() throws Exception {
        setupTestDeleteVariable();

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to delete all variables";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.deleteAllVariables(ceciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    private void setupTestDeleteVariable() throws Exception {
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).hexOff();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).variableScreen();
    }

    @Test
    public void testGetEIB() throws Exception {
        setupTestGetEIB();
        Assert.assertTrue("Error in getEIB() method",  ceciSpy.getEIB(ceciTerminalMock) instanceof CeciExecInterfaceBlockImpl);

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getEIB(wrongCeciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testGetEIBException1() throws Exception {
        setupTestGetEIB();

        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to navigate to EIB screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getEIB(ceciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetEIBException2() throws Exception {
        setupTestGetEIB();

        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to navigate to EIB screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getEIB(ceciTerminalMock);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    private void setupTestGetEIB() throws Exception {
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).hexOff();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).hexOn();
    }

    @Test
    public void testLinkProgram() throws Exception {
        Mockito.doReturn(ceciResponseMock).when(ceciSpy).issueCommand(Mockito.any(),Mockito.any());

        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, null, null, null, false));

        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, TEXT_VARIABLE_NAME, "SYSID", null, false));

        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, TEXT_VARIABLE_NAME, null, "TRAN", false));

        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, TEXT_VARIABLE_NAME, null, null, true));

        Mockito.doReturn(0).when(ceciSpy).defineVariableText(Mockito.any(),Mockito.any(),Mockito.any());
        Assert.assertEquals("Error in linkProgram() method", ceciResponseMock, ceciSpy.linkProgram(ceciTerminalMock, PROGRAM_NAME, "COMMAREA", null, null, false));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.linkProgram(wrongCeciTerminalMock, PROGRAM_NAME, null, null, null, false);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testLinkProgramWithChannel() throws Exception {
        Mockito.doReturn(ceciResponseMock).when(ceciSpy).issueCommand(Mockito.any(),Mockito.any());

        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, null, null, null, false));

        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, CHANNEL_NAME, "SYSID", null, false));

        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, CHANNEL_NAME, null, "TRAN", false));

        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, CHANNEL_NAME, null, null, true));

        Assert.assertEquals("Error in linkProgramWithChannel() method", ceciResponseMock, ceciSpy.linkProgramWithChannel(ceciTerminalMock, PROGRAM_NAME, "CHANNEL_NAME", null, null, false));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.linkProgramWithChannel(wrongCeciTerminalMock, PROGRAM_NAME, null, null, null, false);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testPutContainer() throws Exception {
        Mockito.doReturn(ceciResponseMock).when(ceciSpy).issueCommand(Mockito.any(),Mockito.any());
        Mockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy).setVariable(Mockito.any(),Mockito.any(),Mockito.any());

        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_VALUE, null, null, null));

        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, null, null));

        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, "CHAR", null, null));

        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, "CCID", null));

        Assert.assertEquals("Error in putContainer() method", ceciResponseMock, ceciSpy.putContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, null, "CODEPAGE"));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.putContainer(wrongCeciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_VALUE, null, null, null);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testGetContainer() throws Exception {
        Mockito.doReturn(ceciResponseMock).when(ceciSpy).issueCommand(Mockito.any(),Mockito.any());

        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, null));
        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, "CCID", null));
        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, "CODEPAGE"));
        Assert.assertEquals("Error in getContainer() method", ceciResponseMock, ceciSpy.getContainer(ceciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME.substring(1), null, null));

        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getContainer(wrongCeciTerminalMock, CHANNEL_NAME, CONTAINER_NAME, TEXT_VARIABLE_NAME, null, null);
        });
        Assert.assertEquals("exception should contain expected cause", WRONG_CICS_REGION, expectedException.getMessage());
    }

    @Test
    public void testInitialScreen() throws Exception {
        setupTestInitialScreen();

        Mockito.doReturn(true).when(ceciSpy).isCeciScreen();
        Mockito.doReturn(false).when(ceciSpy).isHelpScreen(Mockito.any());
        Assert.assertEquals("Error in initialScreen() method", ceciTerminalMock, ceciSpy.initialScreen());

        Mockito.doReturn(true).when(ceciSpy).isHelpScreen(Mockito.any());
        Assert.assertEquals("Error in initialScreen() method", ceciTerminalMock, ceciSpy.initialScreen());

        Mockito.when(ceciSpy.isCeciScreen()).thenReturn(false).thenReturn(true);
        Assert.assertEquals("Error in initialScreen() method", ceciTerminalMock, ceciSpy.initialScreen());

        Mockito.doReturn(false).when(ceciSpy).isInitialScreen(Mockito.any());
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	 ceciSpy.initialScreen();
        });
        Assert.assertEquals("exception should contain expected cause", "Unable to navigate to CECI initial screen", expectedException.getMessage());
    }

    @Test
    public void testInitialScreenException1() throws Exception {
        setupTestInitialScreen();

        Mockito.doReturn(false).when(ceciSpy).isCeciScreen();
        String expectedMessage = "Cannot identify terminal as CECI session";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.initialScreen();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testInitialScreenException2() throws Exception {
        setupTestInitialScreen();

        Mockito.doReturn(false).when(ceciSpy).isCeciScreen();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to navigate to CECI initial screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.initialScreen();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testInitialScreenException3() throws Exception {
        setupTestInitialScreen();

        Mockito.doReturn(false).when(ceciSpy).isCeciScreen();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to navigate to CECI initial screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.initialScreen();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    private void setupTestInitialScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(INITIAL_SCREEN_ID);
    }

    @Test
    public void testVariableScreen() throws Exception {
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).initialScreen();
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("").thenReturn(VAR_SCREEN_ID);
        Assert.assertEquals("Error in variableScreen() method", ceciTerminalMock, ceciSpy.variableScreen());

        Mockito.doReturn(false).when(ceciSpy).isVariablesScreen(Mockito.any());
        String expectedMessage = "Unable to navigate to CECI variables screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	 ceciSpy.variableScreen();
        });
		Assert.assertEquals("exception should contain expected cause", expectedMessage , expectedException.getMessage());
    }

    @Test
    public void testVariableScreenException1() throws Exception {
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).initialScreen();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to navigate to CECI variables screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.variableScreen();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testVariableScreenException2() throws Exception {
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).initialScreen();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to navigate to CECI variables screen";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.variableScreen();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testisCeciScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(true).when(ceciSpy).isInitialScreen(Mockito.any());
        Mockito.doReturn(true).when(ceciSpy).isHelpScreen(Mockito.any());
        Mockito.doReturn(true).when(ceciSpy).isCommandBeforeScreen(Mockito.any());
        Mockito.doReturn(true).when(ceciSpy).isCommandAfterScreen(Mockito.any());
        Mockito.doReturn(true).when(ceciSpy).isEibScreen(Mockito.any());
        Mockito.doReturn(true).when(ceciSpy).isVariablesScreen(Mockito.any());
        Mockito.doReturn(true).when(ceciSpy).isVariablesExpansionScreen(Mockito.any());
        Mockito.doReturn(true).when(ceciSpy).isMsgScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isInitialScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isHelpScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isCommandBeforeScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isCommandAfterScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isEibScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isVariablesScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isVariablesExpansionScreen(Mockito.any());
        Assert.assertTrue("Error in isCeciScreen() method", ceciSpy.isCeciScreen());

        Mockito.doReturn(false).when(ceciSpy).isMsgScreen(Mockito.any());
        Assert.assertFalse("Error in isCeciScreen() method", ceciSpy.isCeciScreen());
    }

    @Test
    public void testIsInitialScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isInitialScreen() method", ceciSpy.isInitialScreen(SPACES));
    }

    @Test
    public void testIsCommandBeforeScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isCommandBeforeScreen() method", ceciSpy.isCommandBeforeScreen(SPACES));
    }

    @Test
    public void testIsCommandAfterScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isCommandAfterScreen() method", ceciSpy.isCommandAfterScreen(SPACES));
    }

    @Test
    public void testIsHelpScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isHelpScreen() method", ceciSpy.isHelpScreen(SPACES));
    }

    @Test
    public void testIsEibScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isEibScreen() method", ceciSpy.isEibScreen(SPACES));
    }

    @Test
    public void testIsVariablesScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isVariablesScreen() method", ceciSpy.isVariablesScreen(SPACES));
    }

    @Test
    public void testIsMsgScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isMsgScreen() method", ceciSpy.isMsgScreen(SPACES));
    }

    @Test
    public void testIsVariablesExpansionScreen() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertFalse("Error in isVariablesExpansionScreen() method", ceciSpy.isVariablesExpansionScreen(SPACES));
    }

    @Test
    public void testMultipleTab() throws Exception {
        setTerminalMockOnCeciSpy();
        Assert.assertEquals("Error in multipleTab() method", ceciTerminalMock, ceciSpy.multipleTab(1));
    }

    @Test
    public void testCheckForSyntaxMessages() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(NO_SYNTAX_MESSAGES);

        ceciSpy.checkForSyntaxMessages();
        Mockito.verify(ceciTerminalMock,Mockito.times(1)).enter();
    }

    @Test
    public void testCheckForSyntaxMessagesException1() throws Exception {
        setTerminalMockOnCeciSpy();
        Field f1 = ceciSpy.getClass().getDeclaredField("command");
        f1.setAccessible(true);
        f1.set(ceciSpy,COMMAND_VALUE);

        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(SPACES);
        String expectedMessage = "Command failed syntax check. \nCommand:\n  " + COMMAND_VALUE + "\nSyntax Error Screen:\n" + SPACES;
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.checkForSyntaxMessages();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testCheckForSyntaxMessagesException2() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to check for syntax messages";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.checkForSyntaxMessages();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testCheckForSyntaxMessagesException3() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to check for syntax messages";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.checkForSyntaxMessages();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testValidateVariable() throws Exception {
        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray(), null));
        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME.substring(1), TEXT_VARIABLE_VALUE.toCharArray(), Integer.toString(TEXT_VARIABLE_VALUE.length())));
        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray(), null));
        Assert.assertEquals("Error in validateVariable() method", TEXT_VARIABLE_NAME, ceciSpy.validateVariable(TEXT_VARIABLE_NAME, null, null));

        String name = "&234567890";
        Assert.assertEquals("Error in validateVariable() method", name, ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null));

        name = "&azAZ09@#";
        Assert.assertEquals("Error in validateVariable() method", name, ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null));
    }

    @Test
    public void testValidateVariableException1() throws Exception {
        String name = "&2345678901";
        String expectedMessage = "CECI variable name \"" + name + "\" greater than maximum length of 10 characters including the leading \"&\"";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testValidateVariableException2() throws Exception {
        String name = "&?";
        String expectedMessage = "CECI variable name \"" + name + "\" invalid must. Must start with \"&\" and can contain one of more [a-zA-Z0-9@#]";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.validateVariable(name, TEXT_VARIABLE_VALUE.toCharArray(), null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testValidateVariableException3() throws Exception {
        String value = "123456789";
        String type = "H";
        int maxLength = 6;
        String expectedMessage = "CECI variable value length " + value.length() + " greater than maximum of " + maxLength +  " for type \"" + type + "\"" ;
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.validateVariable(TEXT_VARIABLE_NAME, value.toCharArray(), type);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testValidateVariableException4() throws Exception {
        String value = new String(new char[32768]).replace("\0", "X");
        String expectedMessage = "CECI variable value length " + value.length() + " greater than maximum 32767";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.validateVariable(TEXT_VARIABLE_NAME, value.toCharArray(), null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testSetVariable() throws Exception {
        setupTestVariable();
        Mockito.doReturn(TEXT_VARIABLE_VALUE.length()).when(ceciSpy).setVariableOnPage(Mockito.any(),Mockito.anyInt(),Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "X")).thenReturn(String.format("%-10s", " "));
        Assert.assertEquals("Error in setVariable() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null));

        Assert.assertEquals("Error in setVariable() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariable(TEXT_VARIABLE_NAME + "X", TEXT_VARIABLE_VALUE, null));

        Assert.assertEquals("Error in setVariable() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, "H"));

        int length = 1281;
        Mockito.when(ceciSpy.setVariableOnPage(Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(20).thenReturn(21);
        Assert.assertEquals("Error in setVariable() method", length, ceciSpy.setVariable(TEXT_VARIABLE_NAME, new String(new char[length]).replace("\0", "X"), null));
    }

    @Test
    public void testSetVariableException1() throws Exception {
        setupTestVariable();
        Mockito.doReturn("PF").when(ceciTerminalMock).retrieveFieldAtCursor();
        String expectedMessage = "No space on CECI variable screen for new variables";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testSetVariableException2() throws Exception {
        setupTestVariable();
        Mockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock).retrieveFieldAtCursor();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to set CECI variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testSetVariableException3() throws Exception {
        setupTestVariable();
        Mockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock).retrieveFieldAtCursor();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to set CECI variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariable(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE, null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    private void setupTestVariable() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doNothing().when(ceciSpy).deleteVariable(Mockito.any(),Mockito.any());
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).hexOff();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).variableScreen();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).moveToVariable(Mockito.any());
    }

    private void setTerminalMockOnCeciSpy() throws Exception{
        Field f1 = ceciSpy.getClass().getDeclaredField("terminal");
        f1.setAccessible(true);
        f1.set(ceciSpy,ceciTerminalMock);
    }

    @Test
    public void testSetVariableOnPage() throws Exception {
        setTerminalMockOnCeciSpy();

        String[] chunks = new String[] {TEXT_VARIABLE_VALUE, TEXT_VARIABLE_VALUE};
        int start = 0;
        int numberOfLines = 1;
        Assert.assertEquals("Error in setVariableOnPage() method", numberOfLines, ceciSpy.setVariableOnPage(chunks, start, numberOfLines));

        numberOfLines = 2;
        Assert.assertEquals("Error in setVariableOnPage() method", numberOfLines, ceciSpy.setVariableOnPage(chunks, start, numberOfLines));

        chunks = new String[] {String.format("%-65s", "X"), TEXT_VARIABLE_VALUE};
        Assert.assertEquals("Error in setVariableOnPage() method", numberOfLines, ceciSpy.setVariableOnPage(chunks, start, numberOfLines));
    }

    @Test
    public void testSetVariableOnPageException() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.type(Mockito.any())).thenThrow(new FieldNotFoundException());
        String expectedMessage = "Unable enter variable data";

        String[] chunks = new String[] {TEXT_VARIABLE_VALUE, TEXT_VARIABLE_VALUE};
        int start = 0;
        int numberOfLines = 1;
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariableOnPage(chunks, start, numberOfLines);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testSetVariableHex() throws Exception {
        setupTestVariable();
        Mockito.doReturn(TEXT_VARIABLE_NAME.length()).when(ceciSpy).setVariableHexOnPage(Mockito.any(),Mockito.anyInt(),Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "X")).thenReturn(String.format("%-10s", " "));
        Assert.assertEquals("Error in setVariableHex() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray()));

        Assert.assertEquals("Error in setVariableHex() method", TEXT_VARIABLE_VALUE.length(), ceciSpy.setVariableHex(TEXT_VARIABLE_NAME + "X", TEXT_VARIABLE_VALUE.toCharArray()));

        int length = 21;
        char[] value = new String(new char[length]).replace("\0", "X").toCharArray();
        Mockito.when(ceciSpy.setVariableHexOnPage(Mockito.any(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(20).thenReturn(21);
        Assert.assertEquals("Error in setVariableHex() method", length, ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, value));
    }

    @Test
    public void testSetVariableHexException1() throws Exception {
        setupTestVariable();
        Mockito.doReturn("PF").when(ceciTerminalMock).retrieveFieldAtCursor();
        String expectedMessage = "No space on CECI variable screen for new variables";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray());
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testSetVariableHexException2() throws Exception {
        setupTestVariable();
        Mockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock).retrieveFieldAtCursor();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to set CECI binary variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray());
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testSetVariableHexException3() throws Exception {
        setupTestVariable();
        Mockito.doReturn(String.format("%-10s", " ")).when(ceciTerminalMock).retrieveFieldAtCursor();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to set CECI binary variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariableHex(TEXT_VARIABLE_NAME, TEXT_VARIABLE_VALUE.toCharArray());
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testSetVariableHexOnPage() throws Exception {
        setTerminalMockOnCeciSpy();

        char[] value = TEXT_VARIABLE_VALUE.toCharArray();
        int start = 0;
        int numberOfLines = 1;
        Assert.assertEquals("Error in setVariableHexOnPage() method", value.length, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));

        value = new String(new char[32]).replace("\0", "X").toCharArray();
        numberOfLines = 2;
        Assert.assertEquals("Error in setVariableHexOnPage() method", value.length, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));

        numberOfLines = -1;
        Assert.assertEquals("Error in setVariableHexOnPage() method", 0, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));

        value = new char[0];
        numberOfLines = -1;
        Assert.assertEquals("Error in setVariableHexOnPage() method", value.length, ceciSpy.setVariableHexOnPage(value, start, numberOfLines));
    }

    @Test
    public void testSetVariableHexOnPageException() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.type(Mockito.any())).thenThrow(new FieldNotFoundException());
        String expectedMessage = "Unable enter variable data";

        char[] value = TEXT_VARIABLE_VALUE.toCharArray();
        int start = 0;
        int numberOfLines = 1;
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.setVariableHexOnPage(value, start, numberOfLines);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetVariable() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length()));
        Mockito.doReturn(TEXT_VARIABLE_VALUE).when(ceciSpy).getVariableFromPage(Mockito.anyInt(),Mockito.anyInt());

        Assert.assertEquals("Error in getVariable() method", TEXT_VARIABLE_VALUE, ceciSpy.getVariable(TEXT_VARIABLE_NAME, null));

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length()*2));
        Assert.assertEquals("Error in getVariable() method", TEXT_VARIABLE_VALUE + TEXT_VARIABLE_VALUE, ceciSpy.getVariable(TEXT_VARIABLE_NAME, null));

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("H");
        String value = String.format("%012d", 0);
        Mockito.doReturn(value).when(ceciSpy).getVariableFromPage(Mockito.anyInt(),Mockito.anyInt());
        Assert.assertEquals("Error in getVariable() method", value, ceciSpy.getVariable(TEXT_VARIABLE_NAME, "H"));
    }

    @Test
    public void testGetVariableException1() throws Exception {
        setupTestGetVariable();
        String lengthString = "XXXX";
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(lengthString);
        String expectedMessage = "Unexpected variable type \"" + lengthString  + "\" for \"" + TEXT_VARIABLE_NAME + "\"";
        String type = "H";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariable(TEXT_VARIABLE_NAME, type);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetVariableException2() throws Exception {
        setupTestGetVariable();
        String lengthString = "XXXX";
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(lengthString);
        String expectedMessage = "Unable to determine variable field length";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariable(TEXT_VARIABLE_NAME, null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetVariableException3() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to get CECI variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariable(TEXT_VARIABLE_NAME, null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetVariableException4() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to get CECI variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariable(TEXT_VARIABLE_NAME, null);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    private void setupTestGetVariable() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).moveToVariable(Mockito.any());
    }

    @Test
    public void getVariableFromPage() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(TEXT_VARIABLE_VALUE);

        Assert.assertEquals("Error in getVariableFromPage() method", TEXT_VARIABLE_VALUE, ceciSpy.getVariableFromPage(TEXT_VARIABLE_VALUE.length(), 1));
        Assert.assertEquals("Error in getVariableFromPage() method", TEXT_VARIABLE_VALUE, ceciSpy.getVariableFromPage(TEXT_VARIABLE_VALUE.length()*2, 1));

    }

    @Test
    public void getVariableFromPageException1() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(TEXT_VARIABLE_VALUE);
        Mockito.when(ceciTerminalMock.tab()).thenThrow(new FieldNotFoundException());
        String expectedMessage = "Unable to get variable from page";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariableFromPage(1, 1);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());

    }

    @Test
    public void testGetVariableHex() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length())).thenReturn(TEXT_VARIABLE_VALUE);
        Mockito.doReturn(TEXT_VARIABLE_VALUE).when(ceciSpy).getVariableHexFromPage(Mockito.anyInt(),Mockito.anyInt());

        Assert.assertTrue("Error in getVariableHex() method", Arrays.equals(TEXT_VARIABLE_VALUE.toCharArray(), ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE)));

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%+06d", TEXT_VARIABLE_VALUE.length()*2)).thenReturn(TEXT_VARIABLE_VALUE);
        Assert.assertTrue("Error in getVariableHex() method", Arrays.equals((TEXT_VARIABLE_VALUE + TEXT_VARIABLE_VALUE).toCharArray(), ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE)));
    }

    @Test
    public void testGetVariableHexException1() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("XXXX");
        String expectedMessage = "Unable to determine variable field length";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetVariableHexException2() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to get CECI binary variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetVariableHexException3() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to get CECI binary variable";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariableHex(TEXT_VARIABLE_VALUE);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetVariableHexPage() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("5A5A5A5A");

        Assert.assertEquals("Error in getVariableHexFromPage() method", "ZZZZ", ceciSpy.getVariableHexFromPage(4, 1));
        Assert.assertEquals("Error in getVariableHexFromPage() method", "ZZZZZZZZZZZZZZZZ", ceciSpy.getVariableHexFromPage(20, 1));
    }

    @Test
    public void testGetVariableHexPageException1() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("5A5A5A5A");
        Mockito.when(ceciTerminalMock.tab()).thenThrow(new FieldNotFoundException());
        String expectedMessage = "Unable to get binary variable from page";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getVariableHexFromPage(4, 1);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testMoveToVariable() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(TEXT_VARIABLE_NAME + " ");
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn(String.format("%-10s", "X")).thenReturn(String.format("%-10s", TEXT_VARIABLE_NAME));

        Assert.assertEquals("Error in moveToVariable() method", ceciTerminalMock, ceciSpy.moveToVariable(TEXT_VARIABLE_NAME));
    }

    @Test
    public void testMoveToVariableException1() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(" ");
        String expectedMessage = "Unable to find variable " + TEXT_VARIABLE_NAME;
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.moveToVariable(TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testMoveToVariableException2() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(TEXT_VARIABLE_NAME + " ");
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("PF");
        String expectedMessage = "Unable to find variable " + TEXT_VARIABLE_NAME;
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.moveToVariable(TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testMoveToVariableException3() throws Exception {
        setupMoveToVariable();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(TEXT_VARIABLE_NAME + " ");
        Mockito.when(ceciTerminalMock.newLine()).thenThrow(new FieldNotFoundException());
        String expectedMessage = "Problem serching for variable " + TEXT_VARIABLE_NAME;
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.moveToVariable(TEXT_VARIABLE_NAME);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    private void setupMoveToVariable() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).hexOff();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).variableScreen();
    }

    @Test
    public void testIsHexOn() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn(" EIBTIME      = X'00");
        Assert.assertTrue("Error in isHexOn() method", ceciSpy.isHexOn());
    }

    @Test
    public void testIsHexOnException1() throws Exception, CeciException, TimeoutException, KeyboardLockedException, TerminalInterruptedException {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to determine if CECI is in HEX mode";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.isHexOn();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testIsHexOnException2() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to determine if CECI is in HEX mode";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.isHexOn();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testHexOn() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(true).when(ceciSpy).isHexOn();

        Assert.assertEquals("Error in hexOn() method", ceciTerminalMock, ceciSpy.hexOn());

        Mockito.doReturn(false).when(ceciSpy).isHexOn();
        Assert.assertEquals("Error in hexOn() method", ceciTerminalMock, ceciSpy.hexOn());
    }

    @Test
    public void testHexOnException1() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(false).when(ceciSpy).isHexOn();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to set CECI HEX ON";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.hexOn();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testHexOnException2() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(false).when(ceciSpy).isHexOn();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to set CECI HEX ON";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.hexOn();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testHexOff() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(true).when(ceciSpy).isHexOn();

        Assert.assertEquals("Error in hexOff() method", ceciTerminalMock, ceciSpy.hexOff());

        Mockito.doReturn(false).when(ceciSpy).isHexOn();
        Assert.assertEquals("Error in hexOff() method", ceciTerminalMock, ceciSpy.hexOff());
    }

    @Test
    public void testHexOffException1() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(true).when(ceciSpy).isHexOn();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to set CECI HEX OFF";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.hexOff();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testHexOffException2() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(true).when(ceciSpy).isHexOn();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to set CECI HEX OFF";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.hexOff();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testNewCeciResponse() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("   RESPONSE: FILENOTFOUND          EIBRESP=+0000000012 EIBRESP2=+0000000001     ");
        Mockito.doReturn(new LinkedHashMap<>()).when(ceciSpy).parseResponseOutput();

        ICeciResponse ceciResponse = ceciSpy.newCeciResponse(false);

        ceciSpy.newCeciResponse(true);
        Assert.assertEquals("Error in newCeciResponse() method", "FILENOTFOUND", ceciResponse.getResponse());
        Assert.assertEquals("Error in newCeciResponse() method", 12, ceciResponse.getEIBRESP());
        Assert.assertEquals("Error in newCeciResponse() method", 1, ceciResponse.getEIBRESP2());
    }

    @Test
    public void testParseResponseOutput() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.doReturn(ceciTerminalMock).when(ceciSpy).multipleTab(Mockito.anyInt());
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION1").thenReturn("OPTION2");
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("OPTION= OPTION1      LENGTH= +00008 ").thenReturn("OPTION= OPTION2      LENGTH= +00008 ");
        Mockito.doReturn(new ResponseOutputValueImpl("XXXXXXXX")).when(ceciSpy).getOptionValue(Mockito.any());

        Assert.assertTrue("Error in parseResponseOutput() method", ceciSpy.parseResponseOutput().containsKey("OPTION1"));

        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION3").thenReturn("OPTION4").thenReturn("PF");
        Mockito.when(ceciTerminalMock.retrieveScreen()).thenReturn("OPTION= OPTION3      LENGTH= +00008 ").thenReturn("OPTION= OPTION4      LENGTH= +00008 ");
        Assert.assertTrue("Error in parseResponseOutput() method", ceciSpy.parseResponseOutput().containsKey("OPTION4"));
    }

    @Test
    public void testParseResponseOutputException1() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION1");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to parse command output";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.parseResponseOutput();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testParseResponseOutputException2() throws Exception {
        setTerminalMockOnCeciSpy();
        Mockito.when(ceciTerminalMock.retrieveFieldAtCursor()).thenReturn("OPTION1");
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to parse command output";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.parseResponseOutput();
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetOptionValue() throws Exception {
        setupTestGetVariable();
        String value = "000000";
        Mockito.doReturn(value).when(ceciSpy).getVariableFromPage(Mockito.anyInt(),Mockito.anyInt());
        String screen = "OPTION= LENGTH       LENGTH= H ";
        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValue(screen).getTextValue());

        Mockito.doReturn("000").when(ceciSpy).getVariableFromPage(Mockito.anyInt(),Mockito.anyInt());
        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValue(screen).getTextValue());

        screen = "OPTION= FROM         LENGTH= +00001 ";
        Mockito.doReturn("F1").when(ceciSpy).getOptionValueInHex(Mockito.anyInt(),Mockito.anyInt());
        Assert.assertTrue("Error in getOptionValue() method", Arrays.equals(new char[] {'F', '1'}, ceciSpy.getOptionValue(screen).getHexValue()));
    }

    @Test
    public void testGetOptionValueException1() throws Exception {
        setupTestGetVariable();
        Mockito.doReturn("000").when(ceciSpy).getVariableFromPage(Mockito.anyInt(),Mockito.anyInt());
        String screen = "OPTION= LENGTH       LENGTH= H ";
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to parse command output option value";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getOptionValue(screen);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetOptionValueException2() throws Exception {
        setupTestGetVariable();
        Mockito.doReturn("000").when(ceciSpy).getVariableFromPage(Mockito.anyInt(),Mockito.anyInt());
        String screen = "OPTION= LENGTH       LENGTH= H ";
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to parse command output option value";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getOptionValue(screen);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetOptionValueInHex() throws Exception {
        setupTestGetVariable();
        String value = "F1F1";
        Mockito.doReturn(value).when(ceciSpy).getVariableHexFromPage(Mockito.anyInt(),Mockito.anyInt());

        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValueInHex(1, 0));

        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValueInHex(1, 1));

        Assert.assertEquals("Error in getOptionValue() method", value, ceciSpy.getOptionValueInHex(2, 0));

        Assert.assertEquals("Error in getOptionValue() method", value + value, ceciSpy.getOptionValueInHex(5, 0));
    }

    @Test
    public void testGetOptionValueInHexException1() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TerminalInterruptedException());
        String expectedMessage = "Unable to parse command output binary option value";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getOptionValueInHex(1, 0);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }

    @Test
    public void testGetOptionValueInHexException2() throws Exception {
        setupTestGetVariable();
        Mockito.when(ceciTerminalMock.waitForKeyboard()).thenThrow(new TimeoutException());
        String expectedMessage = "Unable to parse command output binary option value";
        CeciException expectedException = Assert.assertThrows("expected exception should be thrown", CeciException.class, ()->{
        	ceciSpy.getOptionValueInHex(1, 0);
        });
        Assert.assertEquals("exception should contain expected cause", expectedMessage, expectedException.getMessage());
    }
}
