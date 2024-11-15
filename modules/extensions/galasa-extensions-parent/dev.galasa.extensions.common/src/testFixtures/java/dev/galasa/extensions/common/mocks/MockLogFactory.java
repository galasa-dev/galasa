/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks;


import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;

import dev.galasa.extensions.common.api.LogFactory;

// Creates Log objects, which can be used to log trace and error output.
// All the log objects direct their output back to this log factory.
// You can get the log output using the toString() method.
public class MockLogFactory implements LogFactory {

    private ByteArrayOutputStream stream;
    private PrintWriter writer ;

    public MockLogFactory() {
        this.stream = new ByteArrayOutputStream();
        this.writer = new PrintWriter(stream);
    }

    @Override
    public Log getLog(Class<?> clazz) {
        return new MockLog(this.writer,clazz); 
    }

    @Override
    public String toString() {
        return this.stream.toString();
    }
}
