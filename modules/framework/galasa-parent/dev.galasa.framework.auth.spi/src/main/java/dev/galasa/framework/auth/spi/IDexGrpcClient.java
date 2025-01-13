/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.auth.spi;

import com.coreos.dex.api.DexOuterClass.Client;
import dev.galasa.framework.api.common.InternalServletException;

public interface IDexGrpcClient {
    Client createClient(String callbackUrl);
    Client getClient(String clientId) throws InternalServletException;
    void deleteClient(String clientId) throws InternalServletException;
    void revokeRefreshToken(String userId, String clientId) throws InternalServletException;
}
