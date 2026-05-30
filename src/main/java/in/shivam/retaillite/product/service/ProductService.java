package in.shivam.retaillite.product.service;

import in.shivam.retaillite.common.exception.ResourceNotFoundException;
import in.shivam.retaillite.product.dto.ProductRequest;
import in.shivam.retaillite.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    ProductResponse create(ProductRequest request, MultipartFile productImg);

    void delete(String productId) throws ResourceNotFoundException;

    Page<ProductResponse> fetchAll(int page, int size, String sortBy, String orderedBy);
}
