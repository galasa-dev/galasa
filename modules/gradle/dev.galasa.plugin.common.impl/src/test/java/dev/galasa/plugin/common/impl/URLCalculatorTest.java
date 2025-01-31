/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.plugin.common.impl;

import java.net.URL;
import java.util.Properties;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

import dev.galasa.plugin.common.ErrorRaiser;
import dev.galasa.plugin.common.test.MockErrorRaiser;
import dev.galasa.plugin.common.test.MockException;
import dev.galasa.plugin.common.test.MockLog;

public class URLCalculatorTest {

    @Test
    public void testCalculateTestCatalogUrlAddsSuffixesOk() throws Exception {
        String streamName = "mystream";
        String apiServerUrl = "https://myserver/api";
        MockLog mockLog = new MockLog();
        ErrorRaiser<MockException> raiser = new MockErrorRaiser(mockLog);
        UrlCalculatorImpl<MockException> calculator = new UrlCalculatorImpl<MockException>(raiser);
        URL calculatedUrl = calculator.calculateTestCatalogUrl(apiServerUrl, streamName);
        assertThat(calculatedUrl.toString()).isEqualTo("https://myserver/api/testcatalog/mystream");
    }


    @Test
    public void testCalculateApiServerUrlUsesBootstrapPropertyIfSet() throws Exception {
        Properties props = new Properties();
        props.setProperty("framework.testcatalog.url", "https://my.explicitly.set/testcatalog");
        URL bootstrapUrl = new URL("https://my/bootstrap");
        MockLog mockLog = new MockLog();
        ErrorRaiser<MockException> raiser = new MockErrorRaiser(mockLog);
        UrlCalculatorImpl<MockException> calculator = new UrlCalculatorImpl<MockException>(raiser);
        String calculatedUrl = calculator.calculateApiServerUrl(props, bootstrapUrl);
        assertThat(calculatedUrl).isEqualTo("https://my.explicitly.set");
    }

    @Test
    public void testCalculateTestCatalogUrlUsesBootstrapUrlByDefault() throws Exception {
        Properties props = new Properties();
        MockLog mockLog = new MockLog();
        ErrorRaiser<MockException> raiser = new MockErrorRaiser(mockLog);
        UrlCalculatorImpl<MockException> calculator = new UrlCalculatorImpl<MockException>(raiser);        // props.setProperty("framework.testcatalog.url", "https://my.explicitly.set.bootstrap.url");
        String calculatedUrl = calculator.calculateApiServerUrl(props, new URL("https://my.bootstrap.url/bootstrap"));
        assertThat(calculatedUrl.toString()).isEqualTo("https://my.bootstrap.url");
    }

    @Test
    public void testCalculateTestCatalogUrlSpotsBootstrapUrlWhichDoesntEndInBootstrap() throws Exception {
        Properties props = new Properties();
        MockLog mockLog = new MockLog();
        ErrorRaiser<MockException> raiser = new MockErrorRaiser(mockLog);
        UrlCalculatorImpl<MockException> calculator = new UrlCalculatorImpl<MockException>(raiser);        // props.setProperty("framework.testcatalog.url", "https://my.explicitly.set/bootstrap-not-at-end");
        Exception ex = catchException( ()-> calculator.calculateApiServerUrl(props, new URL("https://my.bootstrap.url/bootstrap-not-at-end")) );
        assertThat(ex).isInstanceOf(MockException.class);
        
        assertThat(ex).hasMessageContaining("Unable to calculate the url to the API server, the bootstrap url does not end with /bootstrap, need a framework.testcatalog.url property in the bootstrap");
    }

}
