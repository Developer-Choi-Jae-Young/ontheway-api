package com.ontheway.service;

import com.ontheway.dto.request.ReportBoardRequestDto;
import com.ontheway.dto.request.ReportUserRequestDto;
import com.ontheway.dto.response.ReportBoardResponseDto;
import com.ontheway.dto.response.ReportUserResponseDto;
import com.ontheway.entity.Report;
import com.ontheway.entity.User;
import com.ontheway.enums.ReportCategory;
import com.ontheway.enums.ReportEntityType;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.repository.DeliveryRepository;
import com.ontheway.repository.ProductRepository;
import com.ontheway.repository.ReportRepository;
import com.ontheway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public ReportUserResponseDto reportUser(Long reporterId, ReportUserRequestDto dto) {
        User reporter = userRepository.findByIdAndDeletedAtIsNull(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (dto.getUserId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        if (dto.getUserId().equals(reporterId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        userRepository.findByIdAndDeletedAtIsNull(dto.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Set<ReportCategory> categories = validateCategories(dto.getCategories(), ReportEntityType.USER);
        validateContent(dto.getContent());

        Report report = Report.builder()
                .reporter(reporter)
                .entityType(ReportEntityType.USER)
                .entityId(dto.getUserId())
                .content(dto.getContent())
                .categories(categories)
                .build();
        reportRepository.save(report);

        return ReportUserResponseDto.builder()
                .reportDate(LocalDateTime.now())
                .build();
    }

    @Transactional
    public ReportBoardResponseDto reportBoard(Long reporterId, ReportBoardRequestDto dto) {
        User reporter = userRepository.findByIdAndDeletedAtIsNull(reporterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (dto.getBoardId() == null || dto.getBoardType() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        switch (dto.getBoardType()) {
            case PRODUCT -> productRepository.findByIdAndDeletedAtIsNull(dto.getBoardId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
            case DELIVERY -> deliveryRepository.findByIdAndDeletedAtIsNull(dto.getBoardId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));
            case USER -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Set<ReportCategory> categories = validateCategories(dto.getCategories(), dto.getBoardType());
        validateContent(dto.getContent());

        Report report = Report.builder()
                .reporter(reporter)
                .entityType(dto.getBoardType())
                .entityId(dto.getBoardId())
                .content(dto.getContent())
                .categories(categories)
                .build();
        reportRepository.save(report);

        return ReportBoardResponseDto.builder()
                .reportDate(LocalDateTime.now())
                .build();
    }

    private Set<ReportCategory> validateCategories(Set<ReportCategory> categories, ReportEntityType entityType) {
        if (categories == null || categories.isEmpty()) {
            throw new BusinessException(ErrorCode.REPORT_CATEGORY_REQUIRED);
        }
        if (categories.size() > Report.MAX_CATEGORIES) {
            throw new BusinessException(ErrorCode.REPORT_CATEGORY_LIMIT_EXCEEDED);
        }
        boolean allApplicable = categories.stream().allMatch(category -> category.appliesTo(entityType));
        if (!allApplicable) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return categories;
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.REPORT_CONTENT_REQUIRED);
        }
    }
}