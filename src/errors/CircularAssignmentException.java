package errors;

public class CircularAssignmentException extends RuntimeException {
    public CircularAssignmentException() {super();}

    public CircularAssignmentException(String errorMessage) {
        super(errorMessage);
    }
}
