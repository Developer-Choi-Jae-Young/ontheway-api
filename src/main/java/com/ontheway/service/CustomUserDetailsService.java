package com.ontheway.service;

import com.ontheway.entity.User;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String accountId) throws UsernameNotFoundException {
        User user = userRepository.findByAccountIdAndDeletedAtIsNull(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + accountId));

        return new CustomUserDetails(user);
    }
}
