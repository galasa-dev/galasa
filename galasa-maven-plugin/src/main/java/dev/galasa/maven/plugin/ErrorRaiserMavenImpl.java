/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.maven.plugin;

import java.text.MessageFormat;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import dev.galasa.plugin.common.ErrorRaiser;

// Logs errors, then raises an exception.
public class ErrorRaiserMavenImpl implements ErrorRaiser <MojoExecutionException>{

    Log log ;

    public ErrorRaiserMavenImpl(Log log) {
        this.log = log;
    }

    @Override
    public void raiseError(String template, Object...  parameters) throws MojoExecutionException {
        String msg = MessageFormat.format(template,parameters);
        log.error(msg);
        throw new MojoExecutionException(msg);
    }

    @Override
    public void raiseError( Throwable cause, String template, Object...  parameters) throws MojoExecutionException {
        String msg = MessageFormat.format(template,parameters)+" cause:"+cause.toString();
        log.error(msg);
        throw new MojoExecutionException(msg,cause);
    }
}