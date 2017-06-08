package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.*;

public class GroupCriterion extends Criterion {

    final List<Criterion> criterionList = new LinkedList<>();

    protected GroupCriterion() {
    }

    /**
     * @return all criteria in this group
     */
    public List<Criterion> getCriterionList() {
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
    public GroupCriterion and(Criterion criterion) {
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
    public GroupCriterion or(Criterion criterion) {
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
    public GroupCriterion remove(Criterion criterion) {
        criterionList.remove(criterion);
        return this;
    }

    @Override
    public List<GroovyMultilineDocument.Line> select(List<GroovyMultilineDocument.Line> lines) {
        if (isEmpty())
            return lines;

        final Map<Criterion, List<GroovyMultilineDocument.Line>> criterionResult = new LinkedHashMap<>();
        for (Criterion criterion : criterionList)
            criterionResult.put(criterion, criterion.select(lines));

        Set<GroovyMultilineDocument.Line> result = null;
        for (Criterion criterion : criterionResult.keySet()) {
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
