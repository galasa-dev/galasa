/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal.dse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import dev.galasa.ProductVersion;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.MasType;
import dev.galasa.cicsts.internal.CicstsManagerImpl;
import dev.galasa.cicsts.internal.properties.DseJavaHome;
import dev.galasa.cicsts.internal.properties.DseJvmProfileDir;
import dev.galasa.cicsts.internal.properties.DseUssHome;
import dev.galasa.cicsts.internal.properties.DseVersion;
import dev.galasa.cicsts.spi.BaseCicsImpl;
import dev.galasa.zos.IZosImage;
import dev.galasa.zosbatch.IZosBatchJob;
import dev.galasa.zosbatch.IZosBatchJob.JobStatus;
import dev.galasa.zosbatch.ZosBatchException;

public class DseCicsImpl extends BaseCicsImpl {

    private ProductVersion version;
	private String usshome;
	private String jvmProfileDir;
	private String javaHome;
	private IZosBatchJob regionJob;

    public DseCicsImpl(CicstsManagerImpl cicstsManager, String cicsTag, IZosImage image, String applid)
            throws CicstsManagerException {
        super(cicstsManager, cicsTag, image, applid, MasType.NONE);
    }

    @Override
    public ProductVersion getVersion() throws CicstsManagerException {
        if (this.version != null) {
            return this.version;
        }

        String versionString = DseVersion.get(this.getTag());

        if (versionString == null) {
            throw new CicstsManagerException("The version was missing for DSE tag " + this.getTag());
        }

        try {
            this.version = ProductVersion.parse(versionString);
        } catch (Exception e) {
            throw new CicstsManagerException("Invalid version string for DSE tag " + this.getTag() + ", format should be 0.0.0", e);
        }

        return this.version;
    }

    @Override
    public String getUssHome() throws CicstsManagerException {
    	if (this.usshome == null) {
    		this.usshome = DseUssHome.get(this.getTag());
    		if (this.usshome == null) {
    			throw new CicstsManagerException("A value for USSHOME was missing for DSE tag " + this.getTag());
    		}
    	}
        return this.usshome;
    }

    @Override
    public String getJvmProfileDir() throws CicstsManagerException {
    	if (this.jvmProfileDir == null) {
    		this.jvmProfileDir = DseJvmProfileDir.get(this.getTag());
    		if (this.jvmProfileDir == null) {
    			throw new CicstsManagerException("A value for JVMPROFILEDIR was missing for DSE tag " + this.getTag());
    		}
    	}
        return this.jvmProfileDir;
    }

    @Override
	public String getJavaHome() throws CicstsManagerException {
    	if (this.javaHome == null) {
    		this.javaHome = DseJavaHome.get(this);
    		if (this.javaHome == null) {
    			throw new CicstsManagerException("A value for Java home was missing for DSE tag " + this.getTag());
    		}
    	}
        return this.javaHome;
	}

	@Override
    public boolean isProvisionStart() {
        return true;  // DSE regions are assumed to be started before the test runs
    }

    @Override
    public void startup() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to startup DSE CICS TS regions");
        
    }

    @Override
    public void shutdown() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to shutdown DSE CICS TS regions");
    }

    @Override
    public void submitRuntimeJcl() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to submit DSE CICS TS regions");
    }

    @Override
    public boolean hasRegionStarted() throws CicstsManagerException {
        throw new CicstsManagerException("Unable to check DSE CICS TS regions has started");
    }

	@Override
	public IZosBatchJob getRegionJob() throws CicstsManagerException {
		if (this.regionJob == null) {
			try {
				List<IZosBatchJob> jobs = this.cicstsManager.getZosBatch(this).getJobs(getApplid(), "*");
				for (IZosBatchJob job : jobs) {
					if (job.getStatus().equals(JobStatus.ACTIVE)) {
						String jesmsglg = job.getSpoolFile("JESMSGLG").getRecords();
						Pattern pattern = Pattern.compile("DFHSI1517\\s(\\w+)");
				    	Matcher matcher = pattern.matcher(jesmsglg);
				    	if (matcher.find() && matcher.groupCount() == 1 && getApplid().equals(matcher.group(1))) {
			    			this.regionJob = job;
			    			break;
				    	}
					}
				}
			} catch (ZosBatchException e) {
				throw new CicstsManagerException("Unable to get CICS job", e);
			}
		}
		if (this.regionJob == null) {
			throw new CicstsManagerException("Unable to get DSE CICS job matching APPLID " + getApplid());
		}
		return this.regionJob;
	}
	
	@Override
	public void alterSit(@NotNull String sitParam, String sitValue) throws CicstsManagerException {
	    throw new CicstsManagerException("Alter SIT is not supported under DSE provisioning");
	}
	
	@Override
	public void removeSit(@NotNull String sitParam) throws CicstsManagerException {
		throw new CicstsManagerException("Remove SIT is not supported under DSE provisioning");
	}
}
