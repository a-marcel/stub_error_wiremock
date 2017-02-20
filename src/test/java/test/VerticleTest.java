package test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Date;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class VerticleTest extends BaseTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(options().bindAddress("127.0.0.1").port(port));

	@Test
	public void fireEvent(TestContext context) {

		JsonObject managementAuth = new JsonObject();
		managementAuth.put("access_token", "access_token");
		managementAuth.put("refresh_token", "refresh_token");
		managementAuth.put("token_type", "baerer");
		managementAuth.put("expires_in", 3600);

		stubFor(post(urlMatching("/oauth/token")).willReturn(aResponse().withStatus(200)
				.withBody(managementAuth.toString()).withHeader("content-type", "application/json")));

		stubFor(patch(urlMatching("/api/v2/users/.*")).willReturn(aResponse().withStatus(200)
				.withBody(new JsonObject().toString()).withHeader("content-type", "application/json")));

		JsonObject message = new JsonObject();
		message.put("timestamp", new Date().getTime());

		Async async = context.async();

		vertx.eventBus().send(TestService.EVENTS_CHANNEL, message, result -> {
			// JsonObject rJson = new
			// JsonObject(result.result().body().toString());

			// context.assertEquals("ok", rJson.getString("status"));
			async.complete();
		});
	}

}
