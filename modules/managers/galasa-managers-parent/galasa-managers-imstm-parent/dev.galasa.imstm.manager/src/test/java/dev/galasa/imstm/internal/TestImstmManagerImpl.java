/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.imstm.internal;

import static org.assertj.core.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.imstm.ImsSystem;
import dev.galasa.imstm.ImsTerminal;
import dev.galasa.imstm.IImsSystem;
import dev.galasa.imstm.IImsTerminal;
import dev.galasa.framework.Framework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.language.gherkin.GherkinTest;

public class TestImstmManagerImpl {
    
    private class MockGalasaTest extends GalasaTest{

        public MockGalasaTest(GherkinTest test) {
            super(test);
        }
    }

    private class DummyTestBad{

        @ImsTerminal(imsTag = "TERM01")
        public IImsTerminal terminal;

        @ImsSystem(imsTag = "SYS01")
        public IImsSystem imsSystem;

    }

    private class mockImstmManagerImpl extends ImstmManagerImpl{
        public mockImstmManagerImpl() {
            super();
        }
    }

    @Test
    public void TestGenerateImsTerminalBadReturnsError() throws Exception{
        // Given...
        DummyTestBad dummyTest = new DummyTestBad();
        List<IManager> managersList = new ArrayList<IManager>();
        managersList.add(new ImstmManagerImpl());

        GalasaTest test = new MockGalasaTest(null);

        Framework framework  = new Framework();
        ImstmManagerImpl imsTmManager = new mockImstmManagerImpl();
        imsTmManager.initialise(framework, managersList, managersList, test);
        
        Field terminal = dummyTest.getClass().getField("terminal");
        Field region = dummyTest.getClass().getField("imsSystem");
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(terminal.getAnnotation(ImsTerminal.class));
        annotations.add(region.getAnnotation(ImsSystem.class));

        // When...
        Throwable thrown = catchThrowable(() -> {
            imsTmManager.generateImsTerminal(terminal, annotations);
        });
        
        // Then...
        assertThat(thrown).isNotNull();
        String error = thrown.getMessage();
        String expectedError = "Unable to setup IMS Terminal for field 'terminal', for system with tag 'TERM01'"+
            " as a system with a matching 'imsTag' tag was not found, or the system was not provisioned.";
        assertThat(error).isEqualTo(expectedError);

    }

}
