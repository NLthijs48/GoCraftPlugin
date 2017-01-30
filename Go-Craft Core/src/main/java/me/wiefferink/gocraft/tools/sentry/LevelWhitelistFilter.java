package me.wiefferink.gocraft.tools.sentry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.HashSet;

public class LevelWhitelistFilter extends AbstractFilter {

	private HashSet<Integer> allowLevels;

	/**
	 * Constructor
	 * @param allow The allowLevels to allow through
	 */
	public LevelWhitelistFilter(Level... allow) {
		allowLevels = new HashSet<>();
		for(Level level : allow) {
			allowLevels.add(level.intLevel());
		}
	}

	private Result filter(Level level) {
		if(allowLevels.contains(level.intLevel())) {
			return Result.NEUTRAL;
		} else {
			return Result.DENY;
		}
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
		return filter(level);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Object message, Throwable throwable) {
		return filter(level);
	}

	@Override
	public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
		return filter(level);
	}

	@Override
	public Result filter(LogEvent logEvent) {
		return filter(logEvent.getLevel());
	}

}
