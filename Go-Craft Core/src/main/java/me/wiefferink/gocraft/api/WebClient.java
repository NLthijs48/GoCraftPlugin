package me.wiefferink.gocraft.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.api.messages.in.*;
import me.wiefferink.gocraft.api.messages.out.*;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WebClient {

	private static final Gson gson = new Gson();

	private ServerWebSocket websocket;
	private String name;
	private Api api;
	private ConcurrentLinkedQueue<String> messages;
	private UUID key;

	// Map of command type to the handler class
	public Map<String, Class<? extends Request>> requests = new HashMap<String, Class<? extends Request>>() {{
		put("onlinePlayers", OnlinePlayersRequest.class);
		put("shopLayout", ShopLayoutRequest.class);
		put("voteStatus", VoteStatusRequest.class);
		put("voteTop", VoteTopRequest.class);
	}};

	public WebClient(ServerWebSocket websocket, Api api, UUID key) {
		this.websocket = websocket;
		this.api = api;
		this.key = key;
		messages = new ConcurrentLinkedQueue<>();

		name = websocket.remoteAddress().port()+"";
		websocket.closeHandler(v -> stop());
		websocket.exceptionHandler((e) -> error("exception:", ExceptionUtils.getStackTrace(e)));
		websocket.handler(message -> GoCraftBungee.getInstance().getProxy().getScheduler().runAsync(GoCraftBungee.getInstance(), () -> onMessage(message)));

		GoCraftBungee.getInstance().getProxy().getScheduler().runAsync(GoCraftBungee.getInstance(), this::pushInitialData);
	}

	/**
	 * Get the key of this WebClient
	 * @return Key of this client
	 */
	public UUID getKey() {
		return key;
	}

	/**
	 * Get the ip address of the connected user
	 * @return String witht he ip address
	 */
	public String getIp() {
		return websocket.remoteAddress().host();
	}

	// TODO create version of BukkitDo for Bungeecord

	/**
	 * Push all the initial data to the client we expect it needs
	 */
	private void pushInitialData() {
		GoCraftBungee.async(() -> message(new OnlinePlayersResponse()));
		GoCraftBungee.async(() -> message(new VoteStatusReponse(this)));
		GoCraftBungee.async(() -> message(new VoteTopResponse(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH)+1, 0, 10)));
		GoCraftBungee.async(() -> message(new ShopLayoutResponse()));
	}

	/**
	 * Called when receiving a message from the socket
	 * @param message Message received from the websocket
	 */
	public void onMessage(Buffer message) {
		// Parse as raw JSON
		JsonObject messageObject;
		try {
			messageObject = message.toJsonObject();
		} catch(DecodeException e) {
			error("message could not be decoded as JSON:", message, e);
			return;
		}
		if(messageObject == null) {
			error("received message that is not JSON:", message);
			return;
		}

		// Get the request type
		String requestType = messageObject.getString("type");
		if(requestType == null || requestType.isEmpty()) {
			error("message has no or empty type", message);
			return;
		}

		// Get the request class by type
		Class<? extends Request> requestClass = requests.get(requestType);
		if(requestClass == null) {
			error("unknown request type:", requestType);
			return;
		}

		// Parse into a request class
		Request request;
		try {
			request = gson.fromJson(message.toString(), requestClass);
		} catch(JsonSyntaxException e) {
			error("message could not be parsed into class", requestClass.getName()+":", message);
			return;
		}

		// Execute request
		request.handleRequest(this);
	}

	/**
	 * Disconnect the server and cleanup the thread
	 */
	public void stop() {
		if(websocket != null) {
			try {
				websocket.close();
			} catch(IllegalStateException e) {
				// Ignore, already closed
			}
			websocket = null;
		}
		api.removeClient(this);
		info("disconnected");
	}

	/**
	 * Send a message to this client
	 * @param response Response to send
	 */
	public void message(Response response) {
		message(response.toString());
	}

	/**
	 * Send a message to this client
	 * @param message Message to send
	 */
	public void message(String message) {
		messages.offer(message);
		GoCraftBungee.getInstance().getProxy().getScheduler().runAsync(GoCraftBungee.getInstance(), this::sendMessages);
	}

	/**
	 * Send the commands that are in the queue
	 */
	private void sendMessages() {
		if(websocket == null) {
			warn("tried to send messages of websocket while already closed");
			return;
		}

		// Send messages while the queue is not empty
		String toSend = messages.poll();
		while(toSend != null) {
			websocket.writeTextMessage(toSend);
			toSend = messages.poll();
		}
	}

	/**
	 * Prefix a message with the name of this WebClient
	 * @param message Message to prefix
	 * @return Message prefixed with the name of the client
	 */
	private Object[] addName(Object... message) {
		Object[] newMessage = new Object[message.length+1];
		System.arraycopy(message, 0, newMessage, 1, message.length);
		newMessage[0] = "WebClient["+name+"]:";
		return newMessage;
	}

	/**
	 * Log info message in name of this WebClient
	 * @param message Message to log
	 */
	public void info(Object... message) {
		Log.info(addName(message));
	}

	/**
	 * Log warn message in name of this WebClient
	 * @param message Message to log
	 */
	public void warn(Object... message) {
		Log.warn(addName(message));
	}

	/**
	 * Log error message in name of this WebClient
	 * @param message Message to log
	 */
	public void error(Object... message) {
		Log.error(addName(message));
	}

}
