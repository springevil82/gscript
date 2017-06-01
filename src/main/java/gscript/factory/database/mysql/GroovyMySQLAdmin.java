package gscript.factory.database.mysql;

import gscript.Factory;
import gscript.GroovyException;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.*;

public final class GroovyMySQLAdmin {

    private final Factory factory;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final Connection connection;

    public GroovyMySQLAdmin(Factory factory, String host, int port, String username, String password) throws Exception {
        this.factory = factory;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port, username, password);
    }

    /**
     * Check if database schema exists
     *
     * @param name schema name
     * @return true/false
     */
    public boolean databaseExists(String name) throws SQLException {
        for (GroovyMySQLDatabase database : getDatabases())
            if (database.getDatabase().equals(name))
                return true;

        return false;
    }

    /**
     * Access database by name
     *
     * @param name name
     */
    public GroovyMySQLDatabase getDatabase(String name) throws Exception {
        for (GroovyMySQLDatabase database : getDatabases())
            if (database.getDatabase().equals(name))
                return database;

        throw new GroovyException("Database \"" + name + "\" not found");
    }

    /**
     * Get all schema names
     */
    public GroovyMySQLDatabase[] getDatabases() throws SQLException {
        GroovyMySQLDatabase[] databases = new GroovyMySQLDatabase[0];
        try (final PreparedStatement statement = connection.prepareStatement("SHOW DATABASES")) {
            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
                databases = ArrayUtils.add(databases,
                        new GroovyMySQLDatabase(factory, host, port, resultSet.getString(1), username, password));
        }

        return databases;
    }

    /**
     * Create new schema (database)
     *
     * @param name     schema name
     * @param encoding tables encoding
     */
    public GroovyMySQLDatabase createDatabase(String name, String encoding) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(
                "CREATE DATABASE " + name + " CHARACTER SET " + encoding + " COLLATE " + encoding + "_general_ci")) {
            statement.executeUpdate();
        }

        return new GroovyMySQLDatabase(factory, host, port, name, username, password);
    }

    /**
     * Create new schema (database) with russian encoding cp1251
     *
     * @param name schema name
     */
    public GroovyMySQLDatabase createDatabase(String name) throws SQLException {
        return createDatabase(name, "cp1251");
    }

    /**
     * Drop database
     *
     * @param name schema name
     */
    public void dropDatabase(String name) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("DROP DATABASE " + name)) {
            statement.executeUpdate();
        }
    }

    /**
     * Drop database if exists
     *
     * @param name schema name
     */
    public void dropDatabaseIfExists(String name) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("DROP DATABASE IF EXISTS " + name)) {
            statement.executeUpdate();
        }
    }

}
