/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.manager.internal;

import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.cicsts.ICicsRegion;

public abstract class VtpRecorderImpl {
	protected HashMap<ICicsRegion,RecordingData> recordingRegions;
	protected String dumpHLQ;
	protected Log logger;
	protected VtpManagerImpl manager;
	
	
	protected String currentMethod = new String();
	protected int    recordingNumber = 1;

	public VtpRecorderImpl(HashMap<ICicsRegion,RecordingData> recordingRegions, String HLQ, Log logger, VtpManagerImpl manager) {
		this.recordingRegions = recordingRegions;
		this.logger = logger;
		this.dumpHLQ = HLQ;
		this.manager = manager;
	}
	
	public void setCurrentMethod(String method) {
		this.currentMethod = method;
	}
	
	abstract void startRecording();
	
	abstract void endRecording();
	
	abstract void writeRecording();
	
	abstract void exportRecording();
	
}
