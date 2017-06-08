package gscript.factory.document.query.criterion;

public class FieldCriterionEqual extends FieldCriterion {

    private final Object value;
    private final boolean inverse;

    FieldCriterionEqual(String fieldName, Object value, boolean inverse) {
        super(fieldName);
        this.value = value;
        this.inverse = inverse;
    }

    public Object getValue() {
        return value;
    }

    public boolean isInverse() {
        return inverse;
    }

    @Override
    protected boolean accept(Object value) {
        return ((objectComparator.compare(value, this.value) == 0) == !inverse);
    }
}
