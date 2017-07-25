package gscript.factory.string;

import gscript.Factory;
import gscript.util.DateUtils;
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
     * Check if string contains russian symbols
     *
     * @param s check string
     */
    public boolean containsRussianSymbols(String s) {
        final Pattern compile = Pattern.compile(RUSSIAN_SYMBOLS);
        final Matcher matcher = compile.matcher(s);
        return matcher.find();
    }

    /**
     * Check if string contains russian symbols or spaces
     *
     * @param s check string
     */
    public boolean containsRussianSymbolsOrSpaces(String s) {
        final Pattern compile = Pattern.compile(RUSSIAN_SYMBOLS, Pattern.DOTALL);
        final Matcher matcher = compile.matcher(s);
        return matcher.find();
    }

    /**
     * Check if string contains XML spec symbols, that needs to be wrapped to CDATA section
     *
     * @param s check string
     */
    public boolean containsXMLSpecSymbols(String s) {
        return s != null && XML_SPEC_SYMBOLS.matcher(s).find();
    }

    /**
     * Compare strings similarity (Q-gram algorithm)
     *
     * @param s1 string 1
     * @param s2 string 2
     * @return percent of similarity
     */
    public int like(String s1, String s2) {
        return (int) (QGrammComparator.compare(new QGrammIndex(s1), new QGrammIndex(s2)) * 100);
    }

    /**
     * Check if string contains words
     *
     * @param text  check string
     * @param words words that must be found in string (space separated; any order)
     * @return true if string contains all words
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
     * Check if string contains substring (ignore case)
     *
     * @param text     check string
     * @param fragment substring
     * @return true/false
     */
    public boolean contains(String text, String fragment) {
        //noinspection SimplifiableIfStatement
        if (fragment == null || text == null)
            return false;

        return text.toLowerCase().contains(fragment.toLowerCase());
    }

    /**
     * Check if string starts with substring (ignore case)
     *
     * @param text     check string
     * @param fragment substring
     * @return true/false
     */
    public boolean starts(String text, String fragment) {
        //noinspection SimplifiableIfStatement
        if (fragment == null || text == null)
            return false;

        return text.toLowerCase().startsWith(fragment.toLowerCase());
    }

    /**
     * Check if string ends with substring (ignore case)
     *
     * @param text     check string
     * @param fragment substring
     * @return true/false
     */
    public boolean searchEnd(String text, String fragment) {
        //noinspection SimplifiableIfStatement
        if (fragment == null || text == null)
            return false;

        return text.toLowerCase().endsWith(fragment.toLowerCase());
    }

    /**
     * Present time millis as human readable string
     *
     * @param millis time milis
     * @return string
     */
    public String millisToString(double millis) {
        return DateUtils.millisToString(millis);
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

    public String replaceFileNameSymbols(String str, String replaceTo) {
        return str == null ? null : str.replaceAll("[\\/:*?\"<>|]", replaceTo);
    }

}
