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

    private final String smtpServerAddress;
    private final int smtpServerPort;
    private Integer smtpServerTLSSSLPort;
    private final String smtpServerUsername;
    private final String smtpServerPassword;

    public GroovySMTPClient(Factory factory, String smtpServerAddress, int smtpServerPort, int smtpServerTLSSSLPort, String smtpServerUsername, String smtpServerPassword) {
        this.factory = factory;
        this.smtpServerAddress = smtpServerAddress;
        this.smtpServerPort = smtpServerPort;
        this.smtpServerTLSSSLPort = smtpServerTLSSSLPort;
        this.smtpServerUsername = smtpServerUsername;
        this.smtpServerPassword = smtpServerPassword;
    }

    public GroovySMTPClient(Factory factory, String smtpServerAddress, int smtpServerPort, String smtpServerUsername, String smtpServerPassword) {
        this.factory = factory;
        this.smtpServerAddress = smtpServerAddress;
        this.smtpServerPort = smtpServerPort;
        this.smtpServerUsername = smtpServerUsername;
        this.smtpServerPassword = smtpServerPassword;
    }

    /**
     * Создать почтовое сообщение
     *
     * @param recipients email получателя, или получателей через запятую (например "ivanov@mail.ru" или "ivanov@mail.ru, petrov@mail.ru")
     * @param subject    тема сообщения
     * @param body       тело сообщения
     */
    public GroovySMTPMessage createMailMessage(String recipients, String subject, String body) {
        return new GroovySMTPMessage(recipients, subject, body);
    }

    /**
     * Отправить почтовое сообщение
     *
     * @param mailMessage
     * @throws Exception
     */
    public void sendMail(GroovySMTPMessage mailMessage) throws Exception {

        if (smtpServerTLSSSLPort != null)
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

        properties.setProperty("mail.smtp.host", smtpServerAddress);
        properties.setProperty("mail.smtp.port", String.valueOf(smtpServerPort));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.transport.protocol", "smtp");

        if (smtpServerTLSSSLPort != null) {
            properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            properties.setProperty("mail.smtp.socketFactory.port", String.valueOf(smtpServerTLSSSLPort));
        }

        final Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpServerUsername, smtpServerPassword);
            }
        });

        final Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpServerUsername));

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
