package br.com.petkids.neogrid.exception;

public class NeogridFileException extends NeogridException {

    private static final long serialVersionUID = 1L;

    public NeogridFileException(String message) {
        super(message);
    }

    public NeogridFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
