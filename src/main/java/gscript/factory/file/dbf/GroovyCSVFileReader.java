package gscript.factory.file.dbf;

import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.database.hsqldb.GroovyHSQLDBTable;
import gscript.factory.database.hsqldb.GroovyHSQLDBTableStruct;
import gscript.factory.document.RegExp;
import gscript.factory.format.GroovyStringJoiner;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class GroovyCSVFileReader implements AutoCloseable {

    private final Factory factory;
    private final File file;
    private final BufferedReader bufferedReader;
    private final CSVParser csvParser;
    private final Iterator<CSVRecord> iterator;
    private final String separator;

    private List<ColumnDefinition> columns = new ArrayList<>();
    private boolean skipFirstNext = false;
    private String[] currentRecord;

    public GroovyCSVFileReader(Factory factory, File file, String encoding, String separator, int readFromLine, boolean containsHeader) throws Exception {
        factory.registerAutoCloseable(this);

        this.separator = separator;
        this.factory = factory;
        this.file = file;

        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        if (readFromLine > 0) {
            for (int i = 0; i < readFromLine; i++)
                bufferedReader.readLine();
        }

        csvParser = new CSVParser(bufferedReader, CSVFormat.newFormat(separator.charAt(0)));
        iterator = csvParser.iterator();

        if (iterator.hasNext()) {
            final CSVRecord record = iterator.next();

            if (containsHeader) {
                for (int i = 0; i < record.size(); i++)
                    columns.add(new ColumnDefinition(record.get(i)));

                for (ColumnDefinition column : columns) {
                    if (factory.string.containsRussianSymbolsOrSpaces(column.getName())) {
                        createDefaultColumnNames(columns);
                        break;
                    }
                }
            } else {
                createDefaultColumnNames(record);
                skipFirstNext = true;
            }
        }
    }

    public void printColumns() {
        for (ColumnDefinition columnDefinition : columns)
            System.out.println(columnDefinition);
    }

    /**
     * <p>Set column name and type
     * <p>Available descriptions are:
     * <br>VARCHAR - string unlimited
     * <br>VARCHAR(length) - string limited
     * <br>INTEGER - integer with length
     * <br>DECIMAL(length,scale) - decimal with length and scale
     * <br>DATE(format) - date with dateFormat
     * <br>BOOLEAN - boolean
     *
     * @param columnIndex column index
     * @param columnName  column name
     * @param columnLabel column title
     * @param definition  column description
     */
    public void setColumnDefinition(int columnIndex, String columnName, String columnLabel, String definition) {
        final ColumnDefinition columnDefinition = columns.get(columnIndex);
        columnDefinition.setName(columnName);
        if (columnLabel != null)
            columnDefinition.setLabel(columnLabel);

        columnDefinition.setDefinition(definition);

        Matcher matcher;

        matcher = RegExp.VARCHAR_WITH_SIZE.matcher(definition);
        if (matcher.find()) {
            columnDefinition.setSqlType(Types.VARCHAR);
            columnDefinition.setSqlTypeName(definition);
            columnDefinition.setSize(Integer.parseInt(matcher.group(1)));
        }

        matcher = RegExp.INTEGER_WITH_SIZE.matcher(definition);
        if (matcher.find()) {
            columnDefinition.setSqlType(Types.INTEGER);
            columnDefinition.setSqlTypeName(definition);
            columnDefinition.setSize(Integer.parseInt(matcher.group(1)));
        }

        matcher = RegExp.SIMPLE_INTEGER.matcher(definition);
        if (matcher.find()) {
            columnDefinition.setSqlType(Types.INTEGER);
            columnDefinition.setSqlTypeName(definition);
        }

        matcher = RegExp.DECIMAL_WITH_SIZE_AND_SCALE.matcher(definition);
        if (matcher.find()) {
            columnDefinition.setSqlType(Types.DOUBLE);
            columnDefinition.setSqlTypeName(definition);
            columnDefinition.setSize(Integer.parseInt(matcher.group(1)));
            columnDefinition.setScale(Integer.parseInt(matcher.group(2)));
        }

        matcher = RegExp.DATE_WITH_FORMAT.matcher(definition);
        if (matcher.find()) {
            columnDefinition.setSqlType(Types.DATE);
            columnDefinition.setSqlTypeName("DATE(" + matcher.group(1).length() + ")");
            columnDefinition.setFormat(matcher.group(1));
        } else if ("DATE".equals(definition)) {
            throw new GroovyException("DateFormat is required: DATE(dateFormat).\n" +
                    "Example: DATE(dd.MM.yyyy) or DATE(yyyy-MM-dd HH:mm:ss)");
        }

        matcher = RegExp.SIMPLE_BOOLEAN.matcher(definition);
        if (matcher.find()) {
            columnDefinition.setSqlType(Types.BOOLEAN);
            columnDefinition.setSqlTypeName("BOOLEAN");
        }

    }

    private void createDefaultColumnNames(List<ColumnDefinition> columns) {
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setLabel(columns.get(i).getName());
            columns.get(i).setName("COLUMN_" + i);
            columns.get(i).setSqlType(Types.VARCHAR);
            columns.get(i).setSqlTypeName("VARCHAR");
            columns.get(i).setDefinition("VARCHAR");
        }
    }

    private void createDefaultColumnNames(CSVRecord record) {
        for (int i = 0; i < record.size(); i++) {
            columns.add(new ColumnDefinition("COLUMN_" + i));
            columns.get(i).setSqlType(Types.VARCHAR);
            columns.get(i).setSqlTypeName("VARCHAR");
            columns.get(i).setDefinition("VARCHAR");
        }
    }

    public int getColumnCount() {
        return columns.size();
    }

    public String getColumnName(int column) {
        return columns.get(column).getName();
    }

    public String getColumnLabel(int column) {
        return columns.get(column).getLabel();
    }

    public int getColumnType(int column) {
        return Types.VARCHAR;
    }

    public String getColumnTypeName(int column) {
        return "Character";
    }

    public boolean next() throws Exception {
        if (skipFirstNext) {
            skipFirstNext = false;
            return true;
        }

        if (iterator.hasNext()) {
            final CSVRecord record = iterator.next();

            currentRecord = new String[0];

            for (String value : record)
                currentRecord = ArrayUtils.add(currentRecord, value);

            return true;
        }

        return false;
    }

    private void assertCurrentRow() {
        if (currentRecord == null)
            throw new GroovyException("Next row was not read. Use next() to read next row.");
    }

    public String getCurrentRecord() {
        final GroovyStringJoiner stringJoiner = new GroovyStringJoiner(String.valueOf(separator));
        for (String s : currentRecord)
            stringJoiner.add(s);

        return stringJoiner.toString();
    }

    public Object getObject(int column) throws Exception {
        final ColumnDefinition columnDefinition = columns.get(column);

        if ("".equals(currentRecord[column]))
            return null;

        switch (columnDefinition.getSqlType()) {
            case Types.VARCHAR:
                return currentRecord[column];
            case Types.INTEGER:
                if (columnDefinition.size > 10)
                    return Long.parseLong(currentRecord[column]);
                else
                    return Integer.parseInt(currentRecord[column]);
            case Types.DOUBLE:
                return Double.parseDouble(currentRecord[column]);
            case Types.DATE:
                return new SimpleDateFormat(columnDefinition.format).parse(currentRecord[column]);
            case Types.BOOLEAN:
                return "true".equals(currentRecord[column].toLowerCase()) || "1".equals(currentRecord[column]);
        }

        return currentRecord[column];
    }

    public Object[] getObjects() throws Exception {
        Object[] objects = new Object[0];
        for (int i = 0; i < columns.size(); i++)
            objects = ArrayUtils.add(objects, getObject(i));

        return objects;
    }

    public String getString(int column) {
        assertCurrentRow();

        return "".equals(currentRecord[column]) ? null : currentRecord[column];
    }

    public Integer getInteger(int column) {
        assertCurrentRow();

        final String value = currentRecord[column].trim();
        return "".equals(value) ? null : Integer.parseInt(value);
    }

    public Double getDouble(int column) {
        assertCurrentRow();

        final String value = currentRecord[column].trim();
        return "".equals(value) ? null : Double.parseDouble(value);
    }

    public Float getFloat(int column) {
        assertCurrentRow();

        final String value = currentRecord[column].trim();
        return "".equals(value) ? null : Float.parseFloat(value);
    }

    public BigDecimal getBigDecimal(int column) {
        assertCurrentRow();

        final String value = currentRecord[column].trim();
        return "".equals(value) ? null : BigDecimal.valueOf(Double.parseDouble(value)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Boolean getBoolean(int column) {
        assertCurrentRow();

        final String value = currentRecord[column].trim().toLowerCase();
        return "".equals(value) ? null : "true".equals(value) || "1".equals(value);
    }

    public Date getDate(int column, String format) throws Exception {
        assertCurrentRow();

        final String value = currentRecord[column].trim();
        return "".equals(value) ? null : new SimpleDateFormat(format).parse(value);
    }


    @Override
    public void close() throws Exception {
        csvParser.close();
        bufferedReader.close();
    }

    /**
     * Create new "im mem" HSQLDB table with name (tableName) and load all CSV data into it
     *
     * @param tableName name of new table
     */
    public GroovyHSQLDBTable toSQLTable(String tableName) throws Exception {
        final GroovyHSQLDBTableStruct table = new GroovyHSQLDBTableStruct();
        table.setTableName(tableName);

        for (ColumnDefinition c : columns) {
            final GroovyHSQLDBTableStruct.Column column = new GroovyHSQLDBTableStruct.Column();
            column.setName(c.getName());
            column.setTypeCode(c.getSqlType());

            if (c.getSize() > 0) {
                column.setSize(c.getSize());
                column.setScale(c.getScale());
            }

            table.addColumn(column);
        }

        final GroovyHSQLDBTable sqlTable = factory.database.getSQLConnection().createTable(table);

        while (next())
            sqlTable.writeRecord(getObjects());

        sqlTable.flush();
        close();

        return sqlTable;
    }

    /**
     * Create new "im mem" HSQLDB table with name (filename without ext) and load all CSV data into it
     */
    public GroovyHSQLDBTable toSQLTable() throws Exception {
        return toSQLTable(factory.file.splitFileNameExt(file.getName())[0]);
    }

    private final class ColumnDefinition {
        private String definition;

        private String name;
        private String label;
        private int sqlType;
        private String sqlTypeName;
        private int size;
        private int scale;
        private String format;

        public ColumnDefinition(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getSqlType() {
            return sqlType;
        }

        public void setSqlType(int sqlType) {
            this.sqlType = sqlType;
        }

        public String getSqlTypeName() {
            return sqlTypeName;
        }

        public void setSqlTypeName(String sqlTypeName) {
            this.sqlTypeName = sqlTypeName;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }

        public String getDefinition() {
            return definition;
        }

        public void setDefinition(String definition) {
            this.definition = definition;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        @Override
        public String toString() {
            return "name: '" + name + '\'' +
                    ", label: '" + label + '\'' +
                    ", definition: '" + definition + '\'';
        }
    }

}
