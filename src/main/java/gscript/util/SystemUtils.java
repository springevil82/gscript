package gscript.util;

import java.util.ArrayList;
import java.util.List;

public final class SystemUtils {

    public static String getExceptionCauses(Throwable e) {
        final List<String> messages = new ArrayList<>();

        messages.add(e.toString());

        Throwable cause = e.getCause();
        while (cause != null) {
            if (messages.contains(cause.toString()))
                break;

            messages.add(cause.toString());
            cause = cause.getCause();
        }

        final StringBuilder stringBuilder = new StringBuilder();
        for (String message : messages)
            stringBuilder.append(message).append("\n");

        return stringBuilder.toString().trim();
    }

    private SystemUtils() {
    }
}
