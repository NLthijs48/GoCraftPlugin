package me.wiefferink.gocraft.tools.sentry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class RegexFilter extends AbstractFilter {

	private Set<Pattern> toFilter;

	/**
	 * Constructor
	 * @param toFilter The regexes to use for filtering
	 */
	public RegexFilter(Result onMatch, Result onMismatch, List<String> toFilter) {
		super(onMatch, onMismatch);
		this.toFilter = new HashSet<>();
		for(String filter : toFilter) {
			this.toFilter.add(Pattern.compile(filter));
		}
	}

	/**
	 * Get a result from the message and defined filters
	 * @param message The message to check
	 * @return onMatch if the message matches one of the regexes, otherwise onMismatch
	 */
	private Result filter(String message) {
		if(message == null) {
			return onMismatch;
		}

		for(Pattern pattern : toFilter) {
			if(pattern.matcher(message).find()) { // This will do contains if there are no start/end specifiers in the regex
				return onMatch;
			}
		}

		return onMismatch;
	}

	public Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
		return filter(ParameterizedMessage.format(message, params));
	}

	public Result filter(Logger logger, Level level, Marker marker, Object message, Throwable throwable) {
		if(message == null) {
			return onMismatch;
		} else {
			return filter(message.toString());
		}
	}

	public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
		if(message == null) {
			return onMismatch;
		} else {
			return filter(message.getFormattedMessage());
		}
	}

	public Result filter(LogEvent logEvent) {
		return filter(logEvent.getMessage().getFormattedMessage());
	}

}
