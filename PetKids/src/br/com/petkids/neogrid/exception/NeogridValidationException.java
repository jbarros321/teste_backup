package br.com.petkids.neogrid.exception;

public class NeogridValidationException extends NeogridException {

    private static final long serialVersionUID = 1L;

    public NeogridValidationException(String message) {
        super(message);
    }

    public NeogridValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
