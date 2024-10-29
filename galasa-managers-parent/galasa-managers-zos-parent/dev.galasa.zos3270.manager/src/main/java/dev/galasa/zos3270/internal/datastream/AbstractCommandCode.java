/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.datastream;

import dev.galasa.zos3270.spi.DatastreamException;

public abstract class AbstractCommandCode {

    public static final byte WRITE                        = (byte) 0xf1;
    public static final byte ERASE_WRITE                  = (byte) 0xf5;
    public static final byte ERASE_WRITE_ALTERNATE        = (byte) 0x7e;
    public static final byte READ_BUFFER                  = (byte) 0xf2;
    public static final byte READ_MODIFIED                = (byte) 0xf6;
    public static final byte READ_MODIFIED_ALL            = (byte) 0x6e;
    public static final byte ERASE_ALL_UNPROTECTED        = (byte) 0x6f;
    public static final byte WRITE_STRUCTURED             = (byte) 0xf3;
    public static final byte NONSNA_WRITE                 = (byte) 0x01;
    public static final byte NONSNA_ERASE_WRITE           = (byte) 0x05;
    public static final byte NONSNA_ERASE_WRITE_ALTERNATE = (byte) 0x0d;
    public static final byte NONSNA_READ_BUFFER           = (byte) 0x02;
    public static final byte NONSNA_READ_MODIFIED         = (byte) 0x06;
    public static final byte NONSNA_READ_MODIFIED_ALL     = (byte) 0x0e;
    public static final byte NONSNA_ERASE_ALL_UNPROTECTED = (byte) 0x0f;
    public static final byte NONSNA_WRITE_STRUCTURED      = (byte) 0x11;

    protected AbstractCommandCode() {
    }

    public static AbstractCommandCode getCommandCode(byte commandCode) throws DatastreamException {
        switch (commandCode) {
            case ERASE_WRITE:
            case NONSNA_ERASE_WRITE:
                return new CommandEraseWrite();
            case ERASE_WRITE_ALTERNATE:
            case NONSNA_ERASE_WRITE_ALTERNATE:
                return new CommandEraseWriteAlternate();
            case WRITE:
            case NONSNA_WRITE:
                return new CommandWrite();
            case WRITE_STRUCTURED:
            case NONSNA_WRITE_STRUCTURED:
                return new CommandWriteStructured();
            case READ_BUFFER:
            case NONSNA_READ_BUFFER:
                return new CommandReadBuffer();
            case READ_MODIFIED:
            case NONSNA_READ_MODIFIED:
                return new CommandReadModified();
            case READ_MODIFIED_ALL:
            case NONSNA_READ_MODIFIED_ALL:
                return new CommandReadModifiedAll();
            case ERASE_ALL_UNPROTECTED:
            case NONSNA_ERASE_ALL_UNPROTECTED:
                throw new DatastreamException("Unsupported command code=" + commandCode);
            default:
                throw new DatastreamException("Unrecognised command code=" + commandCode);
        }
    }

}
