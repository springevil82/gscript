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
     * Создать админ MySQL, который может создавать/удалять/бэкапить БД
     *
     * @param host     host
     * @param port     port
     * @param username username
     * @param password password
     * @return админ
     * @throws Exception
     */
    public GroovyMySQLAdmin createMySQLAdmin(String host, int port, String username, String password) throws Exception {
        return new GroovyMySQLAdmin(factory, host, port, username, password);
    }

    /**
     * Создать соединение к БД MySQL
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
     * Создать соединение к HSQLDB базе данных в памяти
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
