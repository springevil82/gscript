package gscript.factory.document.query.criterion;

public class OrderBy {

    private final String fieldName;
    private final Direction direction;

    public enum Direction {
        ASC,
        DESC
    }

    OrderBy(String fieldName, Direction direction) {
        this.fieldName = fieldName;
        this.direction = direction;
    }

    public static OrderBy asc(String fieldName) {
        return new OrderBy(fieldName, Direction.ASC);
    }

    public static OrderBy desc(String fieldName) {
        return new OrderBy(fieldName, Direction.DESC);
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
        final OrderBy orderElem;

        if (obj instanceof OrderBy) {
            orderElem = (OrderBy) obj;

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
