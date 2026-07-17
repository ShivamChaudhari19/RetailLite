package in.shivam.retaillite.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.inventory.dto.InventoryResponse;
import in.shivam.retaillite.inventory.dto.StockUpdateRequest;
import in.shivam.retaillite.inventory.dto.ThresholdUpdateRequest;
import in.shivam.retaillite.inventory.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @MockitoBean
    private InventoryService inventoryService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturn201_WhenStockIsAdded() throws Exception {
        String productId="product001";
        StockUpdateRequest quantity=new StockUpdateRequest(10);
        String quantityJson=objectMapper.writeValueAsString(quantity);
        InventoryResponse inventoryResponse=InventoryResponse.builder()
                        .inventoryId("inventory-xxx").productName("product name").productId("product-xxx").availableQuantity(10).lowStockThreshold(5).build();
        when(inventoryService.addStock(anyString(),any(StockUpdateRequest.class))).thenReturn(inventoryResponse);
        mockMvc.perform(post("/inventory/{productId}/stock/add",productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(quantityJson)
                .with(csrf())
        ).andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturn404_WhenResourceNotFound() throws Exception {
        String productId="product001";
        StockUpdateRequest quantity=new StockUpdateRequest(10);
        String quantityJson=objectMapper.writeValueAsString(quantity);

        when(inventoryService.addStock(anyString(),any(StockUpdateRequest.class))).thenThrow(ResourceNotFoundException.class);
        mockMvc.perform(post("/inventory/{productId}/stock/add",productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(quantityJson)
                .with(csrf())
        ).andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnStock_WhenProductIdIsValid() throws Exception {

        String productId="product001";

        InventoryResponse inventoryResponse=InventoryResponse.builder()
                .inventoryId("inventory-xxx").productName("product name").productId("product-xxx").availableQuantity(10).lowStockThreshold(5).build();
        when(inventoryService.getStock(anyString())).thenReturn(inventoryResponse);
        mockMvc.perform(get("/inventory/{productId}/stock",productId)
                .with(csrf())
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnStockPage_WhenPageRequestIsValid() throws Exception {
        List<InventoryResponse> inventoryResponses=List.of();
        Page<InventoryResponse>inventoryResponsePage=new PageImpl<>(inventoryResponses);

        when(inventoryService.getAll(anyInt(),anyInt(),anyString(),anyString())).thenReturn(inventoryResponsePage);
        mockMvc.perform(get("/inventory/stock")
                .param("page","0")
                .param("size","10")
                .param("sortBy","availableQuantity")
                .param("orderedBy","ASC")
                .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<Integer>pageCaptor=ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer>sizeCaptor=ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String>sortByCaptor=ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String>orderedByCaptor=ArgumentCaptor.forClass(String.class);

        verify(inventoryService).getAll(pageCaptor.capture(),sizeCaptor.capture(),sortByCaptor.capture(),orderedByCaptor.capture());
        assertEquals(0,pageCaptor.getValue());
        assertEquals(10,     sizeCaptor.getValue());
        assertEquals(   "availableQuantity",sortByCaptor.getValue());
        assertEquals("ASC",orderedByCaptor.getValue());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturn204_WhenStockIsRemoved() throws Exception {
        StockUpdateRequest quantity=new StockUpdateRequest(10);
        String quantityJson=objectMapper.writeValueAsString(quantity);
        InventoryResponse inventoryResponse=InventoryResponse.builder()
                .inventoryId("inventory-xxx").productName("product name").productId("product-xxx").availableQuantity(0).lowStockThreshold(5).build();

        when(inventoryService.removeStock(anyString(),any(StockUpdateRequest.class))).thenReturn(inventoryResponse);
        mockMvc.perform(post("/inventory/{productId}/stock/remove","productId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(quantityJson)
                .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<String>productIdCaptor=ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<StockUpdateRequest>stockUpdateRequestArgumentCaptor=ArgumentCaptor.forClass(StockUpdateRequest.class);
        verify(inventoryService).removeStock(productIdCaptor.capture(),stockUpdateRequestArgumentCaptor.capture());
        assertEquals("productId",productIdCaptor.getValue());
        assertEquals(quantity,stockUpdateRequestArgumentCaptor.getValue());

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void setThreshold() throws Exception {
        ThresholdUpdateRequest quantity=new ThresholdUpdateRequest(10);
        String quantityJson=objectMapper.writeValueAsString(quantity);
        InventoryResponse inventoryResponse=InventoryResponse.builder()
                .inventoryId("inventory-xxx").productName("product name").productId("product-xxx").availableQuantity(0).lowStockThreshold(10).build();

        when(inventoryService.setThreshold(anyString(),any(ThresholdUpdateRequest.class))).thenReturn(inventoryResponse);
        mockMvc.perform(patch("/inventory/{productId}/stock/low/threshold","productId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(quantityJson)
                        .with(csrf()))
                .andExpect(status().isOk());
        ArgumentCaptor<String>productIdCaptor=ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ThresholdUpdateRequest>thresholdUpdateRequestArgumentCaptor=ArgumentCaptor.forClass(ThresholdUpdateRequest.class);
        verify(inventoryService).setThreshold(productIdCaptor.capture(),thresholdUpdateRequestArgumentCaptor.capture());
        assertEquals("productId",productIdCaptor.getValue());
        assertEquals(quantity,thresholdUpdateRequestArgumentCaptor.getValue());

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void lowStock() throws Exception {
        InventoryResponse inventoryResponse=InventoryResponse.builder()
                .inventoryId("inventory-xxx").productName("product name").productId("product-xxx").availableQuantity(0).lowStockThreshold(5).build();

        when(inventoryService.lowStock()).thenReturn(List.of(inventoryResponse));
        mockMvc.perform(get("/inventory/stock/low")
                        .with(csrf()))
                .andExpect(status().isOk());

    }
}