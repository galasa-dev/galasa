/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270;

public interface IDatastreamListener {
    
    enum DatastreamDirection {
        INBOUND,   // server to client
        OUTBOUND   // client to server
    }
    
    void datastreamUpdate(DatastreamDirection direction, String datastreamHex);

}
