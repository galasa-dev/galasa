/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import dev.galasa.framework.spi.utils.GalasaGson;
import dev.galasa.textscan.spi.ITextScannerManagerSpi;
import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.mocks.MockNetwork;

public class TerminalTest {

    @Test
    public void testCanCreateTerminal() throws Exception {

        String terminalId = "abcdef";
        String host = "myHost";
        int port = 1234;
        boolean isSSL = false;
        boolean isVerifyServer = false;
        TerminalSize primarySize = new TerminalSize(80,24);
        TerminalSize alternateSize = new TerminalSize(80,24);
        ITextScannerManagerSpi textScan = null ;
        Charset codePage = Charset.availableCharsets().get("1024");

        ByteArrayOutputStream networkOut = new ByteArrayOutputStream();

        MockNetwork network = new MockNetwork(networkOut) {
            public boolean connectClient() throws NetworkException {
                return true;
            }
        };

        new Terminal(terminalId, host, port, isSSL, isVerifyServer, primarySize, alternateSize,
        textScan, codePage, network);

    }

    @Test
    public void testConvertNullTerminalToJsonReturnsNull() throws Exception {

        String terminalId = "abcdef";
        String host = "myHost";
        int port = 1234;
        boolean isSSL = false;
        boolean isVerifyServer = false;
        TerminalSize primarySize = new TerminalSize(80,24);
        TerminalSize alternateSize = new TerminalSize(80,24);
        ITextScannerManagerSpi textScan = null ;
        Charset codePage = Charset.availableCharsets().get("1024");

        ByteArrayOutputStream networkOut = new ByteArrayOutputStream();

        MockNetwork network = new MockNetwork(networkOut) {
            public boolean connectClient() throws NetworkException {
                return true;
            }
        };

        Terminal terminal = new Terminal(terminalId, host, port, isSSL, isVerifyServer, primarySize, alternateSize,
        textScan, codePage, network);

        String terminalJsonStr = terminal.toJsonString();
        assertThat(terminalJsonStr).isNull();
    }

    @Test
    public void testCanConvertTerminalToJson() throws Exception {
        GalasaGson gson = new GalasaGson();

        String terminalId = "abcdef";
        String host = "myHost";
        int port = 1234;
        boolean isSSL = false;
        boolean isVerifyServer = false;
        TerminalSize primarySize = new TerminalSize(80,24);
        TerminalSize alternateSize = new TerminalSize(80,24);
        ITextScannerManagerSpi textScan = null ;
        Charset codePage = Charset.availableCharsets().get("1024");

        ByteArrayOutputStream networkOut = new ByteArrayOutputStream();

        MockNetwork network = new MockNetwork(networkOut) {
            public boolean connectClient() throws NetworkException {
                return true;
            }
        };

        Terminal terminal = new Terminal(terminalId, host, port, isSSL, isVerifyServer, primarySize, alternateSize,
        textScan, codePage, network);

        String textContent = "Hello, world!";
        Character[] fieldChars = textContent.chars().mapToObj(c -> (char)c).toArray(Character[]::new);
        String runId = "run1";
        int sequenceNo = 0;
        boolean isInbound = true;
        String type = "test";
        String aid = "ENTER";
        int cursorColumn = 0;
        int cursorRow = 10;
        dev.galasa.zos3270.common.screens.Terminal rasTerminal = new dev.galasa.zos3270.common.screens.Terminal(terminalId, runId, sequenceNo, primarySize);
        TerminalImage image = new TerminalImage(0, terminalId, isInbound, type, aid, primarySize, cursorColumn, cursorRow);

        TerminalField field = new TerminalField(0, 0, false, false, false,
            true, false, false, false, Colour.BLUE.getLetter(),
                Colour.DEFAULT.getLetter(), Highlight.DEFAULT.getLetter()
        );
        FieldContents fieldContents = new FieldContents(fieldChars);
        field.getContents().add(fieldContents);
        image.getFields().add(field);

        rasTerminal.addImage(image);

        terminal.setCurrentTerminal(rasTerminal);

        String terminalJsonStr = terminal.toJsonString();
        JsonObject terminalJsonObj = gson.fromJson(terminalJsonStr, JsonObject.class);

        assertThat(terminalJsonObj.get("id").getAsString()).isEqualTo(terminalId);
        assertThat(terminalJsonObj.get("runId").getAsString()).isEqualTo(runId);

        JsonArray imagesJsonArr = terminalJsonObj.get("images").getAsJsonArray();
        assertThat(imagesJsonArr).hasSize(1);
        JsonObject imageJsonObj = imagesJsonArr.get(0).getAsJsonObject();
        assertThat(imageJsonObj.get("id").getAsString()).isEqualTo(terminalId);
        assertThat(imageJsonObj.get("type").getAsString()).isEqualTo(type);
        assertThat(imageJsonObj.get("aid").getAsString()).isEqualTo(aid);

        JsonArray imageFieldsArr = imageJsonObj.get("fields").getAsJsonArray();
        assertThat(imageFieldsArr).hasSize(1);
        JsonObject imageFieldJsonObj = imageFieldsArr.get(0).getAsJsonObject();
        assertThat(imageFieldJsonObj.get("foregroundColour").getAsString()).isEqualTo(String.valueOf(Colour.BLUE.getLetter()));

        JsonArray fieldContentsArr = imageFieldJsonObj.get("contents").getAsJsonArray();
        assertThat(fieldContentsArr).hasSize(1);
        JsonObject contentJson = fieldContentsArr.get(0).getAsJsonObject();
        assertThat(contentJson.get("text").getAsString()).isEqualTo(textContent);
    }
    
}
