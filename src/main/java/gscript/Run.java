package gscript;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;

public final class Run {

    private static void showUsage() {
        System.out.println("Script runner");
        System.out.println();
        System.out.println("Console run: \n");
        System.out.println("java -jar gscript.jar \"<path_to_script>\" [-V<variable name>=\"<variable_value>\", ...]");
        System.out.println("Where: ");
        System.out.println("  <path_to_script> - first parameter; required; path to script file");
        System.out.println("  [-V<variable name>=\"<variable_value>\", ...] - next parameters; optional; variables for script");
        System.out.println();
        System.out.println("All passed variables are available in script");
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            showUsage();
            System.exit(0);
        }

        final File scriptFile = new File(args[0]);
        if (!scriptFile.exists()) {
            showUsage();
            System.exit(0);
        }

        String[] otherArgs = new String[0];
        for (int i = 1; i < args.length; i++)
            otherArgs = ArrayUtils.add(otherArgs, args[i]);

        final GroovyRunner groovyRunner = new GroovyRunner();
        groovyRunner.runScript(scriptFile, otherArgs);
    }


}
