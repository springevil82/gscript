package gscript.factory.transport;

import gscript.Factory;
import gscript.factory.transport.ftp.GroovyFTPClient;
import gscript.factory.transport.mail.GroovyIMAPClient;
import gscript.factory.transport.mail.GroovySMTPClient;

public final class GroovyTransportFactory {

    private final Factory factory;

    /**
     * Create SMTP client (use TLS)
     *
     * @param host     smtp server address
     * @param port     smtp server port
     * @param tlsPort  SSL/TLC port
     * @param user     username
     * @param password password
     */
    public GroovySMTPClient createSMTPClient(String host, int port, int tlsPort, String user, String password) {
        return new GroovySMTPClient(factory, host, port, tlsPort, user, password);
    }

    /**
     * Create SMTP client
     *
     * @param host     smtp server address
     * @param port     smtp server port
     * @param user     username
     * @param password password
     */
    public GroovySMTPClient createSMTPClient(String host, int port, String user, String password) {
        return new GroovySMTPClient(factory, host, port, user, password);
    }

    /**
     * Create IMAP client
     *
     * @param host     imap server address
     * @param user     username
     * @param password password
     */
    public GroovyIMAPClient createIMAPClient(String host, String user, String password) {
        return new GroovyIMAPClient(factory, host, user, password);
    }

    /**
     * Create FTP client
     *
     * @param host     ftp server address
     * @param user     username
     * @param password password
     */
    public GroovyFTPClient createFTPClient(String host, String user, String password) throws Exception {
        return new GroovyFTPClient(factory, host, user, password);
    }

    /**
     * Create HTTP client
     *
     * @param URL URL
     */
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
