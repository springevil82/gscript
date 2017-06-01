package gscript.factory.transport.mail;

import java.util.Date;

public final class GroovyIMAPMessageFilter {

    private final String folder;

    private Date fromDate;
    private Date toDate;
    private String from;
    private String subjectContains;
    private String bodyContains;

    private Boolean seen;
    private Boolean deleted;

    private Boolean withAttach;

    public GroovyIMAPMessageFilter(String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }

    public boolean containsFilter() {
        return fromDate != null || toDate != null;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubjectContains() {
        return subjectContains;
    }

    public void setSubjectContains(String subjectContains) {
        this.subjectContains = subjectContains;
    }

    public String getBodyContains() {
        return bodyContains;
    }

    public void setBodyContains(String bodyContains) {
        this.bodyContains = bodyContains;
    }

    public Boolean getWithAttach() {
        return withAttach;
    }

    public void setWithAttach(Boolean withAttach) {
        this.withAttach = withAttach;
    }
}
