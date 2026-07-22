package in.shivam.retaillite.integration.invoice;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.integration.config.BaseIntegrationTest;
import in.shivam.retaillite.invoice.dto.InvoiceItemRequest;
import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectAnonymousUserCreatingInvoice() throws Exception {
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

        String json=objectMapper.writeValueAsString(invoiceRequest);
        mockMvc.perform(post("/invoices/invoice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}

