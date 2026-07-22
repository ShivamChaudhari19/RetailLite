package in.shivam.retaillite.integration.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import in.shivam.retaillite.payment.domain.entity.Payment;
import in.shivam.retaillite.payment.dto.request.PaymentRequest;
import in.shivam.retaillite.payment.dto.response.CashPaymentResponse;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.Role;
import in.shivam.retaillite.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CashPaymentTest extends BaseIntegrationTest {

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
        invoice.setInvoiceItems(List.of(invoiceItem));
        invoiceRepository.save(invoice);
        invoiceItem.setInvoice(invoice);
        invoiceItemsRepository.save(invoiceItem);



        Integer availableQuantityBeforeSuccessPayment=inventory.getAvailableQuantity();
        expectedAvailableQuantityAfterSuccessfulPayment=availableQuantityBeforeSuccessPayment-invoiceItem.getQuantity();
    }

    @Test
    void shouldCompleteCashPaymentSuccessfully() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("INV-001", PaymentMethod.CASH);
        String json=objectMapper.writeValueAsString(paymentRequest);

        MvcResult mvcResult=mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                .with(csrf())
        ).andExpect(status().isCreated())
                .andReturn();
        CashPaymentResponse cashPaymentResponse=objectMapper.readValue(mvcResult.getResponse().getContentAsString(),CashPaymentResponse.class);
        assertEquals(PaymentStatus.SUCCESS,cashPaymentResponse.paymentStatus());
        assertEquals(PaymentMethod.CASH,cashPaymentResponse.paymentMethod());


    }

    @Test
    void shouldDeductInventory_WhenCashPaymentSucceeds() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("INV-001", PaymentMethod.CASH);
        String json=objectMapper.writeValueAsString(paymentRequest);

        MvcResult mvcResult=mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .with(csrf())
                ).andExpect(status().isCreated())
                .andReturn();
        CashPaymentResponse cashPaymentResponse=objectMapper.readValue(mvcResult.getResponse().getContentAsString(),CashPaymentResponse.class);
        assertEquals(PaymentStatus.SUCCESS,cashPaymentResponse.paymentStatus());
        assertEquals(PaymentMethod.CASH,cashPaymentResponse.paymentMethod());

        Inventory inventory=inventoryRepository.findByProduct_productId("PROD-001").orElseThrow();
        Integer realAvailableQuantityAfterSuccessfulPayment=inventory.getAvailableQuantity();
        assertEquals(expectedAvailableQuantityAfterSuccessfulPayment,realAvailableQuantityAfterSuccessfulPayment);

    }

    @Test
    void shouldCreatePaymentRecord_WhenCashPaymentSucceeds() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("INV-001", PaymentMethod.CASH);
        String json=objectMapper.writeValueAsString(paymentRequest);

        MvcResult mvcResult=mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .with(csrf())
                ).andExpect(status().isCreated())
                .andReturn();
        CashPaymentResponse cashPaymentResponse=objectMapper.readValue(mvcResult.getResponse().getContentAsString(),CashPaymentResponse.class);
        assertEquals(PaymentStatus.SUCCESS,cashPaymentResponse.paymentStatus());
        assertEquals(PaymentMethod.CASH,cashPaymentResponse.paymentMethod());

        Optional<Payment> payment=paymentRepository.findByPaymentId(cashPaymentResponse.paymentId());
        assertTrue(payment.isPresent());
    }
    @Test
    void shouldMarkInvoiceAsPaid_WhenCashPaymentSucceeds() throws Exception {
        PaymentRequest paymentRequest=new PaymentRequest("INV-001", PaymentMethod.CASH);
        String json=objectMapper.writeValueAsString(paymentRequest);

        MvcResult mvcResult=mockMvc.perform(post("/payment/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .with(csrf())
                ).andExpect(status().isCreated())
                .andReturn();
        CashPaymentResponse cashPaymentResponse=objectMapper.readValue(mvcResult.getResponse().getContentAsString(),CashPaymentResponse.class);
        assertEquals(PaymentStatus.SUCCESS,cashPaymentResponse.paymentStatus());
        assertEquals(PaymentMethod.CASH,cashPaymentResponse.paymentMethod());

        Invoice invoiceAfterPaymentIsSuccess=invoiceRepository.findByInvoiceId(cashPaymentResponse.invoiceId()).orElseThrow();

        assertEquals(InvoiceStatus.PAID,invoiceAfterPaymentIsSuccess.getInvoiceStatus());
    }
}
