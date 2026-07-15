package in.shivam.retaillite.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.product.dto.ProductRequest;
import in.shivam.retaillite.product.dto.ProductResponse;
import in.shivam.retaillite.product.service.ProductService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn201_WhenProductIsCreate() throws Exception {
        ProductRequest productRequest=new ProductRequest("valid name",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                "valid description",
                "valid category id");
        byte[] productRequestByte=objectMapper.writeValueAsBytes(productRequest);
        MockMultipartFile request=new MockMultipartFile(
                "product",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                productRequestByte
        );

        MockMultipartFile productImg= new MockMultipartFile(
                "productImg",
                "img.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "jpg image byte text example".getBytes()
        );

        ProductResponse productResponse=ProductResponse.builder()
                        .productId("demo product id").name(productRequest.getName()).price(productRequest.getPrice()).taxRate(productRequest.getTaxRate()).description(productRequest.getDescription()).imgUrl("url:// example.example").createdAt(null).updatedAt(null).categoryId("demo category id").build();
        when(productService.create(any(ProductRequest.class),any(MultipartFile.class))).thenReturn(productResponse  );
        mockMvc.perform(multipart("/product")
                .file(request)
                .file(productImg)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(productRequest.getName()));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404_WhenCategoryIsMissingForProductIsCreation() throws Exception {
        ProductRequest productRequest=new ProductRequest("valid name",
                BigDecimal.ONE,
                BigDecimal.ZERO,
                "valid description",
                "missing category id");
        byte[] productRequestByte=objectMapper.writeValueAsBytes(productRequest);
        MockMultipartFile request=new MockMultipartFile(
                "product",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                productRequestByte
        );

        MockMultipartFile productImg= new MockMultipartFile(
                "productImg",
                "img.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "jpg image byte text example".getBytes()
        );

        when(productService.create(any(ProductRequest.class),any(MultipartFile.class))).thenThrow(ResourceNotFoundException.class);
        mockMvc.perform(multipart("/product")
                        .file(request)
                        .file(productImg)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400_WhenProductRequestIsInvalid() throws Exception {
        ProductRequest invalidProductRequest=new ProductRequest(
                "v", //@NotBlank @Size(min = 2, max = 50)
                BigDecimal.ZERO, //@NotNull @Min(1)
                BigDecimal.ZERO, //    @NotNull @Min(0)
                "valid description", //    @Size(min = 2, max = 250)
                "" //    @NotBlank
        );
        byte[] productRequestByte=objectMapper.writeValueAsBytes(invalidProductRequest);
        MockMultipartFile request=new MockMultipartFile(
                "product",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                productRequestByte
        );

        MockMultipartFile productImg= new MockMultipartFile(
                "productImg",
                "img.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "jpg image byte text example".getBytes()
        );

        mockMvc.perform(multipart("/product")
                        .file(request)
                        .file(productImg)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn204_WhenProductIsDelete() throws Exception {
        String productId="valid product id";
        doNothing().when(productService).delete(anyString());
        mockMvc.perform(delete("/product/{productId}",productId).with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404_WhenProductIsMissingForDeletion() throws Exception {
        String productId="missing product id";
        doThrow(ResourceNotFoundException.class).when(productService).delete(anyString());
        mockMvc.perform(delete("/product/{productId}",productId).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200_WhenProductIsFetched() throws Exception {
        List<ProductResponse> productResponses=List.of();
        Page<ProductResponse> responsePage=new PageImpl<>(productResponses);
        String page="0";
        String size="10";
        String sortBy="name";
        String orderedBy="asc";

        when(productService.fetchAll(anyInt(),anyInt(),anyString(),anyString())).thenReturn(responsePage);
        mockMvc.perform(get("/product/products")
                .param("page",page)
                .param("size",size)
                .param("sortBy",sortBy)
                .param("orderedBy",orderedBy))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400_WhenProductFetchArgumentParamAreInvalid() throws Exception {
        List<ProductResponse> productResponses=List.of();
        Page<ProductResponse> responsePage=new PageImpl<>(productResponses);
        String invalidPage="-1";
        String invalidSize="140";
        String invalidSortBy="";
        String invalidOrderedBy="asc";

        mockMvc.perform(get("/product/products")
                        .param("page",invalidPage)
                        .param("size",invalidSize)
                        .param("sortBy",invalidSortBy)
                        .param("orderedBy",invalidOrderedBy))
                .andExpect(status().isBadRequest());
    }
}
