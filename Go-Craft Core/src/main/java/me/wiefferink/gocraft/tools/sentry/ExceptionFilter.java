package me.wiefferink.gocraft.tools.sentry;

import org.apache.commons.lang.exception.ExceptionUtils;
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

public class ExceptionFilter extends AbstractFilter {

	private Set<Pattern> toFilter;

	/**
	 * Constructor
	 * @param toFilter The regexes to use for filtering
	 */
	public ExceptionFilter(Result onMatch, Result onMismatch, List<String> toFilter) {
		super(onMatch, onMismatch);
		this.toFilter = new HashSet<>();
		for(String filter : toFilter) {
			this.toFilter.add(Pattern.compile(filter));
		}
	}

	/**
	 * Get a result from the message and defined filters
	 * @param exception The message to check
	 * @return onMatch if the message matches one of the regexes, otherwise onMismatch
	 */
	private Result filter(String exception) {
		if(exception == null) {
			return onMismatch;
		}

		for(Pattern pattern : toFilter) {
			if(pattern.matcher(exception).find()) { // This will do contains if there are no start/end specifiers in the regex
				return onMatch;
			}
		}

		return onMismatch;
	}

	public Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
		// Params could contain exception, so just pass complete formatted message
		return filter(ParameterizedMessage.format(message, params));
	}

	public Result filter(Logger logger, Level level, Marker marker, Object message, Throwable throwable) {
		if(throwable == null) {
			return onMismatch;
		} else {
			return filter(ExceptionUtils.getStackTrace(throwable));
		}
	}

	public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable) {
		if(throwable == null) {
			return onMismatch;
		} else {
			return filter(ExceptionUtils.getStackTrace(throwable));
		}
	}

	public Result filter(LogEvent logEvent) {
		if(logEvent.getThrown() == null) {
			return onMismatch;
		} else {
			return filter(ExceptionUtils.getStackTrace(logEvent.getThrown()));
		}
	}

}
