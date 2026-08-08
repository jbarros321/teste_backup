package br.com.petkids.neogrid.exception;

public class NeogridServiceException extends NeogridException {

    private static final long serialVersionUID = 1L;

    public NeogridServiceException(String message) {
        super(message);
    }

    public NeogridServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
