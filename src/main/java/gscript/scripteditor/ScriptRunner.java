package gscript.scripteditor;

import groovy.lang.Binding;
import gscript.*;
import gscript.factory.log.GroovyProgressLog;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

public final class ScriptRunner {

    private static FactoryInitializer factoryInitializer;

    /**
     * Run script file
     *
     * @param scriptFile script file
     * @param logger     output logger
     * @return true - script executed successfully, otherwise false
     */
    public static boolean runScript(final File scriptFile, final Logger logger) {
        final PrintStream stdout = System.out;
        try {
            final Runner runner = new Runner(logger);
            runner.runScript(scriptFile);
            return !runner.wereErrors();
        } catch (Throwable e) {
            logger.logError(e);
            return false;
        } finally {
            System.setOut(stdout);
        }
    }

    /**
     * Run script
     *
     * @param script script text
     * @param logger output logger
     * @return true - script executed successfully, otherwise false
     */
    public static boolean runScript(String script, final Logger logger) {
        final PrintStream stdout = System.out;
        try {
            final Runner runner = new Runner(logger);
            runner.runScript(script);
            return !runner.wereErrors();
        } catch (Throwable e) {
            logger.logError(e);
            return false;
        } finally {
            System.setOut(stdout);
        }
    }

    /**
     * Set your factory initializer
     */
    public static void setFactoryInitializer(FactoryInitializer factoryInitializer) {
        ScriptRunner.factoryInitializer = factoryInitializer;
    }

    private final static class Runner extends GroovyRunner {

        private GroovyProgressLog internalProgressLog;
        private Logger logger;
        private boolean wereErrors = false;

        public Runner(Logger logger) {
            this.logger = logger;
        }

        public boolean wereErrors() {
            return wereErrors;
        }

        @Override
        protected Factory createFactory() {
            if (factoryInitializer != null)
                return factoryInitializer.createFactory();

            return super.createFactory();
        }

        @Override
        protected void initialize(Factory factory, Binding binding) {
            final PrintStream scriptOut = new PrintStream(new RedirectStream(new RedirectPublisher() {
                @Override
                public void println(String text) {
                    logger.logMessage(text);
                }
            }));
            binding.setProperty("out", scriptOut);
            System.setOut(scriptOut);

            internalProgressLog = new GroovyProgressLog() {

                @Override
                public void close() throws Exception {
                }

                @Override
                public void setProgressText(final String text) {
                }

                @Override
                public void setProgressDetailText(final String text) {
                }

                @Override
                public void setProgressMax(final int maxValue) {
                }

                @Override
                public void moveProgress() {
                }

                @Override
                public void setProgressInfinity(final boolean infinityProgress) {
                }

                private String object2String(Object text) {
                    if (text == null)
                        return "";

                    String message = text.toString();
                    if (text.getClass().isArray())
                        message = Arrays.toString((Object[]) text);

                    return message;
                }

                @Override
                public void addInfo(final Object text) {
                    logger.logMessage("INFO: " + object2String(text));
                }

                @Override
                public void addWarn(final Object text) {
                    logger.logMessage("WARN: " + object2String(text));
                }

                @Override
                public void addError(final Object text) {
                    wereErrors = true;
                    logger.logMessage("ERROR: " + object2String(text));
                }

                @Override
                public void throwException(final Object text) {
                    wereErrors = true;
                    final GroovyException groovyException = new GroovyException(object2String(text));
                    logger.logError(groovyException);
                    throw groovyException;
                }

                @Override
                public boolean isCancelled() {
                    return false;
                }

                @Override
                public void deleteLogFile() {
                }
            };

            factory.log.presetInternalProgressLog(internalProgressLog);
        }
    }

    public interface Logger {
        void logMessage(String message);

        void logError(Throwable e);
    }

}
