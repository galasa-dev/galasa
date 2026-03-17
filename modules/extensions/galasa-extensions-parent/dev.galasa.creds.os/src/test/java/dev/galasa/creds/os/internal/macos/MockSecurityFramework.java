/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Mock implementation of SecurityFramework for testing.
 */
public class MockSecurityFramework implements SecurityFramework {

    private final Map<String, byte[]> storage = new HashMap<>();
    private int nextStatus = errSecSuccess;
    private boolean shouldCancelAccess = false;
    private boolean shouldFailAuth = false;

    @Override
    public int SecKeychainFindGenericPassword(
            Pointer keychain,
            int serviceNameLength,
            String serviceName,
            int accountNameLength,
            String accountName,
            IntByReference passwordLength,
            PointerByReference passwordData,
            PointerByReference itemRef) {

        if (shouldCancelAccess) {
            return errSecUserCanceled;
        }

        if (shouldFailAuth) {
            return errSecAuthFailed;
        }

        String key = serviceName + ":" + accountName;
        byte[] password = storage.get(key);

        if (password == null) {
            return errSecItemNotFound;
        }

        // Allocate memory for the password
        passwordLength.setValue(password.length);
        
        if (password.length > 0) {
            Memory memory = new Memory(password.length);
            memory.write(0, password, 0, password.length);
            passwordData.setValue(memory);
        } else {
            // For empty passwords, set a null pointer
            passwordData.setValue(null);
        }
        
        // Create a mock item reference that stores the key for deletion
        Memory itemMemory = new Memory(key.length() + 1);
        itemMemory.setString(0, key);
        itemRef.setValue(itemMemory);

        return nextStatus;
    }

    @Override
    public int SecKeychainAddGenericPassword(
            Pointer keychain,
            int serviceNameLength,
            String serviceName,
            int accountNameLength,
            String accountName,
            int passwordLength,
            byte[] passwordData,
            PointerByReference itemRef) {

        if (shouldCancelAccess) {
            return errSecUserCanceled;
        }

        if (shouldFailAuth) {
            return errSecAuthFailed;
        }

        String key = serviceName + ":" + accountName;
        
        if (storage.containsKey(key)) {
            return errSecDuplicateItem;
        }

        byte[] password = new byte[passwordLength];
        System.arraycopy(passwordData, 0, password, 0, passwordLength);
        storage.put(key, password);

        // Create a mock item reference
        if (itemRef != null) {
            Memory itemMemory = new Memory(8);
            itemRef.setValue(itemMemory);
        }

        return nextStatus;
    }

    @Override
    public int SecKeychainItemDelete(Pointer itemRef) {
        if (itemRef == null) {
            return errSecItemNotFound;
        }

        // Extract the key from the item reference
        String key = itemRef.getString(0);
        
        if (storage.containsKey(key)) {
            storage.remove(key);
            return errSecSuccess;
        }
        
        return errSecItemNotFound;
    }

    @Override
    public int SecKeychainItemFreeContent(Pointer attrList, Pointer data) {
        // No-op in mock - memory management is handled by JNA
        return errSecSuccess;
    }

    @Override
    public int SecKeychainItemCopyAttributesAndData(
            Pointer itemRef,
            Pointer info,
            PointerByReference itemClass,
            PointerByReference attrList,
            IntByReference length,
            PointerByReference outData) {
        // Not implemented in mock - not needed for current tests
        return errSecSuccess;
    }

    @Override
    public int SecKeychainItemFreeAttributesAndData(Pointer attrList, Pointer data) {
        // No-op in mock - memory management is handled by JNA
        return errSecSuccess;
    }

    @Override
    public int SecItemCopyMatching(Pointer query, PointerByReference result) {
        // Not implemented in mock - will be implemented when needed for tests
        return errSecItemNotFound;
    }

    // Test helper methods

    public void setNextStatus(int status) {
        this.nextStatus = status;
    }

    public void setShouldCancelAccess(boolean shouldCancel) {
        this.shouldCancelAccess = shouldCancel;
    }

    public void setShouldFailAuth(boolean shouldFail) {
        this.shouldFailAuth = shouldFail;
    }

    public void addPassword(String serviceName, String accountName, String password) {
        String key = serviceName + ":" + accountName;
        storage.put(key, password.getBytes(StandardCharsets.UTF_8));
    }

    public void removePassword(String serviceName, String accountName) {
        String key = serviceName + ":" + accountName;
        storage.remove(key);
    }

    public boolean hasPassword(String serviceName, String accountName) {
        String key = serviceName + ":" + accountName;
        return storage.containsKey(key);
    }

    public void clear() {
        storage.clear();
        nextStatus = errSecSuccess;
        shouldCancelAccess = false;
        shouldFailAuth = false;
    }
}
