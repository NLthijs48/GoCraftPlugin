package me.wiefferink.gocraft.tools.sentry;

import com.getsentry.raven.event.Breadcrumb;
import com.getsentry.raven.event.BreadcrumbBuilder;
import com.getsentry.raven.event.EventBuilder;
import me.wiefferink.gocraft.GoCraft;

import java.util.*;

// Unused
public class StacktraceBreadcrumb {

	private static Set<String> ignoreStack = new HashSet<>(Arrays.asList(
			"me.wiefferink.gocraft.tools.sentry.SentryReporting.",        // This class
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
