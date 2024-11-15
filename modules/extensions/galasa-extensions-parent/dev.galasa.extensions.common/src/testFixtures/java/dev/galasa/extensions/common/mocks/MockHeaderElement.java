/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;

public class MockHeaderElement implements HeaderElement {

    private String name ;
    private String value;

    public MockHeaderElement(String name, String value) {
        this.name = name;
        this.value = value ;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public NameValuePair getParameter(int arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'getParameter'");
    }

    @Override
    public NameValuePair getParameterByName(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'getParameterByName'");
    }

    @Override
    public int getParameterCount() {
        throw new UnsupportedOperationException("Unimplemented method 'getParameterCount'");
    }

    @Override
    public NameValuePair[] getParameters() {
        return new NameValuePair[0];
    }

    @Override
    public String getValue() {
        return this.value;
    }
    
}
