package io.github.angelogalvao.example.jakarta;

import org.jboss.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jms.Destination;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.ws.rs.Path;

@Path("/message")
public class MessageResource {

    private static long counter = 0;

    @Inject
    @JMSConnectionFactory("java:/sharedBrokerCF")
    private JMSContext context;

    @Resource(lookup = "java:/TestQueue")
    private Destination testQueue;

    private static Logger log = Logger.getLogger(MessageResource.class);
    
    @Path("/send/test")
    public void sendTestMessage(){
        log.info("Sending Test Message...");
        context.createProducer().send(testQueue, "TEST MESSAGE " + ++counter);
        log.info("Test message sent!");
    }
}
