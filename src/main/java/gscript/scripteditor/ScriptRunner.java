package gscript.scripteditor;

import groovy.lang.Binding;
import gscript.Factory;
import gscript.GroovyException;
import gscript.GroovyRunner;
import gscript.factory.log.GroovyProgressLog;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public final class ScriptRunner {

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
            final boolean wereErrors[] = new boolean[]{false};

            new GroovyRunner() {
                private GroovyProgressLog internalProgressLog;

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
                            wereErrors[0] = true;
                            logger.logMessage("ERROR: " + object2String(text));
                        }

                        @Override
                        public void throwException(final Object text) {
                            wereErrors[0] = true;
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
            }.runScript(scriptFile);

            return !wereErrors[0];
        } catch (Throwable e) {
            logger.logError(e);
            return false;
        } finally {
            System.setOut(stdout);
        }
    }

    public interface Logger {
        void logMessage(String message);

        void logError(Throwable e);
    }

    private interface RedirectPublisher {
        void println(String text);
    }

    private static class RedirectStream extends OutputStream {

        private final byte DELIMITER = 10;
        private final RedirectPublisher publisher;
        private byte[] buffer = new byte[0];

        public RedirectStream(RedirectPublisher publisher) {
            this.publisher = publisher;
        }

        @Override
        public void write(int b) throws IOException {
            buffer = ArrayUtils.add(buffer, (byte) b);

            if (b == DELIMITER) {
                publisher.println(new String(buffer).trim());
                buffer = new byte[0];
            }
        }
    }

}
