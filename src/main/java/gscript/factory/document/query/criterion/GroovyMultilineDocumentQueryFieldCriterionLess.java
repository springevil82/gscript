package gscript.factory.document.query.criterion;

public class GroovyMultilineDocumentQueryFieldCriterionLess extends GroovyMultilineDocumentQueryFieldCriterion {

    private final Object value;
    private final boolean include;

    GroovyMultilineDocumentQueryFieldCriterionLess(String fieldName, Object value, boolean include) {
        super(fieldName);
        this.value = value;
        this.include = include;
    }

    @Override
    protected boolean accept(Object value) {
        final int comparisonResult = objectComparator.compare(value, this.value);
        return (comparisonResult < 0 || comparisonResult == 0 && include);
    }
}
