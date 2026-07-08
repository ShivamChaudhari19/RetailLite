package in.shivam.retaillite.payment.dto.response;

public sealed interface PaymentResponse permits CashPaymentResponse,RazorpayPaymentResponse {
}
