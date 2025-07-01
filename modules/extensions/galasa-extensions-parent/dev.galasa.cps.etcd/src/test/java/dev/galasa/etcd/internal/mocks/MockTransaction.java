/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal.mocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import io.etcd.jetcd.Txn;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.Op;

public class MockTransaction implements Txn {

    private List<Cmp> ifComparisons = new ArrayList<>();
    private List<Op> elseOperations = new ArrayList<>();
    private List<Op> thenOperations = new ArrayList<>();

    @Override
    public Txn Else(Op... operations) {
        elseOperations.addAll(Arrays.asList(operations));
        return this;
    }

    @Override
    public Txn If(Cmp... comparisons) {
        ifComparisons.addAll(Arrays.asList(comparisons));
        return this;
    }

    @Override
    public Txn Then(Op... operations) {
        thenOperations.addAll(Arrays.asList(operations));
        return this;
    }

    @Override
    public CompletableFuture<TxnResponse> commit() {
        return CompletableFuture.completedFuture(null);
    }

    public List<Op> getThenOperations() {
        return this.thenOperations;
    }
}
