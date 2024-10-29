/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin;

import java.net.URI;
import java.util.List;

import dev.galasa.framework.TestRunException;

public interface IFileLineReader {
    public List<String> readLines(URI gherkinUri) throws TestRunException ;
}
