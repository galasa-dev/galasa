/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.resource.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.cicsts.cicsresource.CicsJvmserverResourceException;
import dev.galasa.cicsts.cicsresource.IJvmserverLog;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.ZosUNIXFileException;

public class JvmserverLogImpl implements IJvmserverLog, ITextScannable {
	
	private static final Log logger = LogFactory.getLog(JvmserverLogImpl.class);
    
	private IZosUNIXFile zosUnixFile;
    private IZosBatchJobOutputSpoolFile zosBatchJobOutputSpoolFile;
    private ILogScanner logScanner;
    private String scannableName;
    
    private static final String LOG_PROBLEM_SEARCHING_LOG = "Problem searching log for ";
    private static final String LOG_SINCE_CHECKPOINT = " since last checkpoint";

    public JvmserverLogImpl(IZosUNIXFile zosUnixFile, ILogScanner logScanner) throws CicsJvmserverResourceException {
        this.zosUnixFile = zosUnixFile;
        this.scannableName = this.zosUnixFile.getUnixPath();
        this.logScanner = logScanner;
        try {
            this.logScanner.setScannable(this);
        } catch (TextScanException e) {
            throw new CicsJvmserverResourceException("Unable to set scannable", e);
        }
    }

    public JvmserverLogImpl(IZosBatchJobOutputSpoolFile zosBatchJobOutputSpoolFile, ILogScanner logScanner) throws CicsJvmserverResourceException {
        this.zosBatchJobOutputSpoolFile = zosBatchJobOutputSpoolFile;
        this.scannableName = "//DD:" + this.zosBatchJobOutputSpoolFile.getDdname();
        this.logScanner = logScanner;
        try {
            this.logScanner.setScannable(this);
        } catch (TextScanException e) {
            throw new CicsJvmserverResourceException("Unable to set scannable", e);
        }
    }

    @Override
	public String getName() throws CicsJvmserverResourceException {
		try {
			if (isZosUNIXFile()) {
				return getZosUNIXFile().getFileName();
            } else if (isZosBatchJobSpoolFile()) {
                return getZosBatchJobOutputSpoolFile().getDdname();
            } else {
                throw new CicsJvmserverResourceException("Log is not a zOS UNIX File or zOS Batch Job spool file");
            }
		} catch (CicsJvmserverResourceException e) {
			throw new CicsJvmserverResourceException("Unable to get log name", e);
		}
	}

	@Override
    public boolean isZosUNIXFile() {
        return this.zosUnixFile != null;
    }

    @Override
    public boolean isZosBatchJobSpoolFile() {
        return this.zosBatchJobOutputSpoolFile != null;
    }

    @Override
    public IZosUNIXFile getZosUNIXFile() throws CicsJvmserverResourceException {
        return this.zosUnixFile;
    }

    @Override
    public IZosBatchJobOutputSpoolFile getZosBatchJobOutputSpoolFile() throws CicsJvmserverResourceException {
        return this.zosBatchJobOutputSpoolFile;
    }

