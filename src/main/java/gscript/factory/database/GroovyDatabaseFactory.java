package gscript.factory.database;

import gscript.Factory;
import gscript.factory.database.cache.GroovyCacheConnection;
import gscript.factory.database.hsqldb.GroovyHSQLDBConnection;
import gscript.factory.database.mysql.GroovyMySQLAdmin;
import gscript.factory.database.mysql.GroovyMySQLConnection;

public final class GroovyDatabaseFactory {

    private final Factory factory;

    private GroovyHSQLDBConnection hsqldbMemConnection;

    /**
     * Create connection to Intersystems Cache database
     *
     * @param host      host
     * @param port      port
     * @param namespace namespace
     * @param username  username
     * @param password  password
     * @return connection
     */
    public GroovyCacheConnection createCacheConnection(String host, int port, String namespace, String username, String password) throws Exception {
        return new GroovyCacheConnection(factory, host, port, namespace, username, password);
    }

    /**
     * Create connection to Intersystems Cache database with default credentials _SYSTEM/SYS
     *
     * @param host      host
     * @param port      port
     * @param namespace namespace
     * @return connection
     */
    public GroovyCacheConnection createCacheConnection(String host, int port, String namespace) throws Exception {
        return createCacheConnection(host, port, namespace, "_SYSTEM", "SYS");
    }

    /**
     * Create mysql database admin tool (allows you to create/drop/backup database schemas)
     *
     * @param host     host
     * @param port     port
     * @param username username
     * @param password password
     * @throws Exception
     */
    public GroovyMySQLAdmin createMySQLAdmin(String host, int port, String username, String password) throws Exception {
        return new GroovyMySQLAdmin(factory, host, port, username, password);
    }

    /**
     * Create connection to mysql database
     *
     * @param host     host
     * @param port     port
     * @param database database
     * @param username username
     * @param password password
     * @return connection
     * @throws Exception
     */
    public GroovyMySQLConnection createMySQLConnection(String host, int port, String database, String username, String password) throws Exception {
        return new GroovyMySQLConnection(factory, host, port, database, username, password);
    }

    /**
     * Create connection to HSQLDB in mem database
     *
     * @return connection
     * @throws Exception
     */
    public GroovyHSQLDBConnection getSQLConnection() throws Exception {
        if (hsqldbMemConnection == null)
            hsqldbMemConnection = new GroovyHSQLDBConnection(factory);

        return hsqldbMemConnection;
    }

    public GroovyDatabaseFactory(Factory factory) {
        this.factory = factory;
    }
}
