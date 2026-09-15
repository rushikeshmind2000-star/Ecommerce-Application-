package com.ecommerce.inventoryservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.inventoryservice.entity.Inventory;

public interface InventoryRepository
        extends JpaRepository<Inventory, UUID> {
}