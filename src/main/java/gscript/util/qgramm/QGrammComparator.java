package gscript.util.qgramm;

import java.util.HashMap;
import java.util.Map;

/**
 * Алгоритм поиска похожих строк методом Q-грамм
 */
public final class QGrammComparator {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<String, String> replaceTable = new HashMap<>();

    /**
     * Сравнить на похожесть два индекса строк
     *
     * @param modelIndex индекс эталонной строки
     * @param workIndex  индекс рабочей строки
     * @return критерий похожести
     */
    public static double compare(QGrammIndex modelIndex, QGrammIndex workIndex) {
        int count = 0;
        int[] grammsLess;
        int[] grammsMore;

        //Фикс ou-261
        if (modelIndex.getLength() == 0 && workIndex.getLength() == 0) {
            return 0d;
        }

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

    static {
        replaceTable.put("таб.", "таблетки");
        replaceTable.put("п/о", "?");
        replaceTable.put("%", "процент");
        replaceTable.put("фл.", "");
        replaceTable.put(" Ед ", "");
        replaceTable.put("Комп.", "");
        replaceTable.put("Персон.", "");
        replaceTable.put("Месяч.", "");
        replaceTable.put("Сист.", "");
        replaceTable.put("Капс.", "");
        replaceTable.put("ежедн.", "");
        replaceTable.put("норм.", "");
        replaceTable.put("амп.", "");
        replaceTable.put("Б/сахара", "");
        replaceTable.put("Д/диаб.", "");
        replaceTable.put("Черн.", "");
        replaceTable.put("Смород.", "");
        replaceTable.put("Д/т", "");
        replaceTable.put("Рект.", "");
        replaceTable.put("З/паста", "");
        replaceTable.put("0.5", "");
        replaceTable.put("похм.", "");
        replaceTable.put("глаз.", "");
        replaceTable.put("медиц.", "");
        replaceTable.put("хирург.", "");

        replaceTable.put("500мкг", "");

        replaceTable.put("1.5мг", "");
        replaceTable.put("2,5мг", "");
        replaceTable.put("5мг", "");
        replaceTable.put("20мг", "");
        replaceTable.put("25мг", "");
        replaceTable.put("40мг", "");
        replaceTable.put("50мг", "");
        replaceTable.put("80мг", "");
        replaceTable.put("10мг", "");
        replaceTable.put("100мг", "");
        replaceTable.put("150мг", "");
        replaceTable.put("250мг", "");
        replaceTable.put("300мг", "");
        replaceTable.put("400мг", "");
        replaceTable.put("500мг", "");

        replaceTable.put("0.05г", "");
        replaceTable.put("0,25г", "");
        replaceTable.put("0.25г", "");
        replaceTable.put("2г", "");
        replaceTable.put("10г", "");
        replaceTable.put("15г", "");
        replaceTable.put("20г", "");
        replaceTable.put("25г", "");
        replaceTable.put("30г", "");
        replaceTable.put("35г", "");
        replaceTable.put("38г", "");
        replaceTable.put("50г", "");
        replaceTable.put("60г", "");
        replaceTable.put("85г", "");
        replaceTable.put("100г", "");
        replaceTable.put("130г", "");
        replaceTable.put("150г", "");
        replaceTable.put("250г", "");

        replaceTable.put("2мл", "");
        replaceTable.put("5мл", "");
        replaceTable.put("10мл", "");
        replaceTable.put("250мл", "");
        replaceTable.put("100мл", "");
        replaceTable.put("200мл", "");

        replaceTable.put("№10", "");
        replaceTable.put("№25", "");
    }

}
