package gscript.factory.file.text;

import gscript.Factory;
import gscript.GroovyException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroovyTextFileReader implements AutoCloseable {

    private final File file;
    private final BufferedReader bufferedReader;
    private Line currentLine = null;
    private Line nextLine = null;

    private int hasNextLineInvokeCounter = 0;

    public GroovyTextFileReader(Factory factory, File file, String encoding) throws Exception {
        factory.registerAutoCloseable(this);

        this.file = file;

        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        nextLine = new Line(bufferedReader.readLine());
    }

    @Override
    public void close() throws Exception {
        if (bufferedReader != null)
            bufferedReader.close();
    }

    public boolean hasNextLine() {
        if (hasNextLineInvokeCounter > 10_000)
            throw new GroovyException("Подозрение на бесконечный цикл. Убедитесь что Вы используете nextLine() в теле цикла.");

        hasNextLineInvokeCounter++;

        return nextLine != null;
    }

    public Line getNextLine() {
        return nextLine;
    }

    public Line nextLine() throws Exception {
        String nextLine = bufferedReader.readLine();

        try {
            currentLine = this.nextLine;
            return this.nextLine;
        } finally {
            if (nextLine != null)
                this.nextLine = new Line(nextLine);
            else
                this.nextLine = null;

            hasNextLineInvokeCounter = 0;
        }
    }

    public Line currentLine() {
        return currentLine;
    }

    public File getFile() {
        return file;
    }

    public class Line {
        private String text;
        private RegExpData regExpValues;

        public Line(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public String asText() {
            return text.trim();
        }

        public boolean isEmpty() {
            return text == null || "".equals(text.trim());
        }

        public boolean isNotEmpty() {
            return !isEmpty();
        }

        public boolean matchRegExp(String regExp) {
            regExpValues = null;

            if (regExp == null)
                return false;

            final Matcher matcher = Pattern.compile(regExp).matcher(text);
            if (matcher.find()) {

                Object[] values = new Object[0];
                for (int i = 1; i <= matcher.groupCount(); i++)
                    values = ArrayUtils.add(values, matcher.group(i));

                regExpValues = new RegExpData(values);

                return true;
            }

            return false;
        }

        public RegExpData matchValues() {
            return regExpValues;
        }

        public boolean notMatchRegExp(String regExp) {
            return !matchRegExp(regExp);
        }

        public Date asDate(String pattern) {
            try {
                return new SimpleDateFormat(pattern).parse(asText());
            } catch (ParseException e) {
                throw new GroovyException("Ошибка разбора даты: " + asText() + " (формат " + pattern + ")" + "\n" + e.getMessage(), e);
            }
        }


        @Override
        public String toString() {
            return text;
        }

        private class RegExpData {
            private final Object[] values;

            public RegExpData(Object[] values) {
                this.values = values;
            }

            public Object[] getValues() {
                return values;
            }

            public Object getValue(int index) {
                return values[index];
            }

            public String getValueAsString(int index) {
                return values[index] != null ? values[index].toString().trim() : null;
            }

            public Double getValueAsDouble(int index) {
                return values[index] != null ? Double.parseDouble(values[index].toString().trim()) : null;
            }

            public Integer getValueAsInt(int index) {
                return values[index] != null ? Integer.parseInt(values[index].toString().trim()) : null;
            }
        }
    }
}


