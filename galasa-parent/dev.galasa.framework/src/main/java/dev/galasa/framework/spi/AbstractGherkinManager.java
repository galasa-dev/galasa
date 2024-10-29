/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.validation.constraints.NotNull;

import dev.galasa.ManagerException;
import dev.galasa.framework.TestRunException;
import dev.galasa.framework.spi.language.gherkin.ExecutionMethod;
import dev.galasa.framework.spi.language.gherkin.GherkinTest;

public abstract class AbstractGherkinManager extends AbstractManager implements IGherkinManager {

    public Boolean registerStatements(@NotNull GherkinTest test, @NotNull IStatementOwner[] owners) throws ManagerException {
        Boolean required = false;
        Class<?>[] methodParams = { IGherkinExecutable.class , Map.class };
        try {
            for(IGherkinExecutable executable : test.getAllExecutables()) {
                for(IStatementOwner owner : owners) {
                    for(Method method : owner.getClass().getDeclaredMethods()) {
                        if(Arrays.equals(method.getParameterTypes(), methodParams)) {
                            ExecutionMethod executeAnno = method.getAnnotation(ExecutionMethod.class);
                            if(executeAnno != null && executeAnno.keyword().equals(executable.getKeyword())) {
                                Pattern annotationRegex = Pattern.compile(executeAnno.regex());
                                Matcher regexMatcher = annotationRegex.matcher(executable.getValue());
                                if(regexMatcher.matches()) {
                                    List<String> groups = new ArrayList<>();
                                    for(int i = 1; i <= regexMatcher.groupCount(); i++) {
                                        groups.add(regexMatcher.group(i));
                                    }
                                    executable.setRegexGroups(groups);
                                    executable.registerManager(this);
                                    executable.registerExecutionMethod(method, owner);
                                    required = true;
                                }
                            }
                        }
                    }
                }
            }
            return required;
        } catch (TestRunException e) {
            throw new ManagerException("Issue registering statements", e);
        }
    }
}
