# Setup local Galasa APIs for testing

## Overview

In order to test new developments on the Galasa API servlets you need to be able to run them locally.
The below instructions will help you setup couchDB, etcd and a local API server in order to run tests against the corresponding Galasa APIs.

## External Applications - Using Docker

Use the `run-locally.sh` script to launch services inside docker containers.

Get gelp for the command is: `run-locally.sh --help`

eg: 
```
run-locally.sh --all
```

or 
```
run-locally.sh --etcd
run-locally.sh --couchdb
run-locally.sh --dex
```

Note: When you start couchdb for the first time you need to set up a user database using the 
wizards accessed from the settings page of the UI.


### Environment variables used
For couchdb: You get to make up the admin password.
```
export COUCHDB_PASSWORD=123xxxx4asxredactedxxxxxxasd
export COUCHDB_USER=admin
```


Note: dex requires a password. It will be created on the first run, and you need to put the value
into your .zprofile or similar to make sure the value is retained.
```
export DEX_ADMIN_PASSWORD="$2y$10$hMpxxxxxredactedxxxxxxxxxgAu3Rfjq"
```

## External Applications - Manually

If you want to test the API server as if it was running as part of an ecosystem you will need to install and run applications external to galasa.

The Galasa API service will still run if none of these applications is installed. It will use the local Galasa settings in the `.galasa` directory.

### 1.1 CouchDB

The current version of CouchDB that is used by Galasa is version 3.3.3.

#### Docker install (recommended)
docker pull couchdb:3.3.3
docker run -it -p 5984:5984 couchdb:3.3.3

