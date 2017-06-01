package gscript.factory.file.dbf;

import java.io.*;
import java.util.Calendar;


// Referenced classes of package com.hexiong.jdbf:
//            JDBFException, JDBField
@SuppressWarnings({"ManualArrayCopy", "ForLoopReplaceableByForEach", "UnusedDeclaration", "MagicConstant"})
public class GroovyDBFWriter {

    private BufferedOutputStream stream;
    private int recCount;
    private GroovyDBFField[] fields;
    private String fileName;
    private final String dbfEncoding;

    public GroovyDBFWriter(String fileName, GroovyDBFField[] fields) throws GroovyDBFException {
        stream = null;
        recCount = 0;
        this.fields = null;
        dbfEncoding = null;
        this.fileName = fileName;

        try {
            init(new FileOutputStream(fileName), fields);
        } catch (FileNotFoundException filenotfoundexception) {
            throw new GroovyDBFException(filenotfoundexception);
        }
    }

    public GroovyDBFWriter(OutputStream outputstream, GroovyDBFField[] fields, String encoding) throws GroovyDBFException {
        stream = null;
        recCount = 0;
        this.fields = null;
        fileName = null;
        dbfEncoding = encoding;
        init(outputstream, fields);
    }

    public GroovyDBFWriter(OutputStream outputstream, GroovyDBFField[] fields, String encoding, int rowsCount) throws GroovyDBFException {
        stream = null;
        recCount = 0;
        this.fields = null;
        fileName = null;
        dbfEncoding = encoding;
        init(outputstream, fields, rowsCount);
    }

    public GroovyDBFWriter(String fileName, GroovyDBFField[] fields, String encoding) throws GroovyDBFException {
        stream = null;
        recCount = 0;
        this.fields = null;
        this.fileName = fileName;
        dbfEncoding = encoding;

        try {
            init(new FileOutputStream(fileName), fields);
        } catch (FileNotFoundException filenotfoundexception) {
            throw new GroovyDBFException(filenotfoundexception);
        }
    }

    private void init(OutputStream outputstream, GroovyDBFField[] fields) throws GroovyDBFException {
        this.fields = fields;

        try {
            stream = new BufferedOutputStream(outputstream);
            writeHeader();

            for (int i = 0; i < fields.length; i++)
                writeFieldHeader(fields[i]);

            stream.write(13);
            stream.flush();
        } catch (Exception exception) {
            throw new GroovyDBFException(exception);
        }
    }

    private void init(OutputStream outputstream, GroovyDBFField[] fields, int rowCount) throws GroovyDBFException {
        this.fields = fields;

        try {
            stream = new BufferedOutputStream(outputstream);
            writeHeader(rowCount);

            for (int i = 0; i < fields.length; i++)
                writeFieldHeader(fields[i]);

            stream.write(13);
            stream.flush();
        } catch (Exception exception) {
            throw new GroovyDBFException(exception);
        }
    }

    private void writeHeader() throws IOException {
        byte[] abyte0 = new byte[16];
        abyte0[0] = 3;

        Calendar calendar = Calendar.getInstance();
/*

        так было до 09.01.14
        abyte0[1] = (byte) (calendar.get(1) - 1900);
        abyte0[2] = (byte) calendar.get(2);
*/
        abyte0[1] = (byte) (calendar.get(1) % 100);     // так выгружает МАП
        abyte0[2] = (byte) (calendar.get(2) + 1);       // месяц начинается с 0, МАП выгружает с 1
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
        stream.write(abyte0, 0, abyte0.length);

        for (int l = 0; l < 16; l++)
            abyte0[l] = 0;

        stream.write(abyte0, 0, abyte0.length);
    }

    private void writeHeader(int rCount) throws IOException {
        byte[] abyte0 = new byte[16];
        abyte0[0] = 3;

        Calendar calendar = Calendar.getInstance();
/*

        так было до 09.01.14
        abyte0[1] = (byte) (calendar.get(1) - 1900);
        abyte0[2] = (byte) calendar.get(2);
*/
        abyte0[1] = (byte) (calendar.get(1) % 100);     // так выгружает МАП
        abyte0[2] = (byte) (calendar.get(2) + 1);       // месяц начинается с 0, МАП выгружает с 1
        abyte0[3] = (byte) calendar.get(5);
        abyte0[4] = (byte) (rCount % 256);
        abyte0[5] = (byte) ((rCount / 256) % 256);
        abyte0[6] = (byte) ((rCount / 0x10000) % 256);
        abyte0[7] = (byte) ((rCount / 0x1000000) % 256);

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
        stream.write(abyte0, 0, abyte0.length);

        for (int l = 0; l < 16; l++)
            abyte0[l] = 0;

        stream.write(abyte0, 0, abyte0.length);
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
        stream.write(abyte0, 0, abyte0.length);

        for (int l = 0; l < 16; l++)
            abyte0[l] = 0;

        abyte0[0] = (byte) field.getLength();
        abyte0[1] = (byte) field.getDecimalCount();
        stream.write(abyte0, 0, abyte0.length);
    }

    public void addRecord(Object[] values) throws GroovyDBFException {
        if (values.length != fields.length)
            throw new GroovyDBFException(
                    "Error adding record: Wrong number of values. Expected " +
                            fields.length + ", got " + values.length + "."
            );

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
            stream.write(32);
            stream.write(abyte0, 0, abyte0.length);
            stream.flush();
        } catch (IOException ioexception) {
            throw new GroovyDBFException(ioexception);
        }

        recCount++;
    }

    public void close() throws GroovyDBFException {
        try {
            stream.write(26);
            stream.close();

            if (fileName != null) {
                RandomAccessFile randomaccessfile =
                        new RandomAccessFile(fileName, "rw"); //NON-NLS
                randomaccessfile.seek(4L);

                byte[] abyte0 = new byte[4];
                abyte0[0] = (byte) (recCount % 256);
                abyte0[1] = (byte) ((recCount / 256) % 256);
                abyte0[2] = (byte) ((recCount / 0x10000) % 256);
                abyte0[3] = (byte) ((recCount / 0x1000000) % 256);
                randomaccessfile.write(abyte0, 0, abyte0.length);
                randomaccessfile.close();
            }
        } catch (IOException ioexception) {
            throw new GroovyDBFException(ioexception);
        }
    }
}