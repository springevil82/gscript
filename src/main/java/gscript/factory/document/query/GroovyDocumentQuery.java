package gscript.factory.document.query;

import gscript.factory.document.GroovyMultilineDocument;
import gscript.factory.document.query.criterion.Criterion;
import gscript.factory.document.query.criterion.LineComparator;
import gscript.factory.document.query.criterion.OrderBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class GroovyDocumentQuery {

    private GroovyMultilineDocument document;

    final List<Criterion> criterionList = new ArrayList<>();
    final List<OrderBy> orders = new ArrayList<>();

    private int limit = -1;

    public GroovyDocumentQuery(GroovyMultilineDocument document) {
        this.document = document;
    }

    /**
     * Add criterion to query
     *
     * @param criterion criterion
     * @return this
     */
    public GroovyDocumentQuery addCriterion(Criterion criterion) {
        criterionList.add(criterion);
        return this;
    }

    /**
     * Add order to query
     *
     * @param orderBy sort
     * @return this
     */
    public GroovyDocumentQuery addOrderBy(OrderBy orderBy) {
        orders.add(orderBy);
        return this;
    }

    /**
     * Set query result limit
     *
     * @param limit max rows
     * @return this
     */
    public GroovyDocumentQuery setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    private List<GroovyMultilineDocument.Line> execute() {
        List<GroovyMultilineDocument.Line> list = new ArrayList<>(document.getLinesCount());
        list.addAll(document.getLines());

        for (Criterion criterion : criterionList)
            list = criterion.select(list);

        final Comparator<GroovyMultilineDocument.Line> comparator = new LineComparator(orders);
        Collections.sort(list, comparator);

        if (limit >= 0)
            for (int i = list.size() - 1; i >= limit; --i)
                list.remove(i);

        return list;
    }

}
