package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.dto.request.CategoryRequestDto;
import com.ecommerce.productservice.dto.request.ProductImageRequestDto;
import com.ecommerce.productservice.dto.request.ProductRequestDto;
import com.ecommerce.productservice.dto.request.ProductUpdateRequestDto;
import com.ecommerce.productservice.dto.response.CategoryResponseDto;
import com.ecommerce.productservice.dto.response.ProductImageResponseDto;
import com.ecommerce.productservice.dto.response.ProductResponseDto;
import com.ecommerce.productservice.entity.Category;
import com.ecommerce.productservice.entity.Product;
import com.ecommerce.productservice.entity.ProductImage;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        Category category = resolveCategory(productRequestDto);

        Product product = Product.builder()
                .name(productRequestDto.getName())
                .description(productRequestDto.getDescription())
                .sku(productRequestDto.getSku())
                .price(productRequestDto.getPrice())
                .currency(productRequestDto.getCurrency())
                .status(productRequestDto.getStatus())
                .brand(productRequestDto.getBrand())
                .category(category)
                .build();

        if (productRequestDto.getImages() != null) {
            for (ProductImageRequestDto imgDto : productRequestDto.getImages()) {
                ProductImage image = ProductImage.builder()
                        .imageUrl(imgDto.getImageUrl())
                        .isPrimary(imgDto.getIsPrimary())
                        .build();
                product.addImage(image);
            }
        }

        Product savedProduct = productRepository.save(product);
        return mapToResponseDto(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new com.ecommerce.productservice.exception.ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponseDto(product);
    }

    @Override
    @Transactional
    public ProductResponseDto updateProduct(ProductUpdateRequestDto productUpdateRequestDto) {
        UUID targetId = productUpdateRequestDto.getId();
        Product existingProduct = productRepository.findById(targetId)
                .orElseThrow(() -> new com.ecommerce.productservice.exception.ResourceNotFoundException("Product not found with id: " + targetId));

        if (productUpdateRequestDto.getCategoryId() != null || productUpdateRequestDto.getCategory() != null) {
            Category category = resolveCategoryForUpdate(productUpdateRequestDto);
            existingProduct.setCategory(category);
        }

        if (productUpdateRequestDto.getName() != null) {
            existingProduct.setName(productUpdateRequestDto.getName());
        }
        if (productUpdateRequestDto.getDescription() != null) {
            existingProduct.setDescription(productUpdateRequestDto.getDescription());
        }
        if (productUpdateRequestDto.getSku() != null) {
            existingProduct.setSku(productUpdateRequestDto.getSku());
        }
        if (productUpdateRequestDto.getPrice() != null) {
            existingProduct.setPrice(productUpdateRequestDto.getPrice());
        }
        if (productUpdateRequestDto.getCurrency() != null) {
            existingProduct.setCurrency(productUpdateRequestDto.getCurrency());
        }
        if (productUpdateRequestDto.getStatus() != null) {
            existingProduct.setStatus(productUpdateRequestDto.getStatus());
        }
        if (productUpdateRequestDto.getBrand() != null) {
            existingProduct.setBrand(productUpdateRequestDto.getBrand());
        }

        if (productUpdateRequestDto.getImages() != null) {
            existingProduct.getImages().clear();
            for (ProductImageRequestDto imgDto : productUpdateRequestDto.getImages()) {
                ProductImage image = ProductImage.builder()
                        .imageUrl(imgDto.getImageUrl())
                        .isPrimary(imgDto.getIsPrimary())
                        .build();
                existingProduct.addImage(image);
            }
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToResponseDto(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new com.ecommerce.productservice.exception.ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private Category resolveCategory(ProductRequestDto productRequestDto) {
        if (productRequestDto.getCategoryId() != null) {
            return categoryRepository.findById(productRequestDto.getCategoryId())
                    .orElseThrow(() -> new com.ecommerce.productservice.exception.ResourceNotFoundException("Category not found with id: " + productRequestDto.getCategoryId()));
        } else if (productRequestDto.getCategory() != null) {
            CategoryRequestDto catDto = productRequestDto.getCategory();
            Category category = Category.builder()
                    .name(catDto.getName())
                    .description(catDto.getDescription())
                    .status(catDto.getStatus())
                    .build();
            return categoryRepository.save(category);
        } else {
            throw new com.ecommerce.productservice.exception.ResourceCreationException("Category information must be provided (categoryId or category DTO)");
        }
    }

    private Category resolveCategoryForUpdate(ProductUpdateRequestDto productUpdateRequestDto) {
        if (productUpdateRequestDto.getCategoryId() != null) {
            return categoryRepository.findById(productUpdateRequestDto.getCategoryId())
                    .orElseThrow(() -> new com.ecommerce.productservice.exception.ResourceNotFoundException("Category not found with id: " + productUpdateRequestDto.getCategoryId()));
        } else if (productUpdateRequestDto.getCategory() != null) {
            CategoryRequestDto catDto = productUpdateRequestDto.getCategory();
            Category category = Category.builder()
                    .name(catDto.getName())
                    .description(catDto.getDescription())
                    .status(catDto.getStatus())
                    .build();
            return categoryRepository.save(category);
        } else {
            throw new com.ecommerce.productservice.exception.ResourceCreationException("Category information must be provided (categoryId or category DTO)");
        }
    }

    private ProductResponseDto mapToResponseDto(Product product) {
        CategoryResponseDto categoryDto = null;
        if (product.getCategory() != null) {
            Category cat = product.getCategory();
            categoryDto = CategoryResponseDto.builder()
                    .id(cat.getId())
                    .name(cat.getName())
                    .description(cat.getDescription())
                    .status(cat.getStatus())
                    .createdAt(cat.getCreatedAt())
                    .updatedAt(cat.getUpdatedAt())
                    .build();
        }

        List<ProductImageResponseDto> imageDtos = new ArrayList<>();
        if (product.getImages() != null) {
            imageDtos = product.getImages().stream()
                    .map(img -> ProductImageResponseDto.builder()
                            .id(img.getId())
                            .imageUrl(img.getImageUrl())
                            .isPrimary(img.getIsPrimary())
                            .createdAt(img.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .sku(product.getSku())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .status(product.getStatus())
                .brand(product.getBrand())
                .category(categoryDto)
                .images(imageDtos)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
