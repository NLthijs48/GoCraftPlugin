package me.wiefferink.gocraft.tools.sentry;

import com.getsentry.raven.Raven;
import com.getsentry.raven.RavenFactory;
import com.getsentry.raven.log4j2.SentryAppender;
import me.wiefferink.gocraft.GoCraft;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;

// TODO implement shutdown method with cleanup
// TODO try changing System.err logging to level error instead of warn?

public class SentryReporting {

	private Raven raven;
	private String bukkitVersion;
	private BreadcrumbCollector breadcrumbCollector;
	// TODO also use this for breadcrumb filtering?
	private List<String> filterMessages = Arrays.asList(
			"^#!#!", // Spammy, line-by-line exceptions from Skript
			"^Exception in thread \"Craft Scheduler Thread - \\d+\" $" // One line warning message before printing actual exceptions from async threads (yes there is a space at the end of the message)
	);

	public SentryReporting(String dsn) {
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

			// Breadcrumbs
			breadcrumbCollector.addBreadcrumbs(eventBuilder);

			// Server information
			eventBuilder.withTag("API", bukkitVersion);
			eventBuilder.withExtra("Online players", Bukkit.getOnlinePlayers().size());
			eventBuilder.withExtra("Bukkit", Bukkit.getBukkitVersion());
			eventBuilder.withExtra("CraftBukkit", Bukkit.getVersion());
		});

		Logger logger = (Logger)LogManager.getRootLogger();
		SentryAppender appender = new ModifiedSentryAppender(raven);
		appender.setServerName(GoCraft.getInstance().getServerId());
		appender.setRelease(GoCraft.getInstance().getDescription().getVersion()); // Instead of this, add a timestamp into the jar and use that: https://stackoverflow.com/questions/802677/adding-the-current-date-with-maven2-filtering
		appender.addFilter(new LevelFilter(Filter.Result.NEUTRAL, Filter.Result.DENY, Level.ERROR, Level.WARN, Level.FATAL));
		appender.addFilter(new RegexFilter(Filter.Result.DENY, Filter.Result.NEUTRAL, filterMessages));
		appender.start();
		logger.addAppender(appender);

		breadcrumbCollector = new BreadcrumbCollector(logger);
	}

	/**
	 * Get the Raven instance, for example for adding extra BuilderHelpers
	 * @return The used Raven instance
	 */
	public Raven getRaven() {
		return raven;
	}

}
