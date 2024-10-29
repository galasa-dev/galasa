/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

/**
 * Exceptions types which support it have more detail than just a string message.
 *
 * This allows a FrameworkException to have some finer-grained error information
 * which is programmatically accessible. ie: Not just the failure message text.
 *
 * We explicitly set an ID for each enumeration value, so that we have complete
 * control over it, and it will never move if the enumeration values are re-ordered,
 * or any are inserted in any order. Using the ordinal leads to issues with support
 * if the compiler allocates different values over different releases.
 */
public interface FrameworkErrorDetails {
    int getErrorCode();
    String toJson();
    String getMessage();
}