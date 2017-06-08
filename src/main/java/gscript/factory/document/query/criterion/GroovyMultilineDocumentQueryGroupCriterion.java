package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.*;

public class GroovyMultilineDocumentQueryGroupCriterion extends GroovyMultilineDocumentQueryCriterion {

    final List<GroovyMultilineDocumentQueryCriterion> criterionList = new LinkedList<>();

    protected GroovyMultilineDocumentQueryGroupCriterion() {
    }

    /**
     * @return all criteria in this group
     */
    public List<GroovyMultilineDocumentQueryCriterion> getCriterionList() {
        return criterionList;
    }

    /**
     * @return true если список критериев пуст, иначе false.
     */
    public boolean isEmpty() {
        return criterionList.isEmpty();
    }

    /**
     * Add criterion to group (logic "AND")
     *
     * @param criterion new criterion
     * @return this
     */
    public GroovyMultilineDocumentQueryGroupCriterion and(GroovyMultilineDocumentQueryCriterion criterion) {
        criterion.logical = Logical.AND;
        criterionList.add(criterion);
        return this;
    }

    /**
     * Add criterion to group (logic "OR")
     *
     * @param criterion new criterion
     * @return this
     */
    public GroovyMultilineDocumentQueryGroupCriterion or(GroovyMultilineDocumentQueryCriterion criterion) {
        criterion.logical = Logical.OR;
        criterionList.add(criterion);
        return this;
    }

    /**
     * Delete criterion from this group
     *
     * @param criterion criterion
     * @return this
     */
    public GroovyMultilineDocumentQueryGroupCriterion remove(GroovyMultilineDocumentQueryCriterion criterion) {
        criterionList.remove(criterion);
        return this;
    }

    @Override
    public List<GroovyMultilineDocument.Line> select(List<GroovyMultilineDocument.Line> lines) {
        if (isEmpty())
            return lines;

        final Map<GroovyMultilineDocumentQueryCriterion, List<GroovyMultilineDocument.Line>> criterionResult = new LinkedHashMap<>();
        for (GroovyMultilineDocumentQueryCriterion criterion : criterionList)
            criterionResult.put(criterion, criterion.select(lines));

        Set<GroovyMultilineDocument.Line> result = null;
        for (GroovyMultilineDocumentQueryCriterion criterion : criterionResult.keySet()) {
            if (result == null) {
                result = new HashSet<>();
                result.addAll(criterionResult.get(criterion));
            } else if (criterion.logical == Logical.AND) {
                result.retainAll(criterionResult.get(criterion));
            } else if (criterion.logical == Logical.OR) {
                result.addAll(criterionResult.get(criterion));
            }
        }

        return result != null ? new ArrayList<>(result) : new ArrayList<GroovyMultilineDocument.Line>();
    }
}
