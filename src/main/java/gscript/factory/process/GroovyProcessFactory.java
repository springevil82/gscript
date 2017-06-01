package gscript.factory.process;

import gscript.Factory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;

public final class GroovyProcessFactory {

    private final Factory factory;

    public GroovyProcessFactory(Factory factory) {
        this.factory = factory;
    }

    private int runProcess(String[] commands, PrintStream redirectOutputTo) throws Exception {

        final ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.redirectErrorStream(true);

        final Process process = processBuilder.start();

        if (redirectOutputTo != null) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    redirectOutputTo.println(line);
                }
            }
        }

        return process.waitFor();
    }

    /**
     * Запустить внешнюю команду
     *
     * @param commands параметры командной строки
     * @return код возврата
     */
    public int runProcess(String... commands) throws Exception {
        return runProcess(commands, System.out);
    }


}
