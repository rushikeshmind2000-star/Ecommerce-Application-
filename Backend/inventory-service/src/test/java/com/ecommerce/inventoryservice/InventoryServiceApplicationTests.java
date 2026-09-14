package com.ecommerce.inventoryservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.repository.InventoryRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private com.ecommerce.inventoryservice.Service.InventoryServiceImpl inventoryService;

    @Test
    void createInventory_ShouldCreateInventorySuccessfully() {

        UUID productId = UUID.randomUUID();

        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(10);

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setSoldQuantity(0);

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        InventoryResponse response = inventoryService.createInventory(request);

        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(10, response.getAvailableQuantity());
        assertEquals(0, response.getReservedQuantity());
        assertEquals(0, response.getSoldQuantity());

        verify(inventoryRepository, times(1))
                .save(any(Inventory.class));
    }
}