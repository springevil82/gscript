package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.List;

public abstract class GroovyMultilineDocumentQueryCriterion {

    enum Logical {
        AND, OR
    }

    Logical logical;

    /**
     * Create criterion group
     *
     * @return criterion group
     */
    public static GroovyMultilineDocumentQueryGroupCriterion group() {
        return new GroovyMultilineDocumentQueryGroupCriterion();
    }

    /**
     * Create criterion for collection field
     *
     * @param criterion criterion for field
     * @return criterion
     */
    public static GroovyMultilineDocumentQueryCriterion forField(String fieldName, GroovyMultilineDocumentQueryCriterion criterion) {
        return new GroovyMultilineDocumentQueryFieldCriterionField(fieldName, criterion);
    }

    /**
     * Create criterion: fieldValue = value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion equals(String fieldName, Object value) {
        return new GroovyMultilineDocumentQueryFieldCriterionEqual(fieldName, value, false);
    }

    /**
     * Create criterion: fieldValue != value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion notEquals(String fieldName, Object value) {
        return new GroovyMultilineDocumentQueryFieldCriterionEqual(fieldName, value, true);
    }

    /**
     * Create criterion: fieldValue > value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion more(String fieldName, Object value) {
        return new GroovyMultilineDocumentQueryFieldCriterionMore(fieldName, value, false);
    }

    /**
     * Create criterion: fieldValue < value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion less(String fieldName, Object value) {
        return new GroovyMultilineDocumentQueryFieldCriterionLess(fieldName, value, false);
    }

    /**
     * Create criterion: fieldValue >= value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion moreOrEquals(String fieldName, Object value) {
        return new GroovyMultilineDocumentQueryFieldCriterionMore(fieldName, value, true);
    }

    /**
     * Create criterion: fieldValue <= value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion lessOrEquals(String fieldName, Object value) {
        return new GroovyMultilineDocumentQueryFieldCriterionLess(fieldName, value, true);
    }

    /**
     * Create criterion: leftValue <= fieldValue <= value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> GroovyMultilineDocumentQueryCriterion between(String fieldName, T leftValue, T rightValue) {
        return new GroovyMultilineDocumentQueryFieldCriterionBetween(fieldName, leftValue, rightValue, true, true);
    }

    /**
     * Create criterion: leftValue <= fieldValue < value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> GroovyMultilineDocumentQueryCriterion betweenIncludeLeft(String fieldName, T leftValue, T rightValue) {
        return new GroovyMultilineDocumentQueryFieldCriterionBetween(fieldName, leftValue, rightValue, true, false);
    }

    /**
     * Create criterion: leftValue < fieldValue <= value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> GroovyMultilineDocumentQueryCriterion betweenIncludeRight(String fieldName, T leftValue, T rightValue) {
        return new GroovyMultilineDocumentQueryFieldCriterionBetween(fieldName, leftValue, rightValue, false, true);
    }

    /**
     * Create criterion: leftValue < fieldValue < value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> GroovyMultilineDocumentQueryCriterion betweenNoInclude(String fieldName, T leftValue, T rightValue) {
        return new GroovyMultilineDocumentQueryFieldCriterionBetween(fieldName, leftValue, rightValue, false, false);
    }

    /**
     * Create criterion: fieldValue in values
     *
     * @param fieldName field value
     * @param values    possible values
     */
    @SafeVarargs
    public static <T> GroovyMultilineDocumentQueryCriterion in(String fieldName, T... values) {
        return new GroovyMultilineDocumentQueryFieldCriterionIn(fieldName, values);
    }

    /**
     * Create criterion: fieldValue 'sql like' value
     *
     * @param fieldName field value
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion like(String fieldName, String value) {
        return new GroovyMultilineDocumentQueryFieldCriterionLike(fieldName, value);
    }

    /**
     * Create criterion: fieldValue starts with value
     *
     * @param fieldName field value
     * @param value     value
     */
    public static <T> GroovyMultilineDocumentQueryCriterion startsWith(String fieldName, String value) {
        return new GroovyMultilineDocumentQueryFieldCriterionLike(fieldName, value + "%");
    }

    /**
     * Create criterion: fieldValue contains value
     *
     * @param fieldName field value
     * @param value     value
     */
    public static GroovyMultilineDocumentQueryCriterion contains(String fieldName, String value) {
        return new GroovyMultilineDocumentQueryFieldCriterionLike(fieldName, "%" + value + "%");
    }

    /**
     * Create criterion: fieldValue = null
     *
     * @param fieldName field value
     */
    public static GroovyMultilineDocumentQueryCriterion isNull(String fieldName) {
        return new GroovyMultilineDocumentQueryFieldCriterionNull(fieldName, true);
    }

    /**
     * Create criterion: fieldValue != null
     *
     * @param fieldName field value
     */
    public static <T> GroovyMultilineDocumentQueryCriterion isNotNull(String fieldName) {
        return new GroovyMultilineDocumentQueryFieldCriterionNull(fieldName, false);
    }

    /**
     * Select document lines using criterion
     *
     * @param lines source lines
     * @return found lines
     */
    public abstract List<GroovyMultilineDocument.Line> select(List<GroovyMultilineDocument.Line> lines);


}
