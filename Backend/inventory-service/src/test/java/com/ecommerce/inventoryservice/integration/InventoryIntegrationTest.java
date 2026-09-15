package com.ecommerce.inventoryservice.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.service.InventoryService;

@SpringBootTest
class InventoryIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Test
    void createAndGetInventory_ShouldWork() {

        UUID productId = UUID.randomUUID();

        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(50);

        inventoryService.createStock(request);

        InventoryResponse response =
                inventoryService.getInventory(productId);

        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(50, response.getAvailableQuantity());
        assertEquals(0, response.getReservedQuantity());
        assertEquals(0, response.getSoldQuantity());
    }
}