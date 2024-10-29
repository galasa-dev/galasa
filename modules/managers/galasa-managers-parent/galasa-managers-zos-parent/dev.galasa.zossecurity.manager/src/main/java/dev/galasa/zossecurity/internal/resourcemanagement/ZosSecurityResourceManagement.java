/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal.resourcemanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IResourceManagement;
import dev.galasa.framework.spi.IResourceManagementProvider;
import dev.galasa.framework.spi.ResourceManagerException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zossecurity.internal.ZosSecurityImpl;
import dev.galasa.zossecurity.internal.ZosSecurityManagerImpl;
import dev.galasa.zossecurity.internal.properties.ZosSecurityPropertiesSingleton;

@Component(service = { IResourceManagementProvider.class })
public class ZosSecurityResourceManagement implements IResourceManagementProvider {
	
	private IHttpManagerSpi httpManager;
	
    private IFramework framework;
    private IResourceManagement resourceManagement;
    private IDynamicStatusStoreService dss;

    private ZosCertificateResourceManagement zosCertificateResourceManagement;
    private ZosIdMapResourceManagement zosIdMapResourceManagement;
    private ZosKerberosPrincipalResourceManagement zosKerberosPrincipalResourceManagement;
    private ZosKeyringResourceManagement zosKeyringResourceManagement;
    private ZosPredefinedProfilePermitResourceManagement zosPredefinedProfilePermitResourceManagement;
    private ZosProfileResourceManagement zosProfileResourceManagement;
    private ZosUseridResourceManagement zosUseridResourceManagement;
    private ZosCicsClassSetResourceManagement zosCicsClassSetResourceManagement;

    @Override
    public boolean initialise(IFramework framework, IResourceManagement resourceManagement) throws ResourceManagerException {  
        this.framework = framework;
        this.resourceManagement = resourceManagement;
        initialisetHttpManager();        
        ZosSecurityImpl zosSecurtityImpl;
        try {
            this.dss = this.framework.getDynamicStatusStoreService(ZosSecurityManagerImpl.NAMESPACE);
            ZosSecurityPropertiesSingleton.setCps(this.framework.getConfigurationPropertyService(ZosSecurityManagerImpl.NAMESPACE));
            zosSecurtityImpl = new ZosSecurityImpl(framework, dss, httpManager);
        } catch (Exception e) {
            throw new ResourceManagerException("Unable to initialise zOS Security resource manager", e);
        }
        
        zosCertificateResourceManagement = new ZosCertificateResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);
        zosIdMapResourceManagement = new ZosIdMapResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);
        zosKerberosPrincipalResourceManagement = new ZosKerberosPrincipalResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);
        zosKeyringResourceManagement = new ZosKeyringResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);
        zosPredefinedProfilePermitResourceManagement = new ZosPredefinedProfilePermitResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);
        zosProfileResourceManagement = new ZosProfileResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);
        zosUseridResourceManagement = new ZosUseridResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);
        zosCicsClassSetResourceManagement = new ZosCicsClassSetResourceManagement(zosSecurtityImpl, framework, resourceManagement, dss);

        return true;
    }

    private void initialisetHttpManager() throws ResourceManagerException {
    	if (httpManager == null) {
	        String classString = "dev.galasa.framework.spi.IManager";
	        String filterString = "(" + Constants.OBJECTCLASS + "=" + classString + ")";
	        ServiceReference<?>[] serviceReferences;
	        try {
	            serviceReferences = FrameworkUtil.getBundle(this.getClass()).getBundleContext().getAllServiceReferences(classString, filterString);
	        } catch (InvalidSyntaxException e) {
	            throw new ResourceManagerException("Unable to get Manager service references", e);
	        }
	        List<IManager> allManagers = new ArrayList<>();
	        for (ServiceReference<?> serviceReference : serviceReferences) {
	        	IManager manager = (IManager) serviceReference.getBundle().getBundleContext().getService(serviceReference);
	            allManagers.add(manager);
	            if (serviceReference.getBundle().getSymbolicName().equals("dev.galasa.http.manager")) {
	            	httpManager = (IHttpManagerSpi) manager;
	            }
	        }
	        if (httpManager == null) {
	        	throw new ResourceManagerException("Failed to the HTTP Manager");
	        }   	
	    	try {
				((IManager) httpManager).initialise(framework, allManagers, new ArrayList<>(), new GalasaTest(this.getClass()));
			} catch (ManagerException e) {
				throw new ResourceManagerException("Unable to initialise the HTTP Manager", e);
			}
    	}
    }

	@Override
    public void start() {
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosCertificateResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosKeyringResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosKerberosPrincipalResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosIdMapResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosPredefinedProfilePermitResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosProfileResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosUseridResourceManagement,
        		this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
        this.resourceManagement.getScheduledExecutorService().scheduleWithFixedDelay(this.zosCicsClassSetResourceManagement,
                this.framework.getRandom().nextInt(20), 20, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void runFinishedOrDeleted(String runName) {
        this.zosCertificateResourceManagement.runFinishedOrDeleted(runName);
        this.zosKeyringResourceManagement.runFinishedOrDeleted(runName);
        this.zosKerberosPrincipalResourceManagement.runFinishedOrDeleted(runName);
        this.zosIdMapResourceManagement.runFinishedOrDeleted(runName);
        this.zosPredefinedProfilePermitResourceManagement.runFinishedOrDeleted(runName);
        this.zosProfileResourceManagement.runFinishedOrDeleted(runName);
        this.zosUseridResourceManagement.runFinishedOrDeleted(runName);
        this.zosCicsClassSetResourceManagement.runFinishedOrDeleted(runName);
    }

    public void setHttpManager(IHttpManagerSpi httpManager) {
    	this.httpManager = httpManager;
    }
}
