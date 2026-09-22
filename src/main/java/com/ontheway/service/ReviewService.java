package com.ontheway.service;

import com.ontheway.dto.request.ReviewSaveRequestDto;
import com.ontheway.dto.response.ReviewListResponseDto;
import com.ontheway.dto.response.ReviewSaveResponseDto;
import com.ontheway.entity.DeliveryOrder;
import com.ontheway.entity.Request;
import com.ontheway.entity.Review;
import com.ontheway.entity.User;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.repository.DeliveryOrderRepository;
import com.ontheway.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final BigDecimal RATING_STEP = BigDecimal.valueOf(0.5);
    private static final BigDecimal RATING_MIN = BigDecimal.ZERO;
    private static final BigDecimal RATING_MAX = BigDecimal.valueOf(5);

    private final ReviewRepository reviewRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;

    @Transactional
    public ReviewSaveResponseDto create(Long reviewerId, ReviewSaveRequestDto dto) {
        DeliveryOrder order = deliveryOrderRepository.findByIdWithParties(dto.getBoardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.isCompleted()) {
            throw new BusinessException(ErrorCode.DELIVERY_NOT_COMPLETED);
        }

        Request request = order.getRequest();
        User requester = request.getRequester();
        User deliverer = request.getDeliverer();

        User reviewer;
        if (reviewerId.equals(requester.getId())) {
            reviewer = requester;
        } else if (reviewerId.equals(deliverer.getId())) {
            reviewer = deliverer;
        } else {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (reviewRepository.existsByOrderIdAndReviewerId(order.getId(), reviewerId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }
        BigDecimal rating = validateRating(dto.getRating());

        Review review = Review.builder()
                .order(order)
                .reviewer(reviewer)
                .rating(rating)
                .content(dto.getContent())
                .build();
        reviewRepository.save(review);

        return ReviewSaveResponseDto.builder()
                .createdAt(LocalDateTime.now())
                .build();
    }

    private BigDecimal validateRating(Double rating) {
        if (rating == null) {
            throw new BusinessException(ErrorCode.INVALID_RATING);
        }
        BigDecimal value = BigDecimal.valueOf(rating).setScale(1, RoundingMode.HALF_UP);
        boolean inRange = value.compareTo(RATING_MIN) >= 0 && value.compareTo(RATING_MAX) <= 0;
        boolean isHalfStep = value.remainder(RATING_STEP).compareTo(BigDecimal.ZERO) == 0;
        if (!inRange || !isHalfStep) {
            throw new BusinessException(ErrorCode.INVALID_RATING);
        }
        return value;
    }

    // 내가 받은 후기 목록
    public ReviewListResponseDto getMyReceivedReviews(Long userId, Pageable pageable) {
        Slice<Review> reviews = reviewRepository.findReceived(userId, pageable);

        List<ReviewListResponseDto.Review> items = reviews.getContent().stream()
                .map(review -> ReviewListResponseDto.Review.builder()
                        .reviewId(review.getId())
                        .reviewContent(review.getContent())
                        .rating(review.getRating())
                        .reviewerImage(review.getReviewer().getProfileImageUrl())
                        .reviewerName(review.getReviewer().getName())
                        .reviewDate(review.getCreatedAt())
                        .build())
                .toList();

        return ReviewListResponseDto.builder()
                .reviewList(items)
                .build();
    }
}
