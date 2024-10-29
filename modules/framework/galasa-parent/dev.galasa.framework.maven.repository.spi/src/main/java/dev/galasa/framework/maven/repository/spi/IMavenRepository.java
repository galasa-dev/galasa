/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.maven.repository.spi;

import java.net.URL;
import java.util.List;

public interface IMavenRepository {

    URL getLocalRepository();

    List<URL> getRemoteRepositories();

    void setRepositories(URL localRepository, List<URL> remoteRepositories);

    void addRemoteRepository(URL remoteRepository);

}
