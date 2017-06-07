package gscript.factory.transport.mail;

import gscript.Factory;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class GroovySMTPClient {

    private final Factory factory;

    private final String host;
    private final int port;
    private Integer tlsPort;
    private final String user;
    private final String password;

    public GroovySMTPClient(Factory factory, String host, int port, int tlsPort, String user, String password) {
        this.factory = factory;
        this.host = host;
        this.port = port;
        this.tlsPort = tlsPort;
        this.user = user;
        this.password = password;
    }

    public GroovySMTPClient(Factory factory, String host, int port, String user, String password) {
        this.factory = factory;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    /**
     * Create mail message
     *
     * @param recipients recipient emails (separated by comma if several)
     * @param subject    message subject
     * @param body       message body
     */
    public GroovySMTPMessage createMailMessage(String recipients, String subject, String body) {
        return new GroovySMTPMessage(recipients, subject, body);
    }

    /**
     * Send message
     *
     * @param mailMessage message
     */
    public void sendMail(GroovySMTPMessage mailMessage) throws Exception {

        if (tlsPort != null)
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        final Properties properties = new Properties();

        if (factory.proxy.getProxy() != null) {
            properties.setProperty("proxySet", "true");
            properties.setProperty("socksProxyHost", factory.proxy.getProxy().getServer());
            properties.setProperty("socksProxyPort", String.valueOf(factory.proxy.getProxy().getPort()));

            if (factory.proxy.getProxy().getUsername() != null)
                properties.setProperty("java.net.socks.username", factory.proxy.getProxy().getUsername());
            if (factory.proxy.getProxy().getPassword() != null)
                properties.setProperty("java.net.socks.password", factory.proxy.getProxy().getPassword());
        }

        properties.setProperty("mail.smtp.host", host);
        properties.setProperty("mail.smtp.port", String.valueOf(port));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.transport.protocol", "smtp");

        if (tlsPort != null) {
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(tlsPort));
        }

        final Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        final Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));

        final List<Address> recipientList = new ArrayList<>();
        for (String recipient : mailMessage.getRecipients())
            recipientList.add(new InternetAddress(recipient));

        message.setRecipients(Message.RecipientType.TO, recipientList.toArray(new Address[recipientList.size()]));
        message.setSubject(mailMessage.getSubject());
        message.setText(mailMessage.getBody());

        if (!mailMessage.getAttachFiles().isEmpty()) {

            final Multipart multipart = new MimeMultipart();

            final MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(mailMessage.getBody());
            multipart.addBodyPart(bodyPart);

            for (File file : mailMessage.getAttachFiles()) {
                final MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(file);
                attachPart.setHeader("Content-Type", "application/octet-stream");
                attachPart.setFileName(MimeUtility.encodeText(file.getName()));
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
        }

        Transport.send(message);
    }

}
