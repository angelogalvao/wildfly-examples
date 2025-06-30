package io.github.angelogalvao.example.jakarta;

import org.jboss.ejb3.annotation.ResourceAdapter;
import org.jboss.logging.Logger;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;

/**
 * @author Angelo Galvao
 */
@MessageDriven( name = "MessageListener" , activationConfig = {
    @ActivationConfigProperty(propertyName = "useJNDI", propertyValue = "true"),
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/TestQueue")
})
@ResourceAdapter("shared-broker-cf")
public class MessageListener implements jakarta.jms.MessageListener{

    private static Logger log = Logger.getLogger(MessageListener.class);

    @Override
    public void onMessage(Message message) {
        try {
            log.info("Message received: " + ((TextMessage)message).getText());
        } catch (JMSException e) {
            log.error("Error consuming the message", e);
        }
    }

}
