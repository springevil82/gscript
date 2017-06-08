package gscript.factory.document.query.criterion;

public class FieldCriterionNull extends FieldCriterion {

    private final boolean isNull;

    FieldCriterionNull(String fieldName, boolean isNull) {
        super(fieldName);
        this.isNull = isNull;
    }

    @Override
    protected boolean accept(Object value) {
        return ((value == null) == isNull);
    }
}
