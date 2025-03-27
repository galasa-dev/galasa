/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.streams;

public interface IStream {

    String getName();

    String getDescription();

    String getMavenRepositoryUrl();

    String getTestCatalogUrl();

    String getObrLocation();

    boolean getIsEnabled();

}
