package errors;

public class UnknownVariableException extends RuntimeException {
    public UnknownVariableException() {super();}

    public UnknownVariableException(String errorMessage) {
        super(errorMessage);
    }
}
