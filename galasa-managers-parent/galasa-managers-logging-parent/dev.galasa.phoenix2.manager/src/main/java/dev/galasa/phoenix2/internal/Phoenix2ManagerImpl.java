/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.phoenix2.internal;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.ManagerException;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IFramework;
import dev.galasa.framework.spi.ILoggingManager;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.http.HttpClientResponse;
import dev.galasa.http.IHttpClient;
import dev.galasa.http.spi.IHttpManagerSpi;
import dev.galasa.phoenix2.internal.properties.Phoenix2Credentials;
import dev.galasa.phoenix2.internal.properties.Phoenix2DefaultBuildLevel;
import dev.galasa.phoenix2.internal.properties.Phoenix2DefaultCustomBuild;
import dev.galasa.phoenix2.internal.properties.Phoenix2DefaultProductRelease;
import dev.galasa.phoenix2.internal.properties.Phoenix2DefaultTestingEnvironment;
import dev.galasa.phoenix2.internal.properties.Phoenix2Enabled;
import dev.galasa.phoenix2.internal.properties.Phoenix2Endpoint;
import dev.galasa.phoenix2.internal.properties.Phoenix2LocalRun;
import dev.galasa.phoenix2.internal.properties.Phoenix2PropertiesSingleton;

/**
 * ElasticLog Manager implementation
 * 
 *  
 */
@Component(service = { IManager.class })
public class Phoenix2ManagerImpl extends AbstractManager {

    private static final Log					logger			= LogFactory.getLog(Phoenix2ManagerImpl.class);
    public final static String					NAMESPACE		= "phoenix2";

    private IFramework							framework;
    private IConfigurationPropertyStoreService	cps;

    private IHttpManagerSpi						httpManager;

    private List<IManager>                      activeManagers;

    private GalasaTest                          galasaTest;

    private Instant                             start;

    private boolean                             active;

    @Override
    public void initialise(@NotNull IFramework framework, @NotNull List<IManager> allManagers,
            @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest) throws ManagerException {
        super.initialise(framework, allManagers, activeManagers, galasaTest);

        try {
            this.framework = framework;
            this.cps = framework.getConfigurationPropertyService(NAMESPACE);
            Phoenix2PropertiesSingleton.setCps(this.cps);
        } catch (Exception e) {
            throw new Phoenix2ManagerException("Unable to request framework services", e);
        }

        if (Phoenix2Enabled.get() && (!framework.getTestRun().isLocal() || Phoenix2LocalRun.get())) {
            youAreRequired(allManagers, activeManagers, galasaTest);
        }

        this.galasaTest = galasaTest;
        this.start = Instant.now();
    }

    @Override
    public void youAreRequired(@NotNull List<IManager> allManagers, @NotNull List<IManager> activeManagers, @NotNull GalasaTest galasaTest)
            throws ManagerException {

        if (activeManagers.contains(this)) {
            return;
        }
        activeManagers.add(this);

        this.active = true;

        this.activeManagers = activeManagers;

        httpManager = addDependentManager(allManagers, activeManagers, galasaTest, IHttpManagerSpi.class);
    }

    @Override
    public boolean doYouSupportSharedEnvironments() {
        return true;
    }

