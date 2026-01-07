package api.service;

/**
 * Εξαίρεση για σφάλματα επικύρωσης (validation) εισόδου.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
