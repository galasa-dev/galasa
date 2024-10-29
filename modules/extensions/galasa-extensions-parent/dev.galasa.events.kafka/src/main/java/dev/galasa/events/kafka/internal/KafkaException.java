/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package dev.galasa.events.kafka.internal;

import dev.galasa.framework.spi.EventsException;

public class KafkaException extends EventsException {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    public KafkaException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public KafkaException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public KafkaException(Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     */
    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }

}
