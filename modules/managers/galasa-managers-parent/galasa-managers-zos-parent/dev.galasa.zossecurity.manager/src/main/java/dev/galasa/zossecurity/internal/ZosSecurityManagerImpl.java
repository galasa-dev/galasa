/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zossecurity.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.ResourceManagerException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosManagerException;
import dev.galasa.zos.spi.IZosManagerSpi;
import dev.galasa.zosfile.spi.IZosFileSpi;
import dev.galasa.zossecurity.IZosCicsClassSet;
import dev.galasa.zossecurity.IZosPreDefinedProfile;
import dev.galasa.zossecurity.IZosSecurity;
import dev.galasa.zossecurity.IZosUserid;
import dev.galasa.zossecurity.ZosCicsClassSet;
import dev.galasa.zossecurity.ZosPreDefinedProfile;
import dev.galasa.zossecurity.ZosSecurity;
import dev.galasa.zossecurity.ZosSecurityManagerException;
import dev.galasa.zossecurity.ZosUserid;
import dev.galasa.zossecurity.internal.properties.ZosSecurityPropertiesSingleton;
import dev.galasa.zossecurity.internal.resourcemanagement.ZosSecurityResourceManagement;
import dev.galasa.zossecurity.spi.IZosSecurityManagerSpi;

@Component(service = { IManager.class })
public class ZosSecurityManagerImpl extends AbstractManager implements IZosSecurityManagerSpi {

	private static final Log logger = LogFactory.getLog(ZosSecurityManagerImpl.class);
	
	public static final String NAMESPACE = "zossecurity";
	
    private IDynamicStatusStoreService dss;

	private final Map<String, ZosSecurityImpl> zosSecuritys = new HashMap<>();

	private final Map<String, ZosSecurityImpl> taggedZosSecuritys = new HashMap<>();
	
    private IZosManagerSpi zosManager;

	private IZosFileSpi zosFileManager;

