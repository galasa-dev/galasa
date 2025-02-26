/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.textscan.TextScanManagerException;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos3270.AttentionIdentification;
import dev.galasa.zos3270.ErrorTextFoundException;
import dev.galasa.zos3270.FieldNotFoundException;
import dev.galasa.zos3270.IDatastreamListener;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.KeyboardLockedException;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.TimeoutException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;

public class Terminal implements ITerminal {

	public ITextScannerManagerSpi textScan;
    private final Screen  screen;
    private final Network network;
    private final String  id;
    private NetworkThread networkThread;
    private boolean connected = false;

    private int           defaultWaitTime = 120_000;

    private Log           logger          = LogFactory.getLog(getClass());
    
    private boolean       autoReconnect   = false;
    
    private List<String>  deviceTypes;

    /**
     * @deprecated use the {@link #Terminal(String id, String host, int port, boolean ssl, TerminalSize primarySize, TerminalSize alternateSize, ITextScannerManagerSpi textScan, Charset codePage)}
     * constructor instead.  
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Terminal(String id, String host, int port, ITextScannerManagerSpi textScan) throws TerminalInterruptedException {
        this(id, host, port, false, 80, 24, 0, 0, textScan);
    }

    /**
     * @deprecated use the {@link #Terminal(String id, String host, int port, boolean ssl, TerminalSize primarySize, TerminalSize alternateSize, ITextScannerManagerSpi textScan, Charset codePage)}
     * constructor instead.  
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Terminal(String id, String host, int port, boolean ssl, ITextScannerManagerSpi textScan) throws TerminalInterruptedException {
        this(id, host, port, ssl, 80, 24, 0, 0, textScan);
    }

    /**
     * @deprecated use the {@link #Terminal(String id, String host, int port, boolean ssl, TerminalSize primarySize, TerminalSize alternateSize, ITextScannerManagerSpi textScan, Charset codePage)}
     * constructor instead.  
     */
    @Deprecated(since = "0.28.0", forRemoval = true)
    public Terminal(String id, String host, int port, boolean ssl, int primaryColumns, int primaryRows, int alternateColumns, int alternateRows, ITextScannerManagerSpi textScan) throws TerminalInterruptedException {
        network = new Network(host, port, ssl, id);
        screen = new Screen(primaryColumns, primaryRows, alternateColumns, alternateRows, this.network);
        this.id = id;
        this.textScan = textScan;
    }


    public Terminal(String id, String host, int port, boolean ssl, TerminalSize primarySize, TerminalSize alternateSize, ITextScannerManagerSpi textScan, Charset codePage) throws TerminalInterruptedException {
        network = new Network(host, port, ssl, id);
        screen = new Screen(primarySize, alternateSize, this.network, codePage);
        this.id = id;
        this.textScan = textScan;
    }
    
    public void setAutoReconnect(boolean newAutoReconnect) {
        this.autoReconnect = newAutoReconnect;
    }
    
    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    @Override
    public synchronized void connect() throws NetworkException {
        connected = network.connectClient();
        networkThread = new NetworkThread(this, screen, network, network.getInputStream(), this.deviceTypes);
        networkThread.start();
        
        Instant expire = Instant.now().plus(60, ChronoUnit.SECONDS);
        boolean started = false;
        while(Instant.now().isBefore(expire)) {
            NetworkThread nThread = this.networkThread;
            if (nThread == null) {
                this.network.close();
                throw new NetworkException("The TN3270 network thread failed to start correctly");
            }
            if (nThread.isStarted()) {
                started = true;
                break;
            }
            
            try {
                Thread.sleep(50);
            } catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new NetworkException("Wait for TN3270 startup was interrupted", e);
            }
        }
        
