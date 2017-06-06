package gscript.factory.script;

import gscript.Factory;

import java.io.File;
import java.lang.management.ManagementFactory;

public final class GroovyScriptFactory {

    private final Factory factory;

    public File getCurrentScriptFile() {
        return factory.file.getCurrentScriptFile();
    }

    public File getCurrentScriptDir() {
        return factory.file.getCurrentScriptDir();
    }


    public GroovyScriptArgsReader createScriptArgsReader() {
        return new GroovyScriptArgsReader(factory);
    }

    /**
     * @return current JVM PID
     */
    public String getCurrentProcessID() {
        String pid = "";
        try {
            final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
            final int index = jvmName.indexOf('@');
            if (index != -1)
                pid = jvmName.substring(0, index);
        } catch (Throwable e) {
            pid = e.getMessage();
        }

        return pid;
    }


    public GroovyScriptFactory(Factory factory) {
        this.factory = factory;
    }
}
