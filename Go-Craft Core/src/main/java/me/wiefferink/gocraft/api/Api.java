package me.wiefferink.gocraft.api;

import com.google.gson.Gson;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.PemKeyCertOptions;
import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.api.messages.out.Response;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Api implements Runnable {

	public static final int PORT = 9192;

	private Vertx vertx;
	private final Map<UUID, WebClient> clients = new ConcurrentHashMap<>();

	public Api() {
		GoCraftBungee plugin = GoCraftBungee.getInstance();
		this.vertx = Vertx.vertx();
		plugin.getProxy().getScheduler().runAsync(plugin, this);

		// Setup Gson object mapping for Unirest
		Unirest.setObjectMapper(new ObjectMapper() {
			private Gson gson = new Gson();

			public <T> T readValue(String s, Class<T> aClass) {
				try {
					return gson.fromJson(s, aClass);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}

			public String writeValue(Object o) {
				try {
					return gson.toJson(o);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	@Override
	public void run() {
		// Setup certificate, based on: https://gist.github.com/InfoSec812/a45eb3b7ba9d4b2a9b94
		String certPath = GoCraftBungee.getInstance().getGeneralConfig().getString("settings.certificate.cert");
		if(certPath == null || certPath.isEmpty()) {
			Log.error("Empty certificate path for the REST API!");
		}
		String certKeyPath = GoCraftBungee.getInstance().getGeneralConfig().getString("settings.certificate.key");
		if(certKeyPath == null || certKeyPath.isEmpty()) {
			Log.error("Empty certificate path for the REST API!");
		}

		HttpServerOptions httpOpts = new HttpServerOptions();
		httpOpts.setPemKeyCertOptions(new PemKeyCertOptions()
				.setCertPath(certPath)
				.setKeyPath(certKeyPath));
		httpOpts.setSsl(true);

		// Create server
		HttpServer server = vertx.createHttpServer(httpOpts)
			.websocketHandler(websocket -> {
				synchronized(clients) {
					UUID key;
					do {
						key = UUID.randomUUID();
					} while(clients.containsKey(key));
					WebClient client = new WebClient(websocket, this, key);
					clients.put(key, client);
				}
			})
			.listen(PORT, "0.0.0.0");
	}

	/**
	 * Stop the api
	 */
	public void stop() {
		synchronized(clients) {
			clients.values().forEach(WebClient::stop);
		}
		this.vertx.close();
	}

	/**
	 * Broadcast a message to all connected clients
	 * @param response Response to broadcast
	 */
	public void broadcast(Response response) {
		String responseString = response.toString();
		clients.values().forEach(webClient -> webClient.message(responseString));
	}

	/**
	 * Add a client
	 * @param client WebClient to add
	 */
	public void addClient(WebClient client) {
		clients.put(client.getKey(), client);
	}

	/**
	 * Remove a client
	 * @param client WebClient to remove
	 */
	public void removeClient(WebClient client) {
		clients.remove(client.getKey());
	}

	/**
	 * Get the connected clients
	 * @return Map with the connected clients (thread-safe)
	 */
	public Map<UUID, WebClient> getClients() {
		return clients;
	}
}
