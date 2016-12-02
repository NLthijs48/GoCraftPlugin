package me.wiefferink.gocraft.sessions;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;


public class SessionConnector {

	private SessionFactory sessionFactory;

	public SessionConnector(String database, String username, String password) {
		StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
				.applySetting("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
				.applySetting("hibernate.connection.url", "jdbc:mysql://localhost/"+database)
				.applySetting("hibernate.connection.username", username)
				.applySetting("hibernate.connection.password", password)
				.applySetting("hibernate.hbm2ddl.auto", "update") // Deploy and update schema automatically
				//.applySetting("hibernate.show_sql", true) // Debug option to show SQL statements in console
				.build();

		try {
			sessionFactory = new MetadataSources(registry)
					.addPackage("me.wiefferink.gocraft")
					.addAnnotatedClass(BungeeSession.class)
					.addAnnotatedClass(ServerSession.class)
					.buildMetadata()
					.buildSessionFactory();
		} catch(Exception e) {
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	/**
	 * Check if the tracker has connected correctly
	 * @return true if connected, otherwise false
	 */
	public boolean isReady() {
		return sessionFactory != null;
	}

	/**
	 * Open a session to the tracker database
	 * @return The opened session
	 */
	public Session session() {
		return sessionFactory.openSession();
	}

}
