package me.wiefferink.gocraft.tools.storage;

import com.sun.rowset.CachedRowSetImpl;
import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.*;

@SuppressWarnings("restriction")
public abstract class Database {
	private JavaPlugin plugin;
	private Connection connection = null;
	private String className = null;
	private String jdbcURL = null;
	private String userName = null;
	private String password = null;

	/**
	 * Construct a database instance.
	 * @param className The database driver classname
	 * @param jdbcURL A JDBC url to use for connecting.
	 * @param userName Username to connect with.
	 * @param password Password to authenticate username.
	 * @param plugin A plugin instance for the schedulers to be assigned to.
	 */
	public Database(String className, String jdbcURL, String userName, String password, JavaPlugin plugin) {
		this.className = className;
		this.userName = userName;
		this.password = password;
		this.jdbcURL = jdbcURL;
		this.plugin = plugin;
		connect();
	}

	/**
	 * Connect to the database
	 */
	public void connect() {
		Properties info = new Properties();
		info.setProperty("user", userName);
		info.setProperty("password", password);

		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			GoCraft.warn("Could not find the jdbc driver: "+className);
			e.printStackTrace();
			return;
		}
		try {
			connection = DriverManager.getConnection(jdbcURL, info);
		} catch (SQLException e) {
			GoCraft.warn("Could not connect to the database: "+jdbcURL);
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Disconnects from the database
	 */
	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException ignored) {
			}
		}
	}

	/**
	 * Query the database and return a cached result.
	 * @param preparedStatement The statement to be queried.
	 * @return Cached rowset returned from query.
	 */
	public CachedRowSet query(final PreparedStatement preparedStatement) {
		CachedRowSet rowSet = null;
		if (isConnected()) {
			try {
				ExecutorService exe = Executors.newCachedThreadPool();
				Future<CachedRowSet> future = exe.submit(new Callable<CachedRowSet>() {
					public CachedRowSet call() {
						try {
							ResultSet resultSet = preparedStatement.executeQuery();

							CachedRowSet cachedRowSet = new CachedRowSetImpl();
							cachedRowSet.populate(resultSet);
							resultSet.close();
							preparedStatement.getConnection().close();

							if (cachedRowSet.next()) {
								return cachedRowSet;
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
						return null;
					}
				});
				if (future.get() != null) {
					rowSet = future.get();
				}
			} catch(InterruptedException|ExecutionException e) {
				e.printStackTrace();
			}
		}
		return rowSet;
	}

	public CachedRowSet query(String query, Object... vars) {
		return query(prepareStatement(query, vars));
	}

	/**
	 * Execute a query
	 * @param preparedStatement query to be executed.
	 */
	public void execute(final PreparedStatement preparedStatement) {
		if (isConnected()) {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				@Override
				public void run() {
					//GoCraft.debug("Running statement: " + preparedStatement.toString());
					try {
						preparedStatement.execute();
						preparedStatement.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public void execute(String query, Object... vars) {
		execute(prepareStatement(query, vars));
	}

	/**
	 * Prepare a statement
	 * @param query Query to be prepared.
	 * @param vars Variables to be replaced from ?.
	 * @return a prepared statement.
	 */
	public PreparedStatement prepareStatement(String query, Object... vars) {
		try {
			PreparedStatement preparedStatement = getConnection().prepareStatement(query);
			int x = 0;
			if (query.contains("?") && vars.length != 0) {
				for (Object var : vars) {
					x++;
					preparedStatement.setString(x, var.toString());
				}
			}
			return preparedStatement;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a connection from the data pool
	 * @return a connection.
	 */
	public Connection getConnection() {
		if (connection == null) {
			connect();
		}
		return connection;
	}

	/**
	 * Check if the data pool is connected.
	 * @return connected Whether the data pool is connected or not.
	 */
	public boolean isConnected() {
		// IS BLOCKING, what is a better way?
		boolean result = false;
		try {
			long before = Calendar.getInstance().getTimeInMillis();
			result = connection != null && connection.isValid(100);
			long after = Calendar.getInstance().getTimeInMillis();
			//GoCraft.debug("isConnected() took " + (after - before) + "ms");
		} catch (SQLException e) {
		}
		return result;
	}


	public static void printSQLException(SQLException ex) {
		for (Throwable e : ex) {
			if (e instanceof SQLException) {
				e.printStackTrace(System.err);
				System.err.println("SQLState: " +
						((SQLException) e).getSQLState());

				System.err.println("Error Code: " +
						((SQLException) e).getErrorCode());

				System.err.println("Message: " + e.getMessage());

				Throwable t = ex.getCause();
				while (t != null) {
					System.out.println("Cause: " + t);
					t = t.getCause();
				}
			}
		}
	}


}