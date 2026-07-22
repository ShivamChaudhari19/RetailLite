package in.shivam.retaillite.integration.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.*;
import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.repository.CategoryRepository;
import in.shivam.retaillite.common.enums.PaymentMethod;
import in.shivam.retaillite.common.enums.PaymentStatus;
import in.shivam.retaillite.integration.config.BaseIntegrationTest;
import in.shivam.retaillite.integration.factory.TestDataFactory;
import in.shivam.retaillite.integration.util.AuthenticationHelper;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.repository.InvoiceItemsRepository;
import in.shivam.retaillite.invoice.repository.InvoiceRepository;
import in.shivam.retaillite.payment.PaymentRepository;
import in.shivam.retaillite.payment.dto.request.PaymentRequest;
import in.shivam.retaillite.payment.dto.request.PaymentVerifyRequest;
import in.shivam.retaillite.payment.dto.response.CashPaymentResponse;
import in.shivam.retaillite.payment.dto.response.RazorpayPaymentResponse;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import io.swagger.v3.core.util.Json;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RazorpayPaymentTest extends BaseIntegrationTest {


    @MockitoBean
    private RazorpayClient razorpayClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthenticationHelper authenticationHelper;

    @Autowired
    private TestDataFactory dataFactory;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private InvoiceItemsRepository invoiceItemsRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    private String token;

    private Integer expectedAvailableQuantityAfterSuccessfulPayment;
    @BeforeEach
    void setUp() throws Exception {
        User user= dataFactory.getUser("shivam@retaillite.com","shivam", Role.ROLE_USER);
        userRepository.save(user);
        token=authenticationHelper.login(mockMvc,"shivam@retaillite.com","shivam");


        Category category=categoryRepository.save(dataFactory.getCategory());
        Product product=productRepository.save(dataFactory.getProduct(category));
        Inventory inventory=inventoryRepository.save(dataFactory.getInventory(product));
        Invoice invoice= dataFactory.getInvoice("INV-001",user, InvoiceStatus.PENDING);
        InvoiceItem invoiceItem=dataFactory.getInvoiceItem(product);
        invoice.setInvoiceItems(new ArrayList<>(List.of(invoiceItem)));
        invoiceRepository.save(invoice);
        invoiceItem.setInvoice(invoice);
        invoiceItemsRepository.save(invoiceItem);



        Integer availableQuantityBeforeSuccessPayment=inventory.getAvailableQuantity();
        expectedAvailableQuantityAfterSuccessfulPayment=availableQuantityBeforeSuccessPayment-invoiceItem.getQuantity();
    }

    @Test
    void shouldCreatePendingPayment_WhenRazorpayOrderCreated() throws Exception {
        RazorpayPaymentResponse razorpayPaymentResponse=createOrderCall();
        assertEquals(PaymentStatus.PENDING,razorpayPaymentResponse.paymentStatus());
        assertEquals("razorpay_order_id",razorpayPaymentResponse.gatewayOrderId());
    }

    @Test
    void shouldVerifyRazorpayPaymentSuccessfully() throws Exception {

        RazorpayPaymentResponse razorpayPaymentResponse=createOrderCall();
        assertEquals(PaymentStatus.PENDING,razorpayPaymentResponse.paymentStatus());
        assertEquals("razorpay_order_id",razorpayPaymentResponse.gatewayOrderId());


        RazorpayPaymentResponse verifyResponse=verifyPayment(razorpayPaymentResponse.gatewayOrderId());
        assertEquals(PaymentStatus.SUCCESS,verifyResponse.paymentStatus());
        assertEquals("razorpay_order_id",verifyResponse.gatewayOrderId());

    }
    @Test
    void shouldDeductInventory_AfterSuccessfulPaymentVerification() throws Exception {
        RazorpayPaymentResponse razorpayPaymentResponse=createOrderCall();
        assertEquals(PaymentStatus.PENDING,razorpayPaymentResponse.paymentStatus());
        assertEquals("razorpay_order_id",razorpayPaymentResponse.gatewayOrderId());


        RazorpayPaymentResponse verifyResponse=verifyPayment(razorpayPaymentResponse.gatewayOrderId());
        assertEquals(PaymentStatus.SUCCESS,verifyResponse.paymentStatus());
        assertEquals("razorpay_order_id",verifyResponse.gatewayOrderId());

        Inventory inventory=inventoryRepository.findByProduct_productId("PROD-001").orElseThrow();
        Integer availableQuantityAfterSuccessfulPayment=inventory.getAvailableQuantity();
        assertEquals(expectedAvailableQuantityAfterSuccessfulPayment,availableQuantityAfterSuccessfulPayment);
    }



    private RazorpayPaymentResponse createOrderCall() throws Exception {
        OrderClient orderClient=mock(OrderClient.class);
        razorpayClient.orders=orderClient;
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id","razorpay_order_id");
        Order order=new Order(jsonObject);
        when(orderClient.create(any(JSONObject.class))).thenReturn(order);

        PaymentRequest paymentRequest=new PaymentRequest("INV-001",
                PaymentMethod.ONLINE
        );

        String json=objectMapper.writeValueAsString(paymentRequest);

        MvcResult mvcResult=mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .with(csrf())
                ).andExpect(status().isCreated())
                .andReturn();
        RazorpayPaymentResponse razorpayPaymentResponse=objectMapper.readValue(mvcResult.getResponse().getContentAsString(), RazorpayPaymentResponse.class);
        return razorpayPaymentResponse;
    }

    private RazorpayPaymentResponse verifyPayment(String gatewayOrderId) throws Exception{
        try (MockedStatic<Utils> mockedStatic=mockStatic(Utils.class)){
            razorpayClient.payments=mock(PaymentClient.class);
            com.razorpay.Payment razorpay_payment=new Payment(new JSONObject().put("status","captured"));
            when(razorpayClient.payments.fetch(anyString())).thenReturn(razorpay_payment);

            mockedStatic.when(()->Utils.verifyPaymentSignature(any(JSONObject.class),anyString())).thenReturn(true);

            PaymentVerifyRequest paymentVerifyRequest=new PaymentVerifyRequest(
                    gatewayOrderId,
                    "razorpay_payment_id",
                    "razorpay_signature"
            );
            String paymentVerifyRequestJson=objectMapper.writeValueAsString(paymentVerifyRequest);
            MvcResult mvcResult1=mockMvc.perform(post("/payment/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentVerifyRequestJson)
                            .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                            .with(csrf()))
                    .andReturn();
            RazorpayPaymentResponse verifyResponse=objectMapper.readValue(mvcResult1.getResponse().getContentAsString(), RazorpayPaymentResponse.class);
            return verifyResponse;
        }
    }
}
