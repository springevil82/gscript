package gscript.factory.document;

import gscript.Factory;
import gscript.GroovyException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.joda.time.DateTime;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;

public final class GroovyMultilineDocumentXMLSerializer {

    private final Factory factory;

    public GroovyMultilineDocumentXMLSerializer(Factory factory) {
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

    private void writeOffset(XMLStreamWriter writer, int offset) throws XMLStreamException {
        for (int i = 0; i < offset; i++)
            writer.writeCharacters(" ");
    }

    private void writeElement(XMLStreamWriter writer, String name, Object value, int offset) throws XMLStreamException {
        if (value == null)
            return;

        writeOffset(writer, offset);
        writer.writeStartElement(name);

        if (value instanceof Date) {

            final DateTime dateTime = new DateTime(value);
            if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0) {
                writer.writeCharacters(dateTime.toString("dd.MM.yyyy"));
            } else {
                writer.writeCharacters(dateTime.toString("dd.MM.yyyy HH:mm:ss"));
            }
        } else if (value instanceof Number) {
            String valueAsString = value.toString();

            // плавающая точка всегда точка
            if (valueAsString.contains(","))
                valueAsString = valueAsString.replace(",", ".");

            writer.writeCharacters(valueAsString);
        } else {
            final String valueAsString = value.toString();

            if (valueAsString.contains("<") || valueAsString.contains(">"))
                writer.writeCData(valueAsString);
            else
                writer.writeCharacters(valueAsString);

        }

        writer.writeEndElement();
    }

    public void saveToFile(GroovyMultilineDocument doc, File file) {
        try (OutputStream outputStream = new FileOutputStream(file)) {

            final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
                    new OutputStreamWriter(outputStream, "UTF-8"));
            try {
                writer.writeStartDocument();
                writer.writeCharacters("\n");
                writer.writeStartElement("document");
                writer.writeCharacters("\n\n");

                for (String headAttr : doc.keySet()) {
                    writeElement(writer, headAttr, doc.get(headAttr), 4);
                    writer.writeCharacters("\n");
                }

                if (!doc.getLines().isEmpty()) {
                    writer.writeCharacters("\n");
                    writeOffset(writer, 4);

                    writer.writeStartElement("lines");

                    for (GroovyMultilineDocument.Line line : doc.getLines()) {
                        writer.writeCharacters("\n");
                        writeOffset(writer, 8);
                        writer.writeStartElement("line");
                        writer.writeCharacters("\n");

                        for (String lineAttr : line.keySet()) {
                            writeElement(writer, lineAttr, line.get(lineAttr), 12);
                            writer.writeCharacters("\n");
                        }

                        writeOffset(writer, 8);
                        writer.writeEndElement();
                        writer.writeCharacters("\n");
                    }

                    writeOffset(writer, 4);
                    writer.writeEndElement();
                }

                writer.writeCharacters("\n");
                writer.writeEndElement();
                writer.writeCharacters("\n");
                writer.writeEndDocument();
            } finally {
                writer.close();
            }
        } catch (IOException | XMLStreamException e) {
            throw new GroovyException("XML seralization error: " + e.getMessage(), e);
        }
    }

    public void loadFromFile(final GroovyMultilineDocument doc, File file) {
        try {
            try (final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {
                final Document document = new SAXReader().read(reader);

                final Element documentElement = document.getRootElement();
                if (!"document".equals(documentElement.getName()))
                    throw new GroovyException("Invalid XML schema");

                for (Object elementObject : documentElement.elements()) {
                    if (elementObject instanceof Element) {
                        final Element element = (Element) elementObject;

                        if ("lines".equals(element.getName())) {
                            for (Iterator lineIterator = element.elementIterator("line"); lineIterator.hasNext(); ) {
                                final Element lineElement = (Element) lineIterator.next();
                                final GroovyMultilineDocument.Line line = doc.createLine();

                                for (Object lineFieldObject : lineElement.elements())
                                    if (lineFieldObject instanceof Element)
                                        line.put(((Element) lineFieldObject).getName(), parseObject(((Element) lineFieldObject).getText()));
                            }
                        } else {
                            doc.put(element.getName(), parseObject(element.getText()));
                        }
                    }
                }

            }
        } catch (Throwable e) {
            throw new GroovyException("XML load error: " + e.getMessage(), e);
        }
    }

}
