# Wildfly - IBMMQ: JNDI Lookup

This project shows how to connect throught JDNI directly to IBM MQ context to send messages to the broker.

## Pre-requisites

- Eclispe with IBM MQ Explorer plugin installed.

## IBM MQ configuration

1. Install docker or podman (this is not explained here).

    1.1. (Optional) IBM MQ Mac M1 chips:

IBM MQ container image is not available to Mac M1 chips. If you are running your developer enviroment in M1 Mac, you need to muild IBM MQ image locally, by running the following:

- Make the container image:

    ```sh
    git clone https://github.com/ibm-messaging/mq-container.git
    cd mq-container
    make build-devserver
    ```

- To verify if it was correctly installed, just run the following:

```sh
podman images
```
- Tag the image to latest, change the name of the image and version with the output of your enviroment:

```sh
podman tag localhost/ibm-mqadvanced-server-dev:9.4.1.0-arm64 ibmcom/mq:latest
```

- If everything is correct, you can delete the mq-container project.

2. Create the secrets for IBM MQ admin and app users:

```sh
printf "passw0rd" | podman secret create mqAdminPassword –
printf "passw0rd" | podman secret create mqAppPassword -
```


3. Run the IBM MQ container:

```sh
podman run \
  --env LICENSE=accept \
  --env MQ_QMGR_NAME=QM1 \
  --publish 1414:1414 \
  --publish 9443:9443 \
  --detach \
  --secret mqAdminPassword \
  --secret mqAppPassword \
  --name ibmmq \
  ibmcom/mq
```

4. Create the ConnectionFactory and Queue binding file that the Wildfly uses to access IBM MQ objects by JNDI:

    4.1. On MQ Explorer Navigator (Eclipse), on folder Queue Managers, create an connection to the queue manager QM1 (The one that we create above).

    4.2. On MQ Explorer Navigator (Eclipse), on folder JMS Administered Objects, add an new Initial Context. You need to choose a destination. In this example, the destination is: ${jboss.server.data.dir}/bindings/

    4.3. On the folder Connection Factories, create a new Connection Factory with the following properties:
    - Name: QM1_CF
    - Transport: MQ Client
    - Connection -> Base Queue manager: QM1
    - Channels -> Channel: DEV.APP.SVRCONN

    4.4. On the folder Destinations, create a new Destination with the following properties:
    - Name: DEV.QUEUE.1
    - General -> Queue Manager: QM1
    - General -> Queue: DEV.QUEUE.1


## Wildfly configuration and deployment

1. Install the IBM MQ Resource adapter in Wildfly:

1.1. Download the lastest IBM MQ RA from IBM web page. The page on the time of this write is this [one](https://www.ibm.com/support/fixcentral/swg/selectFixes?parent=ibm~WebSphere&product=ibm/WebSphere/WebSphere+MQ&release=9.4.0.0&platform=All&function=fixId&fixids=*IBM-MQ-Install-Java-All*&includeSupersedes=0&source=fc)

1.2. Extract the file (Accept the license also):

```sh
java -jar <version>-IBM-MQ-Install-Java-All.jar
```

1.3. Install extracted IBM MQ resource adapter on Wildfly/JBoss EAP:
```sh
cd /path/to/wildfly
bin/jboss-cli.sh --connect
module add --name=com.ibm.mq --resources=/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/bcpkix-jdk18on.jar,/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/bcprov-jdk18on.jar,/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/bcutil-jdk18on.jar,/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/com.ibm.mq.allclient.jar,/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/fscontext.jar,/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/jms.jar,/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/org.json.jar,/path/to/extracted/jar/wmq/JavaSE/lib/modules/javax/providerutil.jar
```

2. Add this to the naming subsystem ( notice that the property java.naming.provider.url is pointing to the path that we configured on step 4.2):

```xml
<subsystem xmlns="urn:jboss:domain:naming:2.0">
    <bindings>
        <external-context name="java:global/remote-ibmmq-context" module="com.ibm.mq" class="javax.naming.InitialContext">
            <environment>
                <property name="java.naming.factory.initial" value="com.sun.jndi.fscontext.RefFSContextFactory"/>
                <property name="java.naming.provider.url" value="file:///${jboss.server.data.dir}/bindings/"/>
                <property name="java.naming.factory.url.pkgs" value="com.ibm.msg.client.jms"/>
                <property name="org.jboss.as.naming.lookup.by.string" value="true"/>
            </environment>
        </external-context>
        <lookup name="java:/jboss/exported/DEV.QUEUE.1" lookup="java:global/remote-ibmmq-context/DEV.QUEUE.1"/>
        <lookup name="java:/DEV.QUEUE.1" lookup="java:global/remote-ibmmq-context/DEV.QUEUE.1"/>
    </bindings>
    <remote-naming/>
</subsystem>
```

2. Compile and deploy the application (Wildfly/JBoss EAP should be running):

```sh
mvn wildfly:deploy 
```

## Test the integration

1. Just run the following:

```sh
curl http://localhost:8080/remote-ibmmq-context-jndi-lookup-1.0.0/test
```