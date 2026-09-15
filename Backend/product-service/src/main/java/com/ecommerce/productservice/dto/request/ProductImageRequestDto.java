package com.ecommerce.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageRequestDto {

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @NotNull(message = "isPrimary is required")
    private Boolean isPrimary;
}
