/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.auth;
import java.util.Collection;


public interface IUser {
    
    String getUserNumber();

    String getVersion();

    String getLoginId();

    Collection<IFrontEndClient> getClients();

    IFrontEndClient getClient(String clientName);

    void addClient(IFrontEndClient client);

}
