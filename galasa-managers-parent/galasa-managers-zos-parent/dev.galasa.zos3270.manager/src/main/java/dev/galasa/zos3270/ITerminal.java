/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

import javax.validation.constraints.NotNull;

import dev.galasa.zos3270.spi.Colour;
import dev.galasa.zos3270.spi.Highlight;
import dev.galasa.zos3270.spi.NetworkException;

public interface ITerminal {

    boolean isConnected();

    void connect() throws NetworkException;

    void disconnect() throws TerminalInterruptedException;
    
    public boolean isSwitchedSSL();

    public void setDoStartTls(boolean doStartTls);

    ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException;

    ITerminal wfk() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException;

    ITerminal positionCursorToFieldContaining(@NotNull String searchText)
            throws TextNotFoundException, KeyboardLockedException;
    
    boolean isClearScreen();
    
    /** 
     * Perform a search on a String to check whether or not it is on the terminal screen.
     * If after a timeout the String is not found on the terminal with at least the amount of occurrences input
     * the method will return false to the caller.
     * 
     * @param text
     * @param occurrences
     * @param milliTimeout
     * @return if the text was found
     */
    boolean searchText(String text, int occurrences, long milliTimeout);
    
    /**
     * Perform a search on a String to check whether or not it is on the terminal screen.
     * If after a timeout the String is not found on the terminal, the method will return false to the caller.
     * 
     * @param text
     * @param milliTimeout
     * @return if the text was found
     */
    boolean searchText(String text, long milliTimeout);

    /**
     * Perform a search on a String to check whether or not it is on the terminal screen.
     * If after a timeout the String is not found on the terminal, the method will return false to the caller.
     * The returned boolean will depend on if the amount of occurrences is found.
     * 
     * @param text
     * @param occurrences
     * @return if the text was found
     */
    boolean searchText(String text, int occurrences);
    
    /**
     * Perform a search on a String to check whether or not it is on the terminal screen.
     * If after a timeout the String is not found on the terminal, the method will return false to the caller.
     * 
     * @param text
     * @return if the text was found
     */
    boolean searchText(String text);

    /**
     * @param string
     * @return
     * @throws TerminalInterruptedException
     * @throws TextNotFoundException
     * @throws ErrorTextFoundException
     * @throws Zos3270Exception
     */
    ITerminal waitForTextInField(String string) throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception;

    /**
     * @param ok - An array of text strings to find on the screen
     * @param error - An array of text strings deemed to be errors
     * @return the index of the ok string that was found
     * @throws TerminalInterruptedException - If the wait was interrupted for some reason
     * @throws TextNotFoundException - None of the ok or error strings were found before the timeout
     * @throws ErrorTextFoundException - One of the error strings were found, index of which is in the exception
     * @throws Zos3270Exception - general zos 3270 error
     */
    int waitForTextInField(String[] ok, String[] error) throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception;

    /**
     * @param ok - An array of text strings to find on the screen
     * @param error - An array of text strings deemed to be errors
     * @param timeoutInMilliseconds - timeout
     * @return the index of the ok string that was found
     * @throws TerminalInterruptedException - If the wait was interrupted for some reason
     * @throws TextNotFoundException - None of the ok or error strings were found before the timeout
     * @throws ErrorTextFoundException - One of the error strings were found
     * @throws Zos3270Exception - general zos 3270 error
     */
    int waitForTextInField(String[] ok, String[] error, long timeoutInMilliseconds) throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception;

    ITerminal verifyTextInField(String string) throws TextNotFoundException;

    boolean isTextInField(String string);

    boolean isTextInField(String string, long timeoutInMilliseconds) throws TerminalInterruptedException;

    ITerminal type(String typeText) throws FieldNotFoundException, KeyboardLockedException;
    
    ITerminal eraseEof() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal eraseInput() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal tab() throws FieldNotFoundException, KeyboardLockedException;
    
    ITerminal backTab() throws FieldNotFoundException, KeyboardLockedException;
    
    ITerminal cursorUp() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal cursorDown() throws KeyboardLockedException, FieldNotFoundException;
    
    ITerminal cursorLeft() throws KeyboardLockedException, FieldNotFoundException;
    
    ITerminal cursorRight() throws KeyboardLockedException, FieldNotFoundException;
    
    ITerminal backSpace() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal home() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal newLine() throws KeyboardLockedException, FieldNotFoundException;

