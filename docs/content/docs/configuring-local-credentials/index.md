---
title: Configuring Credentials
---

Galasa provides flexible options for managing test credentials and retrieving them at runtime.

## Overview

Galasa supports multiple credential storage backends:

- **File-based storage**: Store credentials in the `credentials.properties` file within your Galasa home folder (default)
- **OS-native storage**: Leverage your operating system's secure credential storage

## OS Credentials Store

Starting from Galasa 0.47.0, the OS Credentials Store bundle (`dev.galasa.creds.os`) enables Galasa to read credentials from your operating system's native credential management system.

### Enabling the OS Credentials Store

To enable the OS Credentials Store, add the following properties to your `{GALASA_HOME}/bootstrap.properties` file:

```properties
framework.credentials.store=os:[macOS|windows|auto]
framework.extra.bundles=dev.galasa.creds.os
```

The `os:auto` value automatically detects your operating system and uses the appropriate credentials store implementation.

### Platform-Specific Documentation

See the platform-specific documentation for more information:

- [macOS Keychain Store](./macos-keychain-store.md)
- [Windows Credential Manager Store](./windows-credential-manager-store.md)
