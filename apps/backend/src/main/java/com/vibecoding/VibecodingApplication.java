package com.vibecoding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 바이브코딩 데모 프로젝트 메인 애플리케이션 클래스.
 *
 * <p>Spring Boot 기반의 코드 실행 및 공유 서비스를 제공합니다.
 * 주요 기능:
 * - 다중 언어 코드 실행 (JavaScript, Python, Java)
 * - 코드 저장 및 공유
 * - 실행 결과 관리
 * - 사용자별 코드 히스토리
 * </p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class VibecodingApplication {

    /**
     * 애플리케이션 시작점.
     *
     * @param args 명령줄 인수
     */
    public static void main(final String[] args) {
        SpringApplication.run(VibecodingApplication.class, args);
    }
}