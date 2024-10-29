package dev.galasa.framework.api.common.mocks;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import dev.galasa.ICredentials;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsService;

public class MockCredentialsService implements ICredentialsService {

    private Map<String, ICredentials> creds = new HashMap<>();

    private boolean throwError = false;

    public MockCredentialsService(Map<String, ICredentials> creds) {
        this.creds = creds;
    }

    @Override
    public ICredentials getCredentials(@NotNull String credentialsId) throws CredentialsException {
        if (throwError) {
            throwMockError();
        }
        return this.creds.get(credentialsId);
    }

    @Override
    public void setCredentials(String credentialsId, ICredentials credentials) throws CredentialsException {
        if (throwError) {
            throwMockError();
        }
        this.creds.put(credentialsId, credentials);
    }

    @Override
    public void deleteCredentials(String credentialsId) throws CredentialsException {
        if (throwError) {
            throwMockError();
        }
        this.creds.remove(credentialsId);
    }

    public Map<String, ICredentials> getAllCredentials() {
        return creds;
    }

    public void setThrowError(boolean throwError) {
        this.throwError = throwError;
    }

    private void throwMockError() throws CredentialsException {
        throw new CredentialsException("simulating a credentials service error");
    }
}
