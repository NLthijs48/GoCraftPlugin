package me.wiefferink.gocraft.features.other;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.processing.Replacement;
import me.wiefferink.interactivemessenger.processing.ReplacementProvider;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClickChatMessages extends Feature {

	private ConfigurationSection ranksSection;
	// Rank priorities, lower is more important
	private Map<String, Integer> rankPriorities;
	private Map<String, Message> rankPrefixes;
	private Map<String, Message> rankChatColors;

	public ClickChatMessages() {
		if(listen("clickChatMessages")) {
			ranksSection = plugin.getGeneralConfig().getConfigurationSection("ranks");
			if(ranksSection == null) {
				Log.warn("No ranks section found in the general config, not applying chat formatting!");
				return;
			}

			// Cache rank priority, prefix and chat color
			int priority = 1;
			rankPriorities = new HashMap<>();
			rankPrefixes = new HashMap<>();
			rankChatColors = new HashMap<>();
			for(String rankKey : ranksSection.getKeys(false)) {
				rankPriorities.put(rankKey.toLowerCase(), priority);
				Message prefix = Message.fromList(Utils.listOrSingle(ranksSection, rankKey+".prefix"));
				List<String> prefixFormat = Utils.listOrSingle(ranksSection, rankKey+".prefixFormat");
				if(!prefixFormat.isEmpty()) {
					prefix = Message.fromList(prefixFormat).replacements(Replacement.name("prefix", prefix));
				}
				rankPrefixes.put(rankKey.toLowerCase(), prefix);

				// Get chat color
				rankChatColors.put(rankKey.toLowerCase(), Message.fromList(Utils.listOrSingle(ranksSection, rankKey+".chatColor")));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if(ranksSection == null) {
			return;
		}

		String[] playerGroups = plugin.getPermissionProvider().getPlayerGroups(null, event.getPlayer());
		Message prefix = Message.empty();
		Message chatColor = Message.empty();
		int currentPriority = Integer.MAX_VALUE;
		for(String playerGroup : playerGroups) {
			int priority = rankPriorities.get(playerGroup.toLowerCase());
			if(priority != 0 && priority < currentPriority) {
				currentPriority = priority;
				prefix = rankPrefixes.computeIfAbsent(playerGroup.toLowerCase(), key -> Message.empty());
				chatColor = rankChatColors.computeIfAbsent(playerGroup.toLowerCase(), key -> Message.empty());
			}
		}

		String playerName = event.getPlayer().getName();

		// Get nickname from essentials
		// todo: create own nickname command, save in GCPlayer
		String nickName = event.getPlayer().getName();
		if(plugin.getEssentialsLink() != null) {
			String essentialsNickName = plugin.getEssentialsLink().get().getUser(event.getPlayer()).getNick(true);
			if(essentialsNickName != null && !essentialsNickName.isEmpty()) {
				nickName = essentialsNickName;
			}
		}

		// Cache named replacements
		ReplacementProvider playerR = Replacement.name("player", playerName);
		ReplacementProvider nicknameR = Replacement.name("nickname", nickName);
		ReplacementProvider prefixR = Replacement.name("prefix", prefix);
		ReplacementProvider messageColorR = Replacement.name("messageColor", chatColor);
		ReplacementProvider messageR = Replacement.name("message", event.getMessage());

		// Send message
		Iterator<Player> it = event.getRecipients().iterator();
		while(it.hasNext()) {
			Player to = it.next();

			// Get factions prefix
			Message factionsPrefix = Message.empty();
			if(plugin.getFactionsLink() != null) {
				factionsPrefix = Message.fromString(plugin.getFactionsLink().getChatPrefix(event.getPlayer(), to));
			}

			// Build chat message
			Message.fromKey("newChatMessage").replacements(
					Replacement.name("factionsPrefix", factionsPrefix),
					playerR,
					nicknameR,
					prefixR,
					messageColorR,
					messageR
			).send(to);

			// Remove participant (don't cancel event for DynMap and logging)
			it.remove();
		}
	}
}
