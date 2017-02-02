package me.wiefferink.gocraft.tools.sentry;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Unused
public class StackRepresentation {

	// package+class+method prefixes to ignore
	// Dots will be seen as anything, but that does not really matter
	private final static Set<String> ignoreStack = new HashSet<>(Arrays.asList(
			"^me.wiefferink.gocraft.tools.sentry", // Sentry collection classes
			"^java.util.logging.Logger", // Logging getting to this class
			"^com.getsentry.raven", // Raven building the event
			"^java.util.logging.Logger", // Java logging
			"^org.bukkit.plugin.PluginLogger", // Bukkit logging
			"^org.apache.logging.log4j", // Log4j
			"^org.bukkit.craftbukkit.[0-9a-zA-Z_]+.util.ForwardLogHandler", // Log forwarder of Spigot
			"^java.lang.Thread.getStackTrace" // Getting stacktrace
	));
	private final static Set<Pattern> ignorePatterns = new HashSet<>();
	static {
		for(String igoreRegex : ignoreStack) {
			ignorePatterns.add(Pattern.compile(igoreRegex));
		}
	}

	/**
	 * Get a string showing the current stack
	 * @return A string representing the current stack
	 */
	public static String getStackString() {
		List<String> result = new ArrayList<>();

		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		// Filter StackTraceElements
		// After finding the first not-ignored frame we don't want to ignore anything anymore
		boolean foundCorrect = false;
		List<StackTraceElement> filteredElements = new ArrayList<>();
		for(StackTraceElement element : elements) {
			// Check the ignore list
			String prefix = element.getClassName()+"."+element.getMethodName();
			if(!foundCorrect) {
				boolean ignore = false;
				for(Pattern ignorePattern : ignorePatterns) {
					Matcher matcher = ignorePattern.matcher(prefix);
					if(matcher.find()) {
						ignore = true;
						break;
					}
				}
				if(ignore) {
					continue;
				}
			}
			foundCorrect = true;
			filteredElements.add(element);
		}

		// Build breadcrumb data
		for(int i = filteredElements.size()-1; i >= 0; i--) {
			StackTraceElement element = filteredElements.get(i);
			result.add(
					element.getClassName()+"."+element.getMethodName()+"("+element.getFileName()+":"+element.getLineNumber()+")"
			);
		}
		return StringUtils.join(result, "\n");
	}
}
