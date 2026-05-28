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

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.maven.repository.spi.IMavenRepository;
import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;
import dev.galasa.framework.spi.streams.IStream;

public class MavenRepositoryListBuilder {

    private Log logger = LogFactory.getLog(MavenRepositoryListBuilder.class);
    private IMavenRepository mavenRepo;
    private ICredentialsService credsService;

    public MavenRepositoryListBuilder(IMavenRepository mavenRepo, ICredentialsService credsService) {
        this.mavenRepo = mavenRepo;
        this.credsService = credsService;
    }

    public void addMavenRepositories(IStream stream, String runRepositoryList) throws TestRunException {
        String testRepository = null;
        if (stream != null) {
            testRepository = stream.getMavenRepositoryUrl().toString();

            String streamCredentialsId = stream.getMavenSecretName();
            if (streamCredentialsId != null) {
                setMavenCredentials(streamCredentialsId);
            }
        } else {
            testRepository = getOverriddenValue(testRepository, runRepositoryList);
        }
        addMavenRepositories(mavenRepo, testRepository);
    }

    private void setMavenCredentials(String streamCredentialsId) throws TestRunException {
        logger.debug("Loading maven credentials with ID " + streamCredentialsId);
        
        try {
            ICredentials retrievedCreds = credsService.getCredentials(streamCredentialsId);
            if (retrievedCreds == null) {
                throw new TestRunException("Could not find credentials with ID: " + streamCredentialsId);
            }

            if (retrievedCreds instanceof ICredentialsUsernamePassword) {
                ICredentialsUsernamePassword mavenUsernamePasswordCreds = (ICredentialsUsernamePassword) retrievedCreds;
                mavenRepo.setCredentials(mavenUsernamePasswordCreds.getUsername(), mavenUsernamePasswordCreds.getPassword());
            } else {
                throw new TestRunException("Unsupported credentials type provided. Only username/password credentials are supported");
            }
        } catch (CredentialsException e) {
            throw new TestRunException("Failed to load maven credentials with ID " + streamCredentialsId, e);
        }
    }

    private String getOverriddenValue(String existingValue, String possibleOverrideValue) {
        String result = existingValue ;
        String possibleNulledValue = AbstractManager.nulled(possibleOverrideValue);
        if (possibleNulledValue != null) {
            result = possibleNulledValue;
        }
        return result ;
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



