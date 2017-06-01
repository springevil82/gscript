package gscript.factory.document;

import gscript.Factory;

public final class GroovyDocumentFactory {

    private final Factory factory;

    public GroovyMultilineDocument createDocument() {
        return new GroovyMultilineDocument(factory);
    }

    public GroovyDocumentFactory(Factory factory) {
        this.factory = factory;
    }
}
