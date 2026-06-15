package in.shivam.retaillite.product.dto;

import in.shivam.retaillite.category.entity.Category;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductRequest {
    private String name;
    private BigDecimal price;
    private BigDecimal taxRate;
    private String description;
    private String categoryId;
}
