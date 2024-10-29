# Setup local Galasa APIs for testing

## Index
- [Overview](#overview)
- [GALASA_HOME](#galasa_home)
- [Rancher](#rancher)
- [Couchdb](#couchdb)
- [Dex](#dex)
- [etcd](#etcd)
- [bootstrap.properties](#bootstrapproperties)
- [More .zprofile properties](#more-zprofile-properties)
- [Start the API server](#start-the-api-server)
- [Web UI](#web-ui)
- [CLI](#cli)
- [Set up the CPS](#set-up-the-cps)

## Overview

In order to test new developments on the Galasa API servlets you need to be able to run them locally.
The below instructions will help you setup couchDB, etcd and a local API server in order to run tests against the corresponding Galasa APIs.

## GALASA_HOME
Properties files are such configuration are stored in the Galasa home folder.
Default is `.galasa` directory. Unless you set `GALASA_HOME` to override that default.

## Rancher
We use Rancher desktop. This creates a VM into which we can launch docker images.
Install this and start it up.

## CouchDB
To summarise https://hub.docker.com/_/couchdb:

```bash
docker pull couchdb:3.3.3
```

```bash  
export COUCHDB_USER="admin"
export COUCHDB_PASSWORD=$(openssl rand -base64 10)
echo "Couchdb password for $COUCHDB_USER is $COUCHDB_PASSWORD"
```

### Couchdb .zprofile settings.
Add this to your `.zprofile`:
```bash
  export COUCHDB_USER="admin"
  # Created the couchdb password using this:
  #export COUCHDB_PASSWORD=$(openssl rand -base64 10)
  #echo "Couchdb password for $COUCHDB_USER is $COUCHDB_PASSWORD"
  export COUCHDB_PASSWORD="????????????????????"
  export COUCHDB_TOKEN=$(echo -n "${COUCHDB_USER}:${COUCHDB_PASSWORD}" | base64)

  export GALASA_RAS_TOKEN=${COUCHDB_TOKEN}
  export GALASA_AUTHSTORE_TOKEN=${COUCHDB_TOKEN}
```

### Run the couchdb docker image
```bash
docker run -p 5984:5984 -d -e COUCHDB_USER=${COUCHDB_USER} -e COUCHDB_PASSWORD=${COUCHDB_PASSWORD} --name couchdb couchdb:3.3.3 
```

### View the couchdb docker image logs
Look at the log
```bash
docker logs couchdb  
```

### Health check to see if the REST api is working
open the couchdb dashboard and take a look:
```
http://localhost:5984
```

Try the API using the token.
```bash
curl -H "Authorization: Basic $COUCHDB_TOKEN" http://localhost:5984/_all_dbs
```

### Open the couchdb web UI
```bash
open http://localhost:5984/_utils
```

### First time in... setup the couchdb databases
Go into the settings/spanner and set up a single node using the UI. You will need the userid 'admin' and password we set up earlier.

### To stop couchdb
Stop the docker image when you no longer need it.
```bash
docker rm $(docker ps -a | grep "couchdb" | cut -f1 -d' ' | xargs)
```

## Dex
Get the docker image...
```bash
docker pull ghcr.io/dexidp/dex:v2.38.0
```

Create an admin password we can use.
```bash
export DEX_ADMIN_PASSWORD=$(echo password | htpasswd -BinC 10 admin | cut -d: -f2)
echo "Dex admin password is $DEX_ADMIN_PASSWORD"
```

Run the following, to create a file `~/.dex/config-dev.yaml`

```bash
mkdir -p ~/.dex
cat << EOF >> ~/.dex/config-dev.yaml

# The base path of dex and the external name of the OpenID Connect service.
# This is the canonical URL that all clients MUST use to refer to dex. If a
# path is provided, dex's HTTP service will listen at a non-root URL.
issuer: http://127.0.0.1:5556/dex

# The storage configuration determines where dex stores its state. Supported
# options include SQL flavors and Kubernetes third party resources.
#
# See the documentation (https://dexidp.io/docs/storage/) for further information.
storage:
  type: sqlite3
  config:
    file: var/dex/dex.db

# Configuration for the HTTP endpoints.
web:
  http: 0.0.0.0:5556

# Configuration for telemetry
telemetry:
  http: 0.0.0.0:5558

# Uncomment this block to enable the gRPC API. This values MUST be different
# from the HTTP endpoints.
grpc:
  addr: 0.0.0.0:5557
  reflection: true

# Uncomment this block to enable configuration for the expiration time durations.
# Is possible to specify units using only s, m and h suffixes.
expiry:
  # deviceRequests: "5m"
  signingKeys: "6h"
  idTokens: "24h"
  refreshTokens:
    disableRotation: true
    validIfNotUsedFor: "2160h" # 90 days

# Default values shown below
oauth2:
  skipApprovalScreen: true

# Instead of reading from an external storage, use this list of clients.
#
# If this option isn't chosen clients may be added through the gRPC API.
staticClients:
- id: galasa-webui
  redirectURIs:
  - 'http://localhost:8080/auth/callback'
  name: 'Galasa Web UI'
  secret: example-webui-client-secret

# Let dex keep a list of passwords which can be used to login to dex.
enablePasswordDB: true

# A static list of passwords to login the end user. By identifying here, dex
# won't look in its underlying storage for passwords.
#
# If this option isn't chosen users may be added through the gRPC API.

staticPasswords:
- email: "admin@example.com"
  hash: "${DEX_ADMIN_PASSWORD}"
  username: "admin"
  userID: "08a8684b-db88-4b73-90a9-3cd1661f5466"

EOF
```


### Launching dex
```bash
docker run -d -v ~/.dex/config-dev.yaml:/etc/dex/config.docker.yaml -p 5556:5556 -p 5558:5558 -p 5557:5557 --name dex ghcr.io/dexidp/dex:v2.38.0
```

### Checking health of the dex docker image
```bash
docker logs dex
```

### Stopping dex
```bash
docker rm $(docker ps -a | grep "dex" | cut -f1 -d' ' | xargs)
```

## etcd

### Get etcd docker image
```bash
export ETCD_TAG="3.3.27-debian-11-r100"
docker pull bitnami/etcd:${ETCD_TAG}
```

### Set up the network bridge to be used by the etcd docker image.
```bash
docker network create app-tier --driver bridge
```

### To run the etcd docker image
```bash
docker run -d --name etcd \
    --network app-tier \
    --publish 2379:2379 \
    --publish 2380:2380 \
    --env ALLOW_NONE_AUTHENTICATION=yes \
    --env ETCD_ADVERTISE_CLIENT_URLS=http://etcd:2379 \
    bitnami/etcd:${ETCD_TAG}
```

### Health check of the etcd docker image
```bash
docker logs etcd
```

```bash
curl http://localhost:2379/version
```
results in:
```
{"etcdserver":"3.5.15","etcdcluster":"3.5.0"}%    
```

### to stop the etcd docker image
```bash
docker rm $(docker ps -a | grep "etcd" | cut -f1 -d' ' | xargs)
```



## bootstrap.properties
```
framework.resultarchive.store=couchdb:http://127.0.0.1:5984
framework.config.store=etcd:127.0.0.1:2379
framework.auth.store=couchdb:http://127.0.0.1:5984
framework.extra.bundles=dev.galasa.ras.couchdb,dev.galasa.cps.etcd
api.extra.bundles=dev.galasa.auth.couchdb
```

## More .zprofile properties
```bash
function setup_galasa_dev() {
  export GALASA_OBR_VERSION=0.36.0
  export GALASA_BOOT_JAR_VERSION=0.36.0
  export GALASA_EXTERNAL_API_URL="http://localhost:8080"
  export GALASA_USERNAME_CLAIMS="preferred_username,name,sub"
  export GALASA_ALLOWED_ORIGINS="*"

  # The GALASA_DEX_ISSUER environment variable must match the "issuer" value
  # within your local Dex server's configuration
  export GALASA_DEX_ISSUER="http://127.0.0.1:5556/dex"

  # The GALASA_DEX_GRPC_HOSTNAME environment variable must match the "addr" value
  # within the "grpc" section in your local Dex server's configuration 
  export GALASA_DEX_GRPC_HOSTNAME="127.0.0.1:5557"
}

setup_galasa_dev
```

## Start the API server
From a terminal in the `framework` project...
```bash
java -jar ~/.m2/repository/dev/galasa/galasa-boot/${GALASA_BOOT_JAR_VERSION}/galasa-boot-${GALASA_BOOT_JAR_VERSION}.jar \
--api \
--localmaven file://${HOME}/.m2/repository/ \
--remotemaven https://development.galasa.dev/main/maven-repo/obr \
--obr mvn:dev.galasa/dev.galasa.uber.obr/${GALASA_OBR_VERSION}/obr
```

## Web UI

### install pre-reqs for teh web ui
```bash
brew install node@20
node -v 
npm -v
```

## web ui build and launch
In the `webui` project, run the `run-locally.sh` script.

```bash
open http://localhost:3000
```

Allocate a GALASA_TOKEN

Add that to your `.zprofile`:
```bash
export GALASA_TOKEN="??????????"
```

## CLI

Test that you can set an etcd property and get it back:
```
galasactl properties set --namespace $USER --name test.prop1 --value "$(date)" --bootstrap http://localhost:8080/bootstrap
galasactl properties get --namespace $USER --bootstrap http://localhost:8080/bootstrap
```

## Set up the CPS
Place the following into a yaml file. Say `needed-properties.yaml`
```yaml
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: resource.management.dead.heartbeat.timeout
data:
    value: "40"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: request.type.CLI.prefix
data:
    value: C
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: request.type.REQUEST.prefix
data:
    value: R
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: resource.management.finished.timeout
data:
    value: "40"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: test.stream.cics-prod.description
data:
    value: Local development system
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: request.prefix.maximum
data:
    value: "99999"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: waiting.initial.delay
data:
    value: "180"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: waiting.random.delay
data:
    value: "60"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: auth.store
data:
    value: couchdb:http://localhost:5984
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: credentials.store
data:
    value: etcd:http://localhost:2379
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: dynamicstatus.store
data:
    value: etcd:http://localhost:2379
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: produce.events
data:
    value: "false"
---
apiVersion: galasa-dev/v1alpha1
kind: GalasaProperty
metadata:
    namespace: framework
    name: resultarchive.store
data:
    value: couchdb:http://localhost:5984
---
```

Now apply the minimum properties to the cps store.
```bash
galasactl resources apply -f `needed-properties.yaml` --bootstrap http://localhost:8080/bootstrap
```

That should apply all the properties needed to get the API server running other things, like queries against the couchdb db.
```bash
galasactl runs get --age 1d --bootstrap http://localhost:8080/bootstrap
```



