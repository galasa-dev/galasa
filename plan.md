# macOS Keychain Credentials Store Implementation Plan

## Overview
Create a new Galasa extension that implements the [`ICredentialsStore`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/ICredentialsStore.java) interface to read credentials from the operating system's native credential storage (macOS Keychain, Windows Credential Manager, Linux Secret Service).

## User Experience

Users will:
1. Store credentials in their OS-native credential manager (e.g., macOS Keychain Access application)
2. Configure Galasa to use the OS credentials store by setting properties in `~/.galasa/bootstrap.properties`:
   ```properties
   framework.credentials.store=os:auto
   framework.extra.bundles=dev.galasa.creds.os
   ```

### Supported URI Schemes
- `os:auto` - Automatically detect the operating system
- `os:macOS` - Explicitly use macOS Keychain
- `os:windows` - Explicitly use Windows Credential Manager
- `os:linux` - Explicitly use Linux Secret Service

## Architecture

### Module Structure
Create a new extension module: `dev.galasa.creds.os`

Location: `modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/`

### Key Components

#### 1. Registration Component
**Class**: [`OsCredentialsStoreRegistration`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/src/main/java/dev/galasa/creds/os/internal/OsCredentialsStoreRegistration.java)
- Implements [`ICredentialsStoreRegistration`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/ICredentialsStoreRegistration.java)
- OSGi component that registers the credentials store with the framework
- Parses the `framework.credentials.store` URI to determine OS type
- Registers the appropriate OS-specific credentials store implementation

#### 2. Store Implementation
**Class**: [`OsCredentialsStore`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/src/main/java/dev/galasa/creds/os/internal/OsCredentialsStore.java)
- Implements [`ICredentialsStore`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/ICredentialsStore.java)
- Delegates to OS-specific implementations based on detected/configured platform
- Methods to implement:
  - `getCredentials(String credsId)` - Retrieve credentials by ID
  - `getAllCredentials()` - Retrieve all stored credentials
  - `setCredentials(String credsId, ICredentials credentials)` - Store credentials
  - `deleteCredentials(String credsId)` - Remove credentials
  - `shutdown()` - Clean up resources

#### 3. OS-Specific Implementations

##### macOS Implementation
**Class**: [`MacOsKeychainStore`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/src/main/java/dev/galasa/creds/os/internal/macos/MacOsKeychainStore.java)
- Uses JNA (Java Native Access) to call macOS Security Framework APIs directly
- Key Security Framework functions to use:
  - `SecKeychainFindGenericPassword` - Retrieve password from keychain
  - `SecKeychainAddGenericPassword` - Add password to keychain
  - `SecKeychainItemDelete` - Delete password from keychain
  - `SecKeychainItemFreeContent` - Free memory allocated by keychain operations
- Service name format: `galasa.credentials.<credentialsId>`
- Account name: Derived from credential type (username, token, etc.)
- Benefits of JNA approach:
  - Direct native API access without spawning processes
  - Better performance and error handling
  - No dependency on external CLI tools
  - More secure (no credentials in process arguments)

##### Windows Implementation (Future)
**Class**: [`WindowsCredentialManagerStore`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/src/main/java/dev/galasa/creds/os/internal/windows/WindowsCredentialManagerStore.java)
- Uses Windows Credential Manager API via JNA
- Placeholder implementation that throws `UnsupportedOperationException` initially

##### Linux Implementation (Future)
**Class**: [`LinuxSecretServiceStore`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/src/main/java/dev/galasa/creds/os/internal/linux/LinuxSecretServiceStore.java)
- Uses freedesktop.org Secret Service API via D-Bus
- Placeholder implementation that throws `UnsupportedOperationException` initially

#### 4. Supporting Classes

**Enum**: [`OperatingSystem`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/src/main/java/dev/galasa/creds/os/internal/OperatingSystem.java)
- Values: `MACOS`, `WINDOWS`, `LINUX`, `UNKNOWN`
- Static method to detect current OS from system properties

**Class**: [`OsCredentialsException`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/src/main/java/dev/galasa/creds/os/internal/OsCredentialsException.java)
- Extends [`CredentialsException`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/CredentialsException.java)
- Specific exception for OS credentials store errors

