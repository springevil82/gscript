package gscript.factory.document.query.criterion;

public class GroovyMultilineDocumentQueryFieldCriterionNull extends GroovyMultilineDocumentQueryFieldCriterion {

    private final boolean isNull;

    GroovyMultilineDocumentQueryFieldCriterionNull(String fieldName, boolean isNull) {
        super(fieldName);
        this.isNull = isNull;
    }

    @Override
    protected boolean accept(Object value) {
        return ((value == null) == isNull);
    }
}
