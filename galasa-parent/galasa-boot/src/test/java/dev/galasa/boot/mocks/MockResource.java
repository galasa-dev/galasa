/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot.mocks;

import java.util.Map;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Version;

public class MockResource implements Resource {

    private String id;
    private String symbolicName;
    private String uri;

    public MockResource(String id, String symbolicName, String uri) {
        this.id = id;
        this.symbolicName = symbolicName;
        this.uri = uri;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    @Override
    public String getURI() {
        return uri;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map getProperties() {
        throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public Version getVersion() {
        throw new UnsupportedOperationException("Unimplemented method 'getVersion'");
    }

    @Override
    public String getPresentationName() {
        throw new UnsupportedOperationException("Unimplemented method 'getPresentationName'");
    }

    @Override
    public Long getSize() {
        throw new UnsupportedOperationException("Unimplemented method 'getSize'");
    }

    @Override
    public String[] getCategories() {
        throw new UnsupportedOperationException("Unimplemented method 'getCategories'");
    }

    @Override
    public Capability[] getCapabilities() {
        throw new UnsupportedOperationException("Unimplemented method 'getCapabilities'");
    }

    @Override
    public Requirement[] getRequirements() {
        throw new UnsupportedOperationException("Unimplemented method 'getRequirements'");
    }

    @Override
    public boolean isLocal() {
        throw new UnsupportedOperationException("Unimplemented method 'isLocal'");
    }
}
