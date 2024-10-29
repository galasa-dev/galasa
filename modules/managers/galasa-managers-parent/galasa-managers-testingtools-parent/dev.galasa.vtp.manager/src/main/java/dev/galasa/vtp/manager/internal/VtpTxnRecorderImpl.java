/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.manager.internal;

import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.ManagerException;
import dev.galasa.cicsts.CicstsHashMap;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.vtp.manager.VtpManagerException;
import dev.galasa.zos3270.Zos3270Exception;

public class VtpTxnRecorderImpl extends VtpRecorderImpl {

	public VtpTxnRecorderImpl(HashMap<ICicsRegion, RecordingData> recordingRegions, String HLQ, Log logger, VtpManagerImpl manager) {
		super(recordingRegions, HLQ, logger, manager);
	}

	@Override
	void startRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Starting VTP Recording for region: " + region.getApplid());
			try {
				startRecordingUsingTxn(region, terminal);
			}catch(VtpManagerException e) {
				logger.error("Was unable to start recording on region: " + region.getApplid(),e);
			}		
		}
	}
	
	private void startRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException{
		for(String transaction : recordingRegions.get(region).getRecordingTransactions()) {
			String command = "BZUT " + transaction;
			String expectedResponse = "RECORDING STARTED FOR TRANSACTION " + transaction;
			try {
				logger.trace("Waiting for keyboard to be ready");
				terminal.waitForKeyboard();
				logger.trace("Entering command to start recording: " + command);
				terminal.type(command).enter();
				logger.trace("Waiting for response: " + expectedResponse);
				terminal.waitForTextInField(expectedResponse);
				logger.trace("Resetting terminal");
				terminal.clear().wfk();
			} catch (Zos3270Exception e) {
				throw new VtpManagerException("Error when starting recording for region: " + region.getApplid(), e);
			}
		}
	}

	@Override
	void endRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Stopping VTP Recording");
			try {
				stopRecordingUsingTxn(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}
	}
	
	private void stopRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException{
		String command = "BZUE";
		String expectedResponse = "RECORDING STOPPED";
		try {
			logger.trace("Waiting for keyboard to be ready");
			terminal.waitForKeyboard();
			logger.trace("Entering command to start recording: " + command);
			terminal.type(command).enter();
			logger.trace("Waiting for response: " + expectedResponse);
			terminal.waitForTextInField(expectedResponse);
			logger.trace("Resetting terminal");
			terminal.clear().wfk();
		} catch (Zos3270Exception e) {
			throw new VtpManagerException(e);
		}
	}

	@Override
	void writeRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Writing VTP Recording for region: " + region.getApplid());
			try {
				writeRecordingUsingTxn(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}
	}
	
	private void writeRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException{
		String command = "BZUW";
		String expectedResponse = "RECORDS WRITTEN";
		try {
			logger.trace("Waiting for keyboard to be ready");
			terminal.waitForKeyboard();
			logger.trace("Entering command to start recording: " + command);
			terminal.type(command).enter();
			logger.trace("Waiting for response: " + expectedResponse);
			terminal.waitForTextInField(expectedResponse);
			logger.trace("Resetting terminal");
			terminal.clear().wfk();
			
		} catch (Zos3270Exception e) {
			throw new VtpManagerException(e);
		} 
	}

	@Override
	void exportRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Exporting VTP Recording");
			try {
				exportRecordingUsingTxn(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}
	}
	
	private void exportRecordingUsingTxn(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException {
		HashMap<String, Object> attrs = new HashMap<>();
		try {
			CicstsHashMap tdqAttrs = region.cemt().inquireResource(terminal, "TDQ", "BZUQ");
			String dsName = tdqAttrs.get("dsname");
			String dumpTargetDSName = this.dumpHLQ + ".R" + this.recordingNumber;
			
			attrs.put("SOURCE", dsName);
			attrs.put("TARGET", dumpTargetDSName);
			
			this.recordingNumber++;
			} catch (Exception e) {
				throw new VtpManagerException("unable to get dsname of TDQ BZUQ",e);
			}
		try {
			region.cemt().setResource(terminal, "TDQ", "BZUQ", "CLOSED");
			manager.copyDumpedPlaybackFile(region.getZosImage(), attrs);
			this.recordingRegions.get(region).addExportedRecording(attrs.get("TARGET").toString(), this.currentMethod);
			region.cemt().setResource(terminal, "TDQ", "BZUQ", "OPEN");
		}catch(ManagerException e) {
			throw new VtpManagerException("unable to export recording for region: " + region.getApplid(),e);
		}
			
		
		
	}

}
