package gscript.factory.database.mysql;

import groovy.lang.GString;
import gscript.Factory;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.*;

public final class GroovyMySQLTableWriter implements AutoCloseable {

    private final Connection connection;
    private final String tableName;
    private final String columns;

    private StringBuilder cortege = new StringBuilder();
    private final String insertPrefix;
    private final PreparedStatement insertStatement;
    private final String insertSuffix;
    private long max_allowed_packet = 1024 * 1024;
    private boolean returnGeneratedKeys;
    private long[] generatedKeys = new long[0];
    private long rowsInserted = 0;

    public GroovyMySQLTableWriter(Factory factory, Connection connection, String tableName, String columns, boolean returnGeneratedKeys) throws SQLException {
        factory.registerAutoCloseable(this);

        this.connection = connection;
        this.tableName = tableName;
        this.columns = columns;
        this.returnGeneratedKeys = returnGeneratedKeys;

        try (final PreparedStatement preparedStatement = connection.prepareStatement("SHOW VARIABLES LIKE ?")) {
            preparedStatement.setString(1, "max_allowed_packet");
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                max_allowed_packet = resultSet.getLong("Value");
        }

        String parametersString = "";
        int columnCount = columns.split(",").length;
        for (int i = 0; i < columnCount; i++)
            parametersString = parametersString + (i == columnCount - 1 ? "?" : "?, ");

        this.insertPrefix = "insert into " + tableName + " (" + columns + ") values ";
        this.insertStatement = connection.prepareStatement("(" + parametersString + ")");
        this.insertSuffix = "";
    }

    private String extractStatementSQL(Statement statement) {
        final String str = statement.toString();
        int index = str.indexOf(' ');
        return str.substring(index == -1 ? 0 : index, str.length());
    }

    private String buildCortegeRecord(Object[] values) {
        try {
            for (int i = 0; i < values.length; i++) {

                if (values[i] instanceof GString)
                    values[i] = values[i].toString();

                insertStatement.setObject(i + 1, values[i]);
            }

            return extractStatementSQL(insertStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeRecord(Object... values) throws SQLException {
        final String cortegeRecord = buildCortegeRecord(values);

        if (insertPrefix.length() + cortege.length() + cortegeRecord.length() + insertSuffix.length() + 1 > max_allowed_packet / 2)
            flush();

        cortege.append(",").append(cortegeRecord);
    }

    private String getCortege() {
        if (cortege.length() > 1 && cortege.charAt(0) == ',')
            cortege = cortege.delete(0, 1);

        return cortege.toString();
    }

    private PreparedStatement createStatement(String sql) throws SQLException {
        if (returnGeneratedKeys)
            return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        return connection.prepareStatement(sql);
    }

    public void flush() throws SQLException {
        String cortege = getCortege();
        if ("".equals(cortege))
            return;

        try (final PreparedStatement statement = createStatement(insertPrefix + cortege + insertSuffix)) {
            rowsInserted += statement.executeUpdate();
            if (returnGeneratedKeys) {
                final ResultSet keys = statement.getGeneratedKeys();
                while (keys.next())
                    generatedKeys = ArrayUtils.add(generatedKeys, keys.getLong(1));
            }
        }

        this.cortege = new StringBuilder();
    }

    @Override
    public void close() throws Exception {
        flush();

        try {
            if (insertStatement != null && !insertStatement.isClosed())
                insertStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getRowsInserted() {
        return rowsInserted;
    }

    public long[] getGeneratedKeys() {
        return generatedKeys;
    }
}
