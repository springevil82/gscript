package gscript.factory.format;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public final class GroovyDBTablePrinter {

    private static final int DEFAULT_MAX_ROWS = 10;
    private static final int DEFAULT_MAX_TEXT_COL_WIDTH = 150;
    public static final int CATEGORY_STRING = 1;
    public static final int CATEGORY_INTEGER = 2;
    public static final int CATEGORY_DOUBLE = 3;
    public static final int CATEGORY_DATETIME = 4;
    public static final int CATEGORY_BOOLEAN = 5;
    public static final int CATEGORY_OTHER = 0;

    public interface DataSource {
        int getColumnCount() throws Exception;

        String getColumnLabel(int i) throws Exception;

        int getColumnType(int i) throws Exception;

        String getColumnTypeName(int i) throws Exception;

        String getTableName(int i) throws Exception;

        boolean next() throws Exception;

        String getString(int i) throws Exception;

        Double getDouble(int i) throws Exception;
    }

    private class Column {

        private String label;
        private int type;
        private String typeName;
        private int width = 0;
        private final List<String> values = new ArrayList<>();
        private String justifyFlag = "";
        private int typeCategory = 0;

        public Column(String label, int type, String typeName) {
            this.label = label;
            this.type = type;
            this.typeName = typeName;
        }

        public String getLabel() {
            return label;
        }

        public int getType() {
            return type;
        }

        public String getTypeName() {
            return typeName;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void addValue(String value) {
            values.add(value);
        }

        public String getValue(int i) {
            return values.get(i);
        }

        public String getJustifyFlag() {
            return justifyFlag;
        }

        public void justifyLeft() {
            this.justifyFlag = "-";
        }

        public int getTypeCategory() {
            return typeCategory;
        }

        public void setTypeCategory(int typeCategory) {
            this.typeCategory = typeCategory;
        }
    }

    private class ResultSetDataSource implements DataSource {
        private final ResultSet rs;
        private final ResultSetMetaData rsmd;

        public ResultSetDataSource(ResultSet rs) throws Exception {
            this.rs = rs;
            this.rsmd = rs.getMetaData();
        }

        @Override
        public int getColumnCount() throws Exception {
            return rsmd.getColumnCount();
        }

        @Override
        public String getColumnLabel(int i) throws Exception {
            return rsmd.getColumnLabel(i + 1);
        }

        @Override
        public int getColumnType(int i) throws Exception {
            return rsmd.getColumnType(i + 1);
        }

        @Override
        public String getColumnTypeName(int i) throws Exception {
            return rsmd.getColumnTypeName(i + 1);
        }

        @Override
        public String getTableName(int i) throws Exception {
            return rsmd.getTableName(i + 1);
        }

        @Override
        public boolean next() throws Exception {
            return rs.next();
        }

        @Override
        public String getString(int i) throws Exception {
            return rs.getString(i + 1);
        }

        @Override
        public Double getDouble(int i) throws Exception {
            return rs.getDouble(i + 1);
        }
    }

    public void printResultSet(ResultSet rs) throws Exception {
        printDataSource(new ResultSetDataSource(rs), -1);
    }

    public void printDataSource(DataSource dataSource, int maxRows) throws Exception {
        int maxStringColWidth = DEFAULT_MAX_TEXT_COL_WIDTH;

        int columnCount = dataSource.getColumnCount();
        final List<Column> columns = new ArrayList<>(columnCount);
        final List<String> tableNames = new ArrayList<>(columnCount);

        for (int i = 0; i < columnCount; i++) {
            Column c = new Column(dataSource.getColumnLabel(i),
                    dataSource.getColumnType(i), dataSource.getColumnTypeName(i));
            c.setWidth(c.getLabel().length());
            c.setTypeCategory(whichCategory(c.getType()));
            columns.add(c);

            if (!tableNames.contains(dataSource.getTableName(i)))
                tableNames.add(dataSource.getTableName(i));
        }

        int rowCount = 0;
        while (dataSource.next()) {
            for (int i = 0; i < columnCount; i++) {
                Column c = columns.get(i);
                String value;
                int category = c.getTypeCategory();

                if (category == CATEGORY_OTHER) {
                    value = "(" + c.getTypeName() + ")";
                } else {
                    value = dataSource.getString(i) == null ? "NULL" : dataSource.getString(i);
                }
                switch (category) {
                    case CATEGORY_DOUBLE:

                        if (!value.equals("NULL")) {
                            Double dValue = dataSource.getDouble(i);
                            value = String.format("%.3f", dValue);
                        }

                        break;

                    case CATEGORY_STRING:

                        c.justifyLeft();

                        if (value.length() > maxStringColWidth)
                            value = value.substring(0, maxStringColWidth - 3) + "...";


                        break;
                }

                c.setWidth(value.length() > c.getWidth() ? value.length() : c.getWidth());
                c.addValue(value);
            }

            rowCount++;

            if (maxRows != -1 && rowCount >= maxRows)
                break;
        }

        final StringBuilder strToPrint = new StringBuilder();
        final StringBuilder rowSeparator = new StringBuilder();

        for (Column c : columns) {
            int width = c.getWidth();

            String toPrint;
            String name = c.getLabel();
            int diff = width - name.length();

            if ((diff % 2) == 1) {
                width++;
                diff++;
                c.setWidth(width);
            }

            int paddingSize = diff / 2;

            String padding = new String(new char[paddingSize]).replace("\0", " ");
            toPrint = "| " + padding + name + padding + " ";

            strToPrint.append(toPrint);

            rowSeparator.append("+");
            rowSeparator.append(new String(new char[width + 2]).replace("\0", "-"));
        }

        String lineSeparator = System.getProperty("line.separator");
        lineSeparator = lineSeparator == null ? "\n" : lineSeparator;

        rowSeparator.append("+").append(lineSeparator);

        strToPrint.append("|").append(lineSeparator);
        strToPrint.insert(0, rowSeparator);
        strToPrint.append(rowSeparator);

        GroovyStringJoiner sj = new GroovyStringJoiner(", ");
        for (String name : tableNames) {
            sj.add(name);
        }

        String info = "Printing " + rowCount;
        info += rowCount > 1 ? " rows from " : " row from ";
        info += tableNames.size() > 1 ? "tables " : "table ";
        info += sj.toString();

        System.out.println(info);
        System.out.print(strToPrint.toString());

        String format;

        for (int i = 0; i < rowCount; i++) {
            for (Column c : columns) {
                format = String.format("| %%%s%ds ", c.getJustifyFlag(), c.getWidth());
                System.out.print(String.format(format, c.getValue(i)));
            }

            System.out.println("|");
            System.out.print(rowSeparator);
        }

        System.out.println();
        System.out.println("Printed " + rowCount + " row(s)");
    }

    private int whichCategory(int type) {
        switch (type) {
            case Types.BIGINT:
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                return CATEGORY_INTEGER;

            case Types.REAL:
            case Types.DOUBLE:
            case Types.DECIMAL:
                return CATEGORY_DOUBLE;

            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                return CATEGORY_DATETIME;

            case Types.BOOLEAN:
                return CATEGORY_BOOLEAN;

            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CHAR:
            case Types.NCHAR:
                return CATEGORY_STRING;

            default:
                return CATEGORY_OTHER;
        }
    }
}