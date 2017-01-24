package me.wiefferink.gocraft.information;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InformationRequest extends Feature {

	private Player about;
	private OfflinePlayer aboutOffline;
	private CommandSender to;
	private List<Message> messages;
	private final InformationRequest self = this;

	public InformationRequest(Player about, CommandSender to) {
		this.about = about;
		this.aboutOffline = Bukkit.getOfflinePlayer(about.getUniqueId());
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
		providers.removeIf((InformationProvider provider) -> !hasAccess(provider));
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
				// Only the header, tell no information available
				if(messages.size() == 1) {
					plugin.message(to, "information-noneAvailable", about.getName());
					return;
				}

				// Send messages
				for(Message message : messages) {
					message.send(to);
				}
			});
		});
	}

	/**
	 * Check if the player has access to the content of an information provider
	 * @param provider The provider to check
	 * @return true if the player has access, otherwise false
	 */
	private boolean hasAccess(InformationProvider provider) {
		String access = plugin.getStringSetting("informationProviders."+provider.getClass().getSimpleName());

		// Staff access level
		if("staff".equals(access)) {
			return to.hasPermission("gocraft.staff");
		}
		// Admin access level
		else if("admin".equals(access)) {
			return to.hasPermission("gocraft.admin");
		}
		// Allow by default, false disables it
		else {
			return !"false".equalsIgnoreCase(access);
		}
	}

	/**
	 * Get the Player that the information should be about
	 * @return The Player
	 */
	public Player getAbout() {
		return about;
	}

	/**
	 * Get the OfflinePlayer that the information should be about
	 * @return The OfflinePlayer
	 */
	public OfflinePlayer getAboutOffline() {
		return aboutOffline;
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
