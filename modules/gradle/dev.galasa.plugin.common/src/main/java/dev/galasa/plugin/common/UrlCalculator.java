/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common;

import java.net.URL;
import java.util.Properties;

public interface UrlCalculator<T extends Exception> {
    URL calculateTestCatalogUrl(String apiServerUrl , String testStream) throws T ;
    String calculateApiServerUrl(Properties bootstrapProperties, URL bootstrapUrl) throws T;
}
