package gscript.factory.database;

import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.document.GroovyMultilineDocument;
import gscript.factory.format.GroovyDBTablePrinter;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.*;

public abstract class GroovySQLConnection implements AutoCloseable {

    private String lastError;

    public abstract Factory getFactory();

    public abstract Connection getConnection();

    @Override
    public void close() throws Exception {
        getConnection().close();
    }

    /**
     * Get database version
     */
    public String getDatabaseVersion() throws SQLException {
        return getConnection().getMetaData().getDatabaseProductVersion();
    }

    /**
     * Start new transaction
     */
    public void startTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    /**
     * Commit current transaction
     *
     * @throws SQLException
     */
    public void commit() throws SQLException {
        getConnection().commit();
        getConnection().setAutoCommit(true);
    }

    /**
     * Rollback current transaction
     *
     * @throws SQLException
     */
    public void rollback() throws SQLException {
        getConnection().rollback();
        getConnection().setAutoCommit(true);
    }

    /**
     * Get last error
     */
    public String getLastError() {
        return lastError;
    }

    /**
     * Executes the SQL command, which must be an SQL Data Manipulation Language (DML) statement,
     * such as INSERT, UPDATE or DELETE; or an SQL statement that returns nothing, such as a DDL statement.
     *
     * @param sql SQL
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
     * @throws SQLException
     */
    public int executeSQL(String sql) throws SQLException {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        }
    }

    /**
     * Safely execute SQL (DML), no exception will be thrown.
     * You can get error information by getLastError() method.
     *
     * @param sql SQL
     * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
     */
    public int executeSQLSafe(String sql) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            lastError = e.getMessage();
            return -1;
        }
    }

    /**
     * Safely drop table.
     * You can get error information by getLastError() method.
     *
     * @param tableName table name
     * @return true - table successfully dropped, false - error occurred while dropping the table
     */
    public boolean dropTableSafe(String tableName) {
        try (PreparedStatement preparedStatement = getConnection().prepareStatement("drop table " + tableName)) {
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            lastError = e.getMessage();
            return false;
        }
    }

    /**
     * Query single cell (row/column) result set and retrieve cell value
     *
     * @return single cell value
     */
    public Object queryObject(String sql) throws SQLException {
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            if (resultSet.next())
                return resultSet.getObject(1);
            else
                throw new GroovyException("No one row found");
        }
    }

    /**
     * Query single row result set and retrieve row cell values
     *
     * @return row cell values
     */
    public Object[] queryRow(String sql) throws SQLException {
        Object[] objects = new Object[0];
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            if (resultSet.next()) {

                for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++)
                    objects = ArrayUtils.add(objects, resultSet.getObject(i + 1));

                return objects;
            } else
                throw new GroovyException("No one row found");
        }
    }

    /**
     * Query result set
     *
     * @param sql SQL
     * @return ResultSet
     * @throws SQLException
     */
    public GroovyResultSet queryResultSet(String sql) throws SQLException {
        long currentTime = System.currentTimeMillis();
        final ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery();
        long timeTaken = System.currentTimeMillis() - currentTime;

        return new GroovyResultSet(getFactory(), resultSet, timeTaken);
    }

    /**
     * Query result set and print it to stdout
     *
     * @param sql SQL
     * @throws Exception
     */
    public void printResultSet(String sql) throws Exception {
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            new GroovyDBTablePrinter().printResultSet(resultSet);
        }
    }

    /**
     * Query result set and load it to multiline document.
     *
     * @param sql SQL
     * @return document
     * @throws Exception
     */
    public GroovyMultilineDocument resultSetToDocument(String sql) throws Exception {
        final GroovyMultilineDocument document = getFactory().document.createDocument();

        document.put("query", sql);

        final long startTime = System.currentTimeMillis();
        try (ResultSet resultSet = getConnection().prepareStatement(sql).executeQuery()) {
            final ResultSetMetaData metaData = resultSet.getMetaData();

            int rowCount = 0;
            while (resultSet.next()) {
                final GroovyMultilineDocument.Line line = document.createLine();
                for (int i = 0; i < metaData.getColumnCount(); i++)
                    line.put(metaData.getColumnName(i + 1), resultSet.getObject(i + 1));

                rowCount++;
            }

            document.put("rowsFetched", rowCount);
        }

        document.put("executeTimeInMilis", System.currentTimeMillis() - startTime);

        return document;
    }

}
