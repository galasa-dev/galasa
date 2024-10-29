/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.manager.internal;

import java.util.ArrayList;
import java.util.List;

import dev.galasa.cicsts.ICicsTerminal;

public class RecordingData {
	
	private char [] recordingToken;
	private ICicsTerminal   recordingTerminal;
	private ArrayList<String> recordingTransactions;
	private List<String> exportedRecordings = new ArrayList<>();

	public char[] getRecordingToken() {
		return recordingToken;
	}

	public void setRecordingToken(char[] recordingToken) {
		this.recordingToken = recordingToken;
	}

	public ICicsTerminal getRecordingTerminal() {
		return recordingTerminal;
	}

	public void setRecordingTerminal(ICicsTerminal recordingTerminal) {
		this.recordingTerminal = recordingTerminal;
	}

	public ArrayList<String> getRecordingTransactions() {
		return recordingTransactions;
	}

	public void setRecordingTransactions(ArrayList<String> recordingTransactions) {
		this.recordingTransactions = recordingTransactions;
	}
	
	public void addExportedRecording(String dsName, String method) {
		String info = "Method: " + method + " exported as: " + dsName;
		this.exportedRecordings.add(info);
	}
	
	public String getExportedRecordings() {
		StringBuilder result = new StringBuilder();
		for(String s : exportedRecordings) {
			result.append(s);
			result.append(System.lineSeparator());
		}
		return result.toString();
	}

	public RecordingData() {
	}

}
