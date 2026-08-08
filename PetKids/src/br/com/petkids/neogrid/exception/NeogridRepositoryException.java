package br.com.petkids.neogrid.exception;

public class NeogridRepositoryException extends NeogridException {

    private static final long serialVersionUID = 1L;

    public NeogridRepositoryException(String message) {
        super(message);
    }

    public NeogridRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
