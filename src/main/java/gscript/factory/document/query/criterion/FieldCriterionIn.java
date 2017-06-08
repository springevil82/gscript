package gscript.factory.document.query.criterion;

public class FieldCriterionIn extends FieldCriterion {

    private final Object[] values;

    FieldCriterionIn(String fieldName, Object... values) {
        super(fieldName);
        this.values = values;
    }

    @Override
    protected boolean accept(Object value) {
        final int valuesCount = values.length;
        int i = 0;
        int comparisonResult = -1;

        while (i < valuesCount && comparisonResult != 0) {
            comparisonResult = objectComparator.compare(value, values[i]);
            ++i;
        }

        return comparisonResult == 0;
    }
}
