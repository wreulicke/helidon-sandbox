package com.github.wreulicke.helidon;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.helidon.config.Config;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.json.JsonSupport;
import io.helidon.webserver.prometheus.PrometheusSupport;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

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
		PrometheusMeterRegistry prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
		
		new JvmMemoryMetrics().bindTo(prometheusMeterRegistry);
		new JvmGcMetrics().bindTo(prometheusMeterRegistry);
		new JvmThreadMetrics().bindTo(prometheusMeterRegistry);
		new LogbackMetrics().bindTo(prometheusMeterRegistry);
		
		return Routing.builder()
			.register(JsonSupport.get())
			// .register(MetricsSupport.create()) micrometerのメトリクスを追加する方法が分からなかった
			// 自分でメトリクス取るコードを追加するのはできる。
			// https://github.com/oracle/helidon/blob/03cd458e34dc141f60d5c3bc66751cc2e3ce2b29/microprofile/metrics/metrics-se/src/main/java/io/helidon/metrics/MetricsSupport.java#L75
			
			// なので、PrometheusSupportとmicrometerのPrometheusMeterRegistryでPrometheusのエンドポイントはやしてみた。
			// 取れるものは、上で登録したメトリクスはMetricsSupportで追加したエンドポイントで取れる情報は大体同じだと思う
			 .register(PrometheusSupport.create(prometheusMeterRegistry.getPrometheusRegistry()))
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