	private IHttpManagerSpi httpManager;

    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.IFramework, java.util.List, java.util.List, java.lang.Class)
     */
    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);
        try {
            this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
            ZosSecurityPropertiesSingleton.setCps(framework.getConfigurationPropertyService(NAMESPACE));
        } catch (Exception e) {
            throw new ZosSecurityManagerException("Unable to request framework services", e);
        }

        if(galasaTest.isJava()) {
            List<AnnotatedField> zosSecurityFields = findAnnotatedFields(ZosSecurityField.class);
            List<AnnotatedField> zosCicsClassSetFields = findAnnotatedFields(ZosCicsClassSetField.class);
            List<AnnotatedField> zosPreDefinedProfileFields = findAnnotatedFields(ZosPreDefinedProfileField.class);
            List<AnnotatedField> zosUseridFields = findAnnotatedFields(ZosUseridField.class);
            if (!(zosSecurityFields.isEmpty() && zosCicsClassSetFields.isEmpty() && zosPreDefinedProfileFields.isEmpty() && zosUseridFields.isEmpty())) {
                youAreRequired(allManagers, activeManagers, galasaTest);
            }
        }
    }
	
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        if (activeManagers.contains(this)) {
            return;
        }

        activeManagers.add(this);
        this.zosManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
        if (zosManager == null) {
            throw new ZosSecurityManagerException("The zOS Manager is not available");
        }
        this.zosFileManager = addDependentManager(allManagers, activeManagers, galasaTest, IZosFileSpi.class);
        if (this.zosFileManager == null) {
            throw new ZosSecurityManagerException("The zOS File Manager is not available");
        }
        this.httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new ZosSecurityManagerException("The HTTP Manager is not available");
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * dev.galasa.framework.spi.IManager#areYouProvisionalDependentOn(dev.galasa.framework.spi.IManager)
     */
    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        return otherManager instanceof IZosManagerSpi;
    }
    
    @Override
	public @NotNull IZosSecurity getZosSecurity(IZosImage image) throws ZosSecurityManagerException {
		String imageID = image.getImageID();
		
	    // Have we already generated this image ID
	    if (this.zosSecuritys.containsKey(imageID)) {
	        return this.zosSecuritys.get(imageID);
	    }
	
	    ZosSecurityImpl zosSecurity = new ZosSecurityImpl(this, image);
	    this.zosSecuritys.put(imageID, zosSecurity);
	    
		return zosSecurity;
	}

	private ZosSecurityImpl getZosSecurity(String tag) throws ZosSecurityManagerException {
        // Have we already generated this tag
        if (this.taggedZosSecuritys.containsKey(tag)) {
            return this.taggedZosSecuritys.get(tag);
        }

        IZosImage image = null;
		try {
			image = this.zosManager.getImageForTag(tag);
		} catch (ZosManagerException e) {
			throw new ZosSecurityManagerException("Unable to get zOS Image", e);
		}
        ZosSecurityImpl zosSecurity = new ZosSecurityImpl(this, image);
        this.taggedZosSecuritys.put(tag, zosSecurity);
        
		return zosSecurity;
	}
	
    /* (non-Javadoc)
     * @see dev.galasa.framework.spi.AbstractManager#provisionGenerate()
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        generateAnnotatedFields(ZosSecurityField.class);
        generateAnnotatedFields(ZosUseridField.class);
        generateAnnotatedFields(ZosPreDefinedProfileField.class);
        generateAnnotatedFields(ZosCicsClassSetField.class);
    }
    
	@GenerateAnnotatedField(annotation=ZosSecurity.class)
    public IZosSecurity generateZosSecurity(Field field, List<Annotation> annotations) throws ZosSecurityManagerException {
		ZosSecurity annotationZosSecurity = field.getAnnotation(ZosSecurity.class);
        
        // Default the tag to primary
        String tag = defaultString(annotationZosSecurity.imageTag(), "PRIMARY").toUpperCase();

		return getZosSecurity(tag);
    }

    @GenerateAnnotatedField(annotation=ZosUserid.class)
	public IZosUserid generateZosUserid(Field field, List<Annotation> annotations) throws ZosSecurityManagerException {
		ZosUserid annotationZosUserid = field.getAnnotation(ZosUserid.class);
        
        // Default the tag to primary
        String tag = defaultString(annotationZosUserid.imageTag(), "PRIMARY").toUpperCase();
        boolean runUser = annotationZosUserid.runUser();
        
        //TODO: Do something with these properties 
//      String setSymbolic = annotationZosUserid.setSymbolic();
//      String ensZosClient = annotationZosUserid.ensZosClient();
        
		return getZosSecurity(tag).allocateUserid(runUser);
	}

	@GenerateAnnotatedField(annotation=ZosPreDefinedProfile.class)
	public IZosPreDefinedProfile generateZosPreDefinedProfile(Field field, List<Annotation> annotations) throws ZosSecurityManagerException {
		ZosPreDefinedProfile annotationZosPreDefinedProfile = field.getAnnotation(ZosPreDefinedProfile.class);
        
        // Default the tag to primary
        String tag = defaultString(annotationZosPreDefinedProfile.imageTag(), "PRIMARY").toUpperCase();
        String className = annotationZosPreDefinedProfile.classname();
        String profile = annotationZosPreDefinedProfile.profile();
		return ((ZosSecurityImpl) getZosSecurity(tag)).createPredefinedProfile(className, profile);
	}

	@GenerateAnnotatedField(annotation=ZosCicsClassSet.class)
    public IZosCicsClassSet generateZosCicsClassSet(Field field, List<Annotation> annotations) throws ZosSecurityManagerException {
		ZosCicsClassSet annotationZosCicsClassSet = field.getAnnotation(ZosCicsClassSet.class);
        
        // Default the tag to primary
        String tag = defaultString(annotationZosCicsClassSet.imageTag(), "PRIMARY").toUpperCase();
        boolean allowAllAccess = annotationZosCicsClassSet.allowAllAccess();
        boolean shared = annotationZosCicsClassSet.shared();
		return getZosSecurity(tag).allocateCicsClassSet(allowAllAccess, shared);
    }

	/* (non-Javadoc)
	 * 
	 * @see dev.galasa.framework.spi.IManager#endOfTestRun()
	 */
	@Override
	public void endOfTestRun() {
		try {
			ZosSecurityResourceManagement zosSecurityResourceManagement = new ZosSecurityResourceManagement();
			zosSecurityResourceManagement.setHttpManager(this.httpManager);
			zosSecurityResourceManagement.initialise(getFramework(), null);
			zosSecurityResourceManagement.runFinishedOrDeleted(getFramework().getTestRunName());
		} catch (ResourceManagerException e) {
			logger.warn("Problem during end of test run clean up", e);
		}
	}

	public IDynamicStatusStoreService getDss() {
		return this.dss;
	}

	public IZosManagerSpi getZosManager() {
		return this.zosManager;
	}

	public IZosFileSpi getZosFileManager() {
		return this.zosFileManager;
	}

	public IHttpManagerSpi getHttpManager() {
		return this.httpManager;
	}
}
