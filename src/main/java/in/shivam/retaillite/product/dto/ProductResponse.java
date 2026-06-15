package in.shivam.retaillite.product.dto;

import in.shivam.retaillite.category.entity.Category;
import lombok.*;


import java.math.BigDecimal;
import java.sql.Timestamp;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductResponse {
    private String productId;
    private String name;
    private BigDecimal price;
    private BigDecimal taxRate;
    private String description;
    private String imgUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String  categoryId;
}
