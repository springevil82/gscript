package gscript.scripteditor.completion;

public final class GroovyCompletion {

    private String completion;
    private Class type;

    public GroovyCompletion(String completion, Class type) {
        this.completion = completion;
        this.type = type;
    }

    public String getCompletion() {
        return completion;
    }

    public Class getType() {
        return type;
    }
}
