package gscript.factory.database.hsqldb;

import gscript.Factory;
import gscript.factory.database.GroovySQLConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class GroovyHSQLDBConnection extends GroovySQLConnection {

    private final Factory factory;
    private final Connection connection;

    private final List<String> tables = new ArrayList<>();

    /**
     * Create connection to in mem HSQLDB database
     */
    public GroovyHSQLDBConnection(Factory factory) throws Exception {
        factory.registerAutoCloseable(this);

        this.factory = factory;

        Class.forName("org.hsqldb.jdbcDriver");
        this.connection = DriverManager.getConnection("jdbc:hsqldb:mem:memdb", "SA", "");
    }

    @Override
    public void close() throws Exception {
        for (String tableName : tables) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE " + tableName);
            }
        }

        connection.close();
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
     * Create new table
     *
     * @param table tablename
     */
    public GroovyHSQLDBTable createTable(GroovyHSQLDBTableStruct table) throws Exception {
        connection.prepareCall(table.getCreateTableSql()).executeUpdate();
        tables.add(table.getTableName());

        return new GroovyHSQLDBTable(factory, this, table);
    }


}
