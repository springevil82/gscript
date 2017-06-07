package gscript.factory.transport.ftp;

import gscript.Factory;
import gscript.GroovyException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class GroovyFTPClient {

    private final Factory factory;
    private final String host;
    private final String user;
    private final String password;

    private final FTPClient ftpClient;

    public GroovyFTPClient(Factory factory, String host, String user, String password) throws Exception {
        this.factory = factory;
        this.host = host;
        this.user = user;
        this.password = password;

        ftpClient = new FTPClient();
        ftpClient.connect(host);

        if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode()))
            throw new GroovyException("Connection error: " + getFTPClientReplyString());

        if (!ftpClient.login(user, password))
            throw new GroovyException("Login error: " + getFTPClientReplyString());

        ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
    }

    private String getFTPClientReplyString() {
        try {
            return new String(ftpClient.getReplyString().getBytes(ftpClient.getControlEncoding()));
        } catch (UnsupportedEncodingException e) {
            return ftpClient.getReplyString();
        }
    }

    /**
     * Check if FTP dir exists in current working dir
     *
     * @param ftpDir check dir
     * @return true/false
     */
    public boolean existsDir(String ftpDir) throws Exception {
        final String currentDir = getCurrentDir();
        try {
            return ftpClient.changeWorkingDirectory(ftpDir);
        } finally {
            ftpClient.changeWorkingDirectory(currentDir);
        }
    }

    private void ftpCreateDir(String dirPath) throws Exception {
        String[] dirs;

        if (dirPath.startsWith("/"))
            dirPath = dirPath.substring(1);
        if (dirPath.endsWith("/"))
            dirPath = dirPath.substring(0, dirPath.length() - 1);

        if (dirPath.contains("/"))
            dirs = dirPath.split("/");
        else
            dirs = new String[]{dirPath};

        String currentDir = "";
        for (String dir : dirs) {
            currentDir = currentDir + "/" + dir;

            if (!ftpClient.changeWorkingDirectory(dir)) {
                if (!ftpClient.makeDirectory(dir))
                    throw new GroovyException("Can't create dir: " + dir + ". Current dir: " + ftpClient.printWorkingDirectory() + ". Server response: " + getFTPClientReplyString());

                if (!ftpClient.changeWorkingDirectory(dir))
                    throw new GroovyException("Can't goto dir: " + dir + ". Current dir: " + ftpClient.printWorkingDirectory() + ". Server response: " + getFTPClientReplyString());
            }
        }
    }

    /**
     * Create new FTP dir in current working dir
     *
     * @param ftpDir new dir name
     * @return true - dir created successfully, otherwise false
     * @throws Exception dir already exists
     */
    public void createDir(String ftpDir) throws Exception {
        if (existsDir(ftpDir))
            throw new GroovyException("Dir already exists");

        ftpCreateDir(ftpDir);
    }

    /**
     * Create FTP dir if not exists and goto it
     *
     * @param ftpDir dir name
     * @throws Exception
     */
    public void ensureDir(String ftpDir) throws Exception {
        if (!ftpClient.changeWorkingDirectory(ftpDir))
            ftpCreateDir(ftpDir);
    }

    /**
     * Goto FTP dir
     *
     * @param ftpDir dir
     * @throws Exception dir does not exists
     */
    public void changeDir(String ftpDir) throws Exception {
        if (!ftpClient.changeWorkingDirectory(ftpDir))
            throw new GroovyException("Dir " + ftpDir + " does not exist. " + getFTPClientReplyString());
    }

    /**
     * Get current FTP dir
     *
     * @return current working dir
     */
    public String getCurrentDir() throws Exception {
        return ftpClient.printWorkingDirectory();
    }

    /**
     * Get list of files in current FTP dir
     */
    public List<GroovyFTPFile> listFiles() throws Exception {
        final List<GroovyFTPFile> files = new ArrayList<>();
        for (FTPFile ftpFile : ftpClient.listFiles())
            files.add(new GroovyFTPFile(ftpFile));

        return files;
    }

    /**
     * Upload file into current FTP dir
     *
     * @param file file or path to file
     * @throws Exception upload error
     */
    public void uploadFile(Object file) throws Exception {
        final File f = file instanceof File ? (File) file : new File(file.toString());

        try (InputStream inputStream = new FileInputStream(f)) {
            if (!ftpClient.storeFile(f.getName(), inputStream))
                throw new GroovyException("File upload error. " + getFTPClientReplyString());
        }
    }

    /**
     * Download file from current FTP dir
     *
     * @param ftpFile FTP file name
     * @param file    local file
     */
    public void downloadFile(String ftpFile, Object file) throws Exception {
        final File f = file instanceof File ? (File) file : new File(file.toString());
        final File dir = f.getAbsoluteFile().getParentFile();
        if (!dir.exists())
            if (!dir.mkdirs())
                throw new GroovyException("Can't create dir: " + dir);

        try (FileOutputStream fileOutputStream = new FileOutputStream(f)) {
            if (!ftpClient.retrieveFile(ftpFile, fileOutputStream))
                throw new GroovyException("File download error. " + getFTPClientReplyString());
        }
    }

    /**
     * Delete file from current FTP dir
     *
     * @param ftpFile FTP file name
     */
    public void deleteFile(String ftpFile) throws Exception {
        ftpRemove(ftpFile, true);
    }

    /**
     * Delete dir from current FTP dir (recursive)
     *
     * @param ftpDir FTP dir name
     */
    public void deleteDir(String ftpDir) throws Exception {
        ftpRemove(ftpDir, false);
    }

    private void ftpRemove(String path, boolean isFile) throws Exception {
        if (!path.startsWith("/"))
            path = "/" + path;

        if (isFile) {
            if (!ftpClient.deleteFile(path))
                throw new GroovyException("File deletion error:  " + path + ". " + getFTPClientReplyString());
        } else {
            final FTPFile[] subFiles = ftpClient.listFiles(path);
            for (FTPFile file : subFiles) {
                if (file.getName().equals(".") || file.getName().equals(".."))
                    continue;

                ftpRemove(path + "/" + file.getName(), !file.isDirectory());
            }

            if (!ftpClient.removeDirectory(path))
                throw new GroovyException("Dir deletion error:  " + path + ". " + getFTPClientReplyString());
        }
    }

    /**
     * Logout FTP server
     */
    public void close() throws Exception {
        ftpClient.logout();
    }

}
