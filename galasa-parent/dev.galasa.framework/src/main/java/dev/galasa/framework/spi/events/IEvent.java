/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.events;

public interface IEvent {

    String getId();

    void setId(String id);

    String getTimestamp();

    void setTimestamp(String timestamp);

    String getMessage();

    void setMessage(String message);
    
}
