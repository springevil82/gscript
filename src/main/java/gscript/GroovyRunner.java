package gscript;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import gscript.factory.file.text.GroovyTextFileReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroovyRunner {

    /**
     * Run script
     *
     * @param scriptText script text
     */
    public void runScript(String scriptText) throws Exception {
        final Factory factory = createFactory();

        try {
            final Binding binding = new Binding();
            binding.setVariable("factory", factory);
            initialize(factory, binding);

            try {
                new GroovyShell(binding).evaluate(scriptText);
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

    /**
     * Run script file
     *
     * @param scriptFile script file
     * @param args       args
     */
    public void runScript(File scriptFile, String... args) throws Exception {
        final Factory factory = createFactory();
        factory.setThisScriptFile(new ScriptFile(scriptFile, args));

        try {
            final Binding binding = new Binding();
            binding.setVariable("factory", factory);

            initialize(factory, binding);

            final StringBuilder scriptBuilder = new StringBuilder();
            try (GroovyTextFileReader fileReader = factory.file.createTextFileReader(scriptFile, "UTF-8")) {
                while (fileReader.hasNextLine()) {
                    final String lineText = fileReader.nextLine().getText();
                    scriptBuilder.append(lineText).append("\n");

                    if (lineText.contains("@groovy.transform.Field") && lineText.contains(" factory"))
                        scriptBuilder.delete(0, scriptBuilder.length());
                }
            }

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
