package gscript.factory.transport;

import gscript.Factory;
import gscript.factory.transport.ftp.GroovyFTPClient;
import gscript.factory.transport.mail.GroovyIMAPClient;
import gscript.factory.transport.mail.GroovySMTPClient;

public final class GroovyTransportFactory {

    private final Factory factory;

    /**
     * Создать транспорт SMTP (TLS используется)
     *
     * @param smtpServerAddress    адрес сервера исходящей почты (например: smtp.mail.ru)
     * @param smtpServerPort       порт сервера исходящей почты (для mail.ru это 465)
     * @param smtpServerTLSSSLPort SSL/TLC порт (для mail.ru это тоже 465)
     * @param smtpServerUsername   имя пользователя (например ivanov@mail.ru)
     * @param smtpServerPassword   пароль (Ваш пароль от ящика ivanov@mail.ru)
     * @return
     */
    public GroovySMTPClient createSMTPClient(String smtpServerAddress, int smtpServerPort, int smtpServerTLSSSLPort, String smtpServerUsername, String smtpServerPassword) {
        return new GroovySMTPClient(factory, smtpServerAddress, smtpServerPort, smtpServerTLSSSLPort, smtpServerUsername, smtpServerPassword);
    }

    /**
     * Создать транспорт SMTP (TLS не используется)
     *
     * @param smtpServerAddress  адрес сервера исходящей почты (например: smtp.your_domain.ru)
     * @param smtpServerPort     порт сервера исходящей почты (например 25)
     * @param smtpServerUsername имя пользователя (например ivanov@mail.ru)
     * @param smtpServerPassword пароль (Ваш пароль от ящика ivanov@mail.ru)
     * @return
     */
    public GroovySMTPClient createSMTPClient(String smtpServerAddress, int smtpServerPort, String smtpServerUsername, String smtpServerPassword) {
        return new GroovySMTPClient(factory, smtpServerAddress, smtpServerPort, smtpServerUsername, smtpServerPassword);
    }

    /**
     * Создать транспорт IMAP
     *
     * @param host     хост (например: imap.googlemail.com)
     * @param user     имя пользователя (например: myemailid@gmail.com)
     * @param password пароль
     * @return
     */
    public GroovyIMAPClient createIMAPClient(String host, String user, String password) {
        return new GroovyIMAPClient(factory, host, user, password);
    }

    /**
     * Создать клиента FTP
     *
     * @param host     хост (например: ftp.esc.ru)
     * @param user     имя пользователя
     * @param password пароль
     */
    public GroovyFTPClient createFTPClient(String host, String user, String password) throws Exception {
        return new GroovyFTPClient(factory, host, user, password);
    }

    public GroovyHTTPClient createHTTPClient(String URL) {
        return new GroovyHTTPClient(factory, URL);
    }

    public GroovySOAPClient createSOAPClient() {
        return new GroovySOAPClient();
    }

    public GroovyTransportFactory(Factory factory) {
        this.factory = factory;
    }
}
