/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.test;

public class MockException extends Exception {
    
    public MockException(String msg) {
        super(msg);
    }
    public MockException(String msg, Throwable cause) {
        super(msg,cause);
    }

}
