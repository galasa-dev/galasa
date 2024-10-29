/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.spi;

import java.net.URI;

import javax.validation.constraints.NotNull;

import dev.galasa.cps.etcd.Etcd3ManagerException;

/**
 * The service to obtain an Etcd3 Client.
 * 
 * @author Michael Baylis
 *
 */
public interface IEtcd3ClientService {

    /**
     * <p>
     * Obtain a Etcd3 client. Due to possible authentication differences. A unique
     * client will always be returned.
     * </p>
     * 
     * <p>
     * A prefix may be provided that will added to every key access the client
     * makes. This should make it easier to share an etcd3 server without fear of
     * keys clashing.
     * </p>
     * 
     * @param servers a uri of the servers.
     * @param prefix  - The prefix to use if desired, can be null or empty.
     * @return - A client implementation
     * @throws Etcd3ManagerException A failure occurred.
     */
    IEtcd3Client getClient(@NotNull URI servers, String prefix) throws Etcd3ManagerException;

    /**
     * <p>
     * Obtain a Etcd3 client. Due to possible authentication differences. A unique
     * client will always be returned.
     * </p>
     * 
     * <p>
     * A prefix may be provided that will added to every key access the client
     * makes. This should make it easier to share an etcd3 server without fear of
     * keys clashing.
     * </p>
     * 
     * @param servers             a uri of the servers.
     * @param prefix              - The prefix to use if desired, can be null or
     *                            empty.
     * @param authenticationToken - A token to authenticate with.
     * @return - A client implementation
     * @throws Etcd3ManagerException A failure occurred.
     */
    IEtcd3Client getClient(@NotNull URI servers, String prefix, String authenticationToken)
            throws Etcd3ManagerException;

    /**
     * <p>
     * Obtain a Etcd3 client. Due to possible authentication differences. A unique
     * client will always be returned.
     * </p>
     * 
     * <p>
     * A prefix may be provided that will added to every key access the client
     * makes. This should make it easier to share an etcd3 server without fear of
     * keys clashing.
     * </p>
     * 
     * @param servers  a uri of the servers.
     * @param prefix   - The prefix to use if desired, can be null or empty.
     * @param username - A username to authenticate with.
     * @param password - A username to authenticate with.
     * @return - A client implementation
     * @throws Etcd3ManagerException A failure occurred.
     */
    IEtcd3Client getClient(@NotNull URI servers, String prefix, @NotNull String username, @NotNull String password)
            throws Etcd3ManagerException;

}
