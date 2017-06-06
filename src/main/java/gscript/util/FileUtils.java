package gscript.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

public final class FileUtils {

    public static boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists())
            throw new FileNotFoundException(path.getAbsolutePath());

        boolean ret = true;
        if (path.isDirectory())
            for (File f : path.listFiles())
                ret = ret && FileUtils.deleteRecursive(f);

        return ret && path.delete();
    }

    /**
     * Подобрать имя файла которое не существует путем прибавления индекса (1, 2, 3, ...) между именем и расширением
     *
     * @param fileName     имя файла которое существует и для него нужно подобрать несуществующее имя
     * @param existChecker проверяльщик существования файла
     * @return новое имя файла которое не существует
     */
    public static String pickupFileName(String fileName, ExistChecker existChecker) {
        final String[] nameAndExt = FileUtils.splitFileName(fileName);

        String newFileName;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            newFileName = nameAndExt[0] + "(" + i + ")" + nameAndExt[1];
            if (!existChecker.existFile(newFileName))
                return newFileName;
        }

        return fileName;
    }

    /**
     * Разбить файл на имя файла без расширения result[0] и расширение result[1]
     *
     * @param fileName имя файла
     * @return 2 элемента всегда
     */
    public static String[] splitFileName(String fileName) {
        final int indexOf = fileName.lastIndexOf(".");

        if (indexOf == -1)
            return new String[]{fileName, ""};

        return new String[]{fileName.substring(0, indexOf), fileName.substring(indexOf, fileName.length())};
    }

    /**
     * Проверить соотвествует ли имя файла маске
     *
     * @param fileName имя файла
     * @param mask     маска (разрешены символы * - сколько угодно символов, ? - один символ, ; - разделитель нескольких масок по ИЛИ)
     * @return
     */
    public static boolean filenameMatch(String fileName, String mask) {
        if (mask.contains(";")) {
            final String[] strings = mask.split(Pattern.quote(";"));

            for (String m : strings)
                if (!"".equals(m.trim()) && FilenameUtils.wildcardMatch(fileName, m, IOCase.INSENSITIVE))
                    return true;

        } else {
            return FilenameUtils.wildcardMatch(fileName, mask, IOCase.INSENSITIVE);
        }

        return false;
    }

    public static String bytesize2String(long bytes) {
        final int unit = 1000;
        if (bytes < unit)
            return bytes + " B";

        final int exp = (int) (Math.log(bytes) / Math.log(unit));
        final String pre = ("kMGTPE").charAt(exp - 1) + ("");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public interface ExistChecker {
        boolean existFile(String filename);
    }

    private FileUtils() {
    }
}
