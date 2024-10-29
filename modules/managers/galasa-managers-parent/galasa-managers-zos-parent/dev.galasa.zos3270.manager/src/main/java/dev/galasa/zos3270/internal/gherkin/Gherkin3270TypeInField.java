/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.gherkin;

import java.util.List;
import java.util.Map;

import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;
import dev.galasa.zos3270.TerminalInterruptedException;
import dev.galasa.zos3270.TextNotFoundException;
import dev.galasa.zos3270.Zos3270Exception;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.internal.Zos3270ManagerImpl;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class Gherkin3270TypeInField  implements IStatementOwner {

    private final Gherkin3270Coordinator gerkinCoordinator;

    public Gherkin3270TypeInField(Gherkin3270Coordinator gerkinCoordinator, Zos3270ManagerImpl manager) {
        this.gerkinCoordinator = gerkinCoordinator;
    }

    @ExecutionMethod(keyword = GherkinKeyword.AND, regex = "type \"(.*)\" on terminal( \\w+)? in field labelled \"(.*)\"")
    public void allocateTerminal(IGherkinExecutable executable, Map<String,Object> testVariables) throws Zos3270ManagerException, Zos3270Exception, TextNotFoundException, TerminalInterruptedException {
        List<String> groups = executable.getRegexGroups();  

        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));
        String text = groups.get(0);
        if (text == null || text.isEmpty()) {
            return;
        }
        String field = groups.get(2);
        if (field == null || field.isEmpty()) {
            throw new Zos3270ManagerException("Missing field label");
        }

        Zos3270TerminalImpl terminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (terminal == null ) {
            throw new Zos3270ManagerException("Unable to get terminal "+terminalId);
        }
        if (!terminal.isConnected()) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' is not connected");
        }

        terminal.positionCursorToFieldContaining(field);
        terminal.tab();
        terminal.type(text);
    }

}
