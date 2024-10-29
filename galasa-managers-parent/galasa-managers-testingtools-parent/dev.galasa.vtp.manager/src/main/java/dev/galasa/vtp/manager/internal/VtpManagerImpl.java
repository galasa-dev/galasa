/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.vtp.manager.internal;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.Test;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.artifact.TestBundleResourceException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.cicsts.spi.ICicsRegionProvisioned;
import dev.galasa.cicsts.spi.ICicstsManagerSpi;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.Result;
import dev.galasa.framework.spi.language.GalasaMethod;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.vtp.internal.properties.DataSetHLQ;
import dev.galasa.vtp.internal.properties.TransactionNamesForTag;
import dev.galasa.vtp.internal.properties.VtpAPI;
import dev.galasa.vtp.internal.properties.VtpEnable;
import dev.galasa.vtp.internal.properties.VtpPropertiesSingleton;
import dev.galasa.vtp.manager.VtpManagerException;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos3270.spi.IZos3270ManagerSpi;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosbatch.spi.IZosBatchSpi;

@Component(service = { IManager.class })
public class VtpManagerImpl extends AbstractManager {

	private static final Log logger = LogFactory.getLog(VtpManagerImpl.class);

	public final static String NAMESPACE = "vtp";
	private static final String PASSED_RESULT = "Passed";

	private ICicstsManagerSpi cicsManager;
	private IZosBatchSpi      batchManager;
	private IArtifactManager  artifactManager;
	private IConfigurationPropertyStoreService cps;
	
	private HashMap<ICicsRegion,RecordingData> recordingRegions = new HashMap<>(); 
	
	private String dumpDataSetHLQ  = new String();
	private boolean skipRecordings = false;
	
	private Path storedArtifactRoot;
	
	private VtpRecorderImpl recorder;

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		
		for(Map.Entry<String, ICicsRegionProvisioned> entry : cicsManager.getTaggedCicsRegions().entrySet()) {
			ICicsTerminal terminal = cicsManager.generateCicsTerminal(entry.getKey());
			ICicsRegion region = entry.getValue();
			RecordingData rd = new RecordingData();
			rd.setRecordingTerminal(terminal);
			recordingRegions.put(region, rd);
		}
		if (recordingRegions.size() == 0) {
			logger.info("VTP Recording enabled but test class contains no CICS TS fields - recording will not be attempted");
			skipRecordings = true;
		}
		
