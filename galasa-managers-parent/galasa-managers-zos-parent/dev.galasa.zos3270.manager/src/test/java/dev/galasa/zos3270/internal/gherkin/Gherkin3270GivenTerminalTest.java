/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.gherkin;

import java.util.Map;

import org.junit.Test;

import dev.galasa.framework.spi.language.gherkin.GherkinTest;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.Zos3270ManagerImpl;

import static org.assertj.core.api.Assertions.*;

public class Gherkin3270GivenTerminalTest {


    public class MockZos3270Manager extends Zos3270ManagerImpl {
        private Map<String,String> cpsProperties;

        public MockZos3270Manager(Map<String,String> cpsProperties) {
            this.cpsProperties = cpsProperties;
        }

        public String getCpsProperty(String fullPropertyName) throws Zos3270ManagerException {
            return cpsProperties.get(fullPropertyName);
        }
    }

    public class MockGherkinCoordinator extends Gherkin3270Coordinator {
        public MockGherkinCoordinator(Zos3270ManagerImpl manager, GherkinTest gherkinTest) {
            super(manager, gherkinTest);
        }
    }

    @Test
    public void testGherkin3270GivenTerminalCanBeCreatedOk() {

        Map<String,String> cpsProps = Map.of();
        MockZos3270Manager mockManager = new MockZos3270Manager(cpsProps);
        MockGherkinCoordinator mockCoordinator = new MockGherkinCoordinator(mockManager, null);
        
        new Gherkin3270GivenTerminal(mockCoordinator, mockManager);
    }

    @Test
    public void testGherkin3270CanGetDefaultPreferredTerminalSizeEmptyCPS() throws Exception {
        Map<String,String> cpsProps = Map.of(
            // "zos3270.gherkin.terminal.rows","24",
            // "zos3270.gherkin.terminal.columns","80"
            );
        MockZos3270Manager mockManager = new MockZos3270Manager(cpsProps);
        MockGherkinCoordinator mockCoordinator = new MockGherkinCoordinator(mockManager, null);
        
        Gherkin3270GivenTerminal terminal = new Gherkin3270GivenTerminal(mockCoordinator, mockManager);
        TerminalSize preferredTerminalSize = terminal.getPreferredTerminalSize("","");

        assertThat(preferredTerminalSize.getRows()).isEqualTo(Gherkin3270GivenTerminal.DEFAULT_TERMINAL_ROWS);
        assertThat(preferredTerminalSize.getColumns()).isEqualTo(Gherkin3270GivenTerminal.DEFAULT_TERMINAL_COLUMNS);
    }

    @Test
    public void testGherkin3270CanGetDefaultPreferredTerminalSizeFromCPS() throws Exception {
        Map<String,String> cpsProps = Map.of(
            "zos3270.gherkin.terminal.rows","25",
            "zos3270.gherkin.terminal.columns","81"
            );
        MockZos3270Manager mockManager = new MockZos3270Manager(cpsProps);
        MockGherkinCoordinator mockCoordinator = new MockGherkinCoordinator(mockManager, null);
        
        Gherkin3270GivenTerminal terminal = new Gherkin3270GivenTerminal(mockCoordinator, mockManager);
        TerminalSize preferredTerminalSize = terminal.getPreferredTerminalSize("","");

        assertThat(preferredTerminalSize.getRows()).isEqualTo(25);
        assertThat(preferredTerminalSize.getColumns()).isEqualTo(81);
    }

    @Test
    public void testGherkin3270CanGetDefaultPreferredTerminalSizeFromGherkinStatement() throws Exception {
        Map<String,String> cpsProps = Map.of(
            "zos3270.gherkin.terminal.rows","25",
            "zos3270.gherkin.terminal.columns","81"
            );
        MockZos3270Manager mockManager = new MockZos3270Manager(cpsProps);
        MockGherkinCoordinator mockCoordinator = new MockGherkinCoordinator(mockManager, null);
        
        Gherkin3270GivenTerminal terminal = new Gherkin3270GivenTerminal(mockCoordinator, mockManager);
        TerminalSize preferredTerminalSize = terminal.getPreferredTerminalSize("48","64");

        assertThat(preferredTerminalSize.getRows()).isEqualTo(48);
        assertThat(preferredTerminalSize.getColumns()).isEqualTo(64);
    }

    @Test
    public void testGherkin3270GetDefaultPreferredTerminalSizeFailsIfCPSRowsPropNotANumber() throws Exception {
        Map<String,String> cpsProps = Map.of(
            "zos3270.gherkin.terminal.rows","hello"
            // "zos3270.gherkin.terminal.columns","81"
            );
        MockZos3270Manager mockManager = new MockZos3270Manager(cpsProps);
        MockGherkinCoordinator mockCoordinator = new MockGherkinCoordinator(mockManager, null);
        
        Gherkin3270GivenTerminal terminal = new Gherkin3270GivenTerminal(mockCoordinator, mockManager);

        // When...
        Zos3270ManagerException ex = catchThrowableOfType(
            () -> terminal.getPreferredTerminalSize("",""),
            Zos3270ManagerException.class );

        assertThat(ex).hasMessageContaining("does not contain a number");
    }

    @Test
    public void testGherkin3270GetDefaultPreferredTerminalSizeFailsIfGherkinStatementValueNotANumber() throws Exception {
        Map<String,String> cpsProps = Map.of(
            "zos3270.gherkin.terminal.rows","25",
            "zos3270.gherkin.terminal.columns","81"
            );
        MockZos3270Manager mockManager = new MockZos3270Manager(cpsProps);
        MockGherkinCoordinator mockCoordinator = new MockGherkinCoordinator(mockManager, null);
        
        Gherkin3270GivenTerminal terminal = new Gherkin3270GivenTerminal(mockCoordinator, mockManager);

        // When...
        Zos3270ManagerException ex = catchThrowableOfType(
            () -> terminal.getPreferredTerminalSize("hello-notanumber","notanumber"),
            Zos3270ManagerException.class );

        assertThat(ex).hasMessageContaining("does not contain a number");
    }

    @Test
    public void testGherkin3270GetDefaultPreferredTerminalSizeFailsIfCPSColumnsPropNotANumber() throws Exception {
        Map<String,String> cpsProps = Map.of(
            "zos3270.gherkin.terminal.rows","24",
            "zos3270.gherkin.terminal.columns","hello"
            );
        MockZos3270Manager mockManager = new MockZos3270Manager(cpsProps);
        MockGherkinCoordinator mockCoordinator = new MockGherkinCoordinator(mockManager, null);
        
        Gherkin3270GivenTerminal terminal = new Gherkin3270GivenTerminal(mockCoordinator, mockManager);

        // When...
        Zos3270ManagerException ex = catchThrowableOfType(
            () -> terminal.getPreferredTerminalSize("",""),
            Zos3270ManagerException.class );

        assertThat(ex).hasMessageContaining("does not contain a number");
    }
}
