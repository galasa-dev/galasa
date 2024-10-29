/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.mocks;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;

public class MockHttpHeader implements Header {

    private String name ;
    private String value ;

    public MockHttpHeader(String name, String value) {
        this.name = name ;
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
    public HeaderElement[] getElements() throws ParseException {
        HeaderElement[] elements = new HeaderElement[1];

        elements[0] = new MockHeaderElement(this.name, this.value);
        return elements;
    }

}
