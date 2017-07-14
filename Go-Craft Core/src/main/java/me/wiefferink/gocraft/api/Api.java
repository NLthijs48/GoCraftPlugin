package me.wiefferink.gocraft.api;

import com.google.gson.Gson;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.api.messages.out.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Api implements Runnable {

	public static final int PORT = 9192;

	private GoCraftBungee plugin;
	private Vertx vertx;
	private final Map<UUID, WebClient> clients = new HashMap<>();

	public Api() {
		this.plugin = GoCraftBungee.getInstance();
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
		HttpServer server = vertx.createHttpServer()
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
			for(WebClient client : clients.values()) {
				client.stop();
			}
		}
		this.vertx.close();
	}

	/**
	 * Broadcast a message to all connected clients
	 * @param response Response to broadcast
	 */
	public void broadcast(Response response) {
		String responseString = response.toString();
		synchronized(clients) {
			for(WebClient client : clients.values()) {
				client.message(responseString);
			}
		}
	}

	/**
	 * Add a client
	 * @param client WebClient to add
	 */
	public void addClient(WebClient client) {
		synchronized(clients) {
			clients.put(client.getKey(), client);
		}
	}

	/**
	 * Remove a client
	 * @param client WebClient to remove
	 */
	public void removeClient(WebClient client) {
		synchronized(clients) {
			clients.remove(client.getKey());
		}
	}
}
