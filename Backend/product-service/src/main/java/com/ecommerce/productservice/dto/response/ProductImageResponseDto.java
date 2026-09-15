package com.ecommerce.productservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageResponseDto {

    private UUID id;
    private String imageUrl;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
}
