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
- **Account Name**: Varies by credential type (see below)
- **Password**: The actual password or token value

#### Credential Types

The macOS Keychain store supports multiple credential types, distinguished by the account name format:

1. **Username + Password** (backward compatible):
   - Account Name: Plain username (e.g., `IBMUSER`)
   - Password: The password
   - Returns: `CredentialsUsernamePassword`

2. **Username Only**:
   - Account Name: `username:{actual-username}` (e.g., `username:IBMUSER`)
   - Password: Empty string (required by keychain)
   - Returns: `CredentialsUsername`

3. **Token Only**:
   - Account Name: `token`
   - Password: The token value
   - Returns: `CredentialsToken`

4. **Username + Token**:
   - Account Name: `username-token:{actual-username}` (e.g., `username-token:IBMUSER`)
   - Password: The token value
   - Returns: `CredentialsUsernameToken`

This format allows users to easily add credentials manually using their OS's credential management tools while supporting all Galasa credential types.

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

## Usage Examples

### Adding Credentials (macOS)

#### Username + Password (backward compatible)

Using Keychain Access.app:
1. Open Keychain Access
2. Create new Password item
3. Set Name: `galasa.credentials.SIMBANK`
4. Set Account: `IBMUSER`
5. Set Password: `SYS1`

Using command line:
```bash
security add-generic-password \
  -s "galasa.credentials.SIMBANK" \
  -a "IBMUSER" \
  -w "SYS1" \
  -U
```

#### Username Only

Using command line:
```bash
security add-generic-password \
  -s "galasa.credentials.MYUSER" \
  -a "username:IBMUSER" \
  -w "" \
  -U
```

#### Token Only

Using command line:
```bash
security add-generic-password \
  -s "galasa.credentials.GITHUB" \
  -a "token" \
  -w "ghp_abc123xyz789" \
  -U
```

#### Username + Token

Using command line:
```bash
security add-generic-password \
  -s "galasa.credentials.GITLAB" \
  -a "username-token:myuser" \
  -w "glpat-abc123xyz789" \
  -U
```

### Retrieving Credentials in Tests

```java
@Test
public void testWithCredentials() throws Exception {
    ICredentials credentials = credentialsService.getCredentials("SIMBANK");
    
    // Handle different credential types
    if (credentials instanceof CredentialsUsernamePassword) {
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) credentials;
        String username = creds.getUsername(); // Returns "IBMUSER"
        String password = new String(creds.getPassword()); // Returns "SYS1"
    } else if (credentials instanceof CredentialsUsernameToken) {
        CredentialsUsernameToken creds = (CredentialsUsernameToken) credentials;
        String username = creds.getUsername(); // Returns "myuser"
        String token = new String(creds.getToken()); // Returns token value
    } else if (credentials instanceof CredentialsUsername) {
        CredentialsUsername creds = (CredentialsUsername) credentials;
        String username = creds.getUsername(); // Returns username only
    } else if (credentials instanceof CredentialsToken) {
        CredentialsToken creds = (CredentialsToken) credentials;
        byte[] token = creds.getToken(); // Returns token value
    }
}
```

## Implementation Notes

### Credential Type Detection

The implementation uses account name prefixes to distinguish between credential types:

- **No prefix**: Username+Password (backward compatible)
- **`username:` prefix**: Username-only credentials
- **`token` account name**: Token-only credentials
- **`username-token:` prefix**: Username+Token credentials

This approach:
- Allows explicit type specification without metadata storage
- Maintains backward compatibility with existing credentials
- Works seamlessly with manual credential entry
- Enables proper type-safe credential retrieval

### Prefix Ordering

The detection logic checks prefixes in a specific order to avoid false matches:
1. Check for `token` (exact match)
2. Check for `username-token:` prefix (must come before `username:`)
3. Check for `username:` prefix
4. Default to username+password (backward compatible)

This ordering is critical because `username:` would match `username-token:` if checked first.

### Error Handling

The implementation handles various macOS Keychain error codes:
- `errSecItemNotFound` (-25300): Credential doesn't exist
- `errSecUserCanceled` (-128): User denied access
- `errSecAuthFailed` (-25293): Authorization failed
- `errSecDuplicateItem` (-25299): Credential already exists

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
