/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.GherkinKeyword;

public interface IGherkinExecutable {

    IGherkinManager getRegisteredManager();

    void execute(@NotNull Map<String, Object> testVariables) throws ManagerException;

    void registerManager(@NotNull IGherkinManager manager) throws TestRunException;

    void registerExecutionMethod(@NotNull Method method, IStatementOwner owner) throws TestRunException;

    String getValue();

    GherkinKeyword getKeyword();

    List<String> getRegexGroups();

    void setRegexGroups(@NotNull List<String> groups);
    
    Object getOwner();
    
}