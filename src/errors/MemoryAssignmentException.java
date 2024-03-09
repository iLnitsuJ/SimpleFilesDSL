package errors;

public class MemoryAssignmentException extends RuntimeException {
    public MemoryAssignmentException() {super();}

    public MemoryAssignmentException(String errorMessage) {
        super(errorMessage);
    }
}
