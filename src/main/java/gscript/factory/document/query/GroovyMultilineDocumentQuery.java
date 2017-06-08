package gscript.factory.document.query;

import gscript.factory.document.GroovyMultilineDocument;
import gscript.factory.document.query.criterion.GroovyMultilineDocumentLineComparator;
import gscript.factory.document.query.criterion.GroovyMultilineDocumentQueryCriterion;
import gscript.factory.document.query.criterion.GroovyMultilineDocumentQueryOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class GroovyMultilineDocumentQuery {

    private GroovyMultilineDocument document;

    final List<GroovyMultilineDocumentQueryCriterion> criterionList = new ArrayList<>();
    final List<GroovyMultilineDocumentQueryOrder> orders = new ArrayList<>();

    private int limit = -1;

    public GroovyMultilineDocumentQuery(GroovyMultilineDocument document) {
        this.document = document;
    }

    /**
     * Add criterion to query
     *
     * @param criterion criterion
     * @return this
     */
    public GroovyMultilineDocumentQuery addCriterion(GroovyMultilineDocumentQueryCriterion criterion) {
        criterionList.add(criterion);
        return this;
    }

    /**
     * Add order to query
     *
     * @param orderBy sort
     * @return this
     */
    public GroovyMultilineDocumentQuery addOrderBy(GroovyMultilineDocumentQueryOrder orderBy) {
        orders.add(orderBy);
        return this;
    }

    /**
     * Set query result limit
     *
     * @param limit max rows
     * @return this
     */
    public GroovyMultilineDocumentQuery setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    private List<GroovyMultilineDocument.Line> execute() {
        List<GroovyMultilineDocument.Line> list = new ArrayList<>(document.getLinesCount());
        list.addAll(document.getLines());

        for (GroovyMultilineDocumentQueryCriterion criterion : criterionList)
            list = criterion.select(list);

        final Comparator<GroovyMultilineDocument.Line> comparator = new GroovyMultilineDocumentLineComparator(orders);
        Collections.sort(list, comparator);

        if (limit >= 0)
            for (int i = list.size() - 1; i >= limit; --i)
                list.remove(i);

        return list;
    }

}
