package com.angelogalvao;

import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Stateless
public class IBMMQRemoteContextJNDILookupResource {	

	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	@GET
	public String test() {
		
		try {
			
			InitialContext context = new InitialContext();


			ConnectionFactory cf    = (ConnectionFactory) context.lookup("java:global/remote-ibmmq-context/QM1_CF");
			Destination destination = (Destination)       context.lookup("java:global/remote-ibmmq-context/DEV.QUEUE.1");

			System.out.println("Connection factory and Destination loaded!");

			Connection connection = cf.createConnection("app", "passw0rd");

			System.out.println("Connection created!");

			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			System.out.println("Session created!");

			MessageProducer producer = session.createProducer(destination);

			System.out.println("MessageProducer created!");

			producer.send(session.createTextMessage("This messages is sent using JNDI binding with IBM MQ"));

			System.out.println("message sent!");
		} catch (Exception e) {
			e.printStackTrace();
			return "it not worked";
		} finally {

		}
		
		return "it worked";
	}
}
