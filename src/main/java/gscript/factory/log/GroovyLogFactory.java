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
     * Создать логгер который будет логировать в текущий вывод в файл (который создаться рядом с файлом скрипта)
     */
    public GroovyProgressLog createLog() {
        ensureCloseCurrentProgressLog();

        // прогресс-лог передали извне - оттуда хотят чтобы выводилось все туда
        if (internalProgressLog != null)
            currentProgressLog = internalProgressLog;
        else
            currentProgressLog = new GroovyFileProgressLog(factory);

        return currentProgressLog;
    }

    /**
     * Создать логгер который будет логировать в текущий вывод в файл (который создаться рядом с файлом скрипта) и стандартный вывод
     */
    public GroovyProgressLog createStdoutProgressLog() {
        ensureCloseCurrentProgressLog();

        // прогресс-лог передали извне - оттуда хотят чтобы выводилось все туда
        if (internalProgressLog != null)
            currentProgressLog = internalProgressLog;
        else
            currentProgressLog = new GroovyStdoutProgressLog(factory);

        return currentProgressLog;
    }

    /**
     * Создать логгер который будет логировать в текущий вывод в файл (который создаться рядом с файлом скрипта),
     * а также поднимет окно (frame) с прогресс баром и текстовым редактором в который будет также логироваться текущий логгер
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

    /**
     * Установить внешний progressLog
     */
    public void presetInternalProgressLog(GroovyProgressLog progressLog) {
        this.internalProgressLog = progressLog;
    }

    /**
     * Записать в текущий логгер текст (если логгера нет - он будет создан автоматически)
     *
     * @param text
     */
    public void writeInfo(String text) {
        if (currentProgressLog == null)
            createLog();

        currentProgressLog.addInfo(text);
    }

    /**
     * Записать в текущий логгер текст (если логгера нет - он будет создан автоматически)
     *
     * @param text
     */
    public void writeWarn(String text) {
        if (currentProgressLog == null)
            createLog();

        currentProgressLog.addWarn(text);
    }

    /**
     * Записать в текущий логгер текст (если логгера нет - он будет создан автоматически)
     *
     * @param text
     */
    public void writeError(String text) {
        if (currentProgressLog == null)
            createLog();

        currentProgressLog.addError(text);
    }
}
