/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.IGenericEcosystem;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.spi.IZosManagerSpi;

public abstract class AbstractEcosystemImpl implements IInternalEcosystem, IGenericEcosystem {
    
    private final Log                        logger = LogFactory.getLog(AbstractEcosystemImpl.class);

    private final GalasaEcosystemManagerImpl manager;
    private final String                     tag;
    private IJavaInstallation                javaInstallation;
    private String                           defaultZosImageTag;

    public AbstractEcosystemImpl(GalasaEcosystemManagerImpl manager, 
            String tag,
            IJavaInstallation javaInstallation,
            String            defaultZosImageTag) {
        this.manager           = manager;
        this.tag               = tag;
        this.javaInstallation  = javaInstallation;
        this.defaultZosImageTag = defaultZosImageTag;
    }

    @Override
    public String getTag() {
        return this.tag;
    }

    protected GalasaEcosystemManagerImpl getEcosystemManager() {
        return this.manager;
    }

    public abstract ICommandShell getCommandShell() throws GalasaEcosystemManagerException;

    protected IJavaInstallation getJavaInstallation() {
        return this.javaInstallation;
    }
    
    @Override
    public void build() throws GalasaEcosystemManagerException {
        if (this.defaultZosImageTag != null && !this.defaultZosImageTag.isEmpty()) {
            try {
                IZosImage zosImage = this.manager.getZosManager().getImageForTag(this.defaultZosImageTag);
                addZosImageToCpsAsDefault(zosImage);
            } catch(Exception e) {
                throw new GalasaEcosystemManagerException("Unable to set the default zos image tagged " + this.defaultZosImageTag);
            }
        }
        
    }
    
    @Override
    public void setZosImageDseTag(@NotNull String tag, @NotNull IZosImage image) throws GalasaEcosystemManagerException {
        setCpsProperty("zos.dse.tag." + tag + ".imageid", image.getImageID());
    }
    
    @Override
    public void setZosClusterImages(@NotNull String clusterId, @NotNull IZosImage... images)
            throws GalasaEcosystemManagerException {
        StringBuilder sb = new StringBuilder();
        for(IZosImage image : images) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(image.getImageID());
        }
        
