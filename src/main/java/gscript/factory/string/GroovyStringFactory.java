package gscript.factory.string;

import gscript.Factory;
import gscript.util.qgramm.QGrammComparator;
import gscript.util.qgramm.QGrammIndex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GroovyStringFactory {

    private static final String RUSSIAN_SYMBOLS = "[а-яА-ЯёЁ ]";
    private static final Pattern XML_SPEC_SYMBOLS = Pattern.compile("[<>&'\"]");

    private final Factory factory;

    public GroovyStringFactory(Factory factory) {
        this.factory = factory;
    }

    /**
     * Проверяет содержит ли строка русские символы
     *
     * @param s строка
     * @return да/нет
     */
    public boolean containsRussianSymbols(String s) {
        final Pattern compile = Pattern.compile(RUSSIAN_SYMBOLS);
        final Matcher matcher = compile.matcher(s);
        return matcher.find();
    }

    /**
     * Проверяет содержит ли строка русские символы или пробелы
     *
     * @param s строка
     * @return да/нет
     */
    public boolean containsRussianSymbolsOrSpaces(String s) {
        final Pattern compile = Pattern.compile(RUSSIAN_SYMBOLS, Pattern.DOTALL);
        final Matcher matcher = compile.matcher(s);
        return matcher.find();
    }

    /**
     * Проверить если строка содержит спец символы XML: <>&'"
     *
     * @param s строка
     * @return {@code true} если содержит
     */
    public boolean containsXMLSpecSymbols(String s) {
        return s != null && XML_SPEC_SYMBOLS.matcher(s).find();
    }

    /**
     * Сравнить строки на предмет похожести
     *
     * @param s1 строка 1
     * @param s2 строка 2
     * @return процент похожести от 0 до 100
     */
    public int like(String s1, String s2) {
        return (int) (QGrammComparator.compare(new QGrammIndex(s1), new QGrammIndex(s2)) * 100);
    }

    /**
     * Проверить содержит ли текст строки
     *
     * @param text  текст
     * @param words искать слова в тексте
     * @return в тексте найдены все слова
     */
    public boolean containsWords(String text, String words) {
        if (words == null || text == null)
            return false;

        words = words.toLowerCase();
        text = text.toLowerCase();

        final String[] w = words.split(" ");
        if (w.length == 0)
            return false;
        if (w.length == 1)
            return text.contains(w[0]);

        for (String word : w)
            if (!text.contains(word))
                return false;

        return true;
    }

    /**
     * Проверить содержит ли текст фрагмент
     *
     * @param text     тект
     * @param fragment фрагмент
     * @return фрагмент найден
     */
    public boolean contains(String text, String fragment) {
        //noinspection SimplifiableIfStatement
        if (fragment == null || text == null)
            return false;

        return text.toLowerCase().contains(fragment.toLowerCase());
    }

    /**
     * Проверить что текст начинается на фрагмент
     *
     * @param text     текст
     * @param fragment фрагмент
     * @return текст начинается на фрагмент
     */
    public boolean starts(String text, String fragment) {
        //noinspection SimplifiableIfStatement
        if (fragment == null || text == null)
            return false;

        return text.toLowerCase().startsWith(fragment.toLowerCase());
    }

    /**
     * Проверить что текст оканчивается на фрагмент
     *
     * @param text     текст
     * @param fragment фрагмент
     * @return текст начинается на фрагмент
     */
    public boolean searchEnd(String text, String fragment) {
        //noinspection SimplifiableIfStatement
        if (fragment == null || text == null)
            return false;

        return text.toLowerCase().endsWith(fragment.toLowerCase());
    }

    /**
     * Предствить милисекунды в читабельную строку
     *
     * @param millis милисеки
     * @return строка
     */
    public String millisToString(double millis) {
        long seconds = (long) millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days >= 1)
            return days + " day "
                    + (hours - 24 * days) + " hour "
                    + (minutes - 60 * (hours - 24 * days)) + " min ";
        if (hours >= 1)
            return hours + " hour "
                    + (minutes - 60 * hours) + " min ";
        if (minutes >= 1)
            return minutes + " min "
                    + (seconds - 60 * minutes) + " sec ";
        if (seconds >= 1)
            return seconds + " sec "
                    + (millis - 1000 * seconds) + " millis";

        return millis + " millis";
    }

    public String getFirstSymbols(Object str, int len) {
        if (str == null)
            return null;

        final String s = str.toString();
        if (s.length() <= len)
            return s;

        return s.substring(0, len);
    }

    public String replace(Object str, String from, String to) {
        if (str == null)
            return null;

        return str.toString().replaceAll(from, to);
    }

}
