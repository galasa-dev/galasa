/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.testcatalog;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;

public class DeployTestCatalogExtension {
    
    @Inject
    public DeployTestCatalogExtension(ObjectFactory objectFactory) {
    }

    public String bootstrap;
    public String stream;
    public String token;
    
    public void setStream(String stream) {
        this.stream = stream;
    }
    
    public void setBootstrap(String bootstrap) {
        this.bootstrap = bootstrap;
    }

    public void setToken(String galasaToken) {
        this.token = galasaToken;
        if (this.token!=null) {
            this.token = this.token.trim();
        }
    }

    public String getToken() {
        String token = this.token;
        return token;
    }
    
}
