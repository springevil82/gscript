package gscript.factory.log;

import gscript.Factory;
import gscript.GroovyException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class GroovyFileProgressLog implements GroovyProgressLog {

    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yy hh:mm:ss");

    private final File logFile;

    public GroovyFileProgressLog(Factory factory) {
        factory.registerAutoCloseable(this);

        this.logFile = new File(getFileNameWithoutExt(factory.file.getCurrentScriptFile().getAbsolutePath()) + ".log");

        if (logFile.exists())
            //noinspection ResultOfMethodCallIgnored
            logFile.delete();

        // fileManager.moveFile(logFile, fileManager.getNotExistsFile(logFile));

        internalWriteToLog("SYSTEM", "Log file created");
    }

    private String getFileNameWithoutExt(String fileName) {
        final int indexOf = fileName.lastIndexOf(".");

        if (indexOf == -1)
            return fileName;

        return fileName.substring(0, indexOf);
    }

    private void internalWriteToLog(String prefix, Object text) {
        if (text == null)
            return;

        String message = text.toString();
        if (text.getClass().isArray())
            message = Arrays.toString((Object[]) text);

        writeToLog(prefix, message);
    }

    protected void writeToLog(String prefix, String message) {
        try (final FileWriter fileWriter = new FileWriter(logFile, true)) {
            fileWriter.write(DATE_FORMAT.format(new Date()) + ", " + prefix + ": " + message + "\n");
        } catch (IOException e) {
            throw new GroovyException("Script log file write error: " + e.getMessage(), e);
        }
    }

    @Override
    public void setProgressText(String text) {
    }

    @Override
    public void setProgressDetailText(String text) {
    }

    @Override
    public void setProgressMax(int maxValue) {
    }

    @Override
    public void moveProgress() {
    }

    @Override
    public void setProgressInfinity(boolean infinityProgress) {
    }

    @Override
    public void addInfo(Object text) {
        internalWriteToLog("INFO", text);
    }

    @Override
    public void addWarn(Object text) {
        internalWriteToLog("WARN", text);
    }

    @Override
    public void addError(Object text) {
        internalWriteToLog("ERROR", text);
    }

    @Override
    public void throwException(Object text) {
        internalWriteToLog("FATAL", text);
        throw new GroovyException(text != null ? text.toString() : "Undefined exception");
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public void deleteLogFile() {
        if (!logFile.delete())
            logFile.deleteOnExit();
    }

}
