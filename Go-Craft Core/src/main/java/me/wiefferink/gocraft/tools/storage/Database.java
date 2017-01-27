package me.wiefferink.gocraft.tools.storage;

import me.wiefferink.gocraft.features.players.timedfly.TimedFly;
import me.wiefferink.gocraft.sessions.BungeeSession;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.sessions.ServerSession;
import me.wiefferink.gocraft.tools.DatabaseRun;
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
	private static StandardServiceRegistry registry;

	/**
	 * Setup database connection
	 * @param database The database name
	 * @param username The username of the database
	 * @param password The password of the database
	 * @return true if the database is ready for usage, otherwise false
	 */
	public static boolean setup(String database, String username, String password, boolean debug) {
		try {
			registry = new StandardServiceRegistryBuilder()
					// Connection
					.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQLDialect")
					//.applySetting("hibernate.connection.driver_class", "com.mysql.jdbc.Driver")
					.applySetting("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
					.applySetting("hibernate.connection.url", "jdbc:mysql://localhost/"+database)
					.applySetting("hibernate.connection.username", username)
					.applySetting("hibernate.connection.password", password)
					// Settings
					.applySetting("hibernate.hbm2ddl.auto", "update") // Deploy and update schema automatically
					.applySetting("hibernate.jdbc.batch_size", 50) // Group queries into batches if possible
					.applySetting("hibernate.show_sql", debug) // Debug option to show SQL statements in console
					// Caching
					.applySetting("hibernate.hikari.dataSource.cachePrepStmts", "true")
					.applySetting("hibernate.hikari.dataSource.prepStmtCacheSize", "250")
					.applySetting("hibernate.hikari.dataSource.prepStmtCacheSqlLimit", "2048")
					.applySetting("hibernate.hikari.autoCommit", "false")

					.build();

			sessionFactory = new MetadataSources(registry)
					.addPackage("me.wiefferink.gocraft")
					.addAnnotatedClass(GCPlayer.class)
					.addAnnotatedClass(BungeeSession.class)
					.addAnnotatedClass(ServerSession.class)
					.addAnnotatedClass(TimedFly.class)
					.buildMetadata()
					.buildSessionFactory();
		} catch(Exception e) {
			shutdown();
			// TODO do logging through proper logging classes
			System.out.println("Exception while setting up Hibernate SessionFactory:");
			e.printStackTrace();
		}
		return isReady();
	}

	/**
	 * Shutdown the database
	 */
	public static void shutdown() {
		if(sessionFactory != null) {
			try {
				sessionFactory.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		if(registry != null) {
			try {
				StandardServiceRegistryBuilder.destroy(registry);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Check if the tracker has connected correctly
	 * @return true if connected, otherwise false
	 */
	public static boolean isReady() {
		return sessionFactory != null;
	}

	/**
	 * Check if this thread already has a database session open
	 * @return true if there is an open session, otherwise false
	 */
	public static boolean hasSession() {
		return threadSession.get() != null;
	}

	/**
	 * Get a database Session for the current thread and start a transaction
	 * @return The Session
	 */
	private static Session getSession() {
		Session result = threadSession.get();
		if(result == null) {
			result = sessionFactory.openSession();
			result.beginTransaction();
			threadSession.set(result);
		}
		return result;
	}

	/**
	 * Close the session of the current thread
	 * @param commit true to commit the current transaction, false to rollback
	 */
	private static void closeSession(boolean commit) {
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
	 * Run code in a database session
	 * @param runnable The code to run in a database session
	 */
	public static void run(DatabaseRun runnable) {
		boolean hadSession = hasSession();
		try {
			runnable.run(Database.getSession());
			if(!hadSession) {
				Database.closeSession(true);
			}
		} catch(Exception e) {
			if(!hadSession) {
				Database.closeSession(false);
			}
			throw e; // Possibly cause session above us to fail and print to console
		}
	}

	/**
	 * Get or create a GCPlayer
	 * @param uuid The uuid of the player to get
	 * @param name The name the player should get when created
	 * @return The created or loaded GCPlayer
	 */
	public static GCPlayer getCreatePlayer(UUID uuid, String name) {
		if(!hasSession()) {
			return null;
		}
		GCPlayer result = getPlayer(uuid);
		if(result == null) {
			result = new GCPlayer(uuid, name);
			Database.getSession().saveOrUpdate(result);
		}
		return result;
	}

	/**
	 * Get GCPlayer for a player if it is defined in the database
	 * @param uuid The UUID of the player to get
	 * @return The GCPlayer
	 */
	public static GCPlayer getPlayer(UUID uuid) {
		if(!hasSession()) {
			return null;
		}
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
