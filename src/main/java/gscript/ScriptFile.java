package gscript;

import java.io.File;

public final class ScriptFile {

    private File thisScriptFile;
    private String[] args;

    public ScriptFile(File thisScriptFile, String[] args) {
        this.thisScriptFile = thisScriptFile;
        this.args = args;
    }

    public File getThisScriptFile() {
        return thisScriptFile;
    }

    public String[] getArgs() {
        return args;
    }
}
