package errors;

public class SimpleFilesExecutionException extends RuntimeException {
    public SimpleFilesExecutionException() {
        super();
    }

    public SimpleFilesExecutionException(String errorMessage) {
        super(errorMessage);
    }
}
