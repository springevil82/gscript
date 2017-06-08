package gscript;

import gscript.scripteditor.GroovyScriptEditor;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.io.File;

public final class Run {

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "/?".equals(args[0])) {
            showUsage();
            System.exit(0);
        }

        if (args.length == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    runScriptEditor();
                }
            });
        } else {
            final File scriptFile = new File(args[0]);
            if (!scriptFile.exists()) {
                System.out.println("Script file \"" + scriptFile + "\" not found");
                System.exit(0);
            }

            String[] otherArgs = new String[0];
            for (int i = 1; i < args.length; i++)
                otherArgs = ArrayUtils.add(otherArgs, args[i]);

            final GroovyRunner groovyRunner = new GroovyRunner();
            groovyRunner.runScript(scriptFile, otherArgs);
        }
    }

    private static void showUsage() {
        System.out.println("Script console runner");
        System.out.println();
        System.out.println("Console run: \n");
        System.out.println("java -jar gscript.jar \"<path_to_script>\" [-V<variable name>=\"<variable_value>\", ...]");
        System.out.println("Where: ");
        System.out.println("  <path_to_script> - first parameter; required; path to script file");
        System.out.println("  [-V<variable name>=\"<variable_value>\", ...] - next parameters; optional; variables for script");
    }

    public static void runScriptEditor() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        new GroovyScriptEditor().showWindow();
    }

}
