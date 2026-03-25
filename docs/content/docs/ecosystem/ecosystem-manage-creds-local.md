---
title: "Managing credentials locally using OS-native stores"
---

When running Galasa tests locally (outside of an Ecosystem), you can store credentials securely using your operating system's native credential management system instead of using plain-text files. This provides better security and integrates with your OS's existing credential management tools.

## Overview

The OS-native credentials store extension allows Galasa to read credentials from:
- **macOS**: Keychain Access

This means you can use your operating system's built-in tools to manage test credentials securely, and Galasa will automatically retrieve them when running tests.

## Prerequisites

- Galasa CLI tool (`galasactl`) installed
- The `dev.galasa.creds.os` extension bundle
- Access to your operating system's credential management system

## Configuring the OS credentials store

To use the OS-native credentials store, you need to configure two properties in your `~/.galasa/bootstrap.properties` file:

### 1. Set the credentials store type

Add or update the `framework.credentials.store` property to use the OS-native store:

```properties
framework.credentials.store=os:auto
```

The `os:` prefix indicates you want to use the OS-native credentials store. The value after the colon specifies which operating system:

- `os:auto` - Automatically detect the current operating system (recommended)
- `os:macOS` - Use macOS Keychain (macOS only)

### 2. Add the extension bundle

Add the OS credentials store bundle to the `framework.extra.bundles` property:

```properties
framework.extra.bundles=dev.galasa.creds.os
```

If you already have other bundles listed, separate them with commas:

```properties
framework.extra.bundles=dev.galasa.creds.os,dev.galasa.other.bundle
```

### Complete example

Your `~/.galasa/bootstrap.properties` file should include:

```properties
framework.credentials.store=os:auto
framework.extra.bundles=dev.galasa.creds.os
```

## Managing credentials on macOS

On macOS, credentials are stored in the system Keychain and can be managed using the Keychain Access application or programmatically.

### Using Keychain Access.app

1. Open **Keychain Access** (found in Applications > Utilities)
2. Select the **login** keychain
3. Click the **+** button or choose **File > New Password Item**
4. Fill in the fields:
   - **Keychain Item Name**: `galasa.credentials.{CREDENTIALS-ID}`
   - **Account Name**: Your actual username (e.g., `MYUSER`)
   - **Password**: Your actual password or token
5. Click **Add**

#### Example

To create credentials for a system called `MYBANK` with username `MYUSER`:

- **Keychain Item Name**: `galasa.credentials.MYBANK`
- **Account Name**: `MYUSER`
- **Password**: `SYS1`

### Using the command line

You can also add credentials using the `security` command:

```bash
security add-generic-password \
  -s "galasa.credentials.MYBANK" \
  -a "MYUSER" \
  -w "SYS1" \
  -U
```

Where:
- `-s` specifies the service name (must be `galasa.credentials.` followed by your credentials ID)
- `-a` specifies the account name (your username)
- `-w` specifies the password
- `-U` updates the password if it already exists

### Viewing credentials

To view a credential (you'll be prompted for your macOS password):

```bash
security find-generic-password \
  -s "galasa.credentials.MYBANK" \
  -g
```

Note: The `-g` flag displays the password. You don't need to specify the account name (`-a`) when viewing, as Galasa searches by service name only.

### Deleting credentials

To delete a credential:

```bash
security delete-generic-password \
  -s "galasa.credentials.MYBANK"
```

Note: You don't need to specify the account name (`-a`) when deleting, as Galasa deletes by service name only.

## Using credentials in tests

Once credentials are stored in your OS's credential manager, Galasa tests can retrieve them using the standard credentials API. The credentials are accessed by their ID (the part after `galasa.credentials.` in the service name).

### Example test code

```java
import dev.galasa.framework.spi.creds.CredentialsUsernamePassword;
import dev.galasa.framework.spi.creds.ICredentialsService;

@Test
public void testWithCredentials() throws Exception {
    // Get credentials for SYSTEM1
    ICredentials credentials = credentialsService.getCredentials("SYSTEM1");
    
    // Cast to the appropriate type
    if (credentials instanceof CredentialsUsernamePassword) {
        CredentialsUsernamePassword creds = (CredentialsUsernamePassword) credentials;
        String username = creds.getUsername();
        String password = creds.getPassword();
        
        // Use the credentials in your test
        // ...
    }
}
```

## Credential types

The OS-native credentials store supports all standard Galasa credential types:

- **UsernamePassword**: Username and password combination
- **UsernameToken**: Username and token combination  
- **Username**: Username only
- **Token**: Token only

All credential types are stored using the same keychain entry format. The password field in the keychain entry contains either the password or token value, depending on how you use it in your tests.

## Security considerations

### macOS Keychain security

- Credentials are encrypted and protected by your macOS user account password
- Access to keychain items can be controlled using Access Control Lists (ACLs)
- You may be prompted to allow Galasa to access keychain items the first time they are accessed
- Consider using a separate keychain for test credentials if you want additional isolation

### Best practices

1. **Use unique credentials for testing**: Don't reuse production credentials in test environments
2. **Limit access**: Only store credentials that are necessary for your tests
3. **Regular rotation**: Rotate test credentials periodically
4. **Audit access**: Review keychain access logs if available on your system

## Troubleshooting

### "User cancelled keychain access" error

This error occurs when you click "Deny" when macOS prompts you to allow Galasa to access a keychain item. To fix:

1. Open Keychain Access
2. Find the credential item
3. Double-click it and go to the "Access Control" tab
4. Add the Java application to the list of allowed applications, or choose "Allow all applications to access this item"

### "Credentials not found" error

This error means Galasa couldn't find a keychain entry with the expected service and account names. Verify:

1. The keychain item exists in Keychain Access
2. The service name is exactly `galasa.credentials.{CREDENTIALS-ID}`
3. The account name matches the credentials ID
4. You're looking in the correct keychain (usually "login")

### "Authorization failed for keychain access" error

This error indicates a permissions problem. Try:

1. Unlocking your keychain in Keychain Access
2. Checking the Access Control settings for the credential item
3. Restarting your terminal or IDE

## Migrating from file-based credentials

If you're currently using the file-based credentials store (`~/.galasa/credentials.properties`), you can migrate to the OS-native store:

1. Note all credentials in your `credentials.properties` file
2. Add each credential to your OS's credential manager using the instructions above
3. Update your `bootstrap.properties` to use the OS credentials store
4. Test that your credentials work correctly
5. Optionally, remove or backup your old `credentials.properties` file
