/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import dev.galasa.framework.spi.IGherkinExecutable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import dev.galasa.ManagerException;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.IGherkinManager;
import dev.galasa.framework.spi.IStatementOwner;

/**
 * A GherkinStatement is a single executable statement. ie: A step in the scenario.
 */
public class GherkinStatement implements IGherkinExecutable {

    private String statement;
    private GherkinKeyword keyword;
    private IGherkinManager registeredManager;
    private Method executionMethod;
    private List<String> regexGroups;
    private IStatementOwner owningClass;

    public GherkinStatement(String statement) throws TestRunException {
        this.keyword = GherkinKeyword.get(statement);
        this.statement = statement.substring(statement.indexOf(" ") + 1).trim();
    }

    public static IGherkinExecutable get(String statement) throws TestRunException {
        IGherkinExecutable executable = new GherkinStatement(statement);
        return executable;
    }

    public void registerManager(IGherkinManager manager) throws TestRunException {
        if (this.registeredManager != null) {
            throw new TestRunException("Manager already registered for statement: " + statement);
        }
        this.registeredManager = manager;
    }

    public void registerExecutionMethod(Method method, IStatementOwner owner) throws TestRunException {
        if (this.executionMethod != null) {
            throw new TestRunException("Method already registered for statement: " + statement);
        }
        this.executionMethod = method;
        this.owningClass = owner;
    }

    public IGherkinManager getRegisteredManager() {
        return this.registeredManager;
    }

    public String getValue() {
        return this.statement;
    }

    public GherkinKeyword getKeyword() {
        return this.keyword;
    }

    @Override
    public List<String> getRegexGroups() {
        return this.regexGroups;
    }

    @Override
    public void setRegexGroups(List<String> groups) {
        this.regexGroups = groups;
    }

    @Override
    public void execute(Map<String, Object> testVariables) throws ManagerException {
        try {
            this.executionMethod.invoke(this.owningClass, this, testVariables);
        } catch (Exception e) {
            throw new ManagerException("Issue executing statement", e);
        }
        
    }
    
    @Override
    public IStatementOwner getOwner() {
        return this.owningClass;
    }
}