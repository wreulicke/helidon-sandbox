package com.github.wreulicke.helidon;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.json.JsonSupport;

/**
 * Simple Hello World rest application.
 */
public final class Main {
	
	private static final Logger log = LoggerFactory.getLogger(Main.class);
	
	/**
	 * Creates new {@link Routing}.
	 *
	 * @return the new instance
	 */
	private static Routing createRouting() {
		return Routing.builder()
			.register(JsonSupport.get())
			.register("/greet", new GreetService())
			.build();
	}
	
	/**
	 * Application main entry point.
	 * @param args command line arguments.
	 * @throws IOException if there are problems reading logging properties
	 */
	public static void main(final String[] args) throws IOException {
		startServer();
	}
	
	/**
	 * Start the server.
	 * @return the created {@link WebServer} instance
	 * @throws IOException if there are problems reading logging properties
	 */
	private static WebServer startServer() throws IOException {
		// By default this will pick up application.yaml from the classpath
		Config config = Config.create();
		
		// Get webserver config from the "server" section of application.yaml
		ServerConfiguration serverConfig =
			ServerConfiguration.fromConfig(config.get("server"));
		
		WebServer server = WebServer.create(serverConfig, createRouting());
		
		// Start the server and print some info.
		server.start().
			thenAccept(ws -> log.info("WEB server is up! http://localhost:{}", ws.port()));
		
		// Server threads are not demon. NO need to block. Just react.
		server.whenShutdown().thenRun(() -> log.info("WEB server is DOWN. Good bye!"));
		
		return server;
	}
}
