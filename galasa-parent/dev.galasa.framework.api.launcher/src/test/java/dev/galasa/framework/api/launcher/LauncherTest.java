/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.launcher;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import dev.galasa.framework.FrameworkInitialisation;
import dev.galasa.framework.spi.FrameworkException;
import dev.galasa.framework.spi.IFramework;

//@RunWith(MockitoJUnitRunner.class)
public class LauncherTest {

    @Mock // TODO should not be mocking classes, only interfaces
    FrameworkInitialisation mockedframeInit;

    @InjectMocks
    Launcher                mockLauncher = spy(new Launcher());

    Map<String, Object>     props        = new HashMap<>();

    @Before
    public void setProperties() {
        props.clear();

        props.put("framework.bootstrap.url", "http://localhost:8181/bootstrap");
    }

//    @Test   TODO rewrite tests
    public void testLauncher() throws Exception {
        boolean caught = false;

        IFramework mockedFramework = Mockito.mock(IFramework.class);

        doReturn(mockedframeInit).when(mockLauncher).init(any(Properties.class), any(Properties.class));

        when(mockedFramework.isInitialised()).thenReturn(true);
        when(mockedframeInit.getFramework()).thenReturn(mockedFramework);

        try {
            mockLauncher.activate(props);
        } catch (FrameworkException e) {
            caught = true;
        }
        assertFalse("Threw some exception", caught);
    }

}