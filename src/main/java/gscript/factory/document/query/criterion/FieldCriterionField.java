package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.List;

public class FieldCriterionField extends Criterion {

    private final Criterion criterion;
    private final String fieldName;

    FieldCriterionField(String fieldName, Criterion criterion) {
        this.fieldName = fieldName;
        this.criterion = criterion;
    }

    @Override
    public List<GroovyMultilineDocument.Line> select(List<GroovyMultilineDocument.Line> lines) {
        // todo it
        return null;
    }
}
