/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import org.apache.hc.core5.http.Header;

public class MockHttpHeader implements Header {

    private String name;
    private String value;

    public MockHttpHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isSensitive() {
        return false;
    }

}
