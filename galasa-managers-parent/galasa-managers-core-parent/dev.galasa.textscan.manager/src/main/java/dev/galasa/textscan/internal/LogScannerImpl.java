/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ILogScanner;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;

public class LogScannerImpl implements ILogScanner {
	
	private static final Log logger = LogFactory.getLog(LogScannerImpl.class);

	private static final String MESSAGE_PROBLEM_SCANNING = "Problem scanning '";

	private static final String MESSAGE_UNKNOWN_SCANNABLE_TYPE = "Unknown scannable type";

	private static final String QUOTE = "'";
	
	private ITextScanner textScanner = new TextScannerImpl();
    private ITextScannable scannable;
	private String scannableName;
	protected long checkpoint = -1;

	@Override
    public ILogScanner setScannable(ITextScannable scannable) throws TextScanException {
		setInternalScannable(scannable);
        return this;
    }
	
    @Override
    public ILogScanner updateScannable() throws TextScanException {
    	checkScannableNoNull();
		setScannable(this.scannable);
        return this;
    }

    @Override
    public ILogScanner reset() {
    	this.scannable = null;
    	this.scannableName = null;
    	
        return this;
    }

    @Override
    public ILogScanner checkpoint() throws TextScanException {
    	checkScannableNoNull();
    	if (this.scannable.isScannableInputStream()) {
    		try {
				byte[] bytes = IOUtils.toByteArray(this.scannable.getScannableInputStream());
				this.checkpoint = bytes.length;
			} catch (IOException e) {
	    		throw new TextScanException("Unable to checkpoint scannable '" + this.scannableName + QUOTE, e);
			}
    	} else if (this.scannable.isScannableString()) {
    		this.checkpoint = this.scannable.getScannableString().length();
    	} else {
    		throw new TextScanException("Unable to checkpoint scannable '" + this.scannableName + QUOTE +", unknown scannable type");
    	}

        return this;
    }

    @Override
	public ILogScanner setCheckpoint(long checkpoint) throws TextScanException {
    	this.checkpoint = checkpoint;
		return this;
	}

	@Override
    public ILogScanner resetCheckpoint() {
    	this.checkpoint = -1;
        return this;
    }

    @Override
    public long getCheckpoint() {
    	return this.checkpoint;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkIsCheckpointed();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			skipToCheckpoint();
    			this.textScanner.scan(this.scannable.getScannableInputStream(), searchPattern, failPattern, count);
    		} else if (this.scannable.isScannableString()) {
    			this.textScanner.scan(this.scannable.getScannableString().substring((int) this.checkpoint), searchPattern, failPattern, count);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (FailTextFoundException e) {
			throw new FailTextFoundException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
        return this;
    }

    @Override
    public ILogScanner scanSinceCheckpoint(String searchString, String failString, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkIsCheckpointed();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			skipToCheckpoint();
    			this.textScanner.scan(this.scannable.getScannableInputStream(), searchString, failString, count);
    		} else if (this.scannable.isScannableString()) {
    			this.textScanner.scan(this.scannable.getScannableString().substring((int) this.checkpoint), searchString, failString, count);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (FailTextFoundException e) {
			throw new FailTextFoundException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
        return this;
    }

    @Override
    public String scanForMatchSinceCheckpoint(Pattern searchPattern, Pattern failPattern, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkIsCheckpointed();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			skipToCheckpoint();
    			return this.textScanner.scanForMatch(this.scannable.getScannableInputStream(), searchPattern, failPattern, occurrance);
    		} else if (this.scannable.isScannableString()) {
    			return this.textScanner.scanForMatch(this.scannable.getScannableString().substring((int) this.checkpoint), searchPattern, failPattern, occurrance);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
    }

    @Override
    public String scanForMatchSinceCheckpoint(String searchString, String failString, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkIsCheckpointed();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			skipToCheckpoint();
    			return this.textScanner.scanForMatch(this.scannable.getScannableInputStream(), searchString, failString, occurrance);
    		} else if (this.scannable.isScannableString()) {
    			return this.textScanner.scanForMatch(this.scannable.getScannableString().substring((int) this.checkpoint), searchString, failString, occurrance);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
    }

    @Override
    public ILogScanner scan(Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkScannableNoNull();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			this.textScanner.scan(this.scannable.getScannableInputStream(), searchPattern, failPattern, count);
    		} else if (this.scannable.isScannableString()) {
    			this.textScanner.scan(this.scannable.getScannableString(), searchPattern, failPattern, count);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (FailTextFoundException e) {
			throw new FailTextFoundException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
        return this;
    }

    @Override
    public ILogScanner scan(String searchString, String failString, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkScannableNoNull();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			this.textScanner.scan(this.scannable.getScannableInputStream(), searchString, failString, count);
    		} else if (this.scannable.isScannableString()) {
    			this.textScanner.scan(this.scannable.getScannableString(), searchString, failString, count);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (FailTextFoundException e) {
			throw new FailTextFoundException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
        return this;
    }

    @Override
    public String scanForMatch(Pattern searchPattern, Pattern failPattern, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkScannableNoNull();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			return this.textScanner.scanForMatch(this.scannable.getScannableInputStream(), searchPattern, failPattern, occurrance);
    		} else if (this.scannable.isScannableString()) {
    			return this.textScanner.scanForMatch(this.scannable.getScannableString(), searchPattern, failPattern, occurrance);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
    }

    @Override
    public String scanForMatch(String searchString, String failString, int occurrance) throws MissingTextException, IncorrectOccurrencesException, TextScanException {
    	checkScannableNoNull();
    	try {
    		if (this.scannable.isScannableInputStream()) {
    			return this.textScanner.scanForMatch(this.scannable.getScannableInputStream(), searchString, failString, occurrance);
    		} else if (this.scannable.isScannableString()) {
    			return this.textScanner.scanForMatch(this.scannable.getScannableString(), searchString, failString, occurrance);
    		} else {
    			throw new TextScanException(MESSAGE_UNKNOWN_SCANNABLE_TYPE);
    		}
		} catch (MissingTextException e) {
			throw new MissingTextException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		} catch (TextScanException e) {
			throw new TextScanException(MESSAGE_PROBLEM_SCANNING + this.scannableName + QUOTE ,e);
		}
    }

	protected void setInternalScannable(ITextScannable scannable) {
		reset();
		this.scannable = scannable;
		this.scannableName = scannable.getScannableName();
	}

	protected void checkScannableNoNull() throws TextScanException {
		if (this.scannable == null) {
			throw new TextScanException("Scannable must not be null");
		}
	}

	protected void checkIsCheckpointed() throws TextScanException {
		checkScannableNoNull();
		if (this.checkpoint == -1) {
			throw new TextScanException("Scannable has not been checkpointed");
		}
	}

	protected void skipToCheckpoint() throws TextScanException {
		if (getCheckpoint() == -1) {
			logger.warn("Log '" + this.scannableName + "' has not been checkpointed");
			return;
		}
		try {
			this.scannable.updateScannable();
			long skipped = this.scannable.getScannableInputStream().skip(this.checkpoint);
			if (skipped != this.checkpoint) {
				throw new IOException("Failed to skip " + checkpoint + " bytes. Actual bytes skipped " + skipped);
			}
		} catch (IOException e) {
			throw new TextScanException("Unable to skip to checkpoint of scannable '" + this.scannableName + QUOTE, e);
		}
	}

}
