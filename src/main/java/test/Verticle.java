package test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ProxyHelper;

public class Verticle extends AbstractVerticle {

	MessageConsumer<JsonObject> testServiceProxyHelper;
	MessageConsumer<Object> testConsumer;

	@Override
	public void start(Future<Void> future) throws Exception {
		super.start();

		TestServiceImpl testService = new TestServiceImpl(vertx, config());

		// register the service proxy on event bus
		testServiceProxyHelper = ProxyHelper.registerService(TestService.class, vertx, testService,
				TestService.SERVICE_ADDRESS);

		TestServiceVertxEBProxy testServiceProxy = new TestServiceVertxEBProxy(vertx, TestService.SERVICE_ADDRESS);

		bindEventBusListener(testServiceProxy).setHandler(future.completer());

	}

	private Future<Void> bindEventBusListener(TestService testService) {
		Future<Void> future = Future.future();

		this.testConsumer = vertx.eventBus().consumer(TestService.EVENTS_CHANNEL);
		this.testConsumer.handler(m -> {
			System.out.println("Recieved event " + m.body() + " headers: " + m.headers());
			
			testService.test();
		});
		this.testConsumer.completionHandler(future.completer());
		return future.map(r -> null);
	}

	@Override
	public void stop(Future<Void> future) throws Exception {
		if (null != this.testServiceProxyHelper && this.testServiceProxyHelper.isRegistered()) {
			this.testServiceProxyHelper.unregister();
		}

		if (null != this.testConsumer && this.testConsumer.isRegistered()) {
			this.testConsumer.unregister();
		}
		super.stop(future);
	}
}
