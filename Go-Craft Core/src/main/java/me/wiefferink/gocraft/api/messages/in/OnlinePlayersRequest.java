package me.wiefferink.gocraft.api.messages.in;

import me.wiefferink.gocraft.api.WebClient;
import me.wiefferink.gocraft.api.messages.out.OnlinePlayersResponse;

public class OnlinePlayersRequest extends Request {

    @Override
    public void handleRequest(WebClient client) {
        client.message(new OnlinePlayersResponse());
    }

}
