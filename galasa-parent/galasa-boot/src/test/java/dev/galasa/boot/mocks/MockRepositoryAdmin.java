/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import java.net.URL;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.InvalidSyntaxException;

public class MockRepositoryAdmin implements RepositoryAdmin {

    private MockResolver resolver;

    public MockRepositoryAdmin(MockResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public Resource[] discoverResources(String filterExpr) throws InvalidSyntaxException {
        String resourceName = filterExpr.replace("(symbolicname=", "").replace(")", "");
        MockResource mockResource = new MockResource(resourceName, resourceName, "uri");
        return new Resource[] { mockResource };
    }

    @Override
    public Resolver resolver() {
        return resolver;
    }

    @Override
    public Resource[] discoverResources(Requirement[] requirements) {
        throw new UnsupportedOperationException("Unimplemented method 'discoverResources'");
    }

    @Override
    public Resolver resolver(Repository[] repositories) {
        throw new UnsupportedOperationException("Unimplemented method 'resolver'");
    }

    @Override
    public Repository addRepository(String repository) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'addRepository'");
    }

    @Override
    public Repository addRepository(URL repository) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'addRepository'");
    }

    @Override
    public boolean removeRepository(String repository) {
        throw new UnsupportedOperationException("Unimplemented method 'removeRepository'");
    }

    @Override
    public Repository[] listRepositories() {
        throw new UnsupportedOperationException("Unimplemented method 'listRepositories'");
    }

    @Override
    public Repository getSystemRepository() {
        throw new UnsupportedOperationException("Unimplemented method 'getSystemRepository'");
    }

    @Override
    public Repository getLocalRepository() {
        throw new UnsupportedOperationException("Unimplemented method 'getLocalRepository'");
    }

    @Override
    public DataModelHelper getHelper() {
        throw new UnsupportedOperationException("Unimplemented method 'getHelper'");
    }
}
