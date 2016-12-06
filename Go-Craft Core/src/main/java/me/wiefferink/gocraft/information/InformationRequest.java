package me.wiefferink.gocraft.information;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.messages.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InformationRequest extends Feature {

	private Player about;
	private CommandSender to;
	private List<Message> messages;
	private final InformationRequest self = this;

	public InformationRequest(Player about, CommandSender to) {
		this.about = about;
		this.to = to;
		this.messages = new ArrayList<>();
	}

	/**
	 * Execute the information request
	 */
	public void execute() {
		// Header
		message(Message.fromKey("information-header").replacements(about.getName()).prefix());

		// Add sync messages
		List<InformationProvider> providers = plugin.getInformationManager().getInformationProviders();
		for(InformationProvider provider : providers) {
			provider.showSync(this);
		}

		// Add async messages
		async(() -> {

			// Get async messages
			for(InformationProvider provider : providers) {
				provider.showAsync(self);
			}

			// Send the messages in sync
			sync(() -> {
				for(Message message : messages) {
					message.send(to);
				}
			});
		});
	}

	/**
	 * Get the Player that the information should be about
	 * @return The Player
	 */
	public Player getAbout() {
		return about;
	}

	/**
	 * Get the CommandSender the information should be send to
	 * @return The CommandSender
	 */
	public CommandSender getTo() {
		return to;
	}

	/**
	 * Add result a message to the request
	 * @param message The message to add
	 */
	public void message(Message message) {
		messages.add(message);
	}

}
