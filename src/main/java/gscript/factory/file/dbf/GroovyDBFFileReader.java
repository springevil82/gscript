package gscript.factory.file.dbf;

import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.database.hsqldb.GroovyHSQLDBTable;
import gscript.factory.database.hsqldb.GroovyHSQLDBTableStruct;

import java.io.File;
import java.io.FileInputStream;

public class GroovyDBFFileReader implements AutoCloseable {

    private final FileInputStream fileInputStream;
    private final GroovyDBFReader dbfReader;
    private final Factory factory;
    private final File file;

    private Object[] currentRecord;

    public GroovyDBFFileReader(Factory factory, File file, String encoding) throws Exception {
        factory.registerAutoCloseable(this);

        this.factory = factory;
        this.file = file;

        fileInputStream = new FileInputStream(file);
        dbfReader = new GroovyDBFReader(fileInputStream, encoding);
    }

    public int getRecordCount() {
        return dbfReader.getRecordCount();
    }

    public int getColumnCount() {
        return dbfReader.getColumns().length;
    }

    public String getColumnName(int column) {
        return dbfReader.getColumns()[column].getName();
    }

    public int getColumnType(int column) {
        return dbfReader.getColumns()[column].getSqlType();
    }

    public String getColumnTypeName(int column) {
        return dbfReader.getColumns()[column].getColumnTypeName();
    }

    public boolean next() throws Exception {
        if (dbfReader.hasNextRecord()) {
            currentRecord = dbfReader.nextRecord();
            return true;
        }

        return false;
    }

    private void assertCurrentRow() {
        if (currentRecord == null)
            throw new GroovyException("Next row was not read. Use next() for read next row.");
    }

    public Object getObject(int column) {
        assertCurrentRow();

        return currentRecord[column];
    }

    public String getString(int column) {
        assertCurrentRow();

        final Object value = currentRecord[column];

        if (value == null)
            return null;

        if (value instanceof String)
            return (String) value;

        return value.toString();
    }

    public Double getDouble(int column) {
        assertCurrentRow();

        final Object value = currentRecord[column];

        if (value == null)
            return null;

        if (value instanceof Double)
            return (Double) value;

        return Double.parseDouble(value.toString());
    }

    @Override
    public void close() throws Exception {
        dbfReader.close();
        fileInputStream.close();
    }

    /**
     * Считать весь файл в SQL таблицу в памяти, с которой можно производить SQL операции
     *
     * @param tableName имя таблицы
     */
    public GroovyHSQLDBTable toSQLTable(String tableName) throws Exception {
        final GroovyHSQLDBTableStruct table = new GroovyHSQLDBTableStruct();
        table.setTableName(tableName);

        for (int i = 0; i < dbfReader.getFieldCount(); i++) {
            final GroovyHSQLDBTableStruct.Column column = new GroovyHSQLDBTableStruct.Column();
            final GroovyDBFField field = dbfReader.getField(i);
            column.setName(field.getName());
            column.setTypeCode(field.getSQLType());
            column.setSize(field.getLength());
            column.setScale(field.getDecimalCount());
            table.addColumn(column);
        }

        final GroovyHSQLDBTable sqlTable = factory.database.getSQLConnection().createTable(table);

        while (dbfReader.hasNextRecord())
            sqlTable.writeRecord(dbfReader.nextRecord());

        sqlTable.flush();
        dbfReader.close();

        return sqlTable;
    }

    /**
     * Считать весь файл в SQL таблицу в памяти, с которой можно производить SQL операции
     */
    public GroovyHSQLDBTable toSQLTable() throws Exception {
        return toSQLTable(factory.file.splitFileNameExt(file.getName())[0]);
    }

}
