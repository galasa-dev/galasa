/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package t;

import java.nio.ByteBuffer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.spi.Field;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class TestInbound extends Zos3270TestBase {

    
    public static void main(String[] args) throws TerminalInterruptedException, NetworkException, DecoderException {
        
//        String inbound = "f5c01140c1131140401d004a54444a53303120776173207375636365737366756c2e204a534f4e20636f6e76657274656420746f20446174612e2020205472616e73666f726d20636f6d6d616e64207375636365737366756c2e1d00200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001d00200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001d00200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001d0020000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        
        String inbound1 = "f5c311c5e41311405d290242f1c0f8c3e5c6d4e2f0f140e3c5e2e340d4c1d7e2c5e311c5d21d60c3e4e2e3d6d4c5d940d5e4d4c2c5d97a1dd1f1f1f1f1f1f11df011c7f6290242f1c060d7c1d9e340d5e4d4c2c5d97a1dd1f2f2f2f2f2f2f2f2f2f21df0114ad9290242f1c060d8e4c1d5e3c9e3e87a1dd1f3f3f3f340401df0115a50290242f2c061c3d6d5e3d9d6d340e3c5e2e34040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040404040"; // expected input as a hex code, not a secret //pragma: allowlist secret
        String inbound2 = "f140114040124040";
        String inbound3 = "f1c611c26013";
        byte[] inbound1Bytes = Hex.decodeHex(inbound1);
        byte[] inbound2Bytes = Hex.decodeHex(inbound2);
        byte[] inbound3Bytes = Hex.decodeHex(inbound3);
        
        Network network = new Network("here", 1, "a");
        Screen screen = CreateTestScreen(80, 24, network);
        NetworkThread networkThread = new NetworkThread(null, screen, null, null);
        
        ByteBuffer buffer = ByteBuffer.wrap(inbound1Bytes);
        Inbound3270Message im = networkThread.process3270Data(buffer);
        screen.processInboundMessage(im);
        System.out.println(screen.printScreenTextWithCursor());
        
        buffer = ByteBuffer.wrap(inbound2Bytes);
        im = networkThread.process3270Data(buffer);
        screen.processInboundMessage(im);
        System.out.println(screen.printScreenTextWithCursor());
        
        buffer = ByteBuffer.wrap(inbound3Bytes);
        im = networkThread.process3270Data(buffer);
        screen.processInboundMessage(im);
        System.out.println(screen.printScreenTextWithCursor());
        
        for(Field field : screen.calculateFields()) {
            System.out.println(field);
        }
        
    }

}