    ITerminal enter() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal clear() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf4() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf5() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf6() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf7() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf8() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf9() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf10() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf11() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf12() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf13() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf14() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf15() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf16() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf17() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf18() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf19() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf20() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf21() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf22() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf23() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pf24() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pa1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pa2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    ITerminal pa3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException;

    /**
     * Temporary Print to console
     * 
     * @return ITerminal for chaining
     */
    ITerminal reportScreen();

    ITerminal reportScreenWithCursor();

    /**
     * Report to the log the current state of the terminal with optional extended datastream settings
     * 
     * @param printCursor - report cursor position
     * @param printColour - report the colour
     * @param printHighlight - report highlighting
     * @param printIntensity - report intensity
     * @param printProtected - report field protection
     * @param printNumeric - report numeric restrictions
     * @param printModified - report field modification
     * @return the ITerminal for fluent API
     * @throws Zos3270Exception 
     */
    ITerminal reportExtendedScreen(boolean printCursor, boolean printColour, boolean printHighlight,
            boolean printIntensity, boolean printProtected, boolean printNumeric, boolean printModified) throws Zos3270Exception;
    
    String retrieveScreen();

    /**
     * Retrieve the contents of the field under the current cursor position.   If the field contains nulls, the string returned will 
     * be compressed as it would be when sent back to the server.
     * 
     * @return The contents of the screen in the field
     */
    String retrieveFieldAtCursor();

    /**
     * Retrieve the contents of the field which is immediately after a field containing the string provided.  Used to obtain the contents of a labelled field.  
     * If the field contains nulls, the string returned will be compressed as it would be when sent back to the server.
     * 
     * @return The contents of the screen in the field
     */
    String retrieveFieldTextAfterFieldWithString(String string) throws TextNotFoundException;
    
    String getId();
    
    void registerDatastreamListener(IDatastreamListener listener);
    void unregisterDatastreamListener(IDatastreamListener listener);
    
    /**
     * Set the position of the Cursor
     * 
     * @param row - The row on the screen to set the cursor
     * @param col - the column on the screen to set the cursor
     * @throws KeyboardLockedException - If an attempt is made to move the cursor whilst the screen is locked
     * @throws Zos3270Exception - If the position exceeds the boundaries of the screen
     */
    void setCursorPosition(int row, int col) throws KeyboardLockedException, Zos3270Exception;    
   
    /**
     * Retrieve text from the screen.   Null characters and field attribute positions are replaced with spaces.
     * If there are not enough characters on the row to satisfy the length requirement,  the retrieve will wrap to the next 
     * row, unless it is the last row, in which case an exception will be thrown. 
     * 
     * @param row - The row on the screen to start the extract
     * @param col - The column on the screen to start the extract
     * @param length - The number of characters to extract
     * @return The contents extracted
     * @throws Zos3270Exception - If the position exceeds the boundaries of the screen or the length causes the extract to overflow the end of the screen buffer
     */
    String retrieveText(int row, int col, int length) throws Zos3270Exception;
    
    /**
     * Retrieve text from the screen.   Null characters and field attribute positions are replaced with spaces.
     * If there are not enough characters on the row to satisfy the length requirement,  the retrieve will wrap to the next 
     * row, unless it is the last row, in which case an exception will be thrown. 
     *
     * @param length - The number of characters to extract
     * @return The contents extracted
     * @throws Zos3270Exception - If the length causes the extract to overflow the end of the screen buffer
     */
    String retrieveTextAtCursor(int length) throws Zos3270Exception;
    
    /**
     * Return the colour of the character at the cursor position
     * 
     * @return - if position is not in an extended field,  null us returned
     */
    Colour retrieveColourAtCursor();
    
    /**
     * Return the colour of the character at the cursor position
     * 
     * @param row of the screen, index 1 based
     * @param col of the screen, index 1 based
     * @return - if position is not in an extended field,  null us returned
     * @throws Zos3270Exception 
     */
    Colour retrieveColourAtPosition(int row, int col) throws Zos3270Exception;
    
    /**
     * Return the highlighting of the character at the cursor position
     * 
     * @return - if position is not in an extended field,  null us returned
     */
    Highlight retrieveHighlightAtCursor();
    
    /**
     * Return the highlighting of the character at the cursor position
     * 
     * @param row of the screen, index 1 based
     * @param col of the screen, index 1 based
     * @return - if position is not in an extended field,  null us returned
     * @throws Zos3270Exception 
     */
    Highlight retrieveHighlightAtPosition(int row, int col) throws Zos3270Exception;
    
    

}
