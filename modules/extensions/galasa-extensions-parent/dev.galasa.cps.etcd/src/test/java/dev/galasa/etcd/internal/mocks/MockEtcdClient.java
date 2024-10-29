/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal.mocks;

import java.util.Map;

import io.etcd.jetcd.Auth;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Cluster;
import io.etcd.jetcd.Election;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import io.etcd.jetcd.Maintenance;
import io.etcd.jetcd.Watch;

public class MockEtcdClient implements Client {

    private KV kvClient;
    private boolean isClientShutDown = false;

    public MockEtcdClient(Map<String, String> kvContents) {
        this.kvClient = new MockEtcdKvClient(kvContents);
    }

    @Override
    public KV getKVClient() {
        return kvClient;
    }

    @Override
    public void close() {
        isClientShutDown = true;
    }

    public boolean isClientShutDown() {
        return isClientShutDown;
    }

    @Override
    public Auth getAuthClient() {
        throw new UnsupportedOperationException("Unimplemented method 'getAuthClient'");
    }

    @Override
    public Cluster getClusterClient() {
        throw new UnsupportedOperationException("Unimplemented method 'getClusterClient'");
    }

    @Override
    public Election getElectionClient() {
        throw new UnsupportedOperationException("Unimplemented method 'getElectionClient'");
    }

    @Override
    public Lease getLeaseClient() {
        throw new UnsupportedOperationException("Unimplemented method 'getLeaseClient'");
    }

    @Override
    public Lock getLockClient() {
        throw new UnsupportedOperationException("Unimplemented method 'getLockClient'");
    }

    @Override
    public Maintenance getMaintenanceClient() {
        throw new UnsupportedOperationException("Unimplemented method 'getMaintenanceClient'");
    }

    @Override
    public Watch getWatchClient() {
        throw new UnsupportedOperationException("Unimplemented method 'getWatchClient'");
    }
    
}
