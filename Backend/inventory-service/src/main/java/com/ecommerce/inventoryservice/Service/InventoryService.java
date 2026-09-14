package com.ecommerce.inventoryservice.Service;

import java.util.UUID;

import com.ecommerce.inventoryservice.dto.ConfirmStockRequest;
import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.dto.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.ReserveStockRequest;

public interface InventoryService {

    InventoryResponse getInventory(UUID productId);

    void createStock(InventoryRequest request);

    void reserveStock(ReserveStockRequest request);

    void releaseStock(ReleaseStockRequest request);

    void confirmStock(ConfirmStockRequest request);
}