package gscript.factory.log;

import gscript.Factory;

public class GroovyStdoutProgressLog extends GroovyFileProgressLog {

    private String prevMessage;

    public GroovyStdoutProgressLog(Factory factory) {
        super(factory);
    }

    @Override
    protected void writeToLog(String prefix, String message) {
        // не флудим дублирующими строками в консоль
        if (prevMessage != null && prevMessage.equals(message))
            return;

        System.out.println(message);
        super.writeToLog(prefix, message);

        prevMessage = message;
    }

}
