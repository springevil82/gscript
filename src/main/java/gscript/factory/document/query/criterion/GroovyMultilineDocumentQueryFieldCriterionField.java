package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.List;

public class GroovyMultilineDocumentQueryFieldCriterionField extends GroovyMultilineDocumentQueryCriterion {

    private final GroovyMultilineDocumentQueryCriterion criterion;
    private final String fieldName;

    GroovyMultilineDocumentQueryFieldCriterionField(String fieldName, GroovyMultilineDocumentQueryCriterion criterion) {
        this.fieldName = fieldName;
        this.criterion = criterion;
    }

    @Override
    public List<GroovyMultilineDocument.Line> select(List<GroovyMultilineDocument.Line> lines) {
        // todo it
        return null;
    }
}
