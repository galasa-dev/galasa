/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.windows;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.WinBase.FILETIME;
import com.sun.jna.ptr.PointerByReference;

/**
 * Mock implementation of CredentialManagerJNA for testing.
 * Simulates Windows Credential Manager behavior without requiring native libraries.
 */
public class MockCredentialManagerJNA implements CredentialManagerJNA {

    private final Map<String, CredentialItem> credentials = new HashMap<>();
    private final Map<String, Memory> allocatedMemory = new HashMap<>();
    private int lastError = 0;
    private boolean shouldFail = false;
    private boolean isMemoryFreed = false;

    /**
     * Adds a credential to the mock store.
     * 
     * @param targetName the credential target name
     * @param username the username
     * @param password the password
     */
    public void addCredential(String targetName, String username, String password) {
        credentials.put(targetName, new CredentialItem(username, password));
    }

    /**
     * Sets the error code that will be returned by GetLastError.
     * 
     * @param errorCode the error code
     */
    public void setLastError(int errorCode) {
        this.lastError = errorCode;
        this.shouldFail = true;
    }

    /**
     * Clears all credentials and resets error state.
     * Removes references to allocated memory, making them eligible for garbage collection.
     */
    public void clear() {
        // Clear the map to remove strong references to Memory objects
        // This makes them eligible for garbage collection by JNA
        allocatedMemory.clear();
        credentials.clear();
        lastError = 0;
        shouldFail = false;
        isMemoryFreed = false;
    }

    /**
     * Gets the last error code.
     * 
     * @return the last error code
     */
    public int getLastError() {
        return lastError;
    }

    @Override
    public boolean CredReadW(WString targetName, int type, int flags, PointerByReference credential) {
        if (shouldFail) {
            return false;
        }

        String target = targetName.toString();
        CredentialItem item = credentials.get(target);

        if (item == null) {
            lastError = WindowsCredentialManager.ERROR_NOT_FOUND;
            return false;
        }

        // Create a mock WindowsCredential structure in memory
        WindowsCredential cred = new WindowsCredential();
        
        // Set the target name
        cred.TargetName = new WString(target);
        
        // Set the username
        if (item.getUsername() != null && !item.getUsername().isEmpty()) {
            cred.UserName = new WString(item.getUsername());
        }
        
        // Set the password as a byte array in UTF-16LE encoding
        if (item.getPassword() != null && !item.getPassword().isEmpty()) {
            byte[] passwordBytes = item.getPassword().getBytes(StandardCharsets.UTF_16LE);
            // Allocate memory and keep reference to prevent garbage collection
            Memory passwordMemory = new Memory(passwordBytes.length);
            passwordMemory.write(0, passwordBytes, 0, passwordBytes.length);
            allocatedMemory.put(target, passwordMemory);
            cred.CredentialBlob = passwordMemory;
            cred.CredentialBlobSize = passwordBytes.length;
        } else {
            cred.CredentialBlob = null;
            cred.CredentialBlobSize = 0;
        }
        
        // Set other required fields
        cred.Type = CRED_TYPE_GENERIC;
        cred.Flags = 0;
        cred.Persist = 2; // CRED_PERSIST_LOCAL_MACHINE
        cred.AttributeCount = 0;
        cred.LastWritten = new FILETIME();
        
        // Write the structure to memory
        cred.write();
        
        // Set the credential pointer
        credential.setValue(cred.getPointer());
        
        return true;
    }

    @Override
    public void CredFree(Pointer buffer) {
        // In the mock, we don't need to actually free memory
        // JNA will handle memory cleanup via garbage collection
        isMemoryFreed = true;
    }

    public boolean isMemoryFreed() {
        return isMemoryFreed;
    }
}
