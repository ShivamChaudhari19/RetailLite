package in.shivam.retaillite.category.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CategoryRequest {
    @NotBlank(message = "category name shouldn't be null")
    @Size(min = 2, max=25)
    private String name;

    @Size(max = 300, message = "description too long")
    private String description;
}
