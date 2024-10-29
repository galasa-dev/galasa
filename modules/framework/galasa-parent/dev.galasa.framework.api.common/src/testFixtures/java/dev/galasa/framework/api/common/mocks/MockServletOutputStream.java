/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class MockServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream outputStream;

    public MockServletOutputStream() {
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

    @Override
    public void write(int b) throws IOException {
        this.outputStream.write(b);
    }

    @Override
    public String toString() {
        return outputStream.toString();
    }
}
