/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.mocks;

import java.util.Map;

import dev.galasa.framework.spi.IConfidentialTextService;

public class MockConfidentialTextStore implements IConfidentialTextService {

    // private Map<String, String> confidentialTextProps;

    public MockConfidentialTextStore(Map<String, String> confidentialTextProps) {
        // this.confidentialTextProps = confidentialTextProps;
    }

    @Override
    public void registerText(String confidentialString, String comment) {
        throw new UnsupportedOperationException("Unimplemented method 'registerText'");
    }

    @Override
    public String removeConfidentialText(String text) {
        throw new UnsupportedOperationException("Unimplemented method 'removeConfidentialText'");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

}