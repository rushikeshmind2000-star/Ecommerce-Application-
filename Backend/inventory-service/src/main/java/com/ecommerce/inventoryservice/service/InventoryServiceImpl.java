package com.ecommerce.inventoryservice.service;

import java.util.UUID;

import feign.RetryableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.inventoryservice.client.ProductClient;
import com.ecommerce.inventoryservice.client.dto.ProductResponse;
import com.ecommerce.inventoryservice.dto.ConfirmStockRequest;
import com.ecommerce.inventoryservice.dto.InventoryRequest;
import com.ecommerce.inventoryservice.dto.InventoryResponse;
import com.ecommerce.inventoryservice.dto.ReleaseStockRequest;
import com.ecommerce.inventoryservice.dto.ReserveStockRequest;
import com.ecommerce.inventoryservice.entity.Inventory;
import com.ecommerce.inventoryservice.entity.InventoryReservation;
import com.ecommerce.inventoryservice.enums.ReservationStatus;
import com.ecommerce.inventoryservice.exceptions.InsufficientStockException;
import com.ecommerce.inventoryservice.exceptions.InvalidProductResponseException;
import com.ecommerce.inventoryservice.exceptions.InventoryNotFoundException;
import com.ecommerce.inventoryservice.exceptions.ProductServiceUnavailableException;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.repository.InventoryReservationRepository;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;
    private final ProductClient productClient;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            InventoryReservationRepository reservationRepository,
            ProductClient productClient) {

        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
        this.productClient = productClient;
    }

    @Override
    @Transactional
    public void createStock(InventoryRequest request) {

        if (inventoryRepository.existsById(request.getProductId())) {
            throw new IllegalArgumentException(
                    "Inventory already exists for product: "
                            + request.getProductId());
        }

        ProductResponse product;
        try {
            product = productClient.getProductById(request.getProductId());
        } catch (RetryableException exception) {
            throw new ProductServiceUnavailableException(
                    "Product service is unavailable");
        }
        if (product == null || !request.getProductId().equals(product.getId())) {
            throw new InvalidProductResponseException(
                    "Product service returned invalid data for product: "
                            + request.getProductId());
        }

        Inventory inventory = new Inventory();

        inventory.setProductId(request.getProductId());
        inventory.setAvailableQuantity(request.getQuantity());
        inventory.setReservedQuantity(0);
        inventory.setSoldQuantity(0);

        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(UUID productId) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for product: "
                                        + productId));

        return new InventoryResponse(
                inventory.getProductId(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                inventory.getSoldQuantity());
    }

    @Override
    @Transactional
    public void reserveStock(ReserveStockRequest request) {

        Inventory inventory = inventoryRepository
                .findById(request.getProductId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for product: "
                                        + request.getProductId()));

        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: "
                            + request.getProductId());
        }

        if (reservationRepository
                .findByOrderId(request.getOrderId())
                .isPresent()) {
            return;
        }

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity()
                        - request.getQuantity());

        inventory.setReservedQuantity(
                inventory.getReservedQuantity()
                        + request.getQuantity());

        inventoryRepository.save(inventory);

        InventoryReservation reservation =
                new InventoryReservation();

        reservation.setOrderId(request.getOrderId());
        reservation.setProductId(request.getProductId());
        reservation.setQuantity(request.getQuantity());
        reservation.setStatus(ReservationStatus.RESERVED);

        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void releaseStock(ReleaseStockRequest request) {

        InventoryReservation reservation =
                reservationRepository
                        .findByOrderId(request.getOrderId())
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Reservation not found for order: "
                                                + request.getOrderId()));

        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {
            return;
        }

        Inventory inventory = inventoryRepository
                .findById(reservation.getProductId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for product: "
                                        + reservation.getProductId()));

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity()
                        + reservation.getQuantity());

        inventory.setReservedQuantity(
                inventory.getReservedQuantity()
                        - reservation.getQuantity());

        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.RELEASED);

        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void confirmStock(ConfirmStockRequest request) {

        InventoryReservation reservation =
                reservationRepository
                        .findByOrderId(request.getOrderId())
                        .orElseThrow(() ->
                                new InventoryNotFoundException(
                                        "Reservation not found for order: "
                                                + request.getOrderId()));

        if (reservation.getStatus()
                != ReservationStatus.RESERVED) {
            return;
        }

        Inventory inventory = inventoryRepository
                .findById(reservation.getProductId())
                .orElseThrow(() ->
                        new InventoryNotFoundException(
                                "Inventory not found for product: "
                                        + reservation.getProductId()));

        inventory.setReservedQuantity(
                inventory.getReservedQuantity()
                        - reservation.getQuantity());

        inventory.setSoldQuantity(
                inventory.getSoldQuantity()
                        + reservation.getQuantity());

        inventoryRepository.save(inventory);

        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.save(reservation);
    }
}