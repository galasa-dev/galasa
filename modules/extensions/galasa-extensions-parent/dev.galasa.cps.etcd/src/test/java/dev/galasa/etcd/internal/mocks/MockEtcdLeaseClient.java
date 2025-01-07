/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal.mocks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import io.etcd.jetcd.Lease;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lease.LeaseRevokeResponse;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.options.LeaseOption;
import io.etcd.jetcd.support.CloseableClient;
import io.grpc.stub.StreamObserver;

public class MockEtcdLeaseClient implements Lease {

    private List<LeaseGrantResponse> leases = new ArrayList<>();

    @Override
    public CompletableFuture<LeaseGrantResponse> grant(long ttl) {
        LeaseGrantResponse mockLeaseResponse = new LeaseGrantResponse(
            io.etcd.jetcd.api.LeaseGrantResponse.newBuilder()
            .setID(123)
            .setTTL(ttl)
            .build()
        );
        leases.add(mockLeaseResponse);
        return CompletableFuture.completedFuture(mockLeaseResponse);
    }

    public List<LeaseGrantResponse> getLeases() {
        return leases;
    }

    @Override
    public CompletableFuture<LeaseGrantResponse> grant(long ttl, long timeout, TimeUnit unit) {
        throw new UnsupportedOperationException("Unimplemented method 'grant'");
    }

    @Override
    public CompletableFuture<LeaseRevokeResponse> revoke(long leaseId) {
        throw new UnsupportedOperationException("Unimplemented method 'revoke'");
    }

    @Override
    public CompletableFuture<LeaseKeepAliveResponse> keepAliveOnce(long leaseId) {
        throw new UnsupportedOperationException("Unimplemented method 'keepAliveOnce'");
    }

    @Override
    public CompletableFuture<LeaseTimeToLiveResponse> timeToLive(long leaseId, LeaseOption leaseOption) {
        throw new UnsupportedOperationException("Unimplemented method 'timeToLive'");
    }

    @Override
    public CloseableClient keepAlive(long leaseId, StreamObserver<LeaseKeepAliveResponse> observer) {
        throw new UnsupportedOperationException("Unimplemented method 'keepAlive'");
    }
    
}
