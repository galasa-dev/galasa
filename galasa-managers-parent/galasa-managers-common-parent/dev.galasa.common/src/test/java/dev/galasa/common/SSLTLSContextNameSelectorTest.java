/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.common;

import org.junit.Test;

import static org.junit.Assert.* ;
import java.util.Properties;

public class SSLTLSContextNameSelectorTest {
    @Test
    public void testNonIBMJVMGivesTLS12Context() throws Exception {
        Properties testProps = new Properties();
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VENDOR_PROPERTY,"FakeJavaRuntimeVendorName" );
        // When...
        String contextName = new SSLTLSContextNameSelector().getSelectedSSLContextName(testProps);
        // Then...
        String expected = "TLSv1.2";
        assertEquals(expected,contextName);
    }
    @Test
    public void testIBMJVM8GivesSSL_TLSv2Context() throws Exception {
        // Given...
        Properties testProps = new Properties();
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VENDOR_PROPERTY,"IBMJavaRuntimeVendorName" );
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VERSION_PROPERTY, "8.0.16");
        // When...
        String contextName = new SSLTLSContextNameSelector().getSelectedSSLContextName(testProps);
        // Then...
        String expected = "SSL_TLSv2";
        assertEquals(expected,contextName);
    }
    
    @Test
    public void testIBMJVM11GivesTLSv12Context() throws Exception {
        // Given...
        Properties testProps = new Properties();
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VENDOR_PROPERTY,"IBMJavaRuntimeVendorName" );
        testProps.setProperty(SSLTLSContextNameSelector.JAVA_VERSION_PROPERTY, "11.0.16.1+1");
        // When...
        String contextName = new SSLTLSContextNameSelector().getSelectedSSLContextName(testProps);
        // Then...
        String expected = "TLSv1.2";
        assertEquals(expected,contextName);
    }
}