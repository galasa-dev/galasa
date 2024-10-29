/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.Terminal;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;

public class TerminalJsonTransformTest {

    /**
     * Check that the pjos can be serialised to json and back again
     */
    @Test
    public void testWriteAndReadJson() {
        

        String terminalID = "bob";
        int terminalSequence = 3345;

        String imageID = "bob-1";
        int imageSequence = 5588;
        String imageType = "system";

        int terminalColumns = 80;
        int terminalRows = 24;
        int altColumns = 133;
        int altRows = 32;

        int posRow = 6;
        int posColumn = 8;

        int cursorRow = 3;
        int cursorCol = 4;

        String aid = "aidy";
        String runid = "eeek";

        TerminalSize defaultSize = new TerminalSize(terminalColumns, terminalRows);
        TerminalSize altSize = new TerminalSize(altColumns, altRows);

        Terminal terminal = new Terminal(terminalID, runid, terminalSequence, defaultSize);
        TerminalImage image = new TerminalImage(imageSequence, imageID, true, imageType, aid, altSize, cursorCol,
                cursorRow);
        terminal.addImage(image);
        terminal.addImage(
                new TerminalImage(imageSequence + 1, imageID, true, imageType, aid, altSize, cursorCol, cursorRow));

        TerminalField field = new TerminalField(posRow, posColumn, false, true, false, true, true, true, true, null, null, null);
        image.getFields().add(field);

        Character[] data = new Character[] { 'a', 'c', 'b' };

        FieldContents contents = new FieldContents(data);
        field.getContents().add(contents);

        TerminalJsonTransform transform = new TerminalJsonTransform(true);

        String tempJson = transform.toJsonString(terminal);

        Terminal testTerminal = transform.toTerminal(tempJson);

        assertThat(testTerminal.getId()).isEqualTo(terminalID);
        assertThat(testTerminal.getRunId()).isEqualTo(runid);
        assertThat(testTerminal.getSequence()).isEqualTo(terminalSequence);
        assertThat(testTerminal.getDefaultSize().getColumns()).isEqualTo(terminalColumns);
        assertThat(testTerminal.getDefaultSize().getRows()).isEqualTo(terminalRows);
        assertThat(testTerminal.getImages().size()).isEqualTo(2);

        TerminalImage testImage = testTerminal.getImages().get(0);

        assertThat(testImage.getId()).isEqualTo(imageID);
        assertThat(testImage.getSequence()).isEqualTo(imageSequence);
        assertThat(testImage.getImageSize().getColumns()).isEqualTo(altColumns);
        assertThat(testImage.getImageSize().getRows()).isEqualTo(altRows);
        assertThat(testImage.getCursorColumn()).isEqualTo(cursorCol);
        assertThat(testImage.getCursorRow()).isEqualTo(cursorRow);
        assertThat(testImage.getType()).isEqualTo(imageType);
        assertThat(testImage.isInbound()).isTrue();
        assertThat(testImage.getAid()).isEqualTo(aid);
        assertThat(testImage.getFields().size()).isEqualTo(1);

        TerminalField testField = testImage.getFields().get(0);

        assertThat(testField.getRow()).isEqualTo(posRow);
        assertThat(testField.getColumn()).isEqualTo(posColumn);
        assertThat(testField.isUnformatted()).isFalse();
        assertThat(testField.isFieldProtected()).isTrue();
        assertThat(testField.isFieldNumeric()).isFalse();
        assertThat(testField.isFieldDisplay()).isTrue();
        assertThat(testField.isFieldIntenseDisplay()).isTrue();
        assertThat(testField.isFieldSelectorPen()).isTrue();
        assertThat(testField.isFieldModifed()).isTrue();
        assertThat(testField.getContents().size()).isEqualTo(1);

        FieldContents testContents = testField.getContents().get(0);
        assertThat(testContents.getChars()).isEqualTo(data);
    }

}
