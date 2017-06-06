package gscript.factory.document;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gscript.Factory;
import gscript.GroovyException;
import org.joda.time.DateTime;

import java.util.*;

/**
 * Represent of multiline (multi-dimensional) document.
 * <p>
 * Contains header (Map<String, Object>) and collection of lines (Map<String, Object>).
 * Also it can contains columns mapping, for easily exporting to DBF/CSV files.
 */
public final class GroovyMultilineDocument extends LinkedHashMap<String, Object> {

    private final Factory factory;

    public GroovyMultilineDocument(Factory factory) {
        this.factory = factory;
    }

    private final LinkedHashMap<String, Column> columns = new LinkedHashMap<>();

    /**
     * Column definition
     */
    public final class Column {
        private String name;
        private String title;
        private Class javaClass;
        private Integer size;
        private Integer scale;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Class getJavaClass() {
            return javaClass;
        }

        public void setJavaClass(Class javaClass) {
            this.javaClass = javaClass;
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

        @Override
        public String toString() {
            return "title: '" + title + '\'' +
                    ", javaClass:" + javaClass;
        }
    }

    private final List<Line> lines = new ArrayList<>();

    /**
     * Document line. Provides auto creation column definitions.
     */
    public final class Line extends LinkedHashMap<String, Object> {

