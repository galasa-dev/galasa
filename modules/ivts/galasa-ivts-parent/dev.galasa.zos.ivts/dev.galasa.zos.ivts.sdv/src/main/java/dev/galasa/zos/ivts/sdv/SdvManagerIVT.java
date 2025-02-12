/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos.ivts.sdv;

import org.apache.commons.logging.Log;

import static org.assertj.core.api.Assertions.assertThat;

import dev.galasa.BeforeClass;
import dev.galasa.ProductVersion;
import dev.galasa.Test;
import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.CicstsManagerException;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.core.manager.Logger;
import dev.galasa.sdv.ISdvUser;
import dev.galasa.sdv.SdvManagerException;
import dev.galasa.sdv.SdvUser;
import dev.galasa.zosbatch.ZosBatchException;

 @Test
 public class SdvManagerIVT {
   
    @Logger
    public Log logger;

    @CicsRegion(cicsTag = "SDVIVT")
    public ICicsRegion cics;

    @CicsTerminal(cicsTag = "SDVIVT")
    public ICicsTerminal terminal;

    @SdvUser(cicsTag = "SDVIVT", roleTag = "R1")
    public ISdvUser user1;

    private static final String SDV_TCPIPSERVICE_NAME = "SDVXSDT";

    @BeforeClass
    public void logIntoTerminals() throws SdvManagerException, ZosBatchException, CicstsManagerException {
        // Only run test if running on CICS 6.2+ & SEC=YES
        if (!cics.getRegionJob().retrieveOutputAsString().contains("DFHXS1102I")
            && !cics.getVersion().isEarlierThan(ProductVersion.v(750))
        ) {
                user1.logIntoTerminal(terminal);
        }
    }

    @Test
    public void userUsesCeda() throws Exception {

        // Only run test if running on CICS 6.2+ & SEC=YES
        if (!cics.getRegionJob().retrieveOutputAsString().contains("DFHXS1102I")
            && !cics.getVersion().isEarlierThan(ProductVersion.v(750))
        ) {

            terminal.type("CEDA DI G(SDVGRP)").enter().waitForTextInField(SDV_TCPIPSERVICE_NAME);

            assertThat(terminal.searchText(SDV_TCPIPSERVICE_NAME))
                    .as("Expectation to see " + SDV_TCPIPSERVICE_NAME + " in terminal").isTrue();
            terminal.pf3();
        } else {
            // Just pass the test if running on earlier CICS versions
            assertThat(true).isTrue();
        }
    }
  
 }
 