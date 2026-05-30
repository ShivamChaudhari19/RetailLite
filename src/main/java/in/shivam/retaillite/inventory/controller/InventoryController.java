package in.shivam.retaillite.inventory.controller;

import in.shivam.retaillite.inventory.dto.StockUpdateRequest;
import in.shivam.retaillite.inventory.dto.InventoryResponse;
import in.shivam.retaillite.inventory.dto.ThresholdUpdateRequest;
import in.shivam.retaillite.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@PreAuthorize(value = "hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/{productId}/stock/add")
    public ResponseEntity<InventoryResponse> addStock(
            @PathVariable String productId,
            @Valid @RequestBody StockUpdateRequest quantity
            ){return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        inventoryService.addStock(productId,quantity)
                );
    }


    @GetMapping("/{productId}/stock")
    public ResponseEntity<InventoryResponse>getStock(@PathVariable String productId){
        return ResponseEntity.ok(inventoryService.getStock(productId));
    }


    @GetMapping("/stock")
    public ResponseEntity<Page<InventoryResponse>> getAllStock(
            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "15")
            int size,

            @RequestParam(defaultValue = "availableQuantity")
            String sortBy,

            @RequestParam(defaultValue = "ASC")
            String orderedBy
    ){
        return ResponseEntity.ok(
                inventoryService.getAll(
                        page,
                        size,
                        sortBy,
                        orderedBy
                )
        );
    }


    @PostMapping("/{productId}/stock/remove")
    public ResponseEntity<InventoryResponse> removeStock(@PathVariable String productId,
                            @Valid @RequestBody StockUpdateRequest stockRequest
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        inventoryService.removeStock(productId, stockRequest)
                );
    }


    @PatchMapping("/{productId}/stock/low/threshold")
    public ResponseEntity<InventoryResponse> setThreshold(
            @PathVariable   String productId,
            @RequestBody ThresholdUpdateRequest threshold){
        return ResponseEntity.ok(
                inventoryService.setThreshold(productId,  threshold)
        );
    }


    @GetMapping("/stock/low")
    public ResponseEntity<List<InventoryResponse>> lowStock(){
        return ResponseEntity.ok(
        inventoryService.lowStock());
    }
}
