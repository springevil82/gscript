package gscript.factory.document;

import gscript.Factory;
import gscript.GroovyException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;

public final class GroovyMultilineDocumentCSVSerializer {

    private final Factory factory;

    public GroovyMultilineDocumentCSVSerializer(Factory factory) {
        this.factory = factory;
    }

    private Object parseObject(String objectAsString) {
        if (objectAsString == null)
            return null;

        // match datetime
        final Matcher cdataMatcher = RegExp.CDATA_PATTERN.matcher(objectAsString);
        if (cdataMatcher.find())
            return cdataMatcher.group(1);

        // match datetime
        final Matcher dateTimeMatcher = RegExp.DATETIME_PATTERN.matcher(objectAsString);
        if (dateTimeMatcher.find())
            return factory.date.createDate(dateTimeMatcher.group(3), dateTimeMatcher.group(2), dateTimeMatcher.group(1), dateTimeMatcher.group(4), dateTimeMatcher.group(5), dateTimeMatcher.group(6));

        // match date
        final Matcher dateMatcher = RegExp.DATE_PATTERN.matcher(objectAsString);
        if (dateMatcher.find())
            return factory.date.createDate(dateMatcher.group(3), dateMatcher.group(2), dateMatcher.group(1));

        // match decimal
        final Matcher decimalMatcher = RegExp.DECIMAL_PATTERN.matcher(objectAsString);
        if (decimalMatcher.find())
            return new BigDecimal(Double.parseDouble(objectAsString)).setScale(2, BigDecimal.ROUND_HALF_UP);

        // match int and long
        final Matcher intMatcher = RegExp.INT_PATTERN.matcher(objectAsString);
        if (intMatcher.find()) {
            final Long asLong = Long.parseLong(objectAsString);

            if (asLong > Integer.MAX_VALUE)
                return asLong;
            else
                return asLong.intValue();
        }

        // match boolean
        final Matcher boolMatcher = RegExp.BOOLEAN_PATTERN.matcher(objectAsString);
        if (boolMatcher.find())
            return Boolean.parseBoolean(boolMatcher.group(1));

        return objectAsString;
    }

    private String objectAsString(Object value) {
        if (value == null)
            return null;

        if (value instanceof Date) {
            final DateTime dateTime = new DateTime(value);
            if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0) {
                return dateTime.toString("dd.MM.yyyy");
            } else {
                return dateTime.toString("dd.MM.yyyy HH:mm:ss");
            }
        } else if (value instanceof Number) {
            String valueAsString = value.toString();

            // floating point is always dot
            if (valueAsString.contains(","))
                valueAsString = valueAsString.replace(",", ".");

            return valueAsString;
        } else {
            return value.toString();
        }
    }

    public CSVFormat getCSVFormat() {
        return CSVFormat.DEFAULT;
    }

    public void saveToFile(GroovyMultilineDocument doc, File file, String encoding) {
        final Set<String> fields = new LinkedHashSet<>();
        for (GroovyMultilineDocument.Line line : doc.getLines())
            for (String fieldName : line.keySet())
                fields.add(fieldName);

        try (PrintStream printStream = new PrintStream(file, encoding)) {

            // head values
            for (String headName : doc.keySet())
                printStream.println(headName + ": " + objectAsString(doc.get(headName)));

            printStream.println();

            final CSVPrinter csvPrinter = new CSVPrinter(printStream, getCSVFormat());

            // columns
            csvPrinter.printRecord(fields);

            // lines
            for (GroovyMultilineDocument.Line line : doc.getLines()) {
                final List<String> values = new ArrayList<>();

                for (String column : fields)
                    values.add(objectAsString(line.get(column)));

                csvPrinter.printRecord(values);
            }

        } catch (Exception e) {
            throw new GroovyException("CSV serialization error: " + e.getMessage(), e);
        }
    }

    public void loadFromFile(final GroovyMultilineDocument doc, File file, String encoding) {
        String line;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            while ((line = bufferedReader.readLine()) != null) {

                if ("".equals(line.trim())) {
                    final CSVParser csvParser = new CSVParser(bufferedReader, getCSVFormat());

                    List<String> columns = null;
                    for (CSVRecord record : csvParser) {
                        if (columns == null) {
                            columns = new ArrayList<>();
                            for (int i = 0; i < record.size(); i++)
                                columns.add(record.get(i));
                        } else {
                            final GroovyMultilineDocument.Line docLine = doc.createLine();
                            for (int i = 0; i < columns.size(); i++)
                                docLine.put(columns.get(i), parseObject(record.get(i)));
                        }
                    }

                    break;
                } else {
                    final int indexOfParameter = line.indexOf(": ");
                    if (indexOfParameter != -1)
                        doc.put(line.substring(0, indexOfParameter), parseObject(line.substring(indexOfParameter + 2)));
                }
            }
        } catch (Exception e) {
            throw new GroovyException("CSV load error: " + e.getMessage(), e);
        }
    }

}
