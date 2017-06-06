package gscript.factory.file.dbf;

import java.io.*;
import java.util.Calendar;

public final class GroovyDBFWriter {

    private BufferedOutputStream outputStream;
    private int recordCount;
    private GroovyDBFField[] fields;
    private String fileName;
    private final String dbfEncoding;

    public GroovyDBFWriter(String fileName, GroovyDBFField[] fields) throws GroovyDBFException {
        outputStream = null;
        recordCount = 0;
        this.fields = null;
        dbfEncoding = null;
        this.fileName = fileName;

        try {
            init(new FileOutputStream(fileName), fields);
        } catch (FileNotFoundException e) {
            throw new GroovyDBFException(e);
        }
    }

    public GroovyDBFWriter(OutputStream outputStream, GroovyDBFField[] fields, String encoding) throws GroovyDBFException {
        this.outputStream = null;
        recordCount = 0;
        this.fields = null;
        fileName = null;
        dbfEncoding = encoding;
        init(outputStream, fields);
    }

    public GroovyDBFWriter(OutputStream outputStream, GroovyDBFField[] fields, String encoding, int rowsCount) throws GroovyDBFException {
        this.outputStream = null;
        recordCount = 0;
        this.fields = null;
        fileName = null;
        dbfEncoding = encoding;
        init(outputStream, fields, rowsCount);
    }

    public GroovyDBFWriter(String fileName, GroovyDBFField[] fields, String encoding) throws GroovyDBFException {
        outputStream = null;
        recordCount = 0;
        this.fields = null;
        this.fileName = fileName;
        dbfEncoding = encoding;

        try {
            init(new FileOutputStream(fileName), fields);
        } catch (FileNotFoundException e) {
            throw new GroovyDBFException(e);
        }
    }

    private void init(OutputStream outputStream, GroovyDBFField[] fields) throws GroovyDBFException {
        this.fields = fields;

        try {
            this.outputStream = new BufferedOutputStream(outputStream);
            writeHeader();

            for (int i = 0; i < fields.length; i++)
                writeFieldHeader(fields[i]);

            this.outputStream.write(13);
            this.outputStream.flush();
        } catch (Exception e) {
            throw new GroovyDBFException(e);
        }
    }

    private void init(OutputStream outputStream, GroovyDBFField[] fields, int rowCount) throws GroovyDBFException {
        this.fields = fields;

        try {
            this.outputStream = new BufferedOutputStream(outputStream);
            writeHeader(rowCount);

            for (int i = 0; i < fields.length; i++)
                writeFieldHeader(fields[i]);

            this.outputStream.write(13);
            this.outputStream.flush();
        } catch (Exception e) {
            throw new GroovyDBFException(e);
        }
    }

    private void writeHeader() throws IOException {
        byte[] abyte0 = new byte[16];
        abyte0[0] = 3;

        final Calendar calendar = Calendar.getInstance();
        abyte0[1] = (byte) (calendar.get(1) % 100);
        abyte0[2] = (byte) (calendar.get(2) + 1);
        abyte0[3] = (byte) calendar.get(5);
        abyte0[4] = 0;
        abyte0[5] = 0;
        abyte0[6] = 0;
        abyte0[7] = 0;

        int i = ((fields.length + 1) * 32) + 1;
        abyte0[8] = (byte) (i % 256);
        abyte0[9] = (byte) (i / 256);

        int j = 1;

        for (int k = 0; k < fields.length; k++)
            j += fields[k].getLength();

        abyte0[10] = (byte) (j % 256);
        abyte0[11] = (byte) (j / 256);
        abyte0[12] = 0;
        abyte0[13] = 0;
        abyte0[14] = 0;
        abyte0[15] = 0;
        outputStream.write(abyte0, 0, abyte0.length);

        for (int l = 0; l < 16; l++)
            abyte0[l] = 0;

        outputStream.write(abyte0, 0, abyte0.length);
    }

