/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.http;

public enum TLS {

    v1_0(org.apache.hc.core5.http.ssl.TLS.V_1_0),
    v1_1(org.apache.hc.core5.http.ssl.TLS.V_1_1),
    v1_2(org.apache.hc.core5.http.ssl.TLS.V_1_2),
    v1_3(org.apache.hc.core5.http.ssl.TLS.V_1_3);


    private final org.apache.hc.core5.http.ssl.TLS tls;

    TLS(org.apache.hc.core5.http.ssl.TLS tls) {
        this.tls = tls;
    }

    public org.apache.hc.core5.http.ssl.TLS getTls() {
        return tls;
    }

}
