package me.wiefferink.gocraft.tools.storage;

import org.hibernate.Session;

public interface DatabaseGet<T> {
	T run(Session session);
}