    @Override
    public OutputStream retrieve() throws CicsJvmserverResourceException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            if (isZosUNIXFile()) {
                baos.write(this.zosUnixFile.retrieveAsText().getBytes());
            } else if (isZosBatchJobSpoolFile()) {
                baos.write(this.zosBatchJobOutputSpoolFile.getRecords().getBytes());
            } else {
                throw new CicsJvmserverResourceException("Log is not a zOS UNIX File or zOS Batch Job spool file");
            }
        } catch (ZosUNIXFileException | IOException e) {
            throw new CicsJvmserverResourceException("Problem retrieving content of log", e);
        }
        return baos;
    }
    
    @Override
    public void delete() throws CicsJvmserverResourceException {
        if (!isZosUNIXFile()) {
            throw new CicsJvmserverResourceException("Log is not a zOS UNIX File");
        }
        try {
            this.zosUnixFile.delete();
        } catch (ZosUNIXFileException e) {
            throw new CicsJvmserverResourceException("Unable to delete Log", e);
        }
    }

    @Override
    public void saveToResultsArchive(String rasPath) throws CicsJvmserverResourceException {
        try {
            if (isZosUNIXFile()) {
                this.zosUnixFile.saveToResultsArchive(rasPath);
            } else if (isZosBatchJobSpoolFile()) {
                this.getZosBatchJobOutputSpoolFile().saveToResultsArchive(rasPath);
            } else {
                throw new CicsJvmserverResourceException("Log is not a zOS UNIX File or zOS Batch Job spool file");
            }
        } catch (ZosUNIXFileException | ZosBatchException e) {
            throw new CicsJvmserverResourceException("Unable to store Log to RAS", e);
        }
    }

    @Override
    public long checkpoint() throws CicsJvmserverResourceException {
        try {
            if (isZosUNIXFile()) {
                this.logScanner.setCheckpoint(this.zosUnixFile.getSize());
            } else if (isZosBatchJobSpoolFile()) {
                this.logScanner.setCheckpoint(this.zosBatchJobOutputSpoolFile.getSize());
            } else {
                throw new CicsJvmserverResourceException("Log is not a zOS UNIX File or zOS Batch Job spool file");
            }
        } catch (TextScanException | ZosUNIXFileException | ZosBatchException e) {
            throw new CicsJvmserverResourceException("Unable to set checkpoint", e);
        }
        
        return getCheckpoint();
    }
    
    @Override
    public long getCheckpoint() {
        return this.logScanner.getCheckpoint();
    }

    @Override
    public OutputStream retrieveSinceCheckpoint() throws CicsJvmserverResourceException {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(((ByteArrayOutputStream) retrieve()).toByteArray());
            long checkpoint = getCheckpoint();
            if (checkpoint == -1) {
            	logger.warn("Log '" + this.scannableName + "' has not been checkpointed");
            } else {
	            long skipped = bais.skip(checkpoint);
	            if (skipped != getCheckpoint()) {
	                throw new IOException("Failed to skip " + checkpoint + " bytes. Actual bytes skipped " + skipped);
	            }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(bais, baos);
            return baos;
        } catch (IOException e) {
            throw new CicsJvmserverResourceException("Problem retrieving log since last checkpoint", e);
        }
    }

    @Override
    public String searchForText(String searchText) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatch(searchText, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG + searchText, e);
        }
    }

    @Override
    public String searchForText(String searchText, String failText) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatch(searchText, failText, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG + searchText, e);
        }
    }

    @Override
    public String searchForTextSinceCheckpoint(String searchText) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchText, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG + searchText + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String searchForTextSinceCheckpoint(String searchText, String failText) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchText, failText, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG + searchText + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String searchForPattern(Pattern searchPattern) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatch(searchPattern, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG, e);
        }
    }

    @Override
    public String searchForPattern(Pattern searchPattern, Pattern failPattern) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatch(searchPattern, failPattern, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG, e);
        }
    }

    @Override
    public String searchForPatternSinceCheckpoint(Pattern searchPattern) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchPattern, null, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG + searchPattern + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String searchForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern) throws CicsJvmserverResourceException {
        try {
            return this.logScanner.scanForMatchSinceCheckpoint(searchPattern, failPattern, 1);
        } catch (MissingTextException e) {
            return null;
        } catch (IncorrectOccurrencesException | TextScanException e) {
            throw new CicsJvmserverResourceException(LOG_PROBLEM_SEARCHING_LOG + searchPattern + LOG_SINCE_CHECKPOINT, e);
        }
    }

    @Override
    public String waitForText(String searchText, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForText(searchText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForText(String searchText, String failText, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForText(searchText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
        }
        return null;
    }

    @Override
    public String waitForTextSinceCheckpoint(String searchText, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForTextSinceCheckpoint(searchText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForTextSinceCheckpoint(String searchText, String failText, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForTextSinceCheckpoint(searchText, failText);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForPattern(Pattern searchPattern, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForPattern(searchPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
        }
        return null;
    }

    @Override
    public String waitForPattern(Pattern searchPattern, Pattern failPattern, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForPattern(searchPattern, failPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForPatternSinceCheckpoint(Pattern searchPattern, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForPatternSinceCheckpoint(searchPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
            }
        }
        return null;
    }

    @Override
    public String waitForPatternSinceCheckpoint(Pattern searchPattern, Pattern failPattern, long timeout) throws CicsJvmserverResourceException {
    	LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(timeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
            String returnText = searchForPatternSinceCheckpoint(searchPattern, failPattern);
            if (returnText != null && !returnText.isEmpty()) {
                return returnText;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new CicsJvmserverResourceException("Interrupted during wait", e);
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
        } catch (CicsJvmserverResourceException e) {
            throw new TextScanException("Problem retrieving " + getScannableName(), e);
        }
    }

    @Override
    public String getScannableString() throws TextScanException {
        try {
            return new String(((ByteArrayOutputStream) retrieve()).toByteArray());
        } catch (CicsJvmserverResourceException e) {
            throw new TextScanException("Problem retrieving " + getScannableName(), e);
        }
    }
    
    @Override
    public String toString() {
        if (isZosUNIXFile()) {
            return "[IZosUNIXFile] " + this.getScannableName();
        } else if (isZosBatchJobSpoolFile()) {
            return "[IZosBatchJobOutputSpoolFile] " + this.getScannableName();
        } else {
            return "[UNKNOWN_TYPE] " + this.getScannableName();
        }
    }
}