#### Local/Native install (not recommended)
It can be downloaded [here](https://couchdb.apache.org/#download) or you can downlaod it from the [archive](http://archive.apache.org/dist/couchdb/binary/)

Once you have installed CouchDB you will need to create a username and password in order to authorise tranactions with the database. Keep this in mind as you will need it to view and access the database.

You should now be able access your local couchDB by going to [http://127.0.0.1:5984/_utils](http://127.0.0.1:5984/_utils)
Once you have logged in you should be able to go to the verification page and verify the couchDB Installation by clicking the green "Verify Installation" button and getting a success message.

### 1.2 etcd

Install etcd on your machine by following the official instructions [here](https://etcd.io/docs/v3.5/install/)

### 1.3 Dex
Galasa uses [Dex](https://dexidp.io) for authentication using OpenID Connect and needs a Dex server to be running for it to authenticate users.

For instructions on installing and configuring Dex, please refer to Dex's documentation on building the Dex binary locally [here](https://dexidp.io/docs/getting-started).

When configuring Dex, ensure that you uncomment the `grpc` section in the `config-dev.yaml` file, so that the following values are set:

```yaml
grpc:
  addr: 127.0.0.1:5557
  reflection: true
```

Take note of the `addr` value above as well as the `issuer` value within your `config-dev.yaml` file as these will need to be set as environment variables before starting the Galasa API server, which is shown in the next section.

If you would like to configure Dex to authenticate with an upstream identity provider, like GitHub, Google, or Microsoft, please refer to the appropriate instructions in Dex's documentation on [connectors](https://dexidp.io/docs/connectors).

## 2. Set Environment variables and aliases

As the commands for running the API server or even a test are so big, it would be wise to make aliases for them by placing the text below into your shell's rc file.
Additionally setting the export for the authorization token from above means you will only need to do it once in the rc file.

To see which file you will need to update, check the shell you are using by running `echo $SHELL`.
Use the table below to determine which file in your home direcotry you will need to change:

|$SHELL   |filename|
|---------|--------|
|/bin/zsh |.zshrc  |
|/bin/bash|.bashrc |
|/bin/sh  |.shrc   |
|/bin/ksh |.kshrc  |

Open the file and paste the below in replacing `${HOME}` with the fully qualified path to your home directory, which can be identified using `echo $HOME`.
Please note that the version numbers in these commmands will need to be updated to the next version number of Galasa in order to work with your changes.

```shell
export GALASA_OBR_VERSION=0.32.0
export GALASA_BOOT_JAR_VERSION=0.32.0
export GALASA_EXTERNAL_API_URL="http://localhost:8080"
export GALASA_USERNAME_CLAIMS="preferred_username,name,sub"
export GALASA_ALLOWED_ORIGINS="*"

# The GALASA_DEX_ISSUER environment variable must match the "issuer" value
# within your local Dex server's configuration
export GALASA_DEX_ISSUER="http://127.0.0.1:5556/dex"

# The GALASA_DEX_GRPC_HOSTNAME environment variable must match the "addr" value
# within the "grpc" section in your local Dex server's configuration 
export GALASA_DEX_GRPC_HOSTNAME="127.0.0.1:5557"

# Replace "/path/to/dex/repo" with the absolute path to the Dex repository that
# you cloned when setting up Dex locally
alias galasadex='cd /path/to/dex/repo && bin/dex serve examples/config-dev.yaml'

alias galasaapi='java -jar ~/.m2/repository/dev/galasa/galasa-boot/${GALASA_BOOT_JAR_VERSION}/galasa-boot-${GALASA_BOOT_JAR_VERSION}.jar --api --localmaven file://${HOME}/.m2/repository/ --remotemaven https://development.galasa.dev/ --obr mvn:dev.galasa/dev.galasa.uber.obr/${GALASA_OBR_VERSION}/obr ;'

alias galasatest='java -jar ~/.m2/repository/dev/galasa/galasa-boot/${GALASA_BOOT_JAR_VERSION}/galasa-boot-${GALASA_BOOT_JAR_VERSION}.jar --api --localmaven file://${HOME}/.m2/repository/ --remotemaven https://development.galasa.dev/  --obr mvn:dev.galasa/dev.galasa.uber.obr/${GALASA_OBR_VERSION}/obr  --obr mvn:dev.galasa.example.banking/dev.galasa.example.banking.obr/0.0.1-SNAPSHOT/obr --test dev.galasa.example.banking.account/dev.galasa.example.banking.account.TestAccount;'

```

You will need to reload the file to take effect using the following command and replacing the `.zshrc` with the appropriate file name depending on your shell.

``` shell
% source ~/.zshrc
```

## 3. Update Properties files

Before you can run a test against the new database you will need to tell Galasa where your database is. To do this insert the below lines into the corresponding files in your .galasa direcotry.

Please note that if you make future runs against a Galasa ecosystem you will need to override these properties.

#### cps.properties

```shell
framework.resultarchive.store=couchdb:http://127.0.0.1:5984
```

When the local API server is started for the first time, you will also need to supply this property into the CPS API in order for Galasa to pick it up.

To do this run the command below using `galasactl`.

```shell
galasactl properties set --namespace framework --name resultarchive.store --value "http://127.0.0.1:5984"
```

#### bootstrap.properties

```shell
framework.config.store=etcd\:http\://127.0.0.1\:2379
framework.extra.bundles=dev.galasa.cps.etcd,dev.galasa.ras.couchdb
```

### 3.1 Update Properties for CouchDB only

If you are only using the couchDB external application you will need to use the lines in this section with your local Galasa API instance.

#### 3.1.1 cps.properties

```shell
framework.resultarchive.store=couchdb:http://127.0.0.1:5984
```

#### 3.1.2 bootstrap.properties

```shell
framework.extra.bundles=dev.galasa.ras.couchdb
```

### 3.2 Update Properties for etcd only

If you are only using the etcd external application you will need to use the lines in this section with your local Galasa API instance.

#### 3.2.1 bootstrap.properties

```shell
framework.config.store=etcd\:http\://127.0.0.1\:2379
framework.extra.bundles=dev.galasa.cps.etcd
```

### 4. Build the framework locally

In order to run the full functionality you will need to have all core framework projects downloaded and built by using the [build-all-locally.sh](../200-automation/building-locally.md) script. If you do not have that script speak to another developer to get it.
You will not be able to continue without a full local build.

### 5. Start the API server

You are now ready to run your test file.

If you are using the etcd/couchDB external applications please start those before using the aliases to avoid start-up errors from galasa. Likewise, please ensure your local Dex server is running to enable authentication in the API server.

The `galasatest` alias uses the tests generated by the cli from the full build in order to run a test and populate the database.

Once you have a finished test you should be able to use the `galasaapi` alias to start the API server on [http://127.0.0.1:8080/](http://127.0.0.1:8080) where you can navigate to the appropriate API via a web browser or the `galasactl` CLI.
