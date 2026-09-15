package com.ecommerce.inventoryservice.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryServiceImpl(
            InventoryRepository inventoryRepository,
            InventoryReservationRepository reservationRepository) {

        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    @Transactional
    public void createStock(InventoryRequest request) {

        if (inventoryRepository.existsById(request.getProductId())) {
            throw new IllegalArgumentException(
                    "Inventory already exists for product: "
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