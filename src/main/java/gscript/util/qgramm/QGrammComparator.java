package gscript.util.qgramm;

/**
 * Compare string using q-gramm algorithm
 */
public final class QGrammComparator {

    /**
     * Compare two q-gramm indices
     *
     * @param modelIndex index1
     * @param workIndex  index2
     * @return criterion of similarity
     */
    public static double compare(QGrammIndex modelIndex, QGrammIndex workIndex) {
        int count = 0;
        int[] grammsLess;
        int[] grammsMore;

        if (modelIndex.getLength() == 0 && workIndex.getLength() == 0)
            return 0d;

        if (modelIndex.getIndex().length > workIndex.getIndex().length) {
            grammsLess = workIndex.getIndex();
            grammsMore = modelIndex.getIndex();
        } else {
            grammsLess = modelIndex.getIndex();
            grammsMore = workIndex.getIndex();
        }

        for (int grammL : grammsLess) {
            for (int grammM : grammsMore) {
                if (grammM == grammL) {
                    count++;
                    break;
                }
            }
        }

        double identityCriterion1 = ((double) count / (double) modelIndex.getLength());
        double identityCriterion2 = ((double) count * 2) / ((double) modelIndex.getLength() + (double) workIndex.getLength());

        if (identityCriterion1 == identityCriterion2)
            return identityCriterion1;
        if (identityCriterion1 >= identityCriterion2)
            return identityCriterion1;
        else
            return identityCriterion2;
    }

}
