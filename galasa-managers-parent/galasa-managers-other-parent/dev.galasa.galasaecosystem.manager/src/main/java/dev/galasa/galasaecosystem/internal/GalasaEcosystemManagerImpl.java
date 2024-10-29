/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.galasaecosystem.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.Gson;

import dev.galasa.ManagerException;
import dev.galasa.artifact.IArtifactManager;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.AnnotatedField;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ILoggingManager;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.InsufficientResourcesAvailableException;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.SharedEnvironmentRunType;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerException;
import dev.galasa.galasaecosystem.GalasaEcosystemManagerField;
import dev.galasa.galasaecosystem.IKubernetesEcosystem;
import dev.galasa.galasaecosystem.ILocalEcosystem;
import dev.galasa.galasaecosystem.IsolationInstallation;
import dev.galasa.galasaecosystem.KubernetesEcosystem;
import dev.galasa.galasaecosystem.LocalEcosystem;
import dev.galasa.galasaecosystem.internal.properties.DockerVersion;
import dev.galasa.galasaecosystem.internal.properties.GalasaEcosystemPropertiesSingleton;
import dev.galasa.galasaecosystem.internal.properties.KubernetesEcosystemTagSharedEnvironment;
import dev.galasa.galasaecosystem.internal.properties.RuntimeVersion;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.java.IJavaInstallation;
import dev.galasa.java.JavaManagerException;
import dev.galasa.java.spi.IJavaManagerSpi;
import dev.galasa.java.ubuntu.spi.IJavaUbuntuManagerSpi;
import dev.galasa.java.windows.spi.IJavaWindowsManagerSpi;
import dev.galasa.kubernetes.IKubernetesNamespace;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.spi.IKubernetesManagerSpi;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxManagerException;
import dev.galasa.linux.spi.ILinuxManagerSpi;
import dev.galasa.windows.IWindowsImage;
import dev.galasa.windows.WindowsManagerException;
import dev.galasa.windows.spi.IWindowsManagerSpi;
import dev.galasa.zos.spi.IZosManagerSpi;

/**
 * The Galasa Ecosystem Manager
 * 
 *  
 *
 */
@Component(service = { IManager.class })
public class GalasaEcosystemManagerImpl extends AbstractManager implements ILoggingManager {

    public final static String               NAMESPACE = "galasaecosystem";
    private final Log                           logger = LogFactory.getLog(getClass());
    private IDynamicStatusStoreService          dss;
    private final GalasaGson                          gson = new GalasaGson();

    private IArtifactManager                    artifactManager;
    private IHttpManagerSpi                     httpManager;
    private IKubernetesManagerSpi               k8sManager;
    private ILinuxManagerSpi                    linuxManager;
    private IWindowsManagerSpi                  windowsManager;
    private IJavaManagerSpi                     javaManager;
    private IZosManagerSpi                      zosManager;

    private boolean                             required           = false;
    private boolean                             requiresK8s        = false;
    private boolean                             requiresDocker     = false;
    private boolean                             requiresLocal      = false;
    private boolean                             requiresStandalone = false;
    private boolean                             requiresLinux      = false;
    private boolean                             requiresWindows    = false;
    private boolean                             requiresZos        = false;

    private HashMap<String, IInternalEcosystem> taggedEcosystems = new HashMap<>();
    private HashSet<String> sharedEnvironmentEcosystemTags = new HashSet<>();

    /**
     * Initialise the Manager if the Ecosystem TPI is included in the test class
     */
    @Override
    public void initialise(@NotNull IFramework framework, 
            @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, 
            @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        if(galasaTest.isJava()) {
            //*** Check to see if we are needed
            if (!required) {
                calculateWhichEcosystemsAreRequired();

                if (requiresK8s || requiresDocker || requiresLocal || requiresStandalone) {
                    required = true;
                }
            }

            if (!required) {
                return;
            }
        }

        youAreRequired(allManagers, activeManagers, galasaTest);

        try {
            GalasaEcosystemPropertiesSingleton.setCps(getFramework().getConfigurationPropertyService(NAMESPACE));
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Failed to set the CPS with the Galasa Ecosystem namespace", e);
        }

        try {
            this.dss = this.getFramework().getDynamicStatusStoreService(NAMESPACE);
        } catch(DynamicStatusStoreException e) {
            throw new GalasaEcosystemManagerException("Unable to provide the DSS for the Galasa Ecosystem Manager", e);
        }

        this.logger.info("Galasa Ecosystem Manager initialised");
    }

