package gscript.factory.file.dbf;

import java.io.PrintStream;
import java.io.PrintWriter;


public class GroovyDBFException extends Exception {

    private final Throwable detail;

    public GroovyDBFException(String s) {
        this(s, null);
    }

    public GroovyDBFException(Throwable throwable) {
        this(throwable.getMessage(), throwable);
    }

    private GroovyDBFException(String s, Throwable throwable) {
        super(s);
        detail = throwable;
    }

    @Override
    public String getMessage() {
        if (detail == null)
            return super.getMessage();
        else

            return super.getMessage();
    }

    @Override
    public void printStackTrace(PrintStream printstream) {
        if (detail == null) {
            super.printStackTrace(printstream);

            return;
        }

        printstream.println(this);
        detail.printStackTrace(printstream);
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintWriter printwriter) {
        if (detail == null) {
            super.printStackTrace(printwriter);

            return;
        }

        printwriter.println(this);
        detail.printStackTrace(printwriter);
    }
}