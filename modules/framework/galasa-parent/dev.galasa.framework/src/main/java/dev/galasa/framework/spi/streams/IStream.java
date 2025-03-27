/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.streams;

import java.net.URL;
import java.util.List;

public interface IStream {

    String getName();

    String getDescription();

    URL getMavenRepositoryUrl();

    URL getTestCatalogUrl();

    List<String> getObrs();

    boolean getIsEnabled();

    boolean isValid();

}
