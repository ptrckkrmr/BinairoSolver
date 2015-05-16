package net.pgrid.binairosolver;

/**
 * Exception type indicating a failure to solve the puzzle.
 * @author Patrick Kramer
 */
public class SolverException extends Exception {

    /**
     * Creates a new SolverException instance.
     */
    public SolverException() {
    }
    
    /**
     * Creates a new SolverException instance.
     * @param message The Exception message.
     */
    public SolverException(String message) {
        super(message);
    }

    /**
     * Creates a new SolverException instance.
     * @param message The Exception message 
     * @param cause   The {@code Throwable} that caused this SolverException.
     */
    public SolverException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new SolverException instance.
     * @param cause The {@code Throwable} that caused this SolverException.
     */
    public SolverException(Throwable cause) {
        super(cause);
    }
    
}
