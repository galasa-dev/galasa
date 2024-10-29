/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.common.mocks;

import java.util.ArrayList;
import java.util.Collection;

import dev.galasa.framework.spi.auth.IFrontEndClient;
import dev.galasa.framework.spi.auth.IUser;

public class MockUser implements IUser {

    public String userNumber;
    public String version;
    public String loginId;
    public Collection<IFrontEndClient> clients = new ArrayList<IFrontEndClient>();

    @Override
    public String getUserNumber() {
        return userNumber;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getLoginId() {
        return loginId;
    }

    @Override
    public Collection<IFrontEndClient> getClients() {
        return clients;
    }

    @Override
    public IFrontEndClient getClient(String clientName) {
        IFrontEndClient match = null;
        for (IFrontEndClient client : clients) {
            if (clientName != null && clientName.equals(client.getClientName())) {
                match = client;
                break;
            }
        }
        return match;
    }

    @Override
    public void addClient(IFrontEndClient client) {
        clients.add(client);
    }

};