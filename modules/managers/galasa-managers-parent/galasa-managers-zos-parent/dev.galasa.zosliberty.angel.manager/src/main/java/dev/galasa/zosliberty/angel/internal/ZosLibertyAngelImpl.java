/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zosliberty.angel.internal;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zosbatch.IZosBatch;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.IZosBatchJobOutputSpoolFile;
import dev.galasa.zosbatch.ZosBatchException;
import dev.galasa.zosconsole.IZosConsole;
import dev.galasa.zosconsole.ZosConsoleException;
import dev.galasa.zosliberty.angel.IZosLibertyAngel;
import dev.galasa.zosliberty.angel.ZosLibertyAngelException;
import dev.galasa.zosliberty.angel.ZosLibertyAngelManagerException;
import dev.galasa.zosliberty.angel.internal.properties.DefaultTimeout;
import dev.galasa.zosliberty.angel.internal.properties.Procname;

public class ZosLibertyAngelImpl implements IZosLibertyAngel {
    private static final Log logger = LogFactory.getLog(ZosLibertyAngelImpl.class);

    private ZosLibertyAngelManagerImpl zosLibertyAngelManager;
	private IZosConsole zosConsole;
	private IZosBatch zosBatch;
	private IZosImage zosImage;
    private String angelName;
	private String procname;
	private String root;
	private int defaultTimeout;
	private IZosBatchJob angelJob;

	private static final String SLASH_SYBMOL = "/";

    public ZosLibertyAngelImpl(ZosLibertyAngelManagerImpl zosLibertyManager, IZosImage zosImage, String angelName) throws ZosLibertyAngelException {
        this.zosLibertyAngelManager = zosLibertyManager;
        this.zosImage = zosImage;
        if (angelName.isEmpty()) {
    		String runid = this.zosLibertyAngelManager.getFramework().getTestRunName();
			String prefix = runid.substring(0, 1);
    		String suffix = runid.substring(1);
    		if (suffix.length() > 7) {
    			suffix = suffix.substring(suffix.length()-7);
    		} else {
    			suffix = StringUtils.leftPad(suffix, 7, "0");
    		}
    		this.angelName = (prefix + suffix).toUpperCase();
        } else {
        	if (angelName.length() > 8) {
        		throw new ZosLibertyAngelException("Angel Name \"" + angelName + "\"greater than the Galasa allowed maximum of 8 characters");
        	}
        	this.angelName = angelName.toUpperCase();
        }
        try {
        	this.procname = Procname.get(this.zosImage);
        } catch (ZosLibertyAngelManagerException e) {
            throw new ZosLibertyAngelException("Unable to get Liberty angel JCL procedure name", e);
        }
        try {
        	this.root = this.zosImage.getLibertyInstallDir();
            if (this.root == null) {
                throw new ZosLibertyAngelException("The Liberty install directory not been set and a value has not been supplied in the CPS for zOS image " + this.zosImage);
            }
        } catch (ZosManagerException e) {
            throw new ZosLibertyAngelException("Unable to get Liberty install directory", e);
        }
        try {
			this.defaultTimeout = DefaultTimeout.get(this.zosImage);
		} catch (ZosLibertyAngelManagerException e) {
			throw new ZosLibertyAngelException("Unable to get default timeout", e);
		}
        try {
			this.zosConsole = this.zosLibertyAngelManager.getZosConsole(this.zosImage);
		} catch (ZosLibertyAngelManagerException e) {
			throw new ZosLibertyAngelException("Problem getting zOS Console for image " + this.zosImage.getImageID(), e);
		}
        this.zosBatch = this.zosLibertyAngelManager.getZosBatch(this.zosImage);
        start();
    }
    
    @Override
    public void start() throws ZosLibertyAngelException {
    	String command = "START " + this.procname + "." + this.angelName + ",NAME=" + this.angelName + ",ROOT='" + this.root + "'";
		try {
			this.zosConsole.issueCommand(command);
		} catch (ZosConsoleException e) {
			throw new ZosLibertyAngelException("Problem issuing start command for zOS Liberty Angel", e);
		}
		waitForStart();
		if (!isActive()) {
			throw new ZosLibertyAngelException("zOS Liberty Angel \"" + this.angelName + "\" did not start");
		}
	}

