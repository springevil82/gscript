package gscript.factory.document;

import groovy.lang.GString;
import org.codehaus.groovy.runtime.GStringImpl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class GroovyUtil {

    static Class getExtendedClass(Class class1, Class class2) {
        if (class1.equals(class2))
            return class1;

        if (isStringClass(class1)) {
            if (isStringClass(class2) || isNumber(class2) || isBoolean(class2) || isDate(class2))
                return String.class;
        }

        if (isInteger(class1)) {
            if (isStringClass(class2))
                return String.class;

            if (isInteger(class2))
                return Integer.class;

            if (isLong(class2))
                return Long.class;

            if (isDecimal(class2))
                return BigDecimal.class;
        }

        if (isLong(class1)) {
            if (isStringClass(class2))
                return String.class;

            if (isInteger(class2))
                return Long.class;

            if (isLong(class2))
                return Long.class;

            if (isDecimal(class2))
                return BigDecimal.class;
        }

        if (isDecimal(class1)) {
            if (isStringClass(class2))
                return String.class;

            if (isInteger(class2))
                return BigDecimal.class;

            if (isLong(class2))
                return BigDecimal.class;

            if (isDecimal(class2))
                return BigDecimal.class;
        }

        if (isBoolean(class1)) {
            if (isStringClass(class2))
                return String.class;

            if (isBoolean(class2))
                return Boolean.class;
        }

        if (isDate(class1)) {
            if (isStringClass(class2))
                return String.class;

            if (isDate(class2))
                return Date.class;
        }

        return Object.class;
    }


    private static boolean isStringClass(Class clazz) {
        return clazz.equals(String.class) || clazz.equals(Character.class) ||
                clazz.equals(GString.class) || clazz.equals(GStringImpl.class);
    }

    private static boolean isNumber(Class clazz) {
        return Number.class.isAssignableFrom(clazz);
    }

    private static boolean isInteger(Class clazz) {
        return clazz.equals(Integer.class) || clazz.equals(int.class) ||
                clazz.equals(Short.class) || clazz.equals(short.class);
    }

    private static boolean isLong(Class clazz) {
        return clazz.equals(Long.class) || clazz.equals(long.class) ||
                clazz.equals(BigInteger.class);
    }

    private static boolean isDecimal(Class clazz) {
        return clazz.equals(Float.class) || clazz.equals(float.class) ||
                clazz.equals(Double.class) || clazz.equals(double.class) ||
                clazz.equals(BigDecimal.class);
    }

    private static boolean isBoolean(Class clazz) {
        return clazz.equals(Boolean.class) || clazz.equals(boolean.class);
    }

    private static boolean isDate(Class clazz) {
        return Date.class.isAssignableFrom(clazz);
    }

    public static Object cast(Object value, Class toClass) throws Exception {
        if (value == null)
            return null;

        final Class valueClass = value.getClass();
        if (valueClass.equals(toClass))
            return value;

        if (valueClass.equals(String.class)) {
            if (toClass.equals(String.class)) return value;
            if (toClass.equals(Date.class)) return new SimpleDateFormat().parse((String) value);
            if (toClass.equals(Boolean.class))
                return "true".equals(((String) value).toLowerCase()) || "1".equals(value);
            if (toClass.equals(Float.class)) return Float.parseFloat((String) value);
            if (toClass.equals(Double.class)) return Double.parseDouble((String) value);
            if (toClass.equals(Integer.class)) return Integer.parseInt((String) value);
            if (toClass.equals(BigDecimal.class)) return new BigDecimal((String) value);
            if (toClass.equals(Long.class)) return Long.parseLong((String) value);
            if (toClass.equals(Short.class)) return Short.parseShort((String) value);
            if (toClass.equals(BigInteger.class)) return new BigInteger((String) value);
        }

        if (valueClass.equals(Date.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return value;
            if (toClass.equals(Double.class)) return value;
            if (toClass.equals(Integer.class)) return value;
            if (toClass.equals(BigDecimal.class)) return value;
            if (toClass.equals(Long.class)) return value;
            if (toClass.equals(Short.class)) return value;
            if (toClass.equals(BigInteger.class)) return value;
        }

        if (valueClass.equals(Boolean.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return value;
            if (toClass.equals(Double.class)) return value;
            if (toClass.equals(Integer.class)) return value;
            if (toClass.equals(BigDecimal.class)) return value;
            if (toClass.equals(Long.class)) return value;
            if (toClass.equals(Short.class)) return value;
            if (toClass.equals(BigInteger.class)) return value;
        }

        if (valueClass.equals(Float.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return value;
            if (toClass.equals(Double.class)) return Double.parseDouble(value.toString());
            if (toClass.equals(Integer.class)) return Integer.parseInt(value.toString());
            if (toClass.equals(BigDecimal.class)) return new BigDecimal(value.toString());
            if (toClass.equals(Long.class)) return Long.parseLong(value.toString());
            if (toClass.equals(Short.class)) return Short.parseShort(value.toString());
            if (toClass.equals(BigInteger.class)) return new BigInteger(value.toString());
        }

        if (valueClass.equals(Double.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return Float.parseFloat(value.toString());
            if (toClass.equals(Double.class)) return value;
            if (toClass.equals(Integer.class)) return Integer.parseInt(value.toString());
            if (toClass.equals(BigDecimal.class)) return new BigDecimal(value.toString());
            if (toClass.equals(Long.class)) return Long.parseLong(value.toString());
            if (toClass.equals(Short.class)) return Short.parseShort(value.toString());
            if (toClass.equals(BigInteger.class)) return new BigInteger(value.toString());
        }

        if (valueClass.equals(Integer.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return Float.parseFloat(value.toString());
            if (toClass.equals(Double.class)) return Double.parseDouble(value.toString());
            if (toClass.equals(Integer.class)) return value;
            if (toClass.equals(BigDecimal.class)) return new BigDecimal(value.toString());
            if (toClass.equals(Long.class)) return Long.parseLong(value.toString());
            if (toClass.equals(Short.class)) return Short.parseShort(value.toString());
            if (toClass.equals(BigInteger.class)) return new BigInteger(value.toString());
        }

        if (valueClass.equals(BigDecimal.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return Float.parseFloat(value.toString());
            if (toClass.equals(Double.class)) return Double.parseDouble(value.toString());
            if (toClass.equals(Integer.class)) return Integer.parseInt(value.toString());
            if (toClass.equals(BigDecimal.class)) return value;
            if (toClass.equals(Long.class)) return Long.parseLong(value.toString());
            if (toClass.equals(Short.class)) return Short.parseShort(value.toString());
            if (toClass.equals(BigInteger.class)) return new BigInteger(value.toString());
        }

        if (valueClass.equals(Long.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return Float.parseFloat(value.toString());
            if (toClass.equals(Double.class)) return Double.parseDouble(value.toString());
            if (toClass.equals(Integer.class)) return Integer.parseInt(value.toString());
            if (toClass.equals(BigDecimal.class)) return new BigDecimal(value.toString());
            if (toClass.equals(Long.class)) return value;
            if (toClass.equals(Short.class)) return Short.parseShort(value.toString());
            if (toClass.equals(BigInteger.class)) return new BigInteger(value.toString());
        }

        if (valueClass.equals(Short.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return Float.parseFloat(value.toString());
            if (toClass.equals(Double.class)) return Double.parseDouble(value.toString());
            if (toClass.equals(Integer.class)) return Integer.parseInt(value.toString());
            if (toClass.equals(BigDecimal.class)) return new BigDecimal(value.toString());
            if (toClass.equals(Long.class)) return Long.parseLong(value.toString());
            if (toClass.equals(Short.class)) return Short.parseShort(value.toString());
            if (toClass.equals(BigInteger.class)) return new BigInteger(value.toString());
        }

        if (valueClass.equals(BigInteger.class)) {
            if (toClass.equals(String.class)) return value.toString();
            if (toClass.equals(Date.class)) return value;
            if (toClass.equals(Boolean.class)) return value;
            if (toClass.equals(Float.class)) return Float.parseFloat(value.toString());
            if (toClass.equals(Double.class)) return Double.parseDouble(value.toString());
            if (toClass.equals(Integer.class)) return Integer.parseInt(value.toString());
            if (toClass.equals(BigDecimal.class)) return new BigDecimal(value.toString());
            if (toClass.equals(Long.class)) return Long.parseLong(value.toString());
            if (toClass.equals(Short.class)) return Short.parseShort(value.toString());
            if (toClass.equals(BigInteger.class)) return value;
        }

        return value;
    }
}
