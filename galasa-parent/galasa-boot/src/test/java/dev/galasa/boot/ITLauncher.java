/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.boot;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ITLauncher {

    /**
     * Test Launcher - runs during integration test verify phase Requires
     * infrastructure and test OBRs. If unavailable, the test will be allowed to
     * fail
     * 
     * @throws InterruptedException
     * @thr9ows LauncherException
     */
//	@Test   Skipping test as requires the obrs before they are actually built.  this will be run during the
// integration tests
    public void testLauncher() {

        Path localmavenPath = Paths.get(System.getProperty("user.home"), ".m2", "repository");
        if (System.getenv("JENKINS_HOME") != null) {
            localmavenPath = Paths.get(System.getenv("WORKSPACE"), ".repository");
        }

        String obrOption = "--obr";

        String[] args = new String[11];
        args[0] = "--trace";
        args[1] = "--localmaven";
        args[2] = Paths.get(localmavenPath.toFile().getAbsolutePath()).toUri().toString();
        args[3] = obrOption;
        args[4] = "mvn:dev.galasa/dev.galasa.framework.obr/0.3.0-SNAPSHOT/obr";
        args[5] = obrOption;
        args[6] = "mvn:dev.galasa/dev.galasa.core.obr/0.3.0-SNAPSHOT/obr";
        args[7] = obrOption;
        args[8] = "mvn:dev.galasa/dev.galasa.ivt.obr/0.3.0-SNAPSHOT/obr";
        args[9] = "--test";
        args[10] = "dev.galasa.ivt.core/dev.galasa.ivt.test.BasicTestExtendedAgain";

        Launcher launcher = new Launcher();
        try {
            launcher.launch(args);
        } catch (Exception e) {
            // Ignore failure until we have the Maven repositories set up
            if (!e.getCause().getCause().toString()
                    .contains("Unable to locate maven artifact mvn:dev.galasa/dev.galasa.core.obr")) {
                fail(e.getMessage());
            }
        }
    }
}