package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.dto.CreateReviewRequest;
import com.ecommerce.productservice.dto.ReviewDTO;
import com.ecommerce.productservice.dto.ReviewImageDTO;
import com.ecommerce.productservice.dto.ReviewSummaryDTO;
import com.ecommerce.productservice.entity.*;
import com.ecommerce.productservice.exception.DuplicateResourceException;
import com.ecommerce.productservice.exception.ResourceNotFoundException;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.productservice.repository.ProductReviewRepository;
import com.ecommerce.productservice.repository.ReviewReactionRepository;
import com.ecommerce.productservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ProductReviewRepository reviewRepository;
    private final ReviewReactionRepository reactionRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ReviewDTO createReview(UUID productId, CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (reviewRepository.existsByProductIdAndUserId(productId, request.getUserId())) {
            throw new DuplicateResourceException("User has already reviewed this product");
        }

        ProductReview review = ProductReview.builder()
                .product(product)
                .userId(request.getUserId())
                .userName(request.getUserName())
                .rating(request.getRating())
                .title(request.getTitle())
                .body(request.getBody())
                .build();

        if (request.getImages() != null) {
            request.getImages().forEach(imgReq -> {
                ReviewImage img = ReviewImage.builder()
                        .review(review)
                        .imageUrl(imgReq.getImageUrl())
                        .caption(imgReq.getCaption())
                        .sortOrder(imgReq.getSortOrder())
                        .build();
                review.getImages().add(img);
            });
        }

        ProductReview saved = reviewRepository.save(review);
        return mapToDTO(saved, request.getUserId());
    }

    @Override
    @Transactional
    public ReviewDTO updateReview(UUID reviewId, UUID userId, CreateReviewRequest request) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("User can only update their own review");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setBody(request.getBody());
        review.setUserName(request.getUserName());

        if (request.getImages() != null) {
            review.getImages().clear();
            request.getImages().forEach(imgReq -> {
                ReviewImage img = ReviewImage.builder()
                        .review(review)
                        .imageUrl(imgReq.getImageUrl())
                        .caption(imgReq.getCaption())
                        .sortOrder(imgReq.getSortOrder())
                        .build();
                review.getImages().add(img);
            });
        }

        ProductReview saved = reviewRepository.save(review);
        return mapToDTO(saved, userId);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId, UUID userId) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("User can only delete their own review");
        }

        reviewRepository.delete(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewDTO> getProductReviews(UUID productId, UUID viewerId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Page<ProductReview> reviews = reviewRepository.findByProductIdAndApprovedTrue(productId, pageable);
        return reviews.map(r -> mapToDTO(r, viewerId));
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryDTO getProductReviewSummary(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        List<ProductReview> reviews = reviewRepository.findByProductIdAndApprovedTrue(productId, Pageable.unpaged()).getContent();
        
        long totalReviews = reviews.size();
        if (totalReviews == 0) {
            return ReviewSummaryDTO.builder().build();
        }

        double avgRating = reviews.stream().mapToInt(ProductReview::getRating).average().orElse(0.0);
        
        long fiveStar = reviews.stream().filter(r -> r.getRating() == 5).count();
        long fourStar = reviews.stream().filter(r -> r.getRating() == 4).count();
        long threeStar = reviews.stream().filter(r -> r.getRating() == 3).count();
        long twoStar = reviews.stream().filter(r -> r.getRating() == 2).count();
        long oneStar = reviews.stream().filter(r -> r.getRating() == 1).count();

        return ReviewSummaryDTO.builder()
                .averageRating(avgRating)
                .totalReviews(totalReviews)
                .fiveStar(fiveStar)
                .fourStar(fourStar)
                .threeStar(threeStar)
                .twoStar(twoStar)
                .oneStar(oneStar)
                .build();
    }

    @Override
    @Transactional
    public void reactToReview(UUID reviewId, UUID userId, ReactionType type) {
        ProductReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        if (review.getUserId().equals(userId)) {
            throw new IllegalStateException("Users cannot react to their own reviews");
        }

        ReviewReaction reaction = reactionRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElse(ReviewReaction.builder()
                        .review(review)
                        .userId(userId)
                        .build());

        reaction.setType(type);
        reactionRepository.save(reaction);
    }

    @Override
    @Transactional
    public void removeReaction(UUID reviewId, UUID userId) {
        ReviewReaction reaction = reactionRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Reaction not found"));
        reactionRepository.delete(reaction);
    }

    private ReviewDTO mapToDTO(ProductReview review, UUID viewerId) {
        long likes = reactionRepository.countByReviewIdAndType(review.getId(), ReactionType.LIKE);
        long dislikes = reactionRepository.countByReviewIdAndType(review.getId(), ReactionType.DISLIKE);

        ReactionType myReaction = null;
        if (viewerId != null && !viewerId.equals(review.getUserId())) {
            myReaction = reactionRepository.findTypeByReviewIdAndUserId(review.getId(), viewerId).orElse(null);
        }

        return ReviewDTO.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .title(review.getTitle())
                .body(review.getBody())
                .verifiedPurchase(review.isVerifiedPurchase())
                .approved(review.isApproved())
                .likes(likes)
                .dislikes(dislikes)
                .myReaction(myReaction)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .images(review.getImages().stream().map(img ->
                        ReviewImageDTO.builder()
                                .id(img.getId())
                                .imageUrl(img.getImageUrl())
                                .caption(img.getCaption())
                                .sortOrder(img.getSortOrder())
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }
}
