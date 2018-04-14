package me.wiefferink.gocraft.api.messages.in;

import me.wiefferink.gocraft.api.WebClient;
import me.wiefferink.gocraft.api.messages.out.VoteTopResponse;

public class VoteTopRequest extends Request {

    public int year;
    public int month;
    public int start;
    public int items;

    @Override
    public void handleRequest(WebClient client) {
        // Prevent DOS
        start = Math.min(start, 100);
        items = Math.min(items, 10);

        client.message(new VoteTopResponse(year, month, start, items));
    }

}
