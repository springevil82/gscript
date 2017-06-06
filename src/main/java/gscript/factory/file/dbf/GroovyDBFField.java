package gscript.factory.file.dbf;

import java.sql.Types;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class GroovyDBFField {

    private static final SimpleDateFormat DATE_FORMAT_YYYYMMDD = new SimpleDateFormat("yyyyMMdd");

    private String name;
    private char type;
    private int length;
    private int decimalCount;

    /**
     * Constructor
     *
     * @param name         name of field (max len 10)
     * @param type         DBF type (available types are: 'C' - character, 'N' - number, 'L' - logical, 'D' - double, 'F' - float)
     * @param length       length of data type
     * @param decimalCount decimalCount
     */
    public GroovyDBFField(String name, char type, int length, int decimalCount) throws GroovyDBFException {
        if (name.length() > 10)
            throw new GroovyDBFException("The field name is more than 10 characters long: " + name);

        if (type != 'C' && type != 'N' && type != 'L' && type != 'D' && type != 'F' && type != ' ')
            throw new GroovyDBFException("The field type is not a valid. Got: \"" + type + "\"");

        if (length < 1)
            throw new GroovyDBFException("The field length should be a positive integer. Got: " + length);

        if (type == 'L' && length != 1)
            throw new GroovyDBFException("The field length should be 1 character for logical fields. Got: " + length);

        if (type == 'D' && length != 8)
            throw new GroovyDBFException("The field length should be 8 characaters for date fields. Got: " + length);

        if (decimalCount < 0)
            throw new GroovyDBFException("The field decimal count should not be a negative integer. Got: " + decimalCount);

        if ((type == 'C' || type == 'L' || type == 'D') && (decimalCount != 0))
            throw new GroovyDBFException("The field decimal count should be 0 for character, logical, and date fields. Got: " + decimalCount);

        if (decimalCount > length - 1)
            throw new GroovyDBFException("The field decimal count should be less than the length - 1. Got: " + decimalCount);

        this.name = name;
        this.type = type;
        this.length = length;
        this.decimalCount = decimalCount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(char type) {
        this.type = type;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setDecimalCount(int decimalCount) {
        this.decimalCount = decimalCount;
    }

    public String getName() {
        return name;
    }

    public char getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getDecimalCount() {
        return decimalCount;
    }

    public String format(Object obj) throws GroovyDBFException {
        if (type == 'N' || type == 'F') {

            if (obj == null) {
                final StringBuilder stringBuilder = new StringBuilder(getLength());
                for (int i = 0; i < getLength(); i++)
                    stringBuilder.append(" ");

                return stringBuilder.toString();
            }

            if (obj instanceof Number) {
                final Number number = (Number) obj;
                final StringBuilder stringBuilder = new StringBuilder(getLength());

                for (int i = 0; i < getLength(); i++)
                    stringBuilder.append("#");

                if (getDecimalCount() > 0)
                    stringBuilder.setCharAt(getLength() - getDecimalCount() - 1, '.');

                final DecimalFormat decimalformat = new DecimalFormat(stringBuilder.toString());
                String s1 = decimalformat.format(number).replace(',', '.');
                int k = getLength() - s1.length();

                if (k < 0)
                    throw new GroovyDBFException("Value " + number + " cannot fit in pattern: '" + stringBuilder + "'.");

                final StringBuilder stringBuilder1 = new StringBuilder(k);
                for (int l = 0; l < k; l++)
                    stringBuilder1.append(" ");

                return stringBuilder1 + s1;
            } else
                throw new GroovyDBFException("Expected a Number, got " + obj.getClass() + ".");
        }

        if (type == 'C') {
            if (obj == null)
                obj = "";

            if (obj instanceof String) {
                String s = (String) obj;

                if (s.length() > getLength())
                    throw new GroovyDBFException("'" + obj + "' is longer than " + getLength() + " characters.");

                StringBuilder stringBuilder = new StringBuilder(getLength() - s.length());
                for (int j = 0; j < (getLength() - s.length()); j++)
                    stringBuilder.append(' ');

                return s + stringBuilder;
            } else
                throw new GroovyDBFException("Expected a String, got " + obj.getClass() + ".");
        }

        if (type == 'L') {
            if (obj == null)
                obj = Boolean.FALSE;

            if (obj instanceof Boolean) {
                Boolean b = (Boolean) obj;

                return b ? "Y" : "N";
            } else
                throw new GroovyDBFException("Expected a Boolean, got " + obj.getClass() + ".");
        }

        if (type == 'D') {
            if (obj == null)
                return "        ";

            if (obj instanceof Date) {
                Date date = (Date) obj;
                return DATE_FORMAT_YYYYMMDD.format(date);
            } else {
                throw new GroovyDBFException("Expected a Date, got " + obj.getClass() + ".");
            }
        } else {
            throw new GroovyDBFException("Unrecognized JDBFField type: " + type);
        }
    }

    public int getSQLType() {
        switch (type) {
            case 'N':
            case 'F':
                if (getDecimalCount() == 0)
                    return Types.INTEGER;
                else
                    return Types.DOUBLE;
            case 'C':
                return Types.VARCHAR;
            case 'L':
                return Types.BOOLEAN;
            case 'D':
                return Types.DATE;
        }

        return Types.VARCHAR;
    }

    public String getColumnTypeName() {
        switch (type) {
            case 'N':
            case 'F':
                return "Numeric(" + getLength() + "," + getDecimalCount() + ")";
            case 'L':
                return "Logic";
            case 'D':
                return "Date";
        }

        return "Character(" + getLength() + ")";
    }

    public Class getClassForType() {
        if (type == 'N' || type == 'F') {
            if (getDecimalCount() == 0)
                return Long.class;
            else
                return Double.class;
        }

        if (type == 'C')
            return String.class;

        if (type == 'L')
            return Boolean.class;

        if (type == 'D')
            return Date.class;

        return Class.class;
    }

    public Object parse(String s) throws GroovyDBFException {
        s = s.trim();

        if (type == 'N' || type == 'F') {
            if (s.equals(""))
                s = "0";

            try {
                if (getDecimalCount() == 0)
                    return new Long(s);
                else
                    return new Double(s);

            } catch (NumberFormatException numberformatexception) {
                throw new GroovyDBFException(numberformatexception);
            }
        }

        if (type == 'C')
            return s;

        if (type == 'L') {
            if (s.equals("N") || s.equals("n") || s.equals("F") || s.equals("f") || s.equals(""))
                return Boolean.FALSE;

            if (s.equals("Y") || s.equals("y") || s.equals("T") || s.equals("t"))
                return Boolean.TRUE;

            throw new GroovyDBFException("Unrecognized value for logical field: " + s);
        }

        if (type == 'D') {
            try {
                if ("".equals(s))
                    return null;
                else
                    return DATE_FORMAT_YYYYMMDD.parse(s);
            } catch (ParseException parseexception) {
                throw new GroovyDBFException(parseexception);
            }
        } else {
            throw new GroovyDBFException("Unrecognized JDBFField type: " + type);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String toString() {
        return name;
    }
}