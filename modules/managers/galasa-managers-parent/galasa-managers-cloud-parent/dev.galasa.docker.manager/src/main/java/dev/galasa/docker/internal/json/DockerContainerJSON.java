/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.docker.internal.json;

/**
 * A simplified JSON parsing object to parse the docker API containers list for names and ID
 */
public class DockerContainerJSON{
    private String Id;
    private String[] Names;
    private DockerContainerLabels Labels;

    public void setId(String Id) {
        this.Id = Id;
    }
    public String getId() {
        return this.Id;
    }

    public void setNames(String[] Names) {
        this.Names = Names;
    }
    public String[] getNames() {
        return this.Names;
    }

    public void setLabels(DockerContainerLabels Labels) {
        this.Labels = Labels;
    }
    public DockerContainerLabels getLabels() {
        return this.Labels;
    }

}

