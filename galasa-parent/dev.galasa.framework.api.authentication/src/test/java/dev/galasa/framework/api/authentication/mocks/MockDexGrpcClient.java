/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.authentication.mocks;

import java.util.ArrayList;
import java.util.List;

import com.coreos.dex.api.DexOuterClass.Client;
import com.coreos.dex.api.DexOuterClass.CreateClientReq;
import com.coreos.dex.api.DexOuterClass.CreateClientResp;
import com.coreos.dex.api.DexOuterClass.DeleteClientReq;
import com.coreos.dex.api.DexOuterClass.DeleteClientResp;
import com.coreos.dex.api.DexOuterClass.GetClientReq;
import com.coreos.dex.api.DexOuterClass.GetClientResp;
import com.coreos.dex.api.DexOuterClass.RevokeRefreshReq;
import com.coreos.dex.api.DexOuterClass.RevokeRefreshResp;
import com.coreos.dex.api.DexOuterClass.CreateClientResp.Builder;

import dev.galasa.framework.api.authentication.internal.DexGrpcClient;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

// A class that only mocks out methods that interact with Dex's gRPC service
public class MockDexGrpcClient extends DexGrpcClient {

    private List<Client> dexClients = new ArrayList<>();
    private List<String> refreshTokens = new ArrayList<>();

    private boolean throwError = false;

    public MockDexGrpcClient(String issuerHostname, String clientId, String clientSecret, String callbackUrl) {
        super(issuerHostname, "http://my-ecosystem");
        addDexClient(clientId, clientSecret, callbackUrl);
    }

    public MockDexGrpcClient(String issuerHostname) {
        super(issuerHostname, "http://my-ecosystem");
    }

    // Mock out the creation of the blocking stub
    @Override
    public void initialiseBlockingStub(String issuerHostname) {
        // Do nothing...
    }

    // Mock out the response from Dex's gRPC API
    @Override
    public CreateClientResp sendCreateClientRequest(CreateClientReq createClientReq) {
        checkThrowError();
        Builder createClientRespBuilder = CreateClientResp.newBuilder();
        if (dexClients != null && !dexClients.isEmpty()) {
            createClientRespBuilder.setClient(dexClients.get(0));
        }
        return createClientRespBuilder.build();
    }

    @Override
    public GetClientResp sendGetClientRequest(GetClientReq getClientReq) {
        checkThrowError();
        com.coreos.dex.api.DexOuterClass.GetClientResp.Builder getClientRespBuilder = GetClientResp.newBuilder();
        if (dexClients != null && !dexClients.isEmpty()) {
            Client dexClient = dexClients.get(0);

            getClientRespBuilder.setClient(dexClient);
        }
        return getClientRespBuilder.build();
    }

    @Override
    public DeleteClientResp sendDeleteClientRequest(DeleteClientReq deleteClientReq) {
        checkThrowError();
        com.coreos.dex.api.DexOuterClass.DeleteClientResp.Builder deleteClientRespBuilder = DeleteClientResp.newBuilder();
        String clientId = deleteClientReq.getId();
        Client clientToRemove = getDexClient(clientId);
        deleteClientRespBuilder.setNotFound(true);

        if (clientToRemove != null) {
            dexClients.remove(clientToRemove);
            deleteClientRespBuilder.setNotFound(false);
        }
        return deleteClientRespBuilder.build();
    }

    @Override
    public RevokeRefreshResp sendRevokeRefreshRequest(RevokeRefreshReq revokeRefreshReq) {
        checkThrowError();
        com.coreos.dex.api.DexOuterClass.RevokeRefreshResp.Builder builder = RevokeRefreshResp.newBuilder();
        String clientId = revokeRefreshReq.getClientId();
        builder.setNotFound(true);

        if (!clientId.equals("notfound")) {
            String tokenToRemove = null;
            for (String refreshToken : refreshTokens) {
                if (refreshToken.endsWith(clientId)) {
                    tokenToRemove = refreshToken;
                }
            }
            if (tokenToRemove != null) {
                refreshTokens.remove(tokenToRemove);
                builder.setNotFound(false);
            }
        }
        return builder.build();
    }

    public void addDexClient(String clientId, String clientSecret, String callbackUrl) {
        com.coreos.dex.api.DexOuterClass.Client.Builder clientBuilder = Client.newBuilder();
        clientBuilder.setId(clientId);
        clientBuilder.setSecret(clientSecret);
        clientBuilder.addRedirectUris(callbackUrl);

        dexClients.add(clientBuilder.build());
    }

    public void addMockRefreshToken(String userId, String clientId) {
        refreshTokens.add(userId + clientId);
    }

    private Client getDexClient(String clientId) {
        Client clientToReturn = null;
        for (Client dexClient : dexClients) {
            if (dexClient.getId().equals(clientId)) {
                clientToReturn = dexClient;
                break;
            }
        }
        return clientToReturn;
    }

    public List<Client> getDexClients() {
        return dexClients;
    }

    public List<String> getRefreshTokens() {
        return refreshTokens;
    }

    public void setThrowError(boolean throwError) {
        this.throwError = throwError;
    }

    private void checkThrowError() throws StatusRuntimeException {
        if (throwError) {
            throw new StatusRuntimeException(Status.UNKNOWN);
        }
    }
}