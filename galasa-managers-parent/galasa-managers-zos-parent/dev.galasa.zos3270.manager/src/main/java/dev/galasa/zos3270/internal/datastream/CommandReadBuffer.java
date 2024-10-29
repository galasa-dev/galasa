/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

public class CommandReadBuffer extends AbstractCommandCode {

    public byte[] getBytes() {
        return new byte[] { READ_BUFFER };
    }

}
