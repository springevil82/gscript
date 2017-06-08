package gscript.factory.document.query.criterion;

import gscript.factory.document.GroovyMultilineDocument;

import java.util.ArrayList;
import java.util.List;

public abstract class FieldCriterion extends Criterion {

    protected static final ObjectComparator objectComparator = new ObjectComparator();

    protected final String fieldName;

    protected FieldCriterion(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public List<GroovyMultilineDocument.Line> select(List<GroovyMultilineDocument.Line> lines) {
        final List<GroovyMultilineDocument.Line> result = new ArrayList<>();

        for (GroovyMultilineDocument.Line line : lines) {
            if (line.containsKey(fieldName)) {
                if (accept(line.get(fieldName)))
                    result.add(line);
            }
        }

        return result;
    }

    protected abstract boolean accept(Object value);

}
