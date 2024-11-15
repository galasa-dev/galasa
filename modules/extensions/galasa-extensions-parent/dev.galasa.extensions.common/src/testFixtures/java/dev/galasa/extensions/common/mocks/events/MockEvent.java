/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.extensions.common.mocks.events;

import java.util.UUID;

import dev.galasa.framework.spi.events.IEvent;

public class MockEvent implements IEvent {

    private String id;
    private String timestamp;
    private String message;

    public MockEvent(String timestamp, String message) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = timestamp;
        this.message = message;
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Unimplemented method 'getId'");
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException("Unimplemented method 'setId'");
    }

    @Override
    public String getTimestamp() {
        throw new UnsupportedOperationException("Unimplemented method 'getTimestamp'");
    }

    @Override
    public void setTimestamp(String timestamp) {
        throw new UnsupportedOperationException("Unimplemented method 'setTimestamp'");
    }

    @Override
    public String getMessage() {
        throw new UnsupportedOperationException("Unimplemented method 'getMessage'");
    }

    @Override
    public void setMessage(String message) {
        throw new UnsupportedOperationException("Unimplemented method 'setMessage'");
    }
    
}
