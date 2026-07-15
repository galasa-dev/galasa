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
    private String uri;

    public MockRepository(Resource[] resources) {
        this(resources, "mock://repository");
    }

    public MockRepository(Resource[] resources, String uri) {
        this.resources = resources;
        this.uri = uri;
    }

    @Override
    public String getName() {
        return "mock-repository";
    }

    @Override
    public String getURI() {
        return uri;
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
