# Running the Db2 Manager IVT locally

To run the Db2 manager IVT, it requires a Db2 instance to connect to. This can be achieved locally using [Db2 Community Edition for Docker](https://www.ibm.com/docs/en/db2/11.5?topic=deployments-db2-community-edition-docker).

## Prerequisites

You will need:

- Docker engine (e.g. using [Rancher Desktop](https://rancherdesktop.io) or [colima](https://github.com/abiosoft/colima))
- [galasactl](https://github.com/galasa-dev/cli/releases)
- Gradle 6.8.2

### Running on Apple Silicon

**If you are not on an Apple Silicon machine (e.g. M1 MacBook), you can skip this section.**

Since the Db2 Community Edition Docker image is only built for x86-based machines, if you are on an Apple Silicon machine, you can start an x86_64 Docker environment with Rosetta emulation, using [colima](https://github.com/abiosoft/colima).

To do this:

1. Install colima with `brew install colima`
2. Start the x86_64 Docker environment by running:
    ```bash
    colima start --arch x86_64 --vm-type=vz --vz-rosetta
    ```
3. Verify that your Docker context is set to the new `colima` context using `docker context ls`. The output should look like this:
   ```bash
    NAME              DESCRIPTION                               DOCKER ENDPOINT                                ERROR
    colima *          colima                                    unix:///Users/em/.colima/default/docker.sock
    default           Current DOCKER_HOST based configuration   unix:///var/run/docker.sock
    rancher-desktop   Rancher Desktop moby context              unix:///Users/em/.rd/docker.sock
   ```

## Steps

### 1. Running Db2 in a Docker container

More guidance around running Db2 on different operating systems can be found [here](https://www.ibm.com/docs/en/db2/11.5?topic=system-linux).

To run Db2 locally, you can start a Docker container as follows:

1. Create a `.env_list` file with the following contents:
    ```
    LICENSE=accept
    DB2INSTANCE=db2inst1
    DB2INST1_PASSWORD=password
    DBNAME=testdb
    BLU=false
    ENABLE_ORACLE_COMPATIBILITY=false
    UPDATEAVAIL=NO
    TO_CREATE_SAMPLEDB=false
    REPODB=false
    IS_OSXFS=true
    PERSISTENT_HOME=false
    HADR_ENABLED=false
    ```

    Note: the `DB2INSTANCE` and `DB2INST1_PASSWORD` values will be used as the credentials to connect to the Db2 server in the IVT.

2. Pull the Db2 Docker image from ICR:
    ```bash
    docker pull icr.io/db2_community/db2
    ```
3. Run a Db2 Docker container, making sure `/path/to/.env_list` is replaced with the actual absolute or relative path to the `.env_list` file you created earlier:
    ```bash
    docker run -h db2server --name db2server --restart=always --detach --privileged=true -p 50000:50000 --env-file /path/to/.env_list icr.io/db2_community/db2
    ```
4. It may take a while for Db2 to start, so you can view the logs of the created Docker container until a `(*) Setup has completed` message appears:
   ```bash
   docker logs db2server -f
   ```

5. Verify that the testdb database was created by logging into the Db2 Docker container:
    ```bash
    docker exec -ti db2server bash -c "su - db2inst1"
    ```
    
    Then, inside the Docker container, run:
    ```bash
    db2 list db directory
    ```

    The output should look like similar to the output below:
    ```bash
    [db2inst1@db2server ~]$ db2 list db directory

    System Database Directory

    Number of entries in the directory = 1

    Database 1 entry:

    Database alias                       = TESTDB
    Database name                        = TESTDB
    Local database directory             = /database/data
    Database release level               = 15.00
    Comment                              =
    Directory entry type                 = Indirect
    Catalog database partition number    = 0
    Alternate server hostname            =
    Alternate server port number         =
    ```

### 2. Setting CPS properties and Credentials

Once the Docker container running Db2 is working:

1. Add the following properties to your `cps.properties` file:

    ```properties
    db2.dse.instance.PRIMARY.name=DB2INST1
    db2.dse.schema.PRIMARY.name=testdb
    db2.instance.DB2INST1.url=jdbc:db2://127.0.0.1:50000/testdb
    db2.instance.DB2INST1.credentials=TESTDB
    ```

2. Add the following properties to your `credentials.properties` file:

    ```properties
    secure.credentials.TESTDB.username=db2inst1
    secure.credentials.TESTDB.password=password
    ```

### 3. Running the Db2 manager IVT

Having configured your `cps.properties` and `credentials.properties` files, you should now be able to run the Db2 manager IVT using `galasactl`:

1. Build the `managers` project using the `build-locally.sh` script:
    ```bash
    ./build-locally -c
    ```
2. Run the Db2 manager IVT, replacing `0.34.0` with the relevant Galasa OBR version you wish to use:
    ```bash
    galasactl runs submit local --obr mvn:dev.galasa/dev.galasa.uber.obr/0.34.0/obr --class dev.galasa.db2.manager.ivt/dev.galasa.db2.manager.ivt.Db2ManagerIVT --log -
    ```