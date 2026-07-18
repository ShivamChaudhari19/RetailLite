package in.shivam.retaillite.invoice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.exception.QuantityOutOfBoundException;
import in.shivam.retaillite.invoice.dto.InvoiceItemRequest;
import in.shivam.retaillite.invoice.dto.InvoiceItemResponse;
import in.shivam.retaillite.invoice.dto.InvoiceRequest;
import in.shivam.retaillite.invoice.dto.InvoiceResponse;
import in.shivam.retaillite.invoice.entity.InvoiceStatus;
import in.shivam.retaillite.invoice.service.InvoicePdfService;
import in.shivam.retaillite.invoice.service.InvoiceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(InvoiceController.class)
class InvoiceControllerTest {

    @MockitoBean
    private InvoiceService invoiceService;
    @MockitoBean
    private InvoicePdfService invoicePdfService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    void shouldReturn201_WhenProductIsCreate() throws Exception {
        List<InvoiceItemRequest> invoiceItemRequests=List.of(
                new InvoiceItemRequest("product1",1),
                new InvoiceItemRequest("product2",1)
        );
        InvoiceRequest invoiceRequest=new InvoiceRequest(
                "customer",
                "0123456789",
                "customer@retaillite.com",
                invoiceItemRequests
        );
        String invoiceRequestJson=objectMapper.writeValueAsString(invoiceRequest);

        InvoiceResponse invoiceResponse=InvoiceResponse.builder()
                .invoiceId("invoice id")
                .invoiceItems(List.of(InvoiceItemResponse.builder().build()))
                .userName("user@retaillite.com")
                .customerName("customer")
                .customerNumber("0123456789")
                .customerEmail("customer@retaillite.com")
                .build();
        when(invoiceService.createInvoice(any(InvoiceRequest.class))).thenReturn(invoiceResponse);
        mockMvc.perform(post("/invoices/invoice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invoiceRequestJson)
                .with(csrf())
        ).andExpect(status().isCreated());
        ArgumentCaptor<InvoiceRequest> invoiceRequestArgumentCaptor=ArgumentCaptor.forClass(InvoiceRequest.class);
        verify(invoiceService).createInvoice(invoiceRequestArgumentCaptor.capture());
        InvoiceRequest invoiceRequest1=invoiceRequestArgumentCaptor.getValue();
        assertEquals(invoiceRequest.customerName(),invoiceRequest1.customerName());
        assertEquals(invoiceRequest.customerNumber(),invoiceRequest1.customerNumber());
        assertEquals(invoiceRequest.items(),invoiceRequest1.items());
    }
    @Test
    @WithMockUser(roles = {"USERS","ADMIN"})
    void shouldReturn404_WhenProductIsCreate() throws Exception {
        List<InvoiceItemRequest> invoiceItemRequests=List.of(
                new InvoiceItemRequest("product1",1),
                new InvoiceItemRequest("product2",1)
        );
        InvoiceRequest invoiceRequest=new InvoiceRequest(
                "customer",
                "0123456789",
                "customer@retaillite.com",
                invoiceItemRequests
        );
        String invoiceRequestJson=objectMapper.writeValueAsString(invoiceRequest);

        when(invoiceService.createInvoice(any(InvoiceRequest.class))).thenThrow(ResourceNotFoundException.class);
        mockMvc.perform(post("/invoices/invoice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invoiceRequestJson)
                .with(csrf())
        ).andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(roles = {"USERS","ADMIN"})
    void shouldReturn400_WhenRequestIsInvalid() throws Exception {
        List<InvoiceItemRequest> invoiceItemRequests=List.of(
                new InvoiceItemRequest(null,1),
                new InvoiceItemRequest("product2",0)
        );
        InvoiceRequest invoiceRequest=new InvoiceRequest(
                "customer",
                "00000000",
                "customer",
                invoiceItemRequests
        );
        String invoiceRequestJson=objectMapper.writeValueAsString(invoiceRequest);
        mockMvc.perform(post("/invoices/invoice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invoiceRequestJson)
                .with(csrf())
        ).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USERS","ADMIN"})
    void shouldReturn400_WhenProductIsCreate() throws Exception {
        List<InvoiceItemRequest> invoiceItemRequests=List.of(
                new InvoiceItemRequest("product1",1),
                new InvoiceItemRequest("product2",1)
        );
        InvoiceRequest invoiceRequest=new InvoiceRequest(
                "customer",
                "0123456789",
                "customer@retaillite.com",
                invoiceItemRequests
        );
        String invoiceRequestJson=objectMapper.writeValueAsString(invoiceRequest);

        when(invoiceService.createInvoice(any(InvoiceRequest.class))).thenThrow(QuantityOutOfBoundException.class);
        mockMvc.perform(post("/invoices/invoice")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invoiceRequestJson)
                .with(csrf())
        ).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"USERS","ADMIN"})
    void shouldReturnInvoices_WhenRequestIsValid() throws Exception {

        InvoiceResponse invoiceResponse=InvoiceResponse.builder()
                .invoiceId("invoice id").userName("user@retaillite.com").customerName("customer name").customerNumber("0123456789").customerEmail("customer@retaillite.com").build();
        Page<InvoiceResponse> page = new PageImpl<>(List.of(invoiceResponse));

        when(invoiceService.findAll(0, 15, "invoiceId", "asc"))
                .thenReturn(page);
        mockMvc.perform(get("/invoices")
                .param("page","0")
                .param("size","15")
                .param("sortBy","invoiceId")
                .param("orderedBy","asc")
                .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USERS","ADMIN"})
    void shouldReturnInvoice_WhenInvoiceIsRequested() throws Exception {
        InvoiceResponse invoice=InvoiceResponse.builder()
                        .invoiceId("invoice id").userName("user@retaillite.com").customerName("customer name").customerNumber("0123456789").customerEmail("customer@retaillite.com").build();
        when(invoiceService.findInvoice(anyString())).thenReturn(invoice);
        mockMvc.perform(get("/invoices/{invoiceId}","invoice id")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "PAID",
            "PENDING",
            "CANCELED"
    })
    @WithMockUser(roles = {"USERS","ADMIN"})
    void getInvoiceByStatus(String invoiceStatus) throws Exception {

        InvoiceResponse invoiceResponse=InvoiceResponse.builder()
                .invoiceStatus(InvoiceStatus.valueOf(invoiceStatus)).invoiceId("invoice id").userName("user@retaillite.com").customerName("customer").customerNumber("0123456789").customerEmail("customer@retaillite.com").build();
        Page<InvoiceResponse> invoiceResponsePage=new PageImpl<>(List.of(invoiceResponse));
        when(invoiceService.findByInvoiceStatus(anyInt(),anyInt(),anyString())).thenReturn(invoiceResponsePage  );

        mockMvc.perform(get("/invoices/status/{status}",invoiceStatus )
                .with(csrf())
        ).andExpect(status().isOk());
        ArgumentCaptor<String>captor=ArgumentCaptor.forClass(String.class);
        verify(invoiceService).findByInvoiceStatus(anyInt(),anyInt(),captor.capture());
        assertEquals(invoiceStatus,captor.getValue());
    }
}