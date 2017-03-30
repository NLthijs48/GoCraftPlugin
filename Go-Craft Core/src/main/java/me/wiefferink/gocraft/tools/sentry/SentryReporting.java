package me.wiefferink.gocraft.tools.sentry;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.log4j2.SentryAppender;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

// TODO implement shutdown method with cleanup
// TODO try changing System.err logging to level error instead of warn?
/*
# TODO Make different filters, based on level, message, thread name, etc
# TODO How to set servername and version?
Config:

	# Ignore certain warnings and errors and prevent them getting reported
	# You can use regex
	filter:
		- ""
		- ""

	# Change level options?

	# Change fingerprint options (setup different grouping, for example group together messages that include variables)
	# https://docs.sentry.io/learn/rollups/

	breadcrumbs:
		enabled: true

		# Filter messages from getting into the breadcrumb
		# You can use regex
		filter:
			- ""
			- ""
		# Add reportFilters to breadcrumbFilters
		filtersIncludesReportFilters: true

		# Maximum number of items that can appear in the breadcrumb
		# Sentry does not advice to use more than 100, and reports exceeding limits might get dropped
		maximumEntries: 50

		# The default type for breadcrumb entries (determines the icon on sentry.io)
		# Current useful values: default, debug, user, location, http (hides message), navigation
		defaultType: "default"

		# Change the type of breadcrumb entries (see 'defaultType')
		type:
			<regex>: <type>

		# Set the default category, shown before each breadcrumb line on sentry.io (a space makes it empty)
		defaultCategory: " "

		# Change the category of breadcrumb entries (see 'defaultCategory')
		category:
			<regex>: <category>


*/
public class SentryReporting {

	private Raven raven;
	private String bukkitVersion;
	private BreadcrumbCollector breadcrumbCollector;
	private static final String USER_NAME = "[a-zA-Z0-9_]{1,16}";
	// TODO also use this for breadcrumb filtering?
	private List<String> filterMessages = Arrays.asList(
			"^#!#!", // Spammy, line-by-line exceptions from Skript
			"^Exception in thread \"Craft Scheduler Thread - \\d+\" $", // One line warning message before printing actual exceptions from async threads (yes there is a space at the end of the message)
			"^"+USER_NAME+" moved too quickly!", // Probably caused by lag
			"^"+USER_NAME+" moved wrongly!", // Probably caused by lag
			"^handleDisconnection\\(\\) called twice$" // Annoying Bukkit bug that is supposed to be fixed but actually is not
	);
	private List<String> filterExceptions = Arrays.asList(
			Pattern.quote("java.lang.NoSuchMethodError: org.bukkit.Server.getOnlinePlayers()[Lorg/bukkit/entity/Player")
	);

	public SentryReporting(String dsn) {
		try {
			if (dsn == null || dsn.isEmpty()) {
				Log.warn("Not enabling Sentry reporter, no dsn provided");
				return;
			}

			// Clean Bukkit version
			bukkitVersion = Bukkit.getBukkitVersion();
			if (bukkitVersion.endsWith("-SNAPSHOT")) {
				bukkitVersion = bukkitVersion.substring(0, bukkitVersion.lastIndexOf("-SNAPSHOT"));
			}

			// Setup connection to Sentry.io
			raven = RavenFactory.ravenInstance(dsn);

			// Add function to enhance events
			raven.addBuilderHelper(eventBuilder -> {
				// Plugin information
				eventBuilder.withServerName(GoCraft.getInstance().getServerId());
				eventBuilder.withRelease(GoCraft.getInstance().getDescription().getVersion());

				// Breadcrumbs
				breadcrumbCollector.addBreadcrumbs(eventBuilder);

				// Server information
				eventBuilder.withTag("API", bukkitVersion);
				eventBuilder.withExtra("Online players", Bukkit.getOnlinePlayers().size());
				eventBuilder.withExtra("Bukkit", Bukkit.getBukkitVersion());
				eventBuilder.withExtra("CraftBukkit", Bukkit.getVersion());

				eventBuilder.withExtra("Stack", StackRepresentation.getStackString());
			});

			Logger logger = (Logger) LogManager.getRootLogger();
			SentryAppender appender = new ModifiedSentryAppender(raven);
			appender.setServerName(GoCraft.getInstance().getServerId());
			appender.setRelease(GoCraft.getInstance().getDescription().getVersion()); // Instead of this, add a timestamp into the jar and use that: https://stackoverflow.com/questions/802677/adding-the-current-date-with-maven2-filtering
			appender.addFilter(new LevelFilter(Filter.Result.NEUTRAL, Filter.Result.DENY, Level.ERROR, Level.WARN, Level.FATAL));
			appender.addFilter(new MessageFilter(Filter.Result.DENY, Filter.Result.NEUTRAL, filterMessages));
			appender.addFilter(new MessageFilter(Filter.Result.DENY, Filter.Result.NEUTRAL, filterExceptions));
			appender.start();
			logger.addAppender(appender);

			breadcrumbCollector = new BreadcrumbCollector(logger);
		} catch (Exception e) {
			Log.warn("Setup of Sentry reporting failed:", ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Get the Raven instance, for example for adding extra BuilderHelpers
	 * @return The used Raven instance
	 */
	public Raven getRaven() {
		return raven;
	}

}
