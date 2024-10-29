/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common;

import dev.galasa.framework.spi.auth.IInternalUser;

public class InternalUser implements IInternalUser {

    private String loginId;
    private String dexUserId;

    public InternalUser(String loginId, String dexUserId) {
        this.loginId = loginId;
        this.dexUserId = dexUserId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getDexUserId() {
        return dexUserId;
    }
}
