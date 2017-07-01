package me.wiefferink.gocraft.api.messages.in;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.wiefferink.gocraft.api.WebClient;

public abstract class Request {

    private static final Gson gson = new Gson();

    public String type;
    public transient JsonObject raw;

    public abstract void handleRequest(WebClient client);

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}
