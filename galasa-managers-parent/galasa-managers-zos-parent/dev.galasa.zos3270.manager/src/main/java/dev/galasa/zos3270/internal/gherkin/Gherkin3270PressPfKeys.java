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

public class Gherkin3270PressPfKeys  implements IStatementOwner {

    private final Gherkin3270Coordinator gerkinCoordinator;

    public Gherkin3270PressPfKeys(Gherkin3270Coordinator gerkinCoordinator, Zos3270ManagerImpl manager) {
        this.gerkinCoordinator = gerkinCoordinator;
    }

    @ExecutionMethod(keyword = GherkinKeyword.AND, regex = "press terminal( \\w+)? key (PF1|PF2|PF3|PF4|PF5|PF6|PF7|PF8|PF9|PF10|PF11|PF12|PF13|PF14|PF15|PF16|PF17|PF18|PF19|PF20|PF21|PF22|PF23|PF24)")
    public void pressPfKey(IGherkinExecutable executable, Map<String,Object> testVariables) throws Zos3270ManagerException, Zos3270Exception, TextNotFoundException, TerminalInterruptedException {
        List<String> groups = executable.getRegexGroups();  

        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(0));
        String key = groups.get(1);

        Zos3270TerminalImpl terminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (terminal == null ) {
            throw new Zos3270ManagerException("Unable to get terminal "+terminalId);
        }
        if (!terminal.isConnected()) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' is not connected");
        }

        switch(key) {
            case "PF1":
                terminal.pf1();
                break;
            case "PF2":
                terminal.pf2();
                break;
            case "PF3":
                terminal.pf3();
                break;
            case "PF4":
                terminal.pf4();
                break;
            case "PF5":
                terminal.pf5();
                break;
            case "PF6":
                terminal.pf6();
                break;
            case "PF7":
                terminal.pf7();
                break;
            case "PF8":
                terminal.pf8();
                break;
            case "PF9":
                terminal.pf9();
                break;
            case "PF10":
                terminal.pf10();
                break;
            case "PF11":
                terminal.pf11();
                break;
            case "PF12":
                terminal.pf12();
                break;
            case "PF13":
                terminal.pf13();
                break;
            case "PF14":
                terminal.pf14();
                break;
            case "PF15":
                terminal.pf15();
                break;
            case "PF16":
                terminal.pf16();
                break;
            case "PF17":
                terminal.pf17();
                break;
            case "PF18":
                terminal.pf18();
                break;
            case "PF19":
                terminal.pf19();
                break;
            case "PF20":
                terminal.pf20();
                break;
            case "PF21":
                terminal.pf21();
                break;
            case "PF22":
                terminal.pf22();
                break;
            case "PF23":
                terminal.pf23();
                break;
            case "PF24":
                terminal.pf24();
                break;
           default:
                throw new Zos3270Exception("Unrecognised pf key '" + key + "'");
        }
    }

}
