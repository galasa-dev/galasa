---
title: "Using Galasa offline"
---

Using Galasa offline allows you to work with the framework in environments where internet access is restricted or unavailable. This is particularly useful in enterprise settings where corporate networks may have limited or no access to external resources such as Maven Central or Docker Hub due to security policies, firewalls, or network restrictions.

The Galasa zipped distribution (_isolated.zip_) provides a complete, self-contained package that includes all the necessary components to run Galasa without requiring external network connectivity. This offline distribution can be downloaded once from an internet-connected machine and then shared across your department or organization, making it ideal for:

- **Restricted network environments**: Corporate networks that block access to Maven Central, Docker Hub, or other external repositories
- **Air-gapped systems**: Secure environments that are completely isolated from the internet for security or compliance reasons
- **Internal deployment**: Organizations that want to host Galasa artifacts on internal servers for controlled distribution
- **Team collaboration**: Sharing a consistent Galasa version across a department without requiring each user to download dependencies individually
- **Offline development**: Working in locations or situations where internet connectivity is unreliable or unavailable

The offline distribution includes the Galasa CLI binaries, Maven dependencies, Javadoc API documentation, and optional Docker images for hosting the artifacts on an internal server. This ensures that you have everything needed to develop, build, and run Galasa tests without external network dependencies.