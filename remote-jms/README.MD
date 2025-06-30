# Remote JMS (Inside Wildfly)

This example produces and consumes messages from Wildfly embebeed broker, running in a remote server, configured using the domain mode. 

## Configuration of the domain mode. 

1. Clone the full profile, create 2 news profiles:
- shared-broker : This profile runs the remote broker : port-offest 1 
- shared-broker-client : This profile runs the client that access the broker : port-offest 2 

2. Configure the messaging subsystem in the shared-broker profile and create a queue: 

~~~xml
<jms-queue name="TestQueue" entries="java:/jms/queue/test"/>
~~~

3. Configure the messaging subsystem in the shared-broker-client profile to point to the broker (bellow just what was added): 

~~~xml
<subsystem xmlns="urn:jboss:domain:messaging-activemq:15.0">
    <external-jms-queue name="TestQueue" entries="java:/TestQueue"/>
    <server name="default">
        ...
        <http-connector name="shared-broker-connector" socket-binding="shared-broker-target" endpoint="http-acceptor"/>
        ...       
        <pooled-connection-factory name="shared-broker-cf" entries="java:/sharedBrokerCF" connectors="shared-broker-connector" transaction="xa" user="broker" password="broker"/>
    </server>
</subsystem>
~~~
Notice that I created the user "broker"with the role "guest" in Wildfly.

4. Configure the naming subsystem so you can lookup the destination on the remote broker:

~~~xml
<subsystem xmlns="urn:jboss:domain:naming:2.0">
    <bindings>
        <external-context name="java:global/sharedBrokerContext" module="org.apache.activemq.artemis" class="javax.naming.InitialContext">
            <environment>
                <property name="java.naming.factory.initial" value="org.wildfly.naming.client.WildFlyInitialContextFactory"/>
                <property name="java.naming.provider.url" value="http-remoting://remotehost:8081?useKQueue=false"/>
                <property name="queue.TestQueue" value="TestQueue"/>
            </environment>
        </external-context>
    </bindings>
    <remote-naming/>
</subsystem>
~~~
Notice that port 8081 = default 8080 port + 1 port offset of the server group.

5. Configure the outboud socket binding that points to the remote broker endpoint:

~~~xml
<outbound-socket-binding name="shared-broker-target">
    <remote-destination host="localhost" port="8081"/>
</outbound-socket-binding>
~~~
Notice that port 8081 = default 8080 port + 1 port offset of the server group.

### Example configurration

The project has the file [domain.xml](configuration/domain.xml) as a reference for JBoss EAP 8.0.

## Build and deploy

1. Build the project by running:

~~~sh
$ mvn clean package
~~~

2. Deploy the application by using the Wildfly console

## Test the application

1. To test the application, just run the following in the terminal

~~~sh
curl -v http://localhost:8082/remote-jms/message/send/test
~~~ 