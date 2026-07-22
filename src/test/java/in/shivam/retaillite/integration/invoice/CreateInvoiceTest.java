package in.shivam.retaillite.integration.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.repository.CategoryRepository;
import in.shivam.retaillite.integration.config.BaseIntegrationTest;
import in.shivam.retaillite.integration.factory.TestDataFactory;
import in.shivam.retaillite.integration.util.AuthenticationHelper;
import in.shivam.retaillite.inventory.entity.Inventory;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.invoice.dto.InvoiceItemRequest;
import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.repository.InvoiceItemsRepository;
import in.shivam.retaillite.product.entity.Product;
import in.shivam.retaillite.product.repository.ProductRepository;
import in.shivam.retaillite.user.UserRepository;
import in.shivam.retaillite.user.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateInvoiceTest extends BaseIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthenticationHelper authenticationHelper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestDataFactory dataFactory;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private InvoiceItemsRepository invoiceItemsRepository;
    @Autowired
    private UserRepository userRepository;


    private static final String username="shivam@retaillite.com";
    private static final String password="shivam";
    @BeforeEach
    void setUp(){
        userRepository.save(
                dataFactory.getUser(
                        username,
                        password,
                        Role.ROLE_USER
                )
        );
        Category category=categoryRepository.save(dataFactory.getCategory());
        Product product=productRepository.save(dataFactory.getProduct(category));
        Inventory inventory=inventoryRepository.save(dataFactory.getInventory(product));

    }

    @Test
    void shouldCreateInvoiceSuccessfully_WhenValidRequestProvided() throws Exception {
        InvoiceItemRequest invoiceItemRequest=new InvoiceItemRequest(
                "PROD-001",
                2
        );
        InvoiceRequest invoiceRequest= new InvoiceRequest(
                "customer",
                "0123456789",
                "customer@retaillite.com",
                List.of(invoiceItemRequest)
        );

        String token=authenticationHelper.login(mockMvc,username,password);
        String json=objectMapper.writeValueAsString(invoiceRequest);
        mockMvc.perform(post("/invoices/invoice")
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCalculateInvoiceTotalsCorrectly_WhenInvoiceCreated() throws Exception {
        InvoiceItemRequest invoiceItemRequest=new InvoiceItemRequest(
                "PROD-001",
                2
        );
        InvoiceRequest invoiceRequest= new InvoiceRequest(
                "customer",
                "0123456789",
                "customer@retaillite.com",
                List.of(invoiceItemRequest)
        );
        String token=authenticationHelper.login(mockMvc,username,password);
        String json=objectMapper.writeValueAsString(invoiceRequest);
        mockMvc.perform(post("/invoices/invoice")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.grandTotal").value(2360));
    }

}
