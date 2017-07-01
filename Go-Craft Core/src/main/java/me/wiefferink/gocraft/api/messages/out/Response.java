package me.wiefferink.gocraft.api.messages.out;

import com.google.gson.Gson;

public abstract class Response {

    private static final Gson gson = new Gson();

    public String type;

    public Response(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }
}

