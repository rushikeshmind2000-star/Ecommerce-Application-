package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.dto.CreateReviewRequest;
import com.ecommerce.productservice.dto.ReviewDTO;
import com.ecommerce.productservice.dto.ReviewSummaryDTO;
import com.ecommerce.productservice.entity.ReactionType;
import com.ecommerce.productservice.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "⭐ Product Reviews", description = "Customer reviews and ratings — add, update, delete reviews and LIKE/DISLIKE reactions")
public class ReviewController {

    private final ReviewService reviewService;

    // ── Get Reviews (paginated) ───────────────────────────────────────────────
    @GetMapping("/{productId}/reviews")
    @Operation(
            summary = "Get reviews for a product",
            description = "Returns a paginated, sorted list of customer reviews for a specific product. Pass `viewerId` to include whether the viewer liked/disliked each review."
    )
    @ApiResponse(responseCode = "200", description = "Reviews returned")
    public ResponseEntity<Page<ReviewDTO>> getProductReviews(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID productId,
            @Parameter(description = "Viewer's user UUID (optional — used to show user's own reaction)") @RequestParam(required = false) UUID viewerId,
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field: createdAt | rating") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc | desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(reviewService.getProductReviews(productId, viewerId, pageable));
    }

    // ── Review Summary ────────────────────────────────────────────────────────
    @GetMapping("/{productId}/reviews/summary")
    @Operation(
            summary = "Get review summary for a product",
            description = "Returns aggregated stats: average rating, total review count, and per-star breakdown (1★ to 5★)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ReviewSummaryDTO> getProductReviewSummary(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID productId) {
        return ResponseEntity.ok(reviewService.getProductReviewSummary(productId));
    }

    // ── Create Review ─────────────────────────────────────────────────────────
    @PostMapping("/{productId}/reviews")
    @Operation(
            summary = "Submit a product review",
            description = "Allows a CUSTOMER to post a star rating + comment for a product. 🔒 Requires JWT (CUSTOMER role)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created"),
            @ApiResponse(responseCode = "400", description = "Validation error — rating must be 1–5"),
            @ApiResponse(responseCode = "409", description = "You have already reviewed this product")
    })
    public ResponseEntity<ReviewDTO> createReview(
            @Parameter(description = "Product UUID", required = true) @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request) {
        return new ResponseEntity<>(reviewService.createReview(productId, request), HttpStatus.CREATED);
    }

    // ── Update Review ─────────────────────────────────────────────────────────
    @PutMapping("/reviews/{reviewId}")
    @Operation(
            summary = "Update your review",
            description = "Allows a customer to edit their existing review. Only the original author can update. 🔒 Requires JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review updated"),
            @ApiResponse(responseCode = "403", description = "You cannot edit someone else's review"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewDTO> updateReview(
            @Parameter(description = "Review UUID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "User UUID (must match review author)", required = true) @RequestParam UUID userId,
            @Valid @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, userId, request));
    }

    // ── Delete Review ─────────────────────────────────────────────────────────
    @DeleteMapping("/reviews/{reviewId}")
    @Operation(
            summary = "Delete a review",
            description = "Deletes a review. Only the original author or an Admin can delete. 🔒 Requires JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Review deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden — not the author"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "Review UUID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "User UUID", required = true) @RequestParam UUID userId) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    // ── Add / Update Reaction ─────────────────────────────────────────────────
    @PostMapping("/reviews/{reviewId}/reactions")
    @Operation(
            summary = "Like or Dislike a review",
            description = "Adds or updates the current user's reaction (LIKE / DISLIKE) on a review. Calling again with a different type toggles the reaction. 🔒 Requires JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reaction saved"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<Void> reactToReview(
            @Parameter(description = "Review UUID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "User UUID", required = true) @RequestParam UUID userId,
            @Parameter(description = "Reaction type: LIKE or DISLIKE", required = true) @RequestParam ReactionType type) {
        reviewService.reactToReview(reviewId, userId, type);
        return ResponseEntity.ok().build();
    }

    // ── Remove Reaction ───────────────────────────────────────────────────────
    @DeleteMapping("/reviews/{reviewId}/reactions")
    @Operation(
            summary = "Remove your reaction from a review",
            description = "Removes the current user's LIKE or DISLIKE from a review. 🔒 Requires JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Reaction removed"),
            @ApiResponse(responseCode = "404", description = "Review or reaction not found")
    })
    public ResponseEntity<Void> removeReaction(
            @Parameter(description = "Review UUID", required = true) @PathVariable UUID reviewId,
            @Parameter(description = "User UUID", required = true) @RequestParam UUID userId) {
        reviewService.removeReaction(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
