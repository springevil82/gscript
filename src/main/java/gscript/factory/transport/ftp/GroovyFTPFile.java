package gscript.factory.transport.ftp;

import gscript.util.FileUtils;
import org.apache.commons.net.ftp.FTPFile;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class GroovyFTPFile {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    private final FTPFile ftpFile;

    public GroovyFTPFile(FTPFile ftpFile) {
        this.ftpFile = ftpFile;
    }

    public boolean isFile() {
        return ftpFile.isFile();
    }

    public boolean isDir() {
        return ftpFile.isDirectory();
    }

    public String getName() {
        return ftpFile.getName();
    }

    public long getSize() {
        return ftpFile.getSize();
    }

    public Date getDate() {
        return ftpFile.getTimestamp().getTime();
    }

    public void print() {
        System.out.println((isDir() ? "[dir]" : "[file]") + " " + getName() + " [" + DATE_FORMAT.format(getDate()) + "] " + (isDir() ? "" : "[" + FileUtils.bytesize2String(getSize()) + "]"));
    }
}
