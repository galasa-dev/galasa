/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.core.manager;

import dev.galasa.framework.spi.IRun;
import dev.galasa.framework.spi.IFramework;

public interface ITestResultProvider {

    IFramework getFramework();
    IRun getTestRun();
    String getResult();
    boolean isPassed();
    boolean isFailed();

}