## Implementation Details

### Credential Storage Format

In macOS Keychain:
- **Service**: `galasa.credentials.<credentialsId>`
- **Account**: Credential type identifier
  - `username` - For username-only credentials
  - `username+password` - For username/password credentials
  - `token` - For token credentials
  - `username+token` - For username/token credentials
- **Password**: The actual credential value (encrypted by Keychain)

For compound credentials (username+password), store as JSON:
```json
{
  "username": "myuser",
  "password": "mypassword"
}
```

### Credential Type Mapping

Map Galasa credential types to OS storage:
- [`CredentialsUsername`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/CredentialsUsername.java) → Account: `username`, Password: username value
- [`CredentialsUsernamePassword`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/CredentialsUsernamePassword.java) → Account: `username+password`, Password: JSON with both
- [`CredentialsToken`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/CredentialsToken.java) → Account: `token`, Password: token value
- [`CredentialsUsernameToken`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/CredentialsUsernameToken.java) → Account: `username+token`, Password: JSON with both

### JNA Native Interface

Use JNA to call macOS Security Framework APIs directly:
```java
// Define JNA interface for Security Framework
public interface SecurityFramework extends Library {
    SecurityFramework INSTANCE = Native.load("Security", SecurityFramework.class);
    
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
    
    int SecKeychainItemDelete(Pointer itemRef);
    
    int SecKeychainItemFreeContent(
        Pointer attrList,
        Pointer data
    );
}
```

### Error Handling

Handle common scenarios:
- Credential not found (errSecItemNotFound = -25300) → Return `null` from `getCredentials()`
- User cancelled keychain access (errSecUserCanceled = -128) → Throw `OsCredentialsException`
- Permission denied (errSecAuthFailed = -25293) → Throw `OsCredentialsException`
- Invalid credential format → Throw `OsCredentialsException`
- JNA library not available → Throw `OsCredentialsException` with clear message

Common macOS Security Framework error codes:
- `errSecSuccess = 0` - Operation successful
- `errSecItemNotFound = -25300` - Item not found in keychain
- `errSecUserCanceled = -128` - User cancelled the operation
- `errSecAuthFailed = -25293` - Authorization/authentication failed
- `errSecDuplicateItem = -25299` - Item already exists

## Build Configuration

### File: [`bnd.bnd`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/bnd.bnd)
```properties
Bundle-Name: dev.galasa.creds.os
Bundle-Description: OS-native credentials store for Galasa
Bundle-License: https://www.eclipse.org/legal/epl-2.0
Export-Package: !dev.galasa.creds.os.internal,dev.galasa.creds.os*
Import-Package: \
    dev.galasa,\
    dev.galasa.framework.spi,\
    dev.galasa.framework.spi.creds,\
    org.apache.commons.logging,\
    com.sun.jna,\
    com.sun.jna.ptr
Embed-Transitive: true
Embed-Dependency: *;scope=compile|runtime
-includeresource: \
    jna-*.jar; lib:=true
```

### File: [`build.gradle`](modules/extensions/galasa-extensions-parent/dev.galasa.creds.os/build.gradle)
```gradle
plugins {
    id 'biz.aQute.bnd.builder'
    id 'galasa.extensions'
}

description = 'Galasa OS Credentials Store - Provides credentials from OS-native stores'

dependencies {
    implementation('com.google.code.gson:gson')
    implementation('net.java.dev.jna:jna:5.18.1')
    implementation('net.java.dev.jna:jna-platform:5.18.1')
    testImplementation(testFixtures(project(':dev.galasa.extensions.common')))
}

ext.projectName = project.name
ext.includeInOBR = true
ext.includeInMVP = false
ext.includeInBOM = false
ext.includeInIsolated = false
ext.includeInCodeCoverage = true
ext.includeInJavadoc = false
```

### Update: [`settings.gradle`](modules/extensions/galasa-extensions-parent/settings.gradle)
Add line:
```gradle
include 'dev.galasa.creds.os'
```

## Testing Strategy

### Unit Tests
1. **Registration Tests**
   - Test URI parsing for all supported schemes
   - Test OS detection logic
   - Test registration with framework

