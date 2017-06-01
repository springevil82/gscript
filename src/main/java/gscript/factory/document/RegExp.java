package gscript.factory.document;

import java.util.regex.Pattern;

public interface RegExp {
    Pattern CDATA_PATTERN = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>");
    Pattern DATETIME_PATTERN = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4}) (\\d{2}):(\\d{2}):(\\d{2})");
    Pattern DATE_PATTERN = Pattern.compile("(\\d{2}).(\\d{2}).(\\d{4})");
    Pattern DECIMAL_PATTERN = Pattern.compile("^\\d+.\\d+$");
    Pattern INT_PATTERN = Pattern.compile("^\\d+$");
    Pattern BOOLEAN_PATTERN = Pattern.compile("^(true|false)$");

    Pattern VARCHAR_WITH_SIZE = Pattern.compile("^VARCHAR\\((\\d+)\\)$");
    Pattern INTEGER_WITH_SIZE = Pattern.compile("^INTEGER\\((\\d+)\\)$");
    Pattern SIMPLE_INTEGER = Pattern.compile("^INTEGER$");
    Pattern DECIMAL_WITH_SIZE_AND_SCALE = Pattern.compile("^DECIMAL\\((\\d+),(\\d+)\\)$");
    Pattern DATE_WITH_FORMAT = Pattern.compile("^DATE\\((.+)\\)$");
    Pattern SIMPLE_BOOLEAN = Pattern.compile("^BOOLEAN$");

}
