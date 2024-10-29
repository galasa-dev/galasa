/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.linux.manager.ivt;

import static org.assertj.core.api.Assertions.*;

import org.apache.commons.logging.Log;

import dev.galasa.Test;
import dev.galasa.core.manager.Logger;
import dev.galasa.ipnetwork.ICommandShell;
import dev.galasa.ipnetwork.IIpHost;
import dev.galasa.linux.ILinuxImage;
import dev.galasa.linux.LinuxImage;
import dev.galasa.linux.LinuxIpHost;
import dev.galasa.linux.OperatingSystem;

@Test
public class LinuxManagerIVT {

    @Logger
    public Log         logger;

    @LinuxImage(operatingSystem = OperatingSystem.any, imageTag = "PRIMARY")
    public ILinuxImage linuxPrimary;

    @LinuxIpHost(imageTag = "PRIMARY")
    public IIpHost     linuxHost;

    @Test
    public void checkPrimaryImage() throws Exception {
    	assertThat(linuxPrimary).isNotNull();
    	assertThat(linuxHost).isNotNull();
    }

    @Test
    public void checkCommandShell() throws Exception {

        ICommandShell commandShell = this.linuxPrimary.getCommandShell();

        String response = commandShell.issueCommand("uname -a");

        if (response.isEmpty()) {
            throw new Exception("Invalid response from the command shell :-\n" + response);
        }

    }

}
