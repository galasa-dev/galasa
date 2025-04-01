/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.List;
import java.util.Map;

import org.apache.felix.bundlerepository.Capability;
import org.apache.felix.bundlerepository.Requirement;
import org.apache.felix.bundlerepository.Resource;
import org.osgi.framework.Version;

public class MockResource implements Resource {
    
     private String uri;
     private String symbolicName;
     private List<Capability> capabilities;

     public MockResource(String uri) {
          this.uri = uri ;
     }

     @Override
     public String getURI() {
          return this.uri;
     }

     @Override
     public String getSymbolicName() {
          return this.symbolicName;
     }

     public void setSymbolicName(String symbolicName) {
          this.symbolicName = symbolicName;
     }

     @Override
     public Capability[] getCapabilities() {
          return this.capabilities.toArray(new Capability[0]);
     }

     public void setCapabilities(List<Capability> capabilities) {
          this.capabilities = capabilities;
     }

    // -------------- un-implemented methods follow --------------------------

    @SuppressWarnings("rawtypes")
    @Override
    public Map getProperties() {
         throw new UnsupportedOperationException("Unimplemented method 'getProperties'");
    }

    @Override
    public String getId() {
         throw new UnsupportedOperationException("Unimplemented method 'getId'");
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
    public Requirement[] getRequirements() {
         throw new UnsupportedOperationException("Unimplemented method 'getRequirements'");
    }

    @Override
    public boolean isLocal() {
         throw new UnsupportedOperationException("Unimplemented method 'isLocal'");
    }

}
