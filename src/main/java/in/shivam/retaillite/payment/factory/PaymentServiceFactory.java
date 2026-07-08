package in.shivam.retaillite.payment.factory;

import in.shivam.retaillite.payment.exception.PaymentException;
import in.shivam.retaillite.payment.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
///Strategy for Payment method to choose at runtime
public class PaymentServiceFactory {

    private final Map<String, PaymentService> paymentServiceMap;

    public PaymentServiceFactory(List<PaymentService> paymentServices) {
        this.paymentServiceMap=paymentServices.stream()
                .collect(
                        Collectors.toMap(
                                PaymentService::getPaymentMethod,
                                paymentService -> paymentService
                        )
                );
    }
    public PaymentService getPaymentService(String paymentMethod){
        return Optional.ofNullable(paymentServiceMap.get(paymentMethod.toUpperCase()))
                .orElseThrow(()-> new PaymentException("Unsupported Payment Method: "+paymentMethod, HttpStatus.BAD_REQUEST));
    }

}
