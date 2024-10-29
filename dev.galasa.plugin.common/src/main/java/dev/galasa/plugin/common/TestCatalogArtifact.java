/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

import java.io.OutputStream;

public interface TestCatalogArtifact<Ex extends Exception> {
    void transferTo(OutputStream outputStream) throws Ex;
}
