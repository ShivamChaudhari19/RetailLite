package in.shivam.retaillite.inventory.dto;

import lombok.*;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InventoryResponse {
    private String inventoryId;
    private String productName;
    private String productId;
    private Integer availableQuantity;
    private Integer lowStockThreshold;
    private Timestamp updatedAt;
}
