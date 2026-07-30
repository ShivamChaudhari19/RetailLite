package in.shivam.retaillite.payment.service;

import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.domain.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentQueryService {
    private final PaymentRepository paymentRepository;

    @Transactional(
            readOnly = true,
            propagation = Propagation.REQUIRES_NEW
    )
    public boolean isAlreadySuccessful(String gatewayOrderId){

        if (gatewayOrderId==null){
            throw new ResourceNotFoundException("OrderId is null ");
        }
        Payment payment =
                paymentRepository.findByGatewayOrderId(gatewayOrderId)
                        .orElseThrow(()-> new ResourceNotFoundException("payment not found for orderId: "+gatewayOrderId));

        return payment.getPaymentStatus() == PaymentStatus.SUCCESS;

    }
}
