package in.shivam.retaillite.payment.service;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
///Strategy for Payment method to choose at runtime
public class PaymentFactory {

    private final Map<String,PaymentService> paymentServiceMap;

    public PaymentFactory(List<PaymentService> paymentServices) {
//       for (PaymentService paymentService: paymentServices){
//           paymentServiceMap.put(paymentService.getPaymentMethod(),paymentService);
//       }
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
                .orElseThrow(()-> new IllegalArgumentException("Unsupported Payment Method: "+paymentMethod));
    }

}
