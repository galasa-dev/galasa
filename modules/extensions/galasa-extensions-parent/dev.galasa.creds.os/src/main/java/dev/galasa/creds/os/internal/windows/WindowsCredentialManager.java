/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import java.nio.charset.StandardCharsets;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;

import dev.galasa.creds.os.internal.OsCredentialsException;
import dev.galasa.creds.os.internal.OsCredentialsStore;

/**
 * Wrapper for Windows Credential Manager JNA calls with error handling and validation.
 */
public class WindowsCredentialManager implements ICredentialManager {

    /**
     * Windows error code: The system cannot find the file specified.
     */
    public static final int ERROR_NOT_FOUND = 1168;

    /**
     * Windows error code: No such logon session exists.
     */
    public static final int ERROR_NO_SUCH_LOGON_SESSION = 1312;

    /**
     * Windows error code: The parameter is incorrect.
     */
    public static final int ERROR_INVALID_PARAMETER = 87;

    private final CredentialManagerJNA credManager;

    /**
     * Creates a new CredentialManagerCommand with the default JNA instance.
     */
    public WindowsCredentialManager() {
        this(CredentialManagerJNA.INSTANCE);
    }

    /**
     * Creates a new CredentialManagerCommand with the given JNA instance.
     * @param credManager the JNA instance to use for the calls.
     */
    public WindowsCredentialManager(CredentialManagerJNA credManager) {
        this.credManager = credManager;
    }

    @Override
    public synchronized CredentialItem readCredential(String targetName) throws OsCredentialsException {
        validateTargetName(targetName);

        PointerByReference credentialRef = new PointerByReference();
        WString targetNameWide = new WString(targetName);

        boolean isReadSuccessful = credManager.CredReadW(
            targetNameWide,
            CredentialManagerJNA.CRED_TYPE_GENERIC,
            0,
            credentialRef
        );

        if (!isReadSuccessful) {
            int errorCode = getLastErrorCode();
            return handleReadError(errorCode, targetName);
        }

        Pointer credentialPointer = credentialRef.getValue();
        if (credentialPointer == null) {
            throw new OsCredentialsException("CredReadW returned success but credential pointer is null");
        }

        try {
            WindowsCredential credential = new WindowsCredential(credentialPointer);
            credential.read();

            String username = extractUsername(credential);
            String password = extractPassword(credential);

            return new CredentialItem(username, password);

        } finally {
            credManager.CredFree(credentialPointer);
        }
    }

    int getLastErrorCode() {
        int errorCode = Native.getLastError();
        return errorCode;
    }

    /**
     * Validates that a target name contains only safe characters.
     * 
     * @param targetName the target name to validate
     * @throws OsCredentialsException if the target name is invalid
     */
    private void validateTargetName(String targetName) throws OsCredentialsException {
        if (targetName == null || targetName.trim().isEmpty()) {
            throw new OsCredentialsException("Target name cannot be null or empty");
        }

        if (!OsCredentialsStore.VALID_CREDENTIAL_NAME_PATTERN.matcher(targetName).matches()) {
            throw new OsCredentialsException(
                "Credentials ID contains invalid characters. " +
                "Only alphanumeric characters, dots (.), underscores (_), hyphens (-), and at symbols (@) are allowed"
            );
        }
    }

    /**
     * Handles errors from CredReadW.
     * 
     * @param errorCode the Windows error code
     * @param targetName the target name that was being read
     * @return null if credential not found
     * @throws OsCredentialsException for other errors
     */
    private CredentialItem handleReadError(int errorCode, String targetName) throws OsCredentialsException {
        if (errorCode == ERROR_NOT_FOUND) {
            return null;
        }

        if (errorCode == ERROR_NO_SUCH_LOGON_SESSION) {
            throw new OsCredentialsException(
                "No logon session exists. Please ensure you are logged in to Windows."
            );
        }

        if (errorCode == ERROR_INVALID_PARAMETER) {
            throw new OsCredentialsException(
                "Invalid parameter when reading credential. Target name: " + targetName
            );
        }

        throw new OsCredentialsException(
            "Failed to read credential from Windows Credential Manager. Error code: " + errorCode
        );
    }

    /**
     * Extracts the username from a CREDENTIAL structure.
     *
     * @param credential the credential structure
     * @return the username, or empty string if null
     */
    private String extractUsername(WindowsCredential credential) {
        WString username = credential.UserName;
        if (username == null) {
            return "";
        }
        return username.toString();
    }

    /**
     * Extracts the password from a CREDENTIAL structure.
     *
     * <p>The password is stored as a byte array in the CredentialBlob field.
     * This method converts it to a UTF-16LE string (Windows native encoding).
     *
     * @param credential the credential structure
     * @return the password, or empty string if null or empty
     */
    private String extractPassword(WindowsCredential credential) {
        Pointer credentialBlobPointer = credential.CredentialBlob;
        int credentialBlobSize = credential.CredentialBlobSize;
        if (credentialBlobPointer == null || credentialBlobSize == 0) {
            return "";
        }

        byte[] passwordBytes = credentialBlobPointer.getByteArray(0, credentialBlobSize);

        return new String(passwordBytes, StandardCharsets.UTF_16LE);
    }
}
