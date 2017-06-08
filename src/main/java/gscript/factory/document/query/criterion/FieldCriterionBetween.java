package gscript.factory.document.query.criterion;

public class FieldCriterionBetween extends FieldCriterion {

    private final Object leftValue;
    private final Object rightValue;
    private final boolean includeLeft;
    private final boolean includeRight;

    FieldCriterionBetween(String fieldName, Object leftValue, Object rightValue, boolean includeLeft, boolean includeRight) {
        super(fieldName);
        this.leftValue = leftValue;
        this.rightValue = rightValue;
        this.includeLeft = includeLeft;
        this.includeRight = includeRight;
    }

    @Override
    protected boolean accept(Object value) {
        final int comparisonResultLeft = objectComparator.compare(value, leftValue);
        final int comparisonResultRight = objectComparator.compare(value, rightValue);
        return (comparisonResultLeft > 0 || comparisonResultLeft == 0 && includeLeft)
                && (comparisonResultRight < 0 || comparisonResultRight == 0 && includeRight);
    }
}
