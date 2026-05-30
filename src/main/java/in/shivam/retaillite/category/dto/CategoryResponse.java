package in.shivam.retaillite.category.dto;

import in.shivam.retaillite.product.entity.Product;
import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryResponse {

    private String categoryId;
    private String name;
    private String description;
    private String imgUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    //todo: return total items in the Category
}
