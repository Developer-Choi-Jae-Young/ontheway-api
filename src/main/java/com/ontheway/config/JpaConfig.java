package com.ontheway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * {@code @CreatedDate} 와 {@code @LastModifiedDate} 를 켠다. 없으면 생성/수정 시각이
 * 예외 없이 null 로 들어간다.
 *
 * 타임존은 여기서 안 잡는다. JVM 전역 설정이라 빈 초기화 순서에 기대면 안 되고,
 * {@code OnthewayApplication.main()} 이 스프링 기동 전에 고정한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
