/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.Tags;
import dev.galasa.TestAreas;
import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.CoreManagerField;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.IResourceString;
import dev.galasa.core.manager.Logger;
import dev.galasa.core.manager.ResourceString;
import dev.galasa.core.manager.RunName;
import dev.galasa.core.manager.StoredArtifactRoot;
import dev.galasa.core.manager.TestProperty;
import dev.galasa.core.manager.TestResultProvider;
import dev.galasa.core.manager.ITestResultProvider;
import dev.galasa.core.manager.internal.gherkin.CoreStatementOwner;
import dev.galasa.framework.IResult;
import dev.galasa.framework.spi.AbstractGherkinManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.DynamicStatusStoreException;
import dev.galasa.framework.spi.GenerateAnnotatedField;
import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IDynamicStatusStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.ILoggingManager;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.ResourceUnavailableException;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.language.GalasaTest;

@Component(service = { IManager.class, IGherkinManager.class })
public class CoreManagerImpl extends AbstractGherkinManager implements ICoreManager, ILoggingManager {
    public final static String                 NAMESPACE    = "core";

	private IConfigurationPropertyStoreService cpsTest;
	private IConfidentialTextService           ctf;
	private IConfigurationPropertyStoreService cps;
	private IDynamicStatusStoreService         dss;
	
	private Class<?> testClass;
	
	private ResourceStringGenerator            resourceStringGenerator = new ResourceStringGenerator(this);


	/** 
	 * A single instance of the test result provider. 
	 * 
	 * This is shared between any Galasa test class variables which are marked with the @TestResultProvider annotation.
	 * It makes no sense having multiple implementations as they all provide the same result when asked.
	 */
	private TestResultProviderImpl testProviderSingleton = new TestResultProviderImpl();

	/*
	 * (non-Javadoc)
	 * 
	 * @see dev.galasa.framework.spi.AbstractManager#initialise(dev.galasa.framework.spi.
	 * IFramework, java.util.List, java.util.List, java.lang.Class)
	 */
	@Override
	public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
			@NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
		super.initialise(framework, allManagers, activeManagers, galasaTest);

		try {
			this.cpsTest = framework.getConfigurationPropertyService("test");
			this.cps     = framework.getConfigurationPropertyService(NAMESPACE);
			CorePropertiesSingleton.setCps(this.cps);			
		} catch (ConfigurationPropertyStoreException e) {
			throw new CoreManagerException("Unable to initialise the CPS for Core Manager",e);
		}
		try {
			this.dss = framework.getDynamicStatusStoreService(NAMESPACE);
		} catch (DynamicStatusStoreException e) {
			throw new CoreManagerException("Unable to initialise the DSS for Core Manager",e);
		}
		this.ctf = framework.getConfidentialTextService();

		if(galasaTest.isGherkin()) {
			IStatementOwner coreOwner = new CoreStatementOwner(this, cpsTest);
			IStatementOwner[] statementOwners = { coreOwner };

			if(registerStatements(galasaTest.getGherkinTest(), statementOwners)) {
				youAreRequired(allManagers, activeManagers, galasaTest);
			}
		} else {
		    this.testClass = galasaTest.getJavaTestClass();
		}

