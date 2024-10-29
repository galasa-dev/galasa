/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.internal.gherkin;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.framework.spi.AbstractManager;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;
import dev.galasa.zos3270.Zos3270ManagerException;
import dev.galasa.zos3270.common.screens.TerminalSize;
import dev.galasa.zos3270.internal.Zos3270ManagerImpl;
import dev.galasa.zos3270.spi.NetworkException;
import dev.galasa.zos3270.spi.Zos3270TerminalImpl;

public class Gherkin3270GivenTerminal  implements IStatementOwner {
    
    public static final int DEFAULT_TERMINAL_ROWS    = 24;
    public static final int DEFAULT_TERMINAL_COLUMNS = 80;

    public static final String DEFAULT_TERMINAL_ROWS_STR = Integer.toString(DEFAULT_TERMINAL_ROWS);
    public static final String DEFAULT_TERMINAL_COLUMNS_STR = Integer.toString(DEFAULT_TERMINAL_COLUMNS);

    private final static Log logger = LogFactory.getLog(Gherkin3270GivenTerminal.class);
    private final Gherkin3270Coordinator gerkinCoordinator;
    private final Zos3270ManagerImpl manager;

    public Gherkin3270GivenTerminal(Gherkin3270Coordinator gerkinCoordinator, Zos3270ManagerImpl manager) {
        this.gerkinCoordinator = gerkinCoordinator;
        this.manager = manager;
    }
    
    @ExecutionMethod(keyword = GherkinKeyword.GIVEN, regex = "a terminal( with id of (\\w+))?( tagged (\\w+))?( with (\\d+) rows and (\\d+) columns)?")
    public void allocateTerminal(IGherkinExecutable executable, Map<String,Object> testVariables) throws Zos3270ManagerException {
        // Ensure we have a connected terminal
        List<String> groups = executable.getRegexGroups();
        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));

        Zos3270TerminalImpl terminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (terminal == null) {
            throw new Zos3270ManagerException("Terminal '" + terminalId + "' was not provisioned!");
        }
        
        if (!terminal.isConnected()) {
            try {
                terminal.connect();
            } catch (NetworkException ex ) {
                throw new Zos3270ManagerException("Cannot connect terminal to host system.",ex);
            }
        }
    }

    public void provision(IGherkinExecutable executable) throws Zos3270ManagerException {
        List<String> groups = executable.getRegexGroups();

        // Extract the parameters from the step in the scenario.
        String terminalId = Gherkin3270Coordinator.defaultTerminaId(groups.get(1));
        String imageTag = Gherkin3270Coordinator.defaultImageTag(groups.get(3));
        String rowsStepParameter = groups.get(5);
        String columnsStepParameter = groups.get(6);

        // Log what we have collected.
        {
            StringBuffer msg = new StringBuffer();
            msg.append("Provisioning a terminal:");
            msg.append(" id=");
            msg.append(terminalId);
            msg.append(" imageTag=");
            msg.append(imageTag);
            msg.append(" rows=");
            msg.append(rowsStepParameter);
            msg.append(" columns=");
            msg.append(columnsStepParameter);
            logger.info(msg.toString());
        }
        
        Zos3270TerminalImpl newTerminal = this.gerkinCoordinator.getTerminal(terminalId);
        if (newTerminal == null) {
            TerminalSize terminalSize = getPreferredTerminalSize(rowsStepParameter , columnsStepParameter);
            TerminalSize alternateSize = new TerminalSize(0, 0);

            newTerminal = this.manager.generateTerminal(imageTag, true, terminalSize, alternateSize);
            this.gerkinCoordinator.registerTerminal(terminalId, newTerminal, imageTag);
            logger.info("zOS 3270 Terminal id '" + terminalId + "' as been provisioned for image tag '" + imageTag + "'");
        } 
    }

    protected TerminalSize getPreferredTerminalSize(String rowsStepParameter, String rowsColumnParameter) throws Zos3270ManagerException {
        int columns ;
        int rows ;
        rows = getNumericCPSProperty("zos3270.gherkin.terminal.rows",DEFAULT_TERMINAL_ROWS,rowsStepParameter);
        columns = getNumericCPSProperty("zos3270.gherkin.terminal.columns",DEFAULT_TERMINAL_COLUMNS,rowsColumnParameter);
        logger.info("Preferred terminal size is "+Integer.toString(rows)+" x "+Integer.toString(columns));
        return new TerminalSize(columns, rows);
    }

    private int getNumericCPSProperty(String propertyName, int defaultConstantValue , String stepDefParameterValue) throws Zos3270ManagerException {
        String valueStr ;
        int cpsPropValue ;
        String notANumberMsg ;

        if (stepDefParameterValue==null || stepDefParameterValue.trim().equals("")) {
            // The value wasn't specified in the stepdef, so get it from the CPS property.
            valueStr = this.manager.getCpsProperty(propertyName);
            notANumberMsg = "Error: CPS property '"+propertyName+"' does not contain a number. Current value is "+valueStr;
        } else {
            // The value was specified in the stepdef parameter, so use that in preference to the CPS or the default value.
            valueStr = stepDefParameterValue ;
            notANumberMsg = "Error: value from gherkin statement does not contain a number. Current value is "+valueStr;
        }

        if (valueStr == null || valueStr.trim().isBlank()) {
            // The property is not there, or it's blank. So use the default value.
            cpsPropValue = defaultConstantValue ;
        } else {
            try {
                cpsPropValue = Integer.parseInt(valueStr);
            } catch(NumberFormatException ex) {
                logger.error(notANumberMsg,ex);
                throw new Zos3270ManagerException(notANumberMsg);
            }
        }
        return cpsPropValue;
    }

    public static String defaultRows(String rows) {
        return AbstractManager.defaultString(rows,DEFAULT_TERMINAL_ROWS_STR);
    }

}
