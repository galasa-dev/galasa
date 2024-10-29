/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * An exception which also holds a FrameworkErrorCode to provide more fine-grained detail about the problem
 * being raised/thrown, which is programmatically accessible. ie: Not just the error text, or instance of the
 * exception instance.
 *
 * Error codes can be explicitly set when the exception is created, or will default to
 * {@link FrameworkErrorDetailsBase#UNKNOWN}, or to the same error code from the cause (when that cause is also a
 * {@link FrameworkException}.
 *
 * This allows one type of FrameworkException to wrap another, and the original cause error code will
 * be propagated to the top-most FrameworkException in the chain.
 *
 */
public class FrameworkException extends Exception {
    private static final long serialVersionUID = 1L;

    private FrameworkErrorDetails errorDetails ;

    public static final boolean ENABLE_SUPRESSION_FALSE = false;
    public static final boolean WRITEABLE_STACK_TRACE_TRUE = true;
    private static final Throwable NO_CAUSE = null;
    private static final String UNKNOWN_MESSAGE = "Unknown";

    public FrameworkException() {
        this( new FrameworkErrorDetailsBase(FrameworkErrorDetailsBase.UNKNOWN, UNKNOWN_MESSAGE) , NO_CAUSE, ENABLE_SUPRESSION_FALSE, WRITEABLE_STACK_TRACE_TRUE );
    }

    public FrameworkException(FrameworkErrorDetails errorDetails) {
        this(errorDetails, NO_CAUSE, ENABLE_SUPRESSION_FALSE, WRITEABLE_STACK_TRACE_TRUE );

    }

    public FrameworkException(String message) {
        this( new FrameworkErrorDetailsBase(FrameworkErrorDetailsBase.UNKNOWN, message), NO_CAUSE, ENABLE_SUPRESSION_FALSE, WRITEABLE_STACK_TRACE_TRUE );
    }

    /**
     * If the cause is a framework exception, then the error code from the cause is propagated
     * into this exception instance.
     */
    public FrameworkException(Throwable cause) {
        this( cause.getMessage() , cause , ENABLE_SUPRESSION_FALSE, WRITEABLE_STACK_TRACE_TRUE );
    }

    public FrameworkException(FrameworkErrorDetailsBase details, Throwable cause) {
        this(details, cause, ENABLE_SUPRESSION_FALSE, WRITEABLE_STACK_TRACE_TRUE );
    }

    /**
     * If the cause is a framework exception, then the error code from the cause is propagated
     * into this exception instance.
     */
    public FrameworkException(String message, Throwable cause) {
        this( message , cause , ENABLE_SUPRESSION_FALSE, WRITEABLE_STACK_TRACE_TRUE );
    }

    /**
     * If the cause is a framework exception, then the error code from the cause is propagated
     * into this exception instance.
     */
    public FrameworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        this((cause instanceof FrameworkException) ?
                ((FrameworkException)cause).getErrorDetails() :
                new FrameworkErrorDetailsBase(FrameworkErrorDetailsBase.UNKNOWN, message),
             cause, enableSuppression, writableStackTrace);
    }

    public FrameworkException(FrameworkErrorDetails errorDetails, Throwable cause, boolean enableSuppression,
                              boolean writableStackTrace) {
        super(errorDetails.getMessage(), cause, enableSuppression, writableStackTrace);
        this.errorDetails = errorDetails;
    }

    public FrameworkErrorDetails getErrorDetails() {
        return this.errorDetails;
    }

    public int getErrorCode(){
        return this.errorDetails.getErrorCode();
    }
}
