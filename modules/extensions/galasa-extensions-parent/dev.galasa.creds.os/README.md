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

- **`MacOsKeychainStore`**: Implements `ICredentialsStore` using the macOS `security` CLI tool to manage credentials in the Keychain.

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

1. **Username + Password**:
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

5. **JSON-based Credentials** (Recommended for complex types):
   - Account Name: `JSON` (case-insensitive)
   - Password: JSON object containing credential properties
   - Returns: Type depends on JSON structure
   - **Supported JSON structures**:
     - **KeyStore**: `{"keystore":"base64-content","password":"keystore-password","type":"JKS|PKCS12"}`

This format allows users to easily add credentials manually using their OS's credential management tools while supporting all Galasa credential types.

## Usage Examples

### Adding Credentials (macOS)

#### Username + Password

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

#### KeyStore (JSON Format)

Using command line:
```bash
security add-generic-password \
  -s "galasa.credentials.MYKEYSTORE" \
  -a "JSON" \
  -w '{"keystore":"base64-encoded-keystore","password":"keystorepass","type":"JKS"}' \
  -U
```

Using Keychain Access.app:
1. Open Keychain Access
2. Create new Password item
3. Set Name: `galasa.credentials.MYKEYSTORE`
4. Set Account: `JSON`
5. Set Password: `{"keystore":"base64-encoded-keystore","password":"keystorepass","type":"JKS"}`

**Note**: The keystore content must be base64-encoded. You can encode a keystore file using:
```bash
base64 -i mykeystore.jks | tr -d '\n'
```

## Implementation Notes

### Credential Type Detection

The implementation uses a hybrid approach to distinguish between credential types:

#### Simple Types (Prefix-based)
- **No prefix**: Username+Password
- **`username:` prefix**: Username-only credentials
- **`token` account name**: Token-only credentials
- **`username-token:` prefix**: Username+Token credentials

#### Complex Types (JSON-based)
- **`JSON` account name**: JSON object in password field containing credential properties

This hybrid approach:
- Keeps simple credential types simple and easy to use
- Provides scalability for complex credential types via JSON
- Works seamlessly with manual credential entry
- Enables proper type-safe credential retrieval
- Allows easy extension for future credential types without code changes

### Prefix Ordering

The detection logic checks prefixes in a specific order to avoid false matches:
1. Check for `token` (exact match)
2. Check for `username-token:` prefix (must come before `username:`)
3. Check for `username:` prefix
4. Default to username+password

This ordering is critical because `username:` would match `username-token:` if checked first.

### JSON Credential Format

JSON-based credentials provide a scalable way to store complex credential types:

**Advantages:**
- Self-documenting structure
- Easy to validate and parse
- Extensible without code changes
- Supports multiple properties per credential
- Industry-standard format

**Structure:**
```json
{
  "property1": "value1",
  "property2": "value2"
}
```

**Adding New JSON Types:**
To add support for new JSON-based credential types:
1. Create a new class implementing `JsonCredentialParser` interface
2. Implement the `canParse()`, `parse()`, and `getCredentialType()` methods
3. Add an instance of your parser to the `initializeJsonParsers()` method in `MacOsKeychainStore`
4. Update documentation
