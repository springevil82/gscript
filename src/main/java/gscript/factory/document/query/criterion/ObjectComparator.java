package gscript.factory.document.query.criterion;

import java.util.Comparator;

public final class ObjectComparator implements Comparator {

    public int compareNull(Object obj1, Object obj2) {
        final int result;

        if (obj1 == null)
            if (obj2 == null)
                result = 0;
            else
                result = -1;
        else if (obj2 == null)
            result = 1;
        else
            result = 2;

        return result;
    }

    public boolean equals(Object obj1, Object obj2) {
        final int resultInt = compareNull(obj1, obj2);
        final boolean result;

        if (resultInt == 2)
            result = obj1.equals(obj2);
        else if (resultInt == 0)
            result = true;
        else
            result = false;

        return result;
    }

    public int compare(Object obj1, Object obj2) {
        int result = compareNull(obj1, obj2);

        if (result == 2) {
            Class class1 = obj1.getClass();
            Class class2 = obj2.getClass();

            if (class1 == class2 && obj1 instanceof Comparable)
                // noinspection unchecked
                result = ((Comparable) obj1).compareTo(obj2);
            else
                result = obj1.toString().compareTo(obj2.toString());
        }

        return result;
    }
}
