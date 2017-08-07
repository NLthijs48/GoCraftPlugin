package me.wiefferink.gocraft.tools.config;

import net.md_5.bungee.config.Configuration;

import java.util.Collection;
import java.util.List;

public class BungeeConfig extends Config {
	private Configuration config;

	public BungeeConfig(Configuration config) {
		this.config = config;
	}

	@Override
	public Config getSection(String path) {
		return new BungeeConfig(config.getSection(path));
	}

	@Override
	public Collection<String> getKeys() {
		return config.getKeys();
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
