package gscript.factory.transport.mail;

import com.sun.mail.imap.IMAPFolder;
import gscript.Factory;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class GroovyIMAPClient {

    private final Factory factory;

    private final String host;
    private final String user;
    private final String password;

    private Store store;

    public GroovyIMAPClient(Factory factory, String host, String user, String password) {
        this.factory = factory;
        this.host = host;
        this.user = user;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public GroovyIMAPMessageFilter createMessageFilter(String folder) {
        return new GroovyIMAPMessageFilter(folder);
    }

    /**
     * Find messages using filter
     *
     * @param filter message filter
     * @return list of mail messages
     */
    public List<GroovyIMAPMessage> getMessages(GroovyIMAPMessageFilter filter) throws Exception {
        final List<GroovyIMAPMessage> result = new ArrayList<>();

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.starttls.enable", "true");
        props.setProperty("mail.imaps.port", "993");

        final Session session = Session.getDefaultInstance(props, null);
        final Store store = session.getStore("imaps");
        store.connect(host, user, password);
        this.store = store;

        final IMAPFolder folder = (IMAPFolder) store.getFolder(filter.getFolder());

        if (!folder.isOpen())
            folder.open(Folder.READ_WRITE);

        Message[] messages;
        if (filter.containsFilter()) {

            SearchTerm searchTerm = null;

            if (filter.getFromDate() != null && filter.getToDate() != null) {
                searchTerm = new AndTerm(
                        new ReceivedDateTerm(ComparisonTerm.LT, filter.getToDate()),
                        new ReceivedDateTerm(ComparisonTerm.GT, filter.getFromDate()));
            } else if (filter.getFromDate() != null && filter.getToDate() == null) {
                searchTerm = new ReceivedDateTerm(ComparisonTerm.GT, filter.getFromDate());
            } else if (filter.getFromDate() == null && filter.getToDate() != null) {
                searchTerm = new ReceivedDateTerm(ComparisonTerm.LT, filter.getToDate());
            }

            if (filter.getFrom() != null) {

                SearchTerm term = new FromTerm(new InternetAddress(filter.getFrom()));

                if (searchTerm == null)
                    searchTerm = term;
                else
                    searchTerm = new AndTerm(searchTerm, term);
            }

            if (filter.getSubjectContains() != null) {

                SearchTerm term = new SubjectTerm(filter.getSubjectContains());

                if (searchTerm == null)
                    searchTerm = term;
                else
                    searchTerm = new AndTerm(searchTerm, term);
            }

            if (filter.getBodyContains() != null) {

                SearchTerm term = new BodyTerm(filter.getBodyContains());

                if (searchTerm == null)
                    searchTerm = term;
                else
                    searchTerm = new AndTerm(searchTerm, term);
            }

            // SearchTerm term = new FlagTerm(Flags.Flag.);

            messages = folder.search(searchTerm);
        } else {
            messages = folder.getMessages();
        }

        for (Message message : messages) {
            final GroovyIMAPMessage groovyIMAPMessage = new GroovyIMAPMessage(factory, message);
            result.add(groovyIMAPMessage);
        }

        return result;
    }

    /**
     * Logout mail server
     */
    public void close() throws Exception {
        if (store != null)
            store.close();
    }
}
