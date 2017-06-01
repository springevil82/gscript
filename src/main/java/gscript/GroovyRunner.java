package gscript;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import gscript.factory.file.text.GroovyTextFileReader;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroovyRunner {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Script file not defined");
            System.out.println("Usage: java -cp orders.jar ru.esc.script.GroovyRunner <script_file> -V<script_arg0> -V<script_arg1> -V<script_argX>");
            return;
        }

        final File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("Script file not found");
            System.out.println("Usage: java -cp orders.jar ru.esc.script.GroovyRunner <script_file> -V<script_arg0> -V<script_arg1> -V<script_argX>");
            return;
        }

        String[] otherArgs = new String[0];

        for (int i = 1; i < args.length; i++)
            otherArgs = ArrayUtils.add(otherArgs, args[i]);

        new GroovyRunner().runScript(file, otherArgs);
    }

    public void runScript(File scriptFile, String... args) throws Exception {
        final Factory factory = new Factory(new ScriptFile(scriptFile, args));

        try {
            final Binding binding = new Binding();
            binding.setVariable("factory", factory);

            initialize(factory, binding);

            final StringBuilder scriptBuilder = new StringBuilder();
            try (GroovyTextFileReader fileReader = factory.file.createTextFileReader(scriptFile, "UTF-8")) {
                while (fileReader.hasNextLine()) {
                    final String lineText = fileReader.nextLine().getText();
                    scriptBuilder.append(lineText).append("\n");

                    if (lineText.contains("@groovy.transform.Field") && lineText.contains("gscript.Factory"))
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

    protected void initialize(Factory factory, Binding binding) {
        // перекрыть если нужно что-то от фабрики
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
