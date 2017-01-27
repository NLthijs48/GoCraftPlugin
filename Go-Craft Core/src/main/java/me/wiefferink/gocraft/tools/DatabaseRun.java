package me.wiefferink.gocraft.tools;

import org.hibernate.Session;

public interface DatabaseRun {
	void run(Session session);
}