    private void calculateWhichEcosystemsAreRequired() {
        List<AnnotatedField> fields = findAnnotatedFields(GalasaEcosystemManagerField.class);

        for(AnnotatedField field : fields) {
            if (field.getField().getType() == IKubernetesEcosystem.class) {
                requiresK8s = true;
            }
            if (field.getField().getType() == ILocalEcosystem.class) {
                requiresLocal = true;

                LocalEcosystem localEcosystem = field.getField().getAnnotation(LocalEcosystem.class);
                if (localEcosystem != null) {
                    if (localEcosystem.linuxImageTag() != null && !localEcosystem.linuxImageTag().trim().isEmpty()) {
                        this.requiresLinux = true;
                    }
                    if (localEcosystem.windowsImageTag() != null && !localEcosystem.windowsImageTag().trim().isEmpty()) {
                        this.requiresWindows = true;
                    }
                    if (localEcosystem.addDefaultZosImage() != null && !localEcosystem.addDefaultZosImage().trim().isEmpty()) {
                        this.requiresZos = true;
                    }
                }
            }
        }
    }

    /**
     * This or another manager has indicated that this manager is required
     */
    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {
        super.youAreRequired(allManagers, activeManagers, galasaTest);

        if (activeManagers.contains(this)) {
            return;
        }

        if (!required) {
            calculateWhichEcosystemsAreRequired();
        }

        this.required = true;

        //*** Add dependent managers
        if (this.requiresK8s) {
            this.k8sManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IKubernetesManagerSpi.class);
            if (this.k8sManager == null) {
                throw new GalasaEcosystemManagerException("Unable to locate the Kubernetes Manager");
            }
        }

        if (this.requiresLinux) {
            this.linuxManager = this.addDependentManager(allManagers, activeManagers, galasaTest, ILinuxManagerSpi.class);
            if (this.linuxManager == null) {
                throw new GalasaEcosystemManagerException("Unable to locate the Linux Manager");
            }
        }

