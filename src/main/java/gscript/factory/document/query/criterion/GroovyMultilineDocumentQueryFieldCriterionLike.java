package gscript.factory.document.query.criterion;

public class GroovyMultilineDocumentQueryFieldCriterionLike extends GroovyMultilineDocumentQueryFieldCriterion {

    private final String value;

    GroovyMultilineDocumentQueryFieldCriterionLike(String fieldName, String value) {
        super(fieldName);
        this.value = value;
    }

    @Override
    protected boolean accept(Object value) {
        if (value == null) {
            if (this.value == null)
                return true;
        } else if (this.value != null) {
            if (value instanceof String) {
                if (this.value.startsWith("%") && this.value.endsWith("%")) {
                    if (((String) value).toUpperCase().contains(this.value.replaceAll("%", "").toUpperCase()))
                        return true;
                } else if (this.value.startsWith("%")) {
                    if (((String) value).startsWith(this.value.replaceAll("%", "")))
                        return true;
                }
            }
        }

        return false;
    }

}
