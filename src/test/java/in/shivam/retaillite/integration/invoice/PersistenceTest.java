package in.shivam.retaillite.integration.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.category.entity.Category;
import in.shivam.retaillite.category.repository.CategoryRepository;
import in.shivam.retaillite.integration.config.BaseIntegrationTest;
import in.shivam.retaillite.integration.factory.TestDataFactory;
import in.shivam.retaillite.integration.util.AuthenticationHelper;
import in.shivam.retaillite.inventory.repository.InventoryRepository;
import in.shivam.retaillite.invoice.dto.InvoiceItemRequest;
import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.entity.Invoice;
import in.shivam.retaillite.invoice.entity.InvoiceItem;
import in.shivam.retaillite.invoice.repository.InvoiceItemsRepository;
import in.shivam.retaillite.invoice.repository.InvoiceRepository;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PersistenceTest extends BaseIntegrationTest {
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

    @Autowired
    private InvoiceRepository invoiceRepository;

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
        inventoryRepository.save(dataFactory.getInventory(product));

    }

    @Test
    void shouldPersistInvoiceAndInvoiceItemsCorrectly() throws Exception {
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
        MvcResult mvcResult=mockMvc.perform(post("/invoices/invoice")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        String invoiceResponseString=mvcResult.getResponse().getContentAsString();
        InvoiceResponse invoiceResponse=objectMapper.readValue(invoiceResponseString,InvoiceResponse.class);


        Optional<Invoice> invoice=invoiceRepository.findByInvoiceId(invoiceResponse.invoiceId());
        assertTrue(invoice.isPresent());

    }
    @Test
    void shouldPersistInvoiceItems_WhenInvoiceCreated() throws Exception{
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
        MvcResult mvcResult=mockMvc.perform(post("/invoices/invoice")
                        .header(HttpHeaders.AUTHORIZATION,"Bearer "+token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isCreated()).andReturn();
        String invoiceResponseString=mvcResult.getResponse().getContentAsString();
        InvoiceResponse invoiceResponse=objectMapper.readValue(invoiceResponseString,InvoiceResponse.class);


        invoiceResponse.invoiceItems().forEach((invoiceItemResponse -> {
            Optional<InvoiceItem> invoiceItem=invoiceItemsRepository.findByInvoiceItemId(
                    invoiceItemResponse.invoiceItemId());
            assertTrue(invoiceItem.isPresent());
        }));
    }
}
