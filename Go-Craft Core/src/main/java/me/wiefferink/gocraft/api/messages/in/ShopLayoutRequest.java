package me.wiefferink.gocraft.api.messages.in;

import me.wiefferink.gocraft.api.WebClient;
import me.wiefferink.gocraft.api.messages.out.ShopLayoutResponse;

public class ShopLayoutRequest extends Request {

    @Override
    public void handleRequest(WebClient client) {
        client.message(new ShopLayoutResponse());
    }

}
