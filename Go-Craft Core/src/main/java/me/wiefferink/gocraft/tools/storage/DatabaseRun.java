package me.wiefferink.gocraft.tools.storage;

import org.hibernate.Session;

public interface DatabaseRun {
	void run(Session session);
}
