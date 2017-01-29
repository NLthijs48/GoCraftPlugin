package me.wiefferink.gocraft.tools;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.event.BreadcrumbBuilder;
import com.getsentry.raven.event.EventBuilder;
import com.getsentry.raven.jul.SentryHandler;
import me.wiefferink.gocraft.GoCraft;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SentryReporting {

	private Raven raven;
	private final LinkedList<LogRecord> breadcrumbs = new LinkedList<>();
	private static final int MAX_BREADCRUMBS = 25;
	private String bukkitVersion;
	private SimpleFormatter formatter;
	private Pattern tagPrefix = Pattern.compile("^\\[[a-zA-Z0-9-_]+\\] ");

	public SentryReporting(String dsn) {
		formatter = new SimpleFormatter();


		// Clean Bukkit version
		bukkitVersion = Bukkit.getBukkitVersion();
		if(bukkitVersion.endsWith("-SNAPSHOT"))	{
			bukkitVersion = bukkitVersion.substring(0, bukkitVersion.lastIndexOf("-SNAPSHOT"));
		}

		// Setup connection to Sentry.io
		raven = RavenFactory.ravenInstance(dsn);

		// Add function to enhance events
		raven.addBuilderHelper(eventBuilder -> {
			// Plugin information
			eventBuilder.withServerName(GoCraft.getInstance().getServerId());
			eventBuilder.withRelease(GoCraft.getInstance().getDescription().getVersion());

			// Stacktrace
			buildBreadcrumb(eventBuilder);

			// Server information
			eventBuilder.withExtra("Online players", Bukkit.getOnlinePlayers().size());
			eventBuilder.withTag("API", bukkitVersion);
			eventBuilder.withExtra("Bukkit", Bukkit.getBukkitVersion());
			eventBuilder.withExtra("CraftBukkit", Bukkit.getVersion());
		});


		// Setup hook into logging of this plugin
		SentryHandler handler = new SentryHandler(raven);
		try {
			handler.setEncoding("UTF-8");
		} catch(UnsupportedEncodingException ignore) {}

		handler.setServerName(GoCraft.getInstance().getServerId());
		handler.setRelease(GoCraft.getInstance().getDescription().getVersion()); // Instead of this, add a timestamp into the jar and use that: https://stackoverflow.com/questions/802677/adding-the-current-date-with-maven2-filtering
		handler.setLevel(Level.WARNING); // Only log warnings and errors
		handler.setFilter(record -> {
			String message = record.getMessage();
			// Discard Skipt startup errors (one line for each error part...)
			return !(message != null && message.trim().startsWith("#!#!"));
		});

		// Listen to all loggers (Bukkit itself and all plugins)
		Logger.getLogger("").addHandler(handler);

		// Add breadcrumb handler
		Logger.getLogger("").addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				synchronized(breadcrumbs) {
					breadcrumbs.add(record);
					if(breadcrumbs.size() > MAX_BREADCRUMBS) {
						breadcrumbs.removeFirst();
					}
				}
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() throws SecurityException {
			}
		});


		// apache log4j logging
		/*
		<artifactId>raven-log4j2</artifactIdId>

		org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger)org.apache.logging.log4j.LogManager.getRootLogger();
		SentryAppender appender = new SentryAppender(raven);
		appender.setServerName(GoCraft.getInstance().getServerId());
		appender.setRelease(GoCraft.getInstance().getDescription().getVersion()); // Instead of this, add a timestamp into the jar and use that: https://stackoverflow.com/questions/802677/adding-the-current-date-with-maven2-filtering
		//appender.setLevel(Level.WARNING); // Only log warnings and errors
		logger.addAppender(appender);
		*/
	}

	/**
	 * Get the Raven instance, for example for adding extra BuilderHelpers
	 * @return The used Raven instance
	 */
	public Raven getRaven() {
		return raven;
	}


	private void buildBreadcrumb(EventBuilder eventBuilder) {
		List<Breadcrumb> result = new ArrayList<>();
		synchronized(breadcrumbs) {
			for(LogRecord record : breadcrumbs) {
				BreadcrumbBuilder builder = new BreadcrumbBuilder();

				builder.setTimestamp(new Date(record.getMillis()));
				builder.setCategory(getBreadcrumbCategory(record));
				builder.setLevel(getBreadcrumbLevel(record));
				builder.setType(getBreadcrumbType(record));

				String message = getBreadcrumbMessage(record).trim();
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
	}

	/**
	 * Build a message from a LogRecord
	 * @param record The LogRecord to parse
	 * @return The message including stacktrace if there is ony
	 */
	private String getBreadcrumbMessage(LogRecord record) {
		String result = "";
		if(record.getMessage() != null) {
			result += formatter.formatMessage(record);
		}
		if(record.getThrown() != null) {
			result += ExceptionUtils.getStackTrace(record.getThrown());
		}
		return result;
	}

	/**
	 * Get a breadcrumb type based on a LogRecord
	 * @param record The record to calculate a type for
	 * @return The type of the record
	 */
	private String getBreadcrumbType(LogRecord record) {
		String message = record.getMessage();
		if(message != null && !message.isEmpty()) {
			if(message.contains("issued server command: ")) {
				return "user";
			} else if(message.contains("lost connection: ")) {
				return "http";
			} else if(message.contains("logged in with entity id")) {
				return "http";
			}
			// Use 'navigation'?
		}
		return "default";
	}

	/**
	 * Get a breadcrumb level based on a LogRecord
	 * @param record The record to calculate a level for
	 * @return The level of the record
	 */
	private String getBreadcrumbLevel(LogRecord record) {
		if(record.getLevel() == Level.WARNING) {
			return "warning";
		} else if(record.getLevel() == Level.SEVERE) {
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
	private String getBreadcrumbCategory(LogRecord record) {
		return "log";
	}









	private static Set<String> ignoreStack = new HashSet<>(Arrays.asList(
			"me.wiefferink.gocraft.tools.SentryReporting.",        // This class
			"java.lang.Thread.getStackTrace",                        // Getting the stacktrace
			"java.util.logging.Logger.",                            // Logging getting to this class
			"com.getsentry.raven.",                                    // Raven building the event
			"java.util.logging.Logger",                                // Java logging
			"org.bukkit.plugin.PluginLogger."                        // Bukkit logging
	));
	private static String basePackage = GoCraft.getInstance().getDescription().getMain().substring(0, GoCraft.getInstance().getDescription().getMain().lastIndexOf("."));

	// Mapping of package prefixes to category types
	private static Map<String, String> categories = new HashMap<String, String>() {{
		put("org.bukkit", "Bukkit");
		put("org.spigotmc", "Spigot");
		put("java", "Java");
		put("net.minecraft", "Minecraft");
		put("com.getsentry", "Sentry");
		put(basePackage, GoCraft.getInstance().getName());
	}};

	// Mapping of package prefixes to breadcrumb levels (possible: navigation, http, user, warning, debug, critical, empty, last)
	private static Map<String, String> levels = new HashMap<String, String>() {{
		put(basePackage, "warning");
		// TODO user other types?
	}};


	/**
	 * Build a nicely formatted breadcrumb using the StackTraceElements
	 * @param eventBuilder The stack to use (prevents more functions getting added)
	 */
	private void buildStackTraceBreadcrumb(EventBuilder eventBuilder) {
		// Build breadcrumbs with current stack
		List<Breadcrumb> result = new ArrayList<>();
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
				for(String ignoreItem : ignoreStack) {
					if(prefix.startsWith(ignoreItem)) {
						ignore = true;
						break;
					}
				}
				if(ignore) {
					continue;
				}

				// Set first not-ignored element as culprit
				//eventBuilder.withCulprit(element); // TODO check if we can do something better
			}
			foundCorrect = true;
			filteredElements.add(element);
		}

		// Build breadcrumb data
		for(int i = filteredElements.size()-1; i >= 0; i--) {
			StackTraceElement element = filteredElements.get(i);
			String prefix = element.getClassName()+"."+element.getMethodName();

			// Setup category and level
			String category = "Other";
			for(String categoryCandidate : categories.keySet()) {
				if(prefix.startsWith(categoryCandidate)) {
					category = categories.get(categoryCandidate);
				}
			}

			// Setup level
			String level = "nothing";
			for(String levelCandidate : levels.keySet()) {
				if(prefix.startsWith(levelCandidate)) {
					level = levels.get(levelCandidate);
				}
			}

			// Possible levels:
			result.add(new BreadcrumbBuilder()
					.setMessage(element.getClassName()+"."+element.getMethodName()+"("+element.getFileName()+":"+element.getLineNumber()+")")
					.setLevel(level)
					.setCategory(category)
					.setTimestamp(new Date())
					.setType("type:thing") // TODO check how this is used?
					.build()
			);
		}

		eventBuilder.withBreadcrumbs(result);
	}
}
