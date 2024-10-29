/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.time.Instant;

import dev.galasa.framework.spi.auth.IFrontEndClient;

public class MockFrontEndClient implements IFrontEndClient {

    public String name ;
    public Instant lastLoginTime ;

    public MockFrontEndClient( String name){
        this.name = name ;
    }

    @Override
    public String getClientName() {
        return name;
    }

    @Override
    public Instant getLastLogin() {
        return lastLoginTime;
    }

    @Override
    public void setLastLogin(Instant lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

}