package me.wiefferink.gocraft.tools.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;
import java.util.List;

public class SpigotConfig extends Config {
	private ConfigurationSection config;

	public SpigotConfig(ConfigurationSection config) {
		this.config = config;
	}

	@Override
	public Config getSection(String path) {
		return new SpigotConfig(config.getConfigurationSection(path));
	}

	@Override
	public Collection<String> getKeys() {
		return config.getKeys(false);
	}

	@Override
	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}

	@Override
	public List<?> getList(String path) {
		return config.getList(path);
	}
}
