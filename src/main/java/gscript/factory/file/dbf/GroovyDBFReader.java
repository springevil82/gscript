package gscript.factory.file.dbf;

import org.apache.commons.lang3.ArrayUtils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public final class GroovyDBFReader {

    private String encoding;
    private DataInputStream stream;
    private GroovyDBFField[] fields;
    private byte[] nextRecord;
    private int recordCount;

    public GroovyDBFReader(InputStream inputStream, String encoding) throws GroovyDBFException {
        stream = null;
        fields = null;
        nextRecord = null;
        this.encoding = encoding;
        init(inputStream);
    }

    private void init(InputStream inputstream) throws GroovyDBFException {
        try {
            stream = new DataInputStream(inputstream);

            int i = readHeader();
            fields = new GroovyDBFField[i];

            int j = 1;

            for (int k = 0; k < i; k++) {
                fields[k] = readFieldHeader();
                j += fields[k].getLength();
            }

            if (stream.read() < 1)
                throw new GroovyDBFException("Unexpected end of file reached.");

            int b = stream.read();
            while (b > 0 && (b != 32 && b != 42))
                b = stream.read();

            nextRecord = new byte[j - 1];
            try {
                stream.readFully(nextRecord);

                nextRecord = ArrayUtils.add(nextRecord, 0, (byte) b);
            } catch (EOFException eofexception) {
                nextRecord = null;
                stream.close();
            }
        } catch (IOException e) {
            throw new GroovyDBFException(e);
        }
    }

    public static int readLittleEndianInt(byte[] bytes) throws IOException {
        int bigEndian = 0;
        int current = 0;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8)
            bigEndian |= (bytes[current++] & 0xff) << shiftBy;

        return bigEndian;
    }

    private int readHeader() throws IOException, GroovyDBFException {
        byte[] abyte0 = new byte[16];

        try {
            stream.readFully(abyte0);
        } catch (EOFException eofexception) {
            throw new GroovyDBFException("Unexpected end of file reached.");
        }

        recordCount = readLittleEndianInt(new byte[]{abyte0[4], abyte0[5], abyte0[6], abyte0[7]});

        int i = abyte0[8];

        if (i < 0)
            i += 256;

        i += (256 * abyte0[9]);
        i = --i / 32;
        i--;

        try {
            stream.readFully(abyte0);
        } catch (EOFException e) {
            throw new GroovyDBFException("Unexpected end of file reached.", e);
        }

        return i;
    }

    private GroovyDBFField readFieldHeader() throws IOException, GroovyDBFException {
        byte[] abyte0 = new byte[16];

        try {
            stream.readFully(abyte0);
        } catch (EOFException eofexception) {
            throw new GroovyDBFException("Unexpected end of file reached.");
        }

        final StringBuilder stringBuilder = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            if (abyte0[i] == 0)
                break;

            stringBuilder.append((char) abyte0[i]);
        }

        char c = (char) abyte0[11];

        try {
            stream.readFully(abyte0);
        } catch (EOFException e) {
            throw new GroovyDBFException("Unexpected end of file reached.", e);
        }

        int j = abyte0[0];
        int k = abyte0[1];

        if (j < 0)
            j += 256;

        if (k < 0)
            k += 256;

        return new GroovyDBFField(stringBuilder.toString(), c, j, k);
    }

    public int getFieldCount() {
        return fields.length;
    }

    public GroovyDBFField getField(int i) {
        return fields[i];
    }

    public int getRecordCount() {
        return recordCount;
    }

    public boolean hasNextRecord() {
        return nextRecord != null;
    }

    public Object[] nextRecord() throws GroovyDBFException {
        if (!hasNextRecord())
            throw new GroovyDBFException("No more records available.");

        Object[] obj = new Object[fields.length];
        int i = 1;

        try {
            for (int j = 0; j < obj.length; j++) {
                int k = fields[j].getLength();
                obj[j] = fields[j].parse(new String(nextRecord, i, k, encoding));
                i += fields[j].getLength();
            }

            stream.readFully(nextRecord);
        } catch (EOFException e) {
            nextRecord = null;
        } catch (IOException e) {
            throw new GroovyDBFException(e);
        }

        return obj;
    }

    public int indexOfField(String fieldName) {
        for (int i = 0; i < getFieldCount(); i++)
            if (getField(i).getName().equals(fieldName))
                return i;

        return -1;
    }

    public Column[] getColumns() {
        Column[] fieldNames = new Column[0];

        for (GroovyDBFField field : fields)
            fieldNames = ArrayUtils.add(fieldNames, new Column(
                    field.getName(),
                    field.getSQLType(),
                    field.getColumnTypeName(),
                    field.getClassForType()));

        return fieldNames;
    }

    public void close() throws Exception {
        nextRecord = null;
        stream.close();
    }

    public class Column {
        private final String name;
        private final int sqlType;
        private final String columnTypeName;
        private final Class javaType;

        public Column(String name, int sqlType, String columnTypeName, Class javaType) {
            this.name = name;
            this.sqlType = sqlType;
            this.javaType = javaType;
            this.columnTypeName = columnTypeName;
        }

        public String getName() {
            return name;
        }

        public int getSqlType() {
            return sqlType;
        }

        public Class getJavaType() {
            return javaType;
        }

        public String getColumnTypeName() {
            return columnTypeName;
        }
    }
}
