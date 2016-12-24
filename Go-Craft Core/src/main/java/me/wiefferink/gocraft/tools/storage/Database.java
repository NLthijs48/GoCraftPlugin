package me.wiefferink.gocraft.tools.storage;

import me.wiefferink.gocraft.sessions.BungeeSession;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.sessions.ServerSession;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.NoResultException;
import java.util.UUID;


public class Database {

	private static SessionFactory sessionFactory;
	private static final ThreadLocal<Session> threadSession = new ThreadLocal<>();

	/**
	 * Setup database connection
	 * @param database The database name
	 * @param username The username of the database
	 * @param password The password of the database
	 * @return true if the database is ready for usage, otherwise false
	 */
	public static boolean setup(String database, String username, String password, boolean debug) {
		StandardServiceRegistry registry = null;
		try {
			registry = new StandardServiceRegistryBuilder()
					.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
					.applySetting("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
					.applySetting("hibernate.connection.url", "jdbc:mysql://localhost/"+database) // TODO HikarCP
					.applySetting("hibernate.connection.username", username)
					.applySetting("hibernate.connection.password", password)
					.applySetting("hibernate.hbm2ddl.auto", "update") // Deploy and update schema automatically
					.applySetting("hibernate.jdbc.batch_size", 50) // Group queries into batches if possible
					.applySetting("hibernate.show_sql", debug) // Debug option to show SQL statements in console
					.build();

			sessionFactory = new MetadataSources(registry)
					.addPackage("me.wiefferink.gocraft")
					.addAnnotatedClass(GCPlayer.class)
					.addAnnotatedClass(BungeeSession.class)
					.addAnnotatedClass(ServerSession.class)
					.buildMetadata()
					.buildSessionFactory();
		} catch(Exception e) {
			if(registry != null) {
				StandardServiceRegistryBuilder.destroy(registry);
			}
			// TODO do logging through proper logging classes
			System.out.println("Exception while setting up Hibernate SessionFactory:");
			e.printStackTrace();

		}
		return isReady();
	}

	/**
	 * Check if the tracker has connected correctly
	 * @return true if connected, otherwise false
	 */
	public static boolean isReady() {
		return sessionFactory != null;
	}

	/**
	 * Get a database Session for the current thread and start a transaction
	 * @return The Session
	 */
	public static Session getSession() {
		Session result = threadSession.get();
		if(result == null) {
			result = sessionFactory.openSession();
			result.beginTransaction();
			threadSession.set(result);
		}
		return result;
	}

	/**
	 * Close and commit the session of the current thread
	 */
	public static void closeSession() {
		closeSession(true);
	}

	/**
	 * Close the session of the current thread
	 * @param commit true to commit the current transaction, false to rollback
	 */
	public static void closeSession(boolean commit) {
		Session session = threadSession.get();
		if(session != null && session.isOpen()) {
			Transaction transaction = session.getTransaction();
			if(transaction != null && transaction.isActive()) {
				if(commit) {
					transaction.commit();
				} else {
					transaction.rollback();
				}
			}
			session.clear();
		}
		threadSession.remove();
	}

	/**
	 * Get or create a GCPlayer
	 * @param uuid The uuid of the player to get
	 * @param name The name the player should get when created
	 * @return The created or loaded GCPlayer
	 */
	public static GCPlayer getCreatePlayer(UUID uuid, String name) {
		GCPlayer result = getPlayer(uuid);
		if(result == null) {
			result = new GCPlayer(uuid, name);
			Database.getSession().save(result);
		}
		return result;
	}

	/**
	 * Get GCPlayer for a player if it is defined in the database
	 * @param uuid The UUID of the player to get
	 * @return The GCPlayer
	 */
	public static GCPlayer getPlayer(UUID uuid) {
		GCPlayer result = null;
		try {
			result = Database.getSession()
					.createQuery("FROM GCPlayer WHERE uuid = :uuid", GCPlayer.class)
					.setParameter("uuid", uuid.toString())
					.getSingleResult();
		} catch(NoResultException ignored) {}
		return result;
	}

}
