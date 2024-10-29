/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.textscan.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.galasa.textscan.FailTextFoundException;
import dev.galasa.textscan.ITextScannable;
import dev.galasa.textscan.ITextScanner;
import dev.galasa.textscan.IncorrectOccurrencesException;
import dev.galasa.textscan.MissingTextException;
import dev.galasa.textscan.TextScanException;

public class TextScannerImpl implements ITextScanner {	

	private static final String MSG_INVALID_COUNT = "Count must be greater than or equal to 1";
	private static final String MSG_INCORRECT_SCANNABLE_TYPE = "Incorrect scannable type, must be String or InputStream";
	private static final String MSG_FAIL_FOUND = "Fail %s '%s' found";
	private static final String MSG_SEARCH_NOT_FOUND = "Search %s '%s' not found";
	private static final String MSG_EXPECTING_BUT_FOUND = "Expecting %d instances of %s '%s' but found %d occurrence(s)";
	private static final String MSG_WRONG_NUMBER_FOUND = "Wrong number of occurrences of String '%s' found";
	private static final String MSG_UNABLE_TO_FIND_OCCURRENCE = "Unable to find occurrence %d of Pattern '%s'. Occurrences found: %d";

	private static final String PATTERN = "Pattern";
	private static final String STRING = "String";

	@Override
	public ITextScanner scan(String text, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {

		if (count < 1) {
			throw new TextScanException(MSG_INVALID_COUNT);
		}

		if (failPattern != null && failPattern.matcher(text).find()) {
			throw new FailTextFoundException(String.format(MSG_FAIL_FOUND, PATTERN, failPattern));
		}

		Matcher m = searchPattern.matcher(text);
		int found = 0;

		while (m.find()) {
			found++;
		}

		if (found == 0) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, PATTERN, searchPattern));
		}
		else if (found < count) {
			throw new IncorrectOccurrencesException(String.format(MSG_EXPECTING_BUT_FOUND, count, PATTERN, searchPattern, found));
		}

		return this;
	}

	@Override
	public ITextScanner scan(String text, String searchString, String failString, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {

		Pattern fp = null;
		if (failString != null) {
			fp = Pattern.compile("\\Q" + failString + "\\E");
		}
		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");
		
		ITextScanner textScanner;
		try {
			textScanner = scan(text,p,fp,count);
		} catch (FailTextFoundException e) {
			throw new FailTextFoundException(String.format(MSG_FAIL_FOUND, STRING, failString), e);
		} catch (MissingTextException e) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, STRING, searchString), e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(String.format(MSG_WRONG_NUMBER_FOUND, searchString), e);
		}
		
		return textScanner;

	}

	@Override
	public ITextScanner scan(ITextScannable scannable, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {
		if(scannable.isScannableInputStream()) {
			return scan(scannable.getScannableInputStream(),searchPattern,failPattern,count);	
		} else if(scannable.isScannableString()) {
			return scan(scannable.getScannableString(),searchPattern,failPattern,count);	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanException(MSG_INCORRECT_SCANNABLE_TYPE);
	}

	@Override
	public ITextScanner scan(ITextScannable scannable, String searchString, String failString, int count) throws TextScanException, FailTextFoundException, IncorrectOccurrencesException, MissingTextException {
		if (scannable.isScannableInputStream()) {
			return scan(scannable.getScannableInputStream(),searchString,failString,count);	
		} else if(scannable.isScannableString()) {
			return scan(scannable.getScannableString(),searchString,failString,count);	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanException(MSG_INCORRECT_SCANNABLE_TYPE);
	}

	@Override
	public ITextScanner scan(InputStream inputStream, Pattern searchPattern, Pattern failPattern, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {

		if (count < 1) {
			throw new TextScanException(MSG_INVALID_COUNT);
		}

		ArrayList<Integer> foundPatternStartPositions = new ArrayList<>();
		LinkedList<String> buffer = new LinkedList<>();

		int offset = 0;

		try (BufferedReader reader = new BufferedReader (new InputStreamReader(inputStream))){
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.add(line);
				if (buffer.size() > 10) {
					offset = offset + buffer.remove(0).length() + 1;
				}

				// Build a String from the current buffer
				StringBuilder sb = new StringBuilder();
				for (String queueLine : buffer) {
					sb.append(queueLine);
					sb.append("\n");
				}

				String currentBuffer = sb.toString();

				if (failPattern != null && failPattern.matcher(currentBuffer).find()) {
					throw new FailTextFoundException(String.format(MSG_FAIL_FOUND, PATTERN, failPattern));
				}

				Matcher m = searchPattern.matcher(currentBuffer);
				while (m.find()) {

					int stp = m.start() + offset;
					if (!foundPatternStartPositions.contains(stp)) {
						foundPatternStartPositions.add(stp);

					}
				}
			}
			if (foundPatternStartPositions.size() >= count) {
				return this;
			}
		} catch (IOException e) {
			throw new TextScanException("Problem in InputStream scan", e);
		}

		if(foundPatternStartPositions.isEmpty()) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, PATTERN, searchPattern));
		}
		throw new IncorrectOccurrencesException(String.format(MSG_EXPECTING_BUT_FOUND, count, PATTERN, searchPattern, foundPatternStartPositions.size()));
	}

	@Override
	public ITextScanner scan(InputStream inputStream, String searchString, String failString, int count) throws FailTextFoundException, MissingTextException, IncorrectOccurrencesException, TextScanException {

		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");
		Pattern fp = null;
		if (failString != null) {
			fp = Pattern.compile("\\Q" + failString + "\\E");
		}
		ITextScanner textScanner;
		try {
			textScanner = scan(inputStream,p,fp,count);
		} catch (FailTextFoundException e) {
			throw new FailTextFoundException(String.format(MSG_FAIL_FOUND, STRING, failString), e);
		} catch (MissingTextException e) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, STRING, searchString), e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(String.format(MSG_WRONG_NUMBER_FOUND, searchString), e);
		}
		return textScanner;
	}

	@Override
	public String scanForMatch(String text, Pattern searchPattern, Pattern failPattern, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException {

		if(occurrence < 1) {
			throw new TextScanException(MSG_INVALID_COUNT);
		}

		if (failPattern != null) {
			Matcher fm = failPattern.matcher(text);
			if (fm.find()) {
				return fm.group(); 
			}
		}

		Matcher m = searchPattern.matcher(text);
		int found = 0;
		String searchText = "";

		while (m.find()) {
			found++;
			searchText = m.group();
		}

		if (found == 0) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, PATTERN, searchPattern));
		}
		else if (found < occurrence) {
			throw new IncorrectOccurrencesException(String.format(MSG_UNABLE_TO_FIND_OCCURRENCE, occurrence, searchPattern, found));
		}

		return searchText;
	}

	@Override
	public String scanForMatch(String text, String searchString, String failString, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException {

		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");
		Pattern fp = null;
		if (failString != null) {
			fp = Pattern.compile("\\Q" + failString + "\\E");
		}
		String match;
		try {
			match = scanForMatch(text, p, fp, occurrence);
		} catch (MissingTextException e) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, STRING, searchString), e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(String.format(MSG_WRONG_NUMBER_FOUND, searchString), e);
		}

		return match;
	}

	@Override
	public String scanForMatch(ITextScannable scannable, Pattern searchPattern, Pattern failPattern, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException{
		if(scannable.isScannableInputStream()) {
			return scanForMatch(scannable.getScannableInputStream(), searchPattern, failPattern, occurrence);		
		} else if(scannable.isScannableString()) {
			return scanForMatch(scannable.getScannableString(), searchPattern, failPattern, occurrence);	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanException(MSG_INCORRECT_SCANNABLE_TYPE);
	}

	@Override
	public String scanForMatch(ITextScannable scannable, String searchString, String failString, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException {
		if(scannable.isScannableInputStream()) {
			return scanForMatch(scannable.getScannableInputStream(), searchString, failString, occurrence);		
		} else if(scannable.isScannableString()) {
			return scanForMatch(scannable.getScannableString(),searchString, failString, occurrence);	
		}
		/** It should never reach this line, it should either be an Input Steam or String **/
		throw new TextScanException(MSG_INCORRECT_SCANNABLE_TYPE);
	}

	@Override
	public String scanForMatch(InputStream inputStream, Pattern searchPattern, Pattern failPattern, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException {

		if (occurrence < 1) {
			throw new TextScanException(MSG_INVALID_COUNT);
		}

		ArrayList<Integer> foundPatternStartPositions = new ArrayList<>();
		LinkedList<String> buffer = new LinkedList<>();

		int offset = 0;
		String foundString = "";

		try (BufferedReader  reader = new BufferedReader (new InputStreamReader(inputStream))) {
			String line = "";
			while ((line =reader.readLine()) != null) {
				buffer.add(line);
				if (buffer.size() > 10) {
					offset = offset + buffer.remove(0).length() + 1;
				}

				// Build a String from the current buffer
				StringBuilder sb = new StringBuilder();
				for (String queueLine : buffer) {
					sb.append(queueLine);
					sb.append("\n");
				}

				String currentBuffer = sb.toString();

				if (failPattern != null) {
					Matcher fm = failPattern.matcher(currentBuffer);
					if (fm.find()) {
						return fm.group(); 
					}
				}
				
				Matcher m = searchPattern.matcher(currentBuffer);

				while (m.find()) {

					int stp = m.start() + offset;
					if (!foundPatternStartPositions.contains(stp)) {
						foundPatternStartPositions.add(stp);
						foundString = m.group();
					} 
					if (foundPatternStartPositions.size() >= occurrence) {
						return foundString;
					}
				}
			}

		} catch (IOException e) {
			throw new TextScanException("Problem in InputStream scan", e);
		}
		if(foundPatternStartPositions.isEmpty()) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, PATTERN, searchPattern));
		}
		throw new IncorrectOccurrencesException(String.format(MSG_UNABLE_TO_FIND_OCCURRENCE, occurrence, searchPattern, foundPatternStartPositions.size()));
	}

	@Override
	public String scanForMatch(InputStream inputStream, String searchString, String failString, int occurrence) throws MissingTextException, IncorrectOccurrencesException, TextScanException {
		Pattern p = Pattern.compile("\\Q" + searchString + "\\E");
		Pattern fp = null;
		if (failString != null) {
			fp = Pattern.compile("\\Q" + failString + "\\E");
		}
		String match;
		try {
			match = scanForMatch(inputStream, p, fp, occurrence);
		} catch (MissingTextException e) {
			throw new MissingTextException(String.format(MSG_SEARCH_NOT_FOUND, STRING, searchString), e);
		} catch (IncorrectOccurrencesException e) {
			throw new IncorrectOccurrencesException(String.format(MSG_WRONG_NUMBER_FOUND, searchString), e);
		}

		return match;
	}

}
