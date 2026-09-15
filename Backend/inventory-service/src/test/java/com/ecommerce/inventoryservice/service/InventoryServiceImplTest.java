package com.ecommerce.inventoryservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.inventoryservice.dto.ConfirmStockRequest;
import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.dto.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.ReserveStockRequest;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.entity.InventoryReservation;
import com.ecommerce.inventoryservice.enums.ReservationStatus;
import com.ecommerce.inventoryservice.exceptions.InsufficientStockException;
import com.ecommerce.inventoryservice.exceptions.InventoryNotFoundException;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.repository.InventoryReservationRepository;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryReservationRepository reservationRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void createStock_ShouldCreateStockSuccessfully() {

        UUID productId = UUID.randomUUID();

        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(10);

        when(inventoryRepository.existsById(productId))
                .thenReturn(false);

        inventoryService.createStock(request);

        verify(inventoryRepository, times(1))
                .save(any(Inventory.class));
    }

    @Test
    void createStock_ShouldThrowException_WhenInventoryAlreadyExists() {

        UUID productId = UUID.randomUUID();

        InventoryRequest request = new InventoryRequest();
        request.setProductId(productId);
        request.setQuantity(10);

        when(inventoryRepository.existsById(productId))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> inventoryService.createStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void getInventory_ShouldReturnInventorySuccessfully() {

        UUID productId = UUID.randomUUID();

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(2);
        inventory.setSoldQuantity(3);

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.of(inventory));

        InventoryResponse response =
                inventoryService.getInventory(productId);

        assertNotNull(response);
        assertEquals(productId, response.getProductId());
        assertEquals(10, response.getAvailableQuantity());
        assertEquals(2, response.getReservedQuantity());
        assertEquals(3, response.getSoldQuantity());

        verify(inventoryRepository, times(1))
                .findById(productId);
    }

    @Test
    void getInventory_ShouldThrowException_WhenInventoryNotFound() {

        UUID productId = UUID.randomUUID();

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.getInventory(productId)
        );
    }

    @Test
    void reserveStock_ShouldReserveStockSuccessfully() {

        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ReserveStockRequest request = new ReserveStockRequest();
        request.setProductId(productId);
        request.setOrderId(orderId);
        request.setQuantity(3);

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setSoldQuantity(0);

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.of(inventory));

        when(reservationRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        inventoryService.reserveStock(request);

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(3, inventory.getReservedQuantity());

        verify(inventoryRepository, times(1))
                .save(inventory);

        verify(reservationRepository, times(1))
                .save(any(InventoryReservation.class));
    }

    @Test
    void reserveStock_ShouldThrowException_WhenInventoryNotFound() {

        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ReserveStockRequest request = new ReserveStockRequest();
        request.setProductId(productId);
        request.setOrderId(orderId);
        request.setQuantity(3);

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.reserveStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void reserveStock_ShouldThrowException_WhenStockIsInsufficient() {

        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ReserveStockRequest request = new ReserveStockRequest();
        request.setProductId(productId);
        request.setOrderId(orderId);
        request.setQuantity(15);

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setSoldQuantity(0);

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.of(inventory));

        assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reserveStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(reservationRepository, never())
                .save(any(InventoryReservation.class));
    }

    @Test
    void reserveStock_ShouldNotCreateDuplicateReservation() {

        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ReserveStockRequest request = new ReserveStockRequest();
        request.setProductId(productId);
        request.setOrderId(orderId);
        request.setQuantity(3);

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setSoldQuantity(0);

        InventoryReservation reservation =
                new InventoryReservation();

        reservation.setOrderId(orderId);
        reservation.setProductId(productId);
        reservation.setQuantity(3);
        reservation.setStatus(ReservationStatus.RESERVED);

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.of(inventory));

        when(reservationRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(reservation));

        inventoryService.reserveStock(request);

        assertEquals(10, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(reservationRepository, never())
                .save(any(InventoryReservation.class));
    }

    @Test
    void releaseStock_ShouldReleaseStockSuccessfully() {

        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ReleaseStockRequest request =
                new ReleaseStockRequest();

        request.setOrderId(orderId);

        InventoryReservation reservation =
                new InventoryReservation();

        reservation.setOrderId(orderId);
        reservation.setProductId(productId);
        reservation.setQuantity(3);
        reservation.setStatus(ReservationStatus.RESERVED);

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(7);
        inventory.setReservedQuantity(3);
        inventory.setSoldQuantity(0);

        when(reservationRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(reservation));

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.of(inventory));

        inventoryService.releaseStock(request);

        assertEquals(10, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(
                ReservationStatus.RELEASED,
                reservation.getStatus());

        verify(inventoryRepository, times(1))
                .save(inventory);

        verify(reservationRepository, times(1))
                .save(reservation);
    }

    @Test
    void releaseStock_ShouldThrowException_WhenReservationNotFound() {

        UUID orderId = UUID.randomUUID();

        ReleaseStockRequest request =
                new ReleaseStockRequest();

        request.setOrderId(orderId);

        when(reservationRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.releaseStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }

    @Test
    void confirmStock_ShouldConfirmStockSuccessfully() {

        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        ConfirmStockRequest request =
                new ConfirmStockRequest();

        request.setOrderId(orderId);

        InventoryReservation reservation =
                new InventoryReservation();

        reservation.setOrderId(orderId);
        reservation.setProductId(productId);
        reservation.setQuantity(3);
        reservation.setStatus(ReservationStatus.RESERVED);

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(7);
        inventory.setReservedQuantity(3);
        inventory.setSoldQuantity(0);

        when(reservationRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(reservation));

        when(inventoryRepository.findById(productId))
                .thenReturn(Optional.of(inventory));

        inventoryService.confirmStock(request);

        assertEquals(7, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(3, inventory.getSoldQuantity());

        assertEquals(
                ReservationStatus.CONFIRMED,
                reservation.getStatus());

        verify(inventoryRepository, times(1))
                .save(inventory);

        verify(reservationRepository, times(1))
                .save(reservation);
    }

    @Test
    void confirmStock_ShouldThrowException_WhenReservationNotFound() {

        UUID orderId = UUID.randomUUID();

        ConfirmStockRequest request =
                new ConfirmStockRequest();

        request.setOrderId(orderId);

        when(reservationRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.confirmStock(request)
        );

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }
}