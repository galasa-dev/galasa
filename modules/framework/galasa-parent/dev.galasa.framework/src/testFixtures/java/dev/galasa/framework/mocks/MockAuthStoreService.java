/*
* Copyright contributors to the Galasa project
*
* SPDX-License-Identifier: EPL-2.0
*/
package dev.galasa.framework.mocks;


import java.time.Instant;
import java.util.*;

import dev.galasa.framework.spi.auth.IAuthStoreService;
import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IInternalAuthToken;
import dev.galasa.framework.spi.auth.IInternalUser;
import dev.galasa.framework.spi.auth.IUser;
import dev.galasa.framework.spi.utils.ITimeService;
import dev.galasa.framework.spi.auth.AuthStoreException;

import static org.assertj.core.api.Assertions.*;

public class MockAuthStoreService implements IAuthStoreService {

    public static final String DEFAULT_USER_VERSION_NUMBER = "567897867566";
    public static final String DEFAULT_USER_NUMBER = "hqjwkeh2q1223";
    public static final String DEFAULT_USER_ROLE_ID = "2";

    List<IInternalAuthToken> tokens = new ArrayList<>();
    private ITimeService timeService;
    private int tokenIdCounter = 0;

    private boolean throwException = false;
    private boolean isThrowExceptionOnDeleteToken = false ;

    public MockAuthStoreService(List<IInternalAuthToken> tokens) {
        this.tokens = tokens;
        this.timeService = new MockTimeService(Instant.now());
    }

    public MockAuthStoreService(ITimeService timeService) {
        this.timeService = timeService;
    }

    public void setThrowExceptionOnDeleteToken(boolean isThrowExceptionOnDeleteToken) {
        this.isThrowExceptionOnDeleteToken = isThrowExceptionOnDeleteToken ;
    }

    public void setThrowException(boolean throwException) {
        this.throwException = throwException;
    }

    @Override
    public List<IInternalAuthToken> getTokens() throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        return tokens;
    }

    @Override
    public void storeToken(String clientId, String description, IInternalUser owner) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        tokens.add(new MockInternalAuthToken("token-" + tokenIdCounter, description, timeService.now(), owner, clientId));
        tokenIdCounter++;
    }

    private void throwAuthStoreException() throws AuthStoreException {
        throw new AuthStoreException("simulating an unexpected failure!");
    }

    @Override
    public void deleteToken(String tokenId) throws AuthStoreException {
        if (isThrowExceptionOnDeleteToken) {
            throwAuthStoreException();
        }

        IInternalAuthToken tokenToRemove = getToken(tokenId);
        if (tokenToRemove != null) {
            tokens.remove(tokenToRemove);
        } else {
            throw new AuthStoreException("did not find token to delete!");
        }
    }

    @Override
    public IInternalAuthToken getToken(String tokenId) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }

        IInternalAuthToken tokenToReturn = null;
        for (IInternalAuthToken token : tokens) {
            if (token.getTokenId().equals(tokenId)) {
                tokenToReturn = token;
                break;
            }
        }
        return tokenToReturn;
    }

    @Override
    public List<IInternalAuthToken> getTokensByLoginId(String loginId) throws AuthStoreException {
        if (throwException) {
            throwAuthStoreException();
        }
        List<IInternalAuthToken> tokensToReturn = new ArrayList<>();
        for (IInternalAuthToken token : tokens) {
            if (token.getOwner().getLoginId().equals(loginId)) {
                tokensToReturn.add(token);
            }
        }
        return tokensToReturn;
    }

    @Override
    public Collection<IUser> getAllUsers() throws AuthStoreException {
        
        return usersByLoginId.values();

    }

    @Override
    public void deleteUser(IUser user) throws AuthStoreException {

        if(usersByLoginId.containsKey(user.getLoginId())){
            usersByLoginId.remove(user.getLoginId());
        }

    }

  
    private Map<String,IUser> usersByLoginId = new HashMap<String,IUser>();

    public void addUser(IUser user) {
        String loginId = user.getLoginId();
        usersByLoginId.put(loginId,user);
    }

    @Override
    public IUser getUserByLoginId(String loginId) throws AuthStoreException {
        return usersByLoginId.get(loginId);
    }

    @Override
    public IUser getUser(String userNumber) throws AuthStoreException {

        IUser userOut = null;

        for( IUser possibleUserMatch : usersByLoginId.values()) {
            if (possibleUserMatch.getUserNumber().equals(userNumber)) {
                userOut = possibleUserMatch;
                break;
            }
        }

        return userOut;

    }

    @Override
    public IUser updateUser(IUser userToUpdate) throws AuthStoreException {
        String loginId = userToUpdate.getLoginId();
        IUser userGot = usersByLoginId.get(loginId);
        assertThat(userGot).isNotNull();
        usersByLoginId.put(loginId,userToUpdate);

        return userToUpdate;
    }

    @Override
    public void createUser(String loginId, String clientName, String roleId) throws AuthStoreException {
        MockUser user = new MockUser();
        user.loginId = loginId ;
        MockFrontEndClient client = new MockFrontEndClient(clientName);
        client.lastLoginTime = timeService.now();
        user.addClient(client);
        user.version = DEFAULT_USER_VERSION_NUMBER ;
        user.userNumber = DEFAULT_USER_NUMBER;
        user.roleId = DEFAULT_USER_ROLE_ID;

        usersByLoginId.put(loginId, user);
    }

    @Override
    public IFrontEndClient createClient (String clientName) {
        return new MockFrontEndClient(clientName);
    }
}
