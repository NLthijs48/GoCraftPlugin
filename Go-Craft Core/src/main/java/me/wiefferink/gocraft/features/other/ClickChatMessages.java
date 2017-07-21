package me.wiefferink.gocraft.features.other;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Iterator;

public class ClickChatMessages extends Feature {
	public ClickChatMessages() {
		listen("clickChatMessages");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		String format = event.getFormat();

		// Check for name and message variables
		int nameIndex = format.indexOf("%1$s");
		int messageIndex = format.indexOf("%2$s");
		if(nameIndex < 0
				|| messageIndex < 0
				|| messageIndex < nameIndex) {
			return;
		}

		// Break down message into parts
		String beforeName = format.substring(0, nameIndex);
		String afterName = format.substring(nameIndex+4, messageIndex);
		String afterMessage = format.substring(messageIndex+4);

		String clickable = beforeName+event.getPlayer().getName()+afterName;
		String after = event.getMessage()+afterMessage;
		// TODO make rank clickable

		Message message = Message.fromKey("chatMessage")
				.replacements(clickable, event.getPlayer().getName(), after);

		// Send message
		Iterator<Player> it = event.getRecipients().iterator();
		while(it.hasNext()) {
			Player target = it.next();
			message.send(target);

			// Remove participant (don't cancel event for DynMap and logging)
			it.remove();
		}
	}
}