        setCpsProperty("zos.cluster." + clusterId + ".images", sb.toString());
    }
    
    
    @Override
    public void addZosImageToCpsAsDefault(@NotNull IZosImage image) throws GalasaEcosystemManagerException {
        addZosImageToCps(image);
        setZosClusterImages("DEFAULT", image);
    }


    @Override
    public void addZosImageToCps(IZosImage image) throws GalasaEcosystemManagerException {

        //**********************************************************************************
        //**********************************************************************************
        //**********************************************************************************
        //***                                                                            ***
        //***              D O    N O T                                                  ***
        //***                                                                            ***
        //***  Do not follow this code as an example of how to write a Galasa Manager.   ***
        //***                                                                            ***
        //***  This code breaks all the rules of namespace separation, BUT,              ***
        //***  as this Manager is testing Galasa itself, we have to do it                ***
        //***  this way and is very specific to this use case.                           ***
        //***                                                                            ***
        //**********************************************************************************
        //**********************************************************************************
        //**********************************************************************************
        try {
            IZosManagerSpi zosManager = this.manager.getZosManager();

            copyImage(image);
            
            IFramework framework = this.manager.getFramework();
            IConfigurationPropertyStoreService zosMfCps = framework.getConfigurationPropertyService("zosmf");                
            
            //*** Check for a zosmf definition for the image,  via the sysplex
            String sysplexId = image.getSysplexID();
            
            String sysplexZosmfs = zosMfCps.getProperty("sysplex." + sysplexId, "default.servers");
            if (sysplexZosmfs != null) {
                String[] zosmfImageIds = sysplexZosmfs.split(",");
                for(String zosmfImageId : zosmfImageIds) {
                    zosmfImageId = zosmfImageId.trim();
                    if (!zosmfImageId.isEmpty()) {
                        IZosImage zosImage = zosManager.getUnmanagedImage(zosmfImageId);
                        if (zosImage != image) {
                            copyImage(zosImage);
                        }
                        
                        
                        String port = zosMfCps.getProperty("server." + zosmfImageId, "port");
                        if (port != null) {
                            setCpsProperty("zosmf.server." + zosmfImageId + ".port", port);
                        }
                        
                        String https = zosMfCps.getProperty("server." + zosmfImageId, "https");
                        if (https != null) {
                            setCpsProperty("zosmf.server." + zosmfImageId + ".https", https);
                        }
                        
                        logger.info("zOS/MF server " + zosmfImageId + " copied to ecosystem");
                    }
                }
                
                setCpsProperty("zosmf.sysplex." + sysplexId + ".default.servers", sysplexZosmfs);
            }
            
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem copying zos properties to ecosystem",e);
        }
    }
    
    
    private void copyImage(IZosImage image) throws GalasaEcosystemManagerException {
        //**********************************************************************************
        //**********************************************************************************
        //**********************************************************************************
        //***                                                                            ***
        //***              D O    N O T                                                  ***
        //***                                                                            ***
        //***  Do not follow this code as an example of how to write a Galasa Manager.   ***
        //***                                                                            ***
        //***  This code breaks all the rules of namespace separation, BUT,              ***
        //***  as this Manager is testing Galasa itself, we have to do it                ***
        //***  this way and is very specific to this use case.                           ***
        //***                                                                            ***
        //**********************************************************************************
        //**********************************************************************************
        //**********************************************************************************
        try {
            IIpHost imageHost = image.getIpHost();
            
            String zosPrefix = "zos.image." + image.getImageID() + ".";
            
            setCpsProperty(zosPrefix + "default.hostname", imageHost.getHostname());  
            String ipv4Hostname = imageHost.getIpv4Hostname();
            if (ipv4Hostname != null) {
                setCpsProperty(zosPrefix + "ipv4.hostname", imageHost.getIpv4Hostname());
            }
            String ipv6Hostname = imageHost.getIpv4Hostname();
            if (ipv6Hostname != null) {
                setCpsProperty(zosPrefix + "ipv6.hostname", imageHost.getIpv6Hostname());
            }
            
            setCpsProperty(zosPrefix + "telnet.port", Integer.toString(imageHost.getTelnetPort()));
            setCpsProperty(zosPrefix + "ftp.port", Integer.toString(imageHost.getFtpPort()));
            setCpsProperty(zosPrefix + "ssh.port", Integer.toString(imageHost.getSshPort()));
            
            setCpsProperty(zosPrefix + "telnet.tls", Boolean.toString(imageHost.isTelnetPortTls()));  
            setCpsProperty(zosPrefix + "ftp.tls", Boolean.toString(imageHost.isFtpPortTls()));  
            setCpsProperty(zosPrefix + "max.slots", "1");  
            
            String sysplex = image.getSysplexID();
            if (sysplex != null && !sysplex.equals(image.getImageID())) {
                setCpsProperty(zosPrefix + "sysplex", sysplex);
            }
            
            ICredentials creds = image.getDefaultCredentials();
            if (!(creds instanceof ICredentialsUsernamePassword)) {
                throw new GalasaEcosystemManagerException("Unsupported credentials type - " + creds.getClass().getName());
            }
            ICredentialsUsernamePassword usernamePassword = (ICredentialsUsernamePassword) creds;
            
            String credsId = "ZOS" + image.getImageID() + "COPIED";
            setCpsProperty(zosPrefix + "credentials", credsId);
            
            setCredsProperty("secure.credentials." + credsId + ".username", usernamePassword.getUsername());
            setCredsProperty("secure.credentials." + credsId + ".password", usernamePassword.getPassword());
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Problem copying zos image properties to ecosystem",e);
        }
        
        logger.info("zOS Image " + image.getImageID() + " copied to ecosystem");
    }
    
    /**
     * Ideally we would like runs within the child ecosystem to have a similar run ID to the parent
     * So we extract the current run ID - strip off the prefix and decrement it by 1
     * @throws GalasaEcosystemManagerException
     */
    protected void insertLastRunIDIntoDSS() throws GalasaEcosystemManagerException {
    	//regex to extract the numeric part of a runID i.e RX109 will give us 109 in a group
    	Pattern p = Pattern.compile("[A-Za-z]*([0-9]+)");
    	Matcher m = p.matcher(this.manager.getFramework().getTestRunName());
    	int run = 0;
    	if (m.matches()) {
    	    run = Integer.parseInt(m.group(1));
    	} else {
    	    run = new SecureRandom().nextInt(1000);
    	}
    	String newRunNumber = ""+--run;
    	logger.info("inserting lastUsed runID into DSS with value: " + newRunNumber);
    	setDssProperty("dss.framework.request.prefix.U.lastused", newRunNumber);
    }

}
