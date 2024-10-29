/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.mocks;

import org.apache.http.HttpStatus;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;

public class MockStatusLine implements StatusLine {

    // Status code defaults to OK.
    private int code = HttpStatus.SC_OK;

    @Override
    public ProtocolVersion getProtocolVersion() {
        throw new UnsupportedOperationException("Unimplemented method 'getProtocolVersion'");
    }

    @Override
    public String getReasonPhrase() {
        throw new UnsupportedOperationException("Unimplemented method 'getReasonPhrase'");
    }

    public void setStatusCode(int newCode) {
        this.code = newCode ;
    }

    @Override
    public int getStatusCode() {
        return this.code;
    }
    
}