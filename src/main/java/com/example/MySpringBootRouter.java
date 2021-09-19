package com.example;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {
	
	@Autowired
	private Environment env;

    @Override
    public void configure() throws Exception {
    	
    	String erpUri = "https://5298967-sb1.restlets.api.netsuite.com/app/site/hosting/restlet.nl?script=580&deploy=2";
    	
    	onException(HttpOperationFailedException.class)
    		.handled(true)
    		.process(exchange -> {
    			System.out.println("No se pudo llamar la interfaz");
    			System.out.println(exchange.getProperties());
    		});
    		// .continued(true); // Para continuar con la ruta
		
		/*rest()
			.path("/").consumes("application/json").produces("application/json")
				.get("/get-lead")
		//          .type(Customer.class).outType(CustomerSuccess.class)
				.to("direct:get-customer");

		from("direct:get-lead")
				.setHeader("HTTP_METHOD", constant("GET"))
				.to("direct:request");*/

		from("direct:get-lead")
        	.setHeader(Exchange.HTTP_URI, constant(erpUri))
			.setHeader("CamelHttpMethod", constant("GET"))
        	.process(new Processor() {
                @Override
                public void process(Exchange exchange) throws Exception {
                	String authHeader = OAuthSign.getAuthHeader(erpUri);
                    exchange.getMessage().setHeader("Authorization", authHeader);
                }
        	})
        	.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
        	.to("log:DEBUG?showBody=true&showHeaders=true")
        	.to("https://netsuite")
        	.to("log:DEBUG?showBody=true&showHeaders=true")
        	.to("stream:out");
    }

}
