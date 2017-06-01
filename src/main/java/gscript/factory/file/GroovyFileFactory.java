package gscript.factory.file;

import gscript.Factory;
import gscript.GroovyException;
import gscript.factory.file.dbf.GroovyCSVFileReader;
import gscript.factory.file.dbf.GroovyDBFFileReader;
import gscript.factory.file.text.GroovyTextFileReader;
import gscript.factory.format.GroovyDBTablePrinter;
import gscript.util.ArchiveUtils;
import gscript.util.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class GroovyFileFactory {

    private final Factory factory;

    /**
     * Проверить имя файла с путем или без
     *
     * @param filepath имя файда
     * @return true/false
     */
    public boolean isRelativeFile(String filepath) {
        return filepath.equals(Paths.get(filepath).getFileName().toString());
    }

    /**
     * Получение файла. Если файл - то файл, если файл строка без пути - файл рядом со скриптом, иначе по файл по абсолютному пути
     *
     * @param file
     * @return
     */
    public File getFile(Object file) {
        if (file == null)
            throw new GroovyException("File is not specified");

        if (file instanceof File) {
            return (File) file;
        } else {
            if (isRelativeFile(file.toString())) {
                File currentScriptFile = null;

                if (factory.getThisScript() != null)
                    currentScriptFile = new File(factory.getThisScript().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

                if (factory.getThisScriptFile() != null)
                    currentScriptFile = factory.getThisScriptFile().getThisScriptFile();

                if (currentScriptFile != null) {
                    final File scriptParentDir = currentScriptFile.getAbsoluteFile().getParentFile();
                    return new File(scriptParentDir, file.toString());
                }
            }

            return new File(file.toString());
        }
    }

    /**
     * Скопировать файл из расположения from в расположение to (to перезаписывается)
     *
     * @param from file или string (path_to_file)
     * @param to   file или string (path_to_file)
     */
    public void copyFile(Object from, Object to) {
        final File fromFile = from instanceof File ? (File) from : new File(from.toString());
        final File toFile = to instanceof File ? (File) to : new File(to.toString());

        try {
            Files.copy(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Throwable e) {
            throw new GroovyException("File copy error: " + e.getMessage(), e);
        }
    }

    /**
     * Переместить файл из расположения from в расположение to (to перезаписывается, from удаляется)
     *
     * @param from file или string (path_to_file)
     * @param to   file или string (path_to_file)
     */
    public void moveFile(Object from, Object to) {
        final File fromFile = from instanceof File ? (File) from : new File(from.toString());
        final File toFile = to instanceof File ? (File) to : new File(to.toString());

        try {
            try {
                Files.move(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Throwable e) {
            throw new GroovyException("File move error: " + e.getMessage(), e);
        }
    }

    /**
     * Подобрать имя файла которое не существует путем прибавления индекса (1, 2, 3, ...) между именем и расширением
     *
     * @param file или string (path_to_file)
     * @return новое имя файла которое не существует
     */
    public File getNotExistsFile(Object file) {
        final String fileName = file instanceof File ? ((File) file).getAbsolutePath() : file.toString();
        final String[] nameAndExt = splitFileNameExt(fileName);

        String newFileName;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            newFileName = nameAndExt[0] + "." + i + nameAndExt[1];

            final File f = new File(newFileName);
            if (!f.exists())
                return f;
        }

        return new File(fileName);
    }

    /**
     * Разбить файл на имя файла без расширения result[0] и расширение result[1]
     *
     * @param fileName имя файла
     * @return 2 элемента всегда
     */
    public String[] splitFileNameExt(String fileName) {
        final int indexOf = fileName.lastIndexOf(".");

        if (indexOf == -1)
            return new String[]{fileName, ""};

        return new String[]{fileName.substring(0, indexOf), fileName.substring(indexOf, fileName.length())};
    }

    /**
     * Удалить файл
     *
     * @param file file или string (path_to_file)
     */
    public boolean deleteFile(Object file) {
        final File f = file instanceof File ? (File) file : new File(file.toString());
        return f.delete();
    }

    /**
     * Создать каталог в каталоге parentDir
     *
     * @param parentDir  file или string (path_to_dir)
     * @param newDirName имя нового каталога
     */
    public boolean createDir(Object parentDir, String newDirName) {
        final File dir = parentDir instanceof File ? (File) parentDir : new File(parentDir.toString());
        return new File(dir, newDirName).mkdirs();
    }

    public boolean createDir(Object dir) {
        final File d = dir instanceof File ? (File) dir : new File(dir.toString());
        return d.mkdirs();
    }

    /**
     * Удалить каталог со всеми подкаталогами и файлами
     *
     * @param dir file или string (path_to_dir)
     */
    public void deleteDir(Object dir) throws Exception {
        final File d = dir instanceof File ? (File) dir : new File(dir.toString());
        FileUtils.deleteRecursive(d);
    }

    /**
     * Добавить файл file в архив archiveFile (archiveFile создается если его нет, если есть добавляется в него файл, если такой файл там есть - файл в нем перезаписывается)
     *
     * @param file        file или string (path_to_file)
     * @param archiveFile file или string (path_to_file)
     */
    public void archiveFile(Object file, Object archiveFile) throws Exception {
        final File f = file instanceof File ? (File) file : new File(file.toString());
        final File ar = archiveFile instanceof File ? (File) archiveFile : new File(archiveFile.toString());

        ArchiveUtils.addFileToArchive(f, ar);
    }

    /**
     * Разархивировать архив в каталог
     *
     * @param file              архив
     * @param toDir             каталог разархивации
     * @param flattenDirs       распаковать все в папку toDir (подкаталоги сплющиваются: archive.zip\subdir1\subdir2\file.dbf -> toDir\subdir1.subdir2.file.dbf)
     * @param overwriteExisting существующие файлы в каталоге toDir переписываются, иначе подбирается имя которое не существует
     * @param extractByMask     извлечь только файлы удовлетворяющие маске, null - извлечь все файлы
     */
    public List<File> unarchiveFile(Object file, Object toDir, boolean flattenDirs, boolean overwriteExisting, String extractByMask) throws Exception {
        final File f = file instanceof File ? (File) file : new File(file.toString());
        final File dir = toDir instanceof File ? (File) toDir : new File(toDir.toString());

        return ArchiveUtils.unarchiveFile(f, dir, flattenDirs, overwriteExisting, extractByMask);
    }

    /**
     * @return получить файл текущего скрипта
     */
    public File getCurrentScriptFile() {
        if (factory.getThisScript() != null)
            return new File(factory.getThisScript().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        if (factory.getThisScriptFile() != null)
            return factory.getThisScriptFile().getThisScriptFile();

        throw new GroovyException("Script is not defined");
    }

    /**
     * @return получить каталог текущего скрипта
     */
    public File getCurrentScriptDir() {
        return getCurrentScriptFile().getAbsoluteFile().getParentFile();
    }

    /**
     * Получить файл с именем fileName находящийся рядом с файлом текущего скрипта
     *
     * @param fileName имя файла рядом со скриптом
     * @return файл или исключение если файла нет
     */
    public File getFileNearCurrentScript(String fileName) {
        final File scriptParentDir = getCurrentScriptFile().getAbsoluteFile().getParentFile();
        final File fileNear = new File(scriptParentDir, fileName);
        if (fileNear.exists())
            return fileNear;

        throw new GroovyException("File not found: " + fileNear);
    }

    /**
     * Получить список файлов в каталоге dir
     *
     * @param dirName каталог
     * @param mask    маска файлов
     * @return найденные файлы
     */
    public File[] getFilesInDir(String dirName, String mask) {
        File[] files = new File[0];
        final File dir = new File(dirName);
        if (dir.exists()) {
            final FileFilter fileFilter = new WildcardFileFilter(mask);
            return dir.listFiles(fileFilter);
        }

        return files;
    }

    /**
     * Открыть на чтение текстовый файл для построчного чтения
     *
     * @param file     файл или имя файла
     * @param encoding кодировка
     * @return читатель файла
     * @throws Exception
     */
    public GroovyTextFileReader createTextFileReader(Object file, String encoding) throws Exception {
        File f;
        if (file instanceof File)
            f = (File) file;
        else
            f = getFile(file.toString());

        return new GroovyTextFileReader(factory, f, encoding);
    }

    /**
     * Открыть на чтение DBF файл для построчного чтения
     *
     * @param file     файл или имя файла
     * @param encoding кодировка
     * @return читатель файла
     * @throws Exception
     */
    public GroovyDBFFileReader createDBFFileReader(Object file, String encoding) throws Exception {
        File f;
        if (file instanceof File)
            f = (File) file;
        else
            f = getFile(file.toString());

        return new GroovyDBFFileReader(factory, f, encoding);
    }

    /**
     * Открыть на чтение CSV файл для построчного чтения
     *
     * @param file           файл или имя файла
     * @param encoding       кодировка
     * @param separator      разделитель
     * @param readFromLine   данные начинаются со строки
     * @param containsHeader первая строка данных - это названия колонок
     * @return читатель файла
     * @throws Exception
     */
    public GroovyCSVFileReader createCSVFileReader(Object file, String encoding, String separator, int readFromLine, boolean containsHeader) throws Exception {
        File f;
        if (file instanceof File)
            f = (File) file;
        else
            f = getFile(file.toString());

        return new GroovyCSVFileReader(factory, f, encoding, separator, readFromLine, containsHeader);
    }


    public GroovyFileFactory(Factory factory) {
        this.factory = factory;
    }

    /**
     * Создать временный файл, который удалиться после отработки скрипта
     *
     * @param name имя файла на котором будет базироваться имя временного файла
     * @return временный файл
     */
    public File createTempFile(String name) throws IOException {
        if (name == null) {

            name = "gscript";
            if (factory.getThisScript() != null)
                name = new File(factory.getThisScript().getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
            if (factory.getThisScriptFile() != null)
                name = factory.getThisScriptFile().getThisScriptFile().getName();

            final File tempFile = File.createTempFile(splitFileNameExt(name)[0], ".tmp");
            tempFile.deleteOnExit();
            return tempFile;
        } else if (isRelativeFile(name)) {
            final File tempFile = File.createTempFile(splitFileNameExt(name)[0], "".equals(splitFileNameExt(name)[1]) ? ".tmp" : splitFileNameExt(name)[1]);
            tempFile.deleteOnExit();
            return tempFile;
        } else {
            final File file = new File(name);
            if (file.createNewFile())
                file.deleteOnExit();

            return file;
        }
    }

    /**
     * Создать временный файл, который удалиться после отработки скрипта
     *
     * @return временный файл
     */
    public File createTempFile() throws IOException {
        return createTempFile(null);
    }

    /**
     * Распечатать текстовый файл в консоль
     *
     * @param file     файл или имя файла
     * @param encoding кодировка файла
     */
    public void printTextFile(Object file, String encoding) throws Exception {
        try (GroovyTextFileReader reader = factory.file.createTextFileReader(file, encoding)) {
            while (reader.hasNextLine())
                System.out.println(reader.nextLine().getText());
        }
    }

    /**
     * Распечатать текстовый файл в консоль. Кодировка по умолчанию UTF-8.
     *
     * @param file файл или имя файла
     */
    public void printTextFile(Object file) throws Exception {
        try (GroovyTextFileReader reader = factory.file.createTextFileReader(file, "UTF-8")) {
            while (reader.hasNextLine())
                System.out.println(reader.nextLine().getText());
        }
    }

    /**
     * Распечатать DBF файл в консоль
     *
     * @param file     файл или имя файла
     * @param encoding кодировка файла
     */
    public void printDBFFile(Object file, String encoding) throws Exception {
        printDBFFile(file, encoding, -1);
    }

    /**
     * Распечатать DBF файл в консоль
     *
     * @param file      файл или имя файла
     * @param limitRows вывести первых limitRows записей
     * @param encoding  кодировка файла
     */
    public void printDBFFile(Object file, String encoding, int limitRows) throws Exception {
        final File f;
        if (file instanceof File)
            f = (File) file;
        else
            f = getFile(file.toString());

        try (final GroovyDBFFileReader reader = factory.file.createDBFFileReader(file, encoding)) {
            new GroovyDBTablePrinter().printDataSource(new GroovyDBTablePrinter.DataSource() {
                @Override
                public int getColumnCount() throws Exception {
                    return reader.getColumnCount();
                }

                @Override
                public String getColumnLabel(int i) throws Exception {
                    return reader.getColumnName(i);
                }

                @Override
                public int getColumnType(int i) throws Exception {
                    return reader.getColumnType(i);
                }

                @Override
                public String getColumnTypeName(int i) throws Exception {
                    return reader.getColumnTypeName(i);
                }

                @Override
                public String getTableName(int i) throws Exception {
                    return factory.file.splitFileNameExt(f.getName())[0] + " (total row count " + reader.getRecordCount() + ")";
                }

                @Override
                public boolean next() throws Exception {
                    return reader.next();
                }

                @Override
                public String getString(int i) throws Exception {
                    return reader.getString(i);
                }

                @Override
                public Double getDouble(int i) throws Exception {
                    return reader.getDouble(i);
                }
            }, limitRows);
        }
    }

    public Document parseXML(File file, String encoding) throws Exception {
        try (final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), encoding)) {
            return new SAXReader().read(reader);
        }
    }

    /**
     * Проверить соотсветствует ли файл маске
     *
     * @param file файл или имя файла
     * @param mask маска файла
     * @return соответствует/нет
     */
    public boolean acceptMask(Object file, String mask) {
        final File f = file instanceof File ? (File) file : new File(file.toString());
        final FileFilter fileFilter = new WildcardFileFilter(mask);
        return fileFilter.accept(f);
    }
}
