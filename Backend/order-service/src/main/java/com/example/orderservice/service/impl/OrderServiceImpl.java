package com.example.orderservice.service.impl;

import com.example.orderservice.dto.OrderDTO;
import com.example.orderservice.entity.Order;
import com.example.orderservice.exception.OrderNotFoundException;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.service.OrderService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {

        Order order = new Order();

        order.setQuantity(orderDTO.getQuantity());
        order.setTotalAmount(orderDTO.getTotalAmount());

        // Default status
        order.setStatus("CREATED");

        Order savedOrder = orderRepository.save(order);

        return convertToDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        return convertToDTO(order);
    }

    @Override
    public List<OrderDTO> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public OrderDTO updateOrder(Long id, OrderDTO orderDTO) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        order.setQuantity(orderDTO.getQuantity());
        order.setTotalAmount(orderDTO.getTotalAmount());

        if (orderDTO.getStatus() != null) {
            order.setStatus(orderDTO.getStatus());
        }

        Order updatedOrder = orderRepository.save(order);

        return convertToDTO(updatedOrder);
    }

    @Override
    public void deleteOrder(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + id
                        )
                );

        orderRepository.delete(order);
    }

    // Entity → DTO
    private OrderDTO convertToDTO(Order order) {

        return new OrderDTO(
                order.getId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus()
        );
    }
}
