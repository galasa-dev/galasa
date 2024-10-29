/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.Resource;

public class MockRepository implements Repository {

    private String uri ;

    public MockRepository(String uri) {
        this.uri = uri;
    }

    @Override
    public String getURI() {
        return this.uri;
    }

    @Override
    public Resource[] getResources() {
        throw new UnsupportedOperationException("Unimplemented method 'getResources'");
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
