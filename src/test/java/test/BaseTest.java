package test;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

public class BaseTest {
	static int port = freePort();

	static Vertx vertx;

	@BeforeClass
	public static void setUp(TestContext context) throws Exception {

		vertx = Vertx.vertx();

		JsonObject serviceConfig = new JsonObject();
		serviceConfig.put("host", "127.0.0.1");
		serviceConfig.put("port", port);
		serviceConfig.put("client_id", "client_id");
		serviceConfig.put("client_secret", "client_secret");

		DeploymentOptions options = new DeploymentOptions().setConfig(serviceConfig);
		vertx.deployVerticle(Verticle.class.getName(), options, context.asyncAssertSuccess());
	}

	@AfterClass
	public static void shutdown(TestContext context) {
		vertx.close(context.asyncAssertSuccess());
	}

	private static int freePort() {
		try {
			ServerSocket socket = new ServerSocket(0);
			int port = socket.getLocalPort();
			socket.close();
			return port;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