    @Override
    public void stop() throws ZosLibertyAngelException {
		if (isActive()) {
	    	String command = "STOP " + this.angelName;
			try {
				this.zosConsole.issueCommand(command);
			} catch (ZosConsoleException e) {
				throw new ZosLibertyAngelException("Problem issuing stop command for zOS Liberty Angel", e);
			}
		} else {
			logger.warn("zOS Liberty Angel \"" + this.angelName + "\" not active");
		}
    }
    
    @Override
	public boolean isActive() throws ZosLibertyAngelException {
    	if (this.angelJob == null) {
    		return false;
    	} else {
    		return this.angelJob.getStatus().equals(JobStatus.ACTIVE);
    	}
	}
    
    @Override
    public String getName() {
    	return this.angelName;
    }

    protected void waitForStart() throws ZosLibertyAngelException {
	    logger.info("Waiting up to " + this.defaultTimeout + " second(s) for "+ this.angelName + " to start");
	    
	    LocalDateTime timeoutTime = LocalDateTime.now().plusSeconds(this.defaultTimeout);
	    while (LocalDateTime.now().isBefore(timeoutTime)) {
	    	this.angelJob = getAngelJob();
	        try {
	            if (this.angelJob != null) {
	            	break;
	            }
	            Thread.sleep(1000);
	        } catch (InterruptedException e) {
	            throw new ZosLibertyAngelException("waitForStart Interrupted", e);
	        }
	    }
	}

	protected IZosBatchJob getAngelJob() throws ZosLibertyAngelException {
		try {
			List<IZosBatchJob> jobs = this.zosBatch.getJobs(procname, "*");
			for (IZosBatchJob job : jobs) {
				if (job.getStatus().equals(JobStatus.ACTIVE)) {
	    			IZosBatchJobOutputSpoolFile jesmsglg = job.getSpoolFile("JESMSGLG");
	    			if (jesmsglg != null && jesmsglg.getRecords().contains("CWWKB0069I INITIALIZATION IS COMPLETE FOR THE " + this.angelName + " ANGEL PROCESS.")) {
	    				return job;
	    			}
	    		}				
			}
		} catch (ZosBatchException e) {
			throw new ZosLibertyAngelException("Problem getting zOS Liberty Angel \"" + this.angelName + "\" not active", e);
		}
		return null;
	}

	protected void cleanup() {
		try {
			stop();
			this.angelJob.waitForJob();
		} catch (ZosLibertyAngelException | ZosBatchException e) {
			logger.warn(e);
		}
		try {
			archiveJob();
		} catch (ZosLibertyAngelException e) {
			logger.warn(e);
		}
		try {
			this.angelJob.purge();
		} catch (ZosBatchException e) {
			logger.warn(e);
		}
    }
    
	protected void archiveJob() throws ZosLibertyAngelException {
        try {
            StringBuilder rasPath = new StringBuilder();
            rasPath.append(getDefaultRasPath());
            rasPath.append(SLASH_SYBMOL);
            rasPath.append("libertyAngel");
            rasPath.append(SLASH_SYBMOL);
            rasPath.append(this.angelJob.getJobname().getName());
            rasPath.append("_"); 
            rasPath.append(this.angelName);
            rasPath.append("_");
            rasPath.append(this.angelJob.getJobId());
            rasPath.append("_"); 
            rasPath.append(this.angelJob.getRetcode().replace(" ", "-").replace("????", "UNKNOWN"));
            this.angelJob.saveOutputToResultsArchive(rasPath.toString());
        } catch (ZosBatchException e) {
            throw new ZosLibertyAngelException("Problem during archive of zOS Liberty Angel \"" + this.angelName + "\" not active", e);
        }
    }

	protected String getDefaultRasPath() {
        return this.zosLibertyAngelManager.getCurrentTestMethodArchiveFolder().toString();
    }
    
	@Override
    public String toString() {
    	return "[zOS Liberty Angel] NAME=" + this.angelName;
    }
}