        @Override
        public Object put(String key, Object value) {

            Column column = columns.get(key);
            if (column == null) {
                column = new Column();
                column.setName(key);
                column.setTitle(key);

                if (value != null)
                    column.setJavaClass(value.getClass());

                columns.put(key, column);
            } else {
                if (column.getJavaClass() == null && value != null) {
                    column.setJavaClass(value.getClass());
                } else if (column.getJavaClass() != null && !column.getJavaClass().equals(Object.class) &&
                        value != null && column.getJavaClass() != value.getClass()) {

                    final Class extendedClass = GroovyUtil.getExtendedClass(column.getJavaClass(), value.getClass());

                    final Class oldColumnClass = column.getJavaClass();
                    column.setJavaClass(extendedClass);

                    if (!extendedClass.equals(Object.class)) {
                        try {
                            if (oldColumnClass == extendedClass) {
                                value = GroovyUtil.cast(value, extendedClass);
                            } else {
                                recastColumnValues(key, extendedClass);
                            }
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            return super.put(key, value);
        }
    }

    /**
     * @return document column definitions
     */
    public LinkedHashMap<String, Column> getColumns() {
        return columns;
    }

    private void recastColumnValues(String columnName, Class toClass) throws Exception {
        for (Line line : lines) {
            Object value = line.get(columnName);
            if (value != null)
                line.put(columnName, GroovyUtil.cast(value, toClass));
        }
    }

    /**
     * @return number of head values
     */
    public int getHeadFieldsCount() {
        return size();
    }

    public void createColumn(String columnName, String columnTitle, Class columnClass, Integer columnSize, Integer columnScale) {
        final Column column = new Column();
        column.setName(columnName);
        column.setJavaClass(columnClass);
        column.setTitle(columnTitle);
        column.setSize(columnSize);
        column.setScale(columnScale);

        columns.put(columnName, column);
    }

    /**
     * Define the column
     *
     * @param columnName  column name
     * @param columnTitle column title (use for printing, etc)
     * @param columnClass column java class
     * @param columnSize  column size
     */
    public void createColumn(String columnName, String columnTitle, Class columnClass, Integer columnSize) {
        createColumn(columnName, columnTitle, columnClass, columnSize, null);
    }

    /**
     * Define the column
     *
     * @param columnName  column name
     * @param columnTitle column title (use for printing, etc)
     * @param columnClass column java class
     */
    public void createColumn(String columnName, String columnTitle, Class columnClass) {
        createColumn(columnName, columnTitle, columnClass, null, null);
    }

    /**
     * @return number of defined columns
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * @return set of defined column names
     */
    public Set<String> getColumnNames() {
        return columns.keySet();
    }

    /**
     * Define column index
     *
     * @param columnName
     * @param index
     */
    public void setColumnIndex(String columnName, int index) {
        throw new GroovyException("todo it");
    }

    /**
     * Get column title
     * @param columnName defined column name
     * @return title
     */
    public String getColumnTitle(String columnName) {
        final Column column = columns.get(columnName);

        if (column != null)
            return column.getTitle();

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    /**
     * Set column title
     * @param columnName defined column name
     * @param title new title
     */
    public void setColumnTitle(String columnName, String title) {
        final Column column = columns.get(columnName);
        if (column != null)
            column.setTitle(title);

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    /**
     * Get column java class
     * @param columnName defined column name
     * @return class of column
     */
    public Class getColumnClass(String columnName) {
        final Column column = columns.get(columnName);

        if (column != null)
            return column.getJavaClass();

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    /**
     * Set column class
     *
     * @param columnName  defined column name
     * @param columnClass new column class
     */
    public void setColumnClass(String columnName, Class columnClass) {
        final Column column = columns.get(columnName);

        if (column != null)
            column.setJavaClass(columnClass);

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    /**
     * Set column size
     * @param columnName defined column name
     * @param size new column size
     */
    public void setColumnSize(String columnName, Integer size) {
        final Column column = columns.get(columnName);

        if (column != null)
            column.setSize(size);

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    /**
     * Get document line by index
     * @param index index of line
     * @return line
     */
    public Line getLine(int index) {
        return lines.get(index);
    }

    public Object getLineValue(int lineIndex, String fieldName) {
        return lines.get(lineIndex).get(fieldName);
    }

    public List<Line> getLines() {
        return lines;
    }

    public int getLinesCount() {
        return lines.size();
    }

    public Line createLine() {
        final Line line = new Line();
        lines.add(line);
        return line;
    }


    public JsonObject toJSON() {
        final JsonObject jsonObject = new JsonObject();

        for (String headerName : keySet()) {
            final Object value = get(headerName);

            if (value instanceof Date) {
                jsonObject.addProperty(headerName, factory.date.getDateAsString((Date) value));
            } else {
                jsonObject.addProperty(headerName, value != null ? value.toString() : null);
            }
        }

        if (!lines.isEmpty()) {
            final JsonArray jsonArray = new JsonArray();
            jsonObject.add("lines", jsonArray);

            for (Line line : lines) {
                final JsonObject element = new JsonObject();

                Object lineValue;
                for (String name : line.keySet()) {
                    lineValue = line.get(name);

                    if (lineValue instanceof Date)
                        element.addProperty(name, factory.date.getDateAsString((Date) lineValue));
                    else if (lineValue instanceof Number)
                        element.addProperty(name, (Number) lineValue);
                    else if (lineValue instanceof Boolean)
                        element.addProperty(name, (Boolean) lineValue);
                    else if (lineValue instanceof Character)
                        element.addProperty(name, (Character) lineValue);
                    else
                        element.addProperty(name, lineValue != null ? lineValue.toString() : null);
                }

                jsonArray.add(element);
            }
        }

        return jsonObject;
    }

    public String toJSONString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(toJSON());
    }

    public void print() {
        System.out.println(toJSONString());
    }

    /**
     * Сохранить документ в XML файл
     *
     * @param file файл или имя файла (если имя файла без пути - то выгрузиться в файл этим с именем рядом с файлом скрипта)
     */
    public void saveToXML(Object file) {
        new GroovyMultilineDocumentXMLSerializer(factory).saveToFile(this, factory.file.getFile(file));
    }

    /**
     * Загрузить документ из XML файла
     *
     * @param file file файл или имя файла
     */
    public void loadFromXML(Object file) {
        new GroovyMultilineDocumentXMLSerializer(factory).loadFromFile(this, factory.file.getFile(file));
    }

    /**
     * Сохранить документ в DBF файл
     *
     * @param file файл или имя файла (если имя файла без пути - то выгрузиться в файл этим с именем рядом с файлом скрипта)
     */
    public void saveToDBF(Object file, String encoding) {
        new GroovyMultilineDocumentDBFSerializer(factory).saveToFile(this, factory.file.getFile(file), encoding);
    }

    /**
     * Загрузить документ из DBF файла
     *
     * @param file file файл или имя файла
     */
    public void loadFromDBF(Object file, String encoding) {
        new GroovyMultilineDocumentDBFSerializer(factory).loadFromFile(this, factory.file.getFile(file), encoding);
    }

    /**
     * Сохранить документ в CSV файл
     *
     * @param file файл или имя файла (если имя файла без пути - то выгрузиться в файл этим с именем рядом с файлом скрипта)
     */
    public void saveToCSV(Object file, String encoding) {
        new GroovyMultilineDocumentCSVSerializer(factory).saveToFile(this, factory.file.getFile(file), encoding);
    }

    /**
     * Загрузить документ из CSV файла
     *
     * @param file file файл или имя файла
     */
    public void loadFromCSV(Object file, String encoding) {
        new GroovyMultilineDocumentCSVSerializer(factory).loadFromFile(this, factory.file.getFile(file), encoding);
    }

    public void assertEquals(GroovyMultilineDocument otherDocument) {
        final Set<String> thisDocHeadFields = keySet();
        final Set<String> otherDocHeadFields = otherDocument.keySet();

        // assert heads
        for (String thisDocHeadName : thisDocHeadFields)
            if (!otherDocHeadFields.contains(thisDocHeadName))
                throw new GroovyException("Assertion failed: otherDocument does not contain field \"" + thisDocHeadName + "\" from thisDocument");

        for (String otherDocHeadName : otherDocHeadFields)
            if (!thisDocHeadFields.contains(otherDocHeadName))
                throw new GroovyException("Assertion failed: thisDocument does not contain field \"" + otherDocHeadName + "\" from this otherDocument");

        for (String thisDocHeadName : thisDocHeadFields) {
            final Object thisDocHeadFieldValue = get(thisDocHeadName);
            final Object otherDocHeadFieldValue = otherDocument.get(thisDocHeadName);

            assertObjects(
                    "thisDocument[" + thisDocHeadName + "]", "otherDocument[" + thisDocHeadName + "]",
                    thisDocHeadFieldValue, otherDocHeadFieldValue);
        }

        // assert lines
        final List<Line> thisDocLines = getLines();
        final List<Line> otherDocLines = otherDocument.getLines();

        if (thisDocLines.size() != otherDocLines.size())
            throw new GroovyException("Assertion failed: thisDocument.getLines().size() != otherDocument.getLines().size() " +
                    "(" + thisDocLines.size() + " != " + otherDocLines.size() + ")");

        Line thisDocLine, otherDocLine;
        Set<String> thisDocLineFields, otherDocLineFields;
        for (int i = 0; i < thisDocLines.size(); i++) {
            thisDocLine = thisDocLines.get(i);
            otherDocLine = otherDocLines.get(i);

            thisDocLineFields = thisDocLine.keySet();
            otherDocLineFields = otherDocLine.keySet();

            for (String thisDocLineName : thisDocLineFields)
                if (!otherDocLineFields.contains(thisDocLineName))
                    throw new GroovyException("Assertion failed: otherDocument.line[" + i + "] does not contain field \"" + thisDocLineName + "\" from thisDocument.line[" + i + "]");

            for (String otherDocLineName : otherDocLineFields)
                if (!thisDocLineFields.contains(otherDocLineName))
                    throw new GroovyException("Assertion failed: thisDocument.line[" + i + "] does not contain field \"" + otherDocLineName + "\" from otherDocument.line[" + i + "]");

            for (String thisDocLineName : thisDocLineFields) {
                final Object thisDocLineFieldValue = thisDocLine.get(thisDocLineName);
                final Object otherDocLineFieldValue = otherDocLine.get(thisDocLineName);

                assertObjects(
                        "thisDocument.line[" + i + "][" + thisDocLineName + "]", "otherDocument.line[" + i + "][" + thisDocLineName + "]",
                        thisDocLineFieldValue, otherDocLineFieldValue);
            }
        }
    }

    private void assertObjects(String object0Name, String object1Name,
                               Object object0Value, Object object1Value) {

        //noinspection StatementWithEmptyBody
        if (object0Value == null && object1Value == null) {
            // do nothing
        } else if (object0Value != null && object1Value == null) {
            throw new GroovyException("Assertion failed: " + object0Name + " != " + object1Name +
                    " (" + object0Value + " != " + object1Value + ")");

        } else if (object0Value == null && object1Value != null) {
            throw new GroovyException("Assertion failed: " + object0Name + " != " + object1Name +
                    " (" + object0Value + " != " + object1Value + ")");
        } else {
            if (!object0Value.getClass().equals(object1Value.getClass()))
                throw new GroovyException("Assertion failed: " + object0Name + ".getClass() != " + object1Name + ".getClass() " +
                        "(" + object0Value.getClass() + " != " + object1Value.getClass() + ")");

            if (object0Value instanceof Comparable && object1Value instanceof Comparable) {

                if (object0Value instanceof Date && object1Value instanceof Date) {
                    object0Value = new DateTime(object0Value).withMillisOfSecond(0).toDate();
                    object1Value = new DateTime(object1Value).withMillisOfSecond(0).toDate();
                }

                //noinspection unchecked
                if (((Comparable) object0Value).compareTo(object1Value) != 0)
                    throw new GroovyException("Assertion failed: " + object0Name + " != " + object1Name +
                            " (" + object0Value + " != " + object1Value + ")");
            } else {
                if (object0Value != object1Value)
                    throw new GroovyException("Assertion failed: " + object0Name + " != " + object1Name +
                            " (" + object0Value + " != " + object1Value + ")");
            }
        }

    }

}
