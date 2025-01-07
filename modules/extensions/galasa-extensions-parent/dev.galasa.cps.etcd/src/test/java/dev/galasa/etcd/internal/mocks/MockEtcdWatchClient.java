/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.etcd.internal.mocks;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.WatchOption;

public class MockEtcdWatchClient implements Watch {

    @Override
    public Watcher watch(ByteSequence key, WatchOption option, Listener listener) {
        throw new UnsupportedOperationException("Unimplemented method 'watch'");
    }

    @Override
    public void requestProgress() {
        throw new UnsupportedOperationException("Unimplemented method 'requestProgress'");
    }
}
