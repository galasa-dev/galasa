/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import static org.assertj.core.api.Assertions.*;

import dev.galasa.boot.mocks.MockEnvironment;

import org.junit.Test;

public class TestGalasaHome {

	@Test
    public void testLauncher() throws Exception{
        Launcher l = new Launcher();

        MockEnvironment mockEnv = new MockEnvironment();
        mockEnv.setProperty("GALASA_HOME","/Users/hobbit/galasa_home_dir/");

        String home = l.getGalasaHome(mockEnv);

        assertThat(home).isEqualTo("/Users/hobbit/galasa_home_dir/");
    }
}
