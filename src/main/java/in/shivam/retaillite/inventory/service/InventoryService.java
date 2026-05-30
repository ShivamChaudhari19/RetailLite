package in.shivam.retaillite.inventory.service;

import in.shivam.retaillite.inventory.dto.StockUpdateRequest;
import in.shivam.retaillite.inventory.dto.InventoryResponse;
import in.shivam.retaillite.inventory.dto.ThresholdUpdateRequest;
import org.springframework.data.domain.Page;


import java.util.List;

public interface InventoryService {

    InventoryResponse addStock(String productId, StockUpdateRequest quantity);

    InventoryResponse getStock(String productId);

    Page<InventoryResponse> getAll(int page, int size, String sortBy, String orderedBy);

    InventoryResponse removeStock(String productId, StockUpdateRequest quantity);

    List<InventoryResponse> lowStock();

    InventoryResponse setThreshold(String productId, ThresholdUpdateRequest threshold);
}
