package test;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;

@VertxGen
@ProxyGen
public interface TestService {

	/**
	 * The address on which the service is published.
	 */
	String SERVICE_ADDRESS = "service.message";

	String EVENTS_CHANNEL = "test";

	@Fluent
	public TestService test();
}
