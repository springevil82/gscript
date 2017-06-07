package gscript.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;

public final class FileUtils {

    /**
     * Delete dir recursive (with all sub dirs and files)
     *
     * @param path path
     */
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
     * Find nonexistent file name
     *
     * @param fileName     initial file name
     * @param existChecker file name checker
     * @return nonexistent file name
     */
    public static String findFileName(String fileName, ExistChecker existChecker) {
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
     * Split file name and extension
     *
     * @param fileName file name
     * @return two element array
     */
    public static String[] splitFileName(String fileName) {
        final int indexOf = fileName.lastIndexOf(".");

        if (indexOf == -1)
            return new String[]{fileName, ""};

        return new String[]{fileName.substring(0, indexOf), fileName.substring(indexOf, fileName.length())};
    }

    /**
     * Match file name by mask (ignore case)
     *
     * @param fileName file name
     * @param mask     match mask (allowed multi mask, separated by semicolon)
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

    /**
     * Present file size as human readable string
     */
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
