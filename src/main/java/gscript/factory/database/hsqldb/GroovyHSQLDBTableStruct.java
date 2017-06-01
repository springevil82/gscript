package gscript.factory.database.hsqldb;

import gscript.GroovyException;
import gscript.factory.format.GroovyStringJoiner;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Struct of table
 */
public final class GroovyHSQLDBTableStruct {

    private String tableName;

    private final List<Column> columns = new ArrayList<>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void addColumn(Column column) {
        columns.add(column);
    }

    public String getCreateTableSql() {
        final GroovyStringJoiner columnsJoiner = new GroovyStringJoiner(",");
        for (Column column : columns)
            columnsJoiner.add(column.getName() + " " + column.getDefinition());

        return "create table " + tableName + " (" + columnsJoiner.toString() + ")";
    }

    public String getInsertSqlWithPlaceholders() {
        final GroovyStringJoiner columnsJoiner = new GroovyStringJoiner(",");
        final GroovyStringJoiner placeholdersJoiner = new GroovyStringJoiner(",");
        for (Column column : columns) {
            columnsJoiner.add(column.getName());
            placeholdersJoiner.add("?");
        }

        return "insert into " + tableName + " (" + columnsJoiner.toString() + ") values (" + placeholdersJoiner.toString() + ")";
    }

    public static class Column {
        private String name;
        private int typeCode;
        private Integer size;
        private Integer scale;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTypeCode() {
            return typeCode;
        }

        public void setTypeCode(int typeCode) {
            this.typeCode = typeCode;
        }

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Integer getScale() {
            return scale;
        }

        public void setScale(Integer scale) {
            this.scale = scale;
        }

        public String getDefinition() {
            switch (typeCode) {
                case Types.BIT:
                    return "BIT" + (size != null ? "(" + size + ")" : "");
                case Types.TINYINT:
                    return "TINYINT";
                case Types.SMALLINT:
                    return "SMALLINT";
                case Types.INTEGER:
                    return "INT";
                case Types.BIGINT:
                    return "BIGINT";
                case Types.FLOAT:
                    return "FLOAT";
                case Types.REAL:
                    return "REAL";
                case Types.DOUBLE:
                    return "DOUBLE";
                case Types.NUMERIC:
                    return "NUMERIC";
                case Types.DECIMAL:
                    return "DECIMAL" + (size != null ? (scale != null ? "(" + size + "," + scale + ")" : "(" + size + ")") : "");
                case Types.CHAR:
                    return "CHAR" + (size != null ? "(" + size + ")" : "");
                case Types.VARCHAR:
                    return size != null ? "VARCHAR(" + size + ")" : "VARCHAR(8000)";
                case Types.LONGVARCHAR:
                    return "TEXT";
                case Types.DATE:
                    return "DATE";
                case Types.TIME:
                    return "TIME";
                case Types.TIMESTAMP:
                    return "TIMESTAMP";
                case Types.BINARY:
                    return "BINARY" + (size != null ? "(" + size + ")" : "");
                case Types.VARBINARY:
                case Types.LONGVARBINARY:
                    return "VARBINARY" + (size != null ? "(" + size + ")" : "");
                case Types.BOOLEAN:
                    return "BOOLEAN";
                case Types.BLOB:
                    return "BLOB" + (size != null ? "(" + size + ")" : "");
                case Types.CLOB:
                    return "CLOB" + (size != null ? "(" + size + ")" : "");
            }

            throw new GroovyException("Unsupported SQL type: " + typeCode);
        }
    }

}
