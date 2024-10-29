/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import dev.galasa.framework.spi.FrameworkException;

public class InternalServletException extends FrameworkException {

    ServletError servletError;
    int httpFailureCode;

    public InternalServletException(ServletError servletError, int httpFailureCode ){
        super(servletError.getMessage());
        this.servletError = servletError;
        this.httpFailureCode = httpFailureCode;
    }

    public InternalServletException(ServletError servletError, int httpFailureCode , Throwable cause){
        super(servletError.getMessage(),cause);
        this.servletError = servletError;
        this.httpFailureCode = httpFailureCode;
    }

    public ServletError getError() {
        return this.servletError;
    }
    public int getHttpFailureCode(){
        return this.httpFailureCode;
    }

    @Override
    public String getMessage() {
        return servletError.toJsonString();
    }
}