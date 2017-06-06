package gscript.factory.log;

import gscript.Factory;
import gscript.GroovyException;

public final class GroovyLogFactory {

    private final Factory factory;

    private GroovyProgressLog internalProgressLog;
    private GroovyProgressLog currentProgressLog;

    public GroovyProgressLog getCurrentProgressLog() {
        return currentProgressLog;
    }

    private void ensureCloseCurrentProgressLog() {
        if (currentProgressLog != null && currentProgressLog != internalProgressLog) {
            try {
                currentProgressLog.close();
                factory.getAutoCloseables().remove(currentProgressLog);
            } catch (Exception e) {
                throw new GroovyException(e);
            }
        }
    }

    /**
     * Create logger that will log to file near current script file
     */
    public GroovyProgressLog createLog() {
        ensureCloseCurrentProgressLog();

        if (internalProgressLog != null)
            currentProgressLog = internalProgressLog;
        else
            currentProgressLog = new GroovyFileProgressLog(factory);

        return currentProgressLog;
    }

    /**
     * Create logger that will log to stdout
     */
    public GroovyProgressLog createStdoutProgressLog() {
        ensureCloseCurrentProgressLog();

        if (internalProgressLog != null)
            currentProgressLog = internalProgressLog;
        else
            currentProgressLog = new GroovyStdoutProgressLog(factory);

        return currentProgressLog;
    }

    /**
     * Create logger that will log to log frame (Swing UI)
     */
    public GroovyProgressLog createUIProgressLog() {
        ensureCloseCurrentProgressLog();

        // прогресс-лог передали извне - оттуда хотят чтобы выводилось все туда
        if (internalProgressLog != null)
            currentProgressLog = internalProgressLog;
        else
            currentProgressLog = new GroovyUIProgressLog(factory);

        return currentProgressLog;
    }

    public GroovyLogFactory(Factory factory) {
        this.factory = factory;
    }

    public void presetInternalProgressLog(GroovyProgressLog progressLog) {
        this.internalProgressLog = progressLog;
    }

    /**
     * Write info message to log
     *
     * @param text message
     */
    public void writeInfo(String text) {
        if (currentProgressLog == null)
            createLog();

        currentProgressLog.addInfo(text);
    }

    /**
     * Write warn message to log
     *
     * @param text message
     */
    public void writeWarn(String text) {
        if (currentProgressLog == null)
            createLog();

        currentProgressLog.addWarn(text);
    }

    /**
     * Write error message to log
     *
     * @param text message
     */
    public void writeError(String text) {
        if (currentProgressLog == null)
            createLog();

        currentProgressLog.addError(text);
    }
}
