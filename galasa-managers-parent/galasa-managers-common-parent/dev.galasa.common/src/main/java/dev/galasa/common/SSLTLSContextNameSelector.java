/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.common;

import java.util.Properties;

public class SSLTLSContextNameSelector {

	public static final String JAVA_VENDOR_PROPERTY = "java.vendor";
    public static final String JAVA_VERSION_PROPERTY = "java.version";

    public String getSelectedSSLContextName() {
        return getSelectedSSLContextName(System.getProperties());
    }
    public String getSelectedSSLContextName( Properties props ) {
        boolean ibmJdk = props.getProperty(JAVA_VENDOR_PROPERTY).contains("IBM");
        String name ;
        if (ibmJdk) {
            if (props.getProperty(JAVA_VERSION_PROPERTY).startsWith("8.")) {
            	name="SSL_TLSv2"; 
            }else {
            	name ="TLSv1.2";
            }
        } else {
            name = "TLSv1.2";
        }
        return name ;
    }

}
