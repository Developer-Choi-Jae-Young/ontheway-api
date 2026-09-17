package com.ontheway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class OnthewayApplication {

	public static void main(String[] args) {
		// 시각 컬럼이 전부 타임존 없는 TIMESTAMP 라, JVM 기본 존이 곧 저장되는 값의 의미가 된다.
		// EC2 기본값은 UTC 라서 그대로 두면 아홉 시간 어긋난다.
		// @PostConstruct 로 바꾸면 그보다 먼저 뜬 빈이 계산한 시각은 이미 UTC 라서,
		// 스프링이 뜨기 전인 여기서 고정한다.
		// 운영 systemd 유닛의 ExecStart 에 -Duser.timezone=Asia/Seoul 을 같이 주면 더 안전하다.
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(OnthewayApplication.class, args);
	}

}
