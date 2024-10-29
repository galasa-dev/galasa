/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.osgi.framework.launch.Framework;

import dev.galasa.boot.felix.FelixFramework;

public class MockFelixFramework extends FelixFramework {

    public MockFelixFramework(Framework framework, RepositoryAdmin repositoryAdmin) {
        super.framework = framework;
        super.repositoryAdmin = repositoryAdmin;
    }
}
