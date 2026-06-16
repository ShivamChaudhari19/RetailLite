package in.shivam.retaillite.product.dto;

import in.shivam.retaillite.category.entity.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ProductRequest {
    @NotBlank
    @Size(min = 2, max = 50)
    private String name;
    @NotNull
    @Min(1)
    private BigDecimal price;
    @NotNull
    @Min(0)
    private BigDecimal taxRate;
    @Size(min = 2, max = 250)
    private String description;
    private String categoryId;
}
