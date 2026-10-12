# AGENTS.md — wrapping module

## What this module produces

OSGi bundles that wrap third-party JARs which are not OSGi-compatible by default. Each submodule takes a non-OSGi library and repackages it with a valid OSGi `MANIFEST.MF` so it can be loaded by the Galasa OSGi runtime.

## Wrapped libraries

| Bundle | Wraps |
|---|---|
| `dev.galasa.wrapping.com.auth0.jwt` | Auth0 JWT library |
| `dev.galasa.wrapping.httpclient5` | Apache HttpClient 5 |
| `dev.galasa.wrapping.io.grpc.java` | gRPC Java |
| `dev.galasa.wrapping.io.kubernetes.client-java` | Kubernetes Java client |
| `dev.galasa.wrapping.jetcd-core` | jetcd (etcd Java client) |
| `dev.galasa.wrapping.kafka.clients` | Apache Kafka clients |
| `dev.galasa.wrapping.selenium-java` | Selenium WebDriver |
| `dev.galasa.wrapping.velocity-engine-core` | Apache Velocity |

## Build and test

```bash
./build-locally.sh           # build all wrapper bundles
```

There are typically no unit tests in wrapper bundles — the artifact is the bundle itself.

## Conventions

- Use `maven-bundle-plugin` (Apache Felix) to generate OSGi bundle metadata.
- Use `maven-shade-plugin` to embed transitive dependencies that are also non-OSGi.
- All bundles are named under the `dev.galasa.wrapping.*` namespace.
- Do not add application logic to wrapper bundles — they must only contain the wrapped library and its OSGi metadata.
- To add a new wrapped library: create a new submodule following an existing example (e.g. `dev.galasa.wrapping.httpclient5`), add it to the root `pom.xml` `<modules>` list.
- GPG signing is CI-only; skip locally with `-Dgpg.skip=true`.
