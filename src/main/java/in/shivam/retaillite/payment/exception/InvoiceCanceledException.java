package in.shivam.retaillite.payment.exception;

public class InvoiceCanceledException extends RuntimeException {
    public InvoiceCanceledException(String message) {
        super(message);
    }
}
