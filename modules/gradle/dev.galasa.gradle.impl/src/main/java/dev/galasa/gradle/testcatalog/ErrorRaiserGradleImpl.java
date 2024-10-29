/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.gradle.testcatalog;

import java.text.MessageFormat;

import org.gradle.api.logging.Logger;

import dev.galasa.plugin.common.ErrorRaiser;

public class ErrorRaiserGradleImpl implements ErrorRaiser<TestCatalogException> {

    private Logger log ;

    public ErrorRaiserGradleImpl(Logger log) {
        this.log = log ;
    }

    @Override
    public void raiseError(String template, Object... params) throws TestCatalogException {
        String msg = MessageFormat.format(template,params);
        log.error(msg);
        throw new TestCatalogException(msg);
    }

    @Override
    public void raiseError(Throwable cause, String template, Object... params) throws TestCatalogException {
        String msg = MessageFormat.format(template,params)+" cause: "+cause.toString();
        log.error(msg);
        throw new TestCatalogException(msg,cause);
    }
    
}
