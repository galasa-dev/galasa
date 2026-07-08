/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resource;

public class MockRepository implements Repository {

    private Resource[] resources;

    public MockRepository(Resource[] resources) {
        this.resources = resources;
    }

    @Override
    public String getName() {
        return "mock-repository";
    }

    @Override
    public String getURI() {
        return "mock://repository";
    }

    @Override
    public Resource[] getResources() {
        return resources;
    }

    @Override
    public long getLastModified() {
        return 0L;
    }
}
