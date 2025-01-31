/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.auth.spi;

import dev.galasa.framework.api.common.InternalServletException;
import dev.galasa.framework.spi.auth.IAuthStoreService;

public interface IAuthService {

    void revokeToken(String tokenId, String requestorUserLoginId) throws InternalServletException;

    IDexGrpcClient getDexGrpcClient();
    IAuthStoreService getAuthStoreService();
}
