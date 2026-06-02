/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import java.util.Map;
import java.util.regex.Pattern;

import dev.galasa.ICredentials;
import dev.galasa.creds.os.internal.macos.MacOsKeychainStore;
import dev.galasa.creds.os.internal.windows.WindowsCredentialManagerStore;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStore;

/**
 * OS-native credentials store that delegates to platform-specific implementations.
 */
public class OsCredentialsStore implements ICredentialsStore {

    /**
     * Pattern for validating credential names.
     * Allows alphanumeric characters, dots, underscores, hyphens, and @ symbols.
     */
    public static final Pattern VALID_CREDENTIAL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._@-]+$");

    private final ICredentialsStore delegate;
    private final OperatingSystem os;


    /**
     * Creates an OS credentials store for the specified operating system.
     *
     * @param os the operating system to use
     * @throws OsCredentialsException if the operating system is not supported
     */
    public OsCredentialsStore(OperatingSystem os) throws OsCredentialsException {
        this.os = os;
        this.delegate = createDelegate(os);
    }

    /**
     * Creates an OS credentials store with a specific delegate.
     * Package-private constructor for testing.
     *
     * @param os the operating system
     * @param delegate the credentials store delegate
     */
    OsCredentialsStore(OperatingSystem os, ICredentialsStore delegate) {
        this.os = os;
        this.delegate = delegate;
    }

    private ICredentialsStore createDelegate(OperatingSystem os) throws OsCredentialsException {
        switch (os) {
            case MACOS:
                return new MacOsKeychainStore();
            case WINDOWS:
                return new WindowsCredentialManagerStore();
            case LINUX:
                throw new OsCredentialsException(
                    "Linux Secret Service is not yet implemented. " +
                    "Please use a different credentials store.");
            case UNKNOWN:
            default:
                throw new OsCredentialsException(
                    "Unsupported operating system: " + os.getDisplayName() + ". " +
                    "Supported operating systems are: macOS, Windows");
        }
    }

    @Override
    public ICredentials getCredentials(String credsId) throws CredentialsException {
        return delegate.getCredentials(credsId);
    }

    @Override
    public Map<String, ICredentials> getAllCredentials() throws CredentialsException {
        return delegate.getAllCredentials();
    }

    @Override
    public void setCredentials(String credsId, ICredentials credentials) throws CredentialsException {
        delegate.setCredentials(credsId, credentials);
    }

    @Override
    public void deleteCredentials(String credsId) throws CredentialsException {
        delegate.deleteCredentials(credsId);
    }

    @Override
    public void shutdown() throws CredentialsException {
        delegate.shutdown();
    }

    /**
     * Gets the operating system this store is configured for.
     * 
     * @return the operating system
     */
    public OperatingSystem getOperatingSystem() {
        return os;
    }
}
