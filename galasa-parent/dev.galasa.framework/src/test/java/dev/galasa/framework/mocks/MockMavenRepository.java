/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.framework.maven.repository.spi.IMavenRepository;

public class MockMavenRepository implements IMavenRepository {

    List<URL> remoteRepoUrls = new ArrayList<>();

    @Override
    public URL getLocalRepository() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocalRepository'");
    }

    @Override
    public List<URL> getRemoteRepositories() {
        return remoteRepoUrls;
    }

    @Override
    public void setRepositories(URL localRepository, List<URL> remoteRepositories) {
        throw new UnsupportedOperationException("Unimplemented method 'setRepositories'");
    }

    @Override
    public void addRemoteRepository(URL remoteRepository) {
        remoteRepoUrls.add(remoteRepository);
    }
    
}