2. **Store Implementation Tests**
   - Mock JNA Security Framework calls
   - Test credential retrieval, storage, and deletion
   - Test error handling scenarios
   - Test credential type conversions

3. **macOS-Specific Tests**
   - Test JNA interface mapping
   - Test Security Framework function calls
   - Test error code handling (errSecItemNotFound, errSecAuthFailed, etc.)
   - Test memory management (SecKeychainItemFreeContent)

## Implementation Phases

### Phase 1: Core Infrastructure (Initial Implementation)
- [ ] Create module structure
- [ ] Implement `OsCredentialsStoreRegistration`
- [ ] Implement `OsCredentialsStore` base class
- [ ] Implement `OperatingSystem` enum
- [ ] Implement `OsCredentialsException`
- [ ] Add build configuration files
- [ ] Update parent `settings.gradle`

### Phase 2: macOS Implementation
- [ ] Implement `MacOsKeychainStore`
- [ ] Define JNA interface for Security Framework
- [ ] Implement credential type mapping
- [ ] Implement JNA wrapper methods for keychain operations
- [ ] Add error handling for Security Framework error codes
- [ ] Implement proper memory management (SecKeychainItemFreeContent)
- [ ] Write unit tests - do **NOT** use a mocking library (e.g. Mockito). Use manual mocks instead.

### Phase 3: Documentation
- [ ] Add user documentation for setup
- [ ] Add developer documentation
- [ ] Add examples to AGENTS.md
- [ ] Create troubleshooting guide

### Phase 4: Future Enhancements
- [ ] Windows Credential Manager implementation
- [ ] Linux Secret Service implementation
- [ ] Support for additional credential types
- [ ] Performance optimizations (caching)

## Security Considerations

1. **No Credential Caching**: Always fetch from OS store to ensure freshness
2. **Memory Security**:
   - Always call `SecKeychainItemFreeContent` to free password data after use
   - Zero out password buffers after copying to Java objects
   - Use try-finally blocks to ensure cleanup even on errors
3. **Error Messages**: Don't include credential values in error messages
4. **Permissions**: Document required OS permissions for credential access
5. **Encryption**: Rely on OS-native encryption (Keychain, Credential Manager, etc.)
6. **JNA Security**: Credentials passed through JNA are not exposed in process arguments

## Dependencies

### Required Framework Components
- [`ICredentialsStore`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/ICredentialsStore.java) interface
- [`ICredentialsStoreRegistration`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/ICredentialsStoreRegistration.java) interface
- [`CredentialsException`](modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/CredentialsException.java)
- Credential type classes (Username, UsernamePassword, Token, UsernameToken)

### External Dependencies
- Gson (for JSON serialization of compound credentials)
- JNA (Java Native Access) version 5.14.0 or later
- JNA Platform (for platform-specific utilities)
- JUnit (for testing)

## Success Criteria

1. Users can store credentials in macOS Keychain Access
2. Galasa tests can retrieve credentials using `framework.credentials.store=os:auto`
3. All credential types are supported
4. Error messages are clear and actionable
5. Unit test coverage > 80%
6. Documentation is complete and accurate

## Open Questions

1. Should we support credential encryption keys from CPS like the etcd implementation?
   - **Decision**: No, rely on OS-native encryption initially
2. How should we handle credential namespacing for multiple Galasa installations?
   - **Decision**: Use service name prefix `galasa.credentials.` to avoid conflicts
3. Should we cache credentials for performance?
   - **Decision**: No caching in initial implementation for security and simplicity
4. What should happen if OS credentials store is not available?
   - **Decision**: Throw clear exception during registration

## References

- Existing implementation: [`Etcd3CredentialsStore`](modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/src/main/java/dev/galasa/cps/etcd/internal/Etcd3CredentialsStore.java)
- Framework interfaces: `modules/framework/galasa-parent/dev.galasa.framework/src/main/java/dev/galasa/framework/spi/creds/`
- macOS Security Framework: https://developer.apple.com/documentation/security/keychain_services
- JNA Documentation: https://github.com/java-native-access/jna
- Security Framework Header: `/System/Library/Frameworks/Security.framework/Headers/SecKeychain.h`
- Extension structure: `modules/extensions/galasa-extensions-parent/dev.galasa.cps.etcd/`