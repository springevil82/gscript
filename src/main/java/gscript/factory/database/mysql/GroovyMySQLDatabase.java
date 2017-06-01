package gscript.factory.database.mysql;

import gscript.Factory;

public final class GroovyMySQLDatabase {

    private final Factory factory;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    public GroovyMySQLDatabase(Factory factory, String host, int port, String database, String username, String password) {
        this.factory = factory;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return database;
    }

    /**
     * Соединиться с БД
     *
     * @return
     * @throws Exception
     */
    public GroovyMySQLConnection createConnection() throws Exception {
        return factory.database.createMySQLConnection(host, port, database, username, password);
    }
}
