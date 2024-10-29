/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosliberty.IZosLibertyServerLog;
import dev.galasa.zosliberty.ZosLibertyServerException;

public class ZosLibertyServerLogImpl implements IZosLibertyServerLog, ITextScannable {
    
    private static final Log logger = LogFactory.getLog(ZosLibertyServerLogImpl.class);
    
    private IZosUNIXFile zosUnixFile;
    private ILogScanner logScanner;
    private String scannableName;
    
    private static final String LOG_PROBLEM_SEARCHING_LOG = "Problem searching log for ";
    private static final String LOG_SINCE_CHECKPOINT = " since last checkpoint";

    public ZosLibertyServerLogImpl(IZosUNIXFile zosUnixFile, ILogScanner logScanner) throws ZosLibertyServerException {
        this.zosUnixFile = zosUnixFile;
        this.scannableName = this.zosUnixFile.getUnixPath();
        this.logScanner = logScanner;
        try {
            this.logScanner.setScannable(this);
        } catch (TextScanException e) {
            throw new ZosLibertyServerException("Unable to set scannable", e);
        }
    }

    @Override
    public String getName() throws ZosLibertyServerException {
        return getZosUNIXFile().getFileName();
    }

    @Override
    public IZosUNIXFile getZosUNIXFile() throws ZosLibertyServerException {
        return this.zosUnixFile;
    }

    @Override
    public OutputStream retrieve() throws ZosLibertyServerException {
        try {
            if (checkExists()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(this.zosUnixFile.retrieveAsBinary());
                return baos;
            } else {
                return new ByteArrayOutputStream();
            }
        } catch (ZosUNIXFileException | IOException e) {
            throw new ZosLibertyServerException("Problem retrieving content of log", e);
        }
    }
    
    @Override
    public void delete() throws ZosLibertyServerException {
        try {
            if (checkExists()) {
                this.zosUnixFile.delete();
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to delete Log", e);
        }
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws ZosLibertyServerException {
        try {
            if (checkExists()) {
                this.zosUnixFile.saveToResultsArchive(rasPath);
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to store Log to RAS", e);
        }
    }

    @Override
    public long checkpoint() throws ZosLibertyServerException {
        try {
            if (checkExists()) {
                this.logScanner.setCheckpoint(this.zosUnixFile.getSize());
            } else {
                this.logScanner.setCheckpoint(-1);
            }
        } catch (TextScanException | ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Unable to set checkpoint", e);
        }
        
        return getCheckpoint();
    }
    
    @Override
    public long getCheckpoint() {
        return this.logScanner.getCheckpoint();
    }

    @Override
    public OutputStream retrieveSinceCheckpoint() throws ZosLibertyServerException {
        try {
            if (checkExists()) {
                ByteArrayInputStream bais = new ByteArrayInputStream(((ByteArrayOutputStream) retrieve()).toByteArray());
                long checkpoint = getCheckpoint();
                long skipped = bais.skip(checkpoint);
                if (skipped != getCheckpoint()) {
                    throw new IOException("Failed to skip " + checkpoint + " bytes. Actual bytes skipped " + skipped);
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.writeTo(baos);
                return baos;
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new ZosLibertyServerException("Problem retrieving log since last checkpoint", e);
        }
    }

    @Override
    public String searchForText(String searchText) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatch(searchText, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG + searchText, e);
        }
    }

    @Override
    public String searchForText(String searchText, String failText) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatch(searchText, failText, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG + searchText, e);
        }
    }

    @Override
    public String searchForTextSinceCheckpoint(String searchText) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchText, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG + searchText + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String searchForTextSinceCheckpoint(String searchText, String failText) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchText, failText, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG + searchText + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String searchForPattern(Pattern searchPattern) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatch(searchPattern, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG, e);
        }
    }

    @Override
    public String searchForPattern(Pattern searchPattern, Pattern failPattern) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatch(searchPattern, failPattern, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG, e);
        }
    }

    @Override
    public String searchForPatternSinceCheckpoint(Pattern searchPattern) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchPattern, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG + searchPattern + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String searchForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern) throws ZosLibertyServerException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchPattern, failPattern, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new ZosLibertyServerException(LOG_PROBLEM_SEARCHING_LOG + searchPattern + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String waitForText(String searchText, long millisecondTimeout) throws ZosLibertyServerException {
        long timeout = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeout) {
            String returnText = searchForText(searchText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForText(String searchText, String failText, long millisecondTimeout) throws ZosLibertyServerException {
        long timeout = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeout) {
            String returnText = searchForText(searchText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForTextSinceCheckpoint(String searchText, long millisecondTimeout) throws ZosLibertyServerException {
        long timeout = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeout) { 
            String returnText = searchForTextSinceCheckpoint(searchText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForTextSinceCheckpoint(String searchText, String failText, long millisecondTimeout) throws ZosLibertyServerException {
        long timeoutTimeInMilliseconds = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeoutTimeInMilliseconds) { 
            String returnText = searchForTextSinceCheckpoint(searchText, failText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
        }
        return null;
    }

    @Override
    public String waitForPattern(Pattern searchPattern, long millisecondTimeout) throws ZosLibertyServerException {
        long timeout = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeout) {
            String returnText = searchForPattern(searchPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForPattern(Pattern searchPattern, Pattern failPattern, long millisecondTimeout) throws ZosLibertyServerException {
        long timeout = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeout) {
            String returnText = searchForPattern(searchPattern, failPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForPatternSinceCheckpoint(Pattern searchPattern, long millisecondTimeout) throws ZosLibertyServerException {
        long timeout = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeout) {
            String returnText = searchForPatternSinceCheckpoint(searchPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern, long millisecondTimeout) throws ZosLibertyServerException {
        long timeout = Calendar.getInstance().getTimeInMillis() + millisecondTimeout;
        while(Calendar.getInstance().getTimeInMillis() < timeout) {
            String returnText = searchForPatternSinceCheckpoint(searchPattern, failPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new ZosLibertyServerException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public boolean isScannableInputStream() {
        return false;
    }

    @Override
    public boolean isScannableString() {
        return true;
    }

    @Override
    public String getScannableName() {
        return this.scannableName;
    }

    @Override
    public ITextScannable updateScannable() throws TextScanException {
        return this;
    }

    @Override
    public InputStream getScannableInputStream() throws TextScanException {
        try {
            return new ByteArrayInputStream(((ByteArrayOutputStream) retrieve()).toByteArray());
        } catch (ZosLibertyServerException e) {
            throw new TextScanException("Problem retrieving " + getScannableName(), e);
        }
    }

    @Override
    public String getScannableString() throws TextScanException {
        try {
            return new String(((ByteArrayOutputStream) retrieve()).toByteArray());
        } catch (ZosLibertyServerException e) {
            throw new TextScanException("Problem retrieving " + getScannableName(), e);
        }
    }
    
    @Override
    public String toString() {
        return "[IZosUNIXFile] " + this.getScannableName();
    }

    private boolean checkExists() throws ZosLibertyServerException {
        try {
            if (this.getZosUNIXFile().exists()) {
                return true;
            }
        } catch (ZosUNIXFileException e) {
            throw new ZosLibertyServerException("Problem checking log " + getName(), e);
        }
        logger.warn("File " + getName() + " does not exist");
        return false;
    }
}