		boolean useAPI = VtpAPI.get();
		if(useAPI) {
			logger.info("VTP Manager will use the VTP API for recordings");
			this.recorder = new VtpApiRecorderImp(recordingRegions, this.dumpDataSetHLQ, logger, this);
		}else {
			logger.info("VTP Manager will use the VTP txns for recordings");
			this.recorder = new VtpTxnRecorderImpl(recordingRegions, this.dumpDataSetHLQ, logger, this);
		}
	}

	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);
		
		//get access to the CPS so we can configure ourself
		try {
			this.cps = getFramework().getConfigurationPropertyService(NAMESPACE);
			VtpPropertiesSingleton.setCps(this.cps);
		} catch (ConfigurationPropertyStoreException e1) {
			throw new VtpManagerException("Unable to access framework services", e1);
		}
		
		//if VTP recording is not enabled then exit
		if(!VtpEnable.get()) {
			return;
		}
		
		//if this is not a java galasa test then exit
		if(!galasaTest.isJava()) {
			logger.info("VTP recording is requested but is not eligible as this is not a Java test");
			return;
		}
		
		//ensure that there is a dump playback hlq specified
		dumpDataSetHLQ = DataSetHLQ.get();
		if(dumpDataSetHLQ == null || dumpDataSetHLQ.isEmpty()) {
			logger.error("VTP recording is enabled but no playback HLQ provided");
			return;
		}
		dumpDataSetHLQ = dumpDataSetHLQ + "." + getFramework().getTestRunName();
		
		//if we get here then we are required so add ourself to the list of active managers
		this.storedArtifactRoot = getFramework().getResultArchiveStore().getStoredArtifactsRoot().resolve(VtpManagerImpl.NAMESPACE);
		youAreRequired(allManagers, activeManagers, galasaTest);
	}

	@Override
	public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers,
			@NotNull GalasaTest galasaTest) throws ManagerException {
		if (activeManagers.contains(this)) {
			return;
		}

		activeManagers.add(this);
		cicsManager = addDependentManager(allManagers, activeManagers, galasaTest, ICicstsManagerSpi.class);
		if (cicsManager == null) {
			throw new VtpManagerException("The CICS Manager is not available");
		}
		
		batchManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosBatchSpi.class);
		if (batchManager == null) {
			throw new VtpManagerException("The z/OS Batch Manager is not available");
		}
		
		artifactManager = addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
		if(artifactManager == null) {
			throw new VtpManagerException("The Artifact Manager is not available");
		}
	}

	@Override
	public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
		if (otherManager instanceof ICicstsManagerSpi || otherManager instanceof IZos3270ManagerSpi) {
			return true;
		}

		return super.areYouProvisionalDependentOn(otherManager);
	}

	@Override
	public void startOfTestMethod(@NotNull GalasaMethod galasaMethod) throws ManagerException {
		if(skipRecordings) {
			return;
		}
		if(isTestMethod(galasaMethod)) {
			recorder.startRecording();
		}
	}

	@Override
	public Result endOfTestMethod(@NotNull GalasaMethod galasaMethod, @NotNull Result currentResult,
			Throwable currentException) throws ManagerException {
		if(skipRecordings) {
			//we are not going to do anything and don't need to change the status
			return null;
		}
		if(isTestMethod(galasaMethod)) {
			recorder.endRecording();
		}
		
		if(currentResult.isPassed()) {
			recorder.setCurrentMethod(galasaMethod.getJavaExecutionMethod().getName());
			recorder.writeRecording();
			recorder.exportRecording();
		}
		//we do not need to alter the test result
		return null;
	}
	
	/**
	 * Utility method that checks that the passed method is in fact
	 * a java method and has been annotated @Test
	 * @param method the method to test
	 * @return true if method is java and annotated @Test else false
	 */
	private boolean isTestMethod(GalasaMethod method) {
		if(!method.isJava()) {
			return false;
		}
		for(Annotation a : method.getJavaExecutionMethod().getAnnotations()) {
			if(a instanceof Test) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Retrieves the CPS property listing the comma separated list of transactions that 
	 * we want to record.  We check that each transaction is 4 characters in length and
	 * return an ArrayList of transactions
	 * @param tag the tag of the region we want to record against
	 * @return a list of Strings that represent the transactions retrieved
	 * @throws VtpManagerException
	 */
	private ArrayList<String> getTransactionsForTag(String tag) throws VtpManagerException{
		ArrayList<String> transactions = new ArrayList<String>();
		String rawData = TransactionNamesForTag.get(tag);
		StringTokenizer st = new StringTokenizer(rawData, ",");
		while(st.hasMoreTokens()) {
			String txn = st.nextToken();
			if(txn.length() == 4) {
				transactions.add(txn);
			}
		}
		return transactions;
	}
	
	@Override
	public Result endOfTestClass(@NotNull Result currentResult, Throwable currentException) throws ManagerException {
		for(ICicsRegion region : recordingRegions.keySet()) {
			String applid = region.getApplid();
			String content = recordingRegions.get(region).getExportedRecordings();
			Path f = this.storedArtifactRoot.resolve(applid);
			try {
				Files.write(f, content.getBytes(), StandardOpenOption.CREATE);
			} catch (Exception e) {
				logger.info("Unable to record exported recordings", e);
			} 
		}
		//We are not going to change the result
		return super.endOfTestClass(currentResult, currentException);
	}
	
	/**
	 * Before the test starts, ensure that the recording terminal
	 * is connected and moved to the CECI screen.
	 * Then obtain the VTP API version
	 */
	@Override
	public void startOfTestClass() throws ManagerException {	
		for(ICicsRegion region : recordingRegions.keySet()) {
			ICicsTerminal terminal = recordingRegions.get(region).getRecordingTerminal();
			try {
				if(!terminal.isConnected()) {
					terminal.connect();
				}
				ArrayList<String> transactions = getTransactionsForTag(region.getTag());
				recordingRegions.get(region).setRecordingTransactions(transactions);
			} catch (NetworkException e) {
				throw new VtpManagerException("Unable to initiate CECI on a recording terminal",e);
			} 
		}
	}
	
	public void copyDumpedPlaybackFile(IZosImage image, HashMap<String, Object> attrs) throws VtpManagerException {
		try {
			String jcl = artifactManager.getBundleResources(this.getClass()).retrieveSkeletonFileAsString("/jcl/dumpJCL", attrs).trim();
			IZosBatchJob job = batchManager.getZosBatch(image).submitJob(jcl, null);
			int rc = job.waitForJob();
			if(rc > 4) {
				logger.error("JCL to export recording fail, check artifacts for more details");
			}
		}catch (ZosBatchException | TestBundleResourceException | IOException e) {
			throw new VtpManagerException("Unable to run JCL to export recording",e);
		}
		
		
	}
}
