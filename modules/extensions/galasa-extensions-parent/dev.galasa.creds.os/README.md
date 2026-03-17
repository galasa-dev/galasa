# Galasa OS-Native Credentials Store

This extension provides OS-native credentials store implementations for Galasa, allowing tests to securely retrieve credentials from the operating system's built-in credential management systems.

## Overview

The OS credentials store extension enables Galasa to read credentials from:
- **macOS**: Keychain Access (fully implemented)
- **Windows**: Windows Credential Manager (planned)
- **Linux**: Secret Service API (planned)

## Architecture

### Core Components

- **`OsCredentialsStoreRegistration`**: OSGi component that registers the credentials store with the Galasa framework
- **`OsCredentialsStore`**: Main store implementation that delegates to OS-specific implementations
- **`OperatingSystem`**: Enum for OS detection and parsing
- **`OsCredentialsException`**: Custom exception for credentials store errors

### macOS Implementation

- **`MacOsKeychainStore`**: Implements `ICredentialsStore` using JNA to call macOS Security Framework APIs
- **`SecurityFramework`**: JNA interface defining native macOS Security Framework functions

## How It Works

### Configuration

Users configure the OS credentials store in their `~/.galasa/bootstrap.properties`:

```properties
framework.credentials.store=os:auto
framework.extra.bundles=dev.galasa.creds.os
```

The `os:` URI scheme triggers the registration of this credentials store. The value after the colon (`auto`, `macOS`, `windows`, or `linux`) determines which OS-specific implementation to use.

### Credential Storage Format

Credentials are stored in the OS's native credential manager with a consistent format:

- **Service Name**: `galasa.credentials.{CREDENTIALS-ID}`
- **Account Name**: The actual username (e.g., `IBMUSER`)
- **Password**: The actual password or token value

When retrieved by Galasa, the credentials are returned as a `CredentialsUsernamePassword` object where:
- `getUsername()` returns the Account Name from the credential manager (e.g., `IBMUSER`)
- `getPassword()` returns the Password from the credential manager (e.g., `SYS1`)

**Note**: For token-only credentials (where no username is available), the credentials ID is used as the account name.

This format allows users to easily add credentials manually using their OS's credential management tools.

### macOS Keychain Integration

The macOS implementation uses JNA (Java Native Access) to call Security Framework APIs directly:

- `SecKeychainFindGenericPassword` - Retrieve credentials
- `SecKeychainAddGenericPassword` - Store credentials
- `SecKeychainItemDelete` - Delete credentials
- `SecKeychainItemFreeContent` - Free allocated memory

This approach avoids spawning CLI processes and provides better performance and error handling.

## Building

Build the extension using Gradle:

```bash
cd modules/extensions/galasa-extensions-parent/dev.galasa.creds.os
gradle clean build publishToMavenLocal
```

## Testing

The extension includes comprehensive unit tests:

```bash
gradle test
```

Test coverage includes:
- OS detection and parsing (13 tests)
- Credentials store registration (6 tests)
- macOS Keychain operations (25 tests)

### Mock Framework

Tests use `MockSecurityFramework` to simulate macOS Keychain behavior without requiring actual system calls. This allows tests to run on any platform and verify all code paths including error scenarios.

## Dependencies

- **JNA (Java Native Access)**: 5.18.1 - For calling native macOS APIs
- **JNA Platform**: 5.18.1 - Platform-specific JNA extensions
- **Galasa Framework**: Core framework interfaces and utilities

## Usage Example

### Adding Credentials (macOS)

Using Keychain Access.app:
1. Open Keychain Access
2. Create new Password item
3. Set Name: `galasa.credentials.SIMBANK`
4. Set Account: `IBMUSER` (your username)
5. Set Password: `SYS1`

Using command line:
```bash
security add-generic-password \
  -s "galasa.credentials.SIMBANK" \
  -a "IBMUSER" \
  -w "SYS1" \
  -U
```

### Retrieving Credentials in Tests

```java
@Test
public void testWithCredentials() throws Exception {
    ICredentials credentials = credentialsService.getCredentials("SYSTEM1");
    
    if (credentials instanceof CredentialsUsernamePassword) {
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) credentials;
        String username = creds.getUsername(); // Returns "SYSTEM1"
        String password = creds.getPassword(); // Returns "mypassword"
    }
}
```

## Implementation Notes

### Why Username = Credentials ID?

The implementation uses the credentials ID as both the account name and the returned username. This design choice:
- Simplifies credential lookup (we always know the account name)
- Makes manual credential entry intuitive
- Avoids needing metadata storage for username mapping
- Works well with Galasa's credential ID-based retrieval model

### Credential Type Handling

All credentials are returned as `CredentialsUsernamePassword` with:
- Username = Credentials ID
- Password = The stored password/token value

Tests can interpret the password field as either a password or token based on their needs. This flexible approach works because:
- The semantic meaning (password vs token) is determined by how the test uses it
- The storage format is identical for both
- It simplifies the implementation and user experience

### Error Handling

The implementation handles various macOS Keychain error codes:
- `errSecItemNotFound` (-25300): Credential doesn't exist
- `errSecUserCanceled` (-128): User denied access
- `errSecAuthFailed` (-25293): Authorization failed
- `errSecDuplicateItem` (-25299): Credential already exists

## Future Enhancements

### Windows Support

Planned implementation using Windows Credential Manager APIs:
- `CredRead` - Retrieve credentials
- `CredWrite` - Store credentials
- `CredDelete` - Delete credentials

### Linux Support

Planned implementation using Secret Service API (libsecret):
- D-Bus interface to org.freedesktop.secrets
- Integration with GNOME Keyring, KWallet, etc.

### Additional Features

- Support for credential enumeration (getAllCredentials)
- Credential metadata (descriptions, tags)
- Credential expiration
- Access control and auditing

## Contributing

When contributing to this extension:

1. **Follow existing patterns**: Match the style and structure of existing code
2. **Add tests**: All new functionality must include unit tests
3. **Update documentation**: Keep README and user docs in sync with code changes
4. **Handle errors**: Provide clear error messages for common failure scenarios
5. **Consider security**: Never log sensitive credential data

## Security Considerations

- Credentials are never logged or written to disk by this extension
- Memory containing credentials is cleared after use where possible
- OS-native encryption protects credentials at rest
- Access control is delegated to the OS's credential manager
- Users should follow their organization's security policies for test credentials

## License

Copyright contributors to the Galasa project

SPDX-License-Identifier: EPL-2.0