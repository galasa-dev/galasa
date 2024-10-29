/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.kubernetes.internal.resources;

import dev.galasa.kubernetes.ISecret;
import dev.galasa.kubernetes.KubernetesManagerException;
import dev.galasa.kubernetes.internal.KubernetesNamespaceImpl;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.Yaml;

/**
 * Secret implementation
 * 
 *  
 *
 */
public class SecretImpl implements ISecret {
    
    private final V1Secret secret;

    public SecretImpl(KubernetesNamespaceImpl namespace, V1Secret secret) {
        this.secret = secret;
    }

    @Override
    public String getName() {
        return secret.getMetadata().getName();
    }

    @Override
    public TYPE getType() {
        return TYPE.Secret;
    }

    @Override
    public String getYaml() {
        return Yaml.dump(this.secret);
    }
    
    @Override
    public void refresh() throws KubernetesManagerException {
       throw new UnsupportedOperationException("Not developed yet"); //TODO
    }

}
