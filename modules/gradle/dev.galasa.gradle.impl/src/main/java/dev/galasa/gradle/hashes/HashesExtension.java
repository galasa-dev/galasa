/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.hashes;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;

public class HashesExtension {
    
    @Inject
    public HashesExtension(ObjectFactory objectFactory) {
    }

    public String gitHash;
    
    public void setGitGash(String gitHash) {
        this.gitHash = gitHash;
    }
    
}
