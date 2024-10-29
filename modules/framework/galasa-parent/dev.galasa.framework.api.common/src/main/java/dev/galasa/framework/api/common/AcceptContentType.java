/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

/**
 * A representation of a content type as given in a HTTP "Accept" header.
 * For example: "Accept: application/json;q=0.5".
 *
 * For more information on the "Accept" header, see https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Accept
 */
public class AcceptContentType {
    private String type;
    private double quality;

    public AcceptContentType(String type, double quality) {
        this.type = type;
        this.quality = quality;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }
}
