package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.List;

public abstract class Criterion {

    enum Logical {
        AND, OR
    }

    Logical logical;

    /**
     * Create criterion group
     *
     * @return criterion group
     */
    public static GroupCriterion group() {
        return new GroupCriterion();
    }

    /**
     * Create criterion for collection field
     *
     * @param criterion criterion for field
     * @return criterion
     */
    public static Criterion forField(String fieldName, Criterion criterion) {
        return new FieldCriterionField(fieldName, criterion);
    }

    /**
     * Create criterion: fieldValue = value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static Criterion equals(String fieldName, Object value) {
        return new FieldCriterionEqual(fieldName, value, false);
    }

    /**
     * Create criterion: fieldValue != value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static Criterion notEquals(String fieldName, Object value) {
        return new FieldCriterionEqual(fieldName, value, true);
    }

    /**
     * Create criterion: fieldValue > value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static Criterion more(String fieldName, Object value) {
        return new FieldCriterionMore(fieldName, value, false);
    }

    /**
     * Create criterion: fieldValue < value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static Criterion less(String fieldName, Object value) {
        return new FieldCriterionLess(fieldName, value, false);
    }

    /**
     * Create criterion: fieldValue >= value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static Criterion moreOrEquals(String fieldName, Object value) {
        return new FieldCriterionMore(fieldName, value, true);
    }

    /**
     * Create criterion: fieldValue <= value
     *
     * @param fieldName field name
     * @param value     value
     */
    public static Criterion lessOrEquals(String fieldName, Object value) {
        return new FieldCriterionLess(fieldName, value, true);
    }

    /**
     * Create criterion: leftValue <= fieldValue <= value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> Criterion between(String fieldName, T leftValue, T rightValue) {
        return new FieldCriterionBetween(fieldName, leftValue, rightValue, true, true);
    }

    /**
     * Create criterion: leftValue <= fieldValue < value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> Criterion betweenIncludeLeft(String fieldName, T leftValue, T rightValue) {
        return new FieldCriterionBetween(fieldName, leftValue, rightValue, true, false);
    }

    /**
     * Create criterion: leftValue < fieldValue <= value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> Criterion betweenIncludeRight(String fieldName, T leftValue, T rightValue) {
        return new FieldCriterionBetween(fieldName, leftValue, rightValue, false, true);
    }

    /**
     * Create criterion: leftValue < fieldValue < value
     *
     * @param fieldName  field value
     * @param leftValue  left value
     * @param rightValue right value
     */
    public static <T> Criterion betweenNoInclude(String fieldName, T leftValue, T rightValue) {
        return new FieldCriterionBetween(fieldName, leftValue, rightValue, false, false);
    }

    /**
     * Create criterion: fieldValue in values
     *
     * @param fieldName field value
     * @param values    possible values
     */
    @SafeVarargs
    public static <T> Criterion in(String fieldName, T... values) {
        return new FieldCriterionIn(fieldName, values);
    }

    /**
     * Create criterion: fieldValue 'sql like' value
     *
     * @param fieldName field value
     * @param value     value
     */
    public static Criterion like(String fieldName, String value) {
        return new FieldCriterionLike(fieldName, value);
    }

    /**
     * Create criterion: fieldValue starts with value
     *
     * @param fieldName field value
     * @param value     value
     */
    public static <T> Criterion startsWith(String fieldName, String value) {
        return new FieldCriterionLike(fieldName, value + "%");
    }

    /**
     * Create criterion: fieldValue contains value
     *
     * @param fieldName field value
     * @param value     value
     */
    public static Criterion contains(String fieldName, String value) {
        return new FieldCriterionLike(fieldName, "%" + value + "%");
    }

    /**
     * Create criterion: fieldValue = null
     *
     * @param fieldName field value
     */
    public static Criterion isNull(String fieldName) {
        return new FieldCriterionNull(fieldName, true);
    }

    /**
     * Create criterion: fieldValue != null
     *
     * @param fieldName field value
     */
    public static <T> Criterion isNotNull(String fieldName) {
        return new FieldCriterionNull(fieldName, false);
    }

    /**
     * Select document lines using criterion
     *
     * @param lines source lines
     * @return found lines
     */
    public abstract List<GroovyMultilineDocument.Line> select(List<GroovyMultilineDocument.Line> lines);


}
