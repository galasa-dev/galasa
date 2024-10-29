/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package t;

import java.nio.ByteBuffer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.internal.comms.Inbound3270Message;
import dev.galasa.zos3270.internal.comms.Network;
import dev.galasa.zos3270.internal.comms.NetworkThread;
import dev.galasa.zos3270.spi.Screen;
import dev.galasa.zos3270.util.Zos3270TestBase;

public class TestColour extends Zos3270TestBase {

    
    public static void main(String[] args) throws DecoderException, Zos3270Exception {
        
        String inbound = "f5c2115b611311c2601df8d59699948193408689859384a211c5401df0d596999481931df8c995a38595a28511c6501d40e385a2a340c99597a4a3404040401df011c8f0290341f442f7c0f8c889878893898788a3899587114b50290341f242f2c0f0d9858440d985a58599a285290341f242f2c0f0d98584408187818995114c6f290341f242f1c0f0c293a48540d985a58599a285290341f242f3c0f0d789959240d985a58599a285290341f242f4c0f0c79985859540d985a58599a285290341f242f5c0f0e3a49998a49689a28540d985a58599a285114df0290341f242f7c0f0e68889a38540d985a58599a28511505e290341f442f6c0f0e885939396a640e4958485999389958511d27a290341f142f6c0f0e885939396a640c29389959211d55c290242f6c0f0e885939396a640d6a4a393899585114c60290341f242f6c0f0e885939396a640d985a58599a285114040290242f7c0f0e385a2a340c5a7a3859584858440c481a381a2a39985819440d4819711d550290242f2c0f0d9858440d6a4a39389958511d940290242f2c0f0d98584290242f1c0f0c293a485290242f3c0f0d7899592290242f4c0f0c799858595290242f5c0f0e3a49998a49689a285290242f6c0f0e885939396a6290242f7c0f0e68889a3851df0c4858681a493a3115a50290242f2c0f8d98584290242f1c0f8c293a485290242f3c0f8d7899592290242f4c0f8c799858595290242f5c0f8e3a49998a49689a285290242f6c0f8e885939396a6290242f7c0f8e68889a3851df8c4858681a493a311d2f0290341f142f2c0f0d9858440c293899592115050290341f442f2c0f0d9858440e49584859993899585115b60290341f442f4c0c1115c6f1df0"; // expected input as a hex code, not a secret //pragma: allowlist secret
        byte[] inboundBytes = Hex.decodeHex(inbound);
        
        Network network = new Network("here", 1, "a");
        Screen screen = CreateTestScreen(80, 24, network);
        NetworkThread networkThread = new NetworkThread(null, screen, network, null);
        
        ByteBuffer buffer = ByteBuffer.wrap(inboundBytes);
        Inbound3270Message im = networkThread.process3270Data(buffer);
        screen.processInboundMessage(im);
   //     System.out.println(screen.printScreenTextWithCursor());
        
        System.out.println(screen.printExtendedScreen(false, true, true, false, false, false, false));
//        System.out.println(screen.printExtendedScreen(true, true, true, true, true, true, true));
                
  //      for(Field field : screen.calculateFields()) {
   //         System.out.println(field);
   //     }
        
    }

}