    @Override
    public void testClassResult(@NotNull String finalResult, Throwable finalException) throws ManagerException {

        if (!active) {
            return;
        }

        try {
            if (getFramework().getSharedEnvironmentRunType() != null) {
                return; // do not emit events for shared environments
            }
        } catch (ConfigurationPropertyStoreException e) {
            throw new Phoenix2ManagerException("Problem checking for shared environment", e);
        }

        if ("Ignored".equals(finalResult)) {
            return;
        }

        if ("EnvFail".equals(finalResult)) {
            finalResult = "ENVIRONMENT_FAILURE";
        }

        // Collect all the data

        String testClassName = null;
        if (this.galasaTest.isJava()) {
            testClassName = this.galasaTest.getJavaTestClass().getName();
        } else if (this.galasaTest.isGherkin()) {
            testClassName = this.galasaTest.getGherkinTest().getName();
        } else {
            testClassName = "unknown";
        }

        String testTooling        = "Galasa";
        String testType           = "Galasa";
        String testingEnvironment = Phoenix2DefaultTestingEnvironment.get();
        String productRelease     = Phoenix2DefaultProductRelease.get();
        String buildLevel         = Phoenix2DefaultBuildLevel.get();
        String customBuild        = Phoenix2DefaultCustomBuild.get();
        List<String> testingAreas = new ArrayList<String>();
        List<String> tags         = new ArrayList<String>();

        //Ask other managers for additional logging information
        for(IManager manager : this.activeManagers) {
            if(manager instanceof ILoggingManager) {
                ILoggingManager loggingManager = (ILoggingManager) manager;

                String tooling = loggingManager.getTestTooling();
                if(tooling != null)
                    testTooling = tooling;

                String type = loggingManager.getTestType();
                if(type != null)
                    testType = type;

                String environment = loggingManager.getTestingEnvironment();
                if(environment != null)
                    testingEnvironment = environment;

                String release = loggingManager.getProductRelease();
                if(release != null)
                    productRelease = release;

                String level = loggingManager.getBuildLevel();
                if(level != null)
                    buildLevel = level;

                String custom = loggingManager.getCustomBuild();
                if(custom != null)
                    customBuild = custom;

                List<String> areas = loggingManager.getTestingAreas();
                if(areas != null)
                    testingAreas.addAll(areas);

                List<String> tagList = loggingManager.getTags();
                if(tagList != null)
                    tags.addAll(tagList);
            }
        }
        if (testingAreas.isEmpty())
            testingAreas = null;

        if (tags.isEmpty())
            tags = null;

        UUID execid = UUID.randomUUID();

        try {
            JsonObject testexec = new JsonObject();
            testexec.addProperty("execid", execid.toString());
            testexec.addProperty("testcase", testClassName);
            testexec.addProperty("runid", getFramework().getTestRunName());
            testexec.addProperty("whitelistColour", "");
            testexec.addProperty("testTooling", testTooling);
            testexec.addProperty("testType", testType);
            testexec.addProperty("result", finalResult.toUpperCase());
            testexec.addProperty("testingEnvironment", testingEnvironment);
            testexec.addProperty("productRelease", productRelease);
            testexec.addProperty("buildLevel", buildLevel);
            testexec.addProperty("customBuild", customBuild);
            testexec.addProperty("requestor", this.framework.getTestRun().getRequestor());
            testexec.add("startTimestamp", jsonDate(this.start));
            testexec.add("endTimestamp", jsonDate(Instant.now()));
            testexec.addProperty("execInfo", "RunID(" + getFramework().getTestRunName() + ") RunUUID(" + execid + ")");

            if (tags != null) {
                testexec.add("tags", convertList(tags));
            }
            if (testingAreas != null) {
                testexec.add("testingArea", convertList(testingAreas));
            }


            logger.trace("Sending json to PME -\n" + testexec.toString());

            URI endpoint = new URI(Phoenix2Endpoint.get());
            String credsId  = Phoenix2Credentials.get();

            ICredentials creds = getFramework().getCredentialsService().getCredentials(credsId);

            //Set up http client for requests
            IHttpClient client = this.httpManager.newHttpClient();
            client.setURI(endpoint);

            if(creds != null && creds instanceof ICredentialsUsernamePassword) {
                ICredentialsUsernamePassword userPass = (ICredentialsUsernamePassword) creds;
                String user = userPass.getUsername();
                String pass = userPass.getPassword();
                client.setAuthorisation(user, pass);
            }


            HttpClientResponse<String> response = client.postText(endpoint.getPath(), testexec.toString());
            if (response.getStatusCode() != 200) {
                throw new Phoenix2ManagerException("Error sending PME record - " + response.getStatusLine() + "\n" + response.getContent()); 
            }
            
            logger.debug("Emitted Phoenix 2 PME record");
        } catch(Exception e) {
            logger.warn("Problem writing DevOps PME testcaseexecution JSON event ",e);
        }
    }   


    private JsonArray convertList(List<String> list) {

        JsonArray newArray = new JsonArray(list.size());

        for(String value : list) {
            newArray.add(value);
        }

        return newArray;
    }

    private JsonObject jsonDate(Instant date) {
        if (date == null) {
            return null;
        }

        JsonObject dateObject = new JsonObject();
        dateObject.addProperty("$date", date.toString());

        return dateObject;
    }


}