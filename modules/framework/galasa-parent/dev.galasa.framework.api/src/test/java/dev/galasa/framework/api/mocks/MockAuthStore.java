/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.mocks;

import java.util.List;

import dev.galasa.framework.spi.auth.IAuthStore;
import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.auth.AuthStoreException;

public class MockAuthStore implements IAuthStore, IAuthStoreService {

    @Override
    public List<IInternalAuthToken> getTokens() throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getTokens'");
    }

    @Override
    public List<IInternalAuthToken> getTokensByLoginId(String loginId) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getTokens'");
    }

    @Override
    public void shutdown() throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
    }

    @Override
    public void storeToken(String clientId, String description, IInternalUser owner) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'storeToken'");
    }

    @Override
    public void deleteToken(String tokenId) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteToken'");
    }

    @Override
    public IInternalAuthToken getToken(String tokenId) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getToken'");
    }

    @Override
    public List<IUser> getAllUsers() throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getAllUsers'");
    }

    @Override
    public void createUser(String loginId, String clientName) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public IUser getUserByLoginId(String loginId) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'getUserByLoginId'");
    }

    @Override
    public IUser updateUser(IUser user) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'updateUserClientActivity'");
    }

    @Override
    public void deleteUser(IUser user) throws AuthStoreException {
        throw new UnsupportedOperationException("Unimplemented method 'deleteUser'");
    }

    @Override
    public IFrontEndClient createClient(String clientName) {
        throw new UnsupportedOperationException("Unimplemented method 'createClient'");
    }
}
