package gscript.factory.transport.mail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class GroovySMTPMessage {

    private final List<String> recipients = new ArrayList<>();
    private String subject;
    private String body;
    private List<File> attachFiles = new ArrayList<>();

    /**
     * Создать почтовое сообщение
     *
     * @param recipients email получателя (или получателей через запятую)
     * @param subject    тема сообщения
     * @param body       тело сообщения
     */
    public GroovySMTPMessage(String recipients, String subject, String body) {
        if (recipients.contains(","))
            for (String email : recipients.split(","))
                this.recipients.add(email.trim());
        else
            this.recipients.add(recipients);

        this.subject = subject;
        this.body = body;
    }

    /**
     * Прикрепить файл к письму
     *
     * @param file файл
     */
    public void attachFile(File file) {
        this.attachFiles.add(file);
    }

    public List<String> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public List<File> getAttachFiles() {
        return attachFiles;
    }
}
