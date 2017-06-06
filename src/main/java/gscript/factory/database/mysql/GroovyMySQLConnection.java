package gscript.factory.database.mysql;

import gscript.Factory;
import gscript.factory.database.GroovySQLConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class GroovyMySQLConnection extends GroovySQLConnection {

    private final Factory factory;
    private final Connection connection;

    public GroovyMySQLConnection(Factory factory, String host, int port, String database, String username, String password) throws Exception {
        this.factory = factory;

        final Properties connectionProperties = new Properties();
        connectionProperties.put("user", username);
        connectionProperties.put("password", password);
        connectionProperties.put("allowMultiQueries", "true");
        connectionProperties.put("rewriteBatchedStatements", "true");

        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + database,
                connectionProperties);
        connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
    }

    @Override
    public Factory getFactory() {
        return factory;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * Create table writer
     *
     * @param tableName             table name
     * @param commaSeparatedColumns column names separated by comma
     * @param returnGeneratedKeys   return generated ids
     */
    public GroovyMySQLTableWriter createTableWriter(String tableName, String commaSeparatedColumns, boolean returnGeneratedKeys) throws SQLException {
        return new GroovyMySQLTableWriter(getFactory(), connection, tableName, commaSeparatedColumns, returnGeneratedKeys);
    }
}
