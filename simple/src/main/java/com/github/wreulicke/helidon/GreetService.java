package com.github.wreulicke.helidon;

import javax.json.Json;
import javax.json.JsonObject;

import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

/**
 * A simple service to greet you. Examples:
 *
 * Get default greeting message:
 * curl -X GET http://localhost:8080/greet
 *
 * Get greeting message for Joe:
 * curl -X GET http://localhost:8080/greet/Joe
 *
 * Change greeting
 * curl -X PUT http://localhost:8080/greet/greeting/Hola
 *
 * The message is returned as a JSON object
 */

public class GreetService implements Service {
	
	/**
	 * This gets config from application.yaml on classpath
	 * and uses "app" section.
	 */
	private static final Config CONFIG = Config.create().get("app");
	
	/**
	 * The config value for the key {@code greeting}.
	 */
	private static String greeting = CONFIG.get("greeting").asString("Ciao");
	
	/**
	 * A service registers itself by updating the routine rules.
	 * @param rules the routing rules.
	 */
	@Override
	public final void update(final Routing.Rules rules) {
		rules
			.get("/", this::getDefaultMessage)
			.get("/{name}", this::getMessage)
			.put("/greeting/{greeting}", this::updateGreeting);
	}
	
	/**
	 * Return a wordly greeting message.
	 * @param request the server request
	 * @param response the server response
	 */
	private void getDefaultMessage(final ServerRequest request,
		final ServerResponse response) {
		String msg = String.format("%s %s!", greeting, "World");
		
		JsonObject returnObject = Json.createObjectBuilder()
			.add("message", msg)
			.build();
		response.send(returnObject);
	}
	
	/**
	 * Return a greeting message using the name that was provided.
	 * @param request the server request
	 * @param response the server response
	 */
	private void getMessage(final ServerRequest request,
		final ServerResponse response) {
		String name = request.path().param("name");
		String msg = String.format("%s %s!", greeting, name);
		
		JsonObject returnObject = Json.createObjectBuilder()
			.add("message", msg)
			.build();
		response.send(returnObject);
	}
	
	/**
	 * Set the greeting to use in future messages.
	 * @param request the server request
	 * @param response the server response
	 */
	private void updateGreeting(final ServerRequest request,
		final ServerResponse response) {
		greeting = request.path().param("greeting");
		
		JsonObject returnObject = Json.createObjectBuilder()
			.add("greeting", greeting)
			.build();
		response.send(returnObject);
	}
}
