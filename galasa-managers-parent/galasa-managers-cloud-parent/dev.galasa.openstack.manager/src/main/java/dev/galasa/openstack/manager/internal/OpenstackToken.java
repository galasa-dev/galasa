/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.openstack.manager.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

public class OpenstackToken {

    private final String  token;
    private final Instant expires;

    public OpenstackToken(String token, Instant expires) {
        this.token = token;
        this.expires = expires;
    }

    public String getToken() {
        return this.token;
    }

    public boolean isOk() {
        Instant now = Instant.now();
        now = now.plus(5, ChronoUnit.MINUTES);

        if (now.compareTo(expires) > 0) {
            return false;
        }

        return true;
    }

    public Header getHeader() {
        return new BasicHeader("X-Auth-Token", this.token);
    }

}
