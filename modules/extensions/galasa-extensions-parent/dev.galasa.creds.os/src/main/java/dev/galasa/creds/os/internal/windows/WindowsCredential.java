/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase.FILETIME;

/**
 * JNA mapping of the Windows CREDENTIAL structure.
 * 
 * <p>This structure contains information about a credential stored in the Windows Credential Manager.
 * It maps to the CREDENTIALW structure in the Windows API.
 * 
 * <p>Key fields:
 * <ul>
 *   <li>{@link #TargetName} - The credential identifier</li>
 *   <li>{@link #UserName} - The username associated with the credential</li>
 *   <li>{@link #CredentialBlob} - Pointer to the password/secret data</li>
 *   <li>{@link #CredentialBlobSize} - Size of the credential blob in bytes</li>
 * </ul>
 * 
 * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/wincred/ns-wincred-credentialw">CREDENTIALW structure</a>
 */
public class WindowsCredential extends Structure {

    /**
     * Creates a CREDENTIAL structure from a pointer.
     *
     * @param pointer pointer to the native CREDENTIAL structure
     */
    public WindowsCredential(Pointer pointer) {
        super(pointer);
    }

    /**
     * Default constructor for creating an empty CREDENTIAL structure.
     */
    public WindowsCredential() {
        super();
    }

    /**
     * Flags for the credential.
     */
    public int Flags;

    /**
     * Type of the credential (e.g., CRED_TYPE_GENERIC).
     */
    public int Type;

    /**
     * The name of the credential (target name).
     */
    public WString TargetName;

    /**
     * Optional comment string.
     */
    public WString Comment;

    /**
     * Time stamp of the last modification.
     */
    public FILETIME LastWritten;

    /**
     * Size of the CredentialBlob in bytes.
     */
    public int CredentialBlobSize;

    /**
     * Pointer to the credential data (password/secret).
     */
    public Pointer CredentialBlob;

    /**
     * Persistence of the credential.
     */
    public int Persist;

    /**
     * Number of attributes.
     */
    public int AttributeCount;

    /**
     * Pointer to attributes array.
     */
    public Pointer Attributes;

    /**
     * Alias for the target name.
     */
    public WString TargetAlias;

    /**
     * Username associated with the credential.
     */
    public WString UserName;

    /**
     * Defines the field order for JNA structure mapping.
     * This must match the order of fields in the Windows CREDENTIALW structure.
     * 
     * @return list of field names in the correct order
     */
    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
            "Flags",
            "Type",
            "TargetName",
            "Comment",
            "LastWritten",
            "CredentialBlobSize",
            "CredentialBlob",
            "Persist",
            "AttributeCount",
            "Attributes",
            "TargetAlias",
            "UserName"
        );
    }
}
