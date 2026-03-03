# Galasa MQ Manager IVT

This directory contains the IVTs for Galasa MQ Manager.

## Prerequisites

To run the IVTs, you will need:

- An MQ instance running locally (see [Get an IBM MQ queue for development in a container](https://developer.ibm.com/tutorials/mq-connect-app-queue-manager-containers/) for instructions on how to set this up using Docker or podman)

## Running the IVTs

1. Assuming you have followed the instructions in the [Get an IBM MQ queue for development in a container](https://developer.ibm.com/tutorials/mq-connect-app-queue-manager-containers/) tutorial, you will now need to set the following properties into your CPS properties file:

    ```properties
    mq.tag.bob.instanceid=QUEUEMGR1
    mq.queue.bob.queuename=DEV.QUEUE.1
    mq.server.QUEUEMGR1.channel=DEV.APP.SVRCONN
    mq.server.QUEUEMGR1.credentials.id=MQIVT
    mq.server.QUEUEMGR1.host=127.0.0.1
    mq.server.QUEUEMGR1.name=QM1
    mq.server.QUEUEMGR1.port=1414
    ```

2. Now set the following properties into your credentials properties file, making sure the username and password corresponds to the details set when starting the Docker container:

    ```properties
    secure.credentials.MQIVT.username=app
    secure.credentials.MQIVT.password=passw0rd
    ```

3. Run the IVTs using the following command, replacing `{YOUR_GALASA_VERSION}` with the version of Galasa that you want to use:
    ```
    galasactl runs submit local --obr mvn:dev.galasa/dev.galasa.uber.obr/{YOUR_GALASA_VERSION}/obr --class dev.galasa.mq.manager.ivt/dev.galasa.mq.manager.ivt.MqManagerIVT --log -
    ```
