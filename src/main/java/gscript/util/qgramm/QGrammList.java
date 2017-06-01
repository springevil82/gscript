package gscript.util.qgramm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Список найденных элементов
 */
public final class QGrammList<T> {

    public static final double DEFAULT_REL = 0.75;

    private final List<QGrammElement<T>> elements = new ArrayList<>();
    private final double rel;

    public void add(QGrammElement<T> element) {
        // порог прохождения похожести
        if (element.getRel() > rel)
            elements.add(element);
    }

    public List<QGrammElement<T>> getElements() {
        sort();
        return elements;
    }

    public QGrammList(double rel) {
        this.rel = rel;
    }

    public void clear() {
        elements.clear();
    }

    private void sort() {
        Collections.sort(elements, new Comparator<QGrammElement<T>>() {
            @Override
            public int compare(QGrammElement o1, QGrammElement o2) {
                if (o2.getRel() > o1.getRel())
                    return 1;
                else if (o2.getRel() < o1.getRel())
                    return -1;
                else
                    return 0;
            }
        });
    }
}
