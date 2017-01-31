package me.wiefferink.gocraft.tools.sentry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import java.util.HashSet;

public class LevelFilter extends AbstractFilter {

	private HashSet<Integer> allowLevels;

	/**
	 * Constructor
	 * @param onMatch The result to return when the level of an event is in the given levels
	 * @param onMismatch The result to return wehn the level of an event is not in the given levels
	 * @param allow The allowLevels to allow through
	 */
	public LevelFilter(Result onMatch, Result onMismatch, Level... allow) {
		super(onMatch, onMismatch);
		allowLevels = new HashSet<>();
		for(Level level : allow) {
			allowLevels.add(level.intLevel());
		}
	}

	/**
	 * Return result based on the level
	 * @param level The level to check
	 * @return The result
	 */
	private Result filter(Level level) {
		if(allowLevels.contains(level.intLevel())) {
			return onMatch;
		} else {
			return onMismatch;
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
