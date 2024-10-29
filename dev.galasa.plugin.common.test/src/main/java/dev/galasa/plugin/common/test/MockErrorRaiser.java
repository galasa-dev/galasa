/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.test;

import java.text.*;

import dev.galasa.plugin.common.ErrorRaiser;

public class MockErrorRaiser implements ErrorRaiser<MockException> {

    private MockLog log ;

    public MockErrorRaiser(MockLog log) {
        this.log = log;
    }

    @Override
    public void raiseError(String template, Object... parameters) throws MockException {
        String msg = MessageFormat.format(template, parameters);
        log.error(msg);
        throw new MockException(msg);
    }

    @Override
    public void raiseError(Throwable cause, String template, Object... parameters) throws MockException {
        String msg = MessageFormat.format(template, parameters);
        log.error(msg);
        throw new MockException(msg,cause);
    }
    
}
