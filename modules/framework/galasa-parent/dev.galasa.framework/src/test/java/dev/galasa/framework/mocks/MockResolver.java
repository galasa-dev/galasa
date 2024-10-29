/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.InterruptedResolutionException;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;

public class MockResolver implements Resolver {

    List<Resource> resources = new ArrayList<Resource>();

    boolean isResolveOK ;

    public MockResolver(boolean isResolveOK) {
        this.isResolveOK = isResolveOK ;
    }

    @Override
    public void add(Resource resource) {
        this.resources.add(resource);
    }

    @Override
    public boolean resolve() throws InterruptedResolutionException {
        return this.isResolveOK;
    }

    @Override
    public Resource[] getRequiredResources() {
        Resource[] results = new Resource[resources.size()];
        int i=0;
        for (Resource r : resources) {
            results[i] = r ;
            i+=1;
        }
        return results;
    }

    @Override
    public Resource[] getOptionalResources() {
        return new Resource[0];
    }

    @Override
    public void deploy(int flags) {
        // Do nothing.
    }

    // -------------- un-implemented methods follow --------------------------

    @Override
    public Resource[] getAddedResources() {
        throw new UnsupportedOperationException("Unimplemented method 'getAddedResources'");
    }

    @Override
    public void add(Requirement requirement) {
        throw new UnsupportedOperationException("Unimplemented method 'add'");
    }

    @Override
    public Requirement[] getAddedRequirements() {
        throw new UnsupportedOperationException("Unimplemented method 'getAddedRequirements'");
    }

    @Override
    public void addGlobalCapability(Capability capability) {
        throw new UnsupportedOperationException("Unimplemented method 'addGlobalCapability'");
    }

    @Override
    public Capability[] getGlobalCapabilities() {
        throw new UnsupportedOperationException("Unimplemented method 'getGlobalCapabilities'");
    }

    @Override
    public boolean resolve(int flags) throws InterruptedResolutionException {
        throw new UnsupportedOperationException("Unimplemented method 'resolve'");
    }





    @Override
    public Reason[] getReason(Resource resource) {
        throw new UnsupportedOperationException("Unimplemented method 'getReason'");
    }

    @Override
    public Reason[] getUnsatisfiedRequirements() {
        throw new UnsupportedOperationException("Unimplemented method 'getUnsatisfiedRequirements'");
    }


}
