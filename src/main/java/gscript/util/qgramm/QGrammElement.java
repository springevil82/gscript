package gscript.util.qgramm;

/**
 * Контейнер для хранения найденых строк и релевантности
 */
public final class QGrammElement<T> {

    private final T element;
    private final double rel;

    public QGrammElement(T element, double rel) {
        this.rel = rel;
        this.element = element;
    }

    public double getRel() {
        return rel;
    }

    public T getElement() {
        return element;
    }

    public String getString() {
        return element.toString();
    }
}
