/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import org.junit.Test;

import dev.galasa.framework.mocks.*;

public class TestBaseTestRunner {
    
    @Test
    public void testCanInstantiateABaseTestRunner() throws Exception {

        // When...
        BaseTestRunner runner = new BaseTestRunner();

        MockFileSystem mockFileSystem = new MockFileSystem();

        Map<String,String> propsInRas = Map.of("initial","value");
        Path rasRootPath = mockFileSystem.getPath("/root");
        MockRASStoreService mockRas = new MockRASStoreService(propsInRas, rasRootPath );

        Properties props = new Properties();
        props.put("my.prop2","world");
        props.put("my.prop1", "hello");

        runner.saveAllOverridesPassedToArtifact(props, mockFileSystem, mockRas);

        List<Path> files = mockFileSystem.getListOfAllFiles();
        assertThat(files).hasSize(1);

        List<String> lines = mockFileSystem.readLines(new URI(files.get(0).toString()));
        assertThat(lines).hasSize(4);

        assertThat(lines.get(0)).contains("as overrides to the test");
        assertThat(lines.get(2)).contains("my.prop1","hello");
        assertThat(lines.get(3)).contains("my.prop2","world");
    }
}
