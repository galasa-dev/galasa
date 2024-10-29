/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class OrderText extends AbstractOrder {

    private final Charset ebcdic;

    private StringBuilder        text   = new StringBuilder();

    public OrderText(Charset codePage) {
        this.ebcdic = codePage;
    }

    public OrderText(String newText, Charset codePage) {
        this.ebcdic = codePage;
        this.text.append(newText);
    }

    public void append(byte data) {
        if (data == -1) {
            data = 0x00;
        }

        byte[] charByte = new byte[] { data };
        text.append(ebcdic.decode(ByteBuffer.wrap(charByte)).array()[0]);
    }

    @Override
    public String toString() {
        return "TEXT(" + text.toString() + ")";
    }

    public String getText() {
        return text.toString();
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException("Needs to be written");
    }

}
