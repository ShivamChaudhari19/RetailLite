package in.shivam.retaillite.product.exception;

public class ProductAlreadyExistException extends RuntimeException {
    public ProductAlreadyExistException(String s) {
        super(s);
    }
}
