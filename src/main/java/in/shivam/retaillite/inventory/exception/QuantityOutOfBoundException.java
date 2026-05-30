package in.shivam.retaillite.inventory.exception;

public class QuantityOutOfBoundException extends RuntimeException {
    public QuantityOutOfBoundException(String message) {
        super(message);
    }
}
