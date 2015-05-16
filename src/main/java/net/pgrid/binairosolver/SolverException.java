package net.pgrid.binairosolver;

/**
 *
 * @author Patrick Kramer
 */
public class SolverException extends Exception {

    public SolverException(String message) {
        super(message);
    }

    public SolverException(String message, Throwable cause) {
        super(message, cause);
    }

    public SolverException(Throwable cause) {
        super(cause);
    }

    public SolverException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
