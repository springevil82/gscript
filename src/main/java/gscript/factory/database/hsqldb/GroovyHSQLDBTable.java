package gscript.factory.database.hsqldb;

import gscript.Factory;
import gscript.factory.database.GroovyResultSet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represent in mem table. Allows to write records and perform sql.
 */
public final class GroovyHSQLDBTable {

    private final Factory factory;
    private final GroovyHSQLDBConnection connection;
    private final GroovyHSQLDBTableStruct table;
    private final PreparedStatement insertStatement;

    private long recordCount;

    public String getName() {
        return table.getTableName();
    }

    public GroovyHSQLDBTable(Factory factory, GroovyHSQLDBConnection connection, GroovyHSQLDBTableStruct table) throws SQLException {
        this.factory = factory;
        this.connection = connection;
        this.table = table;
        this.insertStatement = connection.getConnection().prepareStatement(table.getInsertSqlWithPlaceholders());
        this.recordCount = 0;
    }

    public void writeRecord(Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++)
            insertStatement.setObject(i + 1, values[i]);

        insertStatement.addBatch();
        recordCount++;
    }

    public int[] flush() throws SQLException {
        return insertStatement.executeBatch();
    }

    public long getRecordCount() {
        return recordCount;
    }

    /**
     * Query record count in table
     */
    public long selectRecordCount() throws SQLException {
        try (ResultSet resultSet = connection.getConnection().prepareStatement("select count(*) from " + table.getTableName()).executeQuery()) {
            resultSet.next();
            final long count = resultSet.getLong(1);
            recordCount = count;
            return count;
        }
    }

    /**
     * Select all records
     */
    public GroovyResultSet selectAll() throws SQLException {
        return connection.queryResultSet("select * from " + table.getTableName());
    }

    /**
     * Query result set
     *
     * @param sql sql
     */
    public GroovyResultSet queryResultSet(String sql) throws SQLException {
        return connection.queryResultSet(sql);
    }

/*
    public GroovyResultSet query(Closure<GroovyQueryBuilder> closure) throws SQLException {
        final GroovyQueryBuilder queryBuilder = new GroovyQueryBuilder(table.getName());
        closure.call(queryBuilder);
        final String query = queryBuilder.getQuery();
        return new GroovyResultSet(factory, connection.prepareStatement(query).executeQuery());
    }
*/

}
