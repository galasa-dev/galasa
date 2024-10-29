/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.manager.internal;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.logging.Log;

import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICeciResponse;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.vtp.manager.VtpManagerException;

public class VtpApiRecorderImp extends VtpRecorderImpl {
	
	private static final String VtpApiCommandStart = "LINK PROG(BZUCIDRP) COM(";
	private static final String VtpApiCommandEnd   = ")";
	
	private static final String VtpApiVersionData         = "VERS              ";
	private static final String VtpApiStartRecordingData  = "STRT                    ";
	private static final String VtpApiExportRecordingData = "EXPT                         ";
	private static final String VtpApiListRecordingData   = "LIST                    ";
	
	private static final String VtpVersionName  = "&VTPVERS";
	private static final String VtpStartName  = "&VTPSTRT";
	private static final String VtpStopName  = "&VTPSTOP";
	private static final String VtpExportName  = "&VTPEXPT";
	
	private static final int    VtpTokenIndexStart = 16;
	private static final int    VtpTokenIndexEnd   = 24;

	public VtpApiRecorderImp(HashMap<ICicsRegion, RecordingData> recordingRegions, String HLQ, Log logger, VtpManagerImpl manager) {
		super(recordingRegions, HLQ, logger, manager);
	}

	@Override
	void startRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Starting VTP Recording");
			try {
				startRecordingUsingApi(region, terminal);
			}catch(VtpManagerException e) {
				logger.error("Was unable to start recording on region: " + region.getApplid(),e);
			}		
		}

	}
	
	private void startRecordingUsingApi(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException {
		try {
			region.ceci().defineVariableText(terminal, VtpStartName, VtpApiStartRecordingData);
			ICeciResponse response = region.ceci().issueCommand(terminal, VtpApiCommandStart + VtpStartName + VtpApiCommandEnd);
			if(!response.isNormal()) {
				throw new VtpManagerException("Non normal response from CECI while starting recording, EIBRESP: " + response.getEIBRESP() + " EIBRESP2:" + response.getEIBRESP2());
			}
			
			String responseData = region.ceci().retrieveVariableText(terminal, VtpStartName);
			char[] binaryResponse = region.ceci().retrieveVariableBinary(terminal, VtpStartName);
			char[] recordingToken = Arrays.copyOfRange(binaryResponse, VtpTokenIndexStart, VtpTokenIndexEnd);
			recordingRegions.get(region).setRecordingToken(recordingToken);
		} catch (CicstsManagerException e) {
			throw new VtpManagerException("Unable to start VTP recording",e);
		} 
	}

	@Override
	void endRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Stopping VTP Recording");
			try {
				stopRecordingUsingApi(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}

	}
	
	private void stopRecordingUsingApi(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException {
		try {				
			//obtain recording token
			char[] recordingToken = recordingRegions.get(region).getRecordingToken();
			if(recordingToken == null) {
				logger.error("No recording token saved for region tagged: " + region.getTag() + ". Not attempting to stop recording");
				return;
			}
			
			//fill in the API data 
			char[] recordingData  = new char[24];
			char blank = 0;
			Arrays.fill(recordingData, blank);
			//first four characters are STOP
			System.arraycopy(new char[]{226,227,214,215}, 0, recordingData, 0, 4);
			//put the recording token at the end
			System.arraycopy(recordingToken, 0, recordingData, 16, 8);
			
			region.ceci().defineVariableBinary(terminal, VtpStopName, recordingData);
			ICeciResponse response = region.ceci().issueCommand(terminal, VtpApiCommandStart + VtpStopName + VtpApiCommandEnd);
			
			if(!response.isNormal()) {
				throw new VtpManagerException("Non normal response from CECI while stopping recording, EIBRESP: " + response.getEIBRESP() + " EIBRESP2:" + response.getEIBRESP2());
			}
			String responseData = region.ceci().retrieveVariableText(terminal, VtpStopName);
		} catch (CicstsManagerException e) {
			throw new VtpManagerException("Unable to start VTP recording",e);
		}
	}

	@Override
	void writeRecording() {
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			logger.info("Writing VTP Recording");
			try {
				writeRecordingUsingApi(region, terminal);
			} catch (VtpManagerException e) {
				logger.error("Unable to stop recording on region: " + region.getApplid(),e);
			}
		}
	}
	
	private void writeRecordingUsingApi(ICicsRegion region, ICicsTerminal terminal) throws VtpManagerException {
		try {
			region.ceci().defineVariableText(terminal, VtpExportName, VtpApiListRecordingData);
			ICeciResponse response = region.ceci().issueCommand(terminal, VtpApiCommandStart + VtpExportName + VtpApiCommandEnd);
			if(!response.isNormal()) {
				throw new VtpManagerException("Non normal response from CECI while exporting VTP Recording, EIBRESP: " + response.getEIBRESP() + " EIBRESP2:" + response.getEIBRESP2());
			}
			char[] responseData = region.ceci().retrieveVariableBinary(terminal, VtpExportName);
			StringBuilder responseDataStringBuilder = new StringBuilder();
			for(char c : responseData) {
				 String hex = Integer.toHexString(c);
				 hex = String.format("%2S", hex).replaceAll(" ", "0");
                 responseDataStringBuilder.append(hex.toUpperCase());   
			}
			logger.info("VTP returned: " + responseDataStringBuilder.toString());
			} 
		catch (CicstsManagerException e) {
			throw new VtpManagerException("Unable to export VTP recording",e);
		}
		
		try {
			region.ceci().defineVariableText(terminal, VtpExportName, VtpApiExportRecordingData);
			ICeciResponse response = region.ceci().issueCommand(terminal, VtpApiCommandStart + VtpExportName + VtpApiCommandEnd);
			if(!response.isNormal()) {
				throw new VtpManagerException("Non normal response from CECI while exporting VTP Recording, EIBRESP: " + response.getEIBRESP() + " EIBRESP2:" + response.getEIBRESP2());
			}
			String responseData = region.ceci().retrieveVariableText(terminal, VtpExportName);
			char[] binaryResponseData = region.ceci().retrieveVariableBinary(terminal, VtpExportName);
			for(char c : binaryResponseData) {
				int d = c+0;
				logger.info((int)c);
			}

			logger.info("VTP returned: " + responseData);
		} catch (CicstsManagerException e) {
			throw new VtpManagerException("Unable to export VTP recording",e);
		} 
	}

	@Override
	void exportRecording() {


	}

}
