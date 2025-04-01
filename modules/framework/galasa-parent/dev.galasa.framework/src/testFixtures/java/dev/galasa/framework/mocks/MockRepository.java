/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resource;

public class MockRepository implements Repository {

    private String uri;
    private List<Resource> resources;

    public MockRepository(String uri) {
        this.uri = uri;
        this.resources = new ArrayList<>();
    }

    @Override
    public String getURI() {
        return this.uri;
    }

    @Override
    public Resource[] getResources() {
        return this.resources.toArray(new Resource[0]);
    }

    public void addResource(Resource resource) {
        this.resources.add(resource);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Unimplemented method 'getName'");
    }

    @Override
    public long getLastModified() {
        throw new UnsupportedOperationException("Unimplemented method 'getLastModified'");
    }
    
}
