/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.maven.repository.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.maven.repository.spi.IMavenRepository;

@Component
public class GalasaMavenRepository implements IMavenRepository {

    private URL       localRepository;
    private List<URL> remoteRepositories = new ArrayList<URL>();

    @Override
    public URL getLocalRepository() {
        return this.localRepository;
    }

    @Override
    public List<URL> getRemoteRepositories() {
        return this.remoteRepositories;
    }

    @Override
    public void setRepositories(URL localRepository, List<URL> remoteRepositories) {
        this.localRepository = localRepository;
        this.remoteRepositories.addAll(remoteRepositories);
    }

    @Override
    public void addRemoteRepository(URL remoteRepository) {
        this.remoteRepositories.add(0, remoteRepository);
    }
}
