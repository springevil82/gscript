package gscript;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GroovyRunner {

    /**
     * Run script
     *
     * @param scriptText script text
     */
    public void runScript(String scriptText) throws Exception {
        internalRunScript(createFactory(), scriptText);
    }

    /**
     * Run script file
     *
     * @param scriptFile script file
     * @param args       args
     */
    public void runScript(File scriptFile, String... args) throws Exception {
        final Factory factory = createFactory();
        factory.setThisScriptFile(new ScriptFile(scriptFile, args));
        internalRunScript(factory, new String(Files.readAllBytes(scriptFile.toPath()), "UTF-8"));
    }

    private void internalRunScript(Factory factory, String scriptText) throws Exception {
        // remove factory definition from script text, we will replace it with own
        final StringBuilder scriptBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(scriptText)) {
            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                if (line.contains("@groovy.transform.Field") && line.contains(" factory"))
                    continue;

                scriptBuilder.append(line).append("\n");
            }
        }

        try {
            final Binding binding = new Binding();
            binding.setVariable("factory", factory);
            initialize(factory, binding);

            try {
                new GroovyShell(binding).evaluate(scriptBuilder.toString());
            } catch (Throwable e) {

                if (factory.log.getCurrentProgressLog() != null)
                    factory.log.getCurrentProgressLog().addError(getExceptionCauses(e));
                else
                    System.out.println(getExceptionCauses(e));

                throw e;
            }
        } finally {
            for (AutoCloseable closeable : factory.getAutoCloseables())
                closeable.close();
        }
    }


    protected Factory createFactory() {
        return new Factory();
    }

    protected void initialize(Factory factory, Binding binding) {
    }

    public String getExceptionCauses(Throwable e) {
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


}
