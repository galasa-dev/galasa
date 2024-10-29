/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.net.URL;
import java.util.Properties;

import dev.galasa.plugin.common.ErrorRaiser;
import dev.galasa.plugin.common.UrlCalculator;

public class UrlCalculatorImpl <T extends Exception> implements UrlCalculator<T> {

    private ErrorRaiser<T> errorRaiser ;

    public UrlCalculatorImpl(ErrorRaiser<T> errorRaiser) {
        this.errorRaiser = errorRaiser ;
    }
    
    public URL calculateTestCatalogUrl(String apiServerUrl , String testStream) throws T {

        // convert into a proper URL
        URL testCatalogUrl = null ;
        try {
            testCatalogUrl = new URL(apiServerUrl + "/testcatalog/" + testStream);
        } catch(Exception ex) {
            errorRaiser.raiseError(ex,"Problem publishing the test catalog. Badly formed URL to the Galasa server.");
        }
        return testCatalogUrl;
    }

    public String calculateApiServerUrl(Properties bootstrapProperties, URL bootstrapUrl) throws T {

        String apiServerUrl = null ;

        String sTestcatalogUrl = bootstrapProperties.getProperty("framework.testcatalog.url");
        if (sTestcatalogUrl == null || sTestcatalogUrl.trim().isEmpty()) {

            // There is no testcatalog URL specifically. So derive the API server URL from the bootstrap URL itself.
            String sBootstrapUrl = cleanUrlString(bootstrapUrl.toString());

            if (!sBootstrapUrl.endsWith("/bootstrap")) {
                errorRaiser.raiseError("Unable to calculate the url to the API server, the bootstrap url does not end with /bootstrap, need a framework.testcatalog.url property in the bootstrap properties.");
            }

            // strip off the '/bootstrap' ending.
            apiServerUrl = sBootstrapUrl.substring(0, sBootstrapUrl.length() - 10);
        } else {
            // Derive the API server URL from the test catalog URL.
            sTestcatalogUrl = cleanUrlString(sTestcatalogUrl);

            if (!sTestcatalogUrl.endsWith("/testcatalog")) {
                errorRaiser.raiseError("Unable to calculate the url to the API server, the framework.testcatalog.url does not end in /testcatalog");
            }

            // Strip off the /testcatalog part of the URL.
            apiServerUrl = sTestcatalogUrl.replace("/testcatalog", "");
        }
        return apiServerUrl;
    }

    private String cleanUrlString(String uncleanUrl) {
        String url = uncleanUrl ;

        // Strip off any trailing or leading spaces.
        url = url.trim();

        // Strip off the trailing slash if there is one.
        if (url.endsWith("/")) {    
            url = url.substring(0, url.length()-1);
        }
        return url;
    }

}
