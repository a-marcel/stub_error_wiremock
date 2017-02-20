package test;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import rx.Observable;

public class TestServiceImpl implements TestService {

	JsonObject config;
	Vertx vertx;

	HttpClient httpClient;

	private JsonObject oauthConfig;
	private OAuth2Auth managementAuth;
	private AccessToken managementTokenCached;
	AuthProvider authProvider;

	public TestServiceImpl(Vertx vertx, JsonObject config) {
		this.config = config;
		this.vertx = vertx;

		HttpClientOptions options = new HttpClientOptions().setSsl(false).setTrustAll(true)
				.setDefaultHost(this.config.getString("host")).setDefaultPort(this.config.getInteger("port"));

		httpClient = vertx.createHttpClient(options);

		OAuth2ClientOptions credentials = new OAuth2ClientOptions().setClientID(this.config.getString("client_id"))
				.setClientSecret(this.config.getString("client_secret"))
				.setSite("http://" + config.getString("host") + ":" + config.getInteger("port", 443).toString());

		managementAuth = OAuth2Auth.create(vertx, OAuth2FlowType.CLIENT, credentials);
		oauthConfig = new JsonObject().put("client_id", this.config.getString("client_id"))
				.put("client_secret", this.config.getString("client_secret")).put("grant_type", "grant")
				.put("scope", "read:read");

	}

	@Override
	public TestService test() {

		getManagementToken().concatMap(result -> {
			return Observable.create(subscriber -> {
				JsonObject patchData = new JsonObject();
				patchData.put("data", new JsonObject().put("test", "123"));

				System.out.print("Connecting to: " + "http://" + this.config.getString("host") + ":"
						+ this.config.getInteger("port").toString() + "/api/v2/users/123");

				HttpClientRequest request = httpClient.request(HttpMethod.PATCH, "/api/v2/users/123", response -> {
					Buffer totalBuffer = Buffer.buffer();

					int statusCode = response.statusCode();

					response.handler(buffer -> {
						totalBuffer.appendBuffer(buffer);
					});

					response.endHandler(v -> {
						if (statusCode != 200) {
							System.out.println("ERROR");
							// m.reply(new JsonObject().put("status", "error"));
						} else {
							JsonObject r = totalBuffer.toJsonObject();
							System.out.println(r);

							// r.put("status", "ok");
							// m.reply(r);
						}
					});
				});

				request.exceptionHandler(handler -> {
					handler.printStackTrace();
				});

				request.putHeader("content-type", "application/json");

				request.end(patchData.toString());
			});
		}).doOnError(e -> {
			e.printStackTrace();
		}).subscribe(result -> {

			System.out.println(result);
		});
		return this;
	}

	Observable<JsonObject> getManagementToken() {
		if (null == this.managementTokenCached || this.managementTokenCached.expired()) {
			return Observable.create(subscriber -> {
				managementAuth.getToken(this.oauthConfig, res -> {
					if (res.failed()) {
						res.cause().printStackTrace();
						subscriber.onError(new Exception("ERROR"));
						return;
					}

					this.managementTokenCached = res.result();

					subscriber.onNext(this.managementTokenCached.principal());
					subscriber.onCompleted();
				});
			});
		} else {
			return Observable.just(this.managementTokenCached.principal());
		}
	}

}
