/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.cps.etcd.spi;

import java.util.UUID;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * Provides a callback to listen for changes to a key, be it update or delete.
 * </p>
 * 
 * <p>
 * Register a listener with
 * {@link IEtcd3Client#registerWatch(IEtcd3Listener, String)}
 * </p>
 * 
 * @author Michael Baylis
 *
 */
public interface IEtcd3Listener {

    /**
     * The type of event that triggered the watch
     * 
     * @author Michael Baylis
     *
     */
    public enum Event {
    PUT,
    DELETE,
    UNKNOWN
    }

    /**
     * Called when a key has changed in the etcd3 server that is being watched.
     *
     * @param listenerId - The ID of the listener that registered the watch
     * @param event      - type of event
     * @param key        - the key that was changed
     * @param value      - the new value
     */
    void etcd3WatchEvent(@NotNull UUID listenerId, @NotNull Event event, @NotNull String key, String value);

}
