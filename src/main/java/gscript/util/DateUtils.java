package gscript.util;

public final class DateUtils {

    /**
     * Present time millis as human readable string
     *
     * @param millis time milis
     * @return string
     */
    public static String millisToString(double millis) {
        long seconds = (long) millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days >= 1)
            return days + " day " + (hours - 24 * days) + " hour " + (minutes - 60 * (hours - 24 * days)) + " min ";

        if (hours >= 1)
            return hours + " hour " + (minutes - 60 * hours) + " min ";

        if (minutes >= 1)
            return minutes + " min " + (seconds - 60 * minutes) + " sec ";

        if (seconds >= 1)
            return seconds + " sec " + (millis - 1000 * seconds) + " millis";

        return millis + " millis";
    }


    private DateUtils() {
    }
}
