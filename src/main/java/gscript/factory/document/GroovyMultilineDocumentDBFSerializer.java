package gscript.factory.document;

import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.file.dbf.GroovyDBFField;
import gscript.factory.file.dbf.GroovyDBFFileReader;
import gscript.factory.file.dbf.GroovyDBFWriter;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

public final class GroovyMultilineDocumentDBFSerializer {

    private final Factory factory;

    public GroovyMultilineDocumentDBFSerializer(Factory factory) {
        this.factory = factory;
    }

    private GroovyDBFField findField(Set<GroovyDBFField> fields, String fieldName) {
        for (GroovyDBFField field : fields)
            if (field.getName().equals(fieldName))
                return field;

        return null;
    }

    public void saveToFile(GroovyMultilineDocument doc, File file, String encoding) {
        try {
            GroovyDBFField[] fields = new GroovyDBFField[0];

            for (GroovyMultilineDocument.Column column : doc.getColumns().values()) {
                if (column.getJavaClass() == Long.class)
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'N', column.getSize() != null ? column.getSize() : 18, column.getScale() != null ? column.getScale() : 0));
                else if (column.getJavaClass() == Integer.class)
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'N', column.getSize() != null ? column.getSize() : 9, column.getScale() != null ? column.getScale() : 0));
                else if (column.getJavaClass() == Double.class)
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'N', column.getSize() != null ? column.getSize() : 16, column.getScale() != null ? column.getScale() : 2));
                else if (column.getJavaClass() == Float.class)
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'N', column.getSize() != null ? column.getSize() : 16, column.getScale() != null ? column.getScale() : 2));
                else if (column.getJavaClass() == BigDecimal.class)
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'N', column.getSize() != null ? column.getSize() : 16, column.getScale() != null ? column.getScale() : 2));
                else if (column.getJavaClass() == Boolean.class)
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'L', 1, 0));
                else if (Date.class.isAssignableFrom(column.getJavaClass()))
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'D', 8, 0));
                else
                    fields = ArrayUtils.add(fields, new GroovyDBFField(column.getName(), 'C', column.getSize() != null ? column.getSize() : 254, column.getScale() != null ? column.getScale() : 0));
            }

            try (OutputStream outputStream = new FileOutputStream(file)) {
                final GroovyDBFWriter writer = new GroovyDBFWriter(new BufferedOutputStream(outputStream),
                        fields,
                        encoding,
                        doc.getLines().size());

                try {
                    for (GroovyMultilineDocument.Line line : doc.getLines()) {
                        Object[] values = new Object[0];

                        for (String columnName : doc.getColumnNames())
                            values = ArrayUtils.add(values, line.get(columnName));

                        writer.addRecord(values);
                    }
                } finally {
                    writer.close();
                }
            }
        } catch (Exception e) {
            throw new GroovyException("DBF export error: " + e.getMessage(), e);
        }
    }

    private Object getRecordValue(Object recordValue) {
        return recordValue;
    }

    public void loadFromFile(final GroovyMultilineDocument doc, File file, String encoding) {
        try {
            try (GroovyDBFFileReader dbfFileReader = factory.file.createDBFFileReader(file, encoding)) {
                while (dbfFileReader.next()) {
                    final GroovyMultilineDocument.Line line = doc.createLine();

                    for (int i = 0; i < dbfFileReader.getColumnCount(); i++)
                        line.put(dbfFileReader.getColumnName(i), getRecordValue(dbfFileReader.getObject(i)));
                }
            }
        } catch (Throwable e) {
            throw new GroovyException("DBF import error: " + e.getMessage(), e);
        }
    }

}
