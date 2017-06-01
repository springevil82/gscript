package gscript;

public final class GroovyException extends RuntimeException {
    public GroovyException(String message) {
        super(message);
    }

    public GroovyException(Throwable cause) {
        super(cause);
    }

    public GroovyException(String message, Throwable cause) {
        super(message, cause);
    }
}
