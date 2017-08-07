package me.wiefferink.gocraft.tools.config;

import java.util.Collection;
import java.util.List;

/**
 * Abstract config file that can be implemented for specific platforms (Spigot and BungeeCord currently)
 */
public abstract class Config {
	public abstract Config getSection(String path);
	public abstract Collection<String> getKeys();

	public abstract List<String> getStringList(String path);
	public abstract List<?> getList(String path);
}
