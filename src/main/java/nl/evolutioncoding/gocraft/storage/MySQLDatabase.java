package nl.evolutioncoding.gocraft.storage;

import nl.evolutioncoding.gocraft.GoCraft;

public class MySQLDatabase extends Database {

    /**
     * Construct a MySQL database instance.
     *
     * @param host     Host of the MySQL database.
     * @param port     Port of the MySQL database.
     * @param database Database wanted to be access.
     * @param username Username to be authenticated with.
     * @param password Password for the user authentication.
     * @param plugin   Plugin for the schedulers to be assigned to.
     */
    public MySQLDatabase(String host, int port, String database, String username, String password, GoCraft plugin) {
        super("com.mysql.jdbc.Driver", "jdbc:mysql://" + host + ":" + port + "/" + database, username, password, plugin);
    }

}