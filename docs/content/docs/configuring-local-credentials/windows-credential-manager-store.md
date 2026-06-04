---
title: Windows Credential Manager Store
---

The Windows Credential Manager Store extension allows Galasa to securely read test credentials from Windows Credential Manager, providing a more secure alternative to storing credentials in plain text configuration files.

## Prerequisites

- Windows 10 or later
- Galasa 1.0.0 or later

## Configuration

### 1. Enable the OS Credentials Store

Follow the [Enabling the OS Credentials Store](./index.md#enabling-the-os-credentials-store) instructions to enable the OS Credentials Store.

### 2. Add Credentials to Windows Credential Manager

Credentials must be added to Windows Credential Manager before Galasa can retrieve them. Each credential is stored as a generic credential with:

- **Target Name**: `galasa.credentials.{CREDENTIALS-ID}`
- **User Name**: Varies by credential type (see below)
- **Password**: The actual password, token, or JSON data

## Supported Credential Types

### Username + Password

**Format:**

- User Name: The username (e.g., `MYUSER`)
- Password: The password

**Example using Control Panel:**

1. Open Control Panel → User Accounts → Credential Manager
2. Click "Windows Credentials" → "Add a generic credential"
3. Set "Internet or network address" to: `galasa.credentials.SIMBANK`
4. Set "User name" to: `MYUSER`
5. Set "Password" to: `SYS1`
6. Click "OK"

**Example using PowerShell:**

```powershell
cmdkey /generic:"galasa.credentials.SIMBANK" /user:"MYUSER" /pass:"SYS1"
```

### Username Only

For scenarios where only a username is needed (no password).

**Format:**

- User Name: `username:{actual-username}` (e.g., `username:MYUSER`)
- Password: Empty string or any placeholder

**Example using Control Panel:**

1. Open Control Panel → User Accounts → Credential Manager
2. Click "Windows Credentials" → "Add a generic credential"
3. Set "Internet or network address" to: `galasa.credentials.USERNAME`
4. Set "User name" to: `username:MYUSER`
5. Set "Password" to any value (required by Windows)
6. Click "OK"

**Example using PowerShell:**

```powershell
cmdkey /generic:"galasa.credentials.USERNAME" /user:"username:MYUSER" /pass:" "
```

### Token Only

For API tokens, personal access tokens, or other token-based authentication.

**Format:**

- User Name: `token`
- Password: The token value

**Example using Control Panel:**

1. Open Control Panel → User Accounts → Credential Manager
2. Click "Windows Credentials" → "Add a generic credential"
3. Set "Internet or network address" to: `galasa.credentials.TOKEN`
4. Set "User name" to: `token`
5. Set "Password" to the token value
6. Click "OK"

**Example using PowerShell:**

```powershell
cmdkey /generic:"galasa.credentials.TOKEN" /user:"token" /pass:"abc123xyz789"
```

### Username + Token

**Format:**

- User Name: `username-token:{actual-username}` (e.g., `username-token:myuser`)
- Password: The token value

**Example using Control Panel:**

1. Open Control Panel → User Accounts → Credential Manager
2. Click "Windows Credentials" → "Add a generic credential"
3. Set "Internet or network address" to: `galasa.credentials.USERNAMETOKEN`
4. Set "User name" to: `username-token:myuser`
5. Set "Password" to the token value
6. Click "OK"

**Example using PowerShell:**

```powershell
cmdkey /generic:"galasa.credentials.USERNAMETOKEN" /user:"username-token:myuser" /pass:"abc123xyz789"
```

### KeyStore (JSON Format)

For Java KeyStore credentials containing SSL/TLS certificates and private keys.

**Format:**

- User Name: `JSON` (case-insensitive)
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

```powershell
# First, encode your keystore file to base64
$bytes = [System.IO.File]::ReadAllBytes("mykeystore.jks")
$base64 = [System.Convert]::ToBase64String($bytes)

# Create the JSON (single line, no line breaks)
$json = "{`"keystore`":`"$base64`",`"password`":`"keystorepass`",`"type`":`"JKS`"}"

# Add to Credential Manager
cmdkey /generic:"galasa.credentials.MYKEYSTORE" /user:"JSON" /pass:"$json"
```

**Example using Control Panel:**

1. Open Control Panel → User Accounts → Credential Manager
2. Click "Windows Credentials" → "Add a generic credential"
3. Set "Internet or network address" to: `galasa.credentials.MYKEYSTORE`
4. Set "User name" to: `JSON`
5. Set "Password" to: `{"keystore":"base64-content","password":"keystorepass","type":"JKS"}` (must be a single-line JSON string)
6. Click "OK"

## Managing Credentials

### Viewing Credentials

**Using Control Panel:**

1. Open Control Panel → User Accounts → Credential Manager
2. Click "Windows Credentials"
3. Look for credentials starting with `galasa.credentials.`

**Using PowerShell:**

```powershell
# List all Galasa credentials
cmdkey /list | Select-String "galasa.credentials"
```

### Updating Credentials

**Using Control Panel:**

1. Open Credential Manager
2. Find the credential under "Windows Credentials"
3. Click the arrow to expand
4. Click "Edit"
5. Update the values
6. Click "Save"

**Using PowerShell:**

```powershell
# Update by deleting and re-adding
cmdkey /delete:"galasa.credentials.MYCRED"
cmdkey /generic:"galasa.credentials.MYCRED" /user:"newuser" /pass:"newpass"
```

### Deleting Credentials

**Using Control Panel:**

1. Open Credential Manager
2. Find the credential under "Windows Credentials"
3. Click the arrow to expand
4. Click "Remove"
5. Confirm deletion

**Using PowerShell:**

```powershell
cmdkey /delete:"galasa.credentials.MYCRED"
```

## Security Considerations

### Credential Protection

Windows Credential Manager stores credentials encrypted using the Windows Data Protection API (DPAPI):

- Credentials are encrypted with your Windows user account
- Only your user account can decrypt and access the credentials
- Credentials are protected even if someone gains physical access to your hard drive

### Best Practices

1. **Regular audits**: Periodically review stored credentials in Credential Manager
2. **Principle of least privilege**: Only store credentials that are actually needed
3. **Shared systems**: Be cautious when using shared Windows systems - credentials are user-specific
4. **Backup considerations**: Credentials are tied to your user profile and Windows installation

## Troubleshooting

### "Credentials not found" Error

**Cause**: The credential doesn't exist in Credential Manager or the target name is incorrect.

**Solution:**

1. Verify the credential exists:
   ```powershell
   cmdkey /list | Select-String "galasa.credentials.MYCRED"
   ```
2. Check the target name format: must be `galasa.credentials.{ID}`
3. Ensure the credential is stored as a "Generic Credential" (not "Windows Credential" or "Certificate-Based Credential")

### "Access Denied" Error

**Cause**: Insufficient permissions to access Credential Manager.

**Solution:**

1. Ensure you're running as the same user who created the credential
2. Check Windows user account permissions
3. Try running your application as administrator (not recommended for regular use)

### Invalid JSON Error (KeyStore credentials)

**Cause**: The JSON in the password field is malformed or missing required fields.

**Solution:**

1. Validate your JSON using a JSON validator
2. Ensure all required fields are present: `keystore`, `password`, `type`
3. Verify the keystore content is properly base64-encoded
4. Ensure the JSON is a single line with no line breaks
5. Check for special characters that might need escaping in PowerShell

### KeyStore Loading Error

**Cause**: The base64-encoded keystore content is invalid or corrupted.

**Solution:**

1. Re-encode the keystore file:
   ```powershell
   $bytes = [System.IO.File]::ReadAllBytes("keystore.jks")
   $base64 = [System.Convert]::ToBase64String($bytes)
   ```
2. Verify the keystore is valid:
   ```powershell
   keytool -list -keystore keystore.jks
   ```
3. Ensure the keystore password in the JSON matches the actual keystore password

### Credential Manager Not Available

**Cause**: Credential Manager service is not running or disabled.

**Solution:**

1. Open Services (services.msc)
2. Find "Credential Manager" service
3. Ensure it's set to "Automatic" and is running
4. If stopped, start the service
