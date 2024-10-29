/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager.internal.gherkin;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import dev.galasa.ManagerException;
import dev.galasa.core.manager.internal.CoreManagerImpl;
import dev.galasa.framework.spi.ConfigurationPropertyStoreException;
import dev.galasa.framework.spi.IConfigurationPropertyStoreService;
import dev.galasa.framework.spi.IGherkinExecutable;
import dev.galasa.framework.spi.IManager;
import dev.galasa.framework.spi.IStatementOwner;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;

public class CoreStatementOwner implements IStatementOwner {

    private final static Log logger = LogFactory.getLog(CoreStatementOwner.class);
    private final static Pattern patternVariable = Pattern.compile("(<(\\w+)>)");

    private IManager owningManager;
    private IConfigurationPropertyStoreService cpsTest;

    public CoreStatementOwner(CoreManagerImpl manager, IConfigurationPropertyStoreService cps) {
        this.owningManager = manager;
        this.cpsTest = cps;
    }

    @ExecutionMethod(keyword = GherkinKeyword.GIVEN, regex = "<(\\w+)> is test property ([\\w.]+)")
    public void storeTestProperty(IGherkinExecutable executable, Map<String,Object> testVariables) throws ManagerException {
        String variableName = executable.getRegexGroups().get(0);
        String cpsProp = executable.getRegexGroups().get(1);
        String cpsPrefix = cpsProp.substring(0, cpsProp.indexOf("."));
        String cpsSuffix = cpsProp.substring(cpsProp.indexOf(".") + 1);
        try {
            String cpsValue = this.cpsTest.getProperty(cpsPrefix, cpsSuffix);
            if(cpsValue == null) {
                throw new ManagerException("CPS property does not exist");
            }
            testVariables.put(variableName, cpsValue);
        } catch (ConfigurationPropertyStoreException e) {
            throw new ManagerException("Unable to access CPS", e);
        }
    }

    @ExecutionMethod(keyword = GherkinKeyword.THEN, regex = "Write to log \"(.*)\"")
    public void writeToLog(IGherkinExecutable executable, Map<String,Object> testVariables) {
        String output = executable.getRegexGroups().get(0);
        Matcher matcherVariable = patternVariable.matcher(output);
        while(matcherVariable.find()) {
            String variableName = matcherVariable.group(2);
            String variableValue = (String) testVariables.get(variableName);
            output = output.replace(matcherVariable.group(1), variableValue);
            matcherVariable = patternVariable.matcher(output);
        }

        logger.info(output);
    }
    
}