package br.com.petkids.neogrid.exception;

public class NeogridException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public NeogridException(String message) {
        super(message);
    }

    public NeogridException(String message, Throwable cause) {
        super(message, cause);
    }
}
