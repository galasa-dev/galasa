/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.zos3270;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zos3270.ITerminal;
import dev.galasa.zos3270.Zos3270Terminal;

@Test
public class Zos3270IVTSimframe {

    @Logger
    public Log       logger;

    @ZosImage(imageTag = "simbank")
    public IZosImage image;

    @Zos3270Terminal(imageTag = "simbank")
    public ITerminal terminal;

    @Test
    public void checkInjection() {
        assertThat(logger).as("Logger Field").isNotNull();
        assertThat(image).as("zOS Image Field").isNotNull();
        assertThat(terminal).as("zOS 3270 Terminal Field").isNotNull();
    }

    @Test
    public void testWithSimframe() throws Exception {
        // *** Make sure the screen is ready to go and is at the logon screen
        terminal.waitForKeyboard().waitForTextInField("SIMPLATFORM LOGON SCREEN");

        // *** Logon
        terminal.positionCursorToFieldContaining("Userid").tab().type("IBMUSER")
                .positionCursorToFieldContaining("Password").tab().type("SYS1").enter();

        // *** Select the BANKTEST application
        terminal.waitForKeyboard().waitForTextInField("SIMPLATFORM MAIN MENU").positionCursorToFieldContaining("===>")
                .tab().type("BANKTEST").enter();

        // *** Clear the CICS Logo
        terminal.waitForKeyboard().waitForTextInField("***  WELCOME TO CICS  ***").clear();

        // *** Enter the BANK transaction
        terminal.waitForKeyboard().tab().type("BANK").enter();

        // *** check we are at the main menu and then return to the logon screen
        terminal.waitForKeyboard().waitForTextInField("SIMBANK MAIN MENU").pf3().waitForKeyboard().pf3()
                .waitForTextInField("SIMPLATFORM LOGON SCREEN");

        logger.info("SIMFRAME 3270 screen check is complete");
    }
}
