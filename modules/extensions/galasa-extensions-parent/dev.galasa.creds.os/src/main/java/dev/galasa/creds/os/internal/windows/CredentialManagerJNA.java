/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * JNA interface to Windows Credential Manager API (Advapi32.dll).
 * 
 * <p>This interface provides access to the Windows credential management functions
 * needed to read credentials from the Windows Credential Manager.
 * 
 * <p>Key functions:
 * <ul>
 *   <li>{@link #CredReadW(WString, int, int, PointerByReference)} - Read a credential</li>
 *   <li>{@link #CredFree(Pointer)} - Free credential memory</li>
 * </ul>
 * 
 * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/wincred/">Windows Credential Management Functions</a>
 */
public interface CredentialManagerJNA extends StdCallLibrary {

    /**
     * Singleton instance of the JNA interface.
     */
    CredentialManagerJNA INSTANCE = Native.load("Advapi32", CredentialManagerJNA.class);

    /**
     * Generic credential type constant.
     * Used for credentials that are not associated with a specific logon session.
     */
    int CRED_TYPE_GENERIC = 1;

    /**
     * Reads a credential from the user's credential set.
     * 
     * <p>The credential set used is the one associated with the logon session of the current token.
     * The token must not have the user's SID disabled.
     * 
     * @param targetName the name of the credential to read (wide string)
     * @param type the type of the credential to read (use {@link #CRED_TYPE_GENERIC})
     * @param flags reserved and must be zero
     * @param credential pointer to receive the credential structure pointer
     * @return true if successful, false otherwise (use Kernel32.GetLastError() for error code)
     * 
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/wincred/nf-wincred-credreadw">CredReadW function</a>
     */
    boolean CredReadW(WString targetName, int type, int flags, PointerByReference credential);

    /**
     * Frees memory allocated by credential management functions.
     *
     * <p>This function must be called to free the memory allocated by {@link #CredReadW}.
     * Failure to call this function will result in memory leaks.
     *
     * @param buffer pointer to the buffer to free
     *
     * @see <a href="https://docs.microsoft.com/en-us/windows/win32/api/wincred/nf-wincred-credfree">CredFree function</a>
     */
    void CredFree(Pointer buffer);
}
