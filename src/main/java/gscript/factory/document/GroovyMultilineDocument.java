package gscript.factory.document;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gscript.Factory;
import gscript.GroovyException;
import org.joda.time.DateTime;

import java.util.*;

public final class GroovyMultilineDocument extends LinkedHashMap<String, Object> {

    private final Factory factory;

    public GroovyMultilineDocument(Factory factory) {
        this.factory = factory;
    }

    private final LinkedHashMap<String, Column> columns = new LinkedHashMap<>();

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

    public final class Line extends LinkedHashMap<String, Object> {

        @Override
        public Object put(String key, Object value) {

            Column column = columns.get(key);
            if (column == null) {
                // такой колонки еще нет, создадим новую колонку
                column = new Column();
                column.setName(key);
                column.setTitle(key);

                if (value != null)
                    column.setJavaClass(value.getClass());

                columns.put(key, column);
            } else {
                // такая колонка есть, но класс ее не определен и сейчас можно определить
                if (column.getJavaClass() == null && value != null) {
                    column.setJavaClass(value.getClass());
                } else if (column.getJavaClass() != null && !column.getJavaClass().equals(Object.class) &&
                        value != null && column.getJavaClass() != value.getClass()) {

                    // класс текущего значения ячейки не соответсвует классу колонки - расширяем класс колонки
                    final Class extendedClass = GroovyUtil.getExtendedClass(column.getJavaClass(), value.getClass());

                    final Class oldColumnClass = column.getJavaClass();
                    column.setJavaClass(extendedClass);

                    if (!extendedClass.equals(Object.class)) {
                        try {
                            if (oldColumnClass == extendedClass) {
                                // класс колонки остался тот же - закастим текущее значение к классу колонки сразу (перед сетом)
                                value = GroovyUtil.cast(value, extendedClass);
                            } else {
                                // класс колонки поменялся - перекастуем значения всех строк в новому типу
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

    public void createColumn(String columnName, String columnTitle, Class columnClass, Integer columnSize) {
        createColumn(columnName, columnTitle, columnClass, columnSize, null);
    }

    public void createColumn(String columnName, String columnTitle, Class columnClass) {
        createColumn(columnName, columnTitle, columnClass, null, null);
    }

    public int getColumnCount() {
        return columns.size();
    }

    public Set<String> getColumnNames() {
        return columns.keySet();
    }

    public void setColumnIndex(String columnName, int index) {
        throw new GroovyException("todo it");
    }

    public String getColumnTitle(String columnName) {
        final Column column = columns.get(columnName);

        if (column != null)
            return column.getTitle();

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    public void setColumnTitle(String columnName, String title) {
        final Column column = columns.get(columnName);
        if (column != null)
            column.setTitle(title);

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    public Class getColumnClass(String columnName) {
        final Column column = columns.get(columnName);

        if (column != null)
            return column.getJavaClass();

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    public Class setColumnClass(String columnName, Class columnClass) {
        final Column column = columns.get(columnName);

        if (column != null)
            column.setJavaClass(columnClass);

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

    public void setColumnSize(String columnName, Integer size) {
        final Column column = columns.get(columnName);

        if (column != null)
            column.setSize(size);

        throw new GroovyException("Column with name \"" + columnName + "\" not found");
    }

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
