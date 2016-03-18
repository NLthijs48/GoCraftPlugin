package me.wiefferink.gocraft.tools.storage;

public interface Cleaner {
	/**
	 * Clean the given config
	 *
	 * @param config The config to clean
	 * @return true if something has been cleaned, otherwise false
	 */
	boolean clean(UTF8Config config);
}
