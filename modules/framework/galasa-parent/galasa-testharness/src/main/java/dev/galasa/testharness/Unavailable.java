/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.testharness;

public class Unavailable extends UnsupportedOperationException {
    private static final long serialVersionUID = 1L;

    public Unavailable() {
        super("Unavailable in test harness, add if needed");
    }

}
