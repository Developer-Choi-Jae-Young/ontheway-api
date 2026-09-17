package com.ontheway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * {@code @CreatedDate} / {@code @LastModifiedDate} 활성화.
 *
 * <p>타임존 고정은 여기 있지 않다 — JVM 전역 설정이라 빈 초기화 순서에 기대면 안 되고,
 * {@code OnthewayApplication.main()} 이 스프링 기동 전에 잡는다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
