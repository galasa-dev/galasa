# AGENTS.md — extensions module

## What this module produces

OSGi bundles that plug into the Galasa framework to provide concrete backend implementations: CouchDB (auth + RAS), etcd (CPS), Kafka (events), and others. Each bundle is an independent OSGi component.

## Directory layout

```
galasa-extensions-parent/
  dev.galasa.auth.couchdb/      - CouchDB-backed authentication store
  dev.galasa.cps.etcd/          - etcd-backed configuration property store
  dev.galasa.ras.couchdb/       - CouchDB-backed result archive store
  dev.galasa.events.kafka/      - Kafka-backed event publishing
  buildSrc/                     - Gradle build plugin customisation
```

## Build and test

```bash
./build-locally.sh --clean    # full clean build including tests
./build-locally.sh --delta    # incremental build
```

## Coding conventions

- Each bundle uses `biz.aQute.bnd.builder` for OSGi manifest generation.
- Dependency versions are managed by the `dev.galasa.platform` BOM — do not hardcode versions.
- JUnit tests live in `src/test/java/` within the same bundle as the code under test.
- Use Mockito for mocking collaborators and OSGi service dependencies.
- GPG signing is handled by CI; skip locally with `-PisMainOrRelease=false`.
