package com.angelogalvao;

import javax.naming.InitialContext;

public class IBMMQRemoteLookup {

	public static void main(String[] args) throws Throwable {
		
		java.util.Properties prop = new java.util.Properties();
        prop.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        prop.put(javax.naming.Context.PROVIDER_URL, "remote://localhost:4447");
		
        InitialContext ctx = new InitialContext(prop);
        
        
        Object returned_object = ctx.lookup("DEV.QUEUE.1");
        
        System.out.println("Object is null: " + returned_object == null);
        System.out.println("Object instance type: " + returned_object.getClass().getName());
	} 
}
