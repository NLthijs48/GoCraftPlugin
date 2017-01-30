package me.wiefferink.gocraft.tools.sentry;

import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.event.BreadcrumbBuilder;
import com.getsentry.raven.event.EventBuilder;
import me.wiefferink.gocraft.GoCraft;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BreadcrumbCollector {

	private final LinkedList<LogEvent> breadcrumbs = new LinkedList<>();
	private static final int MAX_BREADCRUMBS = 25;
	private Pattern tagPrefix = Pattern.compile("^\\[[a-zA-Z0-9-_]+\\] ");

	public BreadcrumbCollector(Logger logger) {
		Appender breadcrumbAppender = new AbstractAppender("Breadcrumb Builder", null, null, false) {
			@Override
			public void append(LogEvent logEvent) {
				synchronized(breadcrumbs) {
					breadcrumbs.add(logEvent);
					if(breadcrumbs.size() > MAX_BREADCRUMBS) {
						breadcrumbs.removeFirst();
					}
				}
			}
		};
		breadcrumbAppender.start();
		logger.addAppender(breadcrumbAppender);
	}

	public void addBreadcrumbs(EventBuilder eventBuilder) {
		List<Breadcrumb> result = new ArrayList<>();
		synchronized(breadcrumbs) {
			// TODO ignore last entry, same as actual message?
			for(LogEvent logEvent : breadcrumbs) {
				BreadcrumbBuilder builder = new BreadcrumbBuilder();

				builder.setTimestamp(new Date(logEvent.getMillis()));
				builder.setCategory(getBreadcrumbCategory(logEvent));
				builder.setLevel(getBreadcrumbLevel(logEvent));
				builder.setType(getBreadcrumbType(logEvent));

				String message = getBreadcrumbMessage(logEvent);
				// Match tags in the front of the message and set that as category instead
				Matcher matcher = tagPrefix.matcher(message);
				if(matcher.find()) {
					message = message.substring(matcher.group().length());
					builder.setCategory(matcher.group().substring(1, matcher.group().length()-2));
				}
				builder.setMessage(message);

				// Can put more in builder.setData()

				result.add(builder.build());
			}
		}
		eventBuilder.withBreadcrumbs(result);
		GoCraft.debug("add breadcrumbs:", result);
	}

	/**
	 * Build a message from a LogRecord
	 * @param logEvent The LogRecord to parse
	 * @return The message including stacktrace if there is ony
	 */
	private String getBreadcrumbMessage(LogEvent logEvent) {
		return logEvent.getMessage().getFormattedMessage();
	}

	/**
	 * Get a breadcrumb type based on a LogRecord
	 * @param logEvent The record to calculate a type for
	 * @return The type of the record
	 */
	private String getBreadcrumbType(LogEvent logEvent) {
		String message = logEvent.getMessage().getFormattedMessage();
		if(message != null && !message.isEmpty()) {
			if(message.contains("issued server command: ")) {
				return "user";
			} else if(message.contains("lost connection: ")) {
				return "navigation";
			} else if(message.contains("logged in with entity id")) {
				return "navigation";
			}
			// Use 'navigation'?
		}
		return "default";
	}

	/**
	 * Get a breadcrumb level based on a LogRecord
	 * @param logEvent The record to calculate a level for
	 * @return The level of the record
	 */
	private String getBreadcrumbLevel(LogEvent logEvent) {
		if(logEvent.getLevel().equals(Level.WARN)) {
			return "warning";
		} else if(logEvent.getLevel().isAtLeastAsSpecificAs(Level.ERROR)) {
			return "error";
		} else {
			return "info";
		}
	}

	/**
	 * Get a breadcrumb category based on a LogRecord
	 * @param record The record to calculate a category for
	 * @return The category of the record
	 */
	private String getBreadcrumbCategory(LogEvent record) {
		return "log";
	}

}
