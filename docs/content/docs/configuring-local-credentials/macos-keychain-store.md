---
title: macOS Keychain Credentials Store
---

The macOS Keychain Credentials Store extension allows Galasa to securely read test credentials from the macOS Keychain, providing a more secure alternative to storing credentials in plain text configuration files.

## Prerequisites

- macOS
- Galasa 0.47.0 or later

## Configuration

### 1. Enable the OS Credentials Store

Follow the [Enabling the OS Credentials Store](./index.md#enabling-the-os-credentials-store) instructions to enable the OS Credentials Store.

### 2. Add Credentials to Keychain

Credentials must be added to the macOS Keychain before Galasa can retrieve them. Each credential is stored as a generic password item with:

- **Service Name**: `galasa.credentials.{CREDENTIALS-ID}`
- **Account Name**: Varies by credential type (see below)
- **Password**: The actual password, token, or JSON data

## Supported Credential Types

### Username + Password

**Format:**

- Account Name: The username (e.g., `MYUSER`)
- Password: The password

**Example using Keychain Access.app:**

1. Open Keychain Access
2. Click the "+" button to add a new password item
3. Set "Keychain Item Name" to: `galasa.credentials.SIMBANK`
4. Set "Account Name" to: `MYUSER`
5. Set "Password" to: `SYS1`
6. Click "Add"

**Example using command line:**

```bash
security add-generic-password \
  -s "galasa.credentials.SIMBANK" \
  -a "MYUSER" \
  -w "SYS1" \
  -U
```

### Username Only

For scenarios where only a username is needed (no password).

**Format:**

- Account Name: `username:{actual-username}` (e.g., `username:MYUSER`)
- Password: Empty string (required by Keychain)

**Example using Keychain Access.app:**

1. Open Keychain Access
2. Click the "+" button to add a new password item
3. Set "Keychain Item Name" to: `galasa.credentials.USERNAME`
4. Set "Account Name" to: `MYUSER`
5. Set "Password" to any non-empty string (required by Keychain)
6. Click "Add"

**Example using command line:**

```bash
security add-generic-password \
  -s "galasa.credentials.USERNAME" \
  -a "username:MYUSER" \
  -w "" \
  -U
```

### Token Only

For API tokens, personal access tokens, or other token-based authentication.

**Format:**

- Account Name: `token`
- Password: The token value

**Example using Keychain Access.app:**

1. Open Keychain Access
2. Click the "+" button to add a new password item
3. Set "Keychain Item Name" to: `galasa.credentials.TOKEN`
4. Set "Account Name" to: `token`
5. Set "Password" to the token value
6. Click "Add"

**Example using command line:**

```bash
security add-generic-password \
  -s "galasa.credentials.TOKEN" \
  -a "token" \
  -w "abc123xyz789" \
  -U
```

### Username + Token

**Format:**

- Account Name: `username-token:{actual-username}` (e.g., `username-token:myuser`)
- Password: The token value

**Example using Keychain Access.app:**

1. Open Keychain Access
2. Click the "+" button to add a new password item
3. Set "Keychain Item Name" to: `galasa.credentials.USERNAMETOKEN`
4. Set "Account Name" to: `username-token:myuser`
5. Set "Password" to the token value
6. Click "Add"

**Example using command line:**

```bash
security add-generic-password \
  -s "galasa.credentials.USERNAMETOKEN" \
  -a "username-token:myuser" \
  -w "abc123xyz789" \
  -U
```

### KeyStore (JSON Format)

For Java KeyStore credentials containing SSL/TLS certificates and private keys.

**Format:**

- Account Name: `JSON` (case-insensitive)
- Password: JSON object with keystore properties

**JSON Structure:**

```json
{
  "keystore": "base64-encoded-keystore-content",
  "password": "keystore-password",
  "type": "JKS"
}
```

**Supported KeyStore Types:**

- `JKS` - Java KeyStore
- `PKCS12` - PKCS#12 format

**Example - Encoding a KeyStore:**

```bash
# First, encode your keystore file to base64
base64 -i mykeystore.jks | tr -d '\n' > keystore.b64

# Then create the JSON (replace the base64 content)
cat > keystore.json << 'EOF'
{ "keystore": "PASTE_BASE64_CONTENT_HERE", "password": "keystorepass", "type": "JKS" }
EOF

# Add to Keychain
security add-generic-password \
  -s "galasa.credentials.MYKEYSTORE" \
  -a "JSON" \
  -w "$(cat keystore.json)" \
  -U
```

**Example using Keychain Access.app:**

1. Open Keychain Access
2. Click the "+" button
3. Set "Keychain Item Name" to: `galasa.credentials.MYKEYSTORE`
4. Set "Account Name" to: `JSON`
5. Set "Password" to: `{"keystore":"base64-content","password":"keystorepass","type":"JKS"}` (must be a single-line JSON string)
6. Click "Add"

## Managing Keychain Permissions

### First-Time Access

When Galasa first accesses a credential from the Keychain, macOS will display a permission dialog asking if you want to allow access. You have three options:

- **Deny**: Blocks access to this credential
- **Allow**: Grants one-time access (will prompt again next time)
- **Always Allow**: Grants permanent access

### Reducing Permission Prompts

To minimize permission prompts:

1. **Click "Always Allow"** when prompted - this grants permanent access for that specific credential
2. **Pre-authorize credentials** before running tests:
   ```bash
   # This will prompt once but cache the authorization
   security find-generic-password -s "galasa.credentials.MYCRED" -g
   ```
3. **Grant access via Keychain Access.app**:
     - Open Keychain Access
     - Find a `galasa.credentials.*` item
     - Double-click to open
     - Go to "Access Control" tab
     - Add the `security` tool the allowed applications list or check the "Allow all applications to access this item" checkbox

**Note**: You will receive a separate permission prompt for each unique credential ID the first time it is accessed. This is a macOS security feature.

## Troubleshooting

### "User cancelled keychain access" Error

**Cause**: You clicked "Deny" when macOS prompted for keychain access.

**Solution**:

1. Open Keychain Access
2. Find the credential item
3. Delete it and recreate it, or
4. Modify the Access Control settings to allow your application

### "Credentials not found" Error

**Cause**: The credential doesn't exist in the Keychain or the service name is incorrect.

**Solution**:

1. Verify the credential exists: `security find-generic-password -s "galasa.credentials.MYCRED"`
2. Check the service name format: must be `galasa.credentials.{ID}`
3. Ensure you're searching the correct keychain (login keychain by default)

### Invalid JSON Error (KeyStore credentials)

**Cause**: The JSON in the password field is malformed or missing required fields.

**Solution**:

1. Validate your JSON using a JSON validator
2. Ensure all required fields are present: `keystore`, `password`, `type`
3. Verify the keystore content is properly base64-encoded
4. Check for special characters that might need escaping

### KeyStore Loading Error

**Cause**: The base64-encoded keystore content is invalid or corrupted.

**Solution**:

1. Re-encode the keystore file: `base64 -i keystore.jks | tr -d '\n'`
2. Verify the keystore is valid: `keytool -list -keystore keystore.jks`
3. Ensure the keystore password in the JSON matches the actual keystore password

### Best Practices

1. **Use "Always Allow" carefully**: Only grant permanent access to trusted applications
2. **Protect your keychain password**: Your keychain password protects all stored credentials
3. **Regular audits**: Periodically review keychain items and access permissions
4. **Backup considerations**: Keychain items are included in Time Machine backups
5. **Shared systems**: Be cautious when using shared macOS systems
