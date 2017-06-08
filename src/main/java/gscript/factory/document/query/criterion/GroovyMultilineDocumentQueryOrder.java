package gscript.factory.document.query.criterion;

public class GroovyMultilineDocumentQueryOrder {

    private final String fieldName;
    private final Direction direction;

    public enum Direction {
        ASC,
        DESC
    }

    GroovyMultilineDocumentQueryOrder(String fieldName, Direction direction) {
        this.fieldName = fieldName;
        this.direction = direction;
    }

    public static GroovyMultilineDocumentQueryOrder asc(String fieldName) {
        return new GroovyMultilineDocumentQueryOrder(fieldName, Direction.ASC);
    }

    public static GroovyMultilineDocumentQueryOrder desc(String fieldName) {
        return new GroovyMultilineDocumentQueryOrder(fieldName, Direction.DESC);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object obj) {
        final boolean result;
        final GroovyMultilineDocumentQueryOrder orderElem;

        if (obj instanceof GroovyMultilineDocumentQueryOrder) {
            orderElem = (GroovyMultilineDocumentQueryOrder) obj;

            result = fieldName.equals(orderElem.fieldName) && direction == orderElem.direction;
        } else {
            result = false;
        }

        return result;
    }

    @Override
    public int hashCode() {
        int result = fieldName != null ? fieldName.hashCode() : 0;
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }
}