    private void writeHeader(int recordCount) throws IOException {
        byte[] abyte0 = new byte[16];
        abyte0[0] = 3;

        final Calendar calendar = Calendar.getInstance();
        abyte0[1] = (byte) (calendar.get(1) % 100);
        abyte0[2] = (byte) (calendar.get(2) + 1);
        abyte0[3] = (byte) calendar.get(5);
        abyte0[4] = (byte) (recordCount % 256);
        abyte0[5] = (byte) ((recordCount / 256) % 256);
        abyte0[6] = (byte) ((recordCount / 0x10000) % 256);
        abyte0[7] = (byte) ((recordCount / 0x1000000) % 256);

        int i = ((fields.length + 1) * 32) + 1;
        abyte0[8] = (byte) (i % 256);
        abyte0[9] = (byte) (i / 256);

        int j = 1;

        for (int k = 0; k < fields.length; k++)
            j += fields[k].getLength();

        abyte0[10] = (byte) (j % 256);
        abyte0[11] = (byte) (j / 256);
        abyte0[12] = 0;
        abyte0[13] = 0;
        abyte0[14] = 0;
        abyte0[15] = 0;
        outputStream.write(abyte0, 0, abyte0.length);

        for (int l = 0; l < 16; l++)
            abyte0[l] = 0;

        outputStream.write(abyte0, 0, abyte0.length);
    }

    private void writeFieldHeader(GroovyDBFField field) throws IOException {
        byte[] abyte0 = new byte[16];
        String s = field.getName();
        int i = s.length();

        if (i > 10)
            i = 10;

        for (int j = 0; j < i; j++)
            abyte0[j] = (byte) s.charAt(j);

        for (int k = i; k <= 10; k++)
            abyte0[k] = 0;

        abyte0[11] = (byte) field.getType();
        abyte0[12] = 0;
        abyte0[13] = 0;
        abyte0[14] = 0;
        abyte0[15] = 0;
        outputStream.write(abyte0, 0, abyte0.length);

        for (int l = 0; l < 16; l++)
            abyte0[l] = 0;

        abyte0[0] = (byte) field.getLength();
        abyte0[1] = (byte) field.getDecimalCount();
        outputStream.write(abyte0, 0, abyte0.length);
    }

    public void addRecord(Object[] values) throws GroovyDBFException {
        if (values.length != fields.length)
            throw new GroovyDBFException("Error adding record: Wrong number of values. Expected " + fields.length + ", got " + values.length + ".");

        int i = 0;

        for (int j = 0; j < fields.length; j++)
            i += fields[j].getLength();

        byte[] abyte0 = new byte[i];
        int k = 0;

        String s;
        for (int l = 0; l < fields.length; l++) {
            s = fields[l].format(values[l]);
            byte[] abyte1;

            try {
                if (dbfEncoding != null)
                    abyte1 = s.getBytes(dbfEncoding);
                else
                    abyte1 = s.getBytes();
            } catch (UnsupportedEncodingException unsupportedencodingexception) {
                throw new GroovyDBFException(unsupportedencodingexception);
            }

            for (int i1 = 0; i1 < fields[l].getLength(); i1++)
                abyte0[k + i1] = abyte1[i1];

            k += fields[l].getLength();
        }

        try {
            outputStream.write(32);
            outputStream.write(abyte0, 0, abyte0.length);
            outputStream.flush();
        } catch (IOException e) {
            throw new GroovyDBFException(e);
        }

        recordCount++;
    }

    public void close() throws GroovyDBFException {
        try {
            outputStream.write(26);
            outputStream.close();

            if (fileName != null) {
                RandomAccessFile randomaccessfile =
                        new RandomAccessFile(fileName, "rw");
                randomaccessfile.seek(4L);

                byte[] abyte0 = new byte[4];
                abyte0[0] = (byte) (recordCount % 256);
                abyte0[1] = (byte) ((recordCount / 256) % 256);
                abyte0[2] = (byte) ((recordCount / 0x10000) % 256);
                abyte0[3] = (byte) ((recordCount / 0x1000000) % 256);
                randomaccessfile.write(abyte0, 0, abyte0.length);
                randomaccessfile.close();
            }
        } catch (IOException e) {
            throw new GroovyDBFException(e);
        }
    }
}