		// *** We always want the Core Manager initialised and included in the Test Run
		activeManagers.add(this);
	}

	@Override
	public void provisionGenerate() throws ManagerException, ResourceUnavailableException {
		// Make use of the inbuilt field generation routines - createResourceString & createLogField methods will be called automatically
		generateAnnotatedFields(CoreManagerField.class);
	}
	
	@Override
	public void provisionDiscard() {
		this.resourceStringGenerator.discard(); // Discard all the resource strings we generated
		
		super.provisionDiscard();
	}

	/**
	 * Generates a Log instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = Logger.class)
	public Log createLogField(Field field, List<Annotation> annotations) {
		return LogFactory.getLog(getTestClass());
	}

	/**
	 * Generates and locks a random string
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 * @throws ResourceUnavailableException 
	 */
	@GenerateAnnotatedField(annotation = ResourceString.class)
	public IResourceString createResourceString(Field field, List<Annotation> annotations) throws CoreManagerException, ResourceUnavailableException {
		// Hand control to the dedicated Resource String generator
		return this.resourceStringGenerator.generateString(field);
	}

	/**
	 * Generates a ICoreManager instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = dev.galasa.core.manager.CoreManager.class)
	public ICoreManager createICoreManager(Field field, List<Annotation> annotations) {
		return this;
	}

	/**
	 * Generates a test Property
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 * @throws ConfigurationPropertyStoreException 
	 * @throws CoreManagerException 
	 */
	@GenerateAnnotatedField(annotation = TestProperty.class)
	public String createTestproperty(Field field, List<Annotation> annotations) throws ConfigurationPropertyStoreException, CoreManagerException {

		TestProperty testPropertyAnnotation = field.getAnnotation(TestProperty.class);

		String value = nulled(this.cpsTest.getProperty(testPropertyAnnotation.prefix(), 
				testPropertyAnnotation.suffix(), 
				testPropertyAnnotation.infixes()));

		if (testPropertyAnnotation.required() && value == null) { 
			throw new CoreManagerException("Test Property missing for prefix=" 
					+ testPropertyAnnotation.prefix() 
					+ ",suffix=" 
					+ testPropertyAnnotation.suffix()
					+ ",infixes=" 
					+ String.join(",",testPropertyAnnotation.infixes()));
		}

		return value;
	}

	/**
	 * Generates a Stored Artifact Root Path instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = StoredArtifactRoot.class)
	public Path createrootPath(Field field, List<Annotation> annotations) {
		return getFramework().getResultArchiveStore().getStoredArtifactsRoot();
	}

	/**
	 * Generates a Run Name String instance for the Test Class
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = RunName.class)
	public String createRunName(Field field, List<Annotation> annotations) {
		return getRunName();
	}


	/**
	 * Generates a Test Result Provider a Test Case can query the Test Result from.
	 *
	 * @param field       The field in question
	 * @param annotations All the Manager annotations associated with the field
	 * @return The Object the field needs to be filled with
	 */
	@GenerateAnnotatedField(annotation = TestResultProvider.class)
	public ITestResultProvider createTestResultProvider(Field field, List<Annotation> annotations) {
		return testProviderSingleton;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dev.galasa.core.manager.ICoreManager#getRunName()
	 */
	@Override
	public @NotNull String getRunName() {
		return getFramework().getTestRunName();
	}

	@Override
	public ICredentials getCredentials(@NotNull String credentialsId) throws CoreManagerException {
		try {
			return getFramework().getCredentialsService().getCredentials(credentialsId);
		} catch (CredentialsException e) {
			throw new CoreManagerException("Unable to retrieve credentials for id " + credentialsId, e);
		}
	}
	
	@Override
	public ICredentialsUsernamePassword getUsernamePassword(@NotNull String credentialsId) throws CoreManagerException {
		ICredentials cred =getCredentials(credentialsId);
		if(!(cred instanceof ICredentialsUsernamePassword)) {
			throw new CoreManagerException("Unable to retrieve username/password credentials for id"+ credentialsId);
		}
		return (ICredentialsUsernamePassword) cred;
	}

	@Override
	public void registerConfidentialText(String confidentialString, String comment) {
		ctf.registerText(confidentialString, comment);
	}

    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;   // this manager does not provision resources, therefore support environments 
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
        return null;
    }

    @Override
    public String getProductRelease() {
        return null;
    }

    @Override
    public String getBuildLevel() {
        return null;
    }

    @Override
    public String getCustomBuild() {
        return null;
    }

    @Override
    public List<String> getTestingAreas() {
        if(this.testClass == null) {
            return null;
        }
        
        TestAreas annotationAreas = this.testClass.getAnnotation(TestAreas.class);
        if (annotationAreas == null || annotationAreas.value() == null) {
            return null;
        }
        
        ArrayList<String> testingAreas = new ArrayList<>();
        
        for(String testingArea : annotationAreas.value()) {
            if (testingArea == null) {
                continue;
            }
            
            testingArea = testingArea.trim();
            if (testingArea.isEmpty()) {
                continue;
            }
            
            testingAreas.add(testingArea);
        }
        
        if (testingAreas.isEmpty()) {
            return null;
        }
        
        
        return testingAreas;
    }

    @Override
    public List<String> getTags() {
        if(this.testClass == null) {
            return null;
        }
        
        Tags annotationTags = this.testClass.getAnnotation(Tags.class);
        if (annotationTags == null || annotationTags.value() == null) {
            return null;
        }
        
        ArrayList<String> tags = new ArrayList<>();
        
        for(String tag : annotationTags.value()) {
            if (tag == null) {
                continue;
            }
            
            tag = tag.trim();
            if (tag.isEmpty()) {
                continue;
            }
            
            tags.add(tag);
        }
        
        if (tags.isEmpty()) {
            return null;
        }
        
        
        return tags;
    }

	public IDynamicStatusStoreService getDss() {
		return this.dss;
	}

	@Override
	public void setResultSoFar(IResult newResult) {
		// Let the test know what the result is by setting the result into the result provider 
		// which it will have access to via a field annotated as a @TestResultProvider
		testProviderSingleton.setResult(newResult);
	}

}