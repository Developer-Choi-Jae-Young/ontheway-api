package com.ontheway.service;

import com.ontheway.dto.request.*;
import com.ontheway.dto.response.*;
import com.ontheway.entity.User;
import com.ontheway.enums.EmailPurpose;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.global.util.TempPasswordGenerator;
import com.ontheway.global.util.VerificationKeyUtil;
import com.ontheway.infra.cache.RefreshTokenStore;
import com.ontheway.infra.cache.VerificationCodeStore;
import com.ontheway.infra.mail.MailSender;
import com.ontheway.repository.RatingSummary;
import com.ontheway.repository.ReviewRepository;
import com.ontheway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String SIGNUP_PREFIX = "SIGNUP:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeStore codeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final MailSender mailSender;
    private final ReviewRepository reviewRepository;

    //아이디 중복 확인
    @Transactional
    public boolean isDuplicated(MemberCheckIdReqeustDto dto) {
        return userRepository.existsByAccountId(dto.getUserId());
    }

    //회원 가입
    @Transactional
    public void signup(MemberSaveRequestDto dto) {
        String verifyKey = VerificationKeyUtil.of(EmailPurpose.SIGN_UP, dto.getEmail());

        if (userRepository.existsByAccountId(dto.getUserId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (!codeStore.isVerified(verifyKey)) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        User user = User.builder()
                .accountId(dto.getUserId())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .name(dto.getUserName())
                .nickname(dto.getNickName())
                .birthDate(LocalDate.parse(dto.getBirthday()))
                .termsAgreedAt(LocalDateTime.now())
                .build();
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        codeStore.removeVerified(verifyKey);
    }

    //내 정보 조회
    public MemberDetailResponseDto getInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return MemberDetailResponseDto.builder()
                .userId(user.getAccountId())
                .email(user.getEmail())
                .nickName(user.getNickname())
                .birthday(String.valueOf(user.getBirthDate()))
                .build();
    }

    //아이디 찾기
    public MemberFindIdResponseDto findId(MemberFindIdRequestDto dto) {
        String verifyKey = VerificationKeyUtil.of(EmailPurpose.FIND_ID, dto.getEmail());

        if (!codeStore.isVerified(verifyKey)) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        User user = userRepository.findByEmailAndDeletedAtIsNull(dto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        codeStore.removeVerified(verifyKey);

        return MemberFindIdResponseDto.builder()
                .accountId(user.getAccountId())
                .build();
    }

    //비밀번호 찾기(재설정)
    @Transactional
    public MemberFindPasswordResponseDto resetPassword(MemberFindPasswordRequestDto dto) {
        User user = userRepository.findByAccountIdAndEmailAndDeletedAtIsNull(dto.getAccountId(), dto.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String tempPassword = TempPasswordGenerator.generate();
        user.changePassword(passwordEncoder.encode(tempPassword));

        mailSender.send(user.getEmail(), "[OnTheWay] 임시 비밀번호 안내",
                "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경해주세요.");

        refreshTokenStore.delete(user.getAccountId());

        return MemberFindPasswordResponseDto.builder()
                .message("임시 비밀번호가 이메일로 발송되었습니다.")
                .createdAt(LocalDateTime.now())
                .build();
    }

    //내 정보 수정
    @Transactional
    public MemberUpdateInfoResponseDto updateInfo(Long accountId, MemberUpdateInfoRequestDto dto, String imageUrl) {
        User user = userRepository.findByIdAndDeletedAtIsNull(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (dto.getNickName() != null && !dto.getNickName().isBlank()) {
            user.changeNickname(dto.getNickName());
        }

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.changePassword(passwordEncoder.encode(dto.getNewPassword()));
            refreshTokenStore.delete(user.getAccountId());
        }

        if (dto.getNewEmail() != null && !dto.getNewEmail().isBlank()) {
            String verifyKey = VerificationKeyUtil.of(EmailPurpose.CHANGE_EMAIL, dto.getNewEmail());

            if (!codeStore.isVerified(verifyKey)) {
                throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
            }
            if (userRepository.existsByEmail(dto.getNewEmail())) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }

            user.changeEmail(dto.getNewEmail());
            codeStore.removeVerified(verifyKey);
        }

        if (dto.getNewBirthday() != null && !dto.getNewBirthday().isBlank()) {
            user.changeBirthDate(LocalDate.parse(dto.getNewBirthday()));
        }

        if (imageUrl != null) {
            user.changeProfileImage(imageUrl);
        }

        return MemberUpdateInfoResponseDto.builder()
                .nickName(user.getNickname())
                .email(user.getEmail())
                .birthday(String.valueOf(user.getBirthDate()))
                .profileImageUrl(user.getProfileImageUrl())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    //회원 탈퇴
    @Transactional
    public MemberDeleteAccountResponseDto withdraw(Long userId, MemberDeleteAccountRequestDto dto) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        user.withdraw(LocalDateTime.now());
        refreshTokenStore.delete(user.getAccountId());

        return MemberDeleteAccountResponseDto.builder()
                .createdAt(LocalDateTime.now())
                .build();
    }

    //내 평균 만족도 조회
    public MemberRatingResponseDto getRatings(Long userId) {
        RatingSummary summary = reviewRepository.findRatingSummary(userId);

        BigDecimal averageRating = summary.getAverageRating() != null
                ? BigDecimal.valueOf(summary.getAverageRating()).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(1);
        long reviewCount = summary.getReviewCount() != null ? summary.getReviewCount() : 0L;

        return MemberRatingResponseDto.builder()
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .build();
    }
}
