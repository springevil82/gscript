package gscript.factory.log;

public interface GroovyProgressLog extends AutoCloseable {

    void setProgressText(String text);

    void setProgressDetailText(String text);

    void setProgressMax(int maxValue);

    void moveProgress();

    void setProgressInfinity(boolean infinityProgress);

    void addInfo(Object text);

    void addWarn(Object text);

    void addError(Object text);

    void throwException(Object text);

    boolean isCancelled();

    void deleteLogFile();

}