        if (!started) {
            throw new NetworkException("TN3270 server did not start session in time");
        }
    }

    @Override
    public void disconnect() throws TerminalInterruptedException {
        boolean oldAutoReconnect = autoReconnect;
        autoReconnect = false;
        
        connected = false;
        if (network != null) {
            network.close();
        }
        if (networkThread != null) {
            try {
                networkThread.join();
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for network thread to end. Network thread might still be active.");
                throw new TerminalInterruptedException("Join of the network thread was interrupted",e);
            }
            networkThread = null;
        }
        
        autoReconnect = oldAutoReconnect;
    }
    
    public void networkClosed() {
        connected = false;
        if (network != null) {
            network.close();
        }
        networkThread = null;
        
        if (autoReconnect) {
            try {
                connect();
            } catch (NetworkException e) {
                logger.error("Auto reconnect failed",e);
            }
        }
    }
    
    @Override
    public boolean isConnected() {
        if (!connected) {
            return false;
        }
        
        if (network == null) {
            return false;
        }
        
        return network.isConnected();
    }

    @Override
    public ITerminal waitForKeyboard() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException {
        logger.trace("Waiting for keyboard");
        screen.waitForKeyboard(defaultWaitTime);
        logger.trace("Wait for keyboard complete");
        return this;
    }

    @Override
    public ITerminal wfk() throws TimeoutException, KeyboardLockedException, TerminalInterruptedException {
        return waitForKeyboard();
    }

    public Screen getScreen() {
        return this.screen;
    }

    @Override
    public ITerminal positionCursorToFieldContaining(@NotNull String text)
            throws TextNotFoundException, KeyboardLockedException {
        screen.positionCursorToFieldContaining(text);
        return this;
    }

    @Override
    public Terminal verifyTextInField(String text) throws TextNotFoundException {
        screen.searchFieldContaining(text);
        return this;
    }

    @Override
    public boolean isTextInField(String text) {
        return screen.isTextInField(text);
    }
    
    @Override
    public boolean isTextInField(String text, long timeoutInMilliseconds) throws TerminalInterruptedException {
        if (isTextInField(text)) {
            return true;
        }
        
        Instant expire = Instant.now().plus(timeoutInMilliseconds, ChronoUnit.MILLIS);
        while(expire.isAfter(Instant.now())) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TerminalInterruptedException("Wait for text was interrupted",e);
            }
            
            if (isTextInField(text)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean searchText(String text) {
    	return this.searchText(text, 1, this.defaultWaitTime);
    }
    
    @Override
    public boolean searchText(String text, int occurrences) {
    	return this.searchText(text, occurrences, this.defaultWaitTime);
    }
    
    @Override
    public boolean searchText(String text, long milliTimeout) {
    	return this.searchText(text, 1, milliTimeout);
    }
    
    @Override
    public boolean searchText(String text, int occurrences, long milliTimeout) {
    	    	
    	long startTime = System.currentTimeMillis();
    	
    	logger.info("Searching for " + occurrences + " counts of '" + text + "' on terminal screen over " + milliTimeout + "ms");
    	
    	do {
    		try {
    			// Scan the terminal screen
    			textScan.getTextScanner().scan(retrieveScreen(), text, null, occurrences);
    			
    		} catch (TextScanManagerException e) {
    			    			
    			try {
        			Thread.sleep(2000);
    			} catch (InterruptedException interEx) {}
    			
       			// Exception has occurred so text was not found    
    			continue;
    		}
    		
    		// At this point, the text must have been found as an exception wasn't thrown
    		logger.info("Found " + occurrences + " counts of '" + text + "' on terminal screen");
    		return true;
    		
		} while ((System.currentTimeMillis() - startTime) <= milliTimeout);
    	
    	logger.info("Did not find " + occurrences + " counts of '" + text + "' on terminal screen");
    	
    	return false;
    }
    
    @Override
    public ITerminal waitForTextInField(String text) throws TerminalInterruptedException, TextNotFoundException, Zos3270Exception {
        screen.waitForTextInField(text, defaultWaitTime);
        return this;
    }

    @Override
    public int waitForTextInField(String[] ok, String[] error)
            throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception {
        return waitForTextInField(ok, error, this.defaultWaitTime);
    }

    @Override
    public int waitForTextInField(String[] ok, String[] error, long timeoutInMilliseconds)
            throws TerminalInterruptedException, TextNotFoundException, ErrorTextFoundException, Zos3270Exception {
        return screen.waitForTextInField(ok, error, timeoutInMilliseconds);
    }

    @Override
    public ITerminal type(String text) throws KeyboardLockedException, FieldNotFoundException {
        screen.type(text);
        return this;
    }

    @Override
    public ITerminal eraseEof() throws KeyboardLockedException, FieldNotFoundException {
        screen.eraseEof();
        return this;
    }

    @Override
    public ITerminal eraseInput() throws KeyboardLockedException, FieldNotFoundException {
        screen.eraseInput();
        return this;
    }

    @Override
    public ITerminal tab() throws KeyboardLockedException, FieldNotFoundException {
        screen.tab();
        return this;
    }

    @Override
    public ITerminal backTab() throws KeyboardLockedException, FieldNotFoundException {
        screen.backTab();
        return this;
    }

    @Override
    public ITerminal cursorUp() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorUp();
        return this;
    }

    @Override
    public ITerminal cursorDown() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorDown();
        return this;
    }

    @Override
    public ITerminal cursorLeft() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorLeft();
        return this;
    }

    @Override
    public ITerminal cursorRight() throws KeyboardLockedException, FieldNotFoundException {
        screen.cursorRight();
        return this;
    }

    @Override
    public ITerminal home() throws KeyboardLockedException, FieldNotFoundException {
        screen.home();
        return this;
    }

    @Override
    public ITerminal newLine() throws KeyboardLockedException, FieldNotFoundException {
        screen.newLine();
        return this;
    }

    @Override
    public ITerminal backSpace() throws KeyboardLockedException, FieldNotFoundException {
        screen.backSpace();
        return this;
    }

    @Override
    public ITerminal enter() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.ENTER));
        return this;
    }

    @Override
    public ITerminal clear() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.CLEAR));
        return this;
    }

    @Override
    public ITerminal pf1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF1));
        return this;
    }

    @Override
    public ITerminal pf2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF2));
        return this;
    }

    @Override
    public ITerminal pf3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF3));
        return this;
    }

    @Override
    public ITerminal pf4() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF4));
        return this;
    }

    @Override
    public ITerminal pf5() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF5));
        return this;
    }

    @Override
    public ITerminal pf6() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF6));
        return this;
    }

    @Override
    public ITerminal pf7() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF7));
        return this;
    }

    @Override
    public ITerminal pf8() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF8));
        return this;
    }

    @Override
    public ITerminal pf9() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF9));
        return this;
    }

    @Override
    public ITerminal pf10() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF10));
        return this;
    }

    @Override
    public ITerminal pf11() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF11));
        return this;
    }

    @Override
    public ITerminal pf12() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF12));
        return this;
    }

    @Override
    public ITerminal pf13() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF13));
        return this;
    }

    @Override
    public ITerminal pf14() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF14));
        return this;
    }

    @Override
    public ITerminal pf15() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF15));
        return this;
    }

    @Override
    public ITerminal pf16() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF16));
        return this;
    }

    @Override
    public ITerminal pf17() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF17));
        return this;
    }

    @Override
    public ITerminal pf18() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF18));
        return this;
    }

    @Override
    public ITerminal pf19() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF19));
        return this;
    }

    @Override
    public ITerminal pf20() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF20));
        return this;
    }

    @Override
    public ITerminal pf21() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF21));
        return this;
    }

    @Override
    public ITerminal pf22() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF22));
        return this;
    }

    @Override
    public ITerminal pf23() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF23));
        return this;
    }

    @Override
    public ITerminal pf24() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PF24));
        return this;
    }

    @Override
    public ITerminal pa1() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA1));
        return this;
    }

    @Override
    public ITerminal pa2() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA2));
        return this;
    }

    @Override
    public ITerminal pa3() throws KeyboardLockedException, NetworkException, TerminalInterruptedException {
        network.sendDatastream(screen.aid(AttentionIdentification.PA3));
        return this;
    }

    @Override
    public ITerminal reportScreen() {
        logger.info("\n" + screen.printScreen());
        return this;
    }

    @Override
    public ITerminal reportScreenWithCursor() {
        logger.info("\n" + screen.printScreenTextWithCursor());
        return this;
    }
    
    @Override
    public ITerminal reportExtendedScreen(boolean printCursor, boolean printColour, boolean printHighlight, boolean printIntensity, boolean printProtected, boolean printNumeric, boolean printModified) throws Zos3270Exception {
        logger.info("\n" + screen.printExtendedScreen(printCursor, printColour, printHighlight, printIntensity, printProtected, printNumeric, printModified));
        return this;
    }

    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Returns the screen print out as a String. For use in edge testing cases.
     * 
     * @return Screen as String
     */
    public String retrieveScreen() {
        return screen.printScreen();
    }

    /**
     * Return a String of the contents of the current Field. Current field is the
     * one which the cursor is at.
     * 
     * @return Current Field as String
     */
    public String retrieveFieldAtCursor() {
        int cursorPos = screen.getCursor();
        Field cursorField = screen.locateFieldAt(cursorPos);
        return cursorField.getFieldWithoutNulls();
    }

    /**
     * Returns a String of text in a Field with a given name.
     * 
     * @param fieldName Name of the field to be extracted from
     * @return String which has been extracted from the field
     * @throws TextNotFoundException
     */
    public String retrieveFieldTextAfterFieldWithString(String fieldName) throws TextNotFoundException {
        return screen.getValueFromFieldContaining(fieldName);
    }

    public String getHostPort() {
        return this.network.getHostPort();
    }

    @Override
    public void registerDatastreamListener(IDatastreamListener listener) {
        this.screen.registerDatastreamListener(listener);
    }

    @Override
    public void unregisterDatastreamListener(IDatastreamListener listener) {
        this.screen.unregisterDatastreamListener(listener);
    }

    @Override
    public boolean isSwitchedSSL() {
        return this.network.isSwitchedSSL();
    }
    
    @Override
    public void setDoStartTls(boolean doStartTls) {
        this.network.setDoStartTls(doStartTls);
    }

    @Override
    public boolean isClearScreen() {
        return this.screen.isClearScreen();
    }

	@Override
	public void setCursorPosition(int row, int col) throws KeyboardLockedException, Zos3270Exception {
		checkCursorPosition(row, col, 0 /* not worried about length */);
		
		row--;
		col--;
		screen.setCursorPosition(col, row);
	}

	@Override
	public String retrieveText(int row, int col, int length) throws Zos3270Exception {
		checkCursorPosition(row, col, length);
		
		row--;
		col--;
		
		String textScreen = screen.retrieveFlatScreen();

		int startPos = (row * screen.getNoOfColumns()) + col;
		int endPos = startPos + (length - 1);
		
		return textScreen.substring(startPos, endPos);
	}

	@Override
	public String retrieveTextAtCursor(int length) throws Zos3270Exception {
		
		int pos = screen.getCursor();
		if ((pos + length) > this.screen.getScreenSize()) {
			throw new Zos3270Exception("Invalid length, it would exceed the screen buffer");
		}
		
		String textScreen = screen.retrieveFlatScreen();
		
		int endPos = pos + (length - 1);
		
		return textScreen.substring(pos, endPos);
	}
	
	private void checkCursorPosition(int row, int col, int length) throws Zos3270Exception {
		if (row < 1 || col < 1) {
			throw new Zos3270Exception("Invalid cursor position, row and col are index based 1");
		}
		
		int rows = screen.getNoOfRows();
		if (row > rows) {
			throw new Zos3270Exception("Invalid cursor position, row exceeds number of rows (" + rows + ")");
		}
		
		int cols = screen.getNoOfColumns();
		if (col > cols) {
			throw new Zos3270Exception("Invalid cursor position, col exceeds number of columns (" + cols + ")");
		}
		
		row--;
		col--;
		
		int pos = (row * screen.getNoOfColumns()) + col;
		
		if ((pos + length) > this.screen.getScreenSize()) {
			throw new Zos3270Exception("Invalid length, it would exceed the screen buffer");
		}
		
	}

    @Override
    public Colour retrieveColourAtCursor() {
        int pos = screen.getCursor();
        return screen.getColourAtPosition(pos);
    }

    @Override
    public Colour retrieveColourAtPosition(int row, int col) throws Zos3270Exception {
        checkCursorPosition(row, col, 0 /* not worried about length */);
        
        row--;
        col--;
        int pos = (row * screen.getNoOfColumns()) + col;
        
        return screen.getColourAtPosition(pos);
    }

    @Override
    public Highlight retrieveHighlightAtCursor() {
        int pos = screen.getCursor();
        return screen.getHighlightAtPosition(pos);
    }

    @Override
    public Highlight retrieveHighlightAtPosition(int row, int col) throws Zos3270Exception {
        checkCursorPosition(row, col, 0 /* not worried about length */);
        
        row--;
        col--;
        
        int pos = (row * screen.getNoOfColumns()) + col;
        
        return screen.getHighlightAtPosition(pos);
    }


}
