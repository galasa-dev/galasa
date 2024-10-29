/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import java.util.ArrayList;
import java.util.List;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.InterruptedResolutionException;
import org.apache.felix.bundlerepository.Reason;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resolver;
import org.apache.felix.bundlerepository.Resource;

public class MockResolver implements Resolver {

    private List<Resource> resources = new ArrayList<>();

    @Override
    public void add(Resource resource) {
        resources.add(resource);
    }

    @Override
    public boolean resolve() throws InterruptedResolutionException {
        return true;
    }

    @Override
    public Resource[] getRequiredResources() {
        return resources.toArray(new Resource[0]);
    }

    @Override
    public Resource[] getOptionalResources() {
        return new Resource[0];
    }

    @Override
    public void deploy(int flags) {
        // Do nothing...
    }

    public List<Resource> getAllResources() {
        return resources;
    }

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
