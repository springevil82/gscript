package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.Comparator;
import java.util.List;

public class LineComparator implements Comparator<GroovyMultilineDocument.Line> {

    private static final ObjectComparator objectComparator = new ObjectComparator();
    private final List<OrderBy> sort;

    public LineComparator(final List<OrderBy> sort) {
        this.sort = sort;
    }

    @Override
    public int compare(GroovyMultilineDocument.Line line1, GroovyMultilineDocument.Line line2) {
        final int sortCount = sort.size();
        int sortIndex = 0;
        int result = objectComparator.compareNull(line1, line2);
        OrderBy curOrderElement;
        String fieldName;
        Object fieldValue1, fieldValue2;

        if (result == 2)
            result = 0;
        else
            return result;

        while (result == 0 && sortIndex < sortCount) {
            curOrderElement = sort.get(sortIndex);
            fieldName = curOrderElement.getFieldName();

            fieldValue1 = line1.get(fieldName);
            fieldValue2 = line2.get(fieldName);

            result = objectComparator.compare(fieldValue1, fieldValue2);

            if (curOrderElement.getDirection() == OrderBy.Direction.DESC)
                result = -result;

            ++sortIndex;
        }

        return result;
    }
}
