package in.shivam.retaillite.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.shivam.retaillite.category.dto.CategoryRequest;
import in.shivam.retaillite.category.dto.CategoryResponse;
import in.shivam.retaillite.category.exception.CategoryAlreadyExists;
import in.shivam.retaillite.category.service.CategoryService;
import in.shivam.retaillite.common.exception.ResourceNotFoundException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = CategoryController.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @MockitoBean
    private CategoryService categoryService;



    @Test
    @WithMockUser( username = "shivam", roles = "ADMIN")
    void shouldReturn200_ForCreate() throws Exception {
        CategoryRequest categoryRequest=new CategoryRequest("category name","valid description");
        byte[] categoryByte=objectMapper.writeValueAsBytes(categoryRequest);

        MockMultipartFile category=new MockMultipartFile(
                "category",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                categoryByte
        );


        MockMultipartFile categoryImg=new MockMultipartFile(
                "categoryImg",
                "mockFile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "xyz".getBytes()
        );
        CategoryResponse categoryResponse= CategoryResponse.builder()
                        .categoryId("categoryId").name("name").description("description").imgUrl("url://img url").createdAt(null).updatedAt(null).build();
        when(categoryService.create(any(CategoryRequest.class),any(MultipartFile.class))).thenReturn(categoryResponse);
        mockMvc.perform(multipart("/category")
                        .file(category)
                        .file(categoryImg)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").value("categoryId"))
                .andExpect(jsonPath("$.name").value("name"));
    }

    @Test
    @WithMockUser(username = "",roles = "ADMIN")
    void shouldReturn403_IfCategoryAlreadyExists() throws Exception {

        CategoryRequest request=new CategoryRequest("category name","description");
        byte[] requestJson=objectMapper.writeValueAsBytes(request);
        MockMultipartFile categoryRequestPart= new MockMultipartFile(
                "category",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson
        );
        MockMultipartFile categoryImg= new MockMultipartFile(
                "categoryImg",
                "",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "categoryImg".getBytes()
        );
        when(categoryService.create(any(CategoryRequest.class),any(MultipartFile.class))).thenThrow(CategoryAlreadyExists.class);

        mockMvc.perform(multipart("/category")
                        .file(categoryRequestPart)
                        .file(categoryImg)
                        .with(csrf())
                ).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "",roles = "ADMIN")
    void shouldReturn500_ForRuntimeException() throws Exception {

        CategoryRequest request=new CategoryRequest("category name","description");
        byte[] requestJson=objectMapper.writeValueAsBytes(request);
        MockMultipartFile categoryRequestPart= new MockMultipartFile(
                "category",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                requestJson
        );
        MockMultipartFile categoryImg= new MockMultipartFile(
                "categoryImg",
                "",
                MediaType.MULTIPART_FORM_DATA_VALUE,
                "categoryImg".getBytes()
        );
        when(categoryService.create(any(CategoryRequest.class),any(MultipartFile.class))).thenThrow(RuntimeException.class);

        mockMvc.perform(multipart("/category")
                .file(categoryRequestPart)
                .file(categoryImg)
                .with(csrf())
        ).andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn200_WhenRequestedPage() throws Exception {
        String page="0";
        String size="10";
        String sortBy="name";
        String orderedBy="asc";
        List<CategoryResponse> categories= List.of(

                CategoryResponse.builder().categoryId("catId").name("name").description("description").imgUrl("imgKey").createdAt(null).updatedAt(null).build(),
                CategoryResponse.builder().categoryId("catId1").name("name1").description("description1").imgUrl("imgKey1").createdAt(null).updatedAt(null).build()
                );
        Page<CategoryResponse> page0=new PageImpl<>(categories);
        when(categoryService.fetch(anyInt(),anyInt(),anyString(),anyString())).thenReturn(page0);
        mockMvc.perform(
                get("/category/categories")
                        .param("page",page)
                        .param("size", size)
                        .param("sortBy",sortBy)
                        .param("orderedBy",orderedBy)
                        .with(csrf())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalPages").value(page0.getTotalPages()) )
                .andExpect(jsonPath("$.page.totalElements").value(page0.getTotalElements()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn201_WhenDeleteCategory() throws Exception {
        doNothing().when(categoryService).delete(anyString());
        mockMvc.perform(delete("/category/{categoryId}","categoryId")
                .with(csrf())
        ).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404_WhenCategoryIsNotFound() throws Exception {
        doThrow(ResourceNotFoundException.class).when(categoryService).delete(anyString());
        mockMvc.perform(delete("/category/{categoryId}","missingCategoryId")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409_WhenProductsAreAssociatedWithCategory() throws Exception {
        doThrow(new CategoryAlreadyExists("Illegal operation: category has associated products")).when(categoryService).delete(anyString());
        mockMvc.perform(delete("/category/{categoryId}","categoryId").with(csrf()))
                .andExpect(status().isConflict());
    }
}
