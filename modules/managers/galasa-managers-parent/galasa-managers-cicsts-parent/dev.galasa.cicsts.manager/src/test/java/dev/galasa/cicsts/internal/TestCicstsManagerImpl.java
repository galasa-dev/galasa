/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cicsts.internal;

import static org.assertj.core.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dev.galasa.cicsts.CicsRegion;
import dev.galasa.cicsts.CicsTerminal;
import dev.galasa.cicsts.ICicsRegion;
import dev.galasa.cicsts.ICicsTerminal;
import dev.galasa.framework.Framework;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.language.GalasaTest;
import dev.galasa.framework.spi.language.gherkin.GherkinTest;

public class TestCicstsManagerImpl {
    
    private class MockGalasaTest extends GalasaTest{

        public MockGalasaTest(GherkinTest test) {
            super(test);
        }
    }

    private class DummyTestBad{

        @CicsTerminal(cicsTag = "TERM01")
        public ICicsTerminal terminal;

        @CicsRegion(cicsTag = "REGN01")
        public ICicsRegion cicsRegion;

    }

    private class mockCicstsManagerImpl extends CicstsManagerImpl{
        public mockCicstsManagerImpl() {
            super();
        }
    }

    @Test
    public void testGenerateCicsTerminalBadReturnsError() throws Exception{
        // Given...
        DummyTestBad dummyTest = new DummyTestBad();
        List<IManager> managersList = new ArrayList<IManager>();
        managersList.add(new CicstsManagerImpl());

        GalasaTest test = new MockGalasaTest(null);

        Framework framework  = new Framework();
        CicstsManagerImpl cicsTsManager = new mockCicstsManagerImpl();
        cicsTsManager.initialise(framework, managersList, managersList, test);
        
        Field terminal = dummyTest.getClass().getField("terminal");
        Field region = dummyTest.getClass().getField("cicsRegion");
        List<Annotation> annotations = new ArrayList<>();
        annotations.add(terminal.getAnnotation(CicsTerminal.class));
        annotations.add(region.getAnnotation(CicsRegion.class));

        // When...
        Throwable thrown = catchThrowable(() -> {
            cicsTsManager.generateCicsTerminal(terminal, annotations);
        });
        
        // Then...
        assertThat(thrown).isNotNull();
        String error = thrown.getMessage();
        String expectedError = "Unable to setup CICS Terminal for field 'terminal', for region with tag 'TERM01'"+
            " as a region with a matching 'cicsTag' tag was not found, or the region was not provisioned.";
        assertThat(error).isEqualTo(expectedError);

    }

}
