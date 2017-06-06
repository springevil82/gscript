package gscript.factory.database;

import gscript.Factory;
import gscript.factory.database.hsqldb.GroovyHSQLDBTable;
import gscript.factory.database.hsqldb.GroovyHSQLDBTableStruct;
import gscript.factory.document.GroovyMultilineDocument;
import gscript.factory.format.GroovyDBTablePrinter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class GroovyResultSet implements AutoCloseable {

    private final ResultSet resultSet;
    private long timeTaken;
    private Factory factory;

    public GroovyResultSet(Factory factory, ResultSet resultSet, long timeTaken) {
        factory.registerAutoCloseable(this);

        this.factory = factory;
        this.timeTaken = timeTaken;
        this.resultSet = resultSet;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void print() throws Exception {
        new GroovyDBTablePrinter().printResultSet(resultSet);
        System.out.println("SQL execution time: " + factory.string.millisToString(timeTaken));
        resultSet.close();
    }

    public long getQueryExecutionTimeInMilis() {
        return timeTaken;
    }

    public void saveToDBF(Object file, String encoding) throws SQLException {
        toDocument().saveToDBF(file, encoding);
    }

    public void saveToCSV(Object file, String encoding) throws SQLException {
        toDocument().saveToCSV(file, encoding);
    }

    public void saveToXML(Object file) throws SQLException {
        toDocument().saveToXML(file);
    }

    public GroovyMultilineDocument toDocument() throws SQLException {
        final GroovyMultilineDocument document = new GroovyMultilineDocument(factory);

        final List<String> columns = new ArrayList<>();
        final ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 0; i < metaData.getColumnCount(); i++)
            columns.add(metaData.getColumnName(i + 1));

        int rows = 0;
        while (resultSet.next()) {
            final GroovyMultilineDocument.Line line = document.createLine();
            for (int i = 0; i < columns.size(); i++)
                line.put(columns.get(i), resultSet.getObject(i + 1));

            rows++;
        }

        document.put("rowCount", rows);
        document.put("columnCount", columns.size());

        resultSet.close();
        return document;
    }

    /**
     * Load result set in HSQLDB "in mem" table
     *
     * @param tableName table name
     */
    public GroovyHSQLDBTable toSQLTable(String tableName) throws Exception {
        final GroovyHSQLDBTableStruct table = new GroovyHSQLDBTableStruct();
        table.setTableName(tableName);

        final ResultSetMetaData metaData = resultSet.getMetaData();

        for (int i = 0; i < metaData.getColumnCount(); i++) {
            final GroovyHSQLDBTableStruct.Column column = new GroovyHSQLDBTableStruct.Column();
            column.setName(metaData.getColumnName(i + 1));
            column.setTypeCode(metaData.getColumnType(i + 1));
            column.setSize(metaData.getPrecision(i + 1));
            column.setScale(metaData.getScale(i + 1));
            table.addColumn(column);
        }

        final GroovyHSQLDBTable sqlTable = factory.database.getSQLConnection().createTable(table);

        while (resultSet.next()) {
            Object[] values = new Object[0];

            for (int i = 0; i < metaData.getColumnCount(); i++)
                values = ArrayUtils.add(values, resultSet.getObject(i + 1));

            sqlTable.writeRecord(values);
        }

        sqlTable.flush();
        resultSet.close();

        return sqlTable;
    }


    // ================================ java.sql.ResultSet wrapped routines ================================
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    public void close() throws Exception {
        resultSet.close();
    }

    public String getString(int columnIndex) throws SQLException {
        return resultSet.getString(columnIndex);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return resultSet.getBoolean(columnIndex);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return resultSet.getByte(columnIndex);
    }

    public short getShort(int columnIndex) throws SQLException {
        return resultSet.getShort(columnIndex);
    }

    public int getInt(int columnIndex) throws SQLException {
        return resultSet.getInt(columnIndex);
    }

    public long getLong(int columnIndex) throws SQLException {
        return resultSet.getLong(columnIndex);
    }

    public float getFloat(int columnIndex) throws SQLException {
        return resultSet.getFloat(columnIndex);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return resultSet.getDouble(columnIndex);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        //noinspection deprecation
        return resultSet.getBigDecimal(columnIndex, scale);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return resultSet.getBytes(columnIndex);
    }

    public Date getDate(int columnIndex) throws SQLException {
        return resultSet.getDate(columnIndex);
    }

    public Time getTime(int columnIndex) throws SQLException {
        return resultSet.getTime(columnIndex);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return resultSet.getTimestamp(columnIndex);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return resultSet.getAsciiStream(columnIndex);
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return resultSet.getUnicodeStream(columnIndex);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return resultSet.getBinaryStream(columnIndex);
    }

    public String getString(String columnLabel) throws SQLException {
        return resultSet.getString(columnLabel);
    }

    public boolean getBoolean(String columnLabel) throws SQLException {
        return resultSet.getBoolean(columnLabel);
    }

    public byte getByte(String columnLabel) throws SQLException {
        return resultSet.getByte(columnLabel);
    }

    public short getShort(String columnLabel) throws SQLException {
        return resultSet.getShort(columnLabel);
    }

    public int getInt(String columnLabel) throws SQLException {
        return resultSet.getInt(columnLabel);
    }

    public long getLong(String columnLabel) throws SQLException {
        return resultSet.getLong(columnLabel);
    }

    public float getFloat(String columnLabel) throws SQLException {
        return resultSet.getFloat(columnLabel);
    }

    public double getDouble(String columnLabel) throws SQLException {
        return resultSet.getDouble(columnLabel);
    }

    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        //noinspection deprecation
        return resultSet.getBigDecimal(columnLabel, scale);
    }

    public byte[] getBytes(String columnLabel) throws SQLException {
        return resultSet.getBytes(columnLabel);
    }

    public Date getDate(String columnLabel) throws SQLException {
        return resultSet.getDate(columnLabel);
    }

    public Time getTime(String columnLabel) throws SQLException {
        return resultSet.getTime(columnLabel);
    }

    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        return resultSet.getTimestamp(columnLabel);
    }

    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        return resultSet.getAsciiStream(columnLabel);
    }

    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        return resultSet.getUnicodeStream(columnLabel);
    }

    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        return resultSet.getBinaryStream(columnLabel);
    }

    public SQLWarning getWarnings() throws SQLException {
        return resultSet.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        resultSet.clearWarnings();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return resultSet.getMetaData();
    }

    public Object getObject(int columnIndex) throws SQLException {
        return resultSet.getObject(columnIndex);
    }

    public Object getObject(String columnLabel) throws SQLException {
        return resultSet.getObject(columnLabel);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return resultSet.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnLabel) throws SQLException {
        return resultSet.getCharacterStream(columnLabel);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return resultSet.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        return resultSet.getBigDecimal(columnLabel);
    }

    public int getRow() throws SQLException {
        return resultSet.getRow();
    }

    public Statement getStatement() throws SQLException {
        return resultSet.getStatement();
    }

    public Blob getBlob(int columnIndex) throws SQLException {
        return resultSet.getBlob(columnIndex);
    }

    public Clob getClob(int columnIndex) throws SQLException {
        return resultSet.getClob(columnIndex);
    }

    public Blob getBlob(String columnLabel) throws SQLException {
        return resultSet.getBlob(columnLabel);
    }

    public Clob getClob(String columnLabel) throws SQLException {
        return resultSet.getClob(columnLabel);
    }

    public boolean isClosed() throws SQLException {
        return resultSet.isClosed();
    }

}
