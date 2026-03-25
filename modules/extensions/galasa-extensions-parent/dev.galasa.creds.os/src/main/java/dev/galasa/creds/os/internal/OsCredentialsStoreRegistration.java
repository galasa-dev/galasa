/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal;

import java.net.URI;

import javax.validation.constraints.NotNull;

import org.osgi.service.component.annotations.Component;

import dev.galasa.framework.spi.IFrameworkInitialisation;
import dev.galasa.framework.spi.creds.CredentialsException;
import dev.galasa.framework.spi.creds.ICredentialsStoreRegistration;

/**
 * OSGi component that registers the OS-native credentials store with the Galasa framework.
 * 
 * <p>This registration component checks if the credentials store URI uses the "os:" scheme
 * and registers the appropriate OS-specific credentials store implementation.</p>
 * 
 * <p>Supported URI formats:</p>
 * <ul>
 *   <li>os:auto - Automatically detect the operating system</li>
 *   <li>os:macOS - Use macOS Keychain</li>
 *   <li>os:windows - Use Windows Credential Manager (not yet implemented)</li>
 *   <li>os:linux - Use Linux Secret Service (not yet implemented)</li>
 * </ul>
 */
@Component(service = { ICredentialsStoreRegistration.class })
public class OsCredentialsStoreRegistration implements ICredentialsStoreRegistration {

    @Override
    public void initialise(@NotNull IFrameworkInitialisation frameworkInitialisation) throws CredentialsException {
        URI credsUri = frameworkInitialisation.getCredentialsStoreUri();

        if (isOsUri(credsUri)) {
            OperatingSystem os = parseOperatingSystem(credsUri);
            
            if (os == OperatingSystem.UNKNOWN) {
                throw new OsCredentialsException(
                    "Unable to determine operating system from URI: " + credsUri + ". " +
                    "Supported values are: auto, macOS, windows, linux");
            }

            OsCredentialsStore store = new OsCredentialsStore(os);
            frameworkInitialisation.registerCredentialsStore(store);
        }
    }

    /**
     * Checks if the URI uses the "os:" scheme.
     * 
     * @param uri the URI to check
     * @return true if the URI uses the "os:" scheme
     */
    private boolean isOsUri(URI uri) {
        return uri != null && "os".equals(uri.getScheme());
    }

    /**
     * Parses the operating system from the URI.
     * 
     * @param uri the URI to parse (e.g., "os:auto", "os:macOS")
     * @return the parsed operating system
     */
    private OperatingSystem parseOperatingSystem(URI uri) {
        String schemeSpecificPart = uri.getSchemeSpecificPart();
        if (schemeSpecificPart == null || schemeSpecificPart.trim().isEmpty()) {
            return OperatingSystem.detect();
        }

        return OperatingSystem.fromString(schemeSpecificPart);
    }
}
