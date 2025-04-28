/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi;

import java.util.List;

public interface IRunRasActionProcessor {
    void processRasActions(String runName, List<RunRasAction> rasActions);
}
