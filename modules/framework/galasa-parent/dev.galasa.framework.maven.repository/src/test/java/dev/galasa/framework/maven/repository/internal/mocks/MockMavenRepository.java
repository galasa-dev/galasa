/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.maven.repository.internal.mocks;

import java.net.URL;
import java.util.List;

import dev.galasa.framework.maven.repository.spi.IMavenRepository;

/**
 * Mock implementation of IMavenRepository for testing
 */
public class MockMavenRepository implements IMavenRepository {
    private String username;
    private String password;

    @Override
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return (username != null && !username.isEmpty()) ? username : null;
    }

    @Override
    public String getPassword() {
        return (password != null && !password.isEmpty()) ? password : null;
    }

    @Override
    public URL getLocalRepository() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocalRepository'");
    }

    @Override
    public List<URL> getRemoteRepositories() {
        throw new UnsupportedOperationException("Unimplemented method 'getRemoteRepositories'");
    }

    @Override
    public void setRepositories(URL localRepository, List<URL> remoteRepositories) {
        throw new UnsupportedOperationException("Unimplemented method 'setRepositories'");
    }

    @Override
    public void addRemoteRepository(URL remoteRepository) {
        throw new UnsupportedOperationException("Unimplemented method 'addRemoteRepository'");
    }
}
