/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.bundlerepository.DataModelHelper;
import org.apache.felix.bundlerepository.Repository;
import org.apache.felix.bundlerepository.RepositoryAdmin;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.InvalidSyntaxException;

public class MockRepositoryAdmin implements RepositoryAdmin {

    private Resolver resolver ;

    private Map<String,Repository> repositoryURLMap = new HashMap<String,Repository>();

    public MockRepositoryAdmin(List<Repository> repositories, Resolver resolver) {
        for( Repository repo : repositories) {
            String key = repo.getURI().toString();
            repositoryURLMap.put(key,repo);
        }
        this.resolver = resolver;
    }

    @Override
    public Repository[] listRepositories() {
        return repositoryURLMap.values().toArray(new Repository[0]);
    }

    @Override
    public Repository addRepository(String repository) throws Exception {
        MockRepository mockRepository = new MockRepository(repository);
        this.repositoryURLMap.put(repository, mockRepository);
        return repositoryURLMap.get(repository);
    }

    @Override
    public Repository addRepository(URL repositoryUrl) throws Exception {
        String repositoryUrlStr = repositoryUrl.toString();
        MockRepository mockRepository = new MockRepository(repositoryUrlStr);
        this.repositoryURLMap.put(repositoryUrlStr, mockRepository);
        return repositoryURLMap.get(repositoryUrlStr);
    }

    @Override
    public Resolver resolver() {
        return resolver;
    }

    // ----------------- un-implemented methdos follow --------------------

    @Override
    public Resource[] discoverResources(String filterExpr) throws InvalidSyntaxException {
        throw new UnsupportedOperationException("Unimplemented method 'discoverResources'");
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
    public boolean removeRepository(String repository) {
        throw new UnsupportedOperationException("Unimplemented method 'removeRepository'");
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
