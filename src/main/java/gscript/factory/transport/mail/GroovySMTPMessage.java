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
     * Create mail message
     *
     * @param recipients recipient emails (separated by comma if several)
     * @param subject    message subject
     * @param body       message body
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
     * Attach file to message
     *
     * @param file file
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
