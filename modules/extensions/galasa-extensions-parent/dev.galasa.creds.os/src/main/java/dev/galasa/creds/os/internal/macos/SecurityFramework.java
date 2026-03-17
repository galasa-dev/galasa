/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.creds.os.internal.macos;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * JNA interface to macOS Security Framework and Core Foundation for Keychain operations.
 *
 * @see <a href="https://developer.apple.com/documentation/security/keychain_services">Keychain Services</a>
 * @see <a href="https://developer.apple.com/documentation/corefoundation">Core Foundation</a>
 */
public interface SecurityFramework extends Library {
    
    SecurityFramework INSTANCE = Native.load("Security", SecurityFramework.class);

    // Error codes
    int errSecSuccess = 0;
    int errSecItemNotFound = -25300;
    int errSecUserCanceled = -128;
    int errSecAuthFailed = -25293;
    int errSecDuplicateItem = -25299;

    // Keychain item class constants (CFString keys)
    // These are extern const CFStringRef values that need to be looked up at runtime
    // For JNA, we'll use string literals that match the actual constant names
    String kSecClass = "class";
    String kSecClassGenericPassword = "genp";
    String kSecAttrService = "svce";
    String kSecAttrAccount = "acct";
    String kSecValueData = "v_Data";
    String kSecReturnAttributes = "r_Attributes";
    String kSecReturnData = "r_Data";
    String kSecMatchLimit = "m_Limit";
    String kSecMatchLimitOne = "m_LimitOne";

    /**
     * Finds a generic password in the keychain.
     * 
     * @param keychainOrArray   pointer to keychain or array of keychains (null for default)
     * @param serviceNameLength length of service name
     * @param serviceName       service name string
     * @param accountNameLength length of account name
     * @param accountName       account name string
     * @param passwordLength    output: length of password data
     * @param passwordData      output: pointer to password data
     * @param itemRef           output: reference to keychain item (can be null)
     * @return status code (0 for success)
     */
    int SecKeychainFindGenericPassword(
        Pointer keychainOrArray,
        int serviceNameLength,
        String serviceName,
        int accountNameLength,
        String accountName,
        IntByReference passwordLength,
        PointerByReference passwordData,
        PointerByReference itemRef
    );

    /**
     * Adds a generic password to the keychain.
     * 
     * @param keychain          pointer to keychain (null for default)
     * @param serviceNameLength length of service name
     * @param serviceName       service name string
     * @param accountNameLength length of account name
     * @param accountName       account name string
     * @param passwordLength    length of password data
     * @param passwordData      password data bytes
     * @param itemRef           output: reference to created item (can be null)
     * @return status code (0 for success)
     */
    int SecKeychainAddGenericPassword(
        Pointer keychain,
        int serviceNameLength,
        String serviceName,
        int accountNameLength,
        String accountName,
        int passwordLength,
        byte[] passwordData,
        PointerByReference itemRef
    );

    /**
     * Deletes a keychain item.
     * 
     * @param itemRef reference to keychain item
     * @return status code (0 for success)
     */
    int SecKeychainItemDelete(Pointer itemRef);

    /**
     * Frees memory allocated by keychain operations.
     *
     * @param attrList attribute list pointer (can be null)
     * @param data     data pointer to free
     * @return status code (0 for success)
     */
    int SecKeychainItemFreeContent(Pointer attrList, Pointer data);

    /**
     * Copies attributes and data from a keychain item.
     *
     * @param itemRef       reference to keychain item
     * @param info          attribute info (can be null to get all attributes)
     * @param itemClass     output: item class (can be null)
     * @param attrList      output: attribute list (can be null)
     * @param length        output: length of data
     * @param outData       output: pointer to data
     * @return status code (0 for success)
     */
    int SecKeychainItemCopyAttributesAndData(
        Pointer itemRef,
        Pointer info,
        PointerByReference itemClass,
        PointerByReference attrList,
        IntByReference length,
        PointerByReference outData
    );

    /**
     * Frees attribute list memory.
     *
     * @param attrList attribute list to free
     * @return status code (0 for success)
     */
    int SecKeychainItemFreeAttributesAndData(Pointer attrList, Pointer data);

    /**
     * Searches for keychain items matching a query dictionary.
     * This is the modern API for searching keychain items.
     *
     * @param query CFDictionary containing search parameters
     * @param result output: CFDictionary or CFArray of matching items
     * @return status code (0 for success)
     */
    int SecItemCopyMatching(Pointer query, PointerByReference result);
}
