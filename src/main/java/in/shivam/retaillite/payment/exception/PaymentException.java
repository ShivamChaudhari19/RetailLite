package in.shivam.retaillite.payment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PaymentException extends RuntimeException {
    private final HttpStatus statusCode;
    public PaymentException(String message,HttpStatus statusCode) {
        super(message);
        this.statusCode=statusCode;
    }
//    public PaymentException(String message, HttpStatus statusCode,Throwable cause){
//        super(message,cause);
//        this.statusCode=statusCode;
//    }
}
