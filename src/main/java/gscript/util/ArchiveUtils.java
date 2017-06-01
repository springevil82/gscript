package gscript.util;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class ArchiveUtils {

    /**
     * Заархивировать файл и переместить его в каталог archive (создастся автоматом если нет) рядом где был исходный файл
     *
     * @param file который нужно заархивировтаь
     * @return архивный файл в который все ушло
     */
    public static File archiveFile(File file) throws IOException {
        if (file == null || !file.exists())
            throw new FileNotFoundException();

        final File sourceFile = file.getAbsoluteFile();
        final File archiveDir = new File(sourceFile.getParentFile(), "archive");
        if (!archiveDir.exists())
            if (!archiveDir.mkdirs())
                throw new IOException("Can't create archive dir: " + archiveDir.getAbsolutePath());


        File archiveFile = new File(archiveDir, file.getName() + ".zip");
        if (archiveFile.exists()) {
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                archiveFile = new File(archiveDir, file.getName() + "." + i + ".zip");
                if (!archiveFile.exists())
                    break;
            }
        }

        archiveFile(sourceFile, archiveFile);

        if (!sourceFile.delete())
            sourceFile.deleteOnExit();

        return archiveFile;
    }

    /**
     * Заархивировать файл
     *
     * @param fromFile исходный файд
     * @param toFile   архивный файл
     * @throws IOException ошибка
     */
    public static void archiveFile(File fromFile, File toFile) throws IOException {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(toFile))) {
            zipOutputStream.putNextEntry(new ZipEntry(fromFile.getName()));
            int BUFFER_SIZE = 4096;
            int numBytesRead;
            byte[] transferBuffer = new byte[BUFFER_SIZE];
            try (FileInputStream fileInputStream = new FileInputStream(fromFile)) {
                while ((numBytesRead = fileInputStream.read(transferBuffer)) != -1)
                    zipOutputStream.write(transferBuffer, 0, numBytesRead);
            }

            zipOutputStream.closeEntry();
        }
    }

    public static void addFileToArchive(File file, File archiveFile) throws Exception {
        if (archiveFile.exists())
            addFilesToZip(file, new File[]{archiveFile});
        else
            archiveFile(file, archiveFile);
    }

    private static void addFilesToZip(File source, File[] files) throws Exception {
        final List<String> entries = new ArrayList<>();

        final File tmpZip = File.createTempFile(source.getName(), null);
        //noinspection ResultOfMethodCallIgnored
        tmpZip.delete();

        if (!source.renameTo(tmpZip))
            throw new Exception("Could not make temp file (" + source.getName() + ")");

        byte[] buffer = new byte[1024];
        final ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpZip));
        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(source));

        for (File file : files) {
            try (InputStream in = new FileInputStream(file)) {
                out.putNextEntry(new ZipEntry(file.getName()));
                entries.add(file.getName());

                for (int read = in.read(buffer); read > -1; read = in.read(buffer))
                    out.write(buffer, 0, read);

                out.closeEntry();
            }
        }

        for (ZipEntry ze = zin.getNextEntry(); ze != null; ze = zin.getNextEntry()) {
            if (!entries.contains(ze.getName())) {

                out.putNextEntry(ze);

                for (int read = zin.read(buffer); read > -1; read = zin.read(buffer))
                    out.write(buffer, 0, read);

                out.closeEntry();
            }
        }

        out.close();
        //noinspection ResultOfMethodCallIgnored
        tmpZip.delete();
    }

    public static boolean isArchive(String filename) {
        if (filename.toLowerCase().endsWith(".zip")) //NON-NLS
            return true;
        else if (filename.toLowerCase().endsWith(".rar")) //NON-NLS
            return true;
        else if (filename.toLowerCase().endsWith(".7z")) //NON-NLS
            return true;

        return false;
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
    public static List<File> unarchiveFile(File file, File toDir, boolean flattenDirs, boolean overwriteExisting, String extractByMask) throws Exception {
        if (!toDir.isDirectory())
            if (!toDir.mkdirs())
                throw new RuntimeException("Can't create output dir: " + toDir.getAbsolutePath());

        if (file.getName().toLowerCase().endsWith(".zip")) //NON-NLS
            return unarchiveZipFile(file, toDir, flattenDirs, overwriteExisting, extractByMask);
        else if (file.getName().toLowerCase().endsWith(".rar")) //NON-NLS
            return unarchiveRarFile(file, toDir, flattenDirs, overwriteExisting, extractByMask);
        else if (file.getName().toLowerCase().endsWith(".7z")) //NON-NLS
            return unarchive7ZipFile(file, toDir, flattenDirs, overwriteExisting, extractByMask);
        else
            throw new RuntimeException("Unsupported archive format");
    }

    /**
     * Разархивировать zip архив в каталог
     *
     * @param file  архив
     * @param toDir каталог разархивации
     */
    private static List<File> unarchiveZipFile(File file, File toDir, boolean flattenDirs, boolean overwriteExisting, String extractByMask) throws Exception {
        final List<File> files = new ArrayList<>();

        try (ZipFile zin = new ZipFile(file)) {
            final Enumeration<? extends ZipEntry> zipEntries = zin.entries();
            while (zipEntries.hasMoreElements()) {
                final ZipEntry zipEntry = zipEntries.nextElement();

                if (zipEntry.isDirectory()) {
                    if (!flattenDirs) {
                        final File entryDir = new File(toDir, zipEntry.getName());

                        if (!entryDir.isDirectory() && !entryDir.mkdirs())
                            throw new RuntimeException("Can't create output dir: " + entryDir.getAbsolutePath());
                    }
                } else {
                    File entryFile;
                    if (flattenDirs) {
                        entryFile = new File(toDir, zipEntry.getName().replaceAll("/", "."));
                    } else {
                        entryFile = new File(toDir, zipEntry.getName());
                    }

                    if (!overwriteExisting && entryFile.exists())
                        entryFile = new File(FileUtils.pickupFileName(entryFile.getAbsolutePath(), new FileUtils.ExistChecker() {
                            @Override
                            public boolean existFile(String filename) {
                                return new File(filename).exists();
                            }
                        }));

                    if (extractByMask != null && !"".equals(extractByMask.trim()))
                        if (!FileUtils.filenameMatch(entryFile.getName(), extractByMask))
                            continue;

                    try (FileOutputStream fileOutputStream = new FileOutputStream(entryFile)) {
                        int length;
                        byte[] buffer = new byte[1024];
                        try (InputStream inputStream = zin.getInputStream(zipEntry)) {
                            while ((length = inputStream.read(buffer)) > 0)
                                fileOutputStream.write(buffer, 0, length);
                        }
                    }

                    files.add(entryFile);
                }
            }
        }

        return files;
    }

    /**
     * Разархивировать rar архив в каталог
     *
     * @param file  архив
     * @param toDir каталог разархивации
     */
    private static List<File> unarchiveRarFile(File file, File toDir, boolean flattenDirs, boolean overwriteExisting, String extractByMask) throws Exception {
        final List<File> files = new ArrayList<>();
        RarUnarchiver.extractArchive(file, toDir, files, flattenDirs, overwriteExisting, extractByMask);
        return files;
    }

    /**
     * Разархивировать 7zip архив в каталог
     *
     * @param file  архив
     * @param toDir каталог разархивации
     */
    private static List<File> unarchive7ZipFile(File file, File toDir, boolean flattenDirs, boolean overwriteExisting, String extractByMask) throws Exception {
        final List<File> files = new ArrayList<>();
        SevenZipUnarchiver.extractArchive(file, toDir, files, flattenDirs, overwriteExisting, extractByMask);
        return files;
    }


    public static final class RarUnarchiver {

        public static void extractArchive(File archiveFile, File destination, List<File> files, boolean flattenDirs, boolean overwriteExisting, String extractByMask) throws Exception {
            final Archive archive = new Archive(archiveFile);

            if (archive != null) {
                if (archive.isEncrypted())
                    throw new RuntimeException("archive is encrypted cannot extreact");

                FileHeader fileHeader;
                while (true) {
                    fileHeader = archive.nextFileHeader();
                    if (fileHeader == null)
                        break;

                    if (fileHeader.isEncrypted())
                        throw new RuntimeException("file is encrypted cannot extract: "
                                + fileHeader.getFileNameString());

                    if (fileHeader.isDirectory()) {
                        if (!flattenDirs)
                            createDirectory(fileHeader, destination);
                    } else {
                        final File file = createFile(fileHeader, destination, flattenDirs, overwriteExisting);

                        if (extractByMask != null && !"".equals(extractByMask.trim()))
                            if (!FileUtils.filenameMatch(file.getName(), extractByMask))
                                continue;

                        try (OutputStream stream = new FileOutputStream(file)) {
                            archive.extractFile(fileHeader, stream);
                        }

                        files.add(file);
                    }
                }
            }
        }

        private static File createFile(FileHeader fileHeader, File destination, boolean flattenDirs, boolean overwriteExisting) {
            File file;
            String name;
            if (fileHeader.isFileHeader() && fileHeader.isUnicode()) {
                name = fileHeader.getFileNameW();
            } else {
                name = fileHeader.getFileNameString();
            }

            if (flattenDirs)
                name = name.replaceAll("\\\\", ".");

            file = new File(destination, name);

            if (!overwriteExisting && file.exists())
                file = new File(FileUtils.pickupFileName(file.getAbsolutePath(), new FileUtils.ExistChecker() {
                    @Override
                    public boolean existFile(String filename) {
                        return new File(filename).exists();
                    }
                }));

            return file;
        }

        private static void createDirectory(FileHeader fh, File destination) {
            File f;
            if (fh.isDirectory() && fh.isUnicode()) {
                f = new File(destination, fh.getFileNameW());
                if (!f.exists()) {
                    makeDirectory(destination, fh.getFileNameW());
                }
            } else if (fh.isDirectory() && !fh.isUnicode()) {
                f = new File(destination, fh.getFileNameString());
                if (!f.exists()) {
                    makeDirectory(destination, fh.getFileNameString());
                }
            }
        }

        private static void makeDirectory(File destination, String fileName) {
            String[] dirs = fileName.split("\\\\");
            if (dirs == null) {
                return;
            }
            String path = "";
            for (String dir : dirs) {
                path = path + File.separator + dir;
                final File d = new File(destination, path);
                if (!d.isDirectory())
                    if (!d.mkdir())
                        throw new RuntimeException("Can't create dir: " + d.getAbsolutePath());
            }
        }

    }

    public static final class SevenZipUnarchiver {

        public static void extractArchive(File archive, File destination, List<File> files, boolean flattenDirs, boolean overwriteExisting, String extractByMask) throws Exception {
            try (final RandomAccessFile randomAccessFile = new RandomAccessFile(archive, "r")) {
                try (final IInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile))) {
                    inArchive.extract(null, false, new ExtractCallback(inArchive, destination, files, flattenDirs, overwriteExisting, extractByMask));
                }
            }
        }

        private static class ExtractCallback implements IArchiveExtractCallback {
            private final IInArchive inArchive;
            private final File destination;
            private final List<File> files;
            private final boolean flattenDirs;
            private final boolean overwriteExisting;
            private final String extractByMask;

            public ExtractCallback(IInArchive inArchive, File destination, List<File> files, boolean flattenDirs, boolean overwriteExisting, String extractByMask) {
                this.inArchive = inArchive;
                this.destination = destination;
                this.files = files;
                this.flattenDirs = flattenDirs;
                this.overwriteExisting = overwriteExisting;
                this.extractByMask = extractByMask;
            }

            @Override
            public ISequentialOutStream getStream(final int index, ExtractAskMode extractAskMode) throws SevenZipException {
                return new ISequentialOutStream() {
                    @Override
                    public int write(byte[] data) throws SevenZipException {
                        final String filePath = inArchive.getStringProperty(index, PropID.PATH);

                        File file;
                        if (flattenDirs) {
                            file = new File(destination, filePath.replaceAll("\\\\", "."));
                        } else {
                            file = new File(destination, filePath);
                        }

                        final File parentDir = file.getParentFile();

                        try {
                            if (!parentDir.isDirectory() && !parentDir.mkdirs())
                                throw new RuntimeException("Can't create dir " + parentDir);

                            if (!overwriteExisting && file.exists())
                                file = new File(FileUtils.pickupFileName(file.getAbsolutePath(), new FileUtils.ExistChecker() {
                                    @Override
                                    public boolean existFile(String filename) {
                                        return new File(filename).exists();
                                    }
                                }));

                            boolean doExtract = true;

                            if (extractByMask != null && !"".equals(extractByMask.trim()))
                                if (!FileUtils.filenameMatch(file.getName(), extractByMask))
                                    doExtract = false;

                            if (doExtract) {
                                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                                    fileOutputStream.write(data);
                                }

                                files.add(file);
                            }

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return data.length;
                    }
                };
            }

            @Override
            public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
            }

            @Override
            public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
            }

            @Override
            public void setCompleted(long completeValue) throws SevenZipException {
            }

            @Override
            public void setTotal(long total) throws SevenZipException {
            }
        }
    }

    private ArchiveUtils() {
    }
}
