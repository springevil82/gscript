package gscript.util.qgramm;

public final class QGrammIndex {

    private final int[] index;
    private final int length;

    public int[] getIndex() {
        return index;
    }

    public int getLength() {
        return length;
    }

    public QGrammIndex(String s) {
        final StringBuilder source = new StringBuilder(s);

        // прочистим от гамна
        int i = 0;
        char c;
        while (i != source.length()) {
            c = source.charAt(i);
            if (c == '.' || c == '+' || c == '"' || c == '\'' || c == '(' || c == ')' || c == '!') {
                // прочищаем гамно
                source.deleteCharAt(i);
            } else if (c == ' ' && source.length() > i + 1 && source.charAt(i + 1) == ' ') {
                // удаляем длинные пробелы
                source.deleteCharAt(i);
            } else if (c == ' ' && source.length() == i + 1) {
                // удаляем пробел в конце
                source.deleteCharAt(i);
            } else {
                i++;
            }
        }

        //АС: фикс orderunit-261, согласованный.
        if (source.length() == 0) {
            index = new int[0];
            length = 0;
            return;
        }

        // вставим пробелы в начале и в конце
        if (source.length() > 0 && source.charAt(0) != ' ')
            source.insert(0, ' ');
        if (source.length() > 0 && source.charAt(source.length() - 1) != ' ')
            source.insert(source.length(), ' ');

        // делаем индекс
        int idx = 0;
        index = new int[source.length() - 1];
        for (i = 0; i < source.length(); i++) {
            if (i + 2 >= source.length()) {
                if (i + 2 > source.length())
                    index[idx] = Character.toUpperCase(source.charAt(i));
                else
                    index[idx] = Character.toUpperCase(source.charAt(i)) * 31 + Character.toUpperCase(source.charAt(i + 1));

                break;
            } else
                index[idx++] = Character.toUpperCase(source.charAt(i)) * 31 + Character.toUpperCase(source.charAt(i + 1));
        }

        length = source.length() - 2 + 1;
    }
}
