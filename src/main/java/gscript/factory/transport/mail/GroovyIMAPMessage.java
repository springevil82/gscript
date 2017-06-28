package gscript.factory.transport.mail;

import gscript.Factory;
import gscript.factory.format.GroovyStringJoiner;
import org.apache.commons.lang3.StringUtils;

import javax.mail.*;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class GroovyIMAPMessage {

    private final Factory factory;
    private final Message message;

    public GroovyIMAPMessage(Factory factory, Message message) {
        this.factory = factory;
        this.message = message;
    }

    public String getFrom() throws Exception {
        final GroovyStringJoiner stringJoiner = new GroovyStringJoiner(", ");
        for (Address address : message.getFrom())
            stringJoiner.add(address.toString());

        return stringJoiner.toString();
    }

    public Date getSentDate() throws Exception {
        return message.getSentDate();
    }

    public Date getReceivedDate() throws Exception {
        return message.getReceivedDate();
    }

    public String getSubject() throws Exception {
        return message.getSubject();
    }

    public String getContentType() throws Exception {
        return message.getContentType();
    }

    public String getBody() {
        return null;
    }

    public boolean containsAttach() throws Exception {
        return !getAttachFiles().isEmpty();
    }

    public boolean containsAttach(String filemask) throws Exception {
        for (String attachFile : getAttachFiles()) {
            if (factory.file.acceptMask(attachFile, filemask))
                return true;
        }

        return false;
    }

    public List<String> getAttachFiles() throws Exception {
        return getAttachments(message);
    }

    public void saveAttachFile(String filename, Object file) throws Exception {
        final File f = file instanceof File ? (File) file : new File(file.toString());

        final BodyPart bodyPart = findAttachBodyPart(message, filename);
        if (bodyPart != null) {
            int length;
            byte[] buffer = new byte[8192];
            try (InputStream inputStream = bodyPart.getInputStream()) {
                try (FileOutputStream outputStream = new FileOutputStream(f)) {
                    while ((length = inputStream.read(buffer)) > 0)
                        outputStream.write(buffer, 0, length);
                }
            }
        }
    }

    public List<String> getAttachments(Message message) throws Exception {
        Object content = message.getContent();
        if (content instanceof String)
            return null;

        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            final List<String> result = new ArrayList<>();

            for (int i = 0; i < multipart.getCount(); i++)
                result.addAll(getAttachments(multipart.getBodyPart(i)));

            return result;

        }

        return null;
    }

    private List<String> getAttachments(BodyPart part) throws Exception {
        if (part.getDisposition() == null && part.getFileName() == null)
            return new ArrayList<>();

        final List<String> result = new ArrayList<>();
        Object content = part.getContent();
        if (content instanceof InputStream || content instanceof String) {
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || StringUtils.isNotBlank(part.getFileName())) {
                result.add(MimeUtility.decodeText(part.getFileName()));
                return result;
            } else {
                return new ArrayList<>();
            }
        }

        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                result.addAll(getAttachments(bodyPart));
            }
        }
        return result;
    }

    private BodyPart findAttachBodyPart(Message message, String partName) throws Exception {
        Object content = message.getContent();

        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;

            for (int i = 0; i < multipart.getCount(); i++) {
                final BodyPart attachBodyPart = findAttachBodyPart(multipart.getBodyPart(i), partName);
                if (attachBodyPart != null)
                    return attachBodyPart;
            }
        }

        return null;
    }

    private BodyPart findAttachBodyPart(BodyPart part, String partName) throws Exception {
        if (part.getDisposition() == null && part.getFileName() == null)
            return null;

        Object content = part.getContent();
        if (content instanceof InputStream || content instanceof String) {
            if (partName.equals(MimeUtility.decodeText(part.getFileName())))
                return part;
        }

        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                final BodyPart attachBodyPart = findAttachBodyPart(bodyPart, partName);
                if (attachBodyPart != null)
                    return attachBodyPart;
            }
        }

        return null;
    }

    public void print() throws Exception {
        System.out.println("date: " + getSentDate() + "; from: " + getFrom() + "; subject: " + getSubject());
    }

}
