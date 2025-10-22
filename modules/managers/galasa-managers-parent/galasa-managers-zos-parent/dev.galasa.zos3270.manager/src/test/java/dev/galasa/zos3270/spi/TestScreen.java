/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.spi;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;

import org.junit.Test;

import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.comms.Network;

public class TestScreen {

    @Test
    public void testCanCreateAScreen() throws Exception {
        TerminalSize primarySize = new TerminalSize(80, 24);
        Network network = null;
        Charset codePage = null;

        new Screen(primarySize, primarySize, network, codePage);
    }

    @Test
    public void testCanGetCursorRowBack() throws Exception {
        // Given...
        TerminalSize primarySize = new TerminalSize(80, 24);
        Network network = null;
        Charset codePage = null;

        Screen testScreen = new Screen(primarySize, primarySize, network, codePage);

        // When...
        int inputColumn = 15;
        int inputRow = 18;
        testScreen.setCursorPosition(inputColumn, inputRow);

        // Then...
        int row = testScreen.getCursorRow();
        int column = testScreen.getCursorColumn();

        assertThat(column).isEqualTo(inputColumn);
        assertThat(row).isEqualTo(inputRow);
    }

    @Test
    public void testGettingCursorColumnWhenScreenSizeIsZeroDoesntBlowUp() throws Exception {
        // Given...
        TerminalSize primarySize = new TerminalSize(0, 0);
        Network network = null;
        Charset codePage = null;
        Screen testScreen = new Screen(primarySize, primarySize, network, codePage);

        int inputColumn = 5;
        int inputRow = 8;
        testScreen.setCursorPosition(inputColumn, inputRow);

        // When...
        int column = testScreen.getCursorColumn();

        // Then
        assertThat(column).isEqualTo(0);
    }

    @Test
    public void testGettingCursorRowWhenScreenSizeIsZeroDoesntBlowUp() throws Exception {
        // Given...
        TerminalSize primarySize = new TerminalSize(0, 0);
        Network network = null;
        Charset codePage = null;
        Screen testScreen = new Screen(primarySize, primarySize, network, codePage);

        int inputColumn = 5;
        int inputRow = 8;
        testScreen.setCursorPosition(inputColumn, inputRow);

        // When...
        int row = testScreen.getCursorRow();

        // Then
        assertThat(row).isEqualTo(0);
    }

}
