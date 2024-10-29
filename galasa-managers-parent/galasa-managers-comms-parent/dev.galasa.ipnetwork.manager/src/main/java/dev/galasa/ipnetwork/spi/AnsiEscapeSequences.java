/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.ipnetwork.spi;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Hex;

public class AnsiEscapeSequences {

    private static final byte ESCAPE = 0x1b;
    private static final byte CONTROL_SEQUENCE_INTRODUCER = 0x5b;
    private static final byte OPERATING_SYSTEM_COMMAND = 0x5d;
    private static final byte BEL = 0x07;
    private static final byte ST = 0x5C;

    public static byte[] stripAnsiEscapeSequences(byte[] data) throws IOException {
        ByteBuffer in = ByteBuffer.wrap(data);
        ByteBuffer out = ByteBuffer.allocate(data.length); // only subtracting data, not adding
        
        while(in.hasRemaining()) {
            byte b = in.get();
            
            if (b == ESCAPE) {
                processEscapeSequence(in, out);
            } else {
                out.put(b);
            }
        }
        
        byte[] output = new byte[out.position()];
        ((Buffer)out).rewind();  // ((Buffer)out) is required because there appears to be a bug using a later JDK compiling 8 target code
        out.get(output);
        return output;
    }

    private static void processEscapeSequence(ByteBuffer in, ByteBuffer out) throws IOException {
        if (!in.hasRemaining()) {
            throw new IOException("Invalid escape sequence, no type following 0x1B");
        }
        
        byte b = in.get();
        
        switch(b) {
            case CONTROL_SEQUENCE_INTRODUCER:
                processControlSequeneceIntroducer(in,out);
                return;
            case OPERATING_SYSTEM_COMMAND:
                processOperatingSystemCommand(in,out);
                return;
            default:
                byte[] error = new byte[] {b};
                throw new IOException("Unrecognised escape sequence type 0x" + Hex.encodeHexString(error));
        }
        
    }

    private static void processControlSequeneceIntroducer(ByteBuffer in, ByteBuffer out) throws IOException {
        // we dont care what is in here, just that the sequence is terminated by 
        // a byte in the range of 0x40 through 0x7E
        
        while(in.hasRemaining()) {
            byte b = in.get();
            
            if (0x40 <= b && b <= 0x7e) {
                return;
            }
        }
        
        throw new IOException("ANSI escape control sequence introducer was not terminated by a byte in the range of 0x40 and 0x7E");
    }
    
    private static void processOperatingSystemCommand(ByteBuffer in, ByteBuffer out) throws IOException {
        // we dont care what is in here, just that the sequence is terminated by 
        // BEL or ST
        
        while(in.hasRemaining()) {
            byte b = in.get();
            
            if (b == BEL) {
                return;
            }
            
            if (b == ESCAPE) {
                if (!in.hasRemaining()) {
                    throw new IOException("\"ANSI escape operating system command was not terminated correctly, received ESC but no \\");
                }
                
                b = in.get();
                if (b != ST) {
                    throw new IOException("\"ANSI escape operating system command was not terminated correctly, received ESC but no \\");
                }
                return;
            }
        }
        
        throw new IOException("ANSI escape operating system command was not terminated BEL or ST");
    }

}
