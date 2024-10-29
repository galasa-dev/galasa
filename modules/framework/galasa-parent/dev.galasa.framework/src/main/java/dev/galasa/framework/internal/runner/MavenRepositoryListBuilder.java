/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.internal.runner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.net.MalformedURLException;
import java.net.URL;

import dev.galasa.framework.TestRunException;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;

public class MavenRepositoryListBuilder {

    private Log logger = LogFactory.getLog(MavenRepositoryListBuilder.class);
    private IMavenRepository mavenRepo;
    private IConfigurationPropertyStoreService cps;

    public MavenRepositoryListBuilder(IMavenRepository mavenRepo, IConfigurationPropertyStoreService cps) {
        this.mavenRepo = mavenRepo; 
        this.cps = cps;
    }

    public  void addMavenRepositories( String streamName , String runRepositoryList) throws TestRunException {
        String testRepository = getTestRepositoryUrlFromStream(streamName);
        testRepository = getOverriddenValue(testRepository, runRepositoryList);
        addMavenRepositories(mavenRepo, testRepository);
    }

    private String getOverriddenValue(String existingValue, String possibleOverrideValue) {
        String result = existingValue ;
        String possibleNulledValue = AbstractManager.nulled(possibleOverrideValue);
        if (possibleNulledValue != null) {
            result = possibleNulledValue;
        }
        return result ;
    }

    private String getTestRepositoryUrlFromStream(String streamName) throws TestRunException {
        String testRepository = null ;
        if (streamName != null) {
            logger.debug("Loading test stream " + streamName);
            try {
                testRepository = this.cps.getProperty("test.stream", "repo", streamName);
            } catch (Exception e) {
                throw new TestRunException("Unable to load stream " + streamName + " settings", e);
            }
        }
        return testRepository;
    }

    private void addMavenRepositories(IMavenRepository mavenRepo, String testRepository) throws TestRunException {
        if (testRepository != null) {
            logger.debug("Loading test maven repository " + testRepository);
            try {
                String[] repos = testRepository.split("\\,");
                for(String repo : repos) {
                    repo = repo.trim();
                    if (!repo.isEmpty()) {
                        mavenRepo.addRemoteRepository(new URL(repo));
                    }
                }
            } catch (MalformedURLException e) {
                logger.error("Unable to add remote maven repository " + testRepository, e);
                throw new TestRunException("Unable to add remote maven repository " + testRepository, e);
            }
        }
    }
}



