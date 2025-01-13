/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;

import java.util.List;


public interface IAuthStore {

    /**
     * Returns a list of all the token records stored in the auth store.
     *
     * @return a list of all token records stored in the auth store.
     * @throws AuthStoreException if there is an issue accessing the auth store.
     */
    List<IInternalAuthToken> getTokens() throws AuthStoreException;


    /**
     * Returns a list of token records stored in the auth store that match a given login ID.
     *
     * @return a list of all token records stored in the auth store by login ID.
     * @throws AuthStoreException if there is an issue accessing the auth store.
     */
    List<IInternalAuthToken> getTokensByLoginId(String loginId) throws AuthStoreException;

    /**
     * Stores a new token record in the auth store's tokens database.
     *
     * @param clientId    the ID of the Dex client that the token works with.
     * @param description the user-provided description of the token.
     * @param owner       the details of the user that the token belongs to.
     * @throws AuthStoreException if there is an issue accessing the auth store.
     */
    void storeToken(String clientId, String description, IInternalUser owner) throws AuthStoreException;

    /**
     * Deletes an existing token record from the auth store's tokens database.
     * 
     * @param tokenId the ID of the token record to delete.
     * @throws AuthStoreException if there was an issue accessing the auth store.
     */
    void deleteToken(String tokenId) throws AuthStoreException;

    /**
     * Returns a list of all the users using the system.
     *
     * @return a list of all users stored in the users store.
     * @throws AuthStoreException if there is an issue accessing the users store.
     */
    List<IUser> getAllUsers() throws AuthStoreException;

    /**
     * Stores a new user record in the users store's database.
     *
     * @param loginId    the loginId of the user trying to access Galasa API
     * @param clientName the name of the frontend client being used.
     * @param roleId     the id of the role this user has been assigned. A numeric value.
     * @throws AuthStoreException if there is an issue accessing the users store.
     */
    void createUser(String loginId, String clientName, String roleId) throws AuthStoreException;

    /**
     * Retrieves a user record in the users store's database.
     *
     * @param loginId    the loginId of the user trying to access Galasa API
     * @throws AuthStoreException if there is an issue accessing the users store.
     */
    IUser getUserByLoginId(String loginId) throws AuthStoreException;

    /**
     * Retrieves a user record in the users store's database.
     *
     * @param userNumber    the ID of the user record to retrieve
     * @throws AuthStoreException if there is an issue accessing the users store.
     */
    IUser getUser(String userNumber) throws AuthStoreException;

    /**
     * Updates a user record in the users store's database.
     *
     * @param user    The user that needs to be updated
     * @throws AuthStoreException if there is an issue accessing the users store.
     */
    IUser updateUser(IUser user) throws AuthStoreException;

    void shutdown() throws AuthStoreException;

    void deleteUser(IUser user) throws AuthStoreException;

    IFrontEndClient createClient(String clientName);
}