        if (this.requiresWindows) {
            this.windowsManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IWindowsManagerSpi.class);
            if (this.windowsManager == null) {
                throw new GalasaEcosystemManagerException("Unable to locate the Windows Manager");
            }
        }

        if (this.requiresLocal) {
            this.javaManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IJavaManagerSpi.class);
            if (this.javaManager == null) {
                throw new GalasaEcosystemManagerException("Unable to locate the Java Manager");
            }
        }

        if (this.requiresZos) {
            this.zosManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IZosManagerSpi.class);
            if (this.zosManager == null) {
                throw new GalasaEcosystemManagerException("Unable to locate the zOS Manager");
            }
        }


        this.artifactManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IArtifactManager.class);
        if (this.artifactManager == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Artifact Manager");
        }

        this.httpManager = this.addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
        if (this.httpManager == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Http Manager");
        }

        activeManagers.add(this);
    }

    @Override
    public boolean areYouProvisionalDependentOn(@NotNull IManager otherManager) {
        if (otherManager == k8sManager) {
            return true;
        }
        if (otherManager == linuxManager) {
            return true;
        }
        if (otherManager == windowsManager) {
            return true;
        }
        if (otherManager == javaManager) {
            return true;
        }
        if (otherManager == zosManager) {
            return true;
        }
        if (otherManager instanceof IJavaUbuntuManagerSpi) {
            return true;
        }
        if (otherManager instanceof IJavaWindowsManagerSpi) {
            return true;
        }

        return super.areYouProvisionalDependentOn(otherManager);
    }

    @Override
    public boolean doYouSupportSharedEnvironments() {

        if (this.requiresDocker
                || this.requiresLinux
                || this.requiresWindows
                || this.requiresLocal
                || this.requiresStandalone) {
            return false;
        }

        return true;
    }

    /**
     * Generate all the annotated fields, uses the standard generate by method mechanism
     */
    @Override
    public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
        if (getFramework().getTestRun().isSharedEnvironment()) {
            logger.info("Manager running in Shared Environment setup");
        }

        //*** Shared Environment Discard processing
        try {
            if (getFramework().getSharedEnvironmentRunType() == SharedEnvironmentRunType.DISCARD) {
                KubernetesEcosystemImpl.loadEcosystemsFromRun(this, dss, taggedEcosystems, getFramework().getTestRun());
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new KubernetesManagerException("Unable to determine Shared Environment phase", e);
        }


        generateAnnotatedFields(GalasaEcosystemManagerField.class);
    }

    /**
     * Generate a Galasa Kubernetes Ecosystem
     * 
     * @param field The test field
     * @param annotations any annotations with the ecosystem
     * @return a {@link IKubernetesEcosystem} ecosystem
     * @throws InsufficientResourcesAvailableException 
     * @throws KubernetesManagerException if there is a problem generating a ecosystem
     */
    @GenerateAnnotatedField(annotation = KubernetesEcosystem.class)
    public IKubernetesEcosystem generateKubernetesEcosystem(Field field, List<Annotation> annotations) throws GalasaEcosystemManagerException, InsufficientResourcesAvailableException {
        KubernetesEcosystem annotation = field.getAnnotation(KubernetesEcosystem.class);

        String tag = annotation.ecosystemNamespaceTag().trim().toUpperCase();
        if (tag.isEmpty()) {
            tag = "PRIMARY";
        }

        String namespaceTag = annotation.kubernetesNamespaceTag().trim().toUpperCase();
        if (namespaceTag.isEmpty()) {
            namespaceTag = "PRIMARY";
        }

        //*** Check to see if we already have it
        IInternalEcosystem ecosystem = this.taggedEcosystems.get(tag);
        if (ecosystem != null) {
            if (!(ecosystem instanceof IKubernetesEcosystem)) {
                throw new GalasaEcosystemManagerException("Tag " + tag + " is being used for multiple types of Ecosystems");
            }
            return (IKubernetesEcosystem)ecosystem;
        }

        try {
            if (getFramework().getSharedEnvironmentRunType() == SharedEnvironmentRunType.DISCARD) {
                throw new GalasaEcosystemManagerException("Attempt to generate a new Ecosystem during Shared Environment discard");
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new GalasaEcosystemManagerException("Unable to determine Shared Environment phase", e);
        }


        //*** check to see if the tag is a shared environment
        String sharedEnvironmentRunName = KubernetesEcosystemTagSharedEnvironment.get(tag);
        if (sharedEnvironmentRunName != null) {
            try {
                IRun sharedEnvironmentRun = getFramework().getFrameworkRuns().getRun(sharedEnvironmentRunName);

                if (sharedEnvironmentRun == null || !sharedEnvironmentRun.isSharedEnvironment()) {
                    throw new GalasaEcosystemManagerException("Unable to locate Shared Environment " + sharedEnvironmentRunName + " for Ecosystem Tag " + tag);
                }

                HashMap<String, IInternalEcosystem> tempSharedEnvironmentNamespaces = new HashMap<>();
                KubernetesEcosystemImpl.loadEcosystemsFromRun(this, dss, tempSharedEnvironmentNamespaces, sharedEnvironmentRun);

                ecosystem = tempSharedEnvironmentNamespaces.get(tag);
                if (ecosystem == null) {
                    if (!(ecosystem instanceof IKubernetesEcosystem)) {
                        throw new GalasaEcosystemManagerException("Tag " + tag + " is is not a Kubernetes Ecosystem");
                    }
                    throw new GalasaEcosystemManagerException("Unable to locate Shared Environment " + sharedEnvironmentRunName + " for Ecosystem Tag " + tag);
                }

                this.taggedEcosystems.put(tag, ecosystem);
                this.sharedEnvironmentEcosystemTags.add(tag);

                logger.info("Kubernetes Ecosystem tag " + tag + " is using Shared Environment " + sharedEnvironmentRunName);

                return (IKubernetesEcosystem)ecosystem;
            } catch(FrameworkException e) {
                throw new GalasaEcosystemManagerException("Problem loading Shared Environment " + sharedEnvironmentRunName + " for Ecosystem Tag " + tag, e);
            }
        } 

        //*** Locate the Kubernetes Namespace object

        IKubernetesNamespace namespace = k8sManager.getNamespaceByTag(namespaceTag);
        if (namespace == null) {
            throw new GalasaEcosystemManagerException("Unable to locate the Kubernetes Namespace tagged " + namespaceTag);
        }

        KubernetesEcosystemImpl k8sEcosystem = new KubernetesEcosystemImpl(this, tag, namespace);
        k8sEcosystem.reserveRunIdPrefix();
        taggedEcosystems.put(tag, k8sEcosystem);

        try {
            k8sEcosystem.loadYamlResources();
        } catch(Exception e) {
            throw new GalasaEcosystemManagerException("Unable to provision Kubernetes Ecosystem " + tag, e);
        }

        logger.info("Allocated Galasa Kubernetes Ecosystem on Kubernetes Namespace " + namespace.getFullId() + " for tag " + tag);

        return k8sEcosystem;
    }

    /**
     * Generate a Galasa Local Ecosystem
     * 
     * @param field The test field
     * @param annotations any annotations with the ecosystem
     * @return a {@link IKubernetesEcosystem} ecosystem
     * @throws LogConfigurationException 
     * @throws InsufficientResourcesAvailableException 
     * @throws KubernetesManagerException if there is a problem generating a ecosystem
     */
    @GenerateAnnotatedField(annotation = LocalEcosystem.class)
    public ILocalEcosystem generateLocalEcosystem(Field field, List<Annotation> annotations) throws GalasaEcosystemManagerException, InsufficientResourcesAvailableException {
        LocalEcosystem annotation = field.getAnnotation(LocalEcosystem.class);

        String tag = annotation.ecosystemTag().trim().toUpperCase();
        if (tag.isEmpty()) {
            tag = "PRIMARY";
        }

        //*** Check to see if we already have it
        IInternalEcosystem ecosystem = this.taggedEcosystems.get(tag);
        if (ecosystem != null) {
            if (!(ecosystem instanceof ILocalEcosystem)) {
                throw new GalasaEcosystemManagerException("Tag " + tag + " is being used for multiple types of Ecosystems");
            }
            return (ILocalEcosystem)ecosystem;
        }

        //*** Currently, this Manager does not support shared environments for local ecosystems

        //*** locate the Java Installation

        String javaTag = annotation.javaInstallationTag().trim().toUpperCase();
        if (javaTag.isEmpty()) {
            javaTag = "PRIMARY";
        }

        IJavaInstallation javaInstallation = null;
        try {
            javaInstallation = this.javaManager.getInstallationForTag(javaTag);
        } catch(JavaManagerException e) {
            throw new GalasaEcosystemManagerException("Problem locating Java installation for Ecosystem tag " + tag, e);
        }
        
        IsolationInstallation isolationInstallation = annotation.isolationInstallation();
        if (isolationInstallation == null) {
            isolationInstallation = IsolationInstallation.None;
        }

        LocalEcosystemImpl localEcosystem = null;

        //*** check which OS we are deploying to
        String linuxImageTag = annotation.linuxImageTag().trim().toUpperCase();
        String windowsImageTag = annotation.windowsImageTag().trim().toUpperCase();

        if (!linuxImageTag.isEmpty() && !windowsImageTag.isEmpty()) {
            throw new GalasaEcosystemManagerException("Galasa Ecosystem tag " + tag + " references both a Linux and Windows image tag");
        }
        if (linuxImageTag.isEmpty() && windowsImageTag.isEmpty()) {
            throw new GalasaEcosystemManagerException("Galasa Ecosystem tag " + tag + " does not refere to either a Linux and Windows image tag");
        }
        
        if (!linuxImageTag.isEmpty()) {
            try {
                ILinuxImage linuxImage = this.linuxManager.getImageForTag(linuxImageTag);
                localEcosystem = new LocalLinuxEcosystemImpl(this, tag, linuxImage, javaInstallation, isolationInstallation, annotation.startSimPlatform(), annotation.addDefaultZosImage());
            } catch (LinuxManagerException e) {
                throw new GalasaEcosystemManagerException("Problem locating Linux image for Ecosystem tag " + tag, e);
            }
        } else if (!windowsImageTag.isEmpty()) {
            try {
                IWindowsImage windowsImage = this.windowsManager.getImageForTag(windowsImageTag);
                localEcosystem = new LocalWindowsEcosystemImpl(this, tag, windowsImage, javaInstallation, isolationInstallation, annotation.startSimPlatform(), annotation.addDefaultZosImage());
            } catch (WindowsManagerException e) {
                throw new GalasaEcosystemManagerException("Problem locating Windows image for Ecosystem tag " + tag, e);
            }
        }

        taggedEcosystems.put(tag, localEcosystem);

        return localEcosystem;
    }

    @Override
    public void provisionBuild() throws ManagerException, ResourceUnavailableException {
        super.provisionBuild();

        for(IInternalEcosystem ecosystem : taggedEcosystems.values()) {
            try {
                if (this.sharedEnvironmentEcosystemTags.contains(ecosystem.getTag())) {
                    continue; //*** Do not build shared environments
                }
                ecosystem.build();
            } catch(Exception e) {
                logger.error("Problem building provisioned Ecosystem " + ecosystem.getTag(),e);

                throw new GalasaEcosystemManagerException("Problem building the Ecosystem", e);
            }
        }
    }

    @Override
    public void provisionStop() {
        for(IInternalEcosystem ecosystem : taggedEcosystems.values()) {
            if (this.sharedEnvironmentEcosystemTags.contains(ecosystem.getTag())) {
                continue;   //dont stop shared environments
            }
            try {
                ecosystem.stop();
            } catch(Exception e) {
                logger.error("Problem stopping provisioned Ecosystem " + ecosystem.getTag(),e);
            }
        }

        super.provisionStop();
    }

    @Override
    public void provisionDiscard() {
        for(IInternalEcosystem ecosystem : taggedEcosystems.values()) {
            if (this.sharedEnvironmentEcosystemTags.contains(ecosystem.getTag())) {
                continue;   //dont discard shared environments
            }
            try {
                ecosystem.discard();
            } catch(Exception e) {
                logger.error("Problem discarding provisioned Ecosystem " + ecosystem.getTag(),e);
            }
        }

        super.provisionDiscard();
    }

    protected IArtifactManager getArtifactManager() {
        return this.artifactManager;
    }

    protected IZosManagerSpi getZosManager() {
        return this.zosManager;
    }

    protected IHttpManagerSpi getHttpManager() {
        return this.httpManager;
    }

    protected IKubernetesManagerSpi getKubernetesManager() {
        return this.k8sManager;
    }

    protected ILinuxManagerSpi getLinuxManager() {
        return this.linuxManager;
    }

    protected IWindowsManagerSpi getWindowsManager() {
        return this.windowsManager;
    }

    public Gson getGson() {
        return this.gson.getGson();
    }

    public IDynamicStatusStoreService getDss() {
        return this.dss;
    }

    @Override
    public String getTestTooling() {
        return null;
    }

    @Override
    public String getTestType() {
        return null;
    }

    @Override
    public String getTestingEnvironment() {
        return "galasa:" + getBuildLevel();
    }

    @Override
    public String getProductRelease() {
        try {
            return "galasa:" + RuntimeVersion.get();
        } catch (GalasaEcosystemManagerException e) {
            return "galasa:unknown";
        }
    }

    @Override
    public String getBuildLevel() {
        try {
            return DockerVersion.get();
        } catch (GalasaEcosystemManagerException e) {
            return "unknown";
        }
    }

    @Override
    public String getCustomBuild() {
        return null;
    }

    @Override
    public List<String> getTestingAreas() {
        return null;
    }

    @Override
    public List<String> getTags() {
        return null;
    }

}